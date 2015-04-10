package powder;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
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
				update();
				gfps.add();
			}
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {}
		}
	}
	
	static void update() {
		for(int w=0; w<Display.width; w++) {
			for(int h=0; h<Display.height; h++) {
				if(getParticleAt(w, h)!=null) try{cells[w][h].part.update();}catch(NullPointerException e){};
			}
		}
		gfps.add();
	}
	
	static Random r = new Random();
	
	final static Cell[][] cells = new Cell[Display.width][Display.height];;
	
	static void make_cells() {
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
			return x * Display.img_scale;
		}
		
		public int screen_y() {
			return y * Display.img_scale;
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
		if(!valid(x, y)) return;
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
			int ny = p.y + (r.nextInt(3) - 1) + (int)p.vy;
			int nx = p.x + (r.nextInt(3) - 1) + (int)p.vx;
			p.tryMove(nx, ny);
		}
	};
	
	static ElementMovement em_phot = new ElementMovement() {
		public void move(Particle p) {
			int ny = p.y + (int) p.vx;
			int nx = p.x + (int) p.vy;
			if(!particleAt(nx, ny))
				moveTo(p.x, p.y, nx, ny);
			else
				p.setRemove(true);
		}
	};
	
	static ElementMovement em_fire = new ElementMovement() {
		public void move(Particle p) {
			int nx = p.x + (int) (p.vx = r.nextInt(3)-1);
			int ny = p.y + (int) (p.vy = r.nextInt(5)-2 - r.nextInt(2));
			Particle o = getParticleAt(nx, ny);
			if(o!=null) {
				if(o.burn())
					setParticleAt(nx, ny, new Particle(p.el, nx, ny), true);
				else if(p.heavierThan(o)) swap(p.x, p.y, nx, ny);
			} else moveTo(p.x, p.y, nx, ny);
			p.setDeco(new Color((int)p.life, r.nextInt(20), r.nextInt(20)));
		}
	};
	
	final static Map<Integer, Element> el_map = new HashMap<Integer, Element>();
	
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
		phot.setParticleInit(new ParticleInit(){
			public void init(Particle p) {
				while(p.vx==0 && p.vy==0) {
					p.vx = 3 * (r.nextInt(3)-1);
					p.vy = 3 * (r.nextInt(3)-1);
				}
			}
		});
		phot.setMovement(em_phot);
		el_map.put(7, phot);
		
		fire = new Element(8, "FIRE", Color.RED);
		fire.life = 120;
		fire.life_dmode = 1;
		fire.setMovement(em_fire);
		fire.setParticleInit(new ParticleInit(){
			public void init(Particle p) {
				p.life += r.nextInt(50);
				p.setDeco(new Color((int)p.life, r.nextInt(20), r.nextInt(20)));
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
		sprk.setParticleUpdate(new ParticleUpdate(){
			public void update(Particle p) {
				if(p.life==4)
				for(int w=0; w<5; w++)
					for(int h=0; h<5; h++) {
						int x = p.x - (w-2);
						int y = p.y - (h-2);
						if(valid(x, y)) {
							Particle o = getParticleAt(x, y);
							if(o!=null) {
								Particle s = new Particle(sprk, x, y);
								s.ctype = o.el.id;
								if(o.el.conducts && o.life==0) setParticleAt(x, y, s, true);
							}
						}
					}
			}
		});
		el_map.put(10, sprk);
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
		public String description = "Element description.";
		
		public int weight = 10;
		public long life = 0;
		public double celcius = 0.0;
		public boolean remove = false;
		public double flammibility = 0;
		public boolean conducts = false;
		public boolean sandEffect = false;
		
		public boolean life_decay = true;
		public int life_dmode = 0; // 0 = Nothing, 1 = Remove, 2 = Change to Ctype
		
		public boolean heavierThan(Element e) {
			return e.weight > weight;
		}
		
		public boolean lighterThan(Element e) {
			return e.weight < weight;
		}
		
		public Color color = new Color(180, 180, 30);
		
		public void setColor(Color c) {
			color = c;
		}
		
		public Color getColor() {
			return color;
		}
		
		public ElementMovement movement;
		public void setMovement(ElementMovement em) {
			movement = em;
		}
		
		public ParticleInit init;
		public void setParticleInit(ParticleInit pi) {
			init = pi;
		}
		
		public ParticleUpdate update;
		public void setParticleUpdate(ParticleUpdate pu) {
			update = pu; 
		}
	}
	
	static class Particle {
		
		public Particle(Element e, int x, int y) {
			el = e;
			this.x = x;
			this.y = y;
			this.life = el.life;
			this.celcius = el.celcius;
			setRemove(el.remove);
			if(el.sandEffect) addSandEffect();
			if(el.init!=null) el.init.init(this);
		}
		
		public Particle(Element e, int x, int y, long life, double celcius) {
			el = e;
			this.x = x;
			this.y = y;
			this.life = life;
			this.celcius = celcius;
			setRemove(el.remove);
			if(el.sandEffect) addSandEffect();
			if(el.init!=null) el.init.init(this);
		}
		
		public Element el;
		public int x, y;
		
		public int ctype = 0;
		
		public boolean burn() {
			return Math.random() < el.flammibility;
		}
		
		public boolean heavierThan(Particle p) {
			return  el.weight > p.el.weight;
		}
		
		public boolean lighterThan(Particle p) {
			return el.weight < p.el.weight;
		}
		
		public Color getColor() {
			return deco!=null ? deco : el.getColor();
		}
		
		public Color deco;
		public void setDeco(Color c) {
			deco = c;
		}
		
		public void addSandEffect() {
			Color color = el.getColor();
			int red = (color.getRed() + (r.nextInt(19)-10));
			int green = (color.getGreen() + (r.nextInt(19)-10));
			int blue = (color.getBlue() + (r.nextInt(19)-10));
			setDeco(new Color(Math.abs(red) % 256, Math.abs(green) % 256, Math.abs(blue) % 256, color.getAlpha()));
		}
		
		public double vx = 0, vy = 0;
		public long life = 0;
		public double celcius = 0.0;
		
		public boolean remove = false;
		public void setRemove(boolean b) {
			remove = b;
		}
		public boolean remove() {
			return remove;
		}
		
		public long update = 50;
		public long last_update = System.currentTimeMillis();
		
		public boolean ready() {
			return System.currentTimeMillis()-last_update > update;
		}
		
		public void update() {
			if(ready()) {
				if(el.update!=null)
					el.update.update(this);
				if(el.movement!=null)
					el.movement.move(this);
				
				if(life>0) life--;
				if(life-1==0) {
					if(el.life_dmode == 1) setRemove(true);
					if(el.life_dmode == 2) setParticleAt(x, y, new Particle(el_map.get(ctype), x, y), true);
				}
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
	
	static interface ParticleInit {
		public void init(Particle p);
	}
	
	static interface ParticleUpdate {
		public void update(Particle p);
	}
}
