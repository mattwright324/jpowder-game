package main.java.powder.particles;

import main.java.powder.Cells;
import main.java.powder.elements.Element;

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
    
    public boolean convert() {
    	return el.conv!=null && el.convMelt ? el.convAt <= celcius : el.convAt >= celcius;
    }
    
    public boolean warmerThan(Particle p) {
    	return celcius > p.celcius;
    }
    
    public boolean heavierThan(Particle p) {
        return el.weight > p.el.weight;
    }

    public boolean lighterThan(Particle p) {
        return el.weight < p.el.weight;
    }
    
    public double temp() {
    	 return Math.round(celcius * 10000.0) / 10000.0;
    }
    
    public Color getColor() { // EW
        return deco != null ? deco : el.getColor();
    }
    
    public Color getTempColor() {
    	int c = (int) (celcius % 200) + 55;
    	return new Color(c, c, c);
    }
    
    public void setDeco(Color c) { // EW
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
            
            for(int w=-1; w<2; w++)
            	for(int h=-1; h<2; h++)
            		if(Cells.particleAt(x+w, y+h) && !(w==0 && h==0)) {
            			Particle p = Cells.getParticleAt(x+w, y+h);
            			double diff = (celcius - p.celcius);
            			double trans = p.el.heatTransfer;
            			p.celcius += (diff * trans);
        				celcius = celcius - (diff * trans);
        				if(celcius < -273.25) celcius = -273.25;
        				if(celcius > 9725.85) celcius = 9725.85;
            		}
            
            if(convert()) {
            	Particle conv = new Particle(el.conv, x, y);
            	conv.ctype = el.id;
            	conv.celcius = celcius;
            	Cells.setParticleAt(x, y, conv, true);
            }
            
            if (life > 0 && el.life_decay) life--;
            if (life - 1 == 0) {
                // Delete mode
                if (el.life_dmode == 1){
                    setRemove(true);
                    if (el.behaviour != null) el.behaviour.destruct(this);
                }
                // Decay mode
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
