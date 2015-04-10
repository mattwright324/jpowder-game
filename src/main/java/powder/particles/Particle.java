package powder.particles;

import powder.Cells;
import powder.elements.Element;

import java.awt.*;
import java.util.Random;

public class Particle {

    public Random r = new Random();
    public Element el;
    public int x, y;
    public int ctype = 0;
    public Color deco;
    public double vx = 0, vy = 0;
    public long life = 0;
    public double celcius = 0.0;
    public boolean remove = false;
    public long update = 50;
    public long last_update = System.currentTimeMillis();

    public Particle(Element e, int x, int y) {
        el = e;
        this.x = x;
        this.y = y;
        this.life = el.life;
        this.celcius = el.celcius;
        setRemove(el.remove);
        if (el.sandEffect) addSandEffect();
        if (el.behaviour != null) el.behaviour.init(this);
    }

    public Particle(Element e, int x, int y, long life, double celcius) {
        el = e;
        this.x = x;
        this.y = y;
        this.life = life;
        this.celcius = celcius;
        setRemove(el.remove);
        if (el.sandEffect) addSandEffect();
        if (el.behaviour != null) el.behaviour.init(this);
    }

    public boolean burn() {
        return Math.random() < el.flammibility;
    }

    public boolean heavierThan(Particle p) {
        return el.weight > p.el.weight;
    }

    public boolean lighterThan(Particle p) {
        return el.weight < p.el.weight;
    }

    public Color getColor() {
        return deco != null ? deco : el.getColor();
    }

    public void setDeco(Color c) {
        deco = c;
    }

    public void addSandEffect() {
        Color color = el.getColor();
        int red = (color.getRed() + (r.nextInt(19) - 10));
        int green = (color.getGreen() + (r.nextInt(19) - 10));
        int blue = (color.getBlue() + (r.nextInt(19) - 10));
        setDeco(new Color(Math.abs(red) % 256, Math.abs(green) % 256, Math.abs(blue) % 256, color.getAlpha()));
    }

    public void setRemove(boolean b) {
        remove = b;
    }

    public boolean remove() {
        return remove;
    }

    public boolean ready() {
        return System.currentTimeMillis() - last_update > update;
    }

    public void update() {
        if (ready()) {
            if (el.behaviour != null)
                el.behaviour.update(this);
            if (el.movement != null)
                el.movement.move(this);

            if (life > 0) life--;
            if (life - 1 == 0) {
                if (el.life_dmode == 1) setRemove(true);
                if (el.life_dmode == 2) Cells.setParticleAt(x, y, new Particle(Element.el_map.get(ctype), x, y), true);
            }
            if (!Cells.validGame(x, y)) setRemove(true);
            last_update = System.currentTimeMillis();
        }
    }

    public void tryMove(int nx, int ny) {
        Particle o = Cells.getParticleAt(nx, ny);
        if (o != null) {
            if (heavierThan(o)) Cells.swap(x, y, nx, ny);
        } else {
            Cells.moveTo(x, y, nx, ny);
        }
    }
}
