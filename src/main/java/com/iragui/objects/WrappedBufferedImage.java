package com.iragui.objects;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import com.iragui.GUI;

/**
 * A GUIObject that wraps a {@link BufferedImage} for use with IraGUI.
 * <p>
 * This class converts Java {@link BufferedImage} pixel data into GPU-friendly
 * {@link ByteBuffer} objects. It supports both RGB and RGBA formats, and can
 * optionally store directional variants of the same image for sprite animations
 * or rotated rendering.
 * </p>
 */
public class WrappedBufferedImage extends GUIObject {

	/**
     * Creates a new wrapped image without directional variants.
     *
     * @param name          the name of the object
     * @param layer         the rendering layer
     * @param gui           the parent {@link GUI}
     * @param x             the x position of this object
     * @param y             the y position of this object
     * @param nearestFilter whether to use nearest-neighbor filtering
     * @param rgba          true if the image should be stored with an alpha channel (RGBA)
     * @param image         the {@link BufferedImage} to wrap
     */
	public WrappedBufferedImage(String name, 
			int layer, 
			GUI gui, 
			int x, 
			int y,
			boolean nearestFilter,
			boolean rgba,
			BufferedImage image) {
		super(name, layer, gui, x, y, image.getWidth(), image.getHeight(), nearestFilter,rgba, false, false, false);
		
		if(!rgba) {
			
			int p;
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=0;k<image.getWidth();k++) {
				
					p=image.getRGB(k,j);
				
					pixelBuffer.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBuffer.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBuffer.put(((byte) ((p & 0xFF)))); // B
				}
			}
			pixelBuffer.flip();
			
		} else {
		
			int p;
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=0;k<image.getWidth();k++) {
				
					p=image.getRGB(k,j);
				
					pixelBuffer.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBuffer.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBuffer.put(((byte) ((p & 0xFF)))); // B
					pixelBuffer.put(((byte) ((p >> 24) & 0xFF))); // A
				}
			}
			pixelBuffer.flip();
		
		}
	}
	 /**
     * Alternate pixel buffers for each direction (used if {@link #directional} is true).
     */
	private ByteBuffer pixelBufferDirection0;
	private ByteBuffer pixelBufferDirection1;
	private ByteBuffer pixelBufferDirection2;
	private ByteBuffer pixelBufferDirection3;
	
	/**
     * Whether this wrapped image supports directional variants.
     */
	private boolean directional = false;
	
	/**
     * Changes the active pixel buffer to match the given direction.
     * <p>
     * Only works if {@link #directional} is true. Directions are:
     * </p>
     *
     * @param direction the direction index (0–3)
     */
	public void changeDirection(int direction) {
		if(directional) {
			switch(direction) {
			case 0:
				this.setAll(pixelBufferDirection0);
				break;
			case 1:
				this.setAll(pixelBufferDirection1);
				break;
			case 2:
				this.setAll(pixelBufferDirection2);
				break;
			case 3:
				this.setAll(pixelBufferDirection3);
				break;
			}
		}
	}
	
	/**
     * Creates a new wrapped image with directional variants.
     *
     * @param name          the name of the object
     * @param layer         the rendering layer
     * @param gui           the parent {@link GUI}
     * @param x             the x position of this object
     * @param y             the y position of this object
     * @param nearestFilter whether to use nearest-neighbor filtering
     * @param rgba          true if the image should be stored with an alpha channel (RGBA)
     * @param image         the {@link BufferedImage} to wrap
     * @param directional   whether to generate directional pixel buffers
     */
	public WrappedBufferedImage(String name, 
			int layer, 
			GUI gui, 
			int x, 
			int y,
			boolean nearestFilter,
			boolean rgba,
			BufferedImage image,
			boolean directional) {
		super(name, layer, gui, x, y, image.getWidth(), image.getHeight(), nearestFilter,rgba, false, false, false);
		
		this.directional=true;
		
		pixelBufferDirection0 = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);
		pixelBufferDirection1 = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);
		pixelBufferDirection2 = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);
		pixelBufferDirection3 = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL);
		
		if(!rgba) {
			
			int p;
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=0;k<image.getWidth();k++) {
				
					p=image.getRGB(k,j);
				
					pixelBuffer.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBuffer.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBuffer.put(((byte) ((p & 0xFF)))); // B
				}
			}
			pixelBuffer.flip();
			pixelBufferDirection0 = pixelBuffer.duplicate();
			
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=0;k<image.getWidth();k++) {
					p=image.getRGB(j,k);
				
					pixelBufferDirection3.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBufferDirection3.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBufferDirection3.put(((byte) ((p & 0xFF)))); // B
				}
			}
			pixelBufferDirection3.flip();
			
			for(int j=0;j<image.getHeight();j++) {
				for(int k=0;k<image.getWidth();k++) {
					p=image.getRGB(j,k);
				
					pixelBufferDirection2.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBufferDirection2.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBufferDirection2.put(((byte) ((p & 0xFF)))); // B
				}
			}
			pixelBufferDirection2.flip();
			
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=image.getWidth()-1;k>=0;k--) {
				
					p=image.getRGB(k,j);
				
					pixelBufferDirection1.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBufferDirection1.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBufferDirection1.put(((byte) ((p & 0xFF)))); // B
				}
			}
			pixelBufferDirection1.flip();
			
		} else {
		
			int p;
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=0;k<image.getWidth();k++) {
				
					p=image.getRGB(k,j);
				
					pixelBuffer.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBuffer.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBuffer.put(((byte) ((p & 0xFF)))); // B
					pixelBuffer.put(((byte) ((p >> 24) & 0xFF))); // A
				}
			}
			pixelBuffer.flip();
			pixelBufferDirection0 = pixelBuffer.duplicate();
			
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=0;k<image.getWidth();k++) {
					p=image.getRGB(j,k);
				
					pixelBufferDirection3.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBufferDirection3.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBufferDirection3.put(((byte) ((p & 0xFF)))); // B
					pixelBufferDirection3.put(((byte) ((p >> 24) & 0xFF))); // A
				}
			}
			pixelBufferDirection3.flip();
			
			for(int j=0;j<image.getHeight();j++) {
				for(int k=0;k<image.getWidth();k++) {
					p=image.getRGB(j,k);
				
					pixelBufferDirection2.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBufferDirection2.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBufferDirection2.put(((byte) ((p & 0xFF)))); // B
					pixelBufferDirection2.put(((byte) ((p >> 24) & 0xFF))); // A
				}
			}
			pixelBufferDirection2.flip();
			
			for(int j=image.getHeight()-1;j>=0;j--) {
				for(int k=image.getWidth()-1;k>=0;k--) {
				
					p=image.getRGB(k,j);
				
					pixelBufferDirection1.put(((byte) ((p >> 16) & 0xFF))); // R
					pixelBufferDirection1.put(((byte) ((p >> 8) & 0xFF))); // G
					pixelBufferDirection1.put(((byte) ((p & 0xFF)))); // B
					pixelBufferDirection1.put(((byte) ((p >> 24) & 0xFF))); // A
				}
			}
			pixelBufferDirection1.flip();
		}
	}
	
	 /**
     * Returns the main pixel buffer for this image.
     *
     * @return the base {@link ByteBuffer} containing pixel data
     */
	public ByteBuffer getPixelBuffer() {
		return this.pixelBuffer;
	}

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

}
