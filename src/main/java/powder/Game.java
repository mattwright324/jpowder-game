package main.java.powder;

import java.util.Random;

public class Game extends Thread {
	
	static boolean paused = false;
	static Counter gfps = new Counter();
	static Random r = new Random();
	
	public static final int MAX_AIR = 256;
	public static final int MIN_AIR = -256;
	
	public static final int MAX_PARTS = Display.width * Display.height;
	
	public void startUpdateThread() {
		start();
	}

	public void run() {
		gfps.start();
		while (isAlive()) {
			if(Grid.cell(0,0) != null && !paused) update();
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {}
		}
	}
	
	static void update() {
		for(int w = 0; w < Display.width; w++) {
			for(int h = 0; h < Display.height; h++) {
				Grid.cell(w, h).update();
			}
		}
		gfps.add();
	}

}
