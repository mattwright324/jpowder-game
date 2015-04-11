package main.java.powder;

import main.java.powder.elements.Element;
import main.java.powder.particles.Particle;

import java.util.ArrayList;

public class Cells {
	
	//final static Cell[][] grid = new Cell[Display.width/4][Display.height/4]; // for air/wall grid
    final static Cell[][] cells = new Cell[Display.width][Display.height];
    
    public static boolean valid(int x, int y) {
        return !(x < 0 || y < 0 || x >= Display.width || y >= Display.height);
    }

    public static Particle getParticleAt(int x, int y) {
        if (!valid(x, y)) return null;
        return cells[x][y].part;
    }

    /**
     * Gets the particles that surround the particle at co-ordinates x,y.
     * @param x The x co-ord
     * @param y The y co-ord
     * @return An array of particles, with the top row first, then the second row, then the third row.
     */
    public static ArrayList<Particle> getSurroundingParticles(int x, int y) {
        ArrayList<Particle> tmp = new ArrayList<Particle>();
        for (int xx = x-1; xx <= x+1; x++) {
            for (int yy = y-1; yy <= y+1; x++) {
                tmp.add(getParticleAt(xx, yy));
            }
        }
        return tmp;
    }

    public static boolean particleAt(int x, int y) {
        return !valid(x, y) || cells[x][y].part != null;
    }

    public static boolean validGame(int x, int y) {
        return !(x < 4 || y < 4 || x >= Display.width - 4 || y >= Display.height - 4);
    }

    public static void setParticleAt(int x, int y, Particle p, boolean insert) {
        if (!valid(x, y)) return;
        if (!insert && particleAt(x, y)) return;
        if (p != null) {
            p.x = x;
            p.y = y;
        }
        cells[x][y].part = p;
    }
    
    /**
     * Convert all cells of id n to a specific element.
     * id -1 = All
     */
    public static void setAllOfAs(int id, Element e) {
    	for(int w=0; w<Display.width; w++) {
    		for(int h=0; h<Display.height; h++) {
    			if(particleAt(w,h)) {
    				if(id == -1 || getParticleAt(w, h).el.id == id) {
    					setParticleAt(w, h, new Particle(e, w, h), true);
    				}
    			}
    		}
    	}
    }
    
    /**
     * Move from (x1, y1) to (x2, y2). Original spot set null.
     */
    public static void moveTo(int x1, int y1, int x2, int y2) {
        if (!Cells.valid(x2, y2)) return;
        Particle a = getParticleAt(x1, y1);
        setParticleAt(x1, y1, null, true);
        setParticleAt(x2, y2, a, true);
    }

    /**
     * Swap two particles coordinates.
     */
    public static void swap(int x1, int y1, int x2, int y2) {
        if (!valid(x2, y2)) return;
        Particle a = getParticleAt(x1, y1);
        Particle b = getParticleAt(x2, y2);
        setParticleAt(x1, y1, b, true);
        setParticleAt(x2, y2, a, true);
    }
}
