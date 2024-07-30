package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;
import io.mattw.jpowder.ui.MainWindow;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Random;

@Getter
@Setter
public class Particle {

    public static final Random random = new Random();

    public static final int MORPH_FULL = 0;
    public static final int MORPH_KEEP_TEMP = 1;
    public static final int MORPH_EL_ONLY = 2;

    private int x;
    private int y;
    private int pos = 0;
    private Element el;
    private long update = 50;
    private long lastUpdate = System.currentTimeMillis();

    private int ctype = 0;
    private int tmp = 0;
    private Color deco;
    private double vx = 0;
    private double vy = 0;
    private long life = 0;
    private double celcius = 0.0;
    private boolean remove = false;
    private String lastUpdateId;

    public Particle(Element e, int x, int y) {
        init(e, x, y);
    }

    public void init(Element e, int x, int y) {
        el = e;
        this.x = x;
        this.y = y;
        this.life = el.getLife();
        this.celcius = el.getCelcius();
        // setRemove(el.remove);
        deco = null;
        if (el.isSandEffect()) {
            addSandEffect();
        }
        if (el.getBehaviour() != null) {
            el.getBehaviour().init(this);
        }
    }

    public Wall toWall() {
        return Grid.bigcell(x / 4, y / 4).getWall();
    }

    public boolean display() {
        return !remove() || el.isDisplay();
    }

    public boolean burn() {
        return Math.random() < el.getFlammibility();
    }

    public double temp() {
        return Math.round(celcius * 10000.0) / 10000.0;
    }

    public Color getColor() { // EW
        switch (GamePanel.view) {
            case (1):
                return getTempColor();
            case (2):
                return getLifeGradient();
            default:
                return deco != null ? deco : el.getColor();
        }
    }

    public Color getTempColor() { // Colorized temperature with no affect on performance!
        int w = MainWindow.heatColorStrip.getWidth();
        int x = (int) (w * (celcius + Math.abs(ElementType.MIN_TEMP)) / (Math.abs(ElementType.MAX_TEMP) + Math.abs(ElementType.MIN_TEMP)));
        if (w <= x) {
            x = w - 1;
        }
        if (x < 0) {
            x = 0;
        }
        int color = MainWindow.heatColorStrip.getRGB(x, 0);
        int red = (color & 0x00ff0000) >> 16;
        int green = (color & 0x0000ff00) >> 8;
        int blue = color & 0x000000ff;
        return new Color(red, green, blue);
    }

    public Color getLifeGradient() {
        int c = (int) (life % 200 + 55);
        return new Color(c, c, c);
    }

    public void addSandEffect() {
        Color color = el.getColor();
        int red = (color.getRed() + (random.nextInt(19) - 10));
        int green = (color.getGreen() + (random.nextInt(19) - 10));
        int blue = (color.getBlue() + (random.nextInt(19) - 10));
        setDeco(new Color(Math.abs(red) % 256, Math.abs(green) % 256, Math.abs(blue) % 256, color.getAlpha()));
    }

    public boolean remove() {
        return remove || (toWall() != null);
    }

    public boolean ready() {
        return System.currentTimeMillis() - lastUpdate > update;
    }

    public void update() {
        if (ready()) {
            if (el.getBehaviour() != null) {
                el.getBehaviour().update(this);
            }
            if (el.getMovement() != null) {
                el.getMovement().move(this);
            }


            for (int w = -1; w < 2; w++) {
                for (int h = -1; h < 2; h++) {
                    if (Grid.validCell(x + w, y + h, 0) && !Grid.cell(x + w, y + h).empty() && !(w == 0 && h == 0)) {
                        Particle p = Grid.getStackTop(x + w, y + h);
                        if (p == null) {
                            continue; // What? Why the hell is this NullPointering?!?!
                        }
                        double diff = (celcius - p.celcius);
                        double trans = p.el.getHeatTransfer();
                        p.celcius += (diff * trans);
                        celcius = celcius - (diff * trans);
                        if (celcius < ElementType.MIN_TEMP) {
                            celcius = ElementType.MIN_TEMP;
                        }
                        if (celcius > ElementType.MAX_TEMP) {
                            celcius = ElementType.MAX_TEMP;
                        }
                    }
                }
            }

            for (Conversion c : el.getConvs()) {
                if (c != null && c.shouldConvert(this)) {
                    c.doConversion(this);
                }
            }

            if (el.isLifeDecay()) {
                if (life > 0) {
                    life--;
                }
                if (life - 1 == 0) {
                    switch (el.getLifeDecayMode()) {
                        case (ElementType.DECAY_DIE):
                            if (el.getBehaviour() != null) {
                                el.getBehaviour().destruct(this);
                            }
                            setRemove(true);
                            break;
                        case (ElementType.DECAY_CTYPE):
                            morph(ElementType.get(ctype), MORPH_KEEP_TEMP, true);
                            break;
                    }
                }
            }
            if (el.isTmpDecay()) {
                if (tmp > 0) {
                    tmp--;
                }
                if (tmp - 1 == 0) {
                    switch (el.getTmpDecayMode()) {
                        case (ElementType.DECAY_DIE):
                            if (el.getBehaviour() != null) {
                                el.getBehaviour().destruct(this);
                            }
                            setRemove(true);
                            break;
                        case (ElementType.DECAY_CTYPE):
                            morph(ElementType.get(ctype), MORPH_KEEP_TEMP, true);
                            break;
                    }
                }
            }
            if (!Grid.validCell(x, y, 4)) {
                setRemove(true);
            }
            lastUpdate = System.currentTimeMillis();
        }
    }

    public void tryMove(int nx, int ny) {
        if (Grid.validCell(nx, ny, 0)) {
            Cell cell = Grid.cell(x, y);
            Cell cell2 = Grid.cell(nx, ny);
            if (cell2.contains(ElementType.VOID)) {
                cell.rem(pos);
                return;
            }
            Particle o;
            if (!(toWall() != null && !toWall().isParts()) && (cell2.addable(this) || cell2.displaceable(this))) {
                cell.rem(pos);
                for (int i = 0; i < cell2.getStack().length; i++) {
                    if ((o = cell2.part(i)) != null) {
                        cell.add(o);
                        cell2.rem(i);
                    }
                }
                cell2.add(this);
            }
        }
    }

    public void morph(Element e, int type, boolean makectype) {
        int id = el.getId();
        switch (type) {
            case (MORPH_KEEP_TEMP):
                double temp = celcius;
                init(e, x, y);
                celcius = temp;
                break;
            case (MORPH_EL_ONLY):
                el = e;
                break;
            case (MORPH_FULL):
            default:
                init(e, x, y);
                break;
        }
        if (makectype) {
            ctype = id;
        }
    }

}
