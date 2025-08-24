package com.iragui;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_MAXIMIZED;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import com.iragui.listeners.KeyListener;
import com.iragui.listeners.MouseListener;
import com.iragui.listeners.WindowListener;
import com.iragui.objects.GUIObject;
import com.iragui.objects.WrappedBufferedImage;

/**
 * Represents a window in the GUI system, managing rendering, input, and audio context.
 * <p>
 * This class uses GLFW for window creation and input handling, and OpenGL for rendering.
 * It also manages OpenAL contexts for audio playback.
 * </p>
 */
public class Window {
	
	private GUI gui;
	
	private boolean resizable,maximized,exitOnClose,decorated,closed=false,init=false;
	private long window;
	
	protected int sizeX;
	protected int sizeY;

	private int displayX;

	private int displayY;
	private String name;
	
	private WindowListener windowListener;
    private KeyListener keyListener;
    private MouseListener mouseListener;
    
    /** @return the key listener associated with this window */
   public KeyListener getKeyListener() {
	   return this.keyListener;
   }
   
   /** @return the window listener associated with this window */
   public WindowListener getWindowListener() {
	   return this.windowListener;
   }
   
   /** @return the window listener associated with this window */
   public MouseListener getMouseListener() {
	   return this.mouseListener;
   }
   
   /** @return the window width in pixels */
   public int getSizeX() {
	   return this.sizeX;
   }
   
   /** @return the window height in pixels */
   public int getSizeY() {
	   return this.sizeY;
   }
   
   /** @return the GLFW window handle */
   public long getWindow() {
	   return this.window;
   }
	
   /**
    * Creates a new {@code Window} with the specified configuration.
    *
    * @param name            the window title
    * @param sizeX           window width in pixels
    * @param sizeY           window height in pixels
    * @param displayX        display width scaling
    * @param displayY        display height scaling
    * @param resizable       whether the window can be resized
    * @param decorated       whether the window has decorations (title bar, borders)
    * @param maximized       whether the window should start maximized
    * @param exitOnClose     whether to exit the application when closed
    * @param redrawEveryFrame whether to redraw continuously (unused here)
    * @param gui             the GUI manager instance
    */
	public Window(String name,
			int sizeX,
			int sizeY,
			int displayX,
			int displayY,
			boolean resizable,
			boolean decorated,
			boolean maximized,
			boolean exitOnClose,
			boolean redrawEveryFrame,
			GUI gui) {
		
		this.gui=gui;
		this.resizable=resizable;
		this.maximized=maximized;
		this.exitOnClose=exitOnClose;
		this.decorated=decorated;
		this.sizeX=sizeX;
		this.sizeY=sizeY;
		this.displayX=displayX;
		this.displayY=displayY;
		this.name=name;
		
		windowListener = new WindowListener(this,gui);
		keyListener = new KeyListener(gui);
		mouseListener = new MouseListener(gui);
		
		this.pixelSizeX=this.sizeX/displayX;
		this.pixelSizeY=this.sizeY/displayY;
		
		if(this.pixelSizeX<1) {
			this.pixelSizeX=1;
		}
		if(this.pixelSizeY<1) {
			this.pixelSizeY=1;
		}
		
		
	}
	
	/**
     * Renders the GUI and processes events. Clears the frame if {@code showNextFrame} is true.
     *
     * @param showNextFrame whether to clear and redraw the next frame
     */
	public void render(boolean showNextFrame) {
		if(closed) {
			return;
		}
		
		if(!init) {
			init();
			init=true;
		}
		
		if(!GLFW.glfwWindowShouldClose(window)) {
			
			if(showNextFrame) {
				GL30.glClear(GL30.GL_COLOR_BUFFER_BIT |GL30.GL_DEPTH_BUFFER_BIT);
				this.renderTexturedObjects();
				GLFW.glfwSwapBuffers(window);
			}
			GLFW.glfwPollEvents();
		} else {
			this.close();
		}
	}
	
	 /** Clears the window's color and depth buffers. */
	public void clear() {
		GL30.glClear(GL30.GL_COLOR_BUFFER_BIT |GL30.GL_DEPTH_BUFFER_BIT);
	}
	
	 /** Renders the GUI and swaps buffers without clearing first. */
	public void render() {
		if(closed) {
			return;
		}
		
		if(!init) {
			init();
			init=true;
		}
		
		if(!GLFW.glfwWindowShouldClose(window)) {
			this.renderTexturedObjects();
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		} else {
			this.close();
		}
	}
	
