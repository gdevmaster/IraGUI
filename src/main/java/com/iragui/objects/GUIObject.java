package com.iragui.objects;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import com.iragui.GUI;

/**
* Base class for all objects that can be managed and rendered inside a {@link GUI}.
* <p>
* A {@code GUIObject} encapsulates its position, size, rendering state, and
* input callback registration. It manages its own OpenGL texture, buffer data,
* and provides abstract hooks for update and input handling.
* </p>
*/
public abstract class GUIObject {
	
	/** Unique identifier for this object. */
	public final String name;
	
	/** The rendering layer index. Higher layers are drawn above lower ones. */
	protected int layer;
	
	/** The parent {@link GUI} managing this object. */
	protected GUI gui;
	
	/** Pixel dimensions of the object. */
	protected int sizeX,sizeY;
	
	/** Absolute bounds within the object. */
	protected int limitX,limitY,minX,minY;
	
	/** OpenGL pixel buffer storing object texture data. */
	protected ByteBuffer pixelBuffer;
	
	/** Object position in pixels (top-left corner). */
	public int x,y;
	
	/** Window-constrained bounds. */
	protected int winLimitX,winLimitY,winMinX,winMinY;
	
	private final boolean updates;
	private final boolean includeMouseCallback;
	private final boolean includeKeyCallback;
	
	private int scale = 1;
	
	/** @return current rendering scale of this object */
	public int getScale() {
		return this.scale;
	}
	
	/** Sets the rendering scale of this object. */
	public void setScale(int scale) {
		this.scale=scale;
	}
	
	protected int constraint;
	
	/** Whether this object should update each frame. */
	public boolean updates() {
		return this.updates;
	}
	
	/** @return true if this object listens for mouse callbacks */
	public boolean includesMouseCallback() {
		return this.includeMouseCallback;
	}
	
	/** @return true if this object listens for key callbacks */
	public boolean includesKeyCallback() {
		return this.includeKeyCallback;
	}
	
	/** Sets the X position in pixels. */
	public void setX(int x) {
		this.x=x;
	}
	
	/** Sets the Y position in pixels. */
	public void setY(int y) {
		this.y=y;
	}
	
	/** @return current X position */
	public int getX() {
		return this.x;
	}
	
	/** @return current Y position */
	public int getY() {
		return this.y;
	}
	
	
	private boolean preserveTextureIDOverride = false;
	
	/**
	* Sets whether to prevent automatic deletion/regeneration of texture IDs.
	* Useful if texture reuse is required.
	*/
	public void setPreserveTextureIDOverride(boolean bool) {
		this.preserveTextureIDOverride=bool;
	}
	
	
	private static boolean globalShaderCompiled=false;
	
	private boolean dirty=true;
	
	/** Pixel format constants. */
	public static final int RGBA = 4, RGB = 3;
	
	/** Whether this object uses RGBA pixel data. */
	protected final boolean rgba;
	
	/** Bytes per pixel (3 for RGB, 4 for RGBA). */
	protected final int BYTES_PER_PIXEL;
	
	/** OpenGL format flag. */
	private final int glPixelInt;
	
	protected boolean destroyed=false;
	protected boolean focused=false;
	
	/** @return true if this object is focused */
	public boolean isFocused() {
		return this.focused;
	}
	
	/** Sets this object as focused. */
	public void focus() {
		this.focused=true;
	}
	
	/** Removes focus from this object. */
	public void unfocus() {
		this.focused=false;
	}
	
	/** @return true if this object has been destroyed */
	public boolean isDestroyed() {
		return this.destroyed;
	}
	
	/** @return width in pixels */
	public int getSizeX() {
		return this.sizeX;
	}
	
	/** @return height in pixels */
	public int getSizeY() {
		return this.sizeY;
	}
	
	
	public void setWinLimitX(int x) {
		x=(x<0?0:x);
		this.winLimitX=x;
		allowReTexture();
		gui.showNextFrame();
	}
	public void setWinLimitY(int y) {
		y=(y<0?0:y);
		this.winLimitY=y;
		allowReTexture();
		gui.showNextFrame();
	}
	public void setWinMinX(int x) {
		x=(x<0?0:x);
		this.winMinX=x;
		allowReTexture();
		gui.showNextFrame();
	}
	public void setWinMinY(int y) {
		y=(y<0?0:y);
		this.winMinY=y;
		allowReTexture();
		gui.showNextFrame();
	}
	public int getWinLimitX() {
		return this.winLimitX;
	}
	public int getWinLimitY() {
		return this.winLimitY;
	}
	public int getWinMinX() {
		return this.winMinX;
	}
	public int getWinMinY() {
		return this.winMinY;
	}
	
