package net.aegistudio.mcb.stdaln;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import net.aegistudio.mcb.Air;
import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.ComponentFactory;
import net.aegistudio.mcb.Facing;
import net.aegistudio.mcb.Grid;
import net.aegistudio.mcb.layout.LayoutUnitCell;
import net.aegistudio.mcb.layout.LayoutWireCell;
import net.aegistudio.mpp.Interaction;

public class AwtGridComponent extends Component{
	private static final long serialVersionUID = 1L;
	
	private final Grid grid;
	private final AwtPaintable paintable;
	public AwtGridComponent(Grid grid) {
		this.grid = grid;
		this.paintable = new AwtPaintable();
		this.setSize(paintable.width(), paintable.height());
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
	
	public interface Manipulator { public void manipulate(int x, int y, Cell cell);	}
	
	public void manipulate(Point location, Manipulator manipulator) {
		int x = (int) (location.getX() / paintable.size);
		int y = (int) (location.getY() / paintable.size);
		Cell cell = grid.getCell(y / 4, x / 4);
		manipulator.manipulate(x, y, cell);
	}
	
	public int currentX, currentY;
	public String text = null;
	
	public void paint(Graphics g) {
		paintable.setGraphics(g);
		this.grid.paint(paintable);
		if(text != null) {
			String[] lines = text.split("\n");
			int width = 0;			int height = 0;
			for(String line : lines) {
				Rectangle2D dimension = g.getFontMetrics().getStringBounds(line, g);
				if(dimension.getHeight() > height) height = (int) dimension.getHeight();
				if(dimension.getWidth() > width) width = (int) dimension.getWidth();
			}
			int padding = 2;
			int totalHeight = (int) (lines.length * height) + 2 * padding;
			int baseY = Math.max(0, currentY - totalHeight);
			g.setColor(Color.WHITE);
			g.fillRect(currentX, baseY,  width + 2 * padding, totalHeight);
			g.setColor(Color.BLACK);
			g.drawRect(currentX, baseY, width + 2 * padding, totalHeight);
			for(int i = 0; i < lines.length; i ++) 
				g.drawString(lines[i], currentX + padding, (int) (baseY + (1 + i) * height) + padding);
		}
	}
	
	net.aegistudio.mcb.Component component = Air.INSTANCE;
	public void tool(net.aegistudio.mcb.Component component) {
		this.component = component;
	}
}
