package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.MainWindow;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.awt.*;
import java.util.Random;

@ToString
@Log4j2
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

        MainWindow.window.getGamePanel()
                .getGameUpdateThread()
                .getPartsToAdd()
                .add(this);
    }

    public void init(Element e, int x, int y) {
        this.el = e;
        this.x = x;
        this.y = y;
        this.life = el.getLife();
        this.celcius = el.getCelcius();
        this.deco = null;
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

    public Color getColor(ViewType view) {
        switch (view) {
            case TEMP:
                return getTempColor();
            case LIFE:
                return getLifeGradient();
            default:
                return deco != null ? deco : el.getColor();
        }
    }

    public Color getTempColor() {
        var w = MainWindow.HEAT_COLOR_STRIP.getWidth();
        var x = (int) (w * (celcius + Math.abs(ElementType.MIN_TEMP)) / (Math.abs(ElementType.MAX_TEMP) + Math.abs(ElementType.MIN_TEMP)));
        if (w <= x) {
            x = w - 1;
        }
        if (x < 0) {
            x = 0;
        }
        var color = MainWindow.HEAT_COLOR_STRIP.getRGB(x, 0);
        var red = (color & 0x00ff0000) >> 16;
        var green = (color & 0x0000ff00) >> 8;
        var blue = color & 0x000000ff;
        return new Color(red, green, blue);
    }

    public Color getLifeGradient() {
        var c = (int) (life % 200 + 55);
        return new Color(c, c, c);
    }

    public void addSandEffect() {
        var color = el.getColor();
        var red = (color.getRed() + (random.nextInt(19) - 10));
        var green = (color.getGreen() + (random.nextInt(19) - 10));
        var blue = (color.getBlue() + (random.nextInt(19) - 10));
        setDeco(new Color(Math.abs(red) % 256, Math.abs(green) % 256, Math.abs(blue) % 256, color.getAlpha()));
    }

    public boolean remove() {
        return remove || (toWall() != null);
    }

    public void update(String updateId) {
        this.lastUpdateId = updateId;

        var wall = toWall();
        if (wall != null && !wall.isAllowParts()) {
            log.debug("in wall");
        }
        if (wall != null && wall.isRemoveParts()) {
            remove = true;
        }

        if (remove() || wall != null && !wall.isAllowParts()) {
            Grid.cell(x, y).removeParticle(this);
            return;
        }

        if (el.getBehaviour() != null) {
            el.getBehaviour().update(this, updateId);
        }

        if (el.getMovement() != null) {
            el.getMovement().move(this);
        }

        for (int w = -1; w < 2; w++) {
            for (int h = -1; h < 2; h++) {
                if (Grid.validCell(x + w, y + h, 0) && !Grid.cell(x + w, y + h).isStackEmpty() && !(w == 0 && h == 0)) {
                    Particle p = Grid.getStackTop(x + w, y + h);
                    if (p == null) {
                        continue;
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
                    case DIE:
                        if (el.getBehaviour() != null) {
                            el.getBehaviour().destruct(this);
                        }
                        setRemove(true);
                        break;
                    case MORPH_CTYPE:
                        morph(ElementType.get(ctype), MORPH_KEEP_TEMP, true, "Particle life decay ctype");
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
                    case DIE:
                        if (el.getBehaviour() != null) {
                            el.getBehaviour().destruct(this);
                        }
                        setRemove(true);
                        break;
                    case MORPH_CTYPE:
                        morph(ElementType.get(ctype), MORPH_KEEP_TEMP, true, "Particle tmp decay ctype");
                        break;
                }
            }
        }
        if (!Grid.validCell(x, y, 4)) {
            setRemove(true);
        }
        if (remove()) {
            Grid.cell(x, y).removeParticle(this);
        }
    }

    public boolean tryMove(int nx, int ny) {
        if (x == nx && y == ny) {
            return false;
        }
        if (!Grid.validCell(nx, ny, 0)) {
            this.setRemove(true);
            return false;
        }
        var wall = toWall();
        if (wall != null && !wall.isAllowParts()) {
            return false;
        }
        var fromCell = Grid.cell(x, y);
        var toCell = Grid.cell(nx, ny);
        if (toCell.hasElement(ElementType.VOID)) {
            fromCell.removeParticle(this);
            this.setRemove(true);
            return false;
        }
        var addable = toCell.canMoveHere(this);
        if (addable) {
            fromCell.removeParticle(this);
            toCell.moveHere(this);
            return true;
        }
        var displaceable = toCell.canDisplace(this);
        if (!toCell.isStackEmpty() && displaceable) {
            var swapPart = toCell.getParts().get(0);
            fromCell.removeParticle(this);
            toCell.removeParticle(swapPart);
            toCell.moveHere(this);
            fromCell.moveHere(swapPart);
            return true;
        }
        return false;
    }

    public void morph(Element e, int type, boolean makectype, String msg) {
        // log.trace("morph({}, {}, {}) {}", e, type, makectype, msg);
        morph(e, type, makectype);
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
