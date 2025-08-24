package com.iragui.objects;

import java.util.TreeMap;

import com.iragui.GUI;

/**
 * Abstract GUI object that displays an animated sequence of {@link WrappedBufferedImage} frames.
 * <p>
 * The current frame can be switched using {@link #setFrame(int)}, and visibility is handled
 * per-frame while respecting the animation’s overall visibility state.
 * </p>
 */
public abstract class AnimationObject extends GUIObject{
	
	private TreeMap<Integer,WrappedBufferedImage> frames;
	
	private int frame = 0;
	
	private boolean anyFrameVisible=false;
	
	/**
     * Constructs a new {@code AnimationObject}.
     *
     * @param name                  object name
     * @param layer                 rendering layer
     * @param gui                   parent GUI
     * @param x                     X position
     * @param y                     Y position
     * @param sizeX                 width in pixels
     * @param sizeY                 height in pixels
     * @param nearestFilter         whether to use nearest-neighbor filtering
     * @param rgba                  whether the image has an alpha channel
     * @param updates               whether this object should be updated
     * @param includeKeyCallback    whether to receive key input callbacks
     * @param includeMouseCallback  whether to receive mouse input callbacks
     * @param images                the sequence of frames for this animation
     */
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

	/**
     * Destroys this animation and all of its frames.
     */
	@Override
	public void destroyObject() {
		for(Integer i : frames.keySet()) {
			frames.get(i).destroyObject();
		}
		
		super.destroyObject();
	}
	
	 /**
     * Sets the X position of this object and all frames.
     *
     * @param x new X position
     */
	@Override
	public void setX(int x) {
		this.x=x;
		for(Integer key : frames.keySet()) {
			frames.get(key).setX(x);
		}
	}
	
	 /**
     * Sets the Y position of this object and all frames.
     *
     * @param y new Y position
     */
	@Override
	public void setY(int y) {
		this.y=y;
		for(Integer key : frames.keySet()) {
			frames.get(key).setY(y);
		}
	}
	
	/**
     * Returns the current frame index.
     *
     * @return current frame
     */
	public int getFrame() {
		return this.frame;
	}
	
	/**
     * Sets the active frame index. All other frames are hidden.
     *
     * @param frame frame index to display
     */
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
	
	 /** {@inheritDoc} */
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
	
	/**
     * Makes the current frame visible. Sets the flag that any frame is visible.
     */
	@Override
	public void setVisible() {
		this.anyFrameVisible=true;
		frames.get(this.frame).setVisible();
	}
	
	/**
     * Hides all frames and marks the animation as not visible.
     */
	@Override
	public void hide() {
		this.anyFrameVisible=false;
		for(int i=0;i<frames.size();i++) {
			frames.get(i).hide();
		}
	}
	
	 /**
     * Returns whether the current frame is visible.
     *
     * @return {@code true} if the current frame is visible
     */
	@Override
	public boolean getVisible() {
		return frames.get(this.frame).getVisible();
	}
	
	/**
     * Returns whether any frame in this animation has been marked as visible.
     *
     * @return {@code true} if any frame is visible
     */
	public boolean getAnyFrameVisible() {
		return this.anyFrameVisible;
	}
	
	 /**
     * Returns all frames of this animation.
     *
     * @return map of frame index to {@link WrappedBufferedImage}
     */
	public TreeMap<Integer,WrappedBufferedImage> getFrames(){
		return this.frames;
	}
	
}
