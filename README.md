# IraGUI

A cross-platform **Java GUI API** built with **Eclipse** and **Maven**.  
Utilizes 
Originally Built for a **Game Engine** called [IraEngine](https://github.com/gdevmaster/IraEngine).
---

## ✨ Features
- ⚡ GPU acceleration via **OpenGL**  
- 🛠️ Built off of [LWJGL](https://www.lwjgl.org)
---

## 📥 Installation
Requires Java 24 & Maven 3.9.11  
Clone the repo and build with Maven:
```bash

git clone https://github.com/gdevmaster/IraGUI.git
cd IraGUI
mvn clean install

```

## ⚙️ How To Use

To create a GUI window and start rendering GUI objects:

<details>
<summary>Show Code</summary>

```java
import com.iraengine.gui.GUI;
import com.iraengine.gui.GUIObject;

public class GUIApp implements Runnable {

	private boolean running = false;
	private GUI gui;
	
	public GUIApp() {
		this.running=true;
		
		// Create a new GUI window
		gui = new GUI(
            "IraGUI Example",  // Window title
            1920,              // Width
            1001,              // Height
            1920,              // Scaled Width
            1001,              // Scaled Height
            true,              // Resizable
            true,              // Decorated
            false,             // Maximized
            true,              // Exit On Close
            false,             // Redraw Every Frame (tip: true for games, false for GUI)
            true               // Nearest Neighbor Filter
        );
	}

    public static void main(String[] args) {
        GUIApp a = new GUIApp();
        Thread t = new Thread(a);
        t.start();
    }
    
    private void update() {
    	// write code here
    }

	@Override
	public void run() {
		gui.begin();
		gui.getWindow().initSound();
		gui.setBackground(0.15f, 0.15f, 0.15f, 1f);
		
		this.init();
		
		while(running) {
			this.update();
			
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
```
</details>

Recommended setup for games:

<details>
<summary>Show Code</summary>

```java
import com.iraengine.gui.GUI;
import com.iraengine.gui.GUIObject;

public class GUIApp implements Runnable {

	private boolean running = false;
	private GUI gui;
	public static final double UPDATE_CAP = 1.0 / 60.0;
	
	public GUIApp() {
		this.running=true;
		
		// Create a new GUI window
		gui = new GUI(
            "IraGUI Example",  // Window title
            1920,              // Width
            1001,              // Height
            1920,              // Scaled Width
            1001,              // Scaled Height
            true,              // Resizable
            true,              // Decorated
            true,              // Maximized
            true,              // Exit On Close
            true,              // Redraw Every Frame (tip: true for games, false for GUI)
            true               // Nearest Neighbor Filter
        );
	}

    public static void main(String[] args) {
        GUIApp a = new GUIApp();
        Thread t = new Thread(a);
        t.start();
    }
    
    private void init() {
    	// write init code here
    }
    
    private void update() {
    	// write code here
    }
    
    private void render() {
    	// render stuff here
    	gui.getWindow().render();
    }

	@Override
	public void run() {
		
		gui.begin();
		gui.getWindow().initSound();
		
		init();
		
		double firstTime = 0;
		double lastTime =  System.nanoTime() / 1000000000.0;
		double passedTime = 0;
		double unprocessedTime = 0;
		double frameTime = 0;
		
		boolean render;
		while(true) {
			render=false;
			firstTime = System.nanoTime() / 1000000000.0;
			passedTime = firstTime - lastTime;
			lastTime = firstTime;
	
			unprocessedTime += passedTime;
			frameTime += passedTime;
			
			while(unprocessedTime >= UPDATE_CAP) {
				unprocessedTime -= UPDATE_CAP;
				
				try {
					this.update();
				} catch (Exception e) {}
				
				render=true;
		
				if(frameTime >= 1.0) {
					frameTime = 0;
				}
			}
			if(render) {
				this.render();
			} else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {}
			}
		}
	}
}
```
</details>