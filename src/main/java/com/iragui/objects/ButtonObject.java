package com.iragui.objects;

import org.lwjgl.glfw.GLFW;

import com.iragui.GUI;

public class ButtonObject extends AnimationObject {
	
	public ButtonObject(String name, int layer, GUI gui, int x, int y, int sizeX, int sizeY, boolean nearestFilter,
			boolean rgba,WrappedBufferedImage...images) {
		super(name,layer,gui,x,y,sizeX,sizeY,nearestFilter,rgba,false,false,true,images);
	}

	private boolean disabled=false;
	
	public boolean isEnabled() {
		return !this.disabled;
	}
	
	public void disable() {
		this.disabled=true;
	}
	
	public void enable() {
		this.disabled=false;
	}

	@Override
	public void sendKey(int key, int action) {}

	private boolean inBounds=false;
	
	public boolean inBounds() {
		return this.inBounds;
	}
	
	@Override
	public void sendMousePos(long window, double xPos, double yPos) {
		
		
		if(!this.getAnyFrameVisible() || disabled) {
			return;
		}
		
		yPos=gui.getWindow().getSizeY()-yPos;
		
		if(!this.interactionInVisibleBounds((int)xPos,(int)yPos)) {
			this.inBounds=false;
			this.pressing=false;
			this.setFrame(0);
			return;
		}
		
		// TODO Auto-generated method stub
		if(xPos>=this.getX() && xPos<=(this.getX()+this.sizeX) &&
		   yPos>=this.getY() && yPos<=(this.getY()+this.sizeY)) {
			
			if(!this.pressing) {
				this.setFrame(1);
			}
			this.inBounds=true;
		} else {
			this.setFrame(0);
			this.inBounds=false;
			this.pressing=false;
		}
	}

	private boolean pressing=false;
	@Override
	public void sendMouseButton(long window, int button, int action, int mods) {
		// TODO Auto-generated method stub
		if(disabled) {
			return;
		}
		if(!this.getAnyFrameVisible()) {
			return;
		}
		
		if(inBounds && button==GLFW.GLFW_MOUSE_BUTTON_1) {
			if(action==GLFW.GLFW_PRESS) {
				this.setFrame(2);
				this.pressing=true;
			} else if(action==GLFW.GLFW_RELEASE) {
				this.pressed=true;
				this.setFrame(1);
				this.pressing=false;
			}
		}
	}
	
	public boolean getInBounds() {
		return this.inBounds;
	}
	
	private boolean pressed=false;
	public boolean readPress() {
		boolean pressed=this.pressed;
		this.pressed=false;
		return pressed;
	}

	@Override
	public void sendMouseScroll(long window, double xOffset, double yOffset) {}

	@Override
	public void update(boolean showFrame) {}
	
}
