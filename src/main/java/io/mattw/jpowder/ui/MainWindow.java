package io.mattw.jpowder.ui;

import com.formdev.flatlaf.FlatLaf;
import io.mattw.jpowder.game.GameThread;
import io.mattw.jpowder.game.Grid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Objects;

public class MainWindow extends JFrame {

    private static final Logger logger = LogManager.getLogger();

    public static BufferedImage HEAT_COLOR_STRIP;
    public static MainWindow window;
    public static Point mouse = new Point(0, 0);
    private static GamePanel game;
    private static SideMenu sideMenu;
    public static BottomMenu bottomMenu;

    public MainWindow() throws Exception {
        logger.trace("MainWindow()");

        var icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png")));
        HEAT_COLOR_STRIP = ImageIO.read(Objects.requireNonNull(getClass().getResource("/heat-color-strip.png")));

        window = this;
        setIconImage(icon);
        setTitle("JPowder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        var centerX = (Toolkit.getDefaultToolkit().getScreenSize().width - (GamePanel.WIDTH * 2 + SideMenu.WIDTH)) / 2;
        var centerY = (Toolkit.getDefaultToolkit().getScreenSize().height - (GamePanel.HEIGHT * 2 + BottomMenu.HEIGHT)) / 2;
        setLocation(centerX, centerY);

        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#0094FF"));

        var newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> Grid.newGame());

        var pauseGame = new JCheckBoxMenuItem("Pause Game");
        pauseGame.addActionListener(e -> {
            GamePanel.togglePause();
            pauseGame.setSelected(GameThread.paused);
        });

        var exitButton = new JMenuItem("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        var gameMenu = new JMenu("Game");
        gameMenu.add(newGame);
        gameMenu.add(pauseGame);
        gameMenu.add(new JSeparator());
        gameMenu.add(exitButton);

        var keybindHelp = new JCheckBoxMenuItem("Show Keybind Help");
        keybindHelp.addActionListener(e -> {
            GamePanel.help = !GamePanel.help;
            SwingUtilities.invokeLater(() -> keybindHelp.setSelected(GamePanel.help));
        });

        var largeView = new JRadioButtonMenuItem("Large Window (2x)");
        largeView.setSelected(true);
        largeView.addActionListener(e -> GamePanel.makeLarge());

        var smallView = new JRadioButtonMenuItem("Small Window (1x)");
        smallView.addActionListener(e -> GamePanel.makeSmall());

        var group1 = new ButtonGroup();
        group1.add(largeView);
        group1.add(smallView);

        var defaultView = new JRadioButtonMenuItem("Default View");
        defaultView.setSelected(true);
        defaultView.addActionListener(e -> GamePanel.setView(0));

        var tempView = new JRadioButtonMenuItem("Temp View");
        tempView.addActionListener(e -> GamePanel.setView(1));

        var lifeGradientView = new JRadioButtonMenuItem("Life Gradient View");
        lifeGradientView.addActionListener(e -> GamePanel.setView(2));

        var fancyView = new JRadioButtonMenuItem("Fancy View");
        fancyView.addActionListener(e -> GamePanel.setView(3));

        var group2 = new ButtonGroup();
        group2.add(defaultView);
        group2.add(tempView);
        group2.add(lifeGradientView);
        group2.add(fancyView);

        var viewMenu = new JMenu("View");
        viewMenu.add(keybindHelp);
        viewMenu.add(new JSeparator());
        viewMenu.add(largeView);
        viewMenu.add(smallView);
        viewMenu.add(new JSeparator());
        viewMenu.add(defaultView);
        viewMenu.add(tempView);
        viewMenu.add(lifeGradientView);
        viewMenu.add(fancyView);

        var menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);

        game = new GamePanel();
        add(game, BorderLayout.CENTER);

        sideMenu = new SideMenu();
        sideMenu.setPreferredSize(new Dimension(SideMenu.WIDTH, SideMenu.HEIGHT));
        add(sideMenu, BorderLayout.EAST);

        bottomMenu = new BottomMenu();
        bottomMenu.setPreferredSize(new Dimension(BottomMenu.WIDTH, BottomMenu.HEIGHT));
        add(bottomMenu, BorderLayout.SOUTH);

        resize();
    }

    public static void updateMouseInFrame(Point p, Component c) {
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, window);
        mouse = p;
        bottomMenu.repaint();
    }

    public void resize() {
        getContentPane().setPreferredSize(new Dimension(GamePanel.WIDTH * GamePanel.scale + SideMenu.WIDTH, GamePanel.HEIGHT * GamePanel.scale + BottomMenu.HEIGHT));
        pack();
    }

}
