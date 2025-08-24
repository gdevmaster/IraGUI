package com.iragui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;


import com.iragui.objects.GUIObject;
import com.iragui.objects.WrappedBufferedImage;

/**
 * Represents the core GUI manager responsible for rendering, updating,
 * and managing {@link GUIObject}s.
 * <p>
 * A {@code GUI} contains a {@link Window}, a collection of GUI objects,
 * and manages their rendering order by layers. It also provides
 * utilities for cursor graphics, frame rendering, and logging.
 * </p>
 */
public class GUI {
	
	private HashSet<GUIObject> objects;
	protected HashMap<String,GUIObject> objectsByName;
	protected TreeMap<Integer,ArrayList<GUIObject>> objectsByLayer;
	
	private Window window;
	
	private boolean showFrame=true;
	private boolean redrawEveryFrame;
	private int frameCounter=0;
	private final int frameCounterMax=2;
	
	 /** Whether to log debug output to the console. */
	public boolean logOutput = true;
	
	/** Mouse cursor image used for horizontal resizing. */
	public WrappedBufferedImage horizontalMouse;
	
	/** Mouse cursor image used for vertical resizing. */
	public WrappedBufferedImage verticalMouse;
	
	/** Mouse cursor image used for diagonal (left) resizing. */
	public WrappedBufferedImage diagonalLeftMouse;
	
	/** Mouse cursor image used for diagonal (right) resizing. */
	public WrappedBufferedImage diagonalRightMouse;
	
	 /**
     * Constructs a new {@code GUI} with the specified window settings.
     *
     * @param name            the window title
     * @param sizeX           window width in pixels
     * @param sizeY           window height in pixels
     * @param displayX        display scaling width
     * @param displayY        display scaling height
     * @param resizable       whether the window can be resized
     * @param decorated       whether the window should have decorations
     * @param maximized       whether the window should start maximized
     * @param exitOnClose     whether to exit the program on window close
     * @param redrawEveryFrame whether to continuously redraw frames
     * @param nearestFilter   whether to use nearest-neighbor texture filtering
     */
	public GUI(String name,
			int sizeX,
			int sizeY,
			int displayX,
			int displayY,
			boolean resizable,
			boolean decorated,
			boolean maximized,
			boolean exitOnClose,
			boolean redrawEveryFrame,
			boolean nearestFilter) {
		
		window = new Window(name,sizeX,sizeY,displayX,displayY,resizable,decorated,maximized,exitOnClose,redrawEveryFrame,this);
		objects = new HashSet<>();
		objectsByName = new HashMap<>();
		objectsByLayer = new TreeMap<>();
		
		this.redrawEveryFrame=redrawEveryFrame;
	}
	
	 /**
     * Initializes the GUI system, rendering the first frame
     * and loading default mouse cursors.
     */
	public void begin() {
		this.render();
		window.initSound();
		this.horizontalMouse = new WrappedBufferedImage("horizontalMouseWBI", 
				0, 
				this, 
				23, 
				11,
				true, 
				true, 
				FileSystem.getImage("data/newResizeHorizontal.png"));
		this.verticalMouse = new WrappedBufferedImage("verticalMouseWBI", 
				0, 
				this, 
				11, 
				23,
				true, 
				true, 
				FileSystem.getImage("data/newResizeVertical.png"));
		
		this.diagonalLeftMouse = new WrappedBufferedImage("diagonalLeftMouseWBI", 
				0, 
				this, 
				17, 
				17,
				true, 
				true, 
				FileSystem.getImage("data/newResizeDiagRight.png"));
		
		this.diagonalRightMouse = new WrappedBufferedImage("diagonalRightMouseWBI", 
				0, 
				this, 
				17, 
				17,
				true, 
				true, 
				FileSystem.getImage("data/newResizeDiagLeft.png"));
	}
	
	/**
     * Updates all {@link GUIObject}s in the GUI.
     * <p>
     * Determines if a new frame should be shown based on
     * the {@code showFrame} and {@code redrawEveryFrame} flags.
     * </p>
     */
	public void update() {
		boolean initiallyShowFrame = this.showFrame;
		
		for(Integer l : objectsByLayer.keySet()) {
			for(int i=0;i<objectsByLayer.get(l).size();i++) {
				objectsByLayer.get(l).get(i).update(initiallyShowFrame);
			}
		}
		
		if(this.showFrame && !initiallyShowFrame) {
			// We'll draw next frame
		} else if(showFrame){
			if(!redrawEveryFrame) {
				// No need to draw, we didn't get the signal
				if(frameCounter>=frameCounterMax) {
					this.showFrame=false;
					frameCounter=0;
				} else {
					frameCounter++;
				}
			}
		}
	}
	
