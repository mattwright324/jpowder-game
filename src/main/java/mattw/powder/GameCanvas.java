package mattw.powder;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.concurrent.CountDownLatch;

public class GameCanvas extends Canvas {
    private long fps = 0;
    private long lastFPS = 0;
    private long ms = 0;
    public SimpleLongProperty fpsCap = new SimpleLongProperty(CanvasFX.DEFAULT_FPS_CAP);
    private CountDownLatch drawLatch;

    private GameEngine engine;
    private boolean drawCanvas = true;
    private boolean drawGame = true;
    private int parts = 0;

    private MouseEvent mousePos = null, lastMouse = null;
    //private int viewX = 0, viewY = 0;
    public SimpleIntegerProperty widthProperty, heightProperty;
    private int cursorSize = 5;
    private boolean placeEl = false;
    private int gridSize = 0;
    private MouseType type = MouseType.CIRCLE;

    /**
     * Default Game Size 15x15
     */
    public GameCanvas() { this(CanvasFX.DEFAULT_WIDTH, CanvasFX.DEFAULT_HEIGHT); }

    /**
     * Create a custom sandbox size.
     * @param w Engine Width
     * @param h Engine Height
     */
    public GameCanvas(int w, int h) {
        widthProperty = new SimpleIntegerProperty(w);
        heightProperty = new SimpleIntegerProperty(h);
        engine = new GameEngine(w, h);

        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
        Task<Void> task = new Task<Void>() {
            public Void call() {
                do {
                    drawLatch = new CountDownLatch(1);
                    Platform.runLater(() -> draw());
                    try { drawLatch.await(); } catch (Exception ignored) {}
                    if(fpsCap.get() > 1000) fpsCap.setValue(1000);
                    try { Thread.sleep(fpsCap.get() > 0 ? 1000 / fpsCap.get() : 10); } catch (Exception ignored) {}
                } while(drawCanvas);
                return null;
            }
        };
        new Thread(task).start();

        setCursor(Cursor.NONE);
        setOnMousePressed(me -> {
            setMousePoint(me);
            placeEl = true;
        });
        setOnMouseDragged(me -> setMousePoint(me));
        setOnMouseMoved(me -> setMousePoint(me));
        setOnMouseExited(me -> setMousePoint(null));
        setOnMouseReleased(me -> {
            setMousePoint(me);
            placeEl = false;
        });
        setOnScroll(se -> {
            if(se.getDeltaY() > 0) {
                cursorSize++;
            } else if(se.getDeltaY() < 0 && cursorSize > 0) {
                cursorSize--;
            }
        });
    }

    /**
     * Draws the current GameEngine game state.
     */
    private void draw() {
        fps++;
        if(System.currentTimeMillis() - ms > 1000) {
            ms = System.currentTimeMillis();
            lastFPS = fps;
            fps = 0;
        }

        if(placeEl) getEngine().drawAtPoint(this, mousePos, lastMouse, cursorSize, type);
        GraphicsContext gc = getGraphicsContext2D();
        clearCanvas(gc);
        if(drawGame) { drawParticles(gc); }
        if(mousePos != null) { drawMouse(gc); }
        drawGrid(gc);
        drawHUD(gc);
        drawLatch.countDown();
    }