	//-----------------------------------------------//
	
	public void setLimitX(int x) {
		x=(x<0?0:x);
		this.limitX=x;
		allowReTexture();
		gui.showNextFrame();
	}
	public void setLimitY(int y) {
		y=(y<0?0:y);
		this.limitY=y;
		allowReTexture();
		gui.showNextFrame();
	}
	
	public void setMinX(int x) {
		x=(x<0?0:x);
		this.minX=x;
		allowReTexture();
		gui.showNextFrame();
	}
	public void setMinY(int y) {
		y=(y<0?0:y);
		this.minY=y;
		allowReTexture();
		gui.showNextFrame();
	}
	
	public int getMinX() {
		return this.minX;
	}
	public int getMinY() {
		return this.minY;
	}
	
	public int getLimitX() {
		return this.limitX;
	}
	
	public int getLimitY() {
		return this.limitY;
	}
	
	/**
	* Marks this object as needing re-texturing on next render.
	*/
	protected void allowReTexture() {
		this.dirty=true;
	}
	
	/** Visibility flag. */
	protected boolean visible = false;
	
	/** Makes this object visible. */
	public void setVisible() {
		this.visible=true;
		gui.showNextFrame();
	}
	
	/** @return true if this object is visible */
	public boolean getVisible() {
		return this.visible;
	}
	
	/** Hides this object. */
	public void hide() {
		this.visible=false;
		gui.showNextFrame();
	}
	
	 
	private static final String shader = 
		    "#type vertex\n" +
		    "#version 330 core\n" +
		    "\n" +
		    "layout(location = 0) in vec3 inPosition;\n" +
		    "layout(location = 1) in vec2 inTexCoord;\n" +
		    "\n" +
		    "uniform mat4 transform;\n" +
		    "out vec2 texCoord;\n" +
		    "\n" +
		    "void main() {\n" +
		    "    gl_Position = transform * vec4(inPosition, 1);\n" +
		    "    texCoord = inTexCoord;\n" +
		    "}\n" +
		    "\n" +
		    "#type fragment\n" +
		    "#version 330 core\n" +
		    "\n" +
		    "in vec2 texCoord;\n" +
		    "out vec4 fragColor;\n" +
		    "uniform sampler2D textureSampler;\n" +
		    "\n" +
		    "void main() {\n" +
		    "    fragColor = texture(textureSampler, texCoord);\n" +
		    "}";


	private static final Shader s = new Shader(shader,true);
	
	private static final int[] squareVerticesInt = {
    	    -1, 1, 0,    // Top-left in NDC
    	    1, 1, 0,     // Top-right in NDC
    	    1, -1, 0,    // Bottom-right in NDC
    	    1, -1, 0,    // Bottom-right in NDC
    	    -1, -1, 0,   // Bottom-left in NDC
    	    -1, 1, 0     // Top-left in NDC
    	};
    
    private static final int[] textureCoordsInt = {
    	    0, 0, // Bottom-left (was top-left before)
    	    1, 0, // Bottom-right (was top-right before)
    	    1, 1, // Top-right (was bottom-right before)
    	    1, 1, // Top-right (same as above)
    	    0, 1, // Top-left (was bottom-left before)
    	    0, 0  // Bottom-left (same as above)
    	};
    
    /**
    * Constructs a new GUI object with full parameter control.
    *
    * @param name unique object name
    * @param layer rendering layer index
    * @param gui parent GUI instance
    * @param x initial X position
    * @param y initial Y position
    * @param sizeX object width in pixels
    * @param sizeY object height in pixels
    * @param nearestFilter whether to use nearest-neighbor filtering
    * @param rgba whether to use RGBA (vs RGB)
    * @param includeKeyCallback whether to register key callbacks
    * @param includeMouseCallback whether to register mouse callbacks
    * @param updates whether this object updates each frame
    */
    public GUIObject(String name, 
			int layer, 
			GUI gui, 
			int x, 
			int y, 
			int sizeX, 
			int sizeY,
			boolean nearestFilter,
			boolean rgba,
			boolean includeKeyCallback,
			boolean includeMouseCallback,
			boolean updates) {
    	
    	this.includeKeyCallback=includeKeyCallback;
    	this.includeMouseCallback=includeMouseCallback;
    	this.updates=updates;
		
		this.name=name;
		this.layer=layer;
		this.gui=gui;
		this.x=x;
		this.y=y;
		this.sizeX=sizeX;
		this.sizeY=sizeY;
		
		this.limitX=9999;
		this.limitY=9999;
		
		this.setNearestFilter(nearestFilter);
		this.rgba=rgba;
		
		this.minX=0;
		this.minY=0;
		
		this.winLimitX=9999;
		this.winLimitY=9999;
		this.winMinX=0;
		this.winMinY=0;
		
		if(this.rgba) {
			BYTES_PER_PIXEL=RGBA;
			glPixelInt=GL30.GL_RGBA;
		} else {
			BYTES_PER_PIXEL=RGB;
			glPixelInt=GL30.GL_RGB;
		}
		
		this.generateTexture();
		gui.addObject(this);
	}
	 
