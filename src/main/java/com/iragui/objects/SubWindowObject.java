package com.iragui.objects;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL30;

import com.iragui.GUI;
import com.iragui.util.ObjectUtils;

/**
 * A specialized {@link GUIObject} representing a resizable, movable, and 
 * optionally decorated sub-window within the GUI.
 * 
 * <p>This class manages its own constraints, background rendering, title bar, 
 * and embedded objects (including other {@code SubWindowObject} instances). 
 * It provides functionality for scrolling, layering, and parent-child window 
 * relationships.</p>
 */
public class SubWindowObject extends GUIObject{
	
	 /** Window alignment constant: top-left corner. */
    public static final int TOP_LEFT = 0;
    /** Window alignment constant: top-center. */
    public static final int TOP_CENTER = 1;
    /** Window alignment constant: top-right corner. */
    public static final int TOP_RIGHT = 2;
    /** Window alignment constant: center-left. */
    public static final int CENTER_LEFT = 3;
    /** Window alignment constant: absolute center. */
    public static final int CENTER = 4;
    /** Window alignment constant: center-right. */
    public static final int CENTER_RIGHT = 5;
    /** Window alignment constant: bottom-left corner. */
    public static final int BOTTOM_LEFT = 6;
    /** Window alignment constant: bottom-center. */
    public static final int BOTTOM_CENTER = 7;
    /** Window alignment constant: bottom-right corner. */
    public static final int BOTTOM_RIGHT = 8;

    /** Constraint index for the exit button. */
    private static final int EXIT = 9;
    /** Constraint index for the maximize/split button. */
    private static final int MAX_SPLIT = 10;
    /** Constraint index for the minimize button. */
    private static final int MIN = 11;
    /** Constraint index for the title bar text. */
    private static final int TITLE = 12;

    /** Window movement type: constrained to parent window. */
    public static final int MOVABLE = 13;
    /** Window movement type: free (no constraint). */
    public static final int FREE = 14;

    /** Layer offset factor for embedded objects. */
    public static final int OBJECT_LAYER_FACTOR = 1;
    /** Layer offset factor for nested sub-windows. */
    public static final int SUBWINDOW_LAYER_FACTOR = 2;
	
    /** Global registry of subwindows by name. */
	public static HashMap<String,SubWindowObject> windows = new HashMap<>();
	
    /** Tracks scroll offsets for scrollable objects within this subwindow. */
	public HashMap<String,Long> objectConstraintOffset = new HashMap<>();
	
    /** Global subwindows sorted by their rendering layer. */
	private static TreeMap<Integer,SubWindowObject> windowsByLayer = new TreeMap<>();
	
	/** Objects contained in this window, grouped by constraint index. */
	private TreeMap<Integer,ArrayList<GUIObject>> objects;
	
	/** Lookup table of objects contained in this window by name. */
	private HashMap<String,GUIObject> objectsByName;
	
    /** Whether this window is nested inside another subwindow. */
	protected boolean insideAnotherSubWindow=false;
	  /** The immediate parent subwindow, if nested. */
	protected SubWindowObject parentWindow = null;
	/** The absolute top-level parent subwindow in the hierarchy. */
	protected SubWindowObject absoluteParentWindow = null;
	
	 /** Background color of the window content area. */
	private Color bkgColor;
	
	/** Border and top-bar color of the window. */
	private Color topColor;
	
	/** Whether this window currently has input focus. */
	private boolean focused=false;
	
	 /** Height of the top bar if decorated, otherwise 0. */
	private final int topSize;
	
	/** Border thickness if decorated, otherwise 0. */
	private final int borderSize;
	
	private ButtonObject exitButton,maxButton,minButton,splitButton;
	
	private final boolean decorated;
	private boolean resizable;
	
	/** Title text object (only exists if decorated). */
	public TextObject title;
	
	 /** The original constraint used when nested inside another subwindow. */
	protected int originalConstraint = -1;
	
	/** Title text color when focused. */
	private Color titleColor;
	
	/** Title text color when unfocused. */
	private Color unfocusedTitleColor = Color.LIGHT_GRAY;
	
    /** Whether the window supports mouse wheel scrolling. */
	private boolean doesMouseWheelScroll=false;
	  /** Current scroll offset from mouse wheel events. */
	private int mouseWheelScrolls = 0;
	
	 /** Whether the window supports drag-based scrolling. */
	private boolean doesDragScroll=false;
	
	/** Current X/Y drag scroll offset. */
	private int dragScrollX = 0, dragScrollY = 0;
	
	private boolean useMinScroll = false, useMaxScroll = false;
	private int minScroll=0,maxScroll=0;
	
	/**
     * Returns the current accumulated mouse wheel scroll offset for this subwindow.
     *
     * @return the number of mouse wheel scroll steps
     */
	public int getMouseWheelScrollOffset() {
		return this.mouseWheelScrolls;
	}
	
	 /** Enables mouse wheel scrolling within this subwindow. */
	public void allowMouseWheelScroll() {
		doesMouseWheelScroll=true;
	}
	
	 /** Disables mouse wheel scrolling within this subwindow. */
	public void disallowMouseWheelScroll() {
		doesMouseWheelScroll=false;
	}
	
	 /** Enables drag-based scrolling (click and drag to scroll). */
	public void allowDragScroll() {
		this.doesDragScroll=true;
	}
	
	/** Disables drag-based scrolling. */
	public void disallowDragScroll() {
		this.doesDragScroll=false;
	}
	
	/**
     * Gets the horizontal scroll offset from drag scrolling.
     *
     * @return the drag scroll X offset
     */
	public int getDragScrollX() {
		return this.dragScrollX;
	}
	
	  /**
     * Gets the vertical scroll offset from drag scrolling.
     *
     * @return the drag scroll Y offset
     */
	public int getDragScrollY() {
		return this.dragScrollY;
	}
	
	 /**
     * Sets the horizontal scroll offset used when drag scrolling.
     *
     * @param v the new horizontal offset
     */
	public void setDragScrollX(int v) {
		this.dragScrollX = v;
	}
	
	/**
     * Sets the vertical scroll offset used when drag scrolling.
     *
     * @param v the new vertical offset
     */
	public void setDragScrollY(int v) {
		this.dragScrollY = v;
	}
	
	  /**
     * Enables or disables minimum scroll clamping.
     *
     * @param useMinScroll whether to enforce a minimum scroll offset
     * @param min          the minimum scroll offset to enforce
     */
	public void setUseMinScroll(boolean useMinScroll, int min) {
		this.useMinScroll=useMinScroll;
		this.minScroll=min;
	}
	
	 /**
     * Enables or disables maximum scroll clamping.
     *
     * @param useMaxScroll whether to enforce a maximum scroll offset
     * @param max          the maximum scroll offset to enforce
     */
	public void setUseMaxScroll(boolean useMaxScroll, int max) {
		this.useMaxScroll=useMaxScroll;
		this.maxScroll=max;
	}
	
	/** Mapping of child object names to their relative layer index inside this window. */
	private HashMap<String,Integer> objectLayersInWindow = new HashMap<>();
	
	/**
     * Assigns a GUI object to a relative layer within this subwindow.
     *
     * @param o     the object to re-layer
     * @param layer the relative layer value
     */
	public void setObjectLayerInWindow(GUIObject o, int layer) {
		o.layer = (this.getLayer()+layer+OBJECT_LAYER_FACTOR);
		objectLayersInWindow.put(o.name,layer);
	}
	
	 /** Title text for the window. */
	public String titleText;

	 /**
     * Creates a new decorated or undecorated subwindow object.
     *
     * @param name          the subwindow name
     * @param layer         the rendering layer
     * @param gui           the parent GUI instance
     * @param x             initial X position
     * @param y             initial Y position
     * @param sizeX         width of the window
     * @param sizeY         height of the window
     * @param nearestFilter true to use nearest-neighbor filtering
     * @param rgba          true if window textures use RGBA
     * @param bkgColor      background fill color
     * @param topColor      top bar and border color
     * @param decorated     whether the window has borders and title bar
     * @param resizable     whether the window can be resized
     * @param titleColor    font color for the title text
     * @param title         initial title string
     */
	public SubWindowObject(String name, 
							int layer, 
							GUI gui, 
							int x, 
							int y, 
							int sizeX, 
							int sizeY, 
							boolean nearestFilter,
							boolean rgba,
							Color bkgColor,
							Color topColor,
							boolean decorated,
							boolean resizable,
							Color titleColor,
							String title) {
		
		super(name, layer, gui, x, y, sizeX, sizeY, nearestFilter, rgba, false, true, true);
		
		
		if(layer%SUBWINDOW_LAYER_FACTOR!=0) {
			layer++;
			super.setLayer(layer);
		}
		
		this.titleText=title;
		this.bkgColor=bkgColor;
		this.topColor=topColor;
		this.decorated=decorated;
		this.resizable=resizable;
		this.titleColor=titleColor;
		
		this.objects = new TreeMap<>();
		this.objectsByName = new HashMap<>();
		
		
		this.topSize=(decorated?34:0);
		this.borderSize=(decorated?1:0);
		
		initFullscreenQuad();
		loadBackgroundShader();
		
		if(decorated) {
			
		this.title=new TextObject(name+":title", 
				this.layer+OBJECT_LAYER_FACTOR, 
				gui, 
				0, 
				0, 
				true, 
				rgba, 
				title, 
				new Font("Yu Gothic UI",Font.PLAIN,15), 
				titleColor, 
				rgba?new Color(0,0,0,0):topColor, 
				true);
		this.add(this.title,TITLE);
		this.title.setVisible();
			
			
		if(!resizable) {
			this.exitButton = ObjectUtils.createButtonObject("exit:"+name,
					 this.layer+OBJECT_LAYER_FACTOR,
					 gui,
					 0,
					 0,
					 58,
					 32,
					 true,
					 true,
					 "data/x.png",
					 "data/selectedX.png",
					 "data/clickedX.png");
			this.minButton = ObjectUtils.createButtonObject("min:"+name,
					 this.layer+OBJECT_LAYER_FACTOR,
					 gui,
					 0,
					 0,
					 58,
					 32,
					 true,
					 true,
					 "data/min.png",
					 "data/selectedMin.png",
					 "data/clickedMin.png");
			this.add(minButton,MAX_SPLIT);
			this.add(exitButton,EXIT);
		} else {
			
			this.exitButton = ObjectUtils.createButtonObject("exit:"+name,
				 this.layer+OBJECT_LAYER_FACTOR,
				 gui,
				 0,
				 0,
				 58,
				 32,
				 true,
				 true,
				 "data/x.png",
				 "data/selectedX.png",
				 "data/clickedX.png");
			this.maxButton = ObjectUtils.createButtonObject("max:"+name,
				 this.layer+OBJECT_LAYER_FACTOR,
				 gui,
				 0,
				 0,
				 58,
				 32,
				 true,
				 true,
				 "data/max.png",
				 "data/selectedMax.png",
				 "data/clickedMax.png");
			this.minButton = ObjectUtils.createButtonObject("min:"+name,
				 this.layer+OBJECT_LAYER_FACTOR,
				 gui,
				 0,
				 0,
				 58,
				 32,
				 true,
				 true,
				 "data/min.png",
				 "data/selectedMin.png",
				 "data/clickedMin.png");
			this.add(exitButton,EXIT);
			this.add(maxButton,MAX_SPLIT);
			this.add(minButton,MIN);
		
		}
		
		}
		
		windows.put(name,this);
		
		this.forceFocus();
	}
	
	 /** Shader source code used for rendering the subwindow’s background and border. */
	private String shader = "#type vertex\n"
			+ "#version 330 core\n"
			+ "layout(location = 0) in vec2 position;\n"
			+ "void main() {\n"
			+ "gl_Position = vec4(position, 0.0, 1.0);\n"
			+ "}\n"
			+ "\n"
			+ "#type fragment\n"
			+ "#version 330 core\n"
			+ "\n"
			+ "uniform vec4 backgroundColor;\n"
			+ "uniform vec4 borderColor;\n"
			+ "uniform int borderSize;\n"
			+ "uniform bool decorated;\n"
			+ "uniform int topSize;\n"
			+ "\n"
			+ "uniform vec2 subwindowPos;    // x, y in screen coords\n"
			+ "uniform vec2 subwindowSize;   // width, height\n"
			+ "uniform vec2 screenSize;      // full framebuffer size\n"
			+ "\n"
			+ "out vec4 fragColor;\n"
			+ "\n"
			+ "void main() {\n"
			+ "    vec2 fragCoord = gl_FragCoord.xy;\n"
			+ "\n"
			+ "    // Convert fragment position relative to subwindow\n"
			+ "    vec2 localCoord = fragCoord - subwindowPos;\n"
			+ "\n"
			+ "    // Clamp check: only draw inside subwindow\n"
			+ "    if (localCoord.x < 0.0 || localCoord.y < 0.0 ||\n"
			+ "        localCoord.x >= subwindowSize.x || localCoord.y >= subwindowSize.y) {\n"
			+ "        discard;\n"
			+ "    }\n"
			+ "\n"
			+ "    if (decorated) {\n"
			+ "        bool leftBorder = localCoord.x < borderSize;\n"
			+ "        bool rightBorder = localCoord.x >= subwindowSize.x - borderSize;\n"
			+ "        bool bottomBorder = localCoord.y < borderSize;\n"
			+ "        bool topBorder = localCoord.y >= subwindowSize.y - topSize; // use topSize here\n"
			+ "\n"
			+ "        if (leftBorder || rightBorder || bottomBorder || topBorder) {\n"
			+ "            fragColor = borderColor;\n"
			+ "        } else {\n"
			+ "            fragColor = backgroundColor;\n"
			+ "        }\n"
			+ "    } else {\n"
			+ "        fragColor = backgroundColor;\n"
			+ "    }\n"
			+ "}";
	
	private int windowVao, windowVbo;

