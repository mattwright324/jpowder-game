package io.mattw.jpowder.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ElementType {

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
    public static final Element NONE, SPRK, FILL, ANT;
    public static final Element DUST, STNE, SALT, BCOL, PLUT;
    public static final Element METL, QRTZ, DMND, COAL, INSL, CLNE, ICE, VOID;
    public static final Element WATR, LAVA, LN_2, OIL;
    public static final Element PHOT, RADP;
    public static final Element GAS, WARP, FIRE, PLSM, STM;
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
                    (collide.getEl() != ElementType.RADP || collide.getEl() != ElementType.PHOT);
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
                        o.morph(FIRE, Particle.MORPH_FULL, false);
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
            p.morph(RADP, Particle.MORPH_KEEP_TEMP, true);
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
                if (part != null && part.getEl() == ElementType.PLUT) {
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
                                o.morph(SPRK, Particle.MORPH_FULL, true);
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
                        o.morph(FIRE, Particle.MORPH_KEEP_TEMP, false);
                    }
                }
            }
        }
    };
    public static final ParticleBehaviour pb_clne = new ParticleBehaviour() {
        public void init(Particle p) {
            // Needs to be set ctype on click
            p.setCtype(NONE.getId());
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
                            if (o.getEl() == CLNE && o.getCtype() == 0) {
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
            p.setCtype(STNE.getId());
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
                p.morph(LAVA, Particle.MORPH_KEEP_TEMP, true);
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
            if (Grid.validCell(x, y, 0) && Grid.cell(x, y).addable(FILL)) {
                Grid.cell(x, y).add(FILL);
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
                    Particle dead = new Particle(ElementType.COAL, x, y);
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
        NONE = create(0, "NONE", "Erase", Color.BLACK, WEIGHT_NONE);
        NONE.setRemove(true);

        DUST = create(1, "DUST", "Dust", new Color(162, 168, 9), WEIGHT_POWDER - 1);
        DUST.setMovement(em_powder);
        DUST.setFlammibility(0.6);

        STNE = create(2, "STNE", "Stone", Color.LIGHT_GRAY, WEIGHT_POWDER);
        STNE.setMovement(em_powder);

        SALT = create(3, "SALT", "Salt", new Color(243, 243, 243), WEIGHT_POWDER - 1);
        SALT.setMovement(em_powder);

        BCOL = create(4, "BCOL", "Broken Coal", Color.GRAY.brighter(), WEIGHT_POWDER);
        BCOL.setMovement(em_powder);

        METL = create(5, "METL", "Metal", new Color(112, 122, 255), WEIGHT_SOLID);
        METL.setConducts(true);

        QRTZ = create(6, "QRTZ", "Quartz", new Color(120, 226, 237), WEIGHT_SOLID);
        QRTZ.setTmpDecay(false);
        QRTZ.setParticleBehaviour(pb_qrtz);

        DMND = create(7, "DMND", "Diamond", new Color(32, 248, 228), WEIGHT_DMND);

        COAL = create(8, "COAL", "Coal", Color.GRAY, WEIGHT_SOLID);
        COAL.setFlammibility(0.2);

        INSL = create(9, "INSL", "Insulator", new Color(170, 170, 170), WEIGHT_SOLID);
        INSL.setHeatTransfer(0);
        INSL.setFlammibility(0.1);

        PLUT = create(10, "PLUT", "Plutonium", new Color(0, 179, 21), WEIGHT_POWDER);
        PLUT.setParticleBehaviour(pb_plut);
        PLUT.setMovement(em_powder);
        PLUT.setGlow(true);

        SPRK = create(11, "SPRK", "Spark", Color.YELLOW, WEIGHT_SOLID);
        SPRK.setParticleBehaviour(pb_sprk);
        SPRK.setLifeDecayMode(DECAY_CTYPE);
        SPRK.setLife(4);

        WATR = create(12, "WATR", "Water", Color.BLUE, WEIGHT_LIQUID);
        WATR.setMovement(em_liquid);
        WATR.setConducts(true);
        WATR.setGlow(true);

        LAVA = create(13, "LAVA", "Lava", Color.ORANGE, WEIGHT_LIQUID);
        LAVA.setMovement(em_liquid);
        LAVA.setParticleBehaviour(pb_lava);
        LAVA.setCelcius(1522);
        LAVA.setGlow(true);

        LN_2 = create(14, "LN2", "Liquid Nitrogen", new Color(190, 226, 237), WEIGHT_LIQUID);
        LN_2.setMovement(em_liquid);
        LN_2.setCelcius(MIN_TEMP);
        LN_2.setGlow(true);

        OIL = create(15, "OIL", "Oil", Color.GREEN.darker(), WEIGHT_LIQUID);
        OIL.setMovement(em_liquid);
        OIL.setFlammibility(0.3);
        OIL.setGlow(true);

        PHOT = create(16, "PHOT", "Light", Color.WHITE, WEIGHT_RADIO);
        PHOT.setMovement(em_phot);
        PHOT.setParticleBehaviour(pb_phot);
        PHOT.setStackable(true);
        PHOT.setGlow(true);

        RADP = create(17, "RADP", "Radioactive Particle", Color.MAGENTA, WEIGHT_RADIO);
        RADP.setMovement(em_radioactive);
        RADP.setParticleBehaviour(pb_radio);
        RADP.setCelcius(982);
        RADP.setStackable(true);
        RADP.setGlow(true);

        GAS = create(18, "GAS", "Gas", new Color(208, 180, 208), WEIGHT_GAS);
        GAS.setMovement(em_gas);
        GAS.setFlammibility(0.8);
        GAS.setGlow(true);

        WARP = create(19, "WARP", "Warp", new Color(32, 32, 32), WEIGHT_DMND - 1);
        WARP.setMovement(em_gas);
        WARP.setLife(500);
        WARP.setLifeDecayMode(DECAY_DIE);

        FIRE = create(20, "FIRE", "Fire", Color.RED, WEIGHT_GAS);
        FIRE.setMovement(em_gas);
        FIRE.setParticleBehaviour(pb_fire);
        FIRE.setCelcius(450);
        FIRE.setLifeDecayMode(DECAY_DIE);
        FIRE.setLife(120);
        FIRE.setGlow(true);

        PLSM = create(21, "PLSM", "Plasma", new Color(217, 151, 219), WEIGHT_GAS);
        PLSM.setParticleBehaviour(pb_plsm);
        PLSM.setCelcius(MAX_TEMP);
        PLSM.setLifeDecayMode(DECAY_DIE);
        PLSM.setLife(120);
        PLSM.setGlow(true);

        CLNE = create(22, "CLNE", "Clone", Color.YELLOW, WEIGHT_SOLID);
        CLNE.setParticleBehaviour(pb_clne);

        STM = create(23, "STM", "Steam", new Color(172, 177, 242), WEIGHT_GAS);
        STM.setMovement(em_gas);
        STM.setGlow(true);

        ICE = create(24, "ICE", "Ice", new Color(200, 200, 255), WEIGHT_SOLID);
        ICE.setCelcius(-25);

        FILL = create(25, "FILL", "Filler", Color.LIGHT_GRAY, WEIGHT_DMND);
        FILL.setTmpDecay(false);
        FILL.setHeatTransfer(0.5);
        FILL.setParticleBehaviour(pb_fill);

        ANT = create(26, "ANT", "Langton's Ant", Color.GREEN, WEIGHT_DMND);
        ANT.setTmpDecay(false);
        ANT.setLifeDecay(false);
        ANT.setParticleBehaviour(pb_ant);

        VOID = create(27, "VOID", "Removes interacting particles", new Color(255, 96, 96), WEIGHT_DMND);
    }

    public static final Item[] powder = {DUST, STNE, SALT, BCOL};
    public static final Item[] liquid = {WATR, LAVA, LN_2, OIL};
    public static final Item[] solid = {METL, QRTZ, DMND, COAL, INSL, ICE, CLNE, VOID};
    public static final Item[] gasses = {GAS, FIRE, PLSM, STM};
    public static final Item[] radio = {PHOT, RADP, PLUT, WARP};
    public static final Item[] tools = {NONE, SPRK, FILL, ANT, WallType.NONE, WallType.WALL, WallType.AIR, WallType.WVOID};

    static { // Conversions
        LAVA.addCtypeConvert(CS_LSS, 700);

        SALT.addConvert(LAVA, CS_GTR, 750);
        STNE.addConvert(LAVA, CS_GTR, 850);
        METL.addConvert(LAVA, CS_GTR, 1000);
        //qrtz.addConvert(lava, CS_GTR, 1670);

        OIL.addConvert(GAS, CS_GTR, 150);
        GAS.addConvert(FIRE, CS_GTR, 300);

        ICE.addConvert(WATR, CS_GTR, 0);
        WATR.addConvert(ICE, CS_LSS, 0);
        WATR.addConvert(STM, CS_GTR, 100);
        STM.addConvert(WATR, CS_LSS, 100);

        FIRE.addConvert(PLSM, CS_GTR, 1000);
        PLSM.addConvert(FIRE, CS_LSS, 1000);
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
