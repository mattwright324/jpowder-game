package io.mattw.jpowder;

import io.mattw.jpowder.items.Elements;
import io.mattw.jpowder.items.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class SideMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    public static final int WIDTH = 60;
    public static final int HEIGHT = Display.HEIGHT;
    public static Item[] selectedCategory = Elements.solid;

    private BufferedImage img;
    private Graphics2D graphics;
    private boolean init = true;
    private final int b_h = 40;
    private final int b_w = WIDTH - 10;
    private final Rectangle solidRect = new Rectangle(5, 5, b_w, b_h);
    private final Rectangle liquidRect = new Rectangle(5, 5 + (b_h + 5), b_w, b_h);
    private final Rectangle gassesRect = new Rectangle(5, 5 + (b_h + 5) * 2, b_w, b_h);
    private final Rectangle powderRect = new Rectangle(5, 5 + (b_h + 5) * 3, b_w, b_h);
    private final Rectangle radioRect = new Rectangle(5, 5 + (b_h + 5) * 4, b_w, b_h);
    private final Rectangle toolsRect = new Rectangle(5, 5 + (b_h + 5) * 5, b_w, b_h);

    public SideMenu() {
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void init() {
        init = false;
        img = new BufferedImage(WIDTH, HEIGHT * 2, BufferedImage.TYPE_INT_ARGB);
        graphics = img.createGraphics();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (init) {
            init();
        }

        Color bg = new Color(64, 64, 64);
        graphics.setPaint(new GradientPaint(0, 0, bg, WIDTH, 0, bg));
        graphics.fillRect(0, 0, WIDTH, HEIGHT * 2);

        graphics.setColor(new Color(128, 128, 128, 128));
        graphics.fill(solidRect);
        graphics.fill(liquidRect);
        graphics.fill(gassesRect);
        graphics.fill(powderRect);
        graphics.fill(radioRect);
        graphics.fill(toolsRect);

        int line = 0;

        graphics.setColor(Color.WHITE);
        int b_txt = b_h / 2;
        graphics.drawString("Solid", 10, b_txt + (b_h + 5) * line);
        int b_txtn = b_h / 2 + 15;
        graphics.drawString(Elements.solid.length + "", 10, b_txtn + (b_h + 5) * line++);

        graphics.drawString("Liquid", 10, b_txt + (b_h + 5) * line);
        graphics.drawString(Elements.liquid.length + "", 10, b_txtn + (b_h + 5) * line++);

        graphics.drawString("Gass", 10, b_txt + (b_h + 5) * line);
        graphics.drawString(Elements.gasses.length + "", 10, b_txtn + (b_h + 5) * line++);

        graphics.drawString("Powder", 10, b_txt + (b_h + 5) * line);
        graphics.drawString(Elements.powder.length + "", 10, b_txtn + (b_h + 5) * line++);

        graphics.drawString("Radio", 10, b_txt + (b_h + 5) * line);
        graphics.drawString(Elements.radio.length + "", 10, b_txtn + (b_h + 5) * line++);

        graphics.drawString("Tools", 10, b_txt + (b_h + 5) * line);
        graphics.drawString(Elements.tools.length + "", 10, b_txtn + (b_h + 5) * line);

        graphics.setPaintMode();
        g2d.drawImage(img, null, 0, 0);
    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        if (solidRect.contains(p)) {
            selectedCategory = Elements.solid;
        }
        if (liquidRect.contains(p)) {
            selectedCategory = Elements.liquid;
        }
        if (gassesRect.contains(p)) {
            selectedCategory = Elements.gasses;
        }
        if (powderRect.contains(p)) {
            selectedCategory = Elements.powder;
        }
        if (radioRect.contains(p)) {
            selectedCategory = Elements.radio;
        }
        if (toolsRect.contains(p)) {
            selectedCategory = Elements.tools;
        }
        MainWindow.bottomMenu.repaint();
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseDragged(MouseEvent e) {
        MainWindow.updateMouseInFrame(e.getPoint(), this);
    }

    public void mouseMoved(MouseEvent e) {
        MainWindow.updateMouseInFrame(e.getPoint(), this);
    }

}
