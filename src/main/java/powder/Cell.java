package main.java.powder;

import java.util.Arrays;

import main.java.powder.elements.Element;
import main.java.powder.particles.Particle;

public class Cell {
	
    public int x, y;
    public Particle[] part;
    
    public void reset() {
    	part = null;
    	stack = new Particle[0];
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
    
    public Particle[] stack = new Particle[1];
    
    public int count() {
    	int s = 0;
    	for(int i=0; i<stack.length; i++)
    		if(stack[i]!=null) s++;
    	return s;
    }
    
    public boolean empty() {
    	for(int i=0; i<stack.length; i++)
    		if(stack[i]!=null) return false;
    	return stack.length == 0;
    }
    
    public boolean addable(Particle p) {
    	return addable(p.el);
    }
    
    public boolean addable(Element e) {
    	for(int i=0; i<stack.length; i++)
    		if(stack[i]!=null && !stack[i].el.stackable) return false;
    	return true;
    }
    
    public Particle part(int pos) {
    	if(empty()) return null;
    	return stack[pos];
    }
    
    public void rem(int pos) {
    	if(stack.length<=pos) return;
    	stack[pos] = null;
    }
    
    public int findPos(Particle p) {
    	for(int i=0; i<count(); i++)
    		if(stack[i]!=null && stack[i].same(p)) return i;
    	return -1;
    }
    
    public void add(Particle p) {
    	if(empty() && stack.length>1) cleanStack();
    	p.x = x;
    	p.y = y;
    	stack = Arrays.copyOf(stack, stack.length+1);
    	stack[stack.length-1] = p;
    	p.pos = stack.length-1;
    }
    
    public void add(Element e) {
    	add(new Particle(e, x, y));
    }
    
    public void update() {
    	Particle p;
    	for(int i=0; i<stack.length; i++) {
    		if((p=stack[i])!=null) {
    			if(p.remove)
    				stack[i] = null;
    			else
    				p.update();
    		}
    	}
    }
    
    /**
     * Moves all particles up and nulls down then cuts off the nulls at the bottom.
     * Check for affect on performance.
     * Should not be used often, preferred on pause.
     */
    public void cleanStack() {
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
    	if(stack.length==0 || stack.length==1 && stack[0]==null) stack = new Particle[1];
    }
}
