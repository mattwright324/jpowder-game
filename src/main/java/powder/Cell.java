package powder;

import java.util.Arrays;

import powder.elements.Element;
import powder.particles.Particle;
import powder.walls.Wall;

public class Cell {
	
    public int x, y;
    public Particle[] part;
    
    public void reset() {
    	part = null;
    	stack = new Particle[1];
    }
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int screen_x() {
        return x * Display.scale;
    }

    public int screen_y() {
        return y * Display.scale;
    }
    
    public void cascadeUpdateParticlePositions() {
        if (this.part == null) return;
        for(int i = 0; i < 9; i++) {
            if (this.part[i] == null) continue;
            this.part[i].x = this.x;
            this.part[i].y = this.y;
        }
    }
    
    // Stacked Particles
    
    public Particle[] stack = new Particle[0];
    
    public Wall toWall() {
    	return Grid.bigcell(x/4, y/4).wall;
    }
    
    public int count() {
    	int s = 0;
		for (Particle aStack : stack) if (aStack != null) s++;
    	return s;
    }
    
    public int null_count() {
    	return stack.length - count();
    }
    
    public boolean empty() {
		for (Particle aStack : stack) if (aStack != null) return false;
    	return true;
    }
    
    public boolean addable(Particle p) {
    	return addable(p.el);
    }
    
    public boolean addable(Element e) {
		for (Particle aStack : stack) if (aStack != null && !aStack.el.stackable) return false;
    	return true;
    }
    
    public boolean displaceable(Particle p) {
    	return displaceable(p.el);
    }
    
    public boolean displaceable(Element e) {
		for (Particle aStack : stack) if (aStack != null && e.heavierThan(aStack.el)) return false;
    	return true;
    }
    
    public boolean contains(Element e) {
		for (Particle aStack : stack) if (aStack != null && aStack.el == e) return true;
    	return false;
    }
    
    public Particle part(int pos) {
    	if(empty()) return null;
    	return stack[pos];
    }
    
    public void rem(int pos) { // Remove
    	if(stack.length<=pos) return;
    	stack[pos] = null;
    }
    
    /*public int findPos(Particle p) {
    	for(int i=0; i<stack.length; i++)
    		if(stack[i]!=null && stack[i].same(p)) return i;
    	return -1;
    }*/
    
    public void add(Particle p) {
    	for(int i=0; i<stack.length; i++)
    		if(stack[i]==null) {
    			p.x = x;
    	    	p.y = y;
    	    	p.pos = i;
    	    	stack[i] = p;
    	    	return;
    		}
    	
    	stack = Arrays.copyOf(stack, stack.length+1);
    	stack[stack.length-1] = p;
    	p.pos = stack.length-1;
    	p.x = x;
    	p.y = y;
    }
    
    public void add(Element e) {
    	add(new Particle(e, x, y));
    }
    
    /**
     * Update the entire stack.
     */
    public void update() {
    	Particle p;
    	for(int i=0; i<stack.length; i++) {
    		if((p=stack[i])!=null) {
    			if(p.remove)
    				stack[i] = null;
    			else
    				p.update();
    		}
    		if(stack.length>0 && empty()) {
        		stack = new Particle[1];
        	}
    	}
    }
    
    /**
     * Moves all particles up and nulls down then cuts off the nulls at the bottom.
     * Check for affect on performance.
     * Should not be used often, preferred on pause.
     */
    public void cleanStack() {
    	if(stack.length>0 && empty()) {
    		stack = new Particle[0];
    	} else {
    		int nulls = 0;
        	for(int i=0; i<stack.length; i++) {
    			if(stack[i]==null) {
    				for(int n=i; n<stack.length; n++) {
    					if(stack[n]!=null) {
    						stack[i] = stack[n];
    						stack[n] = null;
    						nulls++;
    						break;
    					}
    				}
    			}
    		}
        	if(nulls>0)
        		Arrays.copyOf(stack, stack.length-nulls);
    	}
    }
}
