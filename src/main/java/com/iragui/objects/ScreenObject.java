package com.iragui.objects;

import com.iragui.GUI;

public class ScreenObject extends GUIObject{

	public ScreenObject(String name, int layer, GUI gui, int x, int y, int sizeX, int sizeY, boolean nearestFilter,
			boolean rgba, boolean preserveTextureIDOverride) {
		super(name, layer, gui, x, y, sizeX, sizeY, nearestFilter, rgba);
		this.setPreserveTextureIDOverride(preserveTextureIDOverride);
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
}
