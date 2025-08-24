package com.iragui.objects;

import java.util.TreeMap;

import com.iragui.GUI;

public abstract class AnimationObject extends GUIObject{
	
	private TreeMap<Integer,WrappedBufferedImage> frames;
	
	private int frame = 0;
	
	private boolean anyFrameVisible=false;
	
	public AnimationObject(String name, int layer, GUI gui, int x, int y, int sizeX, int sizeY, boolean nearestFilter,
			boolean rgba, boolean updates, boolean includeKeyCallback, boolean includeMouseCallback,
			WrappedBufferedImage[] images) {
		super(name, layer, gui, x, y, sizeX, sizeY, nearestFilter, rgba,includeKeyCallback, includeMouseCallback, updates);
		frames = new TreeMap<>();
		for(int i=0;i<images.length;i++) {
			frames.put(i,images[i]);
			images[i].setX(x);
			images[i].setY(y);
		}
	}

	@Override
	public void destroyObject() {
		for(Integer i : frames.keySet()) {
			frames.get(i).destroyObject();
		}
		
		super.destroyObject();
	}
	
	@Override
	public void setX(int x) {
		this.x=x;
		for(Integer key : frames.keySet()) {
			frames.get(key).setX(x);
		}
	}
	
	@Override
	public void setY(int y) {
		this.y=y;
		for(Integer key : frames.keySet()) {
			frames.get(key).setY(y);
		}
	}
	
	public int getFrame() {
		return this.frame;
	}
	
	public void setFrame(int frame) {
		if(frames.containsKey(frame)) {
			for(int i=0;i<frames.size();i++) {
				frames.get(i).hide();
			}
			this.frame=frame;
			if(anyFrameVisible) {
				this.setVisible();
			}
		}
	}
	
	@Override
	public void setLayer(int layer) {
		super.setLayer(layer);
		for(Integer key : frames.keySet()) {
			frames.get(key).setLayer(layer);
		}
	}
	
	@Override
	public void setWinLimitX(int x) {
		super.setWinLimitX(x);
		for(Integer key : frames.keySet()) {
			frames.get(key).setWinLimitX(x);
		}
	}
	@Override
	public void setWinLimitY(int y) {
		super.setWinLimitY(y);
		for(Integer key : frames.keySet()) {
			frames.get(key).setWinLimitY(y);
		}
	}
	@Override
	public void setWinMinX(int x) {
		super.setWinMinX(x);
		for(Integer key : frames.keySet()) {
			frames.get(key).setWinMinX(x);
		}
	}
	@Override
	public void setWinMinY(int y) {
		super.setWinMinY(y);
		for(Integer key : frames.keySet()) {
			frames.get(key).setWinMinY(y);
		}
	}
	
	@Override
	public void setLimitX(int x) {
		x=(x<0?0:x);
		this.limitX=x;
		for(Integer key : frames.keySet()) {
			frames.get(key).setLimitX(x);
		}
	}
	@Override
	public void setLimitY(int y) {
		y=(y<0?0:y);
		this.limitY=y;
		for(Integer key : frames.keySet()) {
			frames.get(key).setLimitY(y);
		}
	}
	@Override
	public void setMinX(int x) {
		x=(x<0?0:x);
		this.minX=x;
		for(Integer key : frames.keySet()) {
			frames.get(key).setMinX(x);
		}
	}
	@Override
	public void setMinY(int y) {
		y=(y<0?0:y);
		this.minY=y;
		for(Integer key : frames.keySet()) {
			frames.get(key).setMinY(y);
		}
	}
	
	@Override
	public void setVisible() {
		this.anyFrameVisible=true;
		frames.get(this.frame).setVisible();
	}
	
	@Override
	public void hide() {
		this.anyFrameVisible=false;
		for(int i=0;i<frames.size();i++) {
			frames.get(i).hide();
		}
	}
	
	@Override
	public boolean getVisible() {
		return frames.get(this.frame).getVisible();
	}
	
	public boolean getAnyFrameVisible() {
		return this.anyFrameVisible;
	}
	
	public TreeMap<Integer,WrappedBufferedImage> getFrames(){
		return this.frames;
	}
	
}