	/** Forces the next frame to be drawn. */
	public void showNextFrame() {
		this.showFrame=true;
	}
	
	 /**
     * @return whether a frame should currently be drawn
     */
	public boolean canShowFrame() {
		return this.showFrame;
	}
	
	/**
     * Removes a {@link GUIObject} from the GUI, unregistering it
     * from layers and event listeners.
     *
     * @param o the object to remove
     */
	public void removeObject(GUIObject o) {
		objects.remove(o);
		objectsByName.remove(o.name);
		removeObjectFromLayerList(o);
		
		if(o.includesKeyCallback()) {
			this.window.getKeyListener().remove(o);
		}
		
		if(o.includesMouseCallback()) {
			this.window.getMouseListener().remove(o);
		}
	}
	
	 /**
     * Adds a new {@link GUIObject} to the GUI, registering it
     * in name maps, layers, and event listeners.
     *
     * @param o the object to add
     */
	public void addObject(GUIObject o) {
		
		if(objects.contains(o)) {
			println("Already have object "+o.name);
			return;
		}
		
		objects.add(o);
		objectsByName.put(o.name,o);
		addObjectToLayerList(o);
		
		if(o.includesKeyCallback()) {
			this.window.getKeyListener().add(o);
		}
		if(o.includesMouseCallback()) {
			this.window.getMouseListener().add(o);
		}
		
		println("added object "+o.name);
	}
	
	 /**
     * Moves a {@link GUIObject} to a new layer.
     *
     * @param oldLayer the object's old layer
     * @param o        the object to update
     */
	public void confirmLayerUpdate(int oldLayer,GUIObject o) {
		removeObjectFromLayerList(o);
		addObjectToLayerList(o);
	}
	
	private void removeObjectFromLayerList(GUIObject o) {
		if(objectsByLayer.containsKey(o.getLayer())) {
			if(!objectsByLayer.get(o.getLayer()).remove(o)) {
				for(Integer l : objectsByLayer.keySet()) {
					if(!objectsByLayer.get(l).isEmpty()) {
						for(int i=0;i<objectsByLayer.get(l).size();i++) {
							if(objectsByLayer.get(l).get(i)==o) {
								objectsByLayer.get(l).remove(i);
								println("object layer dy-sync detected when deleting "+o.name+" from layered list");
								return;
							}
						}
					}
				}
			} else {
				println("removed "+o.name+" from layered list");
				return;
			}
			println("[Warning] did not delete "+o.name+" from layered list");
		} else {
			for(Integer l : objectsByLayer.keySet()) {
				if(!objectsByLayer.get(l).isEmpty()) {
					for(int i=0;i<objectsByLayer.get(l).size();i++) {
						if(objectsByLayer.get(l).get(i)==o) {
							objectsByLayer.get(l).remove(i);
							println("layer not found, deleted "+o.name+" from layered list anyways");
							return;
						}
					}
				}
			}
			println("[Warning] did not delete "+o.name+" from layered list, and layer not found");
		}
	}
	
	private void addObjectToLayerList(GUIObject o) {
		if(objectsByLayer.containsKey(o.getLayer())) {
			objectsByLayer.get(o.getLayer()).add(o);
		} else {
			ArrayList<GUIObject> s = new ArrayList<>();
			s.add(o);
			objectsByLayer.put(o.getLayer(),s);
			
		}
	}
	
	 /**
     * @return the objects grouped by rendering layer
     */
	public TreeMap<Integer,ArrayList<GUIObject>> getObjectsByLayer(){
		return this.objectsByLayer;
	}

	  /**
     * Renders the GUI by delegating to the {@link Window}.
     * Draws if {@code showFrame} or {@code redrawEveryFrame} is true.
     */
	public void render() {
		window.render(this.showFrame||this.redrawEveryFrame);
	}
	
	  /**
     * Sets the background color of the {@link Window}.
     *
     * @param r red component (0–1)
     * @param g green component (0–1)
     * @param b blue component (0–1)
     * @param a alpha component (0–1)
     */
	public void setBackground(float r, float g, float b, float a) {
		if(window!=null) {
			window.setBackground(r,g,b,a);
		}
	}

    /** @return the underlying {@link Window} for this GUI */
	public Window getWindow() {
		return this.window;
	}
	
	  /** @return the total number of objects managed by this GUI */
	public int getObjectCount() {
		return this.objects.size();
	}

	 /** @return a map of objects by name */
	public HashMap<String, GUIObject> getObjectsByName() {
		return this.objectsByName;
	}
	
	/**
     * Prints a string to console if {@link #logOutput} is enabled.
     *
     * @param string the text to print
     */
	public void println(String string) {
		if(this.logOutput) {
			System.out.println(string);
		}
	}
}
