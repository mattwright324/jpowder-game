package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;

public class Grid {

    private static final int TEMP = 0;
    private static final int TYPE = 1;
    private static final int CTYPE = 2;
    public static final Cell[][] PART_GRID = new Cell[GamePanel.WIDTH][GamePanel.HEIGHT]; // Particle Grid
    public static final BigCell[][] BIG_GRID = new BigCell[GamePanel.WIDTH / 4][GamePanel.HEIGHT / 4]; // Air Grid (Walls, Gravity .. )

    public static Cell cell(int x, int y) {
        if (x > GamePanel.WIDTH - 1) {
            x = GamePanel.WIDTH - 1;
        }
        if (y > GamePanel.HEIGHT - 1) {
            y = GamePanel.HEIGHT - 1;
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return PART_GRID[x][y];
    }

    public static BigCell bigcell(int x, int y) {
        if (x > GamePanel.WIDTH / 4) {
            x = GamePanel.WIDTH / 4;
        }
        if (y > GamePanel.HEIGHT / 4) {
            y = GamePanel.HEIGHT / 4;
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        return BIG_GRID[x][y];
    }

    public static void newGame() {
        GameThread.paused = true;
        for (int w = 0; w < GamePanel.WIDTH; w++) {
            for (int h = 0; h < GamePanel.HEIGHT; h++) {
                cell(w, h).reset();
            }
        }
        for (int w = 0; w < GamePanel.WIDTH / 4; w++) {
            for (int h = 0; h < GamePanel.HEIGHT / 4; h++) {
                bigcell(w, h).reset();
            }
        }
        GameThread.paused = false;
    }

    /**
     * Returns if a coordinate is within the particle grid or a distance from the edge within.
     * offset=4 for one-layer wall to surround remaining cells
     */
    public static boolean validCell(int x, int y, int offset) {
        return !(x < offset || y < offset || x >= GamePanel.WIDTH - offset || y >= GamePanel.HEIGHT - offset);
    }

    public static boolean validBigCell(int x, int y, int offset) { // Air Grid
        return !(x < offset || y < offset || x >= GamePanel.WIDTH / 4 - offset || y >= GamePanel.HEIGHT / 4 - offset);
    }

    /**
     * Returns uppermost particle in stack.
     */
    public static Particle getStackTop(int x, int y) {
        if (!validCell(x, y, 0) && cell(x, y).empty()) {
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
        if (!validCell(x, y, 0) && cell(x, y).empty()) {
            return null;
        }
        return cell(x, y).getStack();
    }

    public static void remStackTop(int x, int y) {
        if (!validCell(x, y, 0) && cell(x, y).empty()) {
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
        if (!validCell(x, y, 0) && cell(x, y).empty()) {
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
        if (method == TYPE && !ElementType.exists((int) val)) {
            return "Element does not exist.";
        }
        int changed = 0;
        if (ElementType.exists(id)) {
            for (int w = 0; w < GamePanel.WIDTH; w++) {
                for (int h = 0; h < GamePanel.HEIGHT; h++) {
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
                                            cell.getStack()[pos].morph(ElementType.get(id), Particle.MORPH_FULL, false);
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
        return set(m, ElementType.getID(name), val);
    }

    public static String set(int m, String name, String val) { // set(TYPE, "watr", "none")
        return set(m, ElementType.getID(name), ElementType.getID(val));
    }

}