    /**
    * Simplified constructor with callbacks and updates enabled.
    * @param name unique object name
    * @param layer rendering layer index
    * @param gui parent GUI instance
    * @param x initial X position
    * @param y initial Y position
    * @param sizeX object width in pixels
    * @param sizeY object height in pixels
    * @param nearestFilter whether to use nearest-neighbor filtering
    * @param rgba whether to use RGBA (vs RGB)
    */
	public GUIObject(String name, 
			int layer, 
			GUI gui, 
			int x, 
			int y, 
			int sizeX, 
			int sizeY,
			boolean nearestFilter,
			boolean rgba) {
		
		this.includeKeyCallback=true;
		this.includeMouseCallback=true;
		this.updates=true;
		
		this.name=name;
		this.layer=layer;
		this.gui=gui;
		this.x=x;
		this.y=y;
		this.sizeX=sizeX;
		this.sizeY=sizeY;
		
		this.limitX=9999;
		this.limitY=9999;
		
		this.setNearestFilter(nearestFilter);
		this.rgba=rgba;
		
		this.minX=0;
		this.minY=0;
		
		this.winLimitX=9999;
		this.winLimitY=9999;
		this.winMinX=0;
		this.winMinY=0;
		
		if(this.rgba) {
			BYTES_PER_PIXEL=RGBA;
			glPixelInt=GL30.GL_RGBA;
		} else {
			BYTES_PER_PIXEL=RGB;
			glPixelInt=GL30.GL_RGB;
		}
		
		this.generateTexture();
		gui.addObject(this);
	}

	/** @return current rendering layer */
	public int getLayer() {
		return this.layer;
	}
	
	/** Sets the rendering layer and updates parent GUI ordering. */
	public void setLayer(int layer) {
		int oldLayer=this.layer;
		this.layer=layer;
		gui.confirmLayerUpdate(oldLayer,this);
	}
	
	/** Update logic called each frame. */
	public abstract void update(boolean showFrame);

	/** Handles key input events. */
	public abstract void sendKey(int key, int action);

	/** Handles mouse position updates. */
	public abstract void sendMousePos(long window, double xPos, double yPos);

	/** Handles mouse button input. */
	public abstract void sendMouseButton(long window, int button, int action, int mods);

	/** Handles mouse scroll input. */
	public abstract void sendMouseScroll(long window, double xOffset, double yOffset);
	
	protected int vao;
	protected int vbo;
	private int textureID;
	private int texCoordsVbo;
	private boolean nearestFilter;
	
