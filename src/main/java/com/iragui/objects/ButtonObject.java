package com.iragui.objects;

import org.lwjgl.glfw.GLFW;

import com.iragui.GUI;

/**
 * Represents an interactive button within the GUI system. 
 * <p>
 * A button is an {@link AnimationObject} with multiple frames representing
 * different states (normal, hover, pressed). It supports mouse position and 
 * button interactions, and can be enabled/disabled.
 * </p>
 */
public class ButtonObject extends AnimationObject {
	
	 /**
     * Creates a new {@code ButtonObject}.
     *
     * @param name the unique name of the button
     * @param layer the GUI layer this button belongs to
     * @param gui the GUI instance managing this button
     * @param x the x-position of the button
     * @param y the y-position of the button
     * @param sizeX the width of the button
     * @param sizeY the height of the button
     * @param nearestFilter whether nearest-neighbor filtering should be used
     * @param rgba whether the button uses RGBA color format
     * @param images the animation frames for the button
     *               (e.g., idle, hover, pressed states)
     */
	public ButtonObject(String name, int layer, GUI gui, int x, int y, int sizeX, int sizeY, boolean nearestFilter,
			boolean rgba,WrappedBufferedImage...images) {
		super(name,layer,gui,x,y,sizeX,sizeY,nearestFilter,rgba,false,false,true,images);
	}

	private boolean disabled=false;
	

    /**
     * Returns whether the button is currently enabled.
     *
     * @return {@code true} if the button can be interacted with, 
     *         {@code false} if disabled
     */
	public boolean isEnabled() {
		return !this.disabled;
	}
	

    /**
     * Disables the button, preventing interactions.
     */
	public void disable() {
		this.disabled=true;
	}
	
	 /**
     * Enables the button, allowing interactions.
     */
	public void enable() {
		this.disabled=false;
	}

	@Override
	public void sendKey(int key, int action) {}

	private boolean inBounds=false;
	
	 /**
     * Checks if the mouse cursor is currently within the button's bounds.
     *
     * @return {@code true} if the mouse is over the button, {@code false} otherwise
     */
	public boolean inBounds() {
		return this.inBounds;
	}
	
	/**
	 * Handles mouse movement and updates the button's hover/idle state.
	 *
	 * @param window the window handle
	 * @param xPos the current mouse x-position
	 * @param yPos the current mouse y-position
	 */
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
	
	/**
	 * Handles mouse button input and updates the button's pressed state.
	 *
	 * @param window the window handle
	 * @param button the mouse button pressed/released
	 * @param action the action type (press or release)
	 * @param mods modifier keys pressed during the event
	 */
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
	
	/**
     * Returns whether the mouse is currently inside the button.
     *
     * @return {@code true} if inside, {@code false} otherwise
     */
	public boolean getInBounds() {
		return this.inBounds;
	}
	
	
	private boolean pressed=false;
	
	/**
     * Reads and clears the "pressed" state of the button. 
     * <p>
     * This is useful for checking if the button was clicked once, since it 
     * resets after being read.
     * </p>
     *
     * @return {@code true} if the button was pressed since last read,
     *         {@code false} otherwise
     */
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
