package io.mattw.jpowder.ui;

import io.mattw.jpowder.game.ElementType;
import io.mattw.jpowder.game.Item;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

public class SideMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    public static final int WIDTH = 60;
    public static final int HEIGHT = GamePanel.HEIGHT;
    public static Item[] selectedCategory = ElementType.solid;

    private BufferedImage img;
    private Graphics2D graphics;
    private boolean init = true;
    private final int btnHeight = 40;
    private final int btnWidth = WIDTH - 10;
    private final Rectangle solidRect = new Rectangle(5, 5, btnWidth, btnHeight);
    private final Rectangle liquidRect = new Rectangle(5, 5 + (btnHeight + 5), btnWidth, btnHeight);
    private final Rectangle gassesRect = new Rectangle(5, 5 + (btnHeight + 5) * 2, btnWidth, btnHeight);
    private final Rectangle powderRect = new Rectangle(5, 5 + (btnHeight + 5) * 3, btnWidth, btnHeight);
    private final Rectangle radioRect = new Rectangle(5, 5 + (btnHeight + 5) * 4, btnWidth, btnHeight);
    private final Rectangle toolsRect = new Rectangle(5, 5 + (btnHeight + 5) * 5, btnWidth, btnHeight);
    private final Map<String, Item[]> categories = new LinkedHashMap<>();

    public SideMenu() {
        // EventBus.getDefault().register(this);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);

        categories.put("Solid", ElementType.solid);
        categories.put("Liquid", ElementType.liquid);
        categories.put("Gas", ElementType.gasses);
        categories.put("Powder", ElementType.powder);
        categories.put("Radio", ElementType.radio);
        categories.put("Tools", ElementType.tools);
    }

    public void init() {
        init = false;
        img = new BufferedImage(WIDTH, HEIGHT * 2, BufferedImage.TYPE_INT_ARGB);
        graphics = img.createGraphics();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
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



        graphics.setColor(Color.WHITE);

        int btnTxtLine1 = btnHeight / 2;
        int btnTxtLine2 = btnHeight / 2 + 15;
        int line = 0;
        for (String category : categories.keySet()) {
            graphics.drawString(category, 10, btnTxtLine1 + (btnHeight + 5) * line);
            graphics.drawString(categories.get(category).length + "", 10, btnTxtLine2 + (btnHeight + 5) * line++);
        }

        graphics.setPaintMode();

        ((Graphics2D) g).drawImage(img, null, 0, 0);
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
            selectedCategory = ElementType.solid;
        }
        if (liquidRect.contains(p)) {
            selectedCategory = ElementType.liquid;
        }
        if (gassesRect.contains(p)) {
            selectedCategory = ElementType.gasses;
        }
        if (powderRect.contains(p)) {
            selectedCategory = ElementType.powder;
        }
        if (radioRect.contains(p)) {
            selectedCategory = ElementType.radio;
        }
        if (toolsRect.contains(p)) {
            selectedCategory = ElementType.tools;
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
