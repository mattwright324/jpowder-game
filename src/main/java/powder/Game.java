package powder;

import java.util.Random;


public class Game extends Thread {
	static boolean paused = false;
	static Display.FPS gfps = new Display.FPS();
	static Random r = new Random();

	static void update() {
		for (int w = 0; w < Display.width; w++) {
			for (int h = 0; h < Display.height; h++) {
				if (Cells.getParticleAt(w, h) != null) {
					try {
						Cells.cells[w][h].part.update();
					} catch (NullPointerException ignored) {
					}
				}
			}
		}
		gfps.add();
	}

	// Who needs performance?
	static void make_cells() {
		for (int w = 0; w < Display.width; w++) {
			for (int h = 0; h < Display.height; h++) {
				Cells.cells[w][h] = new Cell(w, h);
			}
		}
	}

	public void startUpdateThread() {
		start();
	}

	@SuppressWarnings("deprecation") // Ew
	public void stopUpdateThread() {
		stop();
	}

	public void run() {
		gfps.start();
		while (isAlive()) {
			if (Cells.cells != null && !paused) {
				update();
				gfps.add();
			}
			try {
				Thread.sleep(25);
			} catch (InterruptedException ignored) {
			}
		}
	}


}
