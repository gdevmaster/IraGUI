package com.iragui.objects;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import com.iragui.GUI;

/**
 * A GUI object that renders text as a texture and displays it in OpenGL.
 * <p>
 * This class creates a {@link BufferedImage}, rasterizes the text with a given
 * {@link Font}, {@link Color}, and background, and uploads it as an OpenGL
 * texture. The text can be updated dynamically, and it will automatically
 * regenerate the texture when changed.
 * </p>
 *
 * <p>Features include:</p>
 * <ul>
 *   <li>Support for anti-aliased text rendering</li>
 *   <li>Support for both RGB and RGBA textures</li>
 *   <li>Automatic recalculation of text width when the string changes</li>
 *   <li>Ability to revert to the original text</li>
 * </ul>
 */
public class TextObject extends GUIObject {
	
	private String text = "";
	private Font font = new Font("Consolas",Font.PLAIN,18);
	private Color color = Color.WHITE;
	private Color bkgColor = Color.BLACK;
	private boolean antiAliasing=true;
	
	private Graphics2D g2d;
	private BufferedImage image;
	private FontMetrics fontMetrics;
	
	 /**
     * Constructs a new {@code TextObject}.
     *
     * @param name          the name of this object
     * @param layer         the rendering layer
     * @param gui           the parent GUI
     * @param x             the X position
     * @param y             the Y position
     * @param nearestFilter whether to use nearest-neighbor filtering
     * @param rgba          whether to render as RGBA (true) or RGB (false)
     * @param text          the text string to display
     * @param font          the font used for rendering
     * @param color         the foreground color of the text
     * @param bkgColor      the background color
     * @param antiAliasing  whether to enable text anti-aliasing
     */
	public TextObject(String name, 
			int layer, 
			GUI gui, 
			int x, 
			int y,
			boolean nearestFilter,
			boolean rgba,
			String text, 
			Font font, 
			Color color, 
			Color bkgColor,
			boolean antiAliasing) {
		
		super(name,layer,gui,x,y,0,(int) (font.getSize()*1.5f),nearestFilter,rgba);
		this.text=text;
		this.font=font;
		this.color=color;
		this.bkgColor=bkgColor;
		this.antiAliasing=antiAliasing;
		this.originalText=text;
		
		
		initializeFontMetrics();
		this.sizeX=getTextWidth();
		createTexture();
	}
	
	/**
     * Initializes the {@link FontMetrics} for measuring text dimensions.
     */
	private void initializeFontMetrics() {
		image = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		g2d = image.createGraphics();
		g2d.setFont(font);
		fontMetrics = g2d.getFontMetrics();
	}
	
	/**
     * Updates the text content and regenerates the texture if changed.
     *
     * @param text the new text string
     */
	public void setText(String text) {
		
		if(text.contentEquals(this.text)) {
			return;
		}
		
		this.text=text;
		this.sizeX=getTextWidth();
		createTexture();
	}
	
	/**
     * @return the width of the current text in pixels
     */
	public int getTextWidth() {
		return fontMetrics.stringWidth(text);
	}
	

    /**
     * Returns the width of a specific string in pixels.
     *
     * @param text the string to measure
     * @return the pixel width of the given string
     */
	public int getTextWidth(String text) {
		return fontMetrics.stringWidth(text);
	}
	
	 /**
     * Sets the font used for rendering text and regenerates the texture.
     *
     * @param font the new font
     */
	public void setFont(Font font) {
		this.font=font;
		this.sizeY=(int) (font.getSize()*(1.5f));
		createTexture();
	}
	
