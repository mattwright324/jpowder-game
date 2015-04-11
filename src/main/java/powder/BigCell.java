package main.java.powder;

import main.java.powder.walls.Wall;

public class BigCell {
	
	public int x, y;
	public Wall wall;
	public double pressure = 0;
	
	public BigCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int screen_x() {
        return x * Display.img_scale * 4;
    }

    public int screen_y() {
        return y * Display.img_scale * 4;
    }
}
