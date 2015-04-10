package powder;

import powder.elements.Element;
import powder.elements.IElementMovement;
import powder.particles.IParticleInit;
import powder.particles.IParticleUpdate;
import powder.particles.Particle;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class Game extends Thread {

	public final static Map<Integer, Element> el_map = new HashMap<Integer, Element>();
	static boolean paused = false;
	static Display.FPS gfps = new Display.FPS();
	static Random r = new Random();
	static IElementMovement em_powder = new IElementMovement() {
		public void move(Particle p) {
			int ny = p.y + 1;
			int nx = p.x + r.nextInt(3) - 1;
			p.tryMove(nx, ny);
		}
	};
	static IElementMovement em_gas = new IElementMovement() {
		public void move(Particle p) {
			int ny = p.y + (r.nextInt(3) - 1) + (int) p.vy;
			int nx = p.x + (r.nextInt(3) - 1) + (int) p.vx;
			p.tryMove(nx, ny);
		}
	};
	static IElementMovement em_phot = new IElementMovement() {
		public void move(Particle p) {
			int ny = p.y + (int) p.vx;
			int nx = p.x + (int) p.vy;
			if (!Cells.particleAt(nx, ny))
				Cells.moveTo(p.x, p.y, nx, ny);
			else
				p.setRemove(true);
		}
	};
	static IElementMovement em_fire = new IElementMovement() {
		public void move(Particle p) {
			int nx = p.x + (int) (p.vx = r.nextInt(3) - 1);
			int ny = p.y + (int) (p.vy = r.nextInt(5) - 2 - r.nextInt(2));
			Particle o = Cells.getParticleAt(nx, ny);
			if (o != null) {
				if (o.burn())
					Cells.setParticleAt(nx, ny, new Particle(p.el, nx, ny), true);
				else if (p.heavierThan(o)) Cells.swap(p.x, p.y, nx, ny);
			} else Cells.moveTo(p.x, p.y, nx, ny);
			p.setDeco(new Color((int) p.life, r.nextInt(20), r.nextInt(20)));
		}
	};
	static Element dust, salt;
	static Element none, dmnd, metl, wood, sprk;
	static Element gas, warp, fire;
	static Element phot;
	
	static {
		none = new Element(0, "NONE", Color.BLACK);
		none.remove = true;
		none.weight = -Integer.MAX_VALUE;
		el_map.put(0, none);

		dust = new Element(1, "DUST", "Dust", new Color(180, 180, 30));
		dust.weight = 100;
		dust.flammibility = 0.6;
		dust.sandEffect = true;
		dust.setMovement(em_powder);
		el_map.put(1, dust);

		dmnd = new Element(2, "DMND", "Diamond", new Color(204, 204, 248));
		dmnd.weight = Integer.MAX_VALUE;
		el_map.put(2, dmnd);

		gas = new Element(3, "GAS", new Color(208, 180, 208));
		gas.weight = 5;
		gas.flammibility = 0.8;
		gas.sandEffect = true;
		gas.setMovement(em_gas);
		el_map.put(3, gas);

		warp = new Element(4, "WARP", new Color(32, 32, 32));
		warp.weight = Integer.MAX_VALUE;
		warp.life = 300;
		warp.life_dmode = 1;
		warp.setMovement(em_gas);
		el_map.put(4, warp);

		salt = new Element(5, "SALT", new Color(244, 243, 243));
		salt.weight = 100;
		salt.sandEffect = true;
		salt.setMovement(em_powder);
		el_map.put(5, salt);

		metl = new Element(6, "METL", new Color(64, 64, 224));
		metl.weight = 1000;
		metl.conducts = true;
		el_map.put(6, metl);

		phot = new Element(7, "PHOT", Color.WHITE);
		phot.life = 1000;
		phot.life_dmode = 1;
		phot.setParticleInit(new IParticleInit() {
			public void init(Particle p) {
				while (p.vx == 0 && p.vy == 0) {
					p.vx = 3 * (r.nextInt(3) - 1);
					p.vy = 3 * (r.nextInt(3) - 1);
				}
			}
		});
		phot.setMovement(em_phot);
		el_map.put(7, phot);

		fire = new Element(8, "FIRE", Color.RED);
		fire.life = 120;
		fire.life_dmode = 1;
		fire.setMovement(em_fire);
		fire.setParticleInit(new IParticleInit() {
			public void init(Particle p) {
				p.life += r.nextInt(50);
				p.setDeco(new Color((int) p.life, r.nextInt(20), r.nextInt(20)));
			}
		});
		el_map.put(8, fire);

		wood = new Element(9, "WOOD", Color.ORANGE.darker());
		wood.flammibility = 0.5;
		wood.weight = 500;
		el_map.put(9, wood);

		sprk = new Element(10, "SPRK", "Spark", Color.YELLOW);
		sprk.life = 4;
		sprk.life_dmode = 2;
		sprk.setParticleUpdate(new IParticleUpdate() {
			public void update(Particle p) {
				if (p.life == 4)
					for (int w = 0; w < 5; w++)
						for (int h = 0; h < 5; h++) {
							int x = p.x - (w - 2);
							int y = p.y - (h - 2);
							if (Cells.valid(x, y)) {
								Particle o = Cells.getParticleAt(x, y);
								if (o != null) {
									Particle s = new Particle(sprk, x, y);
									s.ctype = o.el.id;
									if (o.el.conducts && o.life == 0) Cells.setParticleAt(x, y, s, true);
								}
							}
						}
			}
		});
		el_map.put(10, sprk);
	}

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
