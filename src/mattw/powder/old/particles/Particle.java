package mattw.powder.old.particles;

import mattw.powder.old.Cell;
import mattw.powder.old.Display;
import mattw.powder.old.Grid;
import mattw.powder.old.Window;
import mattw.powder.old.elements.Conversion;
import mattw.powder.old.elements.Element;
import mattw.powder.old.elements.Elements;
import mattw.powder.old.walls.Wall;

import java.awt.*;
import java.util.Random;

public class Particle {
	
	public static final Random r = new Random();
	
	public static final int MORPH_FULL = 0;
    public static final int MORPH_KEEP_TEMP = 1;
    public static final int MORPH_EL_ONLY = 2;
	
    public int x, y, pos=0;
    public Element el;
    public long update = 50;
    public long last_update = System.currentTimeMillis();
    
    public int ctype = 0;
    public int tmp = 0;
    public Color deco;
    public double vx = 0, vy = 0;
    public long life = 0;
    public double celcius = 0.0;
    public boolean remove = false;
    
    /*final long cid = r.nextLong();
    public boolean same(Particle p) {
    	//System.out.println(x+"."+y+","+cid+" =? "+p.x+"."+p.y+","+p.cid);
    	return cid==p.cid;
    }*/
    
    public Particle(Element e, int x, int y) {
    	init(e, x, y);
    }

    public void init(Element e, int x, int y) {
    	el = e;
        this.x = x;
        this.y = y;
        this.life = el.life;
        this.celcius = el.celcius;
        //setRemove(el.remove);
        deco = null;
        if (el.sandEffect) addSandEffect();
        if (el.behaviour != null) el.behaviour.init(this);
    }
    
    public Wall toWall() {
    	return Grid.bigcell(x/4, y/4).wall;
    }
    
    public boolean display() {
    	return !remove() || el.display;
    }
    
    public boolean burn() {
        return Math.random() < el.flammibility;
    }
    
    public boolean warmerThan(Particle p) {
    	return celcius > p.celcius;
    }
    
    public boolean heavierThan(Element e) {
        return el.weight > e.weight;
    }
    
    public boolean heavierThan(Particle p) {
    	return heavierThan(p.el);
    }
    
    public double temp() {
    	 return Math.round(celcius * 10000.0) / 10000.0;
    }
    
    public Color getColor() { // EW
    	switch(Display.view) {
    	case(1):
    		return getTempColor();
    	case(2):
    		return getLifeGradient();
    	default:
    		return deco != null ? deco : el.getColor();
    	}
    }
    
    public Color getTempColor() { // Colorized temperature with no affect on performance!
    	int w = Window.heatColorStrip.getWidth();
		int x = (int) (w * (celcius + Math.abs(Elements.MIN_TEMP)) / (Math.abs(Elements.MAX_TEMP)+ Math.abs(Elements.MIN_TEMP)));
		if(w <= x) x = w-1;
		if(x < 0) x = 0;
		int color = Window.heatColorStrip.getRGB(x, 0);
        int  red = (color & 0x00ff0000) >> 16;
        int  green = (color & 0x0000ff00) >> 8;
        int  blue = color & 0x000000ff;
        return new Color(red, green, blue);
	}
    
    public Color getLifeGradient() {
    	int c = (int) (life % 200 + 55);
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
        return remove || (toWall()!=null);
    }

    public boolean ready() {
        return System.currentTimeMillis() - last_update > update;
    }

    public void update() {
        if(ready()) {
            if (el.behaviour != null)
                el.behaviour.update(this);
            if (el.movement != null)
                el.movement.move(this);
            
            
            for(int w=-1; w<2; w++)
            	for(int h=-1; h<2; h++)
            		if(Grid.valid(x+w, y+h, 0) && !Grid.cell(x+w, y+h).empty() && !(w==0 && h==0)) {
            			Particle p = Grid.getStackTop(x+w, y+h);
                        if (p == null) continue; // What? Why the hell is this NullPointering?!?!
            			double diff = (celcius - p.celcius);
            			double trans = p.el.heatTransfer;
            			p.celcius += (diff * trans);
        				celcius = celcius - (diff * trans);
        				if(celcius < Elements.MIN_TEMP) celcius = Elements.MIN_TEMP;
        				if(celcius > Elements.MAX_TEMP) celcius = Elements.MAX_TEMP;
            		}
            
            for(Conversion c : el.convs) {
        		if(c!=null && c.shouldConvert(this)) c.doConversion(this);
        	}
            
            if(el.life_decay) {
            	if(life>0) life--;
            	if(life-1==0) {
                    switch(el.life_decay_mode) {
                        case (Elements.DECAY_DIE):
                            if (el.behaviour != null) el.behaviour.destruct(this);
                            setRemove(true);
                            break;
                        case (Elements.DECAY_CTYPE):
                            morph(Elements.get(ctype), MORPH_KEEP_TEMP, true);
                            break;
                    }
                }
            }
            if(el.tmp_decay) {
            	if(tmp>0) tmp--;
            	if(tmp-1==0) {
                    switch (el.tmp_decay_mode) {
                        case (Elements.DECAY_DIE):
                            if (el.behaviour != null) el.behaviour.destruct(this);
                            setRemove(true);
                            break;
                        case (Elements.DECAY_CTYPE):
                            morph(Elements.get(ctype), MORPH_KEEP_TEMP, true);
                            break;
                    }
                }
            }
            if (!Grid.valid(x, y, 4)) setRemove(true);
            last_update = System.currentTimeMillis();
        }
    }
    
    public void tryMove(int nx, int ny) { 
    	if(Grid.valid(nx, ny, 0)) {
    		Cell cell = Grid.cell(x, y);
    		Cell cell2 = Grid.cell(nx, ny);
    		if(cell2.contains(Elements.void_)) {
    			cell.rem(pos);
    			return;
    		}
    		Particle o;
    		if(!(toWall()!=null && !toWall().parts) && (cell2.addable(this) || cell2.displaceable(this))) {
    			cell.rem(pos);
    			for(int i=0; i<cell2.stack.length; i++) {
    				if((o=cell2.part(i))!=null) {
    					cell.add(o);
        				cell2.rem(i);
    				}
    			}
    			cell2.add(this);
    		}
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
    	case(MORPH_EL_ONLY):
    		el = e;
    		break;
    	default:
    		init(e, x, y); break;
    	}
    	if(makectype) ctype = id;
    }
    
}
