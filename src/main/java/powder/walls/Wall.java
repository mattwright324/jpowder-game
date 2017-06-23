package powder.walls;

import java.awt.Color;

import powder.Item;

public class Wall extends Item {
	
	// Pass-through flags.
	public boolean air = false;
	public boolean parts = false;
	/*public boolean powders = false;
	public boolean liquids = false;
	public boolean gasses = false;
	public boolean solids = false;*/
	
	public boolean conduct = false;
	
	public boolean remove = false;
	
	public Wall(String n, Color c) {
		name = n;
		color = c;
	}
	
	public Wall(String n, String d, Color c) {
		name = n;
		description = d;
		color = c;
	}
}
