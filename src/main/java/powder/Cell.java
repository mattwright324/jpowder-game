package main.java.powder;

import main.java.powder.particles.Particle;

public class Cell {
    public int x, y;
    public Particle part;
    
    // TODO Stacked Particles ?
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int screen_x() {
        return x * Display.img_scale;
    }

    public int screen_y() {
        return y * Display.img_scale;
    }
}
