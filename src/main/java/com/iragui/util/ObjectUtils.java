package com.iragui.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import com.iragui.GUI;
import com.iragui.objects.ButtonObject;
import com.iragui.objects.WrappedBufferedImage;

import com.iragui.FileSystem;

public class ObjectUtils {
	public static ButtonObject createButtonObject(String name, 
			   int layer, 
			   GUI gui, 
			   int x, 
			   int y, 
			   int sizeX, 
			   int sizeY, 
			   boolean nearestFilter,
			   boolean rgba,
			   String...images) {
		
		WrappedBufferedImage[] wBI = new WrappedBufferedImage[images.length];
		for(int i=0;i<wBI.length;i++) {
			wBI[i]=new WrappedBufferedImage(name+":WrappedBufferedImage:"+i, layer, gui, x, y, nearestFilter, rgba,FileSystem.getImage(images[i]));
		}
		return new ButtonObject(name,layer,gui,x,y,sizeX,sizeY,nearestFilter,rgba,wBI);
	}
	
	
	public static long getLongFromInts(int x, int y) {
		return ((long) x << 32) | (y & 0xFFFFFFFFL);
	}
	
	public static int[] getIntegersFromLong(long xy) {
		
		int[] integers = new int[2];
		integers[0] = (int) (xy >> 32);
		integers[1] = (int) (xy);
		
		return integers;
	}
	
	public static String getClipboard() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		boolean hasStringText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
		if (hasStringText) {
		    try {
		       return (String)contents.getTransferData(DataFlavor.stringFlavor);
		    } catch (UnsupportedFlavorException | IOException ex) {
		        return null;
		    }
		}
		return null;
	}
	
	public static ByteBuffer createByteBuffer(byte[] bytes) {
		return BufferUtils.createByteBuffer(bytes.length);
	}
	
	public static ByteBuffer createByteBuffer(int length) {
		return BufferUtils.createByteBuffer(length);
	}
}
