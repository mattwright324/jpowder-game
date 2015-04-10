package powder.elements;

import powder.Cells;
import powder.particles.Particle;
import powder.particles.ParticleBehaviour;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Element {
    public final static Map<Integer, Element> el_map = new HashMap<Integer, Element>();
    // Declare elements
    public static Element none = new Element(0, "NONE", Color.BLACK);
    public static Element dust = new Element(1, "DUST", "Dust", new Color(180, 180, 30));
    public static Element dmnd = new Element(2, "DMND", "Diamond", new Color(204, 204, 248));
    public static Element gas = new Element(3, "GAS", new Color(208, 180, 208));
    public static Element warp = new Element(4, "WARP", new Color(32, 32, 32));
    public static Element salt = new Element(5, "SALT", new Color(243, 243, 243));
    public static Element metl = new Element(6, "METL", new Color(64, 64, 224));
    public static Element phot = new Element(7, "PHOT", Color.WHITE);
    public static Element fire = new Element(8, "FIRE", Color.RED);
    public static Element wood = new Element(9, "WOOD", Color.ORANGE.darker());
    public static Element sprk = new Element(10, "SPRK", "Spark", Color.YELLOW);
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
    private static Random r = new Random();

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
    // Element properties - fun!
    static {
        none.remove = true;
        none.weight = -Integer.MAX_VALUE;
        el_map.put(0, none);

        dust.weight = 100;
        dust.flammibility = 0.6;
        dust.sandEffect = true;
        dust.setMovement(em_powder);
        el_map.put(1, dust);

        dmnd.weight = Integer.MAX_VALUE;
        el_map.put(2, dmnd);

        gas.weight = 5;
        gas.flammibility = 0.8;
        gas.sandEffect = true;
        gas.setMovement(em_gas);
        el_map.put(3, gas);

        warp.weight = Integer.MAX_VALUE;
        warp.life = 300;
        warp.life_dmode = 1;
        warp.setMovement(em_gas);
        el_map.put(4, warp);

        salt.weight = 100;
        salt.sandEffect = true;
        salt.setMovement(em_powder);
        el_map.put(5, salt);

        metl.weight = 1000;
        metl.conducts = true;
        el_map.put(6, metl);

        phot.life = 1000;
        phot.life_dmode = 1;
        phot.setParticleBehaviour(new ParticleBehaviour() {
            public void init(Particle p) {
                while (p.vx == 0 && p.vy == 0) {
                    p.vx = 3 * (r.nextInt(3) - 1);
                    p.vy = 3 * (r.nextInt(3) - 1);
                }
            }

            public void update(Particle p) {
            }
        });
        phot.setMovement(em_phot);
        el_map.put(7, phot);

        fire.life = 120;
        fire.life_dmode = 1;
        fire.setMovement(em_fire);
        fire.setParticleBehaviour(new ParticleBehaviour() {
            public void init(Particle p) {
                p.life += r.nextInt(50);
                p.setDeco(new Color((int) p.life, r.nextInt(20), r.nextInt(20)));
            }

            public void update(Particle p) {
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
        sprk.setParticleBehaviour(new ParticleBehaviour() {
            public void init(Particle p) {
                // TODO: stuff
            }

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
    public Color color = new Color(180, 180, 30);
    public IElementMovement movement;
    public ParticleBehaviour behaviour;
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

    public boolean heavierThan(Element e) {
        return e.weight > weight;
    }

    public boolean lighterThan(Element e) {
        return e.weight < weight;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    public void setMovement(IElementMovement em) {
        movement = em;
    }

    public void setParticleBehaviour(ParticleBehaviour bh) {
        behaviour = bh;
    }
}
