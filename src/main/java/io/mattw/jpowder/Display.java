package io.mattw.jpowder;

import io.mattw.jpowder.elements.Element;
import io.mattw.jpowder.elements.Elements;
import io.mattw.jpowder.particles.Particle;
import io.mattw.jpowder.walls.Wall;
import io.mattw.jpowder.walls.Walls;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Display extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    final static int width = 612; // 612
    final static int height = 384; // 384
    public static int view = 0;
    //static int img_scale = 1;
    //static int cell_w = 0;
    //static int cell_h = 0;
    static int scale = 1; // fillRect vs drawRect
    static boolean small = true;
    static Graphics2D w2d;
    static BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    static Graphics2D b2d = img.createGraphics();
    static Font typeface = new Font("Monospaced", Font.PLAIN, 11);
    static Item left = Elements.dust; // Hacky as fuck.
    static Item right = Elements.none;
    static Counter dfps = new Counter();
    static String viewName = "Default";
    static boolean hud = true;
    static boolean help = false;
    public Timer timer = new Timer(5, this);
    public Point mouse = new Point(0, 0);
    public Point mouse_drag = new Point(0, 0);
    public Game game = new Game();
    public int size = 0, nsize = 0;
    public int draw_size = 0;
    public Point mstart = new Point(0, 0), mstop = new Point(0, 0);
    public boolean mouse_square = false;
    public InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    public ActionMap am = getActionMap();

    public Display() {
        for (int w = 0; w < Display.width; w++) {
            for (int h = 0; h < Display.height; h++) {
                Grid.pgrid[w][h] = new Cell(w, h);
            }
        }
        for (int w = 0; w < Display.width / 4; w++) {
            for (int h = 0; h < Display.height / 4; h++) {
                Grid.agrid[w][h] = new BigCell(w, h);
            }
        }
        game.startUpdateThread();
        timer.start();
        dfps.start();

        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "");
        setCursor(blankCursor);

        setKeyBindings();
    }

    static void makeSmall() {
        //img_scale = 1;
        //cell_w = cell_h = 0;
        scale = 1;
        img = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_4BYTE_ABGR);
        b2d = img.createGraphics();
        small = true;
        MainWindow.window.resize();
    }

    public static void makeLarge() {
        //img_scale = 2;
        //cell_w = cell_h = 1;
        scale = 2;
        img = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_4BYTE_ABGR);
        b2d = img.createGraphics();
        small = false;
        MainWindow.window.resize();
    }

    static void setView(int i) {
        if (i == 0) {
            view = 0;
            viewName = "Default";
        }
        if (i == 1) {
            view = 1;
            viewName = "Temperature";
        }
        if (i == 2) {
            view = 2;
            viewName = "Life Gradient";
        }
        if (i == 3) {
            view = 3;
            viewName = "Fancy";
        }
    }

    static void toggle_size() {
        if (small) {
            makeLarge();
        } else {
            makeSmall();
        }
    }

    static void toggle_pause() {
        Game.paused = !Game.paused;
        MainWindow.menub.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        w2d = (Graphics2D) g;
        b2d.setColor(Color.BLACK);
        b2d.fillRect(0, 0, getWidth(), getHeight());

        for (int w = 0; w < Grid.agrid.length; w++) {
            for (int h = 0; h < Grid.agrid[0].length; h++) {
                draw_bigcell(Grid.bigcell(w, h));
            }
        }
        size = 0;
        nsize = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                draw_cell(Grid.cell(w, h));
            }
        }

        b2d.setColor(Color.LIGHT_GRAY);
        int sx = mstart.x * scale;
        int w = (mstop.x - mstart.x) * scale;
        int sy = mstart.y * scale;
        int h = (mstop.y - mstart.y) * scale;
        if (mouse_square) {
            b2d.drawRect(sx, sy, w, h); // Size
            b2d.setColor(new Color(244, 244, 244, 32));
            b2d.fillRect(sx, sy, w, h); // Size overlay
        } else {
            b2d.drawOval(sx, sy, w, h); // Size
            b2d.setColor(new Color(244, 244, 244, 32));
            b2d.fillOval(sx, sy, w, h); // Size overlay
        }
        int mx = sx + w / 2;
        int my = sy + h / 2;
        b2d.drawRect(mx, my, scale - 1, scale - 1); // Center Dot

        // Edge-lines to find mouse location.
        b2d.drawLine(mx, 0, mx, 4);
        b2d.drawLine(mx, getHeight() - 4, mx, getHeight());
        b2d.drawLine(0, my, 4, my);
        b2d.drawLine(getWidth() - 4, my, getWidth(), my);

        w2d.drawImage(img, null, 0, 0);
        if (hud) {
            w2d.setColor(Color.WHITE);
            w2d.setXORMode(Color.BLACK);
            w2d.setFont(typeface);
            int line = 1;
            int spacing = w2d.getFontMetrics().getHeight();
            w2d.drawString("FPS    " + dfps.fps() + ", UPS    " + Game.gfps.fps(), 5, spacing * line++);
            w2d.drawString("Parts             " + size, 5, spacing * line++);
            w2d.drawString("Null Stack-Cells  " + nsize, 5, spacing * line++); // As in nulls within a Cell's stack[]
            w2d.drawString(left.name + " || " + right.name, 5, spacing * line++);
            if (help) {
                w2d.drawString("", 5, spacing * line++);
                w2d.drawString("KEY      ACTION         STATE", 5, spacing * line++);
                w2d.drawString("T        mouse type     " + (mouse_square ? "Square" : "Circle"), 5, spacing * line++);
                w2d.drawString("F        single frame   ", 5, spacing * line++);
                w2d.drawString("H        toggle hud     ", 5, spacing * line++);
                w2d.drawString("[ ]      mouse size     " + draw_size, 5, spacing * line++);
                w2d.drawString("SPACE    toggle pause   " + (Game.paused ? "Paused" : "Playing"), 5, spacing * line++);
                w2d.drawString("S        window size    " + (small ? "Default" : "Large"), 5, spacing * line++);
                w2d.drawString("1-3      display type   " + viewName, 5, spacing * line++);
            }


            w2d.drawString("X:" + mouse.x + " Y:" + mouse.y, 5, getHeight() - 25);
            Particle p;
            String info = "Empty";
            if ((p = Grid.getStackTop(mouse.x, mouse.y)) != null) {
                if (p.el != null) {
                    info = p.el.name;
                    if (!(p.ctype == 0) && Elements.exists(p.ctype)) {
                        info += "(" + Elements.get(p.ctype) + ")";
                    }
                    info += ", Temp:" + p.temp();
                    info += ", Life:" + p.life;
                    info += ", TMP:" + p.tmp;
                } else {
                    p.setRemove(true);
                }
            }
            w2d.drawString(info, 5, getHeight() - 10);
        }
        dfps.add();
    }

    public void draw_cell(Cell c) {
        Particle p;
        if (!c.empty() && (p = Grid.getStackTop(c.x, c.y)) != null && p.display()) {
            size += c.count();
            Color col = p.getColor();
            b2d.setColor(col);
            b2d.fillRect(c.screen_x(), c.screen_y(), scale, scale);
            if (view == 3 && p.el.glow) { // "Fancy" Display; not great on fps
                b2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 64));
                int s = scale; // Small flicker
                b2d.fillRect(c.screen_x() - s, c.screen_y() - s, scale + s * 2, scale + s * 2);
            }
        }
        nsize += c.null_count();
    }

    public void draw_bigcell(BigCell c) {
        if (c.wall == null) {
            return;
        }
        b2d.setColor(c.wall.color);
        b2d.fillRect(c.screen_x(), c.screen_y(), (scale) * 4, (scale) * 4);
    }

    public void place(Item e, Point pt, int size) {
        if (pt == null) {
            return;
        }
        Point start = new Point(pt.x - size / 2, pt.y - size / 2);
        Point end = new Point(start.x + size, start.y + size);
        for (int x = start.x; x <= end.x; x++) {
            for (int y = start.y; y <= end.y; y++) {
                if (mouse_square) {
                    placeAt(e, x, y);
                } else {
                    if (Math.sqrt(Math.pow(x - pt.x, 2) + Math.pow(y - pt.y, 2)) <= (double) size / 2) {
                        placeAt(e, x, y);
                    }
                }
            }
        }
    }

    public void placeAt(Item e, int x, int y) {
        if (e instanceof Element && Grid.valid(x, y, 0)) {
            Element el = (Element) e;
            Cell c = Grid.cell(x, y);
            Particle p = Grid.getStackTop(x, y);
            if (el == Elements.none) {
                Grid.remStackTop(x, y);
            } else if (e == Elements.sprk) {
                if (p != null && p.el.conducts) {
                    p.ctype = p.el.id;
                    p.morph(Elements.sprk, Particle.MORPH_KEEP_TEMP, true);
                }
            } else if (p != null && el != Elements.clne && p.el == Elements.clne) {
                p.ctype = el.id;
            } else if (c.addable(el)) {
                Grid.cell(x, y).add(el);
            }
        }
        if (e instanceof Wall && Grid.valid_big(x / 4, y / 4, 0)) {
            Wall wl = (Wall) e;
            BigCell bc = Grid.bigcell(x / 4, y / 4);

            if (wl == Walls.none) {
                bc.wall = null;
            } else {
                bc.wall = wl;
            }
        }
    }

    public Point mouseToCell(Point p) {
        return new Point(p.x / scale, p.y / scale);
    }

    public Point mouseToBigCell(Point p) {
        return new Point(p.x / scale / 4, p.y / scale / 4);
    }

    public void updateMouse(Point p) {
        mouse = p;
        mstart = new Point(mouse.x - draw_size / 2, mouse.y - draw_size / 2);
        mstop = new Point(mstart.x + draw_size, mstart.y + draw_size);
    }

    public void mouseDragged(MouseEvent e) {
        mouse_drag = mouse;
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        updateMouse(mouseToCell(e.getPoint()));
        if (SwingUtilities.isLeftMouseButton(e)) {
            for (Point p : Grid.line(mouse_drag, mouse)) {
                place(left, p, draw_size);
            }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            for (Point p : Grid.line(mouse_drag, mouse)) {
                place(right, p, draw_size);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        updateMouse(mouseToCell(e.getPoint()));
    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            place(left, mouse, draw_size);
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            place(right, mouse, draw_size);
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
            Particle m = Grid.getStackTop(mouse.x, mouse.y);
            if (m != null) {
                left = m.el;
            }
        }
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void keyPressed(KeyEvent e) {

    }

    public void keyReleased(KeyEvent e) {

    }

    public void keyTyped(KeyEvent e) {
        // Broken with focus issues.
    }

    @SuppressWarnings("serial")
    public void setKeyBindings() {
        addKeyBinding(KeyEvent.VK_SPACE, "pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Display.toggle_pause();
                if (Game.paused) {
                    for (int w = 0; w < width; w++) {
                        for (int h = 0; h < height; h++) {
                            Grid.cell(w, h).cleanStack();
                        }
                    }
                }
            }
        });
        addKeyBinding(KeyEvent.VK_S, "resize", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Display.toggle_size();
            }
        });
        addKeyBinding(KeyEvent.VK_F, "frame", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                Game.paused = true;
                Game.update();
            }
        });
        addKeyBinding(KeyEvent.VK_OPEN_BRACKET, "mouse_small", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                draw_size -= 2;
                updateMouse(mouse);
                if (draw_size < 0) {
                    draw_size = 0;
                }
            }
        });
        addKeyBinding(KeyEvent.VK_CLOSE_BRACKET, "mouse_big", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                draw_size += 2;
                updateMouse(mouse);
                if (draw_size < 0) {
                    draw_size = 0;
                }
            }
        });
        addKeyBinding(KeyEvent.VK_H, "hud", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hud = !hud;
            }
        });
        addKeyBinding(KeyEvent.VK_1, "view1", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setView(0);
            }
        });
        addKeyBinding(KeyEvent.VK_2, "view2", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setView(1);
            }
        });
        addKeyBinding(KeyEvent.VK_3, "view3", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setView(2);
            }
        });
        addKeyBinding(KeyEvent.VK_4, "view4", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setView(3);
            }
        });
        addKeyBinding(KeyEvent.VK_T, "mouse_shape", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                mouse_square = !mouse_square;
            }
        });
    }

    public void addKeyBinding(int c, String name, Action action) {
        im.put(KeyStroke.getKeyStroke(c, 0), name);
        am.put(name, action);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        draw_size -= e.getWheelRotation();
        if (draw_size < 0) {
            draw_size = 0;
        }
        mstart = new Point(mouse.x - draw_size / 2, mouse.y - draw_size / 2);
        mstop = new Point(mstart.x + draw_size, mstart.y + draw_size);
    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
