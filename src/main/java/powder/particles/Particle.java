package main.java.powder.particles;

import main.java.powder.Cells;
import main.java.powder.Game;
import main.java.powder.elements.Element;

import java.awt.*;
import java.util.Random;

public class Particle {
	
	public static final int MORPH_FULL = 0;
    public static final int MORPH_KEEP_TEMP = 1;
	
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
    	init(e, x, y);
    }

    public void init(Element e, int x, int y) {
    	el = e;
        this.x = x;
        this.y = y;
        this.life = el.life;
        this.celcius = el.celcius;
        setRemove(el.remove);
        deco = null;
        if (el.sandEffect) addSandEffect();
        if (el.behaviour != null) el.behaviour.init(this);
    }
    
    public boolean burn() {
        return Math.random() < el.flammibility;
    }
    
    public boolean convert() {
    	if(!el.convert) return false;
    	switch(el.conv_sign) {
    	case(Element.CS_GTR):
    		return el.conv_temp < celcius;
    	case(Element.CS_LSS):
    		return el.conv_temp > celcius;
    	case(Element.CS_EQ):
    		return (int) el.conv_temp == (int) celcius;
    	default:
    		return el.conv_temp < celcius;
    	}
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
    	int c = (int) celcius % 200 + 55;
    	return new Color(c, c, c);
    	//return getColorFromDecimal(tempToDecimal(celcius));
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
        				if(celcius < Game.MIN_TEMP) celcius = Game.MIN_TEMP;
        				if(celcius > Game.MAX_TEMP) celcius = Game.MAX_TEMP;
            		}
            
            if(convert()) {
            	if(el.conv_method==Element.CM_TYPE)
            		morph(el.conv, MORPH_KEEP_TEMP, true);
            	else if(el.conv_method==Element.CM_CTYPE)
            		morph(Element.getID(ctype), MORPH_KEEP_TEMP, true);
            }
            
            if (life > 0 && el.life_decay) life--;
            if (life - 1 == 0) {
                // Delete mode
                if (el.life_dmode == 1){
                    setRemove(true);
                    if (el.behaviour != null) el.behaviour.destruct(this);
                }
                // Decay mode
                if (el.life_dmode == 2) morph(Element.getID(ctype), MORPH_KEEP_TEMP, false); //Cells.setParticleAt(x, y, new Particle(Element.el_map.get(ctype), x, y), true);
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
    
    public void morph(Element e, int type, boolean makectype) {
    	int id = el.id;
    	switch(type) {
    	case(MORPH_FULL):
    		init(e, x, y); break;
    	case(MORPH_KEEP_TEMP):
    		double temp = celcius;
    		init(e, x, y); 
    		celcius = temp; break;
    	default:
    		init(e, x, y); break;
    	}
    	if(makectype) ctype = id;
    }
    
    /* Attempt at colorized temperature view but is too slow on fps and doesn't look good.
    static long tempToDecimal(double t) {
		return (long) (16777215 * (t/(Math.abs(Game.MAX_TEMP)+Math.abs(Game.MIN_TEMP))));
	}
	
	static Color getColorFromDecimal(long dec) {
		 int p = 3;
         int i = 0;
         int[] pts = new int[4];
         boolean alpha = dec > power256[3];
         if(dec>power256[4])
        	 dec = (long) (dec-power256[4]);
         while(true) {
                 if(dec<i*power256[p]) {
                	 dec = (long) (dec-((i-1)*power256[p]));
                         pts[3-p]=i-1;
                         p--;
                         i=0;
                 } else i++;
                 if(p<0) break;
         }
         return new Color(pts[3], pts[2], pts[1], alpha ? pts[0] : 255);
	}
	
	public static double[] power256 = {
		   Math.pow(256,0),
		   Math.pow(256,1),
		   Math.pow(256,2),
		   Math.pow(256,3),
		   Math.pow(256,4)
	};*/
}
