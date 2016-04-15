package net.aegistudio.mcb.stdaln;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import net.aegistudio.mcb.Cell;
import net.aegistudio.mcb.Grid;

public class AwtGridComponent extends Component{
	private static final long serialVersionUID = 1L;
	
	protected final Grid grid;
	protected final AwtPaintable paintable;
	public AwtGridComponent(Grid grid) {
		this.grid = grid;
		this.paintable = new AwtPaintable();
		this.setSize(paintable.width(), paintable.height());
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
}
