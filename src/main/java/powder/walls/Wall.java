package main.java.powder.walls;

import java.awt.Color;

public class Wall {
	
	public Wall(String n, Color c) {
		name = n;
		color = c;
	}
	
	public Wall(String n, String d, Color c) {
		name = n;
		description = d;
		color = c;
	}
	
	public String name = "Wall";
	public String description = "Blocks everything.";
	
	public Color color = Color.GRAY;
	
	// Pass-through flags.
	public boolean air = false;
	public boolean powders = false;
	public boolean liquids = false;
	public boolean gasses = false;
	public boolean solids = false;
	
	public boolean conduct = false;
	
	public boolean remove = false;
}
