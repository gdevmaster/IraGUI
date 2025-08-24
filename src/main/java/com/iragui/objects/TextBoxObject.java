package com.iragui.objects;

import java.awt.Color;
import java.awt.Font;
import java.util.TreeMap;

import com.iragui.GUI;

public class TextBoxObject extends GUIObject {
	
	
	protected TreeMap<Integer,TextObject> lines;
	
	private int lineSpacing=0;

	public TextBoxObject(String name, int layer, GUI gui, int x, int y, int sizeX, int sizeY, boolean nearestFilter,
			boolean rgba, int lineSpacing) {
		super(name, layer, gui, x, y, sizeX, sizeY, nearestFilter, rgba, false, false, false);
		// TODO Auto-generated constructor stub
		
		lines = new TreeMap<>();
		
		this.lineSpacing=lineSpacing;
	}
	
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
	
	public int getLineSpacing() {
		return this.lineSpacing;
	}
	
	public void setLineSpacing(int space) {
		this.lineSpacing=space;
	}
	
	@Override
	public void setLayer(int layer) {
		super.setLayer(layer);
		for(Integer line : lines.keySet()) {
			lines.get(line).setLayer(layer);
		}
	}
	
	@Override
	public void destroyObject() {
		for(Integer line : lines.keySet()) {
			lines.get(line).destroyObject();
		}
		super.destroyObject();
	}
	
	@Override
	public void setX(int x) {
		this.x=x+2;
		for(Integer line : lines.keySet()) {
			lines.get(line).x=x+2;
		}
	}
	
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
	public void update(boolean showFrame) {
		
	}

	@Override
	public void sendKey(int key, int action) {
		
	}

	@Override
	public void sendMousePos(long window, double xPos, double yPos) {
		
	}

	@Override
	public void sendMouseButton(long window, int button, int action, int mods) {
		
		
	}

	@Override
	public void sendMouseScroll(long window, double xOffset, double yOffset) {
		
	}

	public TreeMap<Integer, TextObject> getLines() {
		return this.lines;
	}
	
}
