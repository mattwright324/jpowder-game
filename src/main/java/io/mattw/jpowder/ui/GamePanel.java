package io.mattw.jpowder.ui;

import io.mattw.jpowder.*;
import io.mattw.jpowder.game.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    public final static int WIDTH = 612; // 612
    public final static int HEIGHT = 384; // 384
    public static int view = 0;
    public static int scale = 1; // fillRect vs drawRect
    public static boolean help = false;

    private static boolean small = true;
    private static Graphics2D hud2d;
    private static BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
    private static Graphics2D game2d = img.createGraphics();
    private static Font typeface = new Font("Monospaced", Font.PLAIN, 11);
    private static PerSecondCounter drawFps = new PerSecondCounter();
    private static String viewName = "Default";
    private static boolean hud = true;

    public static Item leftClickType = ElementType.DUST; // Hacky as fuck.
    public static Item rightClickType = ElementType.NONE;

    private Timer timer = new Timer(5, this);
    private Point mouse = new Point(0, 0);
    private Point mouse_drag = new Point(0, 0);
    private GameThread game = new GameThread();
    private int csize = 0, nsize = 0;
    private int draw_size = 0;
    private Point mstart = new Point(0, 0), mstop = new Point(0, 0);
    private boolean mouseSquare = false;
    private InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    private ActionMap am = getActionMap();

    public GamePanel() {
        for (int w = 0; w < GamePanel.WIDTH; w++) {
            for (int h = 0; h < GamePanel.HEIGHT; h++) {
                Grid.PART_GRID[w][h] = new Cell(w, h);
            }
        }
        for (int w = 0; w < GamePanel.WIDTH / 4; w++) {
            for (int h = 0; h < GamePanel.HEIGHT / 4; h++) {
                Grid.BIG_GRID[w][h] = new BigCell(w, h);
            }
        }
        game.startUpdateThread();
        timer.start();
        drawFps.start();

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
        img = new BufferedImage(WIDTH * scale, HEIGHT * scale, BufferedImage.TYPE_4BYTE_ABGR);
        game2d = img.createGraphics();
        small = true;
        MainWindow.window.resize();
    }

    public static void makeLarge() {
        scale = 2;
        img = new BufferedImage(WIDTH * scale, HEIGHT * scale, BufferedImage.TYPE_4BYTE_ABGR);
        game2d = img.createGraphics();
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
        GameThread.paused = !GameThread.paused;
        MainWindow.bottomMenu.repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        hud2d = (Graphics2D) g;
        game2d.setColor(Color.BLACK);
        game2d.fillRect(0, 0, getWidth(), getHeight());

        for (int w = 0; w < Grid.BIG_GRID.length; w++) {
            for (int h = 0; h < Grid.BIG_GRID[0].length; h++) {
                drawBigCell(Grid.bigcell(w, h));
            }
        }
        csize = 0;
        nsize = 0;
        for (int w = 0; w < WIDTH; w++) {
            for (int h = 0; h < HEIGHT; h++) {
                drawCell(Grid.cell(w, h));
            }
        }

        game2d.setColor(Color.LIGHT_GRAY);
        int sx = mstart.x * scale;
        int w = (mstop.x - mstart.x) * scale;
        int sy = mstart.y * scale;
        int h = (mstop.y - mstart.y) * scale;
        if (mouseSquare) {
            game2d.drawRect(sx, sy, w, h); // Size
            game2d.setColor(new Color(244, 244, 244, 32));
            game2d.fillRect(sx, sy, w, h); // Size overlay
        } else {
            game2d.drawOval(sx, sy, w, h); // Size
            game2d.setColor(new Color(244, 244, 244, 32));
            game2d.fillOval(sx, sy, w, h); // Size overlay
        }
        int mx = sx + w / 2;
        int my = sy + h / 2;
        game2d.drawRect(mx, my, scale - 1, scale - 1); // Center Dot

        // Edge-lines to find mouse location.
        game2d.drawLine(mx, 0, mx, 4);
        game2d.drawLine(mx, getHeight() - 4, mx, getHeight());
        game2d.drawLine(0, my, 4, my);
        game2d.drawLine(getWidth() - 4, my, getWidth(), my);

        hud2d.drawImage(img, null, 0, 0);
        if (hud) {
            hud2d.setColor(Color.WHITE);
            hud2d.setXORMode(Color.BLACK);
            hud2d.setFont(typeface);
            int line = 1;
            int spacing = hud2d.getFontMetrics().getHeight();
            hud2d.drawString("FPS    " + drawFps.fps() + ", UPS    " + GameThread.gameFps.fps(), 5, spacing * line++);
            hud2d.drawString("Parts             " + csize, 5, spacing * line++);
            hud2d.drawString("Null Stack-Cells  " + nsize, 5, spacing * line++); // As in nulls within a Cell's stack[]
            hud2d.drawString(leftClickType.getName() + " || " + rightClickType.getName(), 5, spacing * line++);
            if (help) {
                hud2d.drawString("", 5, spacing * line++);
                hud2d.drawString("KEY      ACTION         STATE", 5, spacing * line++);
                hud2d.drawString("T        mouse type     " + (mouseSquare ? "Square" : "Circle"), 5, spacing * line++);
                hud2d.drawString("F        single frame   ", 5, spacing * line++);
                hud2d.drawString("H        toggle hud     ", 5, spacing * line++);
                hud2d.drawString("[ ]      mouse size     " + draw_size, 5, spacing * line++);
                hud2d.drawString("SPACE    toggle pause   " + (GameThread.paused ? "Paused" : "Playing"), 5, spacing * line++);
                hud2d.drawString("S        window size    " + (small ? "Default" : "Large"), 5, spacing * line++);
                hud2d.drawString("1-3      display type   " + viewName, 5, spacing * line);
            }

            hud2d.drawString("X:" + mouse.x + " Y:" + mouse.y, 5, getHeight() - 25);
            Particle particle;
            String info = "Empty";
            if ((particle = Grid.getStackTop(mouse.x, mouse.y)) != null) {
                if (particle.getEl() != null) {
                    info = particle.getEl().getName();
                    if (!(particle.getCtype() == 0) && ElementType.exists(particle.getCtype())) {
                        info += "(" + ElementType.get(particle.getCtype()) + ")";
                    }
                    info += ", Temp:" + particle.temp();
                    info += ", Life:" + particle.getLife();
                    info += ", TMP:" + particle.getTmp();
                } else {
                    particle.setRemove(true);
                }
            }
            hud2d.drawString(info, 5, getHeight() - 10);
        }
        drawFps.add();
    }

    public void drawCell(Cell cell) {
        Particle particle;
        if (!cell.empty() && (particle = Grid.getStackTop(cell.getX(), cell.getY())) != null && particle.display()) {
            csize += cell.count();
            Color col = particle.getColor();
            game2d.setColor(col);
            game2d.fillRect(cell.screenX(), cell.screenY(), scale, scale);
            if (view == 3 && particle.getEl().isGlow()) { // "Fancy" Display; not great on fps
                game2d.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 64));
                game2d.fillRect(cell.screenX() - scale, cell.screenY() - scale, scale + scale * 2, scale + scale * 2);
            }
        }
        nsize += cell.nullCount();
    }

    public void drawBigCell(BigCell bigCell) {
        if (bigCell.getWall() == null) {
            return;
        }
        game2d.setColor(bigCell.getWall().getColor());
        game2d.fillRect(bigCell.screenX(), bigCell.screenY(), (scale) * 4, (scale) * 4);
    }

    public void place(Item element, Point point, int size) {
        if (point == null) {
            return;
        }
        Point start = new Point(point.x - size / 2, point.y - size / 2);
        Point end = new Point(start.x + size, start.y + size);
        for (int x = start.x; x <= end.x; x++) {
            for (int y = start.y; y <= end.y; y++) {
                if (mouseSquare) {
                    placeAt(element, x, y);
                } else {
                    if (Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2)) <= (double) size / 2) {
                        placeAt(element, x, y);
                    }
                }
            }
        }
    }

    public void placeAt(Item element, int x, int y) {
        if (element instanceof Element && Grid.validCell(x, y, 0)) {
            Element el = (Element) element;
            Cell cell = Grid.cell(x, y);
            Particle particle = Grid.getStackTop(x, y);
            if (el == ElementType.NONE) {
                Grid.remStackTop(x, y);
            } else if (element == ElementType.SPRK) {
                if (particle != null && particle.getEl().isConducts()) {
                    particle.setCtype(particle.getEl().getId());
                    particle.morph(ElementType.SPRK, Particle.MORPH_KEEP_TEMP, true);
                }
            } else if (particle != null && el != ElementType.CLNE && particle.getEl() == ElementType.CLNE) {
                particle.setCtype(el.getId());
            } else if (cell.addable(el)) {
                Grid.cell(x, y).add(el);
            }
        }
        if (element instanceof Wall && Grid.validBigCell(x / 4, y / 4, 0)) {
            Wall wall = (Wall) element;
            BigCell bigCell = Grid.bigcell(x / 4, y / 4);

            if (wall == WallType.NONE) {
                bigCell.setWall(null);
            } else {
                bigCell.setWall(wall);
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

    /**
     * Bresenham's Line Algorithm
     * Used to fill spacing between mouse drags.
     */
    public Point[] line(Point a, Point b) {
        var pts = new Point[0];
        var dx = Math.abs(b.x - a.x);
        var dy = Math.abs(b.y - a.y);
        var sx = a.x < b.x ? 1 : -1;
        var sy = a.y < b.y ? 1 : -1;
        var err = dx - dy;
        while (a.x != b.x || a.y != b.y) {
            var e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                a.x = a.x + sx;
            }
            if (e2 < dx) {
                err = err + dx;
                a.y = a.y + sy;
            }
            pts = Arrays.copyOf(pts, pts.length + 1);
            pts[pts.length - 1] = new Point(a.x, a.y);
        }
        return pts;
    }

    public void mouseDragged(MouseEvent e) {
        mouse_drag = mouse;
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        updateMouse(mouseToCell(e.getPoint()));
        if (SwingUtilities.isLeftMouseButton(e)) {
            for (Point p : line(mouse_drag, mouse)) {
                place(leftClickType, p, draw_size);
            }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            for (Point p : line(mouse_drag, mouse)) {
                place(rightClickType, p, draw_size);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        updateMouse(mouseToCell(e.getPoint()));
    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            place(leftClickType, mouse, draw_size);
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            place(rightClickType, mouse, draw_size);
        }
        if (SwingUtilities.isMiddleMouseButton(e)) {
            Particle m = Grid.getStackTop(mouse.x, mouse.y);
            if (m != null) {
                leftClickType = m.getEl();
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
                GamePanel.togglePause();
                if (GameThread.paused) {
                    for (int w = 0; w < WIDTH; w++) {
                        for (int h = 0; h < HEIGHT; h++) {
                            Grid.cell(w, h).cleanStack();
                        }
                    }
                }
            }
        });
        addKeyBinding(KeyEvent.VK_S, "resize", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                GamePanel.toggle_size();
            }
        });
        addKeyBinding(KeyEvent.VK_F, "frame", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                GameThread.paused = true;
                GameThread.update();
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
                mouseSquare = !mouseSquare;
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