	private void initFullscreenQuad() {
		float[] vertices = {
			    -1f, -1f,  // bottom left
			     1f, -1f,  // bottom right
			     1f,  1f,  // top right

			    -1f, -1f,  // bottom left
			     1f,  1f,  // top right
			    -1f,  1f   // top left
			};

	    windowVao = GL30.glGenVertexArrays();
	    windowVbo = GL30.glGenBuffers();

	    GL30.glBindVertexArray(windowVao);
	    GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, windowVbo);
	    GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW);

	    GL30.glEnableVertexAttribArray(0);
	    GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, 0, 0);

	    GL30.glBindVertexArray(0);
	}
	
	private int shaderProgram;
	private void loadBackgroundShader() {
	   Shader shaderObj =  new Shader(this.shader,true);
	   shaderObj.compile();
	   shaderProgram = shaderObj.getId();
	}
	
	/**
     * Renders the subwindow’s background and borders using its shader.
     *
     * @param window       the GLFW window handle
     * @param windowWidth  full framebuffer width
     * @param windowHeight full framebuffer height
     */
	public void render(long window, int windowWidth, int windowHeight) {
	    GL30.glUseProgram(shaderProgram);

	    // Uniforms
	    GL30.glUniform4f(GL30.glGetUniformLocation(shaderProgram, "backgroundColor"),
	        bkgColor.getRed() / 255f, bkgColor.getGreen() / 255f, bkgColor.getBlue() / 255f, bkgColor.getAlpha() / 255f);
	    GL30.glUniform4f(GL30.glGetUniformLocation(shaderProgram, "borderColor"),
	        topColor.getRed() / 255f, topColor.getGreen() / 255f, topColor.getBlue() / 255f, topColor.getAlpha() / 255f);
	    GL30.glUniform1i(GL30.glGetUniformLocation(shaderProgram, "borderSize"), this.borderSize);
	    GL30.glUniform1i(GL30.glGetUniformLocation(shaderProgram, "decorated"), decorated ? 1 : 0);
	    GL30.glUniform2f(GL30.glGetUniformLocation(shaderProgram, "subwindowPos"), x, y);
	    GL30.glUniform2f(GL30.glGetUniformLocation(shaderProgram, "subwindowSize"), sizeX, sizeY);
	    GL30.glUniform2f(GL30.glGetUniformLocation(shaderProgram, "screenSize"), windowWidth, windowHeight);
	    GL30.glUniform1i(GL30.glGetUniformLocation(shaderProgram, "topSize"), topSize - 1);

	    
	    GL30.glBindVertexArray(windowVao);

	    GL30.glEnable(GL30.GL_SCISSOR_TEST);

	    int startX = Math.max(this.minX, this.winMinX);
	    int startY = Math.max(this.minY, this.winMinY);
	    int endX   = Math.min(this.limitX, this.winLimitX);
	    int endY   = Math.min(this.limitY, this.winLimitY);

	    int width  = Math.max(endX - startX, 0);
	    int height = Math.max(endY - startY, 0);

	    GL30.glScissor(startX, startY, width, height);

	    GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);


	    GL30.glDisable(GL30.GL_SCISSOR_TEST);
	    GL30.glBindVertexArray(0);
	    GL30.glUseProgram(0);
	}

	 /**
     * Resets the texture ID for this subwindow and all contained child objects,
     * forcing them to reload textures if necessary.
     */
    @Override
	public void resetTextureID() {
		super.resetTextureID();
		
		for(Integer key : objects.keySet()) {
			if(!objects.get(key).isEmpty()) {
				for(int i=0;i<objects.get(key).size();i++) {
					objects.get(key).get(i).resetTextureID();
				}
			}
		}
	}
	
    /**
     * Finds the highest-layered subwindow within this window, ignoring a specified one.
     *
     * @param ignore the subwindow to skip during the search
     * @return the highest local {@link SubWindowObject}, or {@code null} if none exist
     */
	private SubWindowObject getHighestLocalWindow(SubWindowObject ignore) {
		TreeMap<Integer,SubWindowObject> ws = new TreeMap<>();
		for(String name : windows.keySet()) {
			if(windows.get(name).parentWindow==this && windows.get(name)!=ignore) {
				ws.put(windows.get(name).getLayer(),windows.get(name));
			}
		}
		if(ws.isEmpty()) {
			return null;
		} else {
			return ws.lastEntry().getValue();
		}
	}
	
	/**
     * Adds a {@link GUIObject} to this subwindow with a positional constraint.
     * <p>
     * If the object is another {@link SubWindowObject}, this will properly
     * configure its parent relationships and assign its rendering layer.
     * Non-window objects are placed above the window layer using {@code OBJECT_LAYER_FACTOR}.
     * </p>
     *
     * @param o          the object to add
     * @param constraint the constraint constant (e.g. {@code FREE}, {@code TOP_LEFT}, etc.)
     */
	public void add(GUIObject o, int constraint) {
		
		gui.println("[SUBWINDOWOBJECT] Added "+o.name+" to "+this.name);
		
		o.constraint=constraint;
		
		if(o.getClass()==SubWindowObject.class) {
			SubWindowObject sO = (SubWindowObject)o;
			sO.insideAnotherSubWindow=true;
			sO.parentWindow=this;
			
			if(sO.originalConstraint==-1) {
				sO.originalConstraint=constraint;
			}
			
			if(this.insideAnotherSubWindow) {
				// if we are inside a subwindow, pass down our absolute parent
				sO.absoluteParentWindow=this.absoluteParentWindow;
			} else {
				// if we aren't inside a subwindow, we are the absolute parent,
				// we pass down ourselves
				sO.absoluteParentWindow=this;
			}
			
			int requestedLayer = this.getLayer()+SUBWINDOW_LAYER_FACTOR;
			
			if(windowsByLayer.containsKey(requestedLayer)) {				
				
				
				// gets the highest localized window
				SubWindowObject highestLocalWindow = this.getHighestLocalWindow(this);
				int highestSubLayer = highestLocalWindow.getHighestLayer(this);
				requestedLayer = highestSubLayer+SUBWINDOW_LAYER_FACTOR;
				
				sO.setLayer(requestedLayer);
			} else {
				sO.setLayer(requestedLayer);
			}
		} else {
			o.setLayer(this.getLayer()+OBJECT_LAYER_FACTOR);
		}
		
		if(objects.containsKey(constraint)) {
			objects.get(constraint).add(o);
		} else {
			ArrayList<GUIObject> l = new ArrayList<>();
			l.add(o);
			objects.put(constraint,l);
		}
		objectsByName.put(o.name,o);
		
		constrainObject(o,constraint);
		o.setVisible();
	}
	
	  /**
     * Sets the maximum X bound for this window based on the parent window's limits.
     *
     * @param sWO the parent subwindow object
     */
	private void limitX(SubWindowObject sWO) {
		
		int x = sWO.getLimitX()<sWO.getWinLimitX()?sWO.getLimitX():sWO.getWinLimitX();
		x = (((sWO.getX()+sWO.sizeX)-sWO.borderSize)<x?((sWO.getX()+sWO.sizeX)-sWO.borderSize):x);
		super.setLimitX(x);
		
		for(String name : objectsByName.keySet()) {
			objectsByName.get(name).setLimitX(x);
		}
	}
	
	/**
     * Sets the maximum Y bound for this window based on the parent window's limits.
     *
     * @param sWO the parent subwindow object
     */
	private void limitY(SubWindowObject sWO) {
		
		int y = sWO.getLimitY()<sWO.getWinLimitY()?sWO.getLimitY():sWO.getWinLimitY();
		y = (((sWO.getY()+sWO.sizeY)-sWO.getTopSize())<y?((sWO.getY()+sWO.sizeY)-sWO.getTopSize()):y);
		
		super.setLimitY(y);
		
		for(String name : objectsByName.keySet()) {
			objectsByName.get(name).setLimitY(y);
		}
	}
	
	  /**
     * Sets the minimum X bound for this window based on the parent window's limits.
     *
     * @param sWO the parent subwindow object
     */
	private void minX(SubWindowObject sWO) {
		
		int x = sWO.getMinX()>sWO.getWinMinX()?sWO.getMinX():sWO.getWinMinX();
		x = (sWO.getX()+sWO.borderSize>x?sWO.getX()+sWO.borderSize:x);
		
		super.setMinX(x);
		for(String name : objectsByName.keySet()) {
			objectsByName.get(name).setMinX(x);
		}
	}
	
	 /**
     * Sets the minimum Y bound for this window based on the parent window's limits.
     *
     * @param sWO the parent subwindow object
     */
	private void minY(SubWindowObject sWO) {
		
		int y = sWO.getMinY()>sWO.getWinMinY()?sWO.getMinY():sWO.getWinMinY();
		y = (sWO.getY()+sWO.borderSize>y?sWO.getY()+sWO.borderSize:y);
		
		super.setMinY(y);
		for(String name : objectsByName.keySet()) {
			objectsByName.get(name).setMinY(y);
		}
	}
	
	  /**
     * Applies window bounds (min/max X/Y) to an object inside this subwindow.
     *
     * @param o the object to constrain
     */
	private void setObjectWindowLimit(GUIObject o) {
		
		if(o.getClass()==SubWindowObject.class) {
			SubWindowObject sO = (SubWindowObject) o;
			sO.limitX(this);
			sO.minX(this);
			sO.limitY(this);
			sO.minY(this);
			return;
		}
		
		int myMinX = this.getMinX()>this.getWinMinX()?this.getMinX():this.getWinMinX();
		int myMinY = this.getMinY()>this.getWinMinY()?this.getMinY():this.getWinMinY();
		
		myMinX = this.getX()+this.borderSize>myMinX?this.getX()+this.borderSize:myMinX;
		myMinY = this.getY()+this.borderSize>myMinY?this.getY()+this.borderSize:myMinY;
				
		
		o.setWinMinX(myMinX);
		o.setWinMinY(myMinY);
		
		int myMaxX = this.getLimitX()<this.getWinLimitX()?this.getLimitX():this.getWinLimitX();
		int myMaxY = this.getLimitY()<this.getWinLimitY()?this.getLimitY():this.getWinLimitY();
		
		myMaxX = ((this.getX()+this.sizeX)-this.borderSize)<myMaxX?((this.getX()+this.sizeX)-this.borderSize):myMaxX;
		myMaxY = ((this.getY()+this.sizeY)-this.getTopSize())<myMaxY?((this.getY()+this.sizeY)-this.getTopSize()):myMaxY;
		
		o.setWinLimitX(myMaxX);
		o.setWinLimitY(myMaxY);
	}
	
	private int mouseWheelScrollMultiplier = 32;
	
	/**
     * Returns the current multiplier used for mouse wheel scroll calculations.
     *
     * @return the scroll multiplier
     */
	public int getMouseWheelScrollMultiplier() {
		return this.mouseWheelScrollMultiplier;
	}
	
	 /**
     * Sets the multiplier applied to mouse wheel scroll offsets.
     *
     * @param i the new scroll multiplier
     */
	public void setMouseScrollMultiplier(int i) {
		this.mouseWheelScrollMultiplier=i;
	}
	
	 /**
     * Marks a {@link GUIObject} as scrollable within this subwindow.
     *
     * @param o the object to make scrollable
     */
	public void makeObjectScrollable(GUIObject o) {
		this.objectConstraintOffset.put(o.name,ObjectUtils.getLongFromInts(0,0));
	}
	
	/**
     * Positions and/or clamps a child {@link GUIObject} within this subwindow according to a constraint.
     * <p>
     * This method:
     * <ul>
     *   <li>Applies optional scroll offsets (mouse wheel and drag scroll) if the object was made scrollable.</li>
     *   <li>Clamps the object to the subwindow’s interior when {@code constraint != FREE}.</li>
     *   <li>Places the object based on the specified constraint anchor (e.g., top-left, center, etc.).</li>
     *   <li>Updates the child’s effective window limits via {@link #setObjectWindowLimit(GUIObject)}.</li>
     * </ul>
     * Supported constraints:
     * <ul>
     *   <li>{@link #FREE} – freely positionable; only limits are updated.</li>
     *   <li>{@link #MOVABLE} – same as FREE; object is intended to be user-movable.</li>
     *   <li>Anchors: {@link #TOP_LEFT} (default), {@link #TOP_CENTER}, {@link #TOP_RIGHT},
     *       {@link #CENTER_LEFT}, {@link #CENTER}, {@link #CENTER_RIGHT},
     *       {@link #BOTTOM_LEFT}, {@link #BOTTOM_CENTER}, {@link #BOTTOM_RIGHT}.</li>
     *   <li>Titlebar/control slots: {@link #TITLE}, {@link #MIN}, {@link #MAX_SPLIT}, {@link #EXIT}.</li>
     * </ul>
     * For {@link #TITLE}, the method also elides the title text with {@code "..."} if it would overlap the
     * minimize button.
     *
     * @param o          the child object to constrain
     * @param constraint one of the constraint constants defined on {@code SubWindowObject}
     */
	private void constrainObject(GUIObject o, int constraint) {
		
		int offsetX = 0;
		int offsetY = 0;
		
		if(this.objectConstraintOffset.containsKey(o.name)) {
			int[] xy = ObjectUtils.getIntegersFromLong(objectConstraintOffset.get(o.name));
			
			offsetX = xy[0];
			offsetY = xy[1] - (this.mouseWheelScrolls*mouseWheelScrollMultiplier);
			
			if(this.doesDragScroll) {
				offsetX = xy[0] - this.dragScrollX;
				offsetY = xy[1] - this.dragScrollY;
			}
		}
		
		if(constraint!=FREE) {
			if(o.getX()<this.getX()) {
				o.setX(this.getX());
			}
			if(o.getY()<this.getY()) {
				o.setY(this.getY());
			}
			if(o.getX()+o.sizeX>this.getX()+this.sizeX) {
				o.setX((this.getX()+this.sizeX)-o.sizeX);
			}
			if(o.getY()+o.sizeY>this.getY()+(this.sizeY-this.topSize)) {
				o.setY((this.getY()+(this.sizeY-this.topSize))-o.sizeY);
			}
		}
		
		switch(constraint) {
		
		// Move around anywhere
		case FREE:
			setObjectWindowLimit(o);
			break;
			
		// Move within the confines
		case MOVABLE:
			setObjectWindowLimit(o);
			break;
		// TOP_LEFT OR DEFAULT
		default:
			o.setX(this.getX()+this.borderSize+offsetX);
			o.setY((((this.getY()+this.sizeY)-topSize)-o.sizeY)+this.borderSize+offsetY);
			
			setObjectWindowLimit(o);
			break;
		case BOTTOM_LEFT:
			
			o.setX(this.getX()+this.borderSize+offsetX);
			o.setY(this.getY()+this.borderSize+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case CENTER_LEFT:
			
			o.setX(this.getX()+this.borderSize+offsetX);
			o.setY(((this.getY()+((this.sizeY-topSize)/2))-(o.sizeY/2))+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case CENTER:
			o.setX(((this.getX()+((this.sizeX)/2))-(o.sizeX/2))+offsetX);
			o.setY(((this.getY()+((this.sizeY-topSize)/2))-(o.sizeY/2))+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case CENTER_RIGHT:
			o.setX(((this.getX()+this.sizeX)-o.sizeX)+offsetX);
			o.setY(((this.getY()+((this.sizeY-topSize)/2))-(o.sizeY/2))+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case BOTTOM_RIGHT:
			o.setX(((this.getX()+this.sizeX)-o.sizeX)+offsetX);
			o.setY((this.getY()+this.borderSize)+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case BOTTOM_CENTER:
			o.setX(((this.getX()+((this.sizeX)/2))-(o.sizeX/2))+offsetX);
			o.setY((this.getY()+this.borderSize)+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case TOP_CENTER:
			o.setX(((this.getX()+((this.sizeX)/2))-(o.sizeX/2))+offsetX);
			o.setY((((this.getY()+this.sizeY)-topSize)-o.sizeY)+this.borderSize+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case TOP_RIGHT:
			o.setX(((this.getX()+this.sizeX)-o.sizeX)+offsetX);
			o.setY((((this.getY()+this.sizeY)-topSize)-o.sizeY)+this.borderSize+offsetY);
			
			setObjectWindowLimit(o);
			break;
			
		case EXIT:
			o.setX((this.getX()+this.sizeX)-o.sizeX);
			o.setY((this.getY()+this.sizeY)-o.sizeY);
			break;
		case MAX_SPLIT:
			o.setX((this.getX()+this.sizeX)-((o.sizeX*2)+this.borderSize));
			o.setY((this.getY()+this.sizeY)-o.sizeY);
			break;
		case MIN:
			o.setX((this.getX()+this.sizeX)-((o.sizeX*3)+(this.borderSize*2)));
			o.setY((this.getY()+this.sizeY)-o.sizeY);
			break;
		case TITLE:
			o.setX(this.getX()+(this.borderSize)+4);
			o.setY((this.getY()+this.sizeY)-((int)(o.sizeY*1.25f)+1));
			
			if(minButton!=null) {
				if(!minButton.isDestroyed()) {
					if(o.getClass()==TextObject.class) {
						TextObject tO = (TextObject) o;
					
						int titleTextWidth = tO.getTextWidth(titleText);
						if(titleTextWidth>(minButton.x-this.x)) {
							tO.setText(titleText.substring(0,4<=tO.getText().length()?4:tO.getText().length())+"...");
						} else {
							tO.setText(titleText);
						}
					}
				}
			}
			break;
		}
	}
	
	/**
     * Changes the constraint bucket for an existing child and reapplies positioning.
     * <p>
     * This removes {@code o} from its current internal list and reassigns it to the list
     * for {@code constraint}, then immediately calls {@link #constrainObject(GUIObject, int)}
     * to update its position and limits.
     * </p>
     *
     * @param o          the child object whose constraint should change
     * @param constraint the new constraint (see constants like {@link #FREE}, {@link #CENTER}, etc.)
     */
	public void changeConstraint(GUIObject o, int constraint) {
		o.constraint=constraint;
		
		gui.println("[SUBWINDOWOBJECT] changing constraint of "+o.name+" in "+this.name+" to "+constraint);
		
		boolean shouldBreak=false;
		for(Integer l : objects.keySet()) {
			if(objects.get(l).size()>0) {
				for(int i=0;i<objects.get(l).size();i++) {
					if(objects.get(l).get(i).name.contentEquals(o.name)) {
						objects.get(l).remove(i);
						shouldBreak=true;
						break;
					}
				}
			}
			if(shouldBreak) {
				break;
			}
		}
		if(objects.containsKey(constraint)) {
			objects.get(constraint).add(o);
		} else {
			ArrayList<GUIObject> l = new ArrayList<>();
			l.add(o);
			objects.put(constraint,l);
		}
		constrainObject(o,constraint);
	} 
	
	/**
     * Removes a child object by name from this subwindow.
     * <p>
     * This method deletes the reference from internal collections. If the removed object is itself a
     * {@link SubWindowObject}, its parent/containment flags are reset. This does <em>not</em> call
     * {@link GUIObject#destroyObject()} on the removed object; it only detaches it from this container.
     * </p>
     *
     * @param name the name of the child object to remove
     */
	public void remove(String name) {
		
		if(windows.containsKey(name)) {
			windows.remove(name);
		}
		objectsByName.remove(name);
		
		for(Integer c : objects.keySet()) {
			if(objects.get(c).size()>0) {
				for(int i=0;i<objects.get(c).size();i++) {
					if(objects.get(c).get(i).name.contentEquals(name)) {
						
						if(objects.get(c).get(i).getClass()==SubWindowObject.class) {
							SubWindowObject sO = (SubWindowObject)(objects.get(c).get(i));
							sO.insideAnotherSubWindow=false;
							sO.parentWindow=null;
						}
						
						objects.get(c).remove(i);
						return;
					}
				}
				
			}
		}
	}
	
	 /**
     * Destroys this subwindow and all of its children.
     * <p>
     * Steps performed:
     * <ol>
     *   <li>Unregisters this window from the global {@code windows} map.</li>
     *   <li>Rebuilds the {@code windowsByLayer} index.</li>
     *   <li>Resets the cursor on the owning window.</li>
     *   <li>Calls {@link GUIObject#destroyObject()} on all child objects and clears internal collections.</li>
     *   <li>Invokes {@code super.destroyObject()} to release this object’s own resources.</li>
     * </ol>
     * </p>
     */
	@Override
	public void destroyObject() {
		
		windows.remove(this.name);
		
		// fixes the windows by layer private system
		windowsByLayer.clear();
		for(String name : windows.keySet()) {
			windowsByLayer.put(windows.get(name).layer,windows.get(name));
		}
		
		gui.getWindow().resetCursor();
		
		for(Integer c : objects.keySet()) {
			if(objects.get(c).size()>0) {
				for(int j = 0; j <objects.get(c).size();j++) {
					objects.get(c).get(j).destroyObject();
				}
			}
		}
		
		objects.clear();
		objectsByName.clear();
		
		// self destruction
		super.destroyObject();
	}

	 /**
     * Per-frame update for this subwindow.
     * <p>
     * Behavior:
     * <ul>
     *   <li>No-op when {@code showFrame == false}.</li>
     *   <li>Updates title color based on focus state.</li>
     *   <li>Enables/disables window control buttons depending on focus, dragging, and resizing state.</li>
     *   <li>If dragging, repositions the window within the screen bounds.</li>
     *   <li>Re-applies constraints for all child objects each frame.</li>
     *   <li>Handles clicks on close/minimize (destroys the window).</li>
     *   <li>Handles maximize/split toggle: maximizes to parent or full window, and restores on split.</li>
     * </ul>
     *
     * @param showFrame whether this frame should be processed and drawn
     */
	@Override
	public void update(boolean showFrame) {
		
		if(!showFrame) {
			return;
		}
		
		if(this.title!=null) {
			if(!this.isFocused()) {
				this.title.setColor(this.unfocusedTitleColor);
			} else {
				this.title.setColor(this.titleColor);
			}
		}
		
		if(!this.isFocused() && decorated) {
			exitButton.disable();
			
			if(maxButton!=null) {maxButton.disable();}
			
			if(splitButton!=null) {splitButton.disable();}
			
			minButton.disable();
		}
		
		if(resizing) {
			exitButton.disable();
		} else {
			if(focused) {
				exitButton.enable();
			};
		}
		
		if(dragging) {
			
			if(decorated) {
				exitButton.disable();
				
				if(maxButton!=null) {maxButton.disable();}
				
				minButton.disable();
			}
			
			double mX = gui.getWindow().getMouseListener().getX();
			double mY = gui.getWindow().getMouseListener().getY();
			double changeX = (mX - lastMX);
			double changeY = (mY - lastMY);
			//int changeY = (int) (gui.getWindow().getMouseListener().getY() - lastMY);
			
			int fX = (int) (this.getX()+(changeX));
			int fY = (int) (this.getY()-(changeY));
			
			if(fX<0) {
				fX=0;
			} else if(fX+this.sizeX>gui.getWindow().getSizeX()) {
				fX = gui.getWindow().getSizeX()-this.sizeX;
			}
			if(fY<0) {
				fY=0;
			} else if(fY+this.sizeY>gui.getWindow().getSizeY()) {
				fY=gui.getWindow().getSizeY()-this.sizeY;
			}
			
			this.setX(fX);
			this.setY(fY);
			
			lastMX = mX;
			lastMY = mY;
		} else if(decorated && this.isFocused()){
			
			if(!resizing) {
				exitButton.enable();
				if(maxButton!=null) {maxButton.enable();}
				if(splitButton!=null) {splitButton.enable();}
			
				minButton.enable();
			}
		}
		
			for(Integer constraint : objects.keySet()) {
				if(!objects.get(constraint).isEmpty()) {
					for(int i=0;i<objects.get(constraint).size();i++) {
						this.constrainObject(objects.get(constraint).get(i),constraint);
					}
				}
			}
		
		if(!decorated) {
			return;
		}
		if(exitButton.readPress() || minButton.readPress()) {
			this.destroyObject();
		}
		
		if(!resizable) {
			return;
		}
		
		if(maxButton!=null) {
			if(maxButton.readPress()) {
				this.splitSizeX=this.sizeX;
				this.splitSizeY=this.sizeY;
				
				
				
				if(this.parentWindow!=null) {
					this.resize(parentWindow.sizeX-(parentWindow.borderSize*2),
								(parentWindow.sizeY)-(parentWindow.topSize+parentWindow.borderSize));
					parentWindow.changeConstraint(this,TOP_LEFT);
				} else {
					this.resize(gui.getWindow().getSizeX(),gui.getWindow().getSizeY());
				}
				
				this.splitX=this.x;
				this.splitY=this.y;
				
				this.x=0;
				this.y=0;
				
				this.remove("max:"+name);
				this.maxButton.destroyObject();
				this.maxButton=null;
				
				if(this.splitButton==null) {
					
					this.splitButton = ObjectUtils.createButtonObject("split:"+name,
						 this.layer+OBJECT_LAYER_FACTOR,
						 gui,
						 0,
						 0,
						 58,
						 32,
						 true,
						 true,
						 "data/split.png",
						 "data/selectedSplit.png",
						 "data/clickedSplit.png");
					this.add(this.splitButton,MAX_SPLIT);
				}
			}
		} else {
			if(splitButton.readPress()) {
				
				if(this.parentWindow!=null) {
					parentWindow.changeConstraint(this,this.originalConstraint);
					//gui.println("ADDED "+this.name+" WITH ORIGINAL CONSTRAINT: "+this.originalConstraint);
				}
				
				this.resize(this.splitSizeX,this.splitSizeY);
				
				this.x=this.splitX;
				this.y=this.splitY;
				
				this.remove("split:"+name);
				this.splitButton.destroyObject();
				this.splitButton=null;
				
				if(this.maxButton==null) {
				
				this.maxButton = ObjectUtils.createButtonObject("max:"+name,
						 this.layer+OBJECT_LAYER_FACTOR,
						 gui,
						 0,
						 0,
						 58,
						 32,
						 true,
						 true,
						 "data/max.png",
						 "data/selectedMax.png",
						 "data/clickedMax.png");
				this.add(maxButton,MAX_SPLIT);
				}
			}
		}
	}
	
	/**
     * Horizontal and vertical split size values for window partitioning.
     */
	private int splitSizeX,splitSizeY;
	
	 /**
     * Current horizontal and vertical split positions.
     */
	private int splitX,splitY;
	
	 /**
     * Resizes this window object, enforcing minimum (225x225) and maximum
     * bounds based on the current window size.
     *
     * @param x new width in pixels
     * @param y new height in pixels
     */
	public void resize(int x, int y) {
		
		if(x<225) {
			x=225;
		}
		if(y<225) {
			y=225;
		}
		
		if(x>gui.getWindow().getSizeX()) {
			x=gui.getWindow().getSizeX();
		}
		if(y>gui.getWindow().getSizeY()) {
			y=gui.getWindow().getSizeY();
		}
		
		this.setX(this.x);
		this.setY(this.y);
		
		this.sizeX=x;
		this.sizeY=y;
	}
	

    /**
     * Sets the rendering layer of this window and updates all contained objects
     * to maintain correct z-ordering.
     *
     * @param layer new layer index
     */
	@Override
	public void setLayer(int layer) {
		super.setLayer(layer);
		for(String name : objectsByName.keySet()) {
			if(objectsByName.get(name).getClass()==SubWindowObject.class) {
				SubWindowObject sO = (SubWindowObject) objectsByName.get(name);
				sO.setLayer(layer+SUBWINDOW_LAYER_FACTOR);
				sO.layerThisWindowInParent();
			} else {
				
				if(objectLayersInWindow.containsKey(name)) {
					objectsByName.get(name).setLayer(layer+OBJECT_LAYER_FACTOR+objectLayersInWindow.get(name));
				} else {
					objectsByName.get(name).setLayer(layer+OBJECT_LAYER_FACTOR);
				}
			}
		}
	}
	
	  /**
     * Gets the highest layer value used by this window and its children.
     *
     * @return highest layer index
     */
	public int getHighestLayer() {
		int highestLayer = this.layer;
		for(String name : objectsByName.keySet()) {
			if(objectsByName.get(name).getLayer()>highestLayer) {
				if(objectsByName.get(name).getClass()==SubWindowObject.class) {
					SubWindowObject sWO = (SubWindowObject) objectsByName.get(name);
					highestLayer=sWO.getHighestLayer();
				} else {
					highestLayer=objectsByName.get(name).getLayer();
				}
			}
		}
		return highestLayer;
	}
	
	 /**
     * Gets the highest layer value used by this window and its children,
     * excluding a specific object.
     *
     * @param ignore object to ignore
     * @return highest layer index without {@code ignore}
     */
	public int getHighestLayer(GUIObject ignore) {
		int highestLayer = this.layer;
		for(String name : objectsByName.keySet()) {
			if(objectsByName.get(name).getLayer()>highestLayer && objectsByName.get(name)!=ignore) {
				
				if(objectsByName.get(name).getClass()==SubWindowObject.class) {
					SubWindowObject sWO = (SubWindowObject) objectsByName.get(name);
					highestLayer=sWO.getHighestLayer();
				} else {
					highestLayer=objectsByName.get(name).getLayer();
				}
			}
		}
		return highestLayer;
	}
	
	 /**
     * Forces this window to the top of the rendering stack among other windows
     * and sets focus.
     *
     * @param otherWindows map of layers to subwindows
     */
	private void layerThisWindow(TreeMap<Integer,ArrayList<SubWindowObject>> otherWindows) {
		for(String name : windows.keySet()) {
			if(windows.get(name)!=this) {
				windows.get(name).unfocus();
			}
		}
		int highestLayer = otherWindows.lastKey();
		ArrayList<SubWindowObject> highestWindows = otherWindows.get(otherWindows.lastKey());
		for(int i=0;i<highestWindows.size();i++) {
			int fetchedHighestLayer = highestWindows.get(i).getHighestLayer();
			if(fetchedHighestLayer>highestLayer) {
				highestLayer=fetchedHighestLayer;
			}
		}
		
		int nL = highestLayer;
		this.setLayer(nL);
		this.focus();
	}
	
	 /**
     * Adjusts this subwindow's layer within its parent window to maintain
     * correct z-ordering.
     */
	private void layerThisWindowInParent() {
		
		SubWindowObject highestLocalWindow = this.parentWindow.getHighestLocalWindow(this);
		int highestSubLayer;
		if(highestLocalWindow==null) {
			this.focus();
			return;
		} else {
			highestSubLayer = highestLocalWindow.getHighestLayer(this);
		}
		
		int nL = highestSubLayer+SUBWINDOW_LAYER_FACTOR;
		
		this.setLayer(nL);
		this.focus();
		
		
		for(String name : windows.keySet()) {
			if(windows.get(name)!=this && windows.get(name).absoluteParentWindow==this.absoluteParentWindow) {
				windows.get(name).unfocus();
			}
		}
	}
	
	 /**
     * Brings this window to focus, forcing it to the top of the rendering
     * hierarchy.
     */
	public void forceFocus() {
		TreeMap<Integer,ArrayList<GUIObject>> otherObjects = gui.getObjectsByLayer();
		TreeMap<Integer,ArrayList<SubWindowObject>> otherWindows = new TreeMap<>();
		
		// Finds every existing window and places it in the otherWindows list
		
		for(Integer l : otherObjects.keySet()) {
			ArrayList<GUIObject> list = otherObjects.get(l);
			if(!list.isEmpty()) {
				for(int i = 0; i < list.size();i++) {
					if(list.get(i).getClass()==SubWindowObject.class) {
						if(otherWindows.containsKey(l)) {
							otherWindows.get(l).add((SubWindowObject)list.get(i));
						} else {
							ArrayList<SubWindowObject> sWL = new ArrayList<>();
							sWL.add((SubWindowObject)list.get(i));
							otherWindows.put(l,sWL);
						}
					}
				}
			}
		}
		layerThisWindow(otherWindows);
	}
	
	  /**
     * Requests focus for this window if the given mouse coordinates are within
     * bounds and not obstructed by another window.
     *
     * @param mX mouse X coordinate
     * @param mY mouse Y coordinate
     */
	private void requestFocus(double mX, double mY) {
		
		if(this.isFocused()) {
			return;
		}
		
		boolean areWeInBounds = (mX>=this.x && mX <=(this.x+this.sizeX) && mY>=this.y && mY <=(this.y+this.sizeY));
		boolean anotherWindowCovers = false;
		int focus = 0;
		int count = 0;
		
		
		if(this.insideAnotherSubWindow) {
			
			if(areWeInBounds && this.absoluteParentWindow.isFocused()) {
				
				for(String name : windows.keySet()) {
					if(windows.get(name).getLayer()>this.getLayer()) {
						if(windows.get(name).interactionInVisibleBounds((int)mX,(int)mY) &&
						  (mX>=windows.get(name).x && mX <=windows.get(name).x+windows.get(name).sizeX) &&
						   mY>=windows.get(name).y && mY <=(windows.get(name).y+windows.get(name).sizeY)) {
							return;
						}
					}
				}
				
				
				ArrayList<SubWindowObject> ws = new ArrayList<>();
				SubWindowObject o = this;
				while(true) {
					if(o.parentWindow!=null) {
						if(!o.parentWindow.isFocused()) {
							ws.add(o.parentWindow);
							o = o.parentWindow;
						} else {
							break;
						}
					} else {
						break;
					}
				}
				if(!ws.isEmpty()) {
					for(int i = ws.size()-1;i>-1;i--) {
						ws.get(i).layerThisWindowInParent();
					}
				}
				
				layerThisWindowInParent();
				gui.println("Focused "+this.name+" inside window "+parentWindow.name);
				return;
			}
			
			if(!areWeInBounds) {
				return;
			}
			if(!this.absoluteParentWindow.isFocused()) {
				return;
			}
			return;
		}
		
		TreeMap<Integer,ArrayList<GUIObject>> otherObjects = gui.getObjectsByLayer();
		TreeMap<Integer,ArrayList<SubWindowObject>> otherWindows = new TreeMap<>();
		
		// Finds every existing window and places it in the otherWindows list
		
		for(Integer l : otherObjects.keySet()) {
			ArrayList<GUIObject> list = otherObjects.get(l);
			if(!list.isEmpty()) {
				for(int i = 0; i < list.size();i++) {
					if(list.get(i).getClass()==SubWindowObject.class) {
						if(otherWindows.containsKey(l)) {
							otherWindows.get(l).add((SubWindowObject)list.get(i));
						} else {
							ArrayList<SubWindowObject> sWL = new ArrayList<>();
							sWL.add((SubWindowObject)list.get(i));
							otherWindows.put(l,sWL);
						}
					}
				}
			}
		}
		
		if(areWeInBounds) {
			for(Integer l : otherWindows.keySet()) {
				ArrayList<SubWindowObject> list = otherWindows.get(l);
				if(!list.isEmpty()) {
					for(int i = 0; i < list.size();i++) {
						if(list.get(i)!=this && list.get(i).absoluteParentWindow!=this) {
							// checks if the click is in bounds of some window
							
							if(mX>=list.get(i).x && mX <=(list.get(i).x+list.get(i).sizeX) &&
								mY>=list.get(i).y && mY <=(list.get(i).y+list.get(i).sizeY) &&
								list.get(i).interactionInVisibleBounds((int)mX, (int)mY)) {
								
								// since we are both in bounds
								// check if we are on top,
								// if not, another window covers
								if(this.getLayer()>list.get(i).getLayer()) {
									focus++;
								} else {
									anotherWindowCovers=true;
								}
								count++;
							}
						}
					}
				}
			}
			
			// this makes it work for some reason
			if(count==focus) {
				layerThisWindow(otherWindows);
			}
			
			// if we didn't find another window that covers up,
			// we are chill
			if(!anotherWindowCovers) {
				layerThisWindow(otherWindows);
			}
		}
	}

	/**
     * Sends a key input event to this window.
     *
     * @param key    key code
     * @param action key action (press/release)
     */
	@Override
	public void sendKey(int key, int action) {}
	
	private static final double OUT_OF_BOUNDS = -9999;
	private double changeInMouseX = 0;
	private double changeInMouseY = 0;
	private double lastMouseX = OUT_OF_BOUNDS;
	private double lastMouseY = OUT_OF_BOUNDS;
	
	private boolean forceLeftResize = false;
	private boolean forceRightResize = false;
	private boolean forceDownResize = false;
	private boolean forceUpResize = false;
	
	private boolean forceBottomLeftResize = false;
	private boolean forceBottomRightResize=false;
	private boolean forceTopRightResize=false;
	private boolean forceTopLeftResize=false;
	
	private boolean resizing=false;
	private boolean mouseIsChanged=false;
	
	private static final int RESIZE_RANGE = 10;
	
	
	private boolean mouseOnLeft=false,mouseOnRight=false,mouseOnDown=false,mouseOnUp=false;
	
	 /**
     * Checks whether the mouse is within resizing bounds and updates cursor
     * state accordingly.
     *
     * @param xPos mouse X coordinate
     * @param yPos mouse Y coordinate
     * @return {@code true} if resizing or cursor is on an edge, otherwise {@code false}
     */
	private boolean checkResizing(double xPos,double yPos) {
		
		if(!decorated || !resizable || dragging) {
			resetAllResizeVariables();
			return false;
		}
		
		if(exitButton.getInBounds() && exitButton.isEnabled()) {
			resetAllResizeVariables();
			return false;
		}
		
		if(!interactionInVisibleBounds((int)xPos,(int)yPos)) {
			resetAllResizeVariables();
			mouseOnLeft = false;
			mouseOnRight = false;
			mouseOnDown = false;
			mouseOnUp = false;
			return false;
		}
		
		boolean insideBox = false;
		if(xPos>this.getX()+1 && xPos<(this.getX()+(this.sizeX-1)) &&
		   yPos>this.getY()+1 && yPos<(this.getY()+(this.sizeY-1))) {
			insideBox=true;
		}
		
		mouseOnLeft = xPos<this.getX() && xPos>=this.getX()-RESIZE_RANGE && yPos>=this.getY()-RESIZE_RANGE && yPos<=((this.sizeY+this.getY())+RESIZE_RANGE);
		mouseOnRight = xPos>=(this.getX()+this.sizeX)-2 && xPos<=(this.getX()+this.sizeX+RESIZE_RANGE) && yPos>=this.getY()-RESIZE_RANGE && yPos<=((this.sizeY+this.getY())+RESIZE_RANGE);
		mouseOnDown = yPos <= this.getY() && yPos >= this.getY()-RESIZE_RANGE && xPos>this.getX()-RESIZE_RANGE && xPos<this.getX()+this.sizeX+RESIZE_RANGE;
		mouseOnUp = yPos>=this.getY()+this.sizeY && yPos <= this.getY()+this.sizeY+RESIZE_RANGE && xPos<this.getX()+this.sizeX+RESIZE_RANGE && xPos>this.getX()-RESIZE_RANGE;
		
		if(!insideBox) {
			
			mouseIsChanged=(mouseOnLeft||mouseOnRight||mouseOnDown||mouseOnUp);
			
			
			if((mouseOnLeft && mouseOnDown) || (mouseOnRight && mouseOnUp) && !resizing) {
				gui.getWindow().setCursor(gui.diagonalLeftMouse);
				if(pressing) {
					resizing=true;
				}
				
			} else if ((mouseOnLeft && mouseOnUp) || (mouseOnRight && mouseOnDown) && !resizing){
				
				gui.getWindow().setCursor(gui.diagonalRightMouse);
				if(pressing) {
					resizing=true;
				}
				
			} else if((mouseOnLeft||mouseOnRight) && !resizing) {
				gui.getWindow().setCursor(gui.horizontalMouse);
				
				// when we are in range, if we press, we should
				// override, this fixes a bug
				if(pressing) {
					resizing=true;
				}
			} else if((mouseOnDown || mouseOnUp) && !resizing) {
				gui.getWindow().setCursor(gui.verticalMouse);
				if(pressing) {
					resizing=true;
				}
			}
			
		} else {
			if(!pressing) {
				if(!anyWindowsInResizeRange()) {gui.getWindow().resetCursor();}
				mouseIsChanged=false;
			}
		}
		
		if(pressing && !resizing) {
			if(!anyWindowsInResizeRange()) {gui.getWindow().resetCursor();}
			mouseIsChanged=false;
		}
		
		if(pressing) {
			if(resizing) {
					
					double currentMouseX = gui.getWindow().getMouseListener().getX();
					double currentMouseY = gui.getWindow().getMouseListener().getY();
					
					if(lastMouseX==OUT_OF_BOUNDS) {
						lastMouseX=currentMouseX;
					}
					if(lastMouseY==OUT_OF_BOUNDS) {
						lastMouseY=currentMouseY;
					}
					
					changeInMouseX=lastMouseX-currentMouseX;
					changeInMouseY=lastMouseY-currentMouseY;
					
					if((mouseOnRight && mouseOnUp) || forceTopRightResize) {
						
						this.resize((int)(this.sizeX+(changeInMouseX*-1)),(int)(this.sizeY+(changeInMouseY)));
						forceTopRightResize=true;
						resizing=true;
						
					} else if((mouseOnLeft && mouseOnDown) || forceBottomLeftResize) {
						
						this.setX((int)(this.x-changeInMouseX));
						this.setY((int)(this.y+changeInMouseY));
						
						forceBottomLeftResize=true;
						this.resize((int)(this.sizeX+changeInMouseX),(int)(this.sizeY+(changeInMouseY*-1)));
						resizing=true;
						
					} else if ((mouseOnRight && mouseOnDown) || forceBottomRightResize) {
						
						
						this.resize((int)(this.sizeX+(changeInMouseX*-1)),(int)(this.sizeY+(changeInMouseY*-1)));
						this.setY((int)(this.y+changeInMouseY));
						forceBottomRightResize=true;
						resizing=true;
						
					} else if((mouseOnLeft && mouseOnUp) || forceTopLeftResize) {
						
						this.setX((int)(this.x-changeInMouseX));
						this.resize((int)(this.sizeX+changeInMouseX),(int)(this.sizeY+(changeInMouseY)));
						forceTopLeftResize=true;
						resizing=true;
						
					} else if((mouseOnLeft || forceLeftResize) && !forceDownResize && !forceUpResize) {
						
						this.setX((int)(this.x-changeInMouseX));
						this.resize((int)(this.sizeX+changeInMouseX),this.sizeY);
						forceLeftResize=true;
						resizing=true;
						
					} else if((mouseOnRight || forceRightResize) && !forceDownResize  && !forceUpResize) {
						
						this.resize((int)(this.sizeX+(changeInMouseX*-1)),this.sizeY);
						forceRightResize=true;
						resizing=true;
					} else if(mouseOnDown || forceDownResize) {
						
						this.resize(this.sizeX,(int)(this.sizeY+(changeInMouseY*-1)));
						this.setY((int)(this.y+changeInMouseY));
						resizing=true;
						forceDownResize=true;
					} else if (mouseOnUp || forceUpResize) {
						this.resize(this.sizeX,(int)(this.sizeY+(changeInMouseY)));
						resizing=true;
						forceUpResize=true;
					}
					
					lastMouseX=currentMouseX;
					lastMouseY=currentMouseY;
				}
		}  else {
			forceLeftResize=false;
			forceRightResize=false;
			forceDownResize=false;
			forceUpResize=false;
			
			forceBottomLeftResize=false;
			forceBottomRightResize=false;
			forceTopRightResize=false;
			forceTopLeftResize=false;
			
			resizing=false;
			lastMouseX=OUT_OF_BOUNDS;
			lastMouseY=OUT_OF_BOUNDS;
		}
		
		if(!mouseOnLeft && !mouseOnRight && !mouseOnUp && !mouseOnDown && !resizing && !pressing) {
			if(!anyWindowsInResizeRange()) {gui.getWindow().resetCursor();}
		}
		
		
		return (resizing || mouseIsChanged);
	}
	
	private boolean avoidReConstrainX=false,avoidReConstrainY=false;
	
	
	/**
     * Safely sets this object's X position, constraining it to window bounds
     * and applying parent constraints if necessary.
     *
     * @param x new X position
     */
	@Override
	public void setX(int x) {
		if(x<0) {
			x=0;
		} else if(x+this.sizeX>gui.getWindow().getSizeX()) {
			x=gui.getWindow().getSizeX()-this.sizeX;
		}
		this.x=x;
		
		if(this.parentWindow!=null) {
			if(!avoidReConstrainX) {
				avoidReConstrainX=true;
				this.parentWindow.constrainObject(this,this.constraint);
				avoidReConstrainX=false;
			}
		}
	}
	
	/**
     * Safely sets this object's Y position, constraining it to window bounds
     * and applying parent constraints if necessary.
     *
     * @param y new Y position
     */
	@Override
	public void setY(int y) {
		if(y<0) {
			y=0;
		} else if(y+this.sizeY>gui.getWindow().getSizeY()) {
			y=gui.getWindow().getSizeY()-this.sizeY;
		}
		this.y=y;
		
		if(this.parentWindow!=null) {
			if(!avoidReConstrainY) {
				avoidReConstrainY=true;
				this.parentWindow.constrainObject(this,this.constraint);
				avoidReConstrainY=false;
			}
		}
	}
	
	/**
     * Resets all resize-related state and cursor overrides.
     */
	private void resetAllResizeVariables() {
		forceLeftResize=false;
		forceRightResize=false;
		forceDownResize=false;
		forceUpResize=false;
		
		forceBottomLeftResize=false;
		forceBottomRightResize=false;
		forceTopRightResize=false;
		forceTopLeftResize=false;
		
		resizing=false;
		lastMouseX=OUT_OF_BOUNDS;
		lastMouseY=OUT_OF_BOUNDS;
		
		if(!anyWindowsInResizeRange()) {gui.getWindow().resetCursor();}
	}
	
	 /**
     * Checks whether the given window is free to interact, i.e. no other window
     * is currently dragging or resizing.
     *
     * @param o subwindow to test
     * @return {@code true} if free to interact, otherwise {@code false}
     */
	public static boolean freeToInteract(SubWindowObject o) {
		for(String name : windows.keySet()) {
			if(windows.get(name).name!=o.name) {
				if(windows.get(name).dragging||windows.get(name).resizing) {
					return false;
				}
			}
		}
		return true;
	}
	
	 /**
     * Checks whether the given window is free to interact, i.e. no other window
     * is currently dragging or resizing.
     *
     * @param o subwindow to test
     * @return {@code true} if free to interact, otherwise {@code false}
     */
	public static boolean anyWindowsInResizeRange() {
		for(String name : windows.keySet()) {
			if(windows.get(name).resizing||
			   windows.get(name).mouseOnDown||
			   windows.get(name).mouseOnUp||
			   windows.get(name).mouseOnRight||
			   windows.get(name).mouseOnLeft) {
				return true;
			}
		}
		return false;
	}

	/**
     * Sends the current mouse position to this window, updating resize and
     * drag state if applicable.
     */
	@Override
	public void sendMousePos(long window, double xPos, double yPos) {
		
		if(!freeToInteract(this)) {
			resetAllResizeVariables();
			return;
		}
		
		if(!this.isFocused()) {
			return;
		} else {
			
			if(checkResizing(xPos,gui.getWindow().getSizeY()-yPos)) {
				dragging=false;
				return;
			}
		}
		
		if(!interactionInVisibleBounds((int)xPos,(int)(gui.getWindow().getSizeY()-yPos))) {
			resetAllResizeVariables();
			return;
		}
		
		yPos=gui.getWindow().getSizeY()-yPos;
		
		inBounds=false;
		if(xPos>=this.getX() && xPos<=(this.getX()+this.sizeX) &&
		   yPos>=this.getY() && yPos<=(this.getY()+this.sizeY)) {
			inBounds=true;
			
			if(yPos>=(this.getY()+this.sizeY)-topSize && pressing) {
				if(!dragging && decorated) {
					// must not be in bounds of the window buttons to drag window
					
					if(maxButton!=null) {
						if(!exitButton.getInBounds() && !maxButton.getInBounds() && !minButton.getInBounds()) {
							lastMX=xPos;
							lastMY=gui.getWindow().getSizeY()-yPos;
							dragging=true;
						}
					} else {
						if(!resizable) {
							if(!exitButton.getInBounds() && !minButton.getInBounds()) {
								lastMX=xPos;
								lastMY=gui.getWindow().getSizeY()-yPos;
								dragging=true;
							}
						}
					}
				}
			}
			if(this.doesDragScroll) {
				if(initDrag) {
					this.dragScrollX -= ((int) gui.getWindow().getMouseListener().getX())-startDragX;
					this.dragScrollY += ((int) gui.getWindow().getMouseListener().getY())-startDragY;
					startDragX = (int) gui.getWindow().getMouseListener().getX();
					startDragY = (int) gui.getWindow().getMouseListener().getY();
				}
			}
		}
	}
	
	private double lastMX,lastMY;
	private boolean pressing=false;
	private boolean inBounds=false;
	private boolean dragging=false;
	
	private int startDragX = 0;
	private int startDragY = 0;
	private boolean initDrag = false;

	 /**
     * Sends a mouse button event to this window, handling dragging, resizing,
     * and scroll-drag interactions.
     */
	@Override
	public void sendMouseButton(long window, int button, int action, int mods) {
		
		if(button==GLFW.GLFW_MOUSE_BUTTON_1) {
			if(action==GLFW.GLFW_PRESS) {
				this.pressing=true;
			} else if(action==GLFW.GLFW_RELEASE) {
				this.pressing=false;
				this.dragging=false;
			}
		}
		
		if(this.doesDragScroll) {
			if(button==GLFW.GLFW_MOUSE_BUTTON_3) {
				if(!inBounds || !this.isFocused()) {
					initDrag=false;
				} else {
					if(action==GLFW.GLFW_PRESS) {
						if(!initDrag) {
							initDrag=true;
							startDragX = (int) gui.getWindow().getMouseListener().getX();
							startDragY = (int) gui.getWindow().getMouseListener().getY();
						}
					} else if(action==GLFW.GLFW_RELEASE) {
						if(initDrag) {
							initDrag=false;
							
							this.dragScrollX -= ((int) gui.getWindow().getMouseListener().getX())-startDragX;
							this.dragScrollY += ((int) gui.getWindow().getMouseListener().getY())-startDragY;
						}
					}
				}
			}
		}
		
		if(!inBounds) {
			if(action==GLFW.GLFW_RELEASE) {
				this.dragging=false;
			}
		}
		
		if(!focused) {
			if(pressing && mouseIsNormal()) {
				this.requestFocus(gui.getWindow().getMouseListener().getX(),gui.getWindow().getSizeY()-gui.getWindow().getMouseListener().getY());
			}
			return;
		}
	}
	
	/**
     * Returns whether the current cursor is the "normal" system cursor.
     *
     * @return {@code true} if cursor is normal, otherwise {@code false}
     */
	public boolean mouseIsNormal() {
		return gui.getWindow().getCursor()==null;
	}

	 /**
     * Sends a mouse scroll event to this window, updating scroll offsets if
     * enabled.
     */
	@Override
	public void sendMouseScroll(long window, double xOffset, double yOffset) {
		if(!this.isFocused()) {
			return;
		}
		if(this.doesMouseWheelScroll){
			this.mouseWheelScrolls+=yOffset;
			
			if(this.useMaxScroll) {
				if(this.mouseWheelScrolls>this.maxScroll) {
					this.mouseWheelScrolls=this.maxScroll;
				}
			}
			if(this.useMinScroll) {
				if(this.mouseWheelScrolls<this.minScroll) {
					this.mouseWheelScrolls=this.minScroll;
				}
			}
		}
	}

	 /**
     * Returns the height of this window's top bar (title bar region).
     *
     * @return top bar height in pixels
     */
	public int getTopSize() {
		return this.topSize;
	}

	 /**
     * Returns the height of this window's top bar (title bar region).
     *
     * @return top bar height in pixels
     */
	public HashMap<String,GUIObject> getObjectsByName() {
		return this.objectsByName;
	}
	
	 /**
     * Adds a constraint offset for the given object within this window.
     *
     * @param o       target GUI object
     * @param offsetX horizontal offset
     * @param offsetY vertical offset
     */
	public void addObjectConstraintOffset(GUIObject o, int offsetX, int offsetY) {
		this.objectConstraintOffset.put(o.name,ObjectUtils.getLongFromInts(offsetX, offsetY));
	}

	/**
     * Returns whether this window is currently being dragged.
     *
     * @return {@code true} if dragging
     */
	public boolean isDragging() {
		return this.dragging;
	}

	/**
     * Returns whether this window is currently being dragged.
     *
     * @return {@code true} if dragging
     */
	public boolean isResizing() {
		return this.resizing;
	}
	
}
