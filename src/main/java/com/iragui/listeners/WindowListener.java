package com.iragui.listeners;

import com.iragui.GUI;
import com.iragui.Window;

public class WindowListener {
	
	public int x,y;
	public boolean maximized,focused,close;
	public float xScale,yScale;
	private Window w;
	private GUI gui;
	
	public WindowListener(Window w,GUI gui) {
		eventHappened=false;
		x=0;
		y=0;
		maximized=false;
		focused=false;
		xScale=0;
		yScale=0;
		this.w=w;
		this.gui=gui;
		this.close=false;
	}
	
	private boolean eventHappened;
	
	public void windowFramebufferSizeCallback(long window, float x, float y) {
		eventHappened=true;
		this.xScale=x;
		this.yScale=y;
		w.modifyDisplaySize((int)x, (int)y);
		gui.showNextFrame();
	}
	
	public void windowCloseCallback(long window) {
		close=true;
	}
	
	public void windowFocusCallback(long window,boolean isFocused) {
		eventHappened=true;
		this.focused=isFocused;
		gui.showNextFrame();
	}
	
	public void windowMaximizeCallback(long window,boolean isMax) {
		eventHappened=true;
		this.maximized=isMax;
		w.modifyDisplaySize((int)x, (int)y);
		gui.showNextFrame();
	}
	
	public void windowPositionCallback(long window,int x, int y) {
		eventHappened=true;
		this.x=x;
		this.y=y;
		gui.showNextFrame();
	}
	
	public void windowRefreshCallback(long window) {
		eventHappened=true;
		gui.showNextFrame();
	}
	
	public boolean eventHappened() {
		if(eventHappened) {
			eventHappened=false;
			return true;
		} else {
			return false;
		}
	}

	public boolean closed() {
		return this.close;
	}
}
