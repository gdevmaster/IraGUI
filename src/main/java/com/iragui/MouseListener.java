package com.iragui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;

import com.iragui.objects.GUIObject;



public class MouseListener {
	private double scrollX,scrollY;
	private double xPos,yPos,lastX,lastY;
	private boolean[] mouseButtonPressed = new boolean[3];
	private boolean isDragging;
	private GUI gui;
	
	private HashMap<String,GUIObject> callbacks;
	
	public void add(GUIObject obj) {
		this.callbacks.put(obj.name,obj);
	}
	public void remove(GUIObject obj) {
		this.callbacks.remove(obj.name);
	}
	
	public MouseListener(GUI gui) {
		this.scrollX=0.0;
		this.scrollY=0.0;
		this.xPos=0.0;
		this.yPos=0.0;
		this.lastX=0.0;
		this.lastY=0.0;
		this.gui=gui;
		this.callbacks=new HashMap<>();
	}
	
	public void mousePosCallback(long window, double xPos, double yPos) {
		this.lastX=xPos;
		this.lastY=yPos;
		this.xPos=xPos;
		this.yPos=yPos;
		this.isDragging = getMouseButtonPressed()[0] || getMouseButtonPressed()[1] || getMouseButtonPressed()[2];
		
		for(String key : callbacks.keySet()) {
			callbacks.get(key).sendMousePos(window,xPos,yPos);
		}
		
		if(isDragging) {
			gui.showNextFrame();
		}
	}
	
	public void mouseButtonCallback(long window, int button, int action, int mods) {
		if(action==GLFW_PRESS) {
			if(button<getMouseButtonPressed().length) {
				getMouseButtonPressed()[button] = true;
			}
		} else if(action==GLFW_RELEASE) {
			if(button<getMouseButtonPressed().length) {
				getMouseButtonPressed()[button] = false;
				isDragging=false;
			}
		}
		gui.showNextFrame();
		for(String key : callbacks.keySet()) {
			callbacks.get(key).sendMouseButton(window,button,action,mods);
		}
	}
	
	public void mouseScrollCallback(long window,double xOffset,double yOffset) {
		scrollX=xOffset;
		scrollY=yOffset;
		gui.showNextFrame();
		
		for(String key : callbacks.keySet()) {
			callbacks.get(key).sendMouseScroll(window, xOffset, yOffset);
		}
	}
	
	public void endFrame() {
		scrollX=0;
		scrollY=0;
		lastX=xPos;
		lastY=yPos;
	}
	
	public float getLastX() {
		return (float) lastX;
	}
	
	public float getLastY() {
		return (float) lastY;
	}
	
	public void setLastX(float lastX) {
		this.lastX=lastX;
	}
	
	public void setLastY(float lastY) {
		this.lastY=lastY;
	}
	
	public float getX() {
		return (float)xPos;
	}
	
	public float getY() {
		return (float)yPos;
	}
	
	public float getDx() {
		return (float)(lastX-xPos);
	}
	
	public float getDy() {
		return (float)(lastY-yPos);
	}
	
	public float getScrollX() {
		return (float)scrollX;
	}
	
	public float getScrollY() {
		return (float)scrollY;
	}
	
	public boolean isDragging() {
		return isDragging;
	}
	
	public boolean mouseButtonDown(int button) {
		if(button<getMouseButtonPressed().length) {
			return getMouseButtonPressed()[button];
		} else {
			return false;
		}
	}

	public boolean[] getMouseButtonPressed() {
		return mouseButtonPressed;
	}

	public void setMouseButtonPressed(boolean[] mouseButtonPressed) {
		this.mouseButtonPressed = mouseButtonPressed;
	}
}
