package main.java.powder;

import main.java.powder.particles.Particle;

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
