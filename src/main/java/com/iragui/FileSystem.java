package com.iragui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class FileSystem {
	public static InputStream getInputStream(String string) {
		try {
			return FileSystem.class.getClassLoader().getResourceAsStream(string);
		} catch(Exception e) {
			return null;
		}
	}
	
	public static BufferedImage getImage(String string) {
		try {
			return loadImage(FileSystem.class.getClassLoader().getResourceAsStream(string));
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static BufferedImage loadImage(InputStream path)
	{
		try 
		{
			BufferedImage loadedImage = ImageIO.read(path);
			BufferedImage formattedImage = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
			formattedImage.getGraphics().drawImage(loadedImage, 0, 0, null);

			return formattedImage;
		}
		catch(IOException exception) 
		{
			exception.printStackTrace();
			return null;
		}
	}
}
