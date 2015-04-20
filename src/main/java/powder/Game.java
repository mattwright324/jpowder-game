package main.java.powder;

import main.java.powder.particles.Particle;

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
			if(Cells.cells[0][0] != null && !paused) update();
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {}
		}
	}
	
	static void update() {
		for(int w = 0; w < Display.width; w++) {
			for(int h = 0; h < Display.height; h++) {
				if(Cells.particleAt(w, h)) {
					Particle p;
					for(int s=0; s<9; s++) {
						if((p = Cells.getParticleAt(w, h, s))!=null) {
							if(p.remove()) {
								Cells.deleteParticle(w, h);
							} else {
								p.update();
							}
						}
					}
				}
			}
		}
		gfps.add();
	}

}
