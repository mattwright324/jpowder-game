package io.mattw.jpowder;

import io.mattw.jpowder.elements.Element;
import io.mattw.jpowder.walls.Wall;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class BottomMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    public static int width = Display.width + SideMenu.width;
    public static int height = 50;

    private Graphics2D g2d;
    private int b_w = 40;
    private int b_h = 18;
    private int b_y = height - b_h - 5;
    private int b_txt_center = b_y + b_h / 2 + 5;
    private Rectangle clear = new Rectangle(5, b_y, b_w, b_h);
    private Rectangle resize = new Rectangle(5 + (b_w + 5), b_y, b_w, b_h);
    private Rectangle pause = new Rectangle(5 + (b_w + 5) * 2, b_y, b_w, b_h);
    private Rectangle view = new Rectangle(5 + (b_w + 5) * 3, b_y, b_w, b_h);
    private Rectangle help = new Rectangle(5 + (b_w + 5) * 4, b_y, b_w, b_h);
    private List<Button> buttons = new ArrayList<>();
    private Point mouse = new Point(0, 0);

    public BottomMenu() {
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2d = (Graphics2D) g;

        g2d.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, height, Color.WHITE));
        g2d.fillRect(0, 0, Display.width * 2 + SideMenu.width, height);

        g2d.setColor(new Color(32, 64, 128, 128));
        g2d.fill(clear);
        g2d.fill(resize);
        g2d.fill(view);
        g2d.fill(help);
        if (Game.paused) {
            g2d.setColor(new Color(255, 64, 128, 128));
        }
        g2d.fill(pause);
        makeButtons();
        draw_buttons();
        g2d.setColor(Color.WHITE);
        g2d.drawString("NEW", clear.x + 5, b_txt_center);
        g2d.drawString("SIZE", resize.x + 5, b_txt_center);
        g2d.fillRect(pause.x + 12, pause.y + 5, 5, pause.height - 9);
        g2d.fillRect(pause.x + 22, pause.y + 5, 5, pause.height - 9);
        g2d.drawString("VIEW", view.x + 5, b_txt_center);
        g2d.drawString("KEYS", help.x + 5, b_txt_center);
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        mouse = e.getPoint();
        if (clear.contains(mouse)) {
            Game.paused = true;
            Grid.newGame();
        }
        if (resize.contains(mouse)) {
            Display.toggle_size();
        }
        if (pause.contains(mouse)) {
            Display.toggle_pause();
        }
        if (view.contains(mouse)) {
            if (Display.view == 0) {
                Display.setView(1);
            } else {
                Display.setView(0);
            }
        }
        for (Button b : buttons) {
            if (b.contains(mouse)) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Display.left = b.getItem();
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    Display.right = b.getItem();
                }
            }
        }
        if (help.contains(mouse)) {
            Display.help = !Display.help;
        }
    }

    public void makeButtons() {
        buttons.clear();
        int i = 0;
        for (Item e : SideMenu.selected) {
            int x = MainWindow.mouse.x;
            if (x > MainWindow.window.getWidth() / 2) {
                x = MainWindow.window.getWidth() / 2;
            }
            Button b = new Button(getWidth() - b_w - (5 + (b_w + 5) * i++) + (getWidth() - x - (getWidth() / 2)), 5, b_w, b_h);
            b.setItem(e);
            buttons.add(b);
        }
    }

    public void draw_buttons() {
        for (Button b : buttons) {
            Color c = Color.GRAY;
            if (b.getItem() instanceof Element) {
                c = b.getItem().getColor();
            }
            if (b.getItem() instanceof Wall) {
                c = b.getItem().getColor();
            }
            g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
            g2d.setPaintMode();
            g2d.fill(b);
            if (b.contains(mouse)) {
                g2d.setColor(new Color(255, 0, 0, 128));
                g2d.drawRect(b.x, b.y, b.width, b.height);
            }
            if (b.getItem() == Display.left) {
                g2d.setColor(new Color(255, 0, 0));
                g2d.drawRect(b.x, b.y, b.width, b.height);
            }
            if (b.getItem() == Display.right) {
                g2d.setColor(new Color(0, 0, 255));
                g2d.drawRect(b.x, b.y, b.width, b.height);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawString(b.getItem().getName(), b.x + 2, b.y + b_h / 2 + 5);
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