	 /**
     * Renders all textured objects in the GUI, layer by layer.
     * Removes objects that no longer exist in the GUI's name registry.
     */
	private void renderTexturedObjects() {
		
		for(Integer l : gui.objectsByLayer.keySet()) {
			
			Iterator<GUIObject> iterator = gui.objectsByLayer.get(l).iterator();
			while (iterator.hasNext()) {
			    GUIObject obj = iterator.next();
			    if (gui.objectsByName.containsKey(obj.name)) {
			        obj.render(window, this.sizeX, this.sizeY);
			    } else {
			        gui.println("Removing stale reference: " + obj.name);
			        iterator.remove();
			    }
			}
		}
	}
	
	/**
     * Initializes the GLFW window, OpenGL context, and sets up callbacks.
     *
     * @throws IllegalStateException if GLFW fails to initialize
     * @throws RuntimeException      if the window cannot be created
     */
	 public void init() {
	    	// Set up an error callback
	    	GLFWErrorCallback.createPrint(System.err).set();
	    					
	    	// Initialize GLFW
	    	if(!GLFW.glfwInit()) {
	    		throw new IllegalStateException("Unable to initialize GLFW");
	    	}		
	    	// Configure GLFW
	    	GLFW.glfwDefaultWindowHints();
	    	GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
	    	GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
	    	
	    	GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
	    			
	    	GLFW.glfwWindowHint(GLFW_VISIBLE,GLFW_FALSE);
	    			
	    	if(resizable) {
	    		GLFW.glfwWindowHint(GLFW_RESIZABLE,GLFW_TRUE);
	    	} else {
	    		GLFW.glfwWindowHint(GLFW_RESIZABLE,GLFW_FALSE);
	    	}
	    			
	    	if(maximized) {
	    		GLFW.glfwWindowHint(GLFW_MAXIMIZED,GLFW_TRUE);
	    	} else {
	    		GLFW.glfwWindowHint(GLFW_MAXIMIZED,GLFW_FALSE);
	    	}
	    			
	    	if(decorated) {
	    		GLFW.glfwWindowHint(GLFW_DECORATED,GLFW_TRUE);
	    	} else {
	    		GLFW.glfwWindowHint(GLFW_DECORATED,GLFW_FALSE);
	    	}
	    	gui.println("creating window of sizex: "+this.sizeX+" sizey: "+sizeY);
	    	window = GLFW.glfwCreateWindow(this.sizeX,this.sizeY,this.name,NULL,NULL);
	    	if(window==NULL) {
	    		throw new RuntimeException("Failed to create the GLFW window");
	    	}
	    			
	    	GLFW.glfwMakeContextCurrent(window);
	    	GLFW.glfwSwapInterval(1);
	    	GLFW.glfwShowWindow(window);
	    	
	    	GLFW.glfwSetWindowFocusCallback(window,windowListener::windowFocusCallback);
	    	GLFW.glfwSetWindowMaximizeCallback(window,windowListener::windowMaximizeCallback);
	    	GLFW.glfwSetWindowPosCallback(window,windowListener::windowPositionCallback);
	    	GLFW.glfwSetWindowRefreshCallback(window,windowListener::windowRefreshCallback);
	    	GLFW.glfwSetKeyCallback(window,keyListener::keyCallback);
	    	GLFW.glfwSetCursorPosCallback(window,mouseListener::mousePosCallback);
	    	GLFW.glfwSetMouseButtonCallback(window,mouseListener::mouseButtonCallback);
	    	GLFW.glfwSetScrollCallback(window,mouseListener::mouseScrollCallback);
	    	GLFW.glfwSetFramebufferSizeCallback(window,windowListener::windowFramebufferSizeCallback);
	    	GLFW.glfwSetWindowCloseCallback(window,windowListener::windowCloseCallback);
	    	
	    	try {
	    		GL.createCapabilities();
	    	} catch (Exception e) {
	    		GL.create();
	    		GL.createCapabilities();
	    	}
	    	
	    	
	    	GL30.glEnable(GL30.GL_BLEND);
	    	GL30.glBlendEquationSeparate(GL30.GL_FUNC_ADD, GL30.GL_FUNC_ADD);
	    	GL30.glBlendFuncSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ZERO);
	    }
	 
	 private long audioContext;
		private long audioDevice;
		 /**
	     * Initializes OpenAL audio context for sound playback.
	     *
	     * @throws AssertionError if the system does not support OpenAL 1.0
	     */
	    public void initSound() {
			String defaultDeviceName = alcGetString(0,ALC_DEFAULT_DEVICE_SPECIFIER);
			audioDevice = alcOpenDevice(defaultDeviceName);
			
			int[] attributes = {0};
			audioContext = alcCreateContext(audioDevice,attributes);
			alcMakeContextCurrent(audioContext);
			
			ALCCapabilities alcCapabilites = ALC.createCapabilities(audioDevice);
			ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilites);
			
			
			if(!alCapabilities.OpenAL10) {
				assert false : "audio library not supported";
			}
	    }
	 
	    /**
	     * Closes the window, destroys OpenGL and OpenAL contexts,
	     * and terminates GLFW.
	     */
	  public void close() {
	    	
	    	GL.destroy();
	    	
	    	ALC10.alcDestroyContext(audioContext);
	    	ALC10.alcCloseDevice(audioDevice);
	    					
	    	// Free the memory
	    	Callbacks.glfwFreeCallbacks(window);
	    	GLFW.glfwDestroyWindow(window);
	    						
	    	// Terminate GLFW and the free the callback
	    	GLFW.glfwTerminate();
	    	GLFW.glfwSetErrorCallback(null).free();
	    						
	    	if(exitOnClose) {
	    		System.exit(0);
	    	}
	    	closed=true;
	    	gui.println("Window Closed");
	    }

	  /**
	     * Modifies the display size and recalculates scaling factors.
	     *
	     * @param x new width
	     * @param y new height
	     */
	  public void modifyDisplaySize(int x, int y) {
			
			
			if(x<=0||y<=0) {
				return;
			}
			
			gui.println("float pX = this.sizeX / this.displayX :"+(float)this.sizeX / this.displayX);
			gui.println("float pY = this.sizeY / this.displayY :"+(float)this.sizeY / this.displayY);
			
			float pX = (float) this.sizeX / this.displayX;
			float pY = (float) this.sizeY / this.displayY;
			
			
				
			this.sizeX=x;
			this.sizeY=y;
				
			this.displayX=(int) (x/pX);
			this.displayY=(int) (y/pY);
				
			gui.println("x : "+x);
			gui.println("pX : "+pX);
			gui.println("displayX = (int) (x/PX) : "+(int) (x/pX));
				
			gui.println("y : "+y);
			gui.println("pY : "+pY);
			gui.println("displayY = (int) (y/PY) : "+(int) (y/pY));
				
			GL30.glViewport(0,0,sizeX,sizeY);
				
			// Combats DIV ZERO Crash
			if(displayX==0) {
				this.setPixelSizeX(1);
			} else {
				this.setPixelSizeX(x/displayX);
			}
				
			if(displayY==0) {
				this.setPixelSizeX(1);
			} else {
				this.setPixelSizeY(y/displayY);
			}
		}
	  
	  /**
	     * Sets the background clear color for OpenGL rendering.
	     *
	     * @param r red component (0–1)
	     * @param g green component (0–1)
	     * @param b blue component (0–1)
	     * @param a alpha component (0–1)
	     */
	 public void setBackground(float r, float g, float b, float a) {
		GL30.glClearColor(r,g,b,a);
	 }
	  
	  private int pixelSizeX,pixelSizeY;
	  
	  /** @return the current pixel height scaling factor */
		public int getPixelSizeY() {
			return this.pixelSizeY;
		}
		
		 /** @return the current pixel width scaling factor */
		public int getPixelSizeX() {
			return this.pixelSizeX;
		}
		
		/** @param i new pixel height scaling factor */
		public void setPixelSizeY(int i) {
			this.pixelSizeY=i;
		}
		
		 /** @param i new pixel width scaling factor */
		public void setPixelSizeX(int i) {
			this.pixelSizeX=i;
		}
		
		private HashMap<WrappedBufferedImage,Long> cursors = new HashMap<>();
		private WrappedBufferedImage cursor=null;
		
		 /** @return the currently active custom cursor, or null if none */
		public WrappedBufferedImage getCursor() {
			return this.cursor;
		}
		
		  /** Resets the cursor to the default system cursor. */
		public void resetCursor() {
			GLFW.glfwSetCursor(window, 0);
			cursor=null;
		}
		
		 /**
	     * Sets a custom cursor image for the window.
	     *
	     * @param cursor the wrapped image to use as cursor
	     */
		public void setCursor(WrappedBufferedImage cursor) {
			this.cursor=cursor;
			
			if(cursors.containsKey(cursor)) {
				GLFW.glfwSetCursor(window,cursors.get(cursor));
				GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
				return;
			}
			
			ByteBuffer cursorImage = cursor.getPixelBuffer();
	        cursorImage.flip();
			
			
			try (MemoryStack _ = MemoryStack.stackPush()) {
	            GLFWImage glfwImage = GLFWImage.create();
	            glfwImage.set(cursor.getSizeX(),cursor.getSizeY(), cursorImage);

	            long currentCursor = GLFW.glfwCreateCursor(glfwImage, cursor.getSizeX()/2, cursor.getSizeY()/2);
	            cursors.put(cursor,currentCursor);

	            GLFW.glfwSetCursor(window,currentCursor);
	            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
	        }
		}
}
