package main.java.powder;

import main.java.powder.particles.Particle;

public class Cell {
    public int x, y;
    // Can have up to 9 particles in one pixel. This increases the density of the simulation from 234992 to 2114928 particles.
    public Particle[] part;
    
    public void reset() {
    	part = null;
    }
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void cascadeUpdateParticlePositions() {
        if (this.part == null) return;
        for(int i = 0; i < 9; i++) {
            if (this.part[i] == null) continue;
            this.part[i].x = this.x;
            this.part[i].y = this.y;
        }
    }

    public int screen_x() {
        return x * Display.scale;
    }

    public int screen_y() {
        return y * Display.scale;
    }
}
