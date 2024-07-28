package io.mattw.jpowder;

import io.mattw.jpowder.elements.Elements;
import io.mattw.jpowder.particles.Particle;

import java.awt.*;
import java.util.Arrays;

public class Grid {

    private static final int TEMP = 0;
    private static final int TYPE = 1;
    private static final int CTYPE = 2;
    public static final Cell[][] pgrid = new Cell[Display.width][Display.height]; // Particle Grid
    public static final BigCell[][] agrid = new BigCell[Display.width / 4][Display.height / 4]; // Air Grid (Walls, Gravity .. )

    public static Cell cell(int x, int y) {
        if (x > Display.width - 1) {
            x = Display.width - 1;
        }
        if (y > Display.height - 1) {
            y = Display.height - 1;
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return pgrid[x][y];
    }

    public static BigCell bigcell(int x, int y) {
        if (x > Display.width / 4) {
            x = Display.width / 4;
        }
        if (y > Display.height / 4) {
            y = Display.height / 4;
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return agrid[x][y];
    }

    public static void newGame() {
        Game.paused = true;
        for (int w = 0; w < Display.width; w++) {
            for (int h = 0; h < Display.height; h++) {
                cell(w, h).reset();
            }
        }
        for (int w = 0; w < Display.width / 4; w++) {
            for (int h = 0; h < Display.height / 4; h++) {
                bigcell(w, h).reset();
            }
        }
        Game.paused = false;
    }

    /**
     * Returns if a coordinate is within the particle grid or a distance from the edge within.
     * offset=4 for one-layer wall to surround remaining cells
     */
    public static boolean valid(int x, int y, int offset) {
        return !(x < offset || y < offset || x >= Display.width - offset || y >= Display.height - offset);
    }

    public static boolean valid_big(int x, int y, int offset) { // Air Grid
        return !(x < offset || y < offset || x >= Display.width / 4 - offset || y >= Display.height / 4 - offset);
    }

    /**
     * Returns uppermost particle in stack.
     */
    public static Particle getStackTop(int x, int y) {
        if (!valid(x, y, 0) && cell(x, y).empty()) {
            return null;
        }
        for (int i = 0; i < cell(x, y).getStack().length; i++) {
            if (cell(x, y).getStack()[i] != null) {
                return cell(x, y).getStack()[i];
            }
        }
        if (cell(x, y).getStack().length == 0) {
            return null;
        }
        return cell(x, y).getStack()[0];
    }

    public static Particle[] getStack(int x, int y) {
        if (!valid(x, y, 0) && cell(x, y).empty()) {
            return null;
        }
        return cell(x, y).getStack();
    }

    public static void remStackTop(int x, int y) {
        if (!valid(x, y, 0) && cell(x, y).empty()) {
            return;
        }
        for (int i = 0; i < cell(x, y).getStack().length; i++) {
            if (cell(x, y).getStack()[i] != null) {
                cell(x, y).getStack()[i] = null;
                return;
            }
        }
    }

    public static void remStack(int x, int y) {
        if (!valid(x, y, 0) && cell(x, y).empty()) {
            return;
        }
        cell(x, y).reset();
    }

    public static void setStack(int x, int y, Particle p) {
        remStack(x, y);
        cell(x, y).add(p);
    }

    public static boolean empty(int x, int y) {
        return cell(x, y).empty();
    }

    /**
     * Returns particles surrounding a cell.
     */
    public static Particle[] getSurrounding(int x, int y) {
        Particle[] tmp = new Particle[8];
        int p = 0;
        for (int w = 0; w < 3; w++) {
            for (int h = 0; h < 3; h++) {
                if (!(w - 1 == 0 && h - 1 == 0)) {
                    tmp[p++] = getStackTop(x + (w - 1), x + (h - 1));
                }
            }
        }
        return tmp;
    }

    /**
     * Change values for all particles. Could be used as set command in a console.
     *
     * @param method Method 	TYPE, TEMP, CTYPE
     * @param id     Particle "all"/ID/name
     * @param val    Value	double/int, "all"/ID/name
     */
    public static String set(int method, int id, double val) { // set(TYPE, 12, 14) set(TEMP, 12, 90000) set(CTYPE, 22, 12)
        if (method == TYPE && !Elements.exists((int) val)) {
            return "Element does not exist.";
        }
        int changed = 0;
        if (Elements.exists(id)) {
            for (int w = 0; w < Display.width; w++) {
                for (int h = 0; h < Display.height; h++) {
                    Cell cell = cell(w, h);
                    if (!cell.empty()) {
                        for (int pos = 0; pos < cell.getStack().length; pos++) {
                            if (cell.getStack()[pos] != null) {
                                changed++;
                                switch (method) {
                                    case (TEMP):
                                        cell.getStack()[pos].setCelcius(val);
                                        break;
                                    case (TYPE):
                                        if (val == 0) {
                                            cell.getStack()[pos] = null;
                                        } else {
                                            cell.getStack()[pos].morph(Elements.get(id), Particle.MORPH_FULL, false);
                                        }
                                        break;
                                    case (CTYPE):
                                        cell.getStack()[pos].setCtype((int) val);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return changed + " altered";
    }

    public static String set(int m, String name, double val) { // set(TYPE, "watr", 0)
        return set(m, Elements.getID(name), val);
    }

    public static String set(int m, String name, String val) { // set(TYPE, "watr", "none")
        return set(m, Elements.getID(name), Elements.getID(val));
    }

    /**
     * Bresenham's Line Algorithm
     * Used to fill spacing between mouse drags.
     */
    public static Point[] line(Point a, Point b) {
        Point[] pts = new Point[0];
        int dx = Math.abs(b.x - a.x);
        int dy = Math.abs(b.y - a.y);

        int sx = (a.x < b.x) ? 1 : -1;
        int sy = (a.y < b.y) ? 1 : -1;

        int err = dx - dy;

        while (a.x != b.x || a.y != b.y) {
            int e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                a.x = a.x + sx;
            }
            if (e2 < dx) {
                err = err + dx;
                a.y = a.y + sy;
            }
            pts = Arrays.copyOf(pts, pts.length + 1);
            pts[pts.length - 1] = new Point(a.x, a.y);
        }
        return pts;
    }
}
