package mattw.powder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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
import java.util.logging.Level;
import java.util.logging.Logger;


public class JPowder extends Application {

    public static Stage stage;
    public Map<String,String> keyMap = new HashMap<>();

    public GameCanvas game;
    public StackPane pane, ctrlPane;

    public Button apply, close, def;
    public Spinner<Integer> gwidth, gheight, fpsSpin, upsSpin;

    static final int DEFAULT_FPS_CAP = 255;
    static final int DEFAULT_UPS_CAP = 60;
    static final int DEFAULT_WIDTH = 612;
    static final int DEFAULT_HEIGHT = 384;
    static Stage getStage() { return stage; }

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        this.stage = stage;
        game = new GameCanvas();

        StackPane background = new StackPane();
        background.setStyle("-fx-background-color: black");

        class Option extends Label {
            public Option(String text) {
                setWidth(10);
                setHeight(10);
                setText(text);
                setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
                setStyle("-fx-border-color: white; -fx-border-width: 1");
                setPadding(new Insets(4));
                setTextFill(Color.WHITE);
                setAlignment(Pos.CENTER);
                setCursor(Cursor.HAND);
                setOnMouseEntered(me -> {
                    setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
                    setTextFill(Color.BLACK);
                });
                setOnMouseExited(me -> {
                    setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
                    setTextFill(Color.WHITE);
                });
            }
        }

        VBox menuR = new VBox(2);
        menuR.setStyle("-fx-background-color: black;");
        menuR.setAlignment(Pos.TOP_CENTER);
        menuR.getChildren().addAll(new Option("A"), new Option("B"), new Option("C"), new Option("D"));
        menuR.setPadding(new Insets(2));

        BorderPane border = new BorderPane();
        border.setCenter(game);
        border.setRight(menuR);

        pane = new StackPane();
        pane.setAlignment(Pos.CENTER);
        pane.getChildren().addAll(background, border);
        game.heightProperty().bind(pane.heightProperty());
        game.widthProperty().bind(pane.widthProperty().subtract(menuR.widthProperty()));

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
                toggleControlPane();
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
        stage.getIcons().add(new Image("/mattw/powder/img/icon.png"));
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

        StackPane problem = new StackPane();
        problem.setAlignment(Pos.CENTER);
        problem.setStyle("-fx-background-color: rgba(127,127,127,0.5);");

        Label label = new Label("A problem has occured and the game stopped updating.");
        label.setPadding(new Insets(25,25,25,25));
        label.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 18));
        label.setStyle("-fx-text-fill: red;");

        Button close = new Button("Close");
        close.setOnAction(ae -> {
            if(pane.getChildren().contains(problem))
                pane.getChildren().remove(problem);
        });

        VBox vbox = new VBox(25);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(label, close);
        problem.getChildren().add(vbox);

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

        ctrlPane = createControlPane();
        stage.show();
    }

    public void toggleControlPane() {
        if(pane.getChildren().contains(ctrlPane)) {
            pane.getChildren().remove(ctrlPane);
        } else {
            pane.getChildren().add(ctrlPane);
        }
    }

    public StackPane createControlPane() {
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

        CheckBox resizeable = new CheckBox();
        resizeable.setSelected(true);

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
        grid2.addRow(row++, new Label("Window Resizable"), resizeable);
        grid2.addRow(row++, new Label("Width x Height"), hbox1);

        fpsSpin = new Spinner<>();
        fpsSpin.setMaxWidth(100);
        fpsSpin.setEditable(true);
        fpsSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,10000, DEFAULT_FPS_CAP,5));
        fpsSpin.focusedProperty().addListener((observable, oldValue, newValue) -> {gwidth.increment(0);});
        grid2.addRow(row++, new Label("Frames-Per-Sec Cap"), fpsSpin);

        upsSpin = new Spinner<>();
        upsSpin.setMaxWidth(100);
        upsSpin.setEditable(true);
        upsSpin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0,10000, DEFAULT_UPS_CAP,1));
        upsSpin.focusedProperty().addListener((observable, oldValue, newValue) -> {gwidth.increment(0);});
        grid2.addRow(row++, new Label("Updates-Per-Sec Cap"), upsSpin);

        close = new Button("Close");
        close.setOnAction(ae -> toggleControlPane());

        apply = new Button("Apply");
        apply.setOnAction(ae -> {
            System.out.println("Applying changes: "+gwidth.getValue()+"x"+gheight.getValue()+"; "+fpsSpin.getValue()+" FPS cap; "+upsSpin.getValue()+" UPS cap;");
            game.newEngine(gwidth.getValue(), gheight.getValue());
            game.setFPSCap(fpsSpin.getValue());
            game.getEngine().setUPSCap(upsSpin.getValue());
            stage.setResizable(resizeable.isSelected());
        });

        def = new Button("Default Settings");
        def.setOnAction(ae -> {
            gwidth.getValueFactory().setValue(DEFAULT_WIDTH);
            gheight.getValueFactory().setValue(DEFAULT_HEIGHT);
            fpsSpin.getValueFactory().setValue(DEFAULT_FPS_CAP);
            upsSpin.getValueFactory().setValue(DEFAULT_UPS_CAP);
            resizeable.setSelected(true);
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
