package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;
import lombok.extern.log4j.Log4j2;

import java.util.ConcurrentModificationException;

@Log4j2
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
        if (!validCell(x, y, 0)) {
            return null;
        }
        var cell = cell(x, y);

        try {
            var parts = cell.getParts();
            synchronized (parts) {
                var it = parts.iterator();
                if (it.hasNext()) {
                    return it.next();
                }
            }
        } catch (ConcurrentModificationException e) {
            log.warn(e);
        }
        return null;
    }

    public static void remStackTop(int x, int y) {
        if (!validCell(x, y, 0)) {
            return;
        }
        var cell = cell(x, y);
        if (cell.isStackEmpty()) {
            return;
        }

        cell.getParts().get(0).setRemove(true);
        cell.getParts().remove(0);
    }

    public static void remStack(int x, int y) {
        if (!validCell(x, y, 0)) {
            return;
        }
        var cell = cell(x, y);
        if (cell.isStackEmpty()) {
            return;
        }
        cell.reset();
    }

    public static void setStack(int x, int y, Particle p) {
        remStack(x, y);
        cell(x, y).moveHere(p);
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
