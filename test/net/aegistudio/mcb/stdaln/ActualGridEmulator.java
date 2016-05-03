package net.aegistudio.mcb.stdaln;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import net.aegistudio.mcb.ComponentFactory;
import net.aegistudio.mcb.Data;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.board.ActualGrid;
import net.aegistudio.mcb.board.SpectatedActualGrid;
import net.aegistudio.mcb.layout.LayoutGrid;
import net.aegistudio.mcb.layout.SpectatedLayoutGrid;
import net.aegistudio.mpp.Interaction;

public class ActualGridEmulator extends AwtGridComponent {
	private static final long serialVersionUID = 1L;
	
	public ActualGridEmulator(ActualGrid grid) {
		super(grid);
		
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				manipulate(me.getPoint(), (x, y, cell) -> {
					boolean right = me.getButton() == MouseEvent.BUTTON3;
					if(cell != null) cell.getComponent().interact(cell, 
								new Interaction(x, y, null, null, null, right));
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
						StringBuilder textBuilder = new StringBuilder();
						String name = cell.getComponent().getClass().getTypeName();
						textBuilder.append(name.substring(1 + name.lastIndexOf('.')));
						textBuilder.append(" (" + cell.getColumn() + ", " + cell.getRow() + ")");
						textBuilder.append("\n");
						for(Facing facing : Facing.values()) {
							textBuilder.append(facing.name().charAt(0) + ":");
							textBuilder.append(cell.getLevel(facing));
							textBuilder.append(' ');
						}
						
						Object data = cell.getData(Data.class);
						if(data != null) {
							textBuilder.append("\n");
							textBuilder.append("Data: ");
							textBuilder.append(data.toString());
						}
						text = new String(textBuilder);
					}
					repaint();
				});
			}
		});
	}
	
	static ActualGridEmulator gridComponent;
	static JLabel noEmulation;
	static ComponentFactory table = new ComponentFactory();
	static void resetGridComponent(JFrame frame, ActualGridEmulator newComponent) {
		if(gridComponent != null) frame.remove(gridComponent);
		else frame.remove(noEmulation);
		gridComponent = newComponent;
		gridComponent.setLocation(0, 0);
		frame.add(gridComponent);
		frame.setSize(gridComponent.getWidth(), gridComponent.getHeight() 
				+ frame.getJMenuBar().getPreferredSize().height);
		frame.repaint();
	}
	
	public void tick() {
		grid.tick(null);
		grid.paint(paintable);
		repaint();
	}
	
	static int interval = 10;
	static Thread tickThread;
	
	public static void main(String[] arguments) {
		try {UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");} catch(Exception e) {}
		JFrame frame = new JFrame("Emulation");
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				if(gridComponent != null) 
					gridComponent.tick();
			}
		});
		
		JMenuBar menubar = new JMenuBar();
		frame.setJMenuBar(menubar);
		
		JMenu file = new JMenu("File");
		JFileChooser layoutChooser = new JFileChooser(); {
			layoutChooser.setFileFilter(new FileFilter() {
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
		
		JMenuItem newEmulation = new JMenuItem("New Emulation");
		newEmulation.addActionListener(a -> {
			if(JFileChooser.APPROVE_OPTION == layoutChooser.showOpenDialog(frame)) try {
				FileInputStream input = new FileInputStream(layoutChooser.getSelectedFile());
				//LayoutGrid grid = new LayoutGrid();	grid.load(input, table);
				LayoutGrid grid = new SpectatedLayoutGrid();	grid.load(input, table);
				
				ActualGrid actualGrid = new SpectatedActualGrid(grid);
				resetGridComponent(frame, new ActualGridEmulator(actualGrid));
			}
			catch(Exception e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), 
						"Fail to open layout!", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(newEmulation);
		
		file.addSeparator();
		JFileChooser chooser = new JFileChooser(); {
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File arg0) {
					if(arg0.isDirectory()) return true;
					return arg0.getName().endsWith(".emu");
				}

				@Override
				public String getDescription() {
					return "Emulation File (*.emu)";
				}
			});
		}
		
		JMenuItem openFile = new JMenuItem("Open Layout");
		openFile.addActionListener(a -> {
			if(JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(frame)) try {
				FileInputStream input = new FileInputStream(chooser.getSelectedFile());
				
				LayoutGrid grid = new SpectatedLayoutGrid();
				grid.load(input, table);
				
				ActualGrid actualGrid = new SpectatedActualGrid(grid);
				actualGrid.load(input, table);
				
				resetGridComponent(frame, new ActualGridEmulator(actualGrid));
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
				if(!save.getName().endsWith(".emu")) {
					save = new File(save.getParentFile(), save.getName().concat(".emu"));
					chooser.setSelectedFile(save);
				}
				
				if(save.exists()) {
					if(JOptionPane.showConfirmDialog(frame, "File already exists, are you sure to replace?")
							!= JOptionPane.YES_OPTION) return;
				}
				FileOutputStream output = new FileOutputStream(save);
				((ActualGrid)gridComponent.grid).layout.save(output, table);
				gridComponent.grid.save(output, table);
			}
			catch(Exception e) {
				JOptionPane.showMessageDialog(frame, e.getMessage(), 
						"Fail to save!", JOptionPane.ERROR_MESSAGE);
			}
		});
		file.add(saveFile);
		saveFile.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
		menubar.add(file);
		
		JMenu emulation = new JMenu("Emulation");
		JCheckBoxMenuItem autoTick = new JCheckBoxMenuItem("Automatically Tick");
		autoTick.addActionListener((a) -> {
			if(tickThread == null) {
				tickThread = new Thread(() -> {
					while(true) try {
						if(gridComponent != null) 
							gridComponent.tick();
						frame.repaint();
						Thread.sleep(interval);
					}
					catch(InterruptedException e) {
						break;
					}
					catch(Exception e) {	e.printStackTrace();	}
				});
				tickThread.start();
			}
			else {
				tickThread.interrupt();
				tickThread = null;
			}
		});
		autoTick.setAccelerator(KeyStroke.getKeyStroke("F5"));
		emulation.add(autoTick);
		menubar.add(emulation);
		
		JMenu help = new JMenu("Help");
		JMenuItem instruction = new JMenuItem("Instruction");
		instruction.addActionListener(e -> {
			JOptionPane.showMessageDialog(frame, "<html><p>Please follow the instruction:</p>"
				+ "<li><b>Left/Right Click</b>: Interact with pointing component"
				+ "<li><b>Space</b>: tick current circuit board"
				+ "</html>", "Usage", JOptionPane.PLAIN_MESSAGE);
		});
		instruction.setAccelerator(KeyStroke.getKeyStroke("F1"));
		help.add(instruction);
		
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(e -> {
			JOptionPane.showMessageDialog(frame, "<html><b>Map Circuit Board</b> - Emulator (beta)"
				+ "<br><b>Author</b>: aegistudio"
				+ "<br><b>Purpose</b>:<br>"
				+ "<li>interactive test of actual circuit board"
				+ "<li>validate circuit board design</html>", 
				"About", JOptionPane.INFORMATION_MESSAGE);
		});
		help.add(about);
		menubar.add(help);

		frame.setSize(500, 500);
		
		noEmulation = new JLabel("<html><center><h1>No emulation.</h1>Please use <b>File -> New Emulation</b> to create a emulation!</center></html>");
		noEmulation.setSize(frame.getSize());
		noEmulation.setHorizontalAlignment(JLabel.CENTER);
		frame.add(noEmulation);
		
		frame.setVisible(true);
	}
}
