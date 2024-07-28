package io.mattw.jpowder;

import com.formdev.flatlaf.FlatLaf;
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

    public static BufferedImage heatColorStrip;
    public static MainWindow window;
    public static Point mouse = new Point(0, 0);
    static Display game;
    static SideMenu menu;
    static BottomMenu menub;

    public MainWindow() throws Exception {
        logger.trace("MainWindow()");

        var icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png")));
        heatColorStrip = ImageIO.read(Objects.requireNonNull(getClass().getResource("/heat-color-strip.png")));

        window = this;
        setIconImage(icon);
        setTitle("JPowder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        var centerX = (Toolkit.getDefaultToolkit().getScreenSize().width - Display.width * 2) / 2;
        var centerY = (Toolkit.getDefaultToolkit().getScreenSize().height - Display.height * 2) / 2;
        setLocation(centerX, centerY);

        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#0094FF"));

        var newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> Grid.newGame());

        var pauseGame = new JCheckBoxMenuItem("Pause Game");
        pauseGame.addActionListener(e -> {
            Display.togglePause();
            pauseGame.setSelected(Game.paused);
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
            Display.help = !Display.help;
            SwingUtilities.invokeLater(() -> keybindHelp.setSelected(Display.help));
        });

        var largeView = new JRadioButtonMenuItem("Large Window (2x)");
        largeView.setSelected(true);
        largeView.addActionListener(e -> Display.makeLarge());

        var smallView = new JRadioButtonMenuItem("Small Window (1x)");
        smallView.addActionListener(e -> Display.makeSmall());

        var group1 = new ButtonGroup();
        group1.add(largeView);
        group1.add(smallView);

        var defaultView = new JRadioButtonMenuItem("Default View");
        defaultView.setSelected(true);
        defaultView.addActionListener(e -> Display.setView(0));

        var tempView = new JRadioButtonMenuItem("Temp View");
        tempView.addActionListener(e -> Display.setView(1));

        var lifeGradientView = new JRadioButtonMenuItem("Life Gradient View");
        lifeGradientView.addActionListener(e -> Display.setView(2));

        var fancyView = new JRadioButtonMenuItem("Fancy View");
        fancyView.addActionListener(e -> Display.setView(3));

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

        game = new Display();
        add(game, BorderLayout.CENTER);

        menu = new SideMenu();
        menu.setPreferredSize(new Dimension(SideMenu.WIDTH, SideMenu.HEIGHT));
        add(menu, BorderLayout.EAST);

        menub = new BottomMenu();
        menub.setPreferredSize(new Dimension(BottomMenu.WIDTH, BottomMenu.HEIGHT));
        add(menub, BorderLayout.SOUTH);

        resize();
    }

    public static void updateMouseInFrame(Point p, Component c) {
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, window);
        mouse = p;
        menub.repaint();
    }

    public void resize() {
        getContentPane().setPreferredSize(new Dimension(Display.width * Display.scale + SideMenu.WIDTH, Display.height * Display.scale + BottomMenu.HEIGHT));
        pack();
    }

}
