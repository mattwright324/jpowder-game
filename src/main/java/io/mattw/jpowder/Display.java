package io.mattw.jpowder;

import io.mattw.jpowder.elements.Element;
import io.mattw.jpowder.elements.Elements;
import io.mattw.jpowder.particles.Particle;
import io.mattw.jpowder.walls.Wall;
import io.mattw.jpowder.walls.Walls;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

@Getter
@Setter
public class Display extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    public final static int width = 612; // 612
    public final static int height = 384; // 384
    public static int view = 0;
    public static int scale = 1; // fillRect vs drawRect
    public static boolean help = false;

    private static boolean small = true;
    private static Graphics2D w2d;
    private static BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    private static Graphics2D b2d = img.createGraphics();
    private static Font typeface = new Font("Monospaced", Font.PLAIN, 11);
    public static Item left = Elements.dust; // Hacky as fuck.
    public static Item right = Elements.none;
    private static Counter dfps = new Counter();
    private static String viewName = "Default";
    private static boolean hud = true;
    private Timer timer = new Timer(5, this);
    private Point mouse = new Point(0, 0);
    private Point mouse_drag = new Point(0, 0);
    private Game game = new Game();
    private int csize = 0, nsize = 0;
    private int draw_size = 0;
    private Point mstart = new Point(0, 0), mstop = new Point(0, 0);
    private boolean mouse_square = false;
    private InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    private ActionMap am = getActionMap();

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

        var cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_4BYTE_ABGR);
        var blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "");
        setCursor(blankCursor);

        setKeyBindings();
    }

    public static void makeSmall() {
        scale = 1;
        img = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_4BYTE_ABGR);
        b2d = img.createGraphics();
        small = true;
        MainWindow.window.resize();
    }

    public static void makeLarge() {
        scale = 2;
        img = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_4BYTE_ABGR);
        b2d = img.createGraphics();
        small = false;
        MainWindow.window.resize();
    }

    public static void setView(int i) {
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

    public static void toggle_size() {
        if (small) {
            makeLarge();
        } else {
            makeSmall();
        }
    }

    public static void togglePause() {
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
                drawBigCell(Grid.bigcell(w, h));
            }
        }
        csize = 0;
        nsize = 0;
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                drawCell(Grid.cell(w, h));
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
            w2d.drawString("Parts             " + csize, 5, spacing * line++);
            w2d.drawString("Null Stack-Cells  " + nsize, 5, spacing * line++); // As in nulls within a Cell's stack[]
            w2d.drawString(left.getName() + " || " + right.getName(), 5, spacing * line++);
            if (help) {
                w2d.drawString("", 5, spacing * line++);
                w2d.drawString("KEY      ACTION         STATE", 5, spacing * line++);
                w2d.drawString("T        mouse type     " + (mouse_square ? "Square" : "Circle"), 5, spacing * line++);
                w2d.drawString("F        single frame   ", 5, spacing * line++);
                w2d.drawString("H        toggle hud     ", 5, spacing * line++);
                w2d.drawString("[ ]      mouse size     " + draw_size, 5, spacing * line++);
                w2d.drawString("SPACE    toggle pause   " + (Game.paused ? "Paused" : "Playing"), 5, spacing * line++);
                w2d.drawString("S        window size    " + (small ? "Default" : "Large"), 5, spacing * line++);
                w2d.drawString("1-3      display type   " + viewName, 5, spacing * line);
            }


            w2d.drawString("X:" + mouse.x + " Y:" + mouse.y, 5, getHeight() - 25);
            Particle p;
            String info = "Empty";
            if ((p = Grid.getStackTop(mouse.x, mouse.y)) != null) {
                if (p.getEl() != null) {
                    info = p.getEl().getName();
                    if (!(p.getCtype() == 0) && Elements.exists(p.getCtype())) {
                        info += "(" + Elements.get(p.getCtype()) + ")";
                    }
                    info += ", Temp:" + p.temp();
                    info += ", Life:" + p.getLife();
                    info += ", TMP:" + p.getTmp();
                } else {
                    p.setRemove(true);
                }
            }
            w2d.drawString(info, 5, getHeight() - 10);
        }
        dfps.add();
    }

    public void drawCell(Cell c) {
        Particle p;
        if (!c.empty() && (p = Grid.getStackTop(c.getX(), c.getY())) != null && p.display()) {
            csize += c.count();
            Color col = p.getColor();
            b2d.setColor(col);
            b2d.fillRect(c.screenX(), c.screenY(), scale, scale);
            if (view == 3 && p.getEl().isGlow()) { // "Fancy" Display; not great on fps
                b2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 64));
                int s = scale; // Small flicker
                b2d.fillRect(c.screenX() - s, c.screenY() - s, scale + s * 2, scale + s * 2);
            }
        }
        nsize += c.nullCount();
    }

    public void drawBigCell(BigCell c) {
        if (c.getWall() == null) {
            return;
        }
        b2d.setColor(c.getWall().getColor());
        b2d.fillRect(c.screenX(), c.screenY(), (scale) * 4, (scale) * 4);
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
                if (p != null && p.getEl().isConducts()) {
                    p.setCtype(p.getEl().getId());
                    p.morph(Elements.sprk, Particle.MORPH_KEEP_TEMP, true);
                }
            } else if (p != null && el != Elements.clne && p.getEl() == Elements.clne) {
                p.setCtype(el.getId());
            } else if (c.addable(el)) {
                Grid.cell(x, y).add(el);
            }
        }
        if (e instanceof Wall && Grid.valid_big(x / 4, y / 4, 0)) {
            Wall wl = (Wall) e;
            BigCell bc = Grid.bigcell(x / 4, y / 4);

            if (wl == Walls.none) {
                bc.setWall(null);
            } else {
                bc.setWall(wl);
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
                left = m.getEl();
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
                Display.togglePause();
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
