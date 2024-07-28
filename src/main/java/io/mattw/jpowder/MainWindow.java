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

        var exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(exitButton);

        var largeView = new JRadioButtonMenuItem("Large View");
        largeView.setSelected(true);
        largeView.addActionListener(e -> Display.makeLarge());

        var smallView = new JRadioButtonMenuItem("Small View");
        smallView.addActionListener(e -> Display.makeSmall());

        var group = new ButtonGroup();
        group.add(largeView);
        group.add(smallView);

        var viewMenu = new JMenu("View");
        viewMenu.add(largeView);
        viewMenu.add(smallView);

        var menuBar = new JMenuBar();
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);

        game = new Display();
        add(game, BorderLayout.CENTER);

        menu = new SideMenu();
        menu.setPreferredSize(new Dimension(SideMenu.width, SideMenu.height));
        add(menu, BorderLayout.EAST);

        menub = new BottomMenu();
        menub.setPreferredSize(new Dimension(BottomMenu.width, BottomMenu.height));
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
        getContentPane().setPreferredSize(new Dimension(Display.width * Display.scale + SideMenu.width, Display.height * Display.scale + BottomMenu.height));
        pack();
    }

}
