package io.mattw.jpowder;

import io.mattw.jpowder.items.Element;
import io.mattw.jpowder.items.Item;
import io.mattw.jpowder.items.Wall;
import lombok.Getter;
import lombok.Setter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BottomMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    public static final int WIDTH = Display.WIDTH + SideMenu.WIDTH;
    public static final int HEIGHT = 50;

    private Graphics2D graphics;
    private final int b_w = 40;
    private final int b_h = 18;
    private final int b_y = HEIGHT - b_h - 5;
    private final Rectangle clearRect = new Rectangle(5, b_y, b_w, b_h);
    private final Rectangle resizeRect = new Rectangle(5 + (b_w + 5), b_y, b_w, b_h);
    private final Rectangle pauseRect = new Rectangle(5 + (b_w + 5) * 2, b_y, b_w, b_h);
    private final Rectangle viewRect = new Rectangle(5 + (b_w + 5) * 3, b_y, b_w, b_h);
    private final Rectangle helpRect = new Rectangle(5 + (b_w + 5) * 4, b_y, b_w, b_h);
    private final List<Button> buttons = new ArrayList<>();
    private Point mouse = new Point(0, 0);

    public BottomMenu() {
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        graphics = (Graphics2D) g;

        graphics.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, HEIGHT, Color.WHITE));
        graphics.fillRect(0, 0, Display.WIDTH * 2 + SideMenu.WIDTH, HEIGHT);

        try {
            var bgImg = ImageIO.read(Objects.requireNonNull(getClass().getResource("/bottom-bg.png")));
            graphics.drawImage(bgImg, 0, 0, getWidth(), getHeight(), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        graphics.setColor(new Color(32, 64, 128, 128));
        graphics.fill(clearRect);
        graphics.fill(resizeRect);
        graphics.fill(viewRect);
        graphics.fill(helpRect);
        if (Game.paused) {
            graphics.setColor(new Color(255, 64, 128, 128));
        }
        graphics.fill(pauseRect);
        makeButtons();
        drawButtons();
        graphics.setColor(Color.WHITE);
        int b_txt_center = b_y + b_h / 2 + 5;
        graphics.drawString("NEW", clearRect.x + 5, b_txt_center);
        graphics.drawString("SIZE", resizeRect.x + 5, b_txt_center);
        graphics.fillRect(pauseRect.x + 12, pauseRect.y + 5, 5, pauseRect.height - 9);
        graphics.fillRect(pauseRect.x + 22, pauseRect.y + 5, 5, pauseRect.height - 9);
        graphics.drawString("VIEW", viewRect.x + 5, b_txt_center);
        graphics.drawString("KEYS", helpRect.x + 5, b_txt_center);
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        mouse = e.getPoint();
        if (clearRect.contains(mouse)) {
            Game.paused = true;
            Grid.newGame();
        }
        if (resizeRect.contains(mouse)) {
            Display.toggle_size();
        }
        if (pauseRect.contains(mouse)) {
            Display.togglePause();
        }
        if (viewRect.contains(mouse)) {
            if (Display.view == 0) {
                Display.setView(1);
            } else {
                Display.setView(0);
            }
        }
        for (Button b : buttons) {
            if (b.contains(mouse)) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Display.leftClickType = b.getItem();
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    Display.rightClickType = b.getItem();
                }
            }
        }
        if (helpRect.contains(mouse)) {
            Display.help = !Display.help;
        }
    }

    public void makeButtons() {
        buttons.clear();
        int i = 0;
        for (Item e : SideMenu.selectedCategory) {
            int x = MainWindow.mouse.x;
            if (x > MainWindow.window.getWidth() / 2) {
                x = MainWindow.window.getWidth() / 2;
            }
            Button b = new Button(getWidth() - b_w - (5 + (b_w + 5) * i++) + (getWidth() - x - (getWidth() / 2)), 5, b_w, b_h);
            b.setItem(e);
            buttons.add(b);
        }
    }

    public void drawButtons() {
        for (Button b : buttons) {
            Color c = Color.GRAY;
            if (b.getItem() instanceof Element) {
                c = b.getItem().getColor();
            }
            if (b.getItem() instanceof Wall) {
                c = b.getItem().getColor();
            }
            graphics.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
            graphics.setPaintMode();
            graphics.fill(b);
            if (b.contains(mouse)) {
                graphics.setColor(new Color(255, 0, 0, 128));
                graphics.drawRect(b.x, b.y, b.width, b.height);
            }
            if (b.getItem() == Display.leftClickType) {
                graphics.setColor(new Color(255, 0, 0));
                graphics.drawRect(b.x, b.y, b.width, b.height);
            }
            if (b.getItem() == Display.rightClickType) {
                graphics.setColor(new Color(0, 0, 255));
                graphics.drawRect(b.x, b.y, b.width, b.height);
            }
            graphics.setColor(Color.WHITE);
            graphics.drawString(b.getItem().getName(), b.x + 2, b.y + b_h / 2 + 5);
        }
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public void mouseDragged(MouseEvent e) {
        mouse = e.getPoint();
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
        mouse = e.getPoint();
        MainWindow.updateMouseInFrame(e.getPoint(), this);
    }
    
    @Getter
    @Setter
    public static class Button extends Rectangle {
        private Item item;

        public Button(int x, int y, int w, int h) {
            super(x, y, w, h);
        }
    }
}
