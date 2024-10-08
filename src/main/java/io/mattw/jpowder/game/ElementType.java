package io.mattw.jpowder.game;

import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Log4j2
public class ElementType {

    public static final Map<Integer, String> ID_NAME = new HashMap<>();
    public static final Map<Integer, Element> EL_MAP = new HashMap<>();
    public static final double MIN_TEMP = -273.15;
    public static final double DEFAULT_TEMP = 22.0;
    public static final double MAX_TEMP = 9725.85;
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
    public static final Random random = new Random();

    public static final Element NONE, WARM, COOL;
    public static final Element BTRY, SPRK, FILL, ANT;
    public static final Element DUST, STNE, SALT, BCOL, PLUT, BOMB;
    public static final Element METL, QRTZ, DMND, COAL, INSL, CLNE, ICE, VOID;
    public static final Element WATR, LAVA, LN_2, OIL;
    public static final Element PHOT, RADP;
    public static final Element GAS, WARP, FIRE, PLSM, STM;

    public static final ElementMovement em_phot = p -> {
        int ny = p.getY() + (int) p.getVy();
        int nx = p.getX() + (int) p.getVx();
        p.tryMove(nx, ny);
    };
    public static final ElementMovement em_radioactive = new ElementMovement() {
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
            Particle collide;
            return (collide = Grid.getStackTop((int) x, (int) y)) != null &&
                    (collide.getEl() != ElementType.RADP || collide.getEl() != ElementType.PHOT);
        }
    };
    public static final ElementMovement em_powder = p -> {
        int y = p.getY() + 1;
        int x = p.getX() + (random.nextBoolean() ? -1 : 1);
        if (!p.tryMove(x, y)) {
            p.tryMove(p.getX(), y);
        }
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
                p.setVx(5 * (random.nextInt(3) - 1));
                p.setVy(5 * (random.nextInt(3) - 1));
            }
        }

        public void update(Particle p, String updateId) {
        }
    };
    public static final ParticleBehaviour pb_fire = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(p.getLife() + random.nextInt(50));
            p.setCelcius(p.getCelcius() + random.nextInt(20));
        }

        public void update(Particle p, String updateId) {
            p.tryMove(p.getX(), p.getY() - (random.nextInt(4) - 1));
            for (int w = 0; w < 3; w++) {
                for (int h = 0; h < 3; h++) {
                    Particle o;
                    if ((o = Grid.getStackTop(p.getX() + w - 1, p.getY() + h - 1)) != null && o.doBurn()) {
                        o.morph(FIRE, Particle.MORPH_FULL, false, "FIRE BURN");
                        o.setLastUpdateId(p.getLastUpdateId());
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

        public void update(Particle p, String updateId) {
        }

        public void destruct(Particle p) {
            p.morph(RADP, Particle.MORPH_KEEP_TEMP, true, "PLUT DECAY");
            p.setCelcius(p.getCelcius() + 4500); // Decay should result in a lot of heat + pressure (when added)
        }
    };
    public static final ParticleBehaviour pb_radio = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(random.nextInt(50) + 1);
            p.setCelcius(1982);
            p.setVx(random.nextInt(4) + 2);
            p.setVy(random.nextInt(4) + 2);
        }

        public void update(Particle p, String updateId) {
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

        public void update(Particle p, String updateId) {
            if (p.getLife() == 4) {
                for (int w = 0; w < 5; w++) {
                    for (int h = 0; h < 5; h++) {
                        int x = p.getX() - (w - 2);
                        int y = p.getY() - (h - 2);
                        Particle o;
                        if (Grid.validCell(x, y, 0) && (o = Grid.getStackTop(x, y)) != null) {
                            if (o.getEl().isConducts() && o.getLife() == 0) {
                                o.morph(SPRK, Particle.MORPH_FULL, true, "SPRK SPREAD");
                                o.setLastUpdateId(p.getLastUpdateId());
                            }
                        }
                    }
                }
            } else if (p.getLife() == 0) {
                p.setLife(4);
                if (p.getCelcius() < 600) {
                    p.setCelcius(p.getCelcius() + 50);
                } else {
                    p.setCelcius(p.getCelcius() - 50);
                }
                if (p.getCtype() != 0) {
                    p.morph(get(p.getCtype()), Particle.MORPH_EL_ONLY, false, "SPRK revert");
                }
            }
        }
    };
    public static final ParticleBehaviour pb_btry = new ParticleBehaviour() {
        @Override
        public void init(Particle p) {

        }

        @Override
        public void update(Particle p, String updateId) {
            for (int x = p.getX() - 1; x <= p.getX() + 1; x++) {
                for (int y = p.getY() - 1; y <= p.getY() + 1; y++) {
                    Grid.cell(x, y).placeNewHere(SPRK);
                }
            }
        }
    };
    public static final ParticleBehaviour pb_plsm = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(p.getLife() + random.nextInt(50));
            p.setCelcius(p.getCelcius() + random.nextInt(20));
        }

        public void update(Particle p, String updateId) {
            p.tryMove(p.getX() + (random.nextInt(3) - 1), p.getY() - (random.nextInt(4) - 1));
            for (int w = 0; w < 3; w++) {
                for (int h = 0; h < 3; h++) {
                    Particle o;
                    if ((o = Grid.getStackTop(p.getX() + (w - 1), p.getY() + (h - 1))) != null && o.doBurn()) {
                        o.morph(FIRE, Particle.MORPH_KEEP_TEMP, false, "PLSM cool");
                        o.setLastUpdateId(p.getLastUpdateId());
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

        public void update(Particle p, String updateId) {
            if (p.getCtype() == 0) {
                return; // Won't create anything when newly placed.
            }
            for (int w = -1; w < 2; w++) {
                for (int h = -1; h < 2; h++) {
                    if (Grid.cell(p.getX() + w, p.getY() + h).isStackEmpty()) {
                        int x = p.getX() + w;
                        int y = p.getY() + h;
                        Grid.cell(x, y).placeNewHere(get(p.getCtype()));
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

        public void update(Particle p, String updateId) {
        }
    };
    public static final ParticleBehaviour pb_qrtz = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setTmp(random.nextInt(10));
            p.setDeco(new Color(120, 226, 150 + (int) (105 * (p.getTmp() / 10.0))));
        }

        public void update(Particle p, String updateId) {
            if (p.getCelcius() > 1670 && Math.random() < 0.01) {
                p.morph(LAVA, Particle.MORPH_KEEP_TEMP, true, "QRTZ melt");
            }
        }
    };
    public static final ParticleBehaviour pb_fill = new ParticleBehaviour() {
        public void init(Particle p) {
        }

        public void update(Particle p, String updateId) {
            if (p.getTmp() == 0) {
                p.setTmp(1);
                set(p.getX() + 1, p.getY());
                set(p.getX() - 1, p.getY());
                set(p.getX(), p.getY() + 1);
                set(p.getX(), p.getY() - 1);
            }
        }

        public void set(int x, int y) {
            if (Grid.validCell(x, y, 0) && Grid.cell(x, y).canMoveHere(FILL)) {
                Grid.cell(x, y).placeNewHere(FILL);
            }
        }
    };
    public static final ParticleBehaviour pb_ant = new ParticleBehaviour() {
        public void init(Particle p) {
            p.setLife(180);
            p.setTmp(1);
        }

        public void update(Particle p, String updateId) { // TODO Works ok but doesn't act as Langton's Ant should.
            if (p.getTmp() == 0) {
                p.setDeco(Color.RED);
            }
            if (p.getTmp() == 1 || p.getTmp() == 2) {
                var angle = p.getLife();
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

                // tmp=1 empty/white square move forward right and add element in previous cell
                // tmp=2 populated/black square move forward left and remove element in previous cell

                var nextCellPart = Grid.getStackTop(nx, ny);
                if (nextCellPart != null && p.getEl().heavierThan(nextCellPart.getEl())) {
                    Grid.remStackTop(nx, ny);
                }

                p.tryMove(nx, ny);

                if (p.getTmp() == 1) {
                    Particle dead = new Particle(ElementType.COAL, x, y);
                    dead.setTmp(0);
                    Grid.cell(x, y).moveHere(dead);
                }

                p.setTmp(nextCellPart == null ? 1 : 2);
            }
        }
    };

    public static final ParticleBehaviour pb_bomb = new ParticleBehaviour() {
        private static final int RADIUS = 25;

        @Override
        public void init(Particle p) {

        }

        @Override
        public void update(Particle p, String updateId) {
            if (p.getLife() > 0) {
                p.setRemove(true);
                return;
            }

            int y = p.getY() + 1;
            int x = p.getX() + (random.nextBoolean() ? -1 : 1);
            if (!p.tryMove(x, y)) {
                p.tryMove(p.getX(), y);
            }
            var explode = false;
            for (int x1 = p.getX()-1; x1 <= p.getX()+1; x1++) {
                for (int y1 = p.getY()-1; y1 <= p.getY()+1; y1++) {
                    var top = Grid.getStackTop(x1, y1);
                    if (top != null && !(top.getEl() == BOMB || top.getEl() == CLNE && top.getCtype() == BOMB.getId())) {
                        explode = true;
                    }
                }
            }
            if (explode) {
                Point start = new Point(p.getX() - RADIUS / 2, p.getY() - RADIUS / 2);
                Point end = new Point(start.x + RADIUS, start.y + RADIUS);
                for (int x1 = start.x; x1 <= end.x; x1++) {
                    for (int y1 = start.y; y1 <= end.y; y1++) {
                        if (Math.sqrt(Math.pow(x1 - p.getX(), 2) + Math.pow(y1 - p.getY(), 2)) <= (double) RADIUS / 2) {
                            Grid.remStackTop(x1, y1);
                            var part = Grid.cell(x1, y1).placeNewHere(BOMB);
                            if (part != null) {
                                part.setLife(1);
                            }
                        }
                    }
                }
            }
        }
    };

    static {
        int id = 0;

        NONE = create(id++, "NONE", "Erase", Color.BLACK, WEIGHT_NONE);
        NONE.setRemove(true);

        WARM = create(id++, "WARM", "Add temp to parts", Color.RED, WEIGHT_NONE);
        WARM.setRemove(true);

        COOL = create(id++, "COOL", "Subtract temp to parts", Color.BLUE, WEIGHT_NONE);
        COOL.setRemove(true);

        DUST = create(id++, "DUST", "Dust", new Color(162, 168, 9), WEIGHT_POWDER - 1);
        DUST.setSandEffect(true);
        DUST.setMovement(em_powder);
        DUST.setFlammibility(0.6);

        STNE = create(id++, "STNE", "Stone", Color.LIGHT_GRAY, WEIGHT_POWDER);
        STNE.setSandEffect(true);
        STNE.setMovement(em_powder);

        SALT = create(id++, "SALT", "Salt", new Color(243, 243, 243), WEIGHT_POWDER - 1);
        SALT.setSandEffect(true);
        SALT.setMovement(em_powder);

        BCOL = create(id++, "BCOL", "Broken Coal", Color.GRAY.brighter(), WEIGHT_POWDER);
        BCOL.setSandEffect(true);
        BCOL.setMovement(em_powder);

        METL = create(id++, "METL", "Metal", new Color(112, 122, 255), WEIGHT_SOLID);
        METL.setConducts(true);

        QRTZ = create(id++, "QRTZ", "Quartz", new Color(120, 226, 237), WEIGHT_SOLID);
        QRTZ.setSandEffect(true);
        QRTZ.setTmpDecay(false);
        QRTZ.setParticleBehaviour(pb_qrtz);

        DMND = create(id++, "DMND", "Diamond", new Color(32, 248, 228), WEIGHT_DMND);

        COAL = create(id++, "COAL", "Coal", Color.GRAY, WEIGHT_SOLID);
        COAL.setFlammibility(0.2);

        INSL = create(id++, "INSL", "Insulator", new Color(170, 170, 170), WEIGHT_SOLID);
        INSL.setHeatTransfer(0);
        INSL.setFlammibility(0.015);

        PLUT = create(id++, "PLUT", "Plutonium", new Color(0, 179, 21), WEIGHT_POWDER);
        PLUT.setParticleBehaviour(pb_plut);
        PLUT.setMovement(em_powder);
        PLUT.setLifeDecayMode(DecayMode.DESTRUCT);
        PLUT.setGlow(true);

        BTRY = create(id++, "BTRY", "Battery", new Color(20, 127, 127), WEIGHT_SOLID);
        BTRY.setParticleBehaviour(pb_btry);
        BTRY.setFlammibility(0.015);

        SPRK = create(id++, "SPRK", "Spark", Color.YELLOW, WEIGHT_SOLID);
        SPRK.setParticleBehaviour(pb_sprk);
        SPRK.setLifeDecayMode(DecayMode.NONE);
        SPRK.setLife(4);

        WATR = create(id++, "WATR", "Water", Color.BLUE, WEIGHT_LIQUID);
        WATR.setMovement(em_liquid);
        WATR.setConducts(true);
        WATR.setGlow(true);

        LAVA = create(id++, "LAVA", "Lava", Color.ORANGE, WEIGHT_LIQUID);
        LAVA.setMovement(em_liquid);
        LAVA.setParticleBehaviour(pb_lava);
        LAVA.setCelcius(1522);
        LAVA.setGlow(true);

        LN_2 = create(id++, "LN2", "Liquid Nitrogen", new Color(190, 226, 237), WEIGHT_LIQUID);
        LN_2.setMovement(em_liquid);
        LN_2.setCelcius(MIN_TEMP);
        LN_2.setGlow(true);

        OIL = create(id++, "OIL", "Oil", Color.GREEN.darker(), WEIGHT_LIQUID);
        OIL.setMovement(em_liquid);
        OIL.setFlammibility(0.3);
        OIL.setGlow(true);

        PHOT = create(id++, "PHOT", "Light", Color.WHITE, WEIGHT_RADIO);
        PHOT.setMovement(em_phot);
        PHOT.setParticleBehaviour(pb_phot);
        PHOT.setStackable(true);
        PHOT.setGlow(true);

        RADP = create(id++, "RADP", "Radioactive Particle", Color.MAGENTA, WEIGHT_RADIO);
        RADP.setMovement(em_radioactive);
        RADP.setParticleBehaviour(pb_radio);
        RADP.setCelcius(1982);
        RADP.setStackable(true);
        RADP.setGlow(true);
        RADP.setLifeDecayMode(DecayMode.DIE);

        GAS = create(id++, "GAS", "Gas", new Color(208, 180, 208), WEIGHT_GAS);
        GAS.setMovement(em_gas);
        GAS.setFlammibility(0.8);
        GAS.setGlow(true);

        WARP = create(id++, "WARP", "Warp", new Color(32, 32, 32), WEIGHT_DMND - 1);
        WARP.setMovement(em_gas);
        WARP.setLife(500);
        WARP.setLifeDecayMode(DecayMode.DIE);

        FIRE = create(id++, "FIRE", "Fire", Color.RED, WEIGHT_GAS);
        FIRE.setMovement(em_gas);
        FIRE.setParticleBehaviour(pb_fire);
        FIRE.setCelcius(450);
        FIRE.setLifeDecayMode(DecayMode.DIE);
        FIRE.setLife(120);
        FIRE.setGlow(true);

        PLSM = create(id++, "PLSM", "Plasma", new Color(217, 151, 219), WEIGHT_GAS);
        PLSM.setParticleBehaviour(pb_plsm);
        PLSM.setCelcius(MAX_TEMP);
        PLSM.setLifeDecayMode(DecayMode.DIE);
        PLSM.setLife(120);
        PLSM.setGlow(true);

        CLNE = create(id++, "CLNE", "Clone", Color.YELLOW, WEIGHT_SOLID);
        CLNE.setParticleBehaviour(pb_clne);

        STM = create(id++, "STM", "Steam", new Color(172, 177, 242), WEIGHT_GAS);
        STM.setMovement(em_gas);
        STM.setGlow(true);

        ICE = create(id++, "ICE", "Ice", new Color(200, 200, 255), WEIGHT_SOLID);
        ICE.setCelcius(-25);

        FILL = create(id++, "FILL", "Filler", Color.LIGHT_GRAY, WEIGHT_DMND);
        FILL.setTmpDecay(false);
        FILL.setHeatTransfer(0.5);
        FILL.setParticleBehaviour(pb_fill);
        FILL.setCelcius(9999);
        FILL.setFlammibility(0.1);

        ANT = create(id++, "ANT", "Langton's Ant", Color.GREEN, WEIGHT_DMND);
        ANT.setTmpDecay(false);
        ANT.setLifeDecay(false);
        ANT.setParticleBehaviour(pb_ant);

        VOID = create(id++, "VOID", "Removes interacting particles", new Color(255, 96, 96), WEIGHT_DMND);

        BOMB = create(id++, "BOMB", "Destroys parts on interaction", Color.YELLOW, 0);
        BOMB.setGlow(true);
        BOMB.setCelcius(9999);
        BOMB.setHeatTransfer(1);
        BOMB.setParticleBehaviour(pb_bomb);
    }

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

        BTRY.addConvert(PLSM, CS_GTR, 1200);

        FIRE.addConvert(PLSM, CS_GTR, 1000);
        PLSM.addConvert(FIRE, CS_LSS, 1000);

        DUST.addConvert(FIRE, CS_GTR, 80);
    }

    public static boolean exists(int id) {
        return EL_MAP.containsKey(id);
    }

    public static Element get(int id) {
        return EL_MAP.get(id);
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
