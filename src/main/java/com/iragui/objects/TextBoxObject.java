package com.iragui.objects;

import java.awt.Color;
import java.awt.Font;
import java.util.TreeMap;

import com.iragui.GUI;

/**
 * A {@code TextBoxObject} represents a multi-line text container within the GUI.
 * <p>
 * Each line of text is internally represented as a {@link TextObject} and stored in a {@link TreeMap}.
 * The text box manages positioning, spacing between lines, and propagates updates 
 * (such as position, limits, and layer changes) to its child {@code TextObject}s.
 * </p>
 */
public class TextBoxObject extends GUIObject {
	
	 /** Holds the lines of text, ordered by index. */
	protected TreeMap<Integer,TextObject> lines;
	
	  /** Spacing (in pixels) between lines of text. */
	private int lineSpacing=0;

	/**
     * Constructs a new {@code TextBoxObject}.
     *
     * @param name          the unique object name
     * @param layer         the rendering layer
     * @param gui           the parent GUI
     * @param x             the x-position
     * @param y             the y-position
     * @param sizeX         the width of the text box
     * @param sizeY         the height of the text box
     * @param nearestFilter whether to use nearest-neighbor filtering
     * @param rgba          whether to use RGBA textures
     * @param lineSpacing   vertical spacing between lines
     */
	public TextBoxObject(String name, int layer, GUI gui, int x, int y, int sizeX, int sizeY, boolean nearestFilter,
			boolean rgba, int lineSpacing) {
		super(name, layer, gui, x, y, sizeX, sizeY, nearestFilter, rgba, false, false, false);
		// TODO Auto-generated constructor stub
		
		lines = new TreeMap<>();
		
		this.lineSpacing=lineSpacing;
	}
	
	 /**
     * Appends a new line of text to this text box.
     *
     * @param text          the string content
     * @param font          the font used to render the text
     * @param color         the text color
     * @param bkgColor      the background color
     * @param rgba          whether the text uses RGBA
     * @param nearestFilter whether to use nearest-neighbor filtering
     * @param antiAliasing  whether to apply anti-aliasing
     */
	public void appendLine(String text, 
						   Font font, 
						   Color color, 
						   Color bkgColor, 
						   boolean rgba, 
						   boolean nearestFilter,
						   boolean antiAliasing) {
		
		int line = lines.size();
		TextObject tO = new TextObject(name+":textObject:"+line,
				this.getLayer(),
				this.gui,
				0, 
				0, 
				nearestFilter, 
				rgba, 
				text, 
				font, 
				color, 
				bkgColor, 
				antiAliasing);
		tO.setVisible();
		lines.put(line,tO);
	}
	
	/** @return the vertical spacing between lines. */
	public int getLineSpacing() {
		return this.lineSpacing;
	}
	
	 /**
     * Sets the vertical spacing between lines.
     *
     * @param space the line spacing in pixels
     */
	public void setLineSpacing(int space) {
		this.lineSpacing=space;
	}
	
	/** {@inheritDoc} */
	@Override
	public void setLayer(int layer) {
		super.setLayer(layer);
		for(Integer line : lines.keySet()) {
			lines.get(line).setLayer(layer);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void destroyObject() {
		for(Integer line : lines.keySet()) {
			lines.get(line).destroyObject();
		}
		super.destroyObject();
	}
	
	/** {@inheritDoc} */
	@Override
	public void setX(int x) {
		this.x=x+2;
		for(Integer line : lines.keySet()) {
			lines.get(line).x=x+2;
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void setY(int y) {
		this.y=y;
		int nextLength=0;
		for(Integer line : lines.keySet()) {
			lines.get(line).y=((y+this.sizeY)-lines.get(line).sizeY)-nextLength;
			nextLength+=(lines.get(line).sizeY-(lines.get(line).sizeY*0.5))+lineSpacing;
		}
	}
	
	@Override
	public void setLimitX(int x) {
		super.setLimitX(x);
		for(Integer line : lines.keySet()) {
			lines.get(line).setLimitX(x);
		}
	}
	
	@Override
	public void setMinX(int x) {
		super.setMinX(x);
		for(Integer line : lines.keySet()) {
			lines.get(line).setMinX(x);
		}
	}
	
	@Override
	public void setMinY(int y) {
		super.setMinY(y);
	
		for(Integer line : lines.keySet()) {
			lines.get(line).setMinY(y);
			
		}
	}
	
	@Override
	public void setLimitY(int y) {
		super.setLimitY(y);
		
		for(Integer line : lines.keySet()) {
			lines.get(line).setLimitY(y);
		}
	}
	
	@Override
	public void setWinLimitX(int x) {
		super.setWinLimitX(x);
		for(Integer line : lines.keySet()) {
			lines.get(line).setWinLimitX(x);
		}
	}
	@Override
	public void setWinLimitY(int y) {
		super.setWinLimitY(y);
		for(Integer line : lines.keySet()) {
			lines.get(line).setWinLimitY(y);
		}
	}
	@Override
	public void setWinMinX(int x) {
		super.setWinMinX(x);
		for(Integer line : lines.keySet()) {
			lines.get(line).setWinMinX(x);
		}
	}
	@Override
	public void setWinMinY(int y) {
		super.setWinMinY(y);
		for(Integer line : lines.keySet()) {
			lines.get(line).setWinMinY(y);
		}
	}

	@Override
	public void update(boolean showFrame) {	}
	@Override
	public void sendKey(int key, int action) {}
	@Override
	public void sendMousePos(long window, double xPos, double yPos) {}
	@Override
	public void sendMouseButton(long window, int button, int action, int mods) {}
	@Override
	public void sendMouseScroll(long window, double xOffset, double yOffset) {}

	/** @return all text lines as a {@link TreeMap}. */
	public TreeMap<Integer, TextObject> getLines() {
		return this.lines;
	}
	
}