	private void updatePixelInfo(int texture) {
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
		GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, this.sizeX, this.sizeY, getGlPixelInt(), GL30.GL_UNSIGNED_BYTE,pixelBuffer);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
	}
	
	private void generateTexture() {
		
		if(!globalShaderCompiled) {
			s.compile();
			globalShaderCompiled=true;
		}
        
		IntBuffer verticesBuffer = BufferUtils.createIntBuffer(squareVerticesInt.length);
		verticesBuffer.put(squareVerticesInt).flip();
        
        //FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(squareVertices.length);
        //verticesBuffer.put(squareVertices).flip();
		
		IntBuffer textureBuffer = BufferUtils.createIntBuffer(textureCoordsInt.length);
        textureBuffer.put(textureCoordsInt).flip();

        //FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
        //textureBuffer.put(textureCoords).flip();
        
        this.vbo = GL30.glGenBuffers();
        
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.vbo);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, verticesBuffer, GL30.GL_STATIC_DRAW);

       this.texCoordsVbo = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, texCoordsVbo);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, textureBuffer, GL30.GL_STATIC_DRAW);

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        
        this.vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(this.vao);

        // Bind the vertex buffer
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.vbo);
        GL30.glVertexAttribPointer(0, 3, GL30.GL_INT, false, 0, 0);
        GL30.glEnableVertexAttribArray(0);

        // Bind the texture coordinate buffer
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, texCoordsVbo);
        GL30.glVertexAttribPointer(1, 2, GL30.GL_INT, false, 0, 0);
        GL30.glEnableVertexAttribArray(1);
        
        GL30.glBindVertexArray(0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        
        this.setTextureID(createRandomTexture(this.sizeX,this.sizeY));
	}
	
	private int createRandomTexture(int sX, int sY) {
		
		int textureID = GL30.glGenTextures();
        // bind texture to GL
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureID);
        
        GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);
        
        if(isNearestFilter()) {
        	GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
        	GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
        } else {
        	GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
        	GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
        }
        
        
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);

        pixelBuffer = BufferUtils.createByteBuffer(sX * sY * BYTES_PER_PIXEL);
        
        // Upload the pixel data to the texture
      	GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, getGlPixelInt(), sX, sY, 0, getGlPixelInt(), GL30.GL_UNSIGNED_BYTE, pixelBuffer);
    
        // Unbind the texture
      	GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
      	
      	return textureID;
	}
	
	/** Renders this object using OpenGL. 
	 * @param window GLFW window id as a long
	 * @param windowWidth GLFW window width in pixels
	 * @param windowHeight GLFW window height in pixels
	 * */
	public void render(long window, int windowWidth, int windowHeight) {
		
	    if (!visible) {
	        return;
	    }

	    if (this.dirty) {
	        updatePixelInfo(getTextureID());
	        this.dirty = false;
	    }

	    GL30.glUseProgram(s.getId());

	    // Define the object's size in pixels
	    float objectWidthInPixels = (this.sizeX/2.0f)*scale;  // Object width in pixels
	    float objectHeightInPixels = (this.sizeY/2.0f)*scale; // Object height in pixels

	    // Create the orthographic projection matrix (in pixel space), flipping the Y-axis
	    Matrix4f projection = new Matrix4f().ortho(0, windowWidth, windowHeight, 0, -1, 1);

	    // Create the transformation matrix (translation and scaling in pixel space)
	    Matrix4f transform = new Matrix4f()
	        .translate(windowWidth - (windowWidth - x - objectWidthInPixels),(windowHeight - y - objectHeightInPixels), 0)  // Align top-left corner and adjust Y
	       .scale(objectWidthInPixels, objectHeightInPixels,0)   // Scale by pixel size
	    	    .rotateZ((float) (Math.toRadians(rotation * 90)))  // apply 90° step rotation
	    	    .rotateY((float)(Math.toRadians(hFlip*180)))
	    	    .rotateX((float)(Math.toRadians(vFlip*180)));                         
	    	    
	    

	    // Multiply projection matrix by transform matrix to get final transformation
	    Matrix4f finalTransform = projection.mul(transform);
	    
	    try (MemoryStack stack = MemoryStack.stackPush()) {
	        FloatBuffer matrixBuffer = stack.mallocFloat(16);
	        finalTransform.get(matrixBuffer);
	        int transformLoc = GL30.glGetUniformLocation(s.getId(), "transform");
	        if (transformLoc == -1) {
	            System.err.println("Could not find uniform 'transform'");
	        } else {
	            GL30.glUniformMatrix4fv(transformLoc,false,matrixBuffer);
	        }
	    }
	    
	    //adjustTextureCoordinates();

	    GL30.glActiveTexture(GL30.GL_TEXTURE0);
	    GL30.glBindTexture(GL30.GL_TEXTURE_2D, getTextureID());

	    int textureSamplerLocation = GL30.glGetUniformLocation(s.getId(), "textureSampler");
	    if (textureSamplerLocation == -1) {
	        System.err.println("Could not find uniform 'textureSampler'");
	    } else {
	        GL30.glUniform1i(textureSamplerLocation, 0);  // Set the uniform to texture unit 0
	    }

	    GL30.glBindVertexArray(vao);  // Bind VAO
	    GL30.glEnableVertexAttribArray(0);  // Enable position attribute
	    GL30.glEnableVertexAttribArray(1);  // Enable texture attribute
	    
	    GL30.glEnable(GL30.GL_SCISSOR_TEST);

	    int startX = (this.minX>this.winMinX?this.minX:this.winMinX);
	    int startY = (this.minY>this.winMinY?this.minY:this.winMinY);
	    int endX  = this.limitX<this.winLimitX?this.limitX:this.winLimitX;
	    int endY = this.limitY<this.winLimitY?this.limitY:this.winLimitY;
	    
	    int width = endX-startX;
	    int height = endY-startY;
	    
	    width=width<0?0:width;
	    height=height<0?0:height;

	    GL30.glScissor(startX, startY, width, height);  // Set scissor box

	    // Draw the object
	    GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);  // Draw call

	    // Disable Scissor Test
	    GL30.glDisable(GL30.GL_SCISSOR_TEST);

	   // GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, 6);  // Draw call

	    // Unbind VAO, disable vertex attributes
	    GL30.glBindVertexArray(0);  // Unbind VAO
	    GL30.glDisableVertexAttribArray(0);
	    GL30.glDisableVertexAttribArray(1);

	    // Unbind texture and shader
	    GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
	    GL30.glUseProgram(0);
	}
	
	/**
	 * Destroys this object, releasing GPU resources and unregistering it.
	 *
	 * <p>If {@code preserveTextureIDOverride} is true,
	 * the OpenGL texture ID will not be deleted.
	 * See {@link #setPreserveTextureIDOverride(boolean)}.</p>
	 */
	public void destroyObject() {
		if(!this.isDestroyed()) {
			gui.removeObject(this);
			if(!preserveTextureIDOverride) {
				GL30.glDeleteTextures(getTextureID());
			}
			GL30.glDeleteBuffers(vbo);
			GL30.glDeleteVertexArrays(vao);
			this.destroyed=true;
		}
	}
	
	/** Sets a single pixel in the buffer. */
	public void setPixel(int x, int y, byte[] p) {
		pixelBuffer.put((x+y*this.sizeX)*BYTES_PER_PIXEL,p,0,BYTES_PER_PIXEL);
		this.allowReTexture();
	}
	
	/** Replaces the entire pixel buffer with the given byte array. */
	public void setAll(int x, int y, byte[] p) {
		pixelBuffer.put(0,p,0,p.length);
		this.allowReTexture();
	}
	
	/** Replaces the entire pixel buffer with the given {@link ByteBuffer}. */
	public void setAll(ByteBuffer p) {
		pixelBuffer = p;
		this.allowReTexture();
	}
	
	/** @return OpenGL texture ID for this object */
	public int getTextureID() {
		return textureID;
	}

	/** Sets the OpenGL texture ID manually. */
	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}
	
	/** Regenerates a new texture ID and re-uploads pixel data. 
	 * <p>If {@code preserveTextureIDOverride} is true,
	 * the OpenGL texture ID will not be reset.
	 * See {@link #setPreserveTextureIDOverride(boolean)}.</p>*/
	
	public void resetTextureID() {
		if(!preserveTextureIDOverride) {
			GL30.glDeleteTextures(getTextureID());
			this.generateTexture();
			this.updatePixelInfo(this.getTextureID());
		}
	}

	/** @return true if using nearest-neighbor filtering */
	public boolean isNearestFilter() {
		return nearestFilter;
	}

	/** Sets nearest-neighbor filtering mode. */
	public void setNearestFilter(boolean nearestFilter) {
		this.nearestFilter = nearestFilter;
	}

	/** @return OpenGL pixel format flag */
	public int getGlPixelInt() {
		return glPixelInt;
	}
	
	/**
	* Tests whether a point lies within the object's visible interaction bounds.
	* @param mX mouse X position
	* @param mY mouse Y position
	* @return true if within bounds
	*/
	public boolean interactionInVisibleBounds(int mX, int mY) {
		int minX = getMinX()>getWinMinX()?getMinX():getWinMinX();
		int minY = getMinY()>getWinMinY()?getMinY():getWinMinY();
		int maxX = getLimitX()<getWinLimitX()?getLimitX():getWinLimitX();
		int maxY = getLimitY()<getWinLimitY()?getLimitY():getWinLimitY();
		
		return(mX>=minX&&mX<=maxX&&mY>=minY&&mY<=maxY);
	}
	
	private int rotation;
	
	/** Sets rotation in 90° increments. 
	 * <p>0 = 0°<br>
	 * 1 = 90°<br>
	 * 2 = 180°<br>
	 * 3 = 270°<br></p>
	 * */
	public void setRotation(int rotation) {
		this.rotation=rotation;
	}
	
	/** @return current rotation
	 * <p>0 = 0°<br>
	 * 1 = 90°<br>
	 * 2 = 180°<br>
	 * 3 = 270°<br></p>
	 * */
	public int getRotation() {
		return this.rotation;
	}
	
	private int hFlip=0,vFlip=0;
	
	/** Sets horizontal flip. */
	public void setHFlip(boolean flip) {
		this.hFlip = flip?1:0;
	}
	
	/** Sets vertical flip. */
	public void setVFlip(boolean flip) {
		this.vFlip = flip?1:0;
	}
	
	/** @return true if horizontally flipped */
	public boolean getHFlip() {
		return hFlip==1;
	}
	
	/** @return true if vertically flipped */
	public boolean getVFlip() {
		return vFlip==1;
	}
}
