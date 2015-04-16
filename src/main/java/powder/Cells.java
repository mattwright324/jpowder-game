package main.java.powder;

import main.java.powder.elements.Element;
import main.java.powder.particles.Particle;

public class Cells {
	
	// TODO Wall & Air Grid
	
	static BigCell[][] cellsb = new BigCell[Display.width/4][Display.height/4];
	// Final bad
    static Cell[][] cells = new Cell[Display.width][Display.height];
    
    public static boolean valid(int x, int y) {
        return !(x < 0 || y < 0 || x >= Display.width || y >= Display.height);
    }
    
    public static boolean validGame(int x, int y) {
        return !(x < 4 || y < 4 || x >= Display.width - 4 || y >= Display.height - 4);
    }
    
    public static Particle getParticleAt(int x, int y) {
        if (!valid(x, y)) return null;
        if (cells[x][y].part == null) return null;
        return cells[x][y].part[0];
    }
    public static Particle getParticleAt(int x, int y, int stackIndex) {
        if (!valid(x, y)) return null;
        if (cells[x][y].part == null) return null;
        return cells[x][y].part[stackIndex];
    }
    public static Particle[] getAllParticlesAt(int x, int y) {
        if (!valid(x, y)) return null;
        return cells[x][y].part;
    }
    
    /**
     * Gets the particles that surround the particle at co-ordinates x,y.
     * @param x The x co-ord
     * @param y The y co-ord
     * @return An array of particles, with the top row first, then the second row, then the third row.
     */
    public static Particle[] getSurroundingParticles(int x, int y) {
    	Particle[] tmp = new Particle[8];
    	int p = 0;
    	for(int w=0; w<3; w++)
    		for(int h=0; h<3; h++)
    			if(!(w-1==0 && h-1==0))
    				tmp[p++] = getParticleAt(x+(w-1), x+(h-1));
        return tmp;
    }

    public static void clearScreen() {
        for(int w = 0; w<Display.width; w++) {
        	 for(int h = 0; h<Display.height; h++) {
             	cells[w][h].reset();
             }
        }
    }

    public static boolean deleteParticle(int x, int y) {
        if (!valid(x, y)) return false;
        if (!particleAt(x, y)) return false;
        cells[x][y].part = null; // java.lang.NullPointerException
        return true;
    }

    public static boolean particleAt(int x, int y) {
        return !valid(x, y) || cells[x][y].part != null;
    }
    // I'm not taking responsibility if you forget to sanitise stackpos and end up passing it 12.
    public static boolean particleInStackPos(int x, int y, int stackpos) {
        return !valid(x, y) && (cells[x][y].part != null || cells[x][y].part[stackpos] != null);
    }
    
    public static boolean setParticleAt(int x, int y, Particle p, boolean insert) {
        if (!valid(x, y)) return false;
        if (!insert && particleAt(x, y)) return false;
        if (p != null) {
            p.x = x;
            p.y = y;
        }
        if (cells[x][y].part == null) cells[x][y].part = new Particle[9];
        cells[x][y].part[0] = p;
        return true;
    }

    public static boolean setParticleAtWithStack(int x, int y, Particle p, int stackpos) {
        if (!valid(x, y)) return false;
        if (!particleAt(x, y) && stackpos != 0) return false;
        if (p != null) {
            p.x = x;
            p.y = y;
        }
        cells[x][y].part[stackpos] = p;
        return true;
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
        deleteParticle(x1, y1);
        setParticleAt(x2, y2, a, true);
    }

    /**
     * Swap two particles coordinates.
     */
    /*public static void swap(int x1, int y1, int x2, int y2) {
        if (!valid(x2, y2)) return;
        Particle a = getParticleAt(x1, y1);
        Particle b = getParticleAt(x2, y2);
        setParticleAt(x1, y1, b, true);
        setParticleAt(x2, y2, a, true);
    }*/

    /**
     * Swap two cells rather than the particles.
     */
    public static void swap(int x1, int y1, int x2, int y2) {
        if (!valid(x2, y2)) return;
        Cell a = cells[x1][y1];
        Cell b = cells[x2][y2];
        a.x = x2;
        a.y = y2;
        b.x = x1;
        b.y = y1;
        a.cascadeUpdateParticlePositions();
        cells[x1][y1] = b;
        b.cascadeUpdateParticlePositions();
        cells[x2][y2] = a;
    }
}