    private void clearCanvas(GraphicsContext gc) {
        //gc.clearRect(0,0,getWidth(),getHeight());
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawGrid(GraphicsContext gc) {
        if(gridSize > 0) {
            gc.setStroke(Color.LIGHTGRAY);
            double xs = getWidth() / gridSize;
            for(int x=0; x<gridSize; x++) {
                gc.strokeLine((x+1)*xs, 0, (x+1)*xs, getHeight());
            }
            double ys = getHeight() / gridSize;
            for(int y=0; y<gridSize; y++) {
                gc.strokeLine(0, (y+1)*ys, getWidth(), (y+1)*ys);
            }
        }
    }

    private void drawMouse(GraphicsContext gc) {
        gc.setFill(new Color(0.8,0.8,0.8,0.5));
        double gw = getWidth() / width();
        double gh = getHeight() / height();
        int x1 = gridX(mousePos.getX());
        int y1 = gridY(mousePos.getY());
        gc.fillRect(x1*gw, y1*gh, gw+0.2, gh+0.2);
        if(type == MouseType.CIRCLE) {
            int lastx = -1, lasty = -1;
            for(double a=0; a<360; a+=0.5) {
                int tx = (int) (cursorSize/2 * Math.cos(a)) + x1;
                int ty = (int) (cursorSize/2 * Math.sin(a)) + y1;
                if(tx != lastx && ty != lasty) {
                    gc.fillRect(tx*gw, ty*gh, gw, gh);
                    lastx = tx;
                    lasty = ty;
                }
            }
        } else if(type == MouseType.SQUARE) {
            double dist = cursorSize / 2;
            double xa = x1-dist, ya = y1-dist, xb = x1+dist, yb = y1+dist;
            gc.fillRect(xa*gw, ya*gh, gw*cursorSize, gh); // top
            gc.fillRect(xa*gw, yb*gh, gw*cursorSize, gh); // bottom
            gc.fillRect(xa*gw, ya*gh, gw, gh*cursorSize); // left
            gc.fillRect(xb*gw, ya*gh, gw, gh*cursorSize); // right
        }
    }

    private void setMousePoint(MouseEvent me) {
        lastMouse = mousePos;
        mousePos = me;
    }

    public void nextMouseShape() {
        type = type.next();
    }
    public int gridX(double screenX) {
        return (int) (screenX * width() / getWidth());
    }
    public int gridY(double screenY) {
        return (int) (screenY * height() / getHeight());
    }
    public double screenX(int gridX) {
        return getWidth() / width() * gridX;
    }
    public double screenY(int gridY) { return getHeight() / height() * gridY; }

    /**
     * TODO Causes high CPU ~30-40 %
     * @param gc
     */
    private void drawParticles(GraphicsContext gc) {
        double cellWidth = getWidth() / width();
        double cellHeight = getHeight() / height();
        for(int h=0; h<height(); h++) {
            for(int w=0; w<width(); w++) {
                Cell cell = engine.getGrid()[h][w];
                if(cell != null) {
                    gc.setFill(cell.getColor());
                    gc.fillRect(w*cellWidth,h*cellHeight,cellWidth+0.2,cellHeight+0.2);
                }
            }
        }
        gc.setEffect(null);
    }

    private void drawHUD(GraphicsContext gc) {
        gc.setFill(Color.CORAL);
        gc.fillText(lastFPS+" FPS  "+getEngine().getLastUPS()+" UPS  "+getEngine().getPartCount()+"  parts", 15, 25);
    }

    public GameEngine getEngine() { return engine; }

    public void newEngine(int w, int h) {
        if(w < 10) w = 10;
        if(h < 10) h = 10;
        if(width() != w && height() != h) {
            widthProperty.setValue(w);
            heightProperty.setValue(h);
            engine.quit();
            engine = engine.copyStateToNewSize(width(), height());
        }
    }

    public int width() { return widthProperty.get(); }
    public int height() { return heightProperty.get(); }
    public int getGameHeight() { return engine.getHeight(); }
    public int getGameWidth() { return engine.getWidth(); }
    public void setDrawCanvas(boolean b) {
        drawCanvas = b;
    }
    public void setDrawGame(boolean b) {
        drawGame = b;
    }
    public void setFPSCap(long cap) { fpsCap.setValue(cap); }
    public boolean isResizable() {return true;}
    public double prefWidth(double height) {return getWidth();}
    public double prefHeight(double width) {return getHeight();}
    public int getGridSize() { return gridSize; }
    public void incrGridSize() { gridSize++; }
    public void setGridSize(int size) { gridSize = size; }
}
