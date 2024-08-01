package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;

public class Grid {

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
        for (int w = 0; w < GamePanel.WIDTH; w++) {
            for (int h = 0; h < GamePanel.HEIGHT; h++) {
                PART_GRID[w][h] = new Cell(w, h);
            }
        }
        for (int w = 0; w < GamePanel.WIDTH / 4; w++) {
            for (int h = 0; h < GamePanel.HEIGHT / 4; h++) {
                BIG_GRID[w][h] = new BigCell(w, h);
            }
        }
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

}
