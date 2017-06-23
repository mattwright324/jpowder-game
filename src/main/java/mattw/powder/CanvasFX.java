package mattw.powder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CanvasFX extends Application {

    public Stage stage;
    public Map<String,String> keyMap = new HashMap<>();

    public GameCanvas game;
    public StackPane pane, ctrlPane;

    public Button apply, close, def;
    public Spinner<Integer> gwidth, gheight, fpsSpin, upsSpin;

    static final int DEFAULT_FPS_CAP = 256;
    static final int DEFAULT_UPS_CAP = 60;
    static final int DEFAULT_WIDTH = 612;
    static final int DEFAULT_HEIGHT = 384;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        this.stage = stage;
        game = new GameCanvas();

        StackPane background = new StackPane();
        background.setStyle("-fx-background-color: black");

        pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(background, game);
        game.heightProperty().bind(pane.heightProperty());
        game.widthProperty().bind(pane.widthProperty());

        Scene scene = new Scene(pane, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        scene.setOnKeyReleased(ke -> {
            KeyCode code = ke.getCode();
            if(code.equals(KeyCode.Q)) {
                game.getEngine().quit();
            } else if(code.equals(KeyCode.N) && ke.isControlDown()) {
                if(!game.getEngine().isPaused())
                    game.getEngine().togglePause();
                game.getEngine().newGrid();
            } else if(code.equals(KeyCode.G)) {
                if(game.getGridSize() >= 12)
                    game.setGridSize(0); else game.incrGridSize();
            } else if(code.equals(KeyCode.BACK_QUOTE)) {
                toggleKeyMapPane();
            } else if(code.equals(KeyCode.SPACE)) {
                game.getEngine().togglePause();
            } else if(code.equals(KeyCode.T)) {
                game.nextMouseShape();
            } else if(code.equals(KeyCode.R) && ke.isControlDown()) {
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                double decoHeight = stage.getHeight() - scene.getHeight();
                double decoWidth = stage.getWidth() - scene.getWidth();
                stage.setHeight(screen.getHeight() >= game.getGameHeight()+decoHeight ? game.getGameHeight()+decoHeight : screen.getHeight());
                stage.setWidth(screen.getWidth() >= game.getGameWidth()+decoWidth ? game.getGameWidth()+decoWidth : screen.getWidth());
            } else if(code.equals(KeyCode.S) && ke.isControlDown()) {
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(stage);
                if(file != null){
                    try {
                        WritableImage writableImage = new WritableImage((int) game.getWidth(), (int) game.getHeight());
                        game.snapshot(null, writableImage);
                        RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                        ImageIO.write(renderedImage, "png", file);
                    } catch (IOException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
        stage.setScene(scene);
        stage.setTitle("JPowder");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/powder/icon.png")));
        stage.setOnCloseRequest(ev -> {
            Platform.exit();
            System.exit(0);
        });

        keyMap.put("Q","Quit the current game.");
        keyMap.put("Ctrl + N","Create a new game.");
        keyMap.put("G","Change the grid size.");
        keyMap.put("~ or `","Toggle the settings and info panel.");
        keyMap.put("Space","Toggle paused state of game updating.");
        keyMap.put("T","Cycle through mouse types of available: Circle, Square.");
        keyMap.put("Ctrl + R", "Resize the window to adjust to the game size (W x H).");
        keyMap.put("Ctrl + S", "Save a screenshot of the current window.");

        Label label = new Label("A problem has occured and the game stopped updating.");
        label.setPadding(new Insets(25,25,25,25));
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 18));
        label.setStyle("-fx-text-fill: red;");

        StackPane problem = new StackPane();
        problem.setAlignment(Pos.CENTER);
        problem.setStyle("-fx-background-color: rgba(127,127,127,0.5);");
        problem.getChildren().add(label);

        Task<Void> task = new Task<Void>(){
            public Void call() {
                while(!game.getEngine().hasQuit()) {
                    try { Thread.sleep(200); } catch (Exception ignored) {}
                }
                Platform.runLater(() -> pane.getChildren().add(problem));
                return null;
            }
        };
        new Thread(task).start();

        ctrlPane = createKeyMapPane();
        stage.show();
    }

    public void toggleKeyMapPane() {
        if(pane.getChildren().contains(ctrlPane)) {
            pane.getChildren().remove(ctrlPane);
        } else {
            pane.getChildren().add(ctrlPane);
        }
    }

    public StackPane createKeyMapPane() {
        Label label = new Label("JPowder 2");
        label.setFont(Font.font("Kankin", FontWeight.SEMI_BOLD, 32));
        label.setStyle("-fx-text-fill: coral");

        Hyperlink git = new Hyperlink("GitHub: mattwright324/jpowder-game");
        git.setOnAction(ae -> openInBrowser("https://github.com/mattwright324/jpowder-game"));

        VBox header = new VBox(10);
        header.setMaxWidth(600);
        header.setAlignment(Pos.TOP_CENTER);
        header.getChildren().addAll(label, git);
        header.setStyle("-fx-background-color: rgba(34,34,34,1); -fx-background-radius: 8 8 0 0;");
        header.setPadding(new Insets(10,10,10,10));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(10);
        grid.addRow(0, new Label("Key Combo"), new Label("Description"));
        int row = 1;
        for(String keyCombo : keyMap.keySet()) {
            grid.addRow(row, new Label(keyCombo), new Label(keyMap.get(keyCombo)));
            row++;
        }

        gwidth = new Spinner<>();
        gwidth.setMaxWidth(100);
        gwidth.setEditable(true);
        gwidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10,10000, DEFAULT_WIDTH,24));
        gwidth.focusedProperty().addListener((observable, oldValue, newValue) -> {gwidth.increment(0);});

        gheight = new Spinner<>();
        gheight.setMaxWidth(100);
        gheight.setEditable(true);
        gheight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10,10000, DEFAULT_HEIGHT,24));
        gheight.focusedProperty().addListener((observable, oldValue, newValue) -> {gwidth.increment(0);});

        HBox hbox1 = new HBox(5);
        hbox1.setAlignment(Pos.CENTER);
        hbox1.getChildren().addAll(gwidth, new Label("x"), gheight);

        GridPane grid2 = new GridPane();
        grid2.setAlignment(Pos.TOP_CENTER);
        grid2.setVgap(5);
        grid2.setHgap(10);

        row = 0;
        grid2.addRow(row++, new Label("Width x Height"), hbox1);

        fpsSpin = new Spinner<>();
        fpsSpin.setMaxWidth(100);
        fpsSpin.setEditable(true);
        fpsSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,10000, DEFAULT_FPS_CAP,10));
        fpsSpin.focusedProperty().addListener((observable, oldValue, newValue) -> {gwidth.increment(0);});
        grid2.addRow(row++, new Label("Frames-Per-Sec Cap"), fpsSpin);

        upsSpin = new Spinner<>();
        upsSpin.setMaxWidth(100);
        upsSpin.setEditable(true);
        upsSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,10000, DEFAULT_UPS_CAP,10));
        upsSpin.focusedProperty().addListener((observable, oldValue, newValue) -> {gwidth.increment(0);});
        grid2.addRow(row++, new Label("Updates-Per-Sec Cap"), upsSpin);

        close = new Button("Close");
        close.setOnAction(ae -> toggleKeyMapPane());

        apply = new Button("Apply");
        apply.setOnAction(ae -> {
            game.newEngine(gwidth.getValue(), gheight.getValue());
            game.setFPSCap(fpsSpin.getValue());
            game.getEngine().setUPSCap(upsSpin.getValue());
        });

        def = new Button("Default Settings");
        def.setOnAction(ae -> {
            gwidth.getValueFactory().setValue(DEFAULT_WIDTH);
            gheight.getValueFactory().setValue(DEFAULT_HEIGHT);
            fpsSpin.getValueFactory().setValue(DEFAULT_FPS_CAP);
            upsSpin.getValueFactory().setValue(DEFAULT_UPS_CAP);
        });

        HBox hbox = new HBox(25);
        hbox.setMaxWidth(600);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(def, apply, close);

        GridPane vals = new GridPane();
        vals.setMaxWidth(600);
        vals.setVgap(5);
        vals.setHgap(10);
        vals.setAlignment(Pos.TOP_CENTER);

        row = 0;
        TextField vw = new TextField();
        vw.setEditable(false);
        vw.textProperty().bind(game.widthProperty.asString().concat(" x ").concat(game.heightProperty));
        vals.addRow(row++, new Label("Game Dimensions"), vw);

        TextField wd = new TextField();
        wd.setEditable(false);
        wd.textProperty().bind(stage.widthProperty().asString().concat(" x ").concat(stage.heightProperty()));
        vals.addRow(row++, new Label("Window Dimensions"), wd);

        TextField mp = new TextField();
        mp.setEditable(false);
        mp.textProperty().bind(game.widthProperty.multiply(game.heightProperty).asString());
        vals.addRow(row++, new Label("Max Particles"), mp);

        TextField fps = new TextField();
        fps.setEditable(false);
        fps.textProperty().bind(game.fpsCap.asString().concat(" FPS"));
        vals.addRow(row++, new Label("Frames-Per-Sec Cap"), fps);

        TextField ups = new TextField();
        ups.setEditable(false);
        ups.textProperty().bind(game.getEngine().upsCap.asString().concat(" UPS"));
        vals.addRow(row++, new Label("Updates-Per-Sec Cap"), ups);

        TitledPane title = new TitledPane();
        title.setAlignment(Pos.TOP_CENTER);
        title.setText("Values");
        title.setContent(vals);
        title.setExpanded(false);
        title.setMaxWidth(600);

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.TOP_CENTER);
        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(25,25,25,25));
        vbox.getChildren().addAll(header, grid2, hbox, new Separator(), grid, new Separator(), title);

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        StackPane stack = new StackPane();
        stack.setStyle("-fx-background-color: rgba(24,24,24,0.8)");
        stack.setAlignment(Pos.CENTER);
        stack.getChildren().addAll(scroll);
        return stack;
    }

    public enum MouseType {
        CIRCLE, SQUARE;
        public MouseType next() {
            if(ordinal() == values().length - 1)
                return values()[0];
            return values()[ordinal()+1];
        }
    }

    public class GameCanvas extends Canvas {
        private long fps = 0;
        private long lastFPS = 0;
        private long ms = 0;
        private SimpleLongProperty fpsCap = new SimpleLongProperty(DEFAULT_FPS_CAP);
        private CountDownLatch drawLatch;

        private GameEngine engine;
        private boolean drawCanvas = true;
        private boolean drawGame = true;
        private int parts = 0;

        private MouseEvent mousePos = null, lastMouse = null;
        //private int viewX = 0, viewY = 0;
        private SimpleIntegerProperty widthProperty, heightProperty;
        private int cursorSize = 5;
        private boolean placeEl = false;
        private int gridSize = 0;
        private MouseType type = MouseType.CIRCLE;

        /**
         * Default Game Size 15x15
         */
        public GameCanvas() { this(DEFAULT_WIDTH, DEFAULT_HEIGHT); }

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

        private void nextMouseShape() {
            type = type.next();
        }
        private int gridX(double screenX) {
            return (int) (screenX * width() / getWidth());
        }
        private int gridY(double screenY) {
            return (int) (screenY * height() / getHeight());
        }
        private double screenX(int gridX) {
            return getWidth() / width() * gridX;
        }
        private double screenY(int gridY) { return getHeight() / height() * gridY; }

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

        private GameEngine getEngine() { return engine; }

        private void newEngine(int w, int h) {
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
        private int getGridSize() { return gridSize; }
        private void incrGridSize() { gridSize++; }
        private void setGridSize(int size) { gridSize = size; }
    }

    public class GameEngine {
        private long ups = 0, lastUPS = 0;
        private long ms = 0;
        private SimpleLongProperty upsCap = new SimpleLongProperty(DEFAULT_UPS_CAP);

        private long parts = 0, lastParts = 0;
        private boolean canUpdate = true;
        private boolean running = true;

        private int width, height;
        private Cell[][] grid;
        private CountDownLatch cdl;

        private GameEngine(int width, int height) {
            this.width = width;
            this.height = height;
            grid = new Cell[height][width];
            //for(int i=0; i<2000; i++) {
            //    grid[(int) (Math.random() * height)][(int) (Math.random() * width)] = new Cell();
            //}
            //for(int x=0; x<width; x++) { for(int y=0; y<height; y++) grid[y][x] = new Cell(); }
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

        private void drawAtPoint(GameCanvas gc, MouseEvent me, MouseEvent prev, int cursorSize, MouseType type) {
            if(me != null) {
                int y = gc.gridY(me.getY());
                int x = gc.gridX(me.getX());
                if(prev != null) {
                    int y2 = gc.gridY(prev.getY());
                    int x2 = gc.gridX(prev.getX());
                }
                if(y >= 0 && x >= 0 && x < width && y < height) {
                    if(grid[y][x] == null) {
                        grid[y][x] = new Cell(254);
                    }
                }
            }
        }

        private int getHeight() { return height; }
        private int getWidth() { return width; }
        private void setUPSCap(long cap) { upsCap.setValue(cap); }
        private long getUPSCap() { return upsCap.get(); }
        private long getLastUPS() { return lastUPS; }
        private long getPartCount() { return lastParts; }
        private void setCanUpdate(boolean b) {
            canUpdate = b;
        }
        private boolean hasQuit() {
            return !running;
        }
        private Cell[][] getGrid() { return grid; }
        private void newGrid() {
            grid = new Cell[height][width];
        }
        private boolean isPaused() { return canUpdate; }
        private void togglePause() { canUpdate = !canUpdate; }

        /**
         * Permanently ends the engine from running.
         */
        private void quit() {
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
        private GameEngine copyStateToNewSize(int w, int h) {
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

    public class Cell {
        public Color color = Color.BEIGE;
        public double blue = 0;
        public long uid = 0;
        public void update(long uid) {
            this.uid = uid;
        }
        public Color getColor() {
            return color;
        }
        public Cell() {}
        public Cell(double blue) {
            this.blue = blue;
        }
    }

    public static void openInBrowser(String link) {
        link = link.replace(" ", "%20");
        try {
            URL url = new URL(link);
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(url.toURI());
            } else {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("xdg-open "+url.getPath());
            }
        } catch (Throwable e2) {
            e2.printStackTrace();
        }
    }
}
