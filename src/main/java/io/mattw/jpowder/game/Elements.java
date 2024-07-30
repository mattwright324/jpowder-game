package io.mattw.jpowder.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Elements {

    private static final Logger logger = LogManager.getLogger();

    public static final Map<Integer, String> ID_NAME = new HashMap<>();
    public static final Map<Integer, Element> EL_MAP = new HashMap<>();
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
    public static final Element none, sprk, fill, ant;
    public static final Element dust, stne, salt, bcol, plut;
    public static final Element metl, qrtz, dmnd, coal, insl, clne, ice, void_;
    public static final Element watr, lava, ln2, oil;
    public static final Element phot, radp;
    public static final Element gas, warp, fire, plsm, stm;
    public static final Random random = new Random();

    public static final ElementMovement em_phot = p -> {
        int ny = p.getY() + (int) p.getVy();
        int nx = p.getX() + (int) p.getVx();
        p.tryMove(nx, ny);
    };
    public static final ElementMovement em_radioactive = new ElementMovement() {
        public Particle collide;

        public void move(Particle p) {
            double plottedX = p.getX() + p.getVx();
            double plottedY = p.getY() + p.getVy();
            // Shitty particle detection
            // Please improve
            // Added check for no collision with other radioactive particles
            if (rad_collide(plottedX, plottedY)) {
                p.setVy(-p.getVy());
                p.setVx(-p.getVx());
            } else if (rad_collide(plottedX, p.getY())) {
                p.setVx(-p.getVx());
            } else if (rad_collide(p.getX(), plottedY)) {
                p.setVy(-p.getVy());
            }
            p.tryMove((int) plottedX, (int) plottedY);
        }

        public boolean rad_collide(double x, double y) {
            return (collide = Grid.getStackTop((int) x, (int) y)) != null &&
                    (collide.getEl() != Elements.radp || collide.getEl() != Elements.phot);
        }
    };
    public static final ElementMovement em_powder = p -> {
        int y = p.getY() + 1;
        p.tryMove(p.getX(), y);
        int x = p.getX() + (random.nextBoolean() ? -1 : 1);
        p.tryMove(x, y);
    };
    public static final ElementMovement em_gas = p -> {
        int ny = p.getY() + (random.nextInt(3) - 1) + (int) p.getVy();
        int nx = p.getX() + (random.nextInt(3) - 1) + (int) p.getVx();
        p.tryMove(nx, ny);
    };
    public static final ElementMovement em_liquid = p -> {
        int nx = p.getX() + random.nextInt(5) - 2;
        p.tryMove(nx, p.getY());
        int ny = p.getY() + random.nextInt(2);
        p.tryMove(nx, ny);
    };
    public static final ParticleBehaviour pb_phot = new ParticleBehaviour() {
        public void init(Particle p) {
            while (p.getVx() == 0 && p.getVy() == 0) {
                p.setVx(3 * (random.nextInt(3) - 1));
                p.setVy(3 * (random.nextInt(3) - 1));
            }
        }

        public void update(Particle p) {
        }
    };
    public static final ParticleBehaviour pb_fire = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(p.getLife() + random.nextInt(50));
            p.setCelcius(p.getCelcius() + random.nextInt(20));
        }

        public void update(Particle p) {
            p.tryMove(p.getX(), p.getY() - (random.nextInt(4) - 1));
            for (int w = 0; w < 3; w++) {
                for (int h = 0; h < 3; h++) {
                    Particle o;
                    if ((o = Grid.getStackTop(p.getX() + w - 1, p.getY() + h - 1)) != null && o.burn()) {
                        o.morph(fire, Particle.MORPH_FULL, false);
                    }
                }
            }

            p.setDeco(new Color((int) p.getLife(), random.nextInt(20), random.nextInt(20)));
        }
    };
    public static final ParticleBehaviour pb_plut = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(random.nextInt(400) + 100);
        }

        public void update(Particle p) {
        }

        public void destruct(Particle p) {
            p.morph(radp, Particle.MORPH_KEEP_TEMP, true);
            p.setCelcius(p.getCelcius() + 4500); // Decay should result in a lot of heat + pressure (when added)
        }
    };
    public static final ParticleBehaviour pb_radio = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(random.nextInt(50) + 1);
            p.setCelcius(982);
            p.setVx(random.nextInt(4) + 1);
            p.setVy(random.nextInt(4) + 1);
        }

        public void update(Particle p) {
            for (Particle part : Grid.getSurrounding(p.getX(), p.getY())) {
                if (part != null && part.getEl() == Elements.plut) {
                    part.setLife(1);
                }
            }
        }
    };
    public static final ParticleBehaviour pb_sprk = new ParticleBehaviour() {
        public void init(Particle p) {
        }

        public void update(Particle p) {
            if (p.getLife() == 4) {
                for (int w = 0; w < 5; w++) {
                    for (int h = 0; h < 5; h++) {
                        int x = p.getX() - (w - 2);
                        int y = p.getY() - (h - 2);
                        Particle o;
                        if (Grid.validCell(x, y, 0) && (o = Grid.getStackTop(x, y)) != null) {
                            if (o.getEl().isConducts() && o.getLife() == 0) {
                                o.morph(sprk, Particle.MORPH_FULL, true);
                            }
                        }
                    }
                }
            }
            if (p.getLife() == 0) {
                if (p.getCtype() != 0) {
                    p.morph(get(p.getCtype()), Particle.MORPH_FULL, false);
                }
                if (p.getCelcius() < 300) {
                    p.setCelcius(p.getCelcius() + 50);
                } else {
                    p.setCelcius(p.getCelcius() - 50);
                }
                p.setLife(6);
            }
        }
    };
    public static final ParticleBehaviour pb_plsm = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(p.getLife() + random.nextInt(50));
            p.setCelcius(p.getCelcius() + random.nextInt(20));
        }

        public void update(Particle p) {
            p.tryMove(p.getX() + (random.nextInt(3) - 1), p.getY() - (random.nextInt(4) - 1));
            for (int w = 0; w < 3; w++) {
                for (int h = 0; h < 3; h++) {
                    Particle o;
                    if ((o = Grid.getStackTop(p.getX() + (w - 1), p.getY() + (h - 1))) != null && o.burn()) {
                        o.morph(fire, Particle.MORPH_KEEP_TEMP, false);
                    }
                }
            }
        }
    };
    public static final ParticleBehaviour pb_clne = new ParticleBehaviour() {
        public void init(Particle p) {
            // Needs to be set ctype on click
            p.setCtype(none.getId());
        }

        public void update(Particle p) {
            if (p.getCtype() == 0) {
                return; // Won't create anything when newly placed.
            }
            for (int w = -1; w < 2; w++) {
                for (int h = -1; h < 2; h++) {
                    if (Grid.cell(p.getX() + w, p.getY() + h).empty()) {
                        int x = p.getX() + w;
                        int y = p.getY() + h;
                        Grid.cell(x, y).add(get(p.getCtype()));
                    } else {
                        Particle o;
                        if ((o = Grid.getStackTop(p.getX() + w, p.getY() + h)) != null) {
                            if (o.getEl() == clne && o.getCtype() == 0) {
                                o.setCtype(p.getCtype());
                            }
                        }
                    }
                }
            }

        }
    };
    public static final ParticleBehaviour pb_lava = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setCtype(stne.getId());
        }

        public void update(Particle p) {
        }
    };
    public static final ParticleBehaviour pb_qrtz = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setTmp(random.nextInt(10));
            p.setDeco(new Color(120, 226, 150 + (int) (105 * (p.getTmp() / 10.0))));
        }

        public void update(Particle p) {
            if (p.getCelcius() > 1670 && Math.random() < 0.01) {
                p.morph(lava, Particle.MORPH_KEEP_TEMP, true);
            }
        }
    };
    public static final ParticleBehaviour pb_fill = new ParticleBehaviour() {
        public void init(Particle p) {
        }

        public void update(Particle p) {
            if (p.getTmp() == 0) {
                p.setTmp(1);
                set(p.getX() + 1, p.getY());
                set(p.getX() - 1, p.getY());
                set(p.getX(), p.getY() + 1);
                set(p.getX(), p.getY() - 1);
            }
        }

        public void set(int x, int y) {
            if (Grid.validCell(x, y, 0) && Grid.cell(x, y).addable(fill)) {
                Grid.cell(x, y).add(fill);
            }
        }
    };
    public static final ParticleBehaviour pb_ant = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(180);
            p.setTmp(1); // tmp 0 = dead, 1 = right, 2 = left
        }

        public void update(Particle p) { // TODO Works ok but doesn't act as Langton's Ant should.
            if (p.getTmp() == 0) {
                p.setDeco(Color.GRAY);
            }
            if (p.getTmp() == 1 || p.getTmp() == 2) {
                int angle = 0;
                if (p.getTmp() == 1) {
                    p.setLife(p.getLife() - 90);
                    angle = (int) p.getLife();
                }
                if (p.getTmp() == 2) {
                    p.setLife(p.getLife() + 90);
                    angle = (int) p.getLife();
                }
                if (angle < 0) {
                    angle = 360 + angle;
                }
                if (angle > 270) {
                    angle = angle - 270;
                }
                p.setLife(angle);
                int x = p.getX();
                int y = p.getY();
                int nx = p.getX();
                int ny = p.getY();
                if (angle == 0) {
                    nx += 1;
                }
                if (angle == 180) {
                    nx -= 1;
                }
                if (angle == 90) {
                    ny -= 1;
                }
                if (angle == 270) {
                    ny += 1;
                }
                logger.info("life: {}, angle: {}, x.y: {}.{} -> nx.ny: {}.{}", p.getLife(), angle, x, y, nx, ny);
                Particle o = Grid.getStackTop(nx, ny);
                if (o == null) {
                    p.setTmp(1);
                    Particle dead = new Particle(Elements.coal, x, y);
                    dead.setTmp(0);
                    Grid.setStack(nx, ny, p);
                    Grid.setStack(x, y, dead);
                } else {
                    p.setTmp(2);
                    Grid.setStack(nx, ny, p);
                    Grid.remStack(x, y);
                }
            }
        }
    };

    static {
        none = create(0, "NONE", "Erase", Color.BLACK, WEIGHT_NONE);
        none.setRemove(true);

        dust = create(1, "DUST", "Dust", new Color(162, 168, 9), WEIGHT_POWDER - 1);
        dust.setMovement(em_powder);
        dust.setFlammibility(0.6);

        stne = create(2, "STNE", "Stone", Color.LIGHT_GRAY, WEIGHT_POWDER);
        stne.setMovement(em_powder);

        salt = create(3, "SALT", "Salt", new Color(243, 243, 243), WEIGHT_POWDER - 1);
        salt.setMovement(em_powder);

        bcol = create(4, "BCOL", "Broken Coal", Color.GRAY.brighter(), WEIGHT_POWDER);
        bcol.setMovement(em_powder);

        metl = create(5, "METL", "Metal", new Color(112, 122, 255), WEIGHT_SOLID);
        metl.setConducts(true);

        qrtz = create(6, "QRTZ", "Quartz", new Color(120, 226, 237), WEIGHT_SOLID);
        qrtz.setTmpDecay(false);
        qrtz.setParticleBehaviour(pb_qrtz);

        dmnd = create(7, "DMND", "Diamond", new Color(32, 248, 228), WEIGHT_DMND);

        coal = create(8, "COAL", "Coal", Color.GRAY, WEIGHT_SOLID);
        coal.setFlammibility(0.2);

        insl = create(9, "INSL", "Insulator", new Color(170, 170, 170), WEIGHT_SOLID);
        insl.setHeatTransfer(0);
        insl.setFlammibility(0.1);

        plut = create(10, "PLUT", "Plutonium", new Color(0, 179, 21), WEIGHT_POWDER);
        plut.setParticleBehaviour(pb_plut);
        plut.setMovement(em_powder);
        plut.setGlow(true);

        sprk = create(11, "SPRK", "Spark", Color.YELLOW, WEIGHT_SOLID);
        sprk.setParticleBehaviour(pb_sprk);
        sprk.setLifeDecayMode(DECAY_CTYPE);
        sprk.setLife(4);

        watr = create(12, "WATR", "Water", Color.BLUE, WEIGHT_LIQUID);
        watr.setMovement(em_liquid);
        watr.setConducts(true);
        watr.setGlow(true);

        lava = create(13, "LAVA", "Lava", Color.ORANGE, WEIGHT_LIQUID);
        lava.setMovement(em_liquid);
        lava.setParticleBehaviour(pb_lava);
        lava.setCelcius(1522);
        lava.setGlow(true);

        ln2 = create(14, "LN2", "Liquid Nitrogen", new Color(190, 226, 237), WEIGHT_LIQUID);
        ln2.setMovement(em_liquid);
        ln2.setCelcius(MIN_TEMP);
        ln2.setGlow(true);

        oil = create(15, "OIL", "Oil", Color.GREEN.darker(), WEIGHT_LIQUID);
        oil.setMovement(em_liquid);
        oil.setFlammibility(0.3);
        oil.setGlow(true);

        phot = create(16, "PHOT", "Light", Color.WHITE, WEIGHT_RADIO);
        phot.setMovement(em_phot);
        phot.setParticleBehaviour(pb_phot);
        phot.setStackable(true);
        phot.setGlow(true);

        radp = create(17, "RADP", "Radioactive Particle", Color.MAGENTA, WEIGHT_RADIO);
        radp.setMovement(em_radioactive);
        radp.setParticleBehaviour(pb_radio);
        radp.setCelcius(982);
        radp.setStackable(true);
        radp.setGlow(true);

        gas = create(18, "GAS", "Gas", new Color(208, 180, 208), WEIGHT_GAS);
        gas.setMovement(em_gas);
        gas.setFlammibility(0.8);
        gas.setGlow(true);

        warp = create(19, "WARP", "Warp", new Color(32, 32, 32), WEIGHT_DMND - 1);
        warp.setMovement(em_gas);
        warp.setLife(500);
        warp.setLifeDecayMode(DECAY_DIE);

        fire = create(20, "FIRE", "Fire", Color.RED, WEIGHT_GAS);
        fire.setMovement(em_gas);
        fire.setParticleBehaviour(pb_fire);
        fire.setCelcius(450);
        fire.setLifeDecayMode(DECAY_DIE);
        fire.setLife(120);
        fire.setGlow(true);

        plsm = create(21, "PLSM", "Plasma", new Color(217, 151, 219), WEIGHT_GAS);
        plsm.setParticleBehaviour(pb_plsm);
        plsm.setCelcius(MAX_TEMP);
        plsm.setLifeDecayMode(DECAY_DIE);
        plsm.setLife(120);
        plsm.setGlow(true);

        clne = create(22, "CLNE", "Clone", Color.YELLOW, WEIGHT_SOLID);
        clne.setParticleBehaviour(pb_clne);

        stm = create(23, "STM", "Steam", new Color(172, 177, 242), WEIGHT_GAS);
        stm.setMovement(em_gas);
        stm.setGlow(true);

        ice = create(24, "ICE", "Ice", new Color(200, 200, 255), WEIGHT_SOLID);
        ice.setCelcius(-25);

        fill = create(25, "FILL", "Filler", Color.LIGHT_GRAY, WEIGHT_DMND);
        fill.setTmpDecay(false);
        fill.setHeatTransfer(0.5);
        fill.setParticleBehaviour(pb_fill);

        ant = create(26, "ANT", "Langton's Ant", Color.GREEN, WEIGHT_DMND);
        ant.setTmpDecay(false);
        ant.setLifeDecay(false);
        ant.setParticleBehaviour(pb_ant);

        void_ = create(27, "VOID", "Removes interacting particles", new Color(255, 96, 96), WEIGHT_DMND);
    }

    public static final Item[] powder = {dust, stne, salt, bcol};
    public static final Item[] liquid = {watr, lava, ln2, oil};
    public static final Item[] solid = {metl, qrtz, dmnd, coal, insl, ice, clne, void_};
    public static final Item[] gasses = {gas, fire, plsm, stm};
    public static final Item[] radio = {phot, radp, plut, warp};
    public static final Item[] tools = {none, sprk, fill, ant, Walls.none, Walls.wall, Walls.air, Walls.wvoid};

    static { // Conversions
        lava.addCtypeConvert(CS_LSS, 700);

        salt.addConvert(lava, CS_GTR, 750);
        stne.addConvert(lava, CS_GTR, 850);
        metl.addConvert(lava, CS_GTR, 1000);
        //qrtz.addConvert(lava, CS_GTR, 1670);

        oil.addConvert(gas, CS_GTR, 150);
        gas.addConvert(fire, CS_GTR, 300);

        ice.addConvert(watr, CS_GTR, 0);
        watr.addConvert(ice, CS_LSS, 0);
        watr.addConvert(stm, CS_GTR, 100);
        stm.addConvert(watr, CS_LSS, 100);

        fire.addConvert(plsm, CS_GTR, 1000);
        plsm.addConvert(fire, CS_LSS, 1000);
    }

    public static boolean exists(int id) {
        return EL_MAP.containsKey(id);
    }

    public static Element get(int id) {
        return EL_MAP.get(id);
    }

    public static Element get(String name) {
        name = name.toUpperCase();
        if (ID_NAME.containsValue(name)) {
            for (int key : ID_NAME.keySet()) {
                if (ID_NAME.get(key).equals(name)) {
                    return EL_MAP.get(key);
                }
            }
        }
        return EL_MAP.get(0);
    }

    public static int getID(String name) {
        name = name.toUpperCase();
        if (ID_NAME.containsValue(name)) {
            for (int key : ID_NAME.keySet()) {
                if (ID_NAME.get(key).equals(name)) {
                    return key;
                }
            }
        }
        return 0;
    }

    public static void add(int id, Element e) {
        ID_NAME.put(id, e.getName().toUpperCase());
        EL_MAP.put(id, e);
    }

    public static Element create(int id, String name, String desc, Color c, int weight) {
        Element e = new Element(id, name, desc, c);
        e.setWeight(weight);
        add(id, e);
        return e;
    }
}
