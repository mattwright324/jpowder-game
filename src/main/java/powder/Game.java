package main.java.powder;

import java.util.Random;


public class Game extends Thread {
	
	static boolean paused = false;
	static Display.FPS gfps = new Display.FPS();
	static Random r = new Random();

	static void update() {
		for (int w = 0; w < Display.width; w++) {
			for (int h = 0; h < Display.height; h++) {
				if (Cells.particleAt(w, h)) {
					try {
						Cells.cells[w][h].part.update();
					} catch (NullPointerException e) {}
				}
			}
		}
		gfps.add();
	}

	public void startUpdateThread() {
		start();
	}

	public void run() {
		gfps.start();
		while (isAlive()) {
			if(Cells.cells[0][0] != null && !paused) update();
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {}
		}
	}


}
