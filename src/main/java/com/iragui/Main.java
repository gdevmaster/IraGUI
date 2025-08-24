package com.iragui;

import java.awt.Color;
import java.awt.Font;

import com.iragui.objects.SubWindowObject;
import com.iragui.objects.TextBoxObject;
import com.iragui.objects.TextObject;

public class Main implements Runnable {
	
	private GUI gui;
	private boolean running=false;
	
	public static void main(String[] args) {
		Main main = new Main();
		Thread mainThread = new Thread(main);
		mainThread.start();
	}
	
	public Main() {
		gui = new GUI("IraGUI",500, 500, 500, 500, true, true, false, true, false, false);
		running=true;
	}
	
	//WrappedBufferedImage o2;
	
	private SubWindowObject sW,sW2,sW3,sW4,sW5;
	
	private TextBoxObject tBO,tBO2;
	
	public static final Color DEEP_GRAY = new Color(15,15,15,255);
	public static final Color DARKER_GRAY = new Color(30,30,30,255);
	
	private void init() {
		//WrappedBufferedImage o = new WrappedBufferedImage("missionStatement",0,gui,0,0,true,true,FileSystem.getImage("data/missionStatement.png"));
		
		sW = new SubWindowObject("window", 0, gui, 0, 0, 500, 500, true, false,DEEP_GRAY,Color.GRAY,true,true,Color.WHITE,"[Window]");
		sW.setVisible();
		
		tBO = new TextBoxObject("textboxobject", 2, gui, 0, 0, 1000,1000,true,true,6);
		sW.add(tBO,SubWindowObject.TOP_LEFT);
		tBO.setVisible();
		
		tBO.appendLine("This line has been appended lmao I am fun",new Font("Consolas",Font.PLAIN,18),Color.WHITE,new Color(0,0,0,0),true,true,true);
		tBO.appendLine("This one also has been appended",new Font("Consolas",Font.PLAIN,18),Color.WHITE,new Color(0,0,0,0),true,true,true);
		
		//-----------------------------------------------------------------
		
		sW2 = new SubWindowObject("window2", 3, gui, 550, 550, 225, 225, true, false,new Color(15,15,15,255),Color.DARK_GRAY,true,true,Color.WHITE,"[Window2]");
		sW2.setVisible();
		
		tBO2 = new TextBoxObject("textboxobject2", 3, gui, 0, 0, 1000,1000,true,true,6);
		sW2.add(tBO2,SubWindowObject.TOP_LEFT);
		tBO2.setVisible();
		
		tBO2.appendLine("Different Lines are here",new Font("Consolas",Font.PLAIN,18),Color.WHITE,new Color(0,0,0,0),true,true,true);
		tBO2.appendLine("And so are they here",new Font("Consolas",Font.PLAIN,18),Color.WHITE,new Color(0,0,0,0),true,true,true);
		
		sW.add(sW2,SubWindowObject.MOVABLE);
		
		
		sW3 = new SubWindowObject("window3", 4, gui, 0, 0, 300, 300, true, false,Color.BLACK,Color.GRAY,true,true,Color.WHITE,"[Window3]");
		sW2.add(sW3,SubWindowObject.MOVABLE);
		
		TextObject tO = new TextObject("HBC", 100, gui, 0, 0, true, true, "HBC", new Font("Perpetua Titling MT",Font.PLAIN,200),new Color(150,0,0,255),Color.BLACK,true);
		
		
		sW4 = new SubWindowObject("window4", 0, gui, 0, 0, 250, 250, true, false,Color.BLACK,Color.GRAY,true,true,Color.WHITE,"[Window4]");
		sW4.setVisible();
		sW4.add(tO,SubWindowObject.MOVABLE);
		
		sW5 = new SubWindowObject("window5", 1, gui, 0, 0, 500, 500, true, false,DARKER_GRAY,Color.GRAY,true,true,Color.WHITE,"[Window5]");
		sW.add(sW5,SubWindowObject.MOVABLE);
		
		SubWindowObject lastWindow = sW5;
		for(int i=5;i<=30;i++) {
			SubWindowObject sO = new SubWindowObject("yaiy"+i, 0, gui, 0, 0, 250, 250, true, false,Color.BLACK,Color.GRAY,true,true,Color.WHITE,""+i);
			lastWindow.add(sO,SubWindowObject.MOVABLE);
			lastWindow=sO;
		}
	}
	
	private void mainUpdate() {}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		gui.begin();
		gui.setBackground(0.15f, 0.15f, 0.15f, 1f);
		
		this.init();
		
		while(running) {
			this.mainUpdate();
			gui.update();
			gui.render();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
