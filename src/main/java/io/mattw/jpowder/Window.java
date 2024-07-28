package io.mattw.jpowder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Window extends JFrame {

    private static final Logger logger = LogManager.getLogger();

    public static BufferedImage heatColorStrip;
    public static Window window;
    public static Point mouse = new Point(0, 0);
    static Display game;
    static SideMenu menu;
    static BottomMenu menub;

    public Window() {
        window = this;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setTitle("JPowder");
        setResizable(false);

        try {
            BufferedImage iconImg = ImageIO.read(ClassLoader.getSystemResourceAsStream("io/mattw/jpowder/jpowder.png"));
            setIconImage(iconImg);

            heatColorStrip = ImageIO.read(ClassLoader.getSystemResourceAsStream("io/mattw/jpowder/colorstrip.png"));
        } catch (IOException e) {
            logger.error(e);
        }

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
