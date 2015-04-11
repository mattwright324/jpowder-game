package main.java.powder.walls;

import java.awt.Color;

public class Walls {
	static Wall wall = new Wall("Wall", Color.GRAY);
	static Wall air = new Wall("Air-Only", new Color(128,128,128, 64));
	
	static {
		air.air = true;
	}
}
