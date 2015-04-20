package main.java.powder;

import java.awt.Color;

import main.java.powder.walls.Wall;

public class BigCell {
	
	public int x, y;
	public Wall wall;
	public double pressure = 0;
	
	public Color getColor() {
		if(wall!=null) return wall.color;
		int c = (int) (255 * Math.abs(pressure) / Game.MAX_AIR);
		if(pressure < 0)
			return new Color(0, 0, c);
		else
			return new Color(c, 0, 0);
	}
	
	public BigCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int screen_x() {
        return x * Display.scale * 4;
    }

    public int screen_y() {
        return y * Display.scale * 4;
    }
}
