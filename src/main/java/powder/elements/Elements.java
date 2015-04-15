package main.java.powder.elements;

import main.java.powder.Cells;
import main.java.powder.particles.Particle;
import main.java.powder.particles.ParticleBehaviour;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Elements {
	
	public static Random r = new Random();
	
	public static final Map<Integer, String> id_name = new HashMap<Integer, String>();
	public static final Map<Integer, Element> el_map = new HashMap<Integer, Element>();
	
	public static final double MIN_TEMP = -273.15;
	public static final double DEFAULT_TEMP = 22.0;
	public static final double MAX_TEMP = 9725.85;
	
	public static final int DECAY_NONE = 0;
	public static final int DECAY_DIE = 1;
	public static final int DECAY_CTYPE = 2;
	
	public static final int WEIGHT_NONE = Integer.MIN_VALUE;
	public static final int WEIGHT_RADIO = 0;
	public static final int WEIGHT_GAS = 10;
	public static final int WEIGHT_LIQUID = 100;
	public static final int WEIGHT_POWDER = 1000;
	public static final int WEIGHT_SOLID = 10000;
	public static final int WEIGHT_DMND = 100000;
	
	public static final int CS_LSS = 10; // Converts less than temp.
    public static final int CS_GTR = 11; // Converts greater than temp.
    public static final int CS_EQ = 12; // Converts equal to temp.
	
    public static boolean exists(int id) {
    	return el_map.containsKey(id);
    }
    
	public static Element get(int id) {
		return el_map.get(id);
	}
	
	public static Element get(String name) {
		if(id_name.containsValue(name)) {
			for(int key : id_name.keySet())
				if(id_name.get(key).equals(name)) return el_map.get(key);
		}
		return el_map.get(0);
	}
	
	public static void add(int id, Element e) {
		id_name.put(id, e.shortName);
		el_map.put(id, e);
	}
	
	public static Element create(int id, String name, String desc, Color c, int weight) {
		Element e = new Element(id, name, desc, c);
		e.weight = weight;
		add(id, e);
		return e;
	}
	
	static ElementMovement em_phot = new ElementMovement() {
        public void move(Particle p) {
            int ny = p.y + (int) p.vy;
            int nx = p.x + (int) p.vx;
            if (!Cells.particleAt(nx, ny))
                Cells.moveTo(p.x, p.y, nx, ny);
            else
                p.setRemove(true);
        }
    };

    static ElementMovement em_radioactive = new ElementMovement() {
        public void move(Particle p) {
            double plottedX = p.x + p.vx;
            double plottedY = p.y + p.vy;
            // Shitty particle detection
            // Please improve
            // Added check for no collision with other radioactive particles
            if(rad_collide(plottedX, plottedY)) {
                p.vy = -p.vy;
                p.vx = -p.vx;
            } else if(rad_collide(plottedX, p.y)) {
                p.vx = -p.vx;
            } else if(rad_collide(p.x, plottedY)) {
                p.vy = -p.vy;
            }
            p.tryMove((int)plottedX, (int)plottedY);
        }
        
        public Particle collide;
        public boolean rad_collide(double x, double y) {
        	return (collide = Cells.getParticleAt((int)x, (int)y)) != null &&
        			(collide.el != Elements.radp || collide.el != Elements.phot);
        }
    };
	
	static ElementMovement em_powder = new ElementMovement() {
		public void move(Particle p) {
			int y = p.y + 1;
			p.tryMove(p.x, y);
			int x = p.x + (r.nextBoolean() ? -1 : 1);
			p.tryMove(x, y);
		}
	};
	
	static ElementMovement em_gas = new ElementMovement() {
        public void move(Particle p) {
            int ny = p.y + (r.nextInt(3) - 1) + (int) p.vy;
            int nx = p.x + (r.nextInt(3) - 1) + (int) p.vx;
            p.tryMove(nx, ny);
        }
    };
    static ElementMovement em_liquid = new ElementMovement() {
        public void move(Particle p) {
            int nx = p.x + r.nextInt(5) - 2;
            p.tryMove(nx, p.y);
            int ny = p.y + r.nextInt(2);
            p.tryMove(nx, ny);
        }
    };

    static ParticleBehaviour phot_behaviour = new ParticleBehaviour() {
        public void init(Particle p) {
            while (p.vx == 0 && p.vy == 0) {
                p.vx = 3 * (r.nextInt(3) - 1);
                p.vy = 3 * (r.nextInt(3) - 1);
            }
        }

        public void update(Particle p) {
        }
    };

    static ParticleBehaviour fire_behaviour = new ParticleBehaviour() {
        public void init(Particle p) {
            p.life += r.nextInt(50);
            p.celcius += r.nextInt(20);
        }

        public void update(Particle p) {
            p.tryMove(p.x, p.y - (r.nextInt(4) - 1));
            for (int w = 0; w < 3; w++)
                for (int h = 0; h < 3; h++) {
                	Particle o;
                	if((o = Cells.getParticleAt(p.x +w-1, p.y+h-1)) != null && o.burn()) {
                        o.morph(fire, Particle.MORPH_KEEP_TEMP, false);
                    }
                }
                    
            p.setDeco(new Color((int) p.life, r.nextInt(20), r.nextInt(20)));
        }
    };

    static ParticleBehaviour plutonium_behaviour = new ParticleBehaviour() {
        public void init(Particle p) {
            p.life = r.nextInt(400) + 100;
        }
        public void update(Particle p) {}
        public void destruct(Particle p) {
            p.morph(radp, Particle.MORPH_KEEP_TEMP, true);
            p.celcius += 4500; // Decay should result in a lot of heat + pressure (when added)
        }
    };

    static ParticleBehaviour radioactive_behaviour = new ParticleBehaviour() {
        public void init(Particle p) {
            p.life = r.nextInt(50) + 1;
            p.celcius = 982;
            p.vx = r.nextInt(4) + 1;
            p.vy = r.nextInt(4) + 1;
        }
        
        public void update(Particle p) {
            for (Particle part : Cells.getSurroundingParticles(p.x, p.y)) {
                if(part!=null && part.el == Elements.plut) {
                    part.life = 1;
                }
            }
        }
    };

    static ParticleBehaviour sprk_behaviour = new ParticleBehaviour() {
        public void init(Particle p) {
        }
        public void update(Particle p) {
            if (p.life == 4)
                for (int w = 0; w < 5; w++)
                    for (int h = 0; h < 5; h++) {
                        int x = p.x - (w - 2);
                        int y = p.y - (h - 2);
                        Particle o;
                        if (Cells.valid(x, y) && (o=Cells.getParticleAt(x, y))!=null) {
                        	if (o.el.conducts && o.life == 0) o.morph(sprk, Particle.MORPH_FULL, true);
                        }
                    }
            if(p.life==0) {
            	p.morph(get(p.ctype), Particle.MORPH_FULL, false);
            	if(p.celcius < 300)
            		p.celcius += 50;
            	else
            		p.celcius -= 50;
            	p.life = 6;
            }
        }
    };

    static ParticleBehaviour plsm_behaviour = new ParticleBehaviour() {
        public void init(Particle p) {
            p.life += r.nextInt(50);
            p.celcius += r.nextInt(20);
        }

        public void update(Particle p) {
            p.tryMove(p.x + (r.nextInt(3) - 1), p.y - (r.nextInt(4) - 1));
            for (int w = 0; w < 3; w++)
                for (int h = 0; h < 3; h++) {
                	Particle o;
                	if((o = Cells.getParticleAt(p.x+(w-1), p.y+(h-1)))!=null && o.burn())
                		o.morph(fire, Particle.MORPH_KEEP_TEMP, false);
                }
        }
    };
    
    static ParticleBehaviour clne_behaviour = new ParticleBehaviour() {
		public void init(Particle p) {
			// Needs to be set ctype on click
			p.ctype = none.id;
		}
		public void update(Particle p) {
            if (p.ctype == 0) return; // Won't create anything when newly placed.
			for(int w=-1; w<2; w++)
				for(int h=-1; h<2; h++)
					if(!Cells.particleAt(p.x+w, p.y+h)) {
						int x = p.x+w;
						int y = p.y+h;
						Cells.setParticleAt(x, y, new Particle(get(p.ctype), x, y), false);
					}
		}
    };
	
	public static final Element none, sprk;
	public static final Element dust, stne, salt, bcol, plut;
	public static final Element metl, qrtz, dmnd, coal, insl, clne;
	public static final Element watr, lava, ln2, oil;
	public static final Element phot, radp;
	public static final Element gas, warp, fire, plsm, stm;
	static { // 
		none = create(0, "NONE", "Erase", Color.BLACK, WEIGHT_NONE);
		none.remove = true;
		
		dust = create(1, "DUST", "Dust", new Color(162, 168, 9), WEIGHT_POWDER-1);
		dust.setMovement(em_powder);
		dust.flammibility = 0.6;
		
		stne = create(2, "STNE", "Stone", Color.LIGHT_GRAY, WEIGHT_POWDER);
		stne.setMovement(em_powder);
		
		salt = create(3, "SALT", "Salt", new Color(243, 243, 243), WEIGHT_POWDER-1);
		salt.setMovement(em_powder);
		
		bcol = create(4, "BCOL", "Broken Coal", Color.GRAY.brighter(), WEIGHT_POWDER);
		bcol.setMovement(em_powder);
		
		metl = create(5, "METL", "Metal", new Color(112, 122, 255), WEIGHT_SOLID);
		metl.conducts = true;
		
		qrtz = create(6, "QRTZ", "Quartz", new Color(120, 226, 237), WEIGHT_SOLID);
		
		dmnd = create(7, "DMND", "Diamond", new Color(32, 248, 228), WEIGHT_DMND);
		
		coal = create(8, "COAL", "Coal", Color.GRAY, WEIGHT_SOLID);
		coal.flammibility = 0.2;
		
		insl = create(9, "INSL", "Insulator", new Color(170, 170, 170), WEIGHT_SOLID);
		insl.heatTransfer = 0;
		insl.flammibility = 0.1;
		
		plut = create(10, "PLUT", "Plutonium", new Color(0, 179, 21), WEIGHT_POWDER);
		plut.setParticleBehaviour(plutonium_behaviour);
		plut.setMovement(em_powder);
		
		sprk = create(11, "SPRK", "Spark", Color.YELLOW, WEIGHT_SOLID);
		sprk.setParticleBehaviour(sprk_behaviour);
		
		watr = create(12, "WATR", "Water", Color.BLUE, WEIGHT_LIQUID);
		watr.setMovement(em_liquid);
		watr.conducts = true;
		
		lava = create(13, "LAVA", "Lava", Color.ORANGE, WEIGHT_LIQUID);
		lava.setMovement(em_liquid);
		lava.setParticleBehaviour(new ParticleBehaviour(){
			public void init(Particle p) {
				p.ctype = stne.id;
			}
			public void update(Particle p) {}
        });
		lava.celcius = 1522;
		
		ln2 = create(14, "LN2", "Liquid Nitrogen", new Color(190, 226, 237), WEIGHT_LIQUID);
		ln2.setMovement(em_liquid);
		ln2.celcius = MIN_TEMP;
		
		oil = create(15, "OIL", "Oil", Color.GREEN.darker(), WEIGHT_LIQUID);
		oil.setMovement(em_liquid);
		oil.flammibility = 0.3;
		
		phot = create(16, "PHOT", "Light", Color.WHITE, WEIGHT_RADIO);
		phot.setMovement(em_phot);
		phot.setParticleBehaviour(phot_behaviour);
		
		radp = create(17, "RADP", "Radioactive Particle", Color.MAGENTA, WEIGHT_RADIO);
		radp.setMovement(em_radioactive);
		radp.setParticleBehaviour(radioactive_behaviour);
		radp.celcius = 982;
		
		gas = create(18, "GAS", "Gas", new Color(208, 180, 208), WEIGHT_GAS);
		gas.setMovement(em_gas);
		gas.flammibility = 0.8;
		
		warp = create(19, "WARP", "Warp", new Color(32, 32, 32), WEIGHT_DMND-1);
		warp.setMovement(em_gas);
		
		fire = create(20, "FIRE", "Fire", Color.RED, WEIGHT_GAS);
		fire.setParticleBehaviour(fire_behaviour);
		fire.celcius = 450;
		
		plsm = create(21, "PLSM", "Plasma", new Color(217, 151, 219), WEIGHT_GAS);
		plsm.setParticleBehaviour(plsm_behaviour);
		plsm.celcius = MAX_TEMP;
		
		clne = create(22, "CLNE", "Clone", Color.YELLOW, WEIGHT_GAS);
		clne.setParticleBehaviour(clne_behaviour);
		
		stm = create(23, "STM", "Steam", new Color(172, 177, 242), WEIGHT_GAS);
		stm.setMovement(em_gas);
	}
	
	static { // Conversions
		lava.setCtypeConvert(CS_LSS, 700);
		
		salt.setConvert(lava, CS_GTR, 750);
		stne.setConvert(lava, CS_GTR, 850);
		metl.setConvert(lava, CS_GTR, 1000);
		qrtz.setConvert(lava, CS_GTR, 3000);

		gas.setConvert(fire, CS_GTR, 300);

		oil.setConvert(gas, CS_GTR, 150);

		watr.setConvert(stm, CS_GTR, 100);
		stm.setConvert(watr, CS_LSS, 100);
		
		fire.setConvert(plsm, CS_GTR, 1000);
		plsm.setConvert(fire, CS_LSS, 1000);
	}
	
	public static final Element[] powder = {dust, stne, salt, bcol, plut};
	public static final Element[] liquid = {watr, lava, ln2, oil};
	public static final Element[] solid = {metl, qrtz, dmnd, coal, insl};
	public static final Element[] gasses = {gas, warp, fire, plsm, stm};
	public static final Element[] radio = {phot, radp};
	public static final Element[] tools = {none, sprk};
}