	/**
     * Creates or updates the texture with the current text, font, and colors.
     * Handles both RGB and RGBA modes.
     */
	public void createTexture() {
		
		if(sizeX<=0) {
			sizeX=1;
		}
		if(sizeY<=0) {
			sizeY=1;
		}
		
		image = new BufferedImage(this.sizeX, this.sizeY, BufferedImage.TYPE_INT_ARGB);
		for(int ix=0;ix<image.getWidth();ix++) {
        	 for(int iy=0;iy<image.getHeight();iy++) {
             	image.setRGB(ix,iy,bkgColor.getRGB());
             }
        }
		this.g2d = image.createGraphics();
		if(antiAliasing) {
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // Enable text anti-aliasing
	    }
		g2d.setFont(font);
	    g2d.setColor(color);
	    g2d.drawString(text,0,font.getSize());
		g2d.dispose();
		
		if(this.rgba) {
			// update pixels if RGBA
		    this.pixelBuffer = BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*this.BYTES_PER_PIXEL);
	      	
	      	int p;
	      	for(int y=this.sizeY-1;y>-1;y--) {
	      	for(int x=0;x<this.sizeX;x++) {
					p = image.getRGB(x,y);
					pixelBuffer.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBuffer.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBuffer.put(((byte) ((p & 0xFF)))); // B
					pixelBuffer.put(((byte) ((p >> 24) & 0xFF))); // A
				}
	      	}
	      	pixelBuffer.flip();
	      	
	      	 GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.getTextureID());
		     GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, getGlPixelInt(), this.sizeX, this.sizeY, 0, getGlPixelInt(), GL30.GL_UNSIGNED_BYTE, pixelBuffer);
		     GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
			
		} else {
			// update pixels if RGB
			
			 this.pixelBuffer = BufferUtils.createByteBuffer(image.getWidth()*image.getHeight()*this.BYTES_PER_PIXEL);
		      	
		      	int p;
		      	for(int y=this.sizeY-1;y>-1;y--) {
		      	for(int x=0;x<this.sizeX;x++) {
						p = image.getRGB(x,y);
						pixelBuffer.put(((byte) ((p >> 16) & 0xFF))); // R
						pixelBuffer.put(((byte) ((p >> 8) & 0xFF))); // G
						pixelBuffer.put(((byte) ((p & 0xFF)))); // B
					}
		      	}
		      	pixelBuffer.flip();
		      	
		      	GL30.glBindTexture(GL30.GL_TEXTURE_2D, this.getTextureID());
			    GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, getGlPixelInt(), this.sizeX, this.sizeY, 0, getGlPixelInt(), GL30.GL_UNSIGNED_BYTE, pixelBuffer);
			    GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}
		fontMetrics = g2d.getFontMetrics();
	}

	@Override
	public void update(boolean showFrame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendKey(int key, int action) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMousePos(long window, double xPos, double yPos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMouseButton(long window, int button, int action, int mods) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMouseScroll(long window, double xOffset, double yOffset) {
		// TODO Auto-generated method stub
		
	}

	 /** @return the current text string */
	public String getText() {
		return this.text;
	}
	
	/** @return true if the text matches the original text */
	public boolean isOriginalText() {
		return this.originalText.contentEquals(this.text);
	}
	
    /** @return the pixel width of the original text */
	public int getOriginalTextWidth() {
		return getTextWidth(this.originalText);
	}

	
	private String originalText;
	
    /** Reverts the text back to its original value. */

	public void revertText() {
		if(this.text.contentEquals(originalText)) {
			return;
		}
		this.setText(originalText);
	}

	 /**
     * Sets the text and overrides the original text value as well.
     *
     * @param string the new text string
     */
	public void setTextOverride(String string) {
		if(this.text.contentEquals(string)) {
			return;
		}
		this.setText(string);
		this.originalText=string;
	}
	
	/**
     * Sets the text color and regenerates the texture.
     *
     * @param color the new color
     */
	public void setColor(Color color) {
		if(this.color.getRGB()==color.getRGB()) {
			return;
		}
		this.color=color;
		createTexture();
	}

	/**
     * @return the current font used for rendering
     */
	public Font getFont() {
		return this.font;
	}
}
