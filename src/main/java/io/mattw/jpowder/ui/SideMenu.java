package io.mattw.jpowder.ui;

import io.mattw.jpowder.event.CategorySelectEvent;
import io.mattw.jpowder.game.ItemCategory;
import lombok.extern.log4j.Log4j2;
import org.greenrobot.eventbus.EventBus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
public class SideMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    public static final int WIDTH = 60;
    public static final int HEIGHT = GamePanel.HEIGHT;
    public static ItemCategory selectedCategory = ItemCategory.POWDER;

    private final int btnHeight = 40;
    private final int btnWidth = WIDTH - 10;
    private final Map<ItemCategory, Rectangle> categoryRects = new LinkedHashMap<>();

    private BufferedImage img;
    private Graphics2D graphics;
    private boolean init = true;

    public SideMenu() {
        // EventBus.getDefault().register(this);
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);

        int i = 0;
        for (ItemCategory category : ItemCategory.values()) {
            var yOffset = 5 + (btnHeight + 5) * i++;
            categoryRects.put(category, new Rectangle(5, yOffset, btnWidth, btnHeight));
        }
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

        //log.debug("Draw category btns");
        for (Map.Entry<ItemCategory, Rectangle> entry : categoryRects.entrySet()) {
            graphics.setColor(new Color(128, 128, 128, 128));
            graphics.fill(entry.getValue());

            var rect = entry.getValue();
            if (selectedCategory.equals(entry.getKey())) {
                graphics.setColor(Color.GREEN);
                graphics.drawRect(rect.x, rect.y, rect.width, rect.height);
            }
        }
        graphics.setColor(Color.WHITE);

        int btnTxtLine1 = btnHeight / 2;
        int btnTxtLine2 = btnHeight / 2 + 15;
        int line = 0;
        for (ItemCategory category : ItemCategory.values()) {
            graphics.drawString(category.getDisplay(), 10, btnTxtLine1 + (btnHeight + 5) * line);
            graphics.drawString(category.getItems().length + "", 10, btnTxtLine2 + (btnHeight + 5) * line++);
        }

        graphics.setPaintMode();

        ((Graphics2D) g).drawImage(img, null, 0, 0);
    }

    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(this::repaint);
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        var before = selectedCategory;

        for (Map.Entry<ItemCategory, Rectangle> entry : categoryRects.entrySet()) {
            if (entry.getValue().contains(p)) {
                selectedCategory = entry.getKey();
            }
        }

        if (selectedCategory != before) {
            EventBus.getDefault().post(new CategorySelectEvent(selectedCategory));
            SwingUtilities.invokeLater(this::repaint);
        }
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
