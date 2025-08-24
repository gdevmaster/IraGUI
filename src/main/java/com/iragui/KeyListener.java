package com.iragui;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;

import com.iragui.objects.GUIObject;


public class KeyListener {
	
	private boolean[] keys = new boolean[350];
	private GUI gui;
	private HashMap<String,GUIObject> callbacks;
	
	public KeyListener(GUI gui) {
		this.gui=gui;
		this.callbacks=new HashMap<>();
	}
	
	public void add(GUIObject obj) {
		this.callbacks.put(obj.name,obj);
	}
	public void remove(GUIObject obj) {
		this.callbacks.remove(obj.name);
	}
	
	public void keyCallback(long window, int key, int scanCode, int action, int mods) {
		
		if(action==GLFW_PRESS) {
			getKeys()[key]=true;
		} else if(action==GLFW_RELEASE) {
			getKeys()[key]=false;
		} else if(action==GLFW_REPEAT) {
			getKeys()[key]=true;
		}
		gui.showNextFrame();
		
		for(String i : callbacks.keySet()) {
			callbacks.get(i).sendKey(key,action);
		}
	}
	
	public boolean getKeyPressed(int key) {
		return getKeys()[key];
	}

	public boolean[] getKeys() {
		return keys;
	}
/*
	public void setKeys(boolean[] keys) {
		this.keys = keys;
	}
	*/
}
