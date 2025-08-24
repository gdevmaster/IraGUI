package com.iragui.objects;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import com.iragui.GUI;

public class WrappedBufferedImage extends GUIObject {

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
	
	private ByteBuffer pixelBufferDirection0;
	private ByteBuffer pixelBufferDirection1;
	private ByteBuffer pixelBufferDirection2;
	private ByteBuffer pixelBufferDirection3;
	private boolean directional = false;
	
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
