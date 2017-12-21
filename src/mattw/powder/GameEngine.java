package mattw.powder;

import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.input.MouseEvent;

import java.awt.*;
import java.util.Arrays;

public class GameEngine {
    private long ups = 0, lastUPS = 0;
    private long ms = 0;
    public SimpleLongProperty upsCap = new SimpleLongProperty(JPowder.DEFAULT_UPS_CAP);

    private long parts = 0, lastParts = 0;
    private boolean canUpdate = true;
    private boolean running = true;

    private int width, height;
    private Cell[][] grid;

    public GameEngine(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new Cell[height][width];
        Task<Void> task = new Task<Void>() {
            public Void call() {
                do {
                    if(canUpdate) {
                        update();
                    }
                    if(upsCap.get() > 1000) upsCap.setValue(1000);
                    try { Thread.sleep(upsCap.get() > 0 ? 1000 / upsCap.get() : 10); } catch (Exception ignored) {}
                } while(running);
                return null;
            }
        };
        new Thread(task).start();
    }

    public void drawAtPoint(GameCanvas gc, MouseEvent me, MouseEvent prev, int cursorSize, MouseType type) {
        if(me != null) {
            int y = gc.gridY(me.getY());
            int x = gc.gridX(me.getX());
            if(prev != null) {
                int y2 = gc.gridY(prev.getY());
                int x2 = gc.gridX(prev.getX());
                if(x != x2 && y != y2) {
                    for(Point p : line(new Point(x, y), new Point(x2, y2))) {
                        if(p.y >= 0 && p.x >= 0 && p.x < width && p.y < height) {
                            drawPartsAtPt(p.x, p.y, cursorSize, type);
                        }
                    }
                }
            }
            if(y >= 0 && x >= 0 && x < width && y < height) {
                drawPartsAtPt(x, y, cursorSize, type);
                return;
            }
        }
    }

    private void drawPartsAtPt(int gx, int gy, double cursorSize, MouseType type) {
        int radius = (int) Math.round(cursorSize / 2.0);
        for(int x=gx-radius; x<gx+radius; x++) {
            for(int y=gy-radius; y<gy+radius; y++) {
                if(x >= 0 && y >= 0 && x < width && y < height) {
                    if(type.equals(MouseType.CIRCLE) && Math.sqrt(Math.pow(x-gx,2)+Math.pow(y-gy,2))<=radius) {
                        if(grid[y][x] == null) {
                            grid[y][x] = new Cell(254);
                        }
                    } else if(type.equals(MouseType.SQUARE)) {
                        if(grid[y][x] == null) {
                            grid[y][x] = new Cell(254);
                        }
                    }
                }
            }
        }
    }

    /**
     * https://en.wikipedia.org/wiki/Bresenham%27s_line_algorithm
     * @param a start of line
     * @param b eng of line
     * @return
     */
    public Point[] line(Point a, Point b) {
        Point[] pts = new Point[0];
        int dx = Math.abs(b.x - a.x);
        int dy = Math.abs(b.y - a.y);

        int sx = (a.x < b.x) ? 1 : -1;
        int sy = (a.y < b.y) ? 1 : -1;

        int err = dx - dy;
        while(true) {
            if(a.x == b.x && a.y == b.y) break;
            int e2 = 2 * err;
            if(e2 > -dy) {
                err = err - dy;
                a.x = a.x + sx;
            }
            if(e2 < dx) {
                err = err + dx;
                a.y = a.y + sy;
            }
            pts = Arrays.copyOf(pts, pts.length+1);
            pts[pts.length-1] = new Point(a.x, a.y);
        }
        return pts;
    }

    public int getHeight() { return height; }
    public int getWidth() { return width; }
    public void setUPSCap(long cap) { upsCap.setValue(cap); }
    public long getUPSCap() { return upsCap.get(); }
    public long getLastUPS() { return lastUPS; }
    public long getPartCount() { return lastParts; }
    public void setCanUpdate(boolean b) {
        canUpdate = b;
    }
    public boolean hasQuit() {
        return !running;
    }
    public Cell[][] getGrid() { return grid; }
    public void newGrid() {
        grid = new Cell[height][width];
    }
    public boolean isPaused() { return canUpdate; }
    public void togglePause() { canUpdate = !canUpdate; }

    /**
     * Permanently ends the engine from running.
     */
    public void quit() {
        running = false;
        ups = 0;
        lastUPS = 0;
    }


    private void update() {
        ups++;
        parts = 0;
        long uid = System.nanoTime();
        if(System.currentTimeMillis() - ms > 1000) {
            ms = System.currentTimeMillis();
            lastUPS = ups;
            ups = 0;
        }
        for(int i=0; i<10; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            if(grid[y][x] == null) {
                grid[y][x] = new Cell();
            }
        }
        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                if(grid[y][x] != null && grid[y][x].uid != uid) {
                    parts++;
                    Cell c = grid[y][x];
                    c.update(uid);
                    swap(x, y, (int) Math.round(x + (Math.random()*2 - 1.0)), (int) Math.round(y+ 3 * Math.random()), false);
                }
            }
        }
        lastParts = parts;
    }

    /**
     * Move particles within the grid.
     * @param x1 x1
     * @param y1 y1
     * @param x2 x2
     * @param y2 y2
     * @param looping if the particle goes out of the grid it will appear on the other side
     */
    private void swap(int x1, int y1, int x2, int y2, boolean looping) {
        if(x1 < width && y1 < height && x2 < width && y2 < height && x1 >= 0 && y1 >= 0 && x2 >= 0 && y2 >= 0) {
            if(!grid[y1][x1].equals(grid[y2][x2])) {
                Cell temp = grid[y1][x1];
                grid[y1][x1] = grid[y2][x2];
                grid[y2][x2] = temp;
            }
        } else if(x1 < width && y1 < height && x1 >= 0 && y1 >= 0) {
            grid[y1][x1] = null;
        } else if(x2 < width && y2 < height && x2 >= 0 && y2 >= 0) {
            grid[y2][x2] = null;
        }
    }

    /**
     * TODO
     * @param w
     * @param h
     * @return
     */
    public GameEngine copyStateToNewSize(int w, int h) {
        GameEngine engine = new GameEngine(w, h);
        for(int y=0; y<h; y++) {
            for(int x=0; x<w; x++) {
                if(y < height && x < width) {
                    engine.getGrid()[y][x] = grid[y][x];
                }
            }
        }
        return engine;
    }
}