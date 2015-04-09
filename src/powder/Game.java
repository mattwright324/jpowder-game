package powder;

import java.awt.Color;
import java.util.Random;

import powder.Display.FPS;

public class Game extends Thread {
	
	static boolean paused = false;
	
	public void startUpdateThread() {
		start();
	}
	
	@SuppressWarnings("deprecation")
	public void stopUpdateThread() {
		stop();
	}
	
	static FPS gfps = new FPS();
	
	public void run() {
		gfps.start();
		while(isAlive()) {
			if(cells!=null && !paused) {
				for(int w=0; w<Display.width; w++) {
					for(int h=0; h<Display.height; h++) {
						if(getParticleAt(w, h)!=null) cells[w][h].part.update();
					}
				}
				gfps.add();
			}
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {}
		}
	}
	
	static Random r = new Random();
	
	static Cell[][] cells = new Cell[Display.width][Display.height];
	
	static void make_cells() {
		cells = new Cell[Display.width][Display.height];
		for(int w=0; w<Display.width; w++) {
			for(int h=0; h<Display.height; h++) {
				cells[w][h] = new Cell(w, h);
			}
		}
	}
	
	static class Cell {
		public Cell(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int screen_x() {
			return x / Display.img_scale;
		}
		
		public int screen_y() {
			return y / Display.img_scale;
		}
		
		public int x, y;
		public Particle part;
	}
	
	static boolean valid(int x, int y) {
		return !(x<0 || y<0 || x>=Display.width || y>=Display.height);
	}
	
	static boolean validGame(int x, int y) {
		return !(x<4 || y<4 || x>=Display.width-4 || y>=Display.height-4);
	}
	
	static Particle getParticleAt(int x, int y) {
		if(!valid(x, y)) return null;
		return cells[x][y].part;
	}
	
	static boolean particleAt(int x, int y) {
		if(!valid(x, y)) return true;
		return cells[x][y].part != null;
	}
	
	static void setParticleAt(int x, int y, Particle p, boolean insert) {
		if(!insert && particleAt(x, y)) return;
		if(p!=null) {
			p.x = x;
			p.y = y;
		}
		cells[x][y].part = p;
	}
	
	/**
	 * Move from (x1, y1) to (x2, y2). Original spot set null.
	 */
	static void moveTo(int x1, int y1, int x2, int y2) {
		if(!valid(x2, y2)) return;
		Particle a = getParticleAt(x1, y1);
		setParticleAt(x1, y1, null, true);
		setParticleAt(x2, y2, a, true);
	}
	
	/**
	 * Swap two particles coordinates.
	 */
	static void swap(int x1, int y1, int x2, int y2) {
		if(!valid(x2, y2)) return;
		Particle a = getParticleAt(x1, y1);
		Particle b = getParticleAt(x2, y2);
		setParticleAt(x1, y1, b, true);
		setParticleAt(x2, y2, a, true);
	}
	
	static ElementMovement em_powder = new ElementMovement() {
		public void move(Particle p) {
			int ny = p.y + 1;
			int nx = p.x + r.nextInt(3) - 1;
			p.tryMove(nx, ny);
		}
	};
	
	static ElementMovement em_gas = new ElementMovement() {
		public void move(Particle p) {
			int ny = p.y + r.nextInt(3) - 1;
			int nx = p.x + r.nextInt(3) - 1;
			p.tryMove(nx, ny);
		}
	};
	
	static ElementMovement em_phot = new ElementMovement() {
		public int vx = r.nextInt(3) - 1 + 3;
		public int vy = r.nextInt(3) - 1 + 3;
		public void move(Particle p) {
			int ny = p.y + vx;
			int nx = p.x + vy;
			p.tryMove(nx, ny);
		}
	};
	
	static Element dust, salt;
	static Element none, dmnd, metl;
	static Element gas, warp;
	static Element phot;
	
