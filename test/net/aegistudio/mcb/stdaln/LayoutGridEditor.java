package net.aegistudio.mcb.stdaln;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.aegistudio.mcb.Air;
import net.aegistudio.mcb.ComponentFactory;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.layout.LayoutGrid;
import net.aegistudio.mcb.layout.LayoutUnitCell;
import net.aegistudio.mcb.layout.LayoutWireCell;
import net.aegistudio.mpp.Interaction;

public class LayoutGridEditor extends AwtGridComponent {
	private static final long serialVersionUID = 1L;
	
	public LayoutGridEditor(LayoutGrid grid) {
		super(grid);
		
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				manipulate(me.getPoint(), (x, y, cell) -> {
					boolean right = me.getButton() == MouseEvent.BUTTON3;
					if(!right) 
						grid.setCell(y / 4, x / 4, component);
					else if(cell != null) {
						if(right) cell.getComponent().interact(cell, 
								new Interaction(x, y, null, null, null, right));
					}
					repaint();
				});
			}
		});
		
		this.addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent me) {
				manipulate(me.getPoint(), (x, y, cell) -> {
					text = null;
					currentX = me.getX();
					currentY = me.getY();
					
					if(cell != null) {
						if(cell instanceof LayoutWireCell) {
							StringBuilder textBuilder = new StringBuilder();
							textBuilder.append("LayoutWire: "); 
							String name = cell.getComponent().getClass().getTypeName();
							textBuilder.append(name.substring(1 + name.lastIndexOf('.')));
							textBuilder.append(" (" + cell.getRow() + ", " + cell.getColumn() + ")");
							LayoutWireCell wire = (LayoutWireCell) cell;
							for(LayoutUnitCell adjacence : wire.allAdjacentUnit()) {
								textBuilder.append("\n");
								for(Facing facing : Facing.values()) {
									short distance = wire.getDistance(adjacence, facing);
									textBuilder.append(facing.name().charAt(0) + ":");
									textBuilder.append(distance == Short.MAX_VALUE? "N" : distance);
									textBuilder.append(' ');
								}
								textBuilder.append("-> (" + adjacence.getRow() + ", " + adjacence.getColumn() + ")");
							}
							
							text = new String(textBuilder);
						}
						else if(cell instanceof LayoutUnitCell) {
							StringBuilder textBuilder = new StringBuilder();
							textBuilder.append("LayoutUnit: ");
							String name = cell.getComponent().getClass().getTypeName();
							textBuilder.append(name.substring(1 + name.lastIndexOf('.')));
							textBuilder.append(" (" + cell.getRow() + ", " + cell.getColumn() + ")");
							Object data = cell.getData(Cloneable.class);
							if(data != null) {
								textBuilder.append("\n");
								textBuilder.append("Data: ");
								textBuilder.append(data.toString());
							}
							text = new String(textBuilder);
						}
					}
					repaint();
				});
			}
		});
		
		this.addMouseWheelListener((MouseWheelEvent e) -> {
			int rotation = e.getWheelRotation();
			List<net.aegistudio.mcb.Component> components = ComponentFactory.all();
			this.tool(ComponentFactory.get((ComponentFactory.id(component) 
					+ (rotation > 0? 1 : components.size() - 1)) % components.size()));
			currentX = e.getX();
			currentY = e.getY();
			text = component.getClass().getName();
			text = text.substring(text.lastIndexOf('.') + 1);
			text = "Tool: " + text;
			repaint();
		});
	}
	
	net.aegistudio.mcb.Component component = Air.INSTANCE;
	public void tool(net.aegistudio.mcb.Component component) {
		this.component = component;
	}
	
	static LayoutGridEditor gridComponent;
	static void resetGridComponent(JFrame frame, LayoutGridEditor newComponent) {
		if(gridComponent != null) frame.remove(gridComponent);
		gridComponent = newComponent;
		gridComponent.setLocation(0, 0);
		frame.add(gridComponent);
		frame.setSize(gridComponent.getWidth(), gridComponent.getHeight());
		frame.repaint();
	}
	
	public static void main(String[] arguments) {
		try {UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");} catch(Exception e) {}
		JFrame frame = new JFrame("Layout");
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		resetGridComponent(frame, new LayoutGridEditor(new LayoutGrid()));
		
		JMenuBar menubar = new JMenuBar();
		frame.setJMenuBar(menubar);
		
		JMenu file = new JMenu("File");
		JMenuItem newFile = new JMenuItem("New");
		newFile.addActionListener(a -> {
			resetGridComponent(frame, new LayoutGridEditor(new LayoutGrid()));
		});
		file.add(newFile);
		file.addSeparator();
		
		JFileChooser chooser = new JFileChooser(); {
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File arg0) {
					if(arg0.isDirectory()) return true;
					return arg0.getName().endsWith(".lyt");
				}

				@Override
				public String getDescription() {
					return "Layout File (*.lyt)";
				}
			});
		}
		
		JMenuItem openFile = new JMenuItem("Open Layout");
		openFile.addActionListener(a -> {
			if(JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(frame)) try {
				FileInputStream input = new FileInputStream(chooser.getSelectedFile());
				LayoutGrid grid = new LayoutGrid();	grid.load(input);
				resetGridComponent(frame, new LayoutGridEditor(grid));
			}
			catch(Exception e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), 
						"Fail to open!", JOptionPane.ERROR_MESSAGE);
			}
		});
		openFile.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
		file.add(openFile);
		
		JMenuItem saveFile = new JMenuItem("Save Layout");
		saveFile.addActionListener(a -> {
			if(JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(frame)) try {
				File save = chooser.getSelectedFile();
				if(!save.getName().endsWith(".lyt")) {
					save = new File(save.getParentFile(), save.getName().concat(".lyt"));
					chooser.setSelectedFile(save);
				}
				
				if(save.exists()) {
					if(JOptionPane.showConfirmDialog(frame, "File already exists, are you sure to replace?")
							!= JOptionPane.YES_OPTION) return;
				}
				FileOutputStream output = new FileOutputStream(save);
				gridComponent.grid.save(output);
			}
			catch(Exception e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), 
						"Fail to save!", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(saveFile);
		saveFile.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		menubar.add(file);
		
		JMenu help = new JMenu("Help");
		JMenuItem instruction = new JMenuItem("Instruction");
		instruction.addActionListener(e -> {
			JOptionPane.showMessageDialog(frame, "<html><p>Please follow the instruction:</p>"
				+ "<li><b>Left Click</b>: Place or remove components"
				+ "<li><b>Right Click</b>: Interact with pointing component"
				+ "<li><b>Scroll Up/Down</b>: Select previous/next component"
				+ "</html>", "Usage", JOptionPane.PLAIN_MESSAGE);
		});
		instruction.setAccelerator(KeyStroke.getKeyStroke("F1"));
		help.add(instruction);
		
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(e -> {
			JOptionPane.showMessageDialog(frame, "<html><b>Map Circuit Board</b> - Layout Debugger (beta)"
				+ "<br><b>Author</b>: aegistudio"
				+ "<br><b>Purpose</b>:<br>"
				+ "<li>interactive test of layout"
				+ "<li>design layout</html>", 
				"About", JOptionPane.INFORMATION_MESSAGE);
		});
		help.add(about);
		menubar.add(help);
		
		frame.setVisible(true);
	}
}