	static {
		none = new Element(0, "NONE", Color.BLACK);
		none.weight = -Integer.MAX_VALUE;
		
		dust = new Element(1, "DUST", "Dust", new Color(180, 180, 30));
		dust.weight = 100;
		dust.setMovement(em_powder);
		
		dmnd = new Element(2, "DMND", "Diamond", new Color(204, 204, 248));
		dust.weight = Integer.MAX_VALUE;
		dmnd.setMovement(null);
		
		gas = new Element(3, "GAS", new Color(208, 180, 208));
		gas.weight = 5;
		gas.setMovement(em_gas);
		
		warp = new Element(4, "WARP", new Color(32, 32, 32));
		warp.weight = Integer.MAX_VALUE;
		warp.setMovement(em_gas);
		
		salt = new Element(5, "SALT", new Color(244, 243, 243));
		salt.weight = 100;
		salt.setMovement(em_powder);
		
		metl = new Element(6, "METL", new Color(64, 64, 224));
		metl.weight = 1000;
		metl.setMovement(null);
		
		phot = new Element(7, "PHOT", Color.WHITE);
		phot.setMovement(em_phot);
	}
	
	static class Element {
		
		public Element(int eid, String name) {
			id = eid;
			shortName = name;
		}
		
		public Element(int eid, String name, Color c) {
			id = eid;
			shortName = name;
			setColor(c);
		}
		
		public Element(int eid, String name, String desc) {
			id = eid;
			shortName = name;
			description = desc;
		}
		
		public Element(int eid, String name, String desc, Color c) {
			id = eid;
			shortName = name;
			description = desc;
			setColor(c);
		}
		
		public int id = 0;
		public String shortName = "ELEM";
		public String longName = "Element";
		public String description = "Element description.";
		public int weight = 10;
		
		public boolean heavierThan(Element e) {
			return e.weight > weight;
		}
		
		public boolean lighterThan(Element e) {
			return e.weight < weight;
		}
		
		public static boolean sandEffect = false;
		public Color color = new Color(180, 180, 30);
		public Color sand = color;
		public Color deco; // For possible future decoration editor.
		
		public void setDeco(Color c) {
			deco = c;
		}
		
		public void setColor(Color c) {
			color = c;
			/*int red = 255 % (color.getRed() + (r.nextInt(19)-10));
			int green = 255 % (color.getGreen() + (r.nextInt(19)-10));
			int blue = 255 % (color.getBlue() + (r.nextInt(19)-10));
			sand = new Color(red, green, blue, color.getAlpha());*/
		}
		
		public Color getColor() {
			return deco != null ? deco : sandEffect ? sand : color;
		}
		
		public ElementMovement movement;
		
		public void setMovement(ElementMovement em) {
			movement = em;
		}
	}
	
	static class Particle {
		
		public Particle(Element e, int x, int y) {
			el = e;
			this.x = x;
			this.y = y;
			this.life = 0;
			this.celcius = 0.0;
		}
		
		public Particle(Element e, int x, int y, long life, double celcius) {
			this(e, x, y);
			this.life = life;
			this.celcius = celcius;
		}
		
		public Element el;
		public int x, y;
		
		public boolean heavierThan(Particle p) {
			return  el.weight > p.el.weight;
		}
		
		public boolean lighterThan(Particle p) {
			return el.weight < p.el.weight;
		}
		
		public Color getColor() {
			return el.getColor();
		}
		
		public void setColor(Color c) {
			el.deco = c;
		}
		
		public long life = 0;
		public double celcius = 0.0;
		
		public boolean remove = false;
		public void setRemove(boolean b) {
			remove = b;
		}
		public boolean remove() {
			return remove;
		}
		
		public long update = 100;
		public long last_update = System.currentTimeMillis();
		
		public boolean ready() {
			return System.currentTimeMillis()-last_update > update;
		}
		
		public void update() {
			if(ready()) {
				if(el.movement!=null)
					el.movement.move(this);
				if(!validGame(x, y)) setRemove(true);
				last_update = System.currentTimeMillis();
			}
		}
		
		public void tryMove(int nx, int ny) {
			Particle o = getParticleAt(nx, ny);
			if(o!=null) {
				if(heavierThan(o)) swap(x, y, nx, ny);
			} else {
				moveTo(x, y, nx, ny);
			}
		}
	}
	
	static interface ElementMovement {
		public void move(Particle p);
	}
}
