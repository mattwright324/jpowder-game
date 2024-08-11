package io.mattw.jpowder.ui;

import io.mattw.jpowder.*;
import io.mattw.jpowder.event.*;
import io.mattw.jpowder.game.*;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

@Getter
public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    private static final Logger logger = LogManager.getLogger();

    public final static int WIDTH = 612; // 612
    public final static int HEIGHT = 384; // 384
    public static int windowScale = 1; // fillRect vs drawRect

    private boolean showHudHelp = false;
    private boolean small = true;
    private Graphics2D hud2d;
    private BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
    private Graphics2D game2d = img.createGraphics();
    private final Font typeface = new Font("Monospaced", Font.PLAIN, 11);
    private final PerSecondCounter drawFps = new PerSecondCounter();
    private boolean hud = true;
    private Item leftClickType = ElementType.DUST;
    private Item rightClickType = ElementType.NONE;
    public ViewType view = ViewType.DEFAULT;
    private final Timer timer = new Timer(5, this);
    private final GameUpdateThread gameUpdateThread = new GameUpdateThread();
    private int csize = 0;
    private int nsize = 0;
    private int drawSize = 0;
    private Point mouse = new Point(0, 0);
    private Point mouseDrag = new Point(0, 0);
    private Point mouseStart = new Point(0, 0);
    private Point mouseStop = new Point(0, 0);
    private boolean mouseSquare = false;
    private final InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    private final ActionMap am = getActionMap();

    public GamePanel() {
        EventBus.getDefault().register(this);

        Grid.newGame();
        gameUpdateThread.start();
        timer.start();
        drawFps.start();
        scaleSetLarge();

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

    private void scaleSetSmall() {
        windowScale = 1;
        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        game2d = img.createGraphics();
        small = true;
    }

    private void scaleSetLarge() {
        windowScale = 2;
        img = new BufferedImage(WIDTH * windowScale, HEIGHT * windowScale, BufferedImage.TYPE_4BYTE_ABGR);
        game2d = img.createGraphics();
        small = false;
    }

    public void togglePause() {
        boolean paused = gameUpdateThread.isPaused();

        EventBus.getDefault().post(new PauseChangeEvent(!paused));
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
        int sx = mouseStart.x * windowScale;
        int w = (mouseStop.x - mouseStart.x) * windowScale;
        int sy = mouseStart.y * windowScale;
        int h = (mouseStop.y - mouseStart.y) * windowScale;
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
        game2d.drawRect(mx, my, windowScale - 1, windowScale - 1); // Center Dot

        // Edge-lines to find mouse location.
        game2d.drawLine(mx, 0, mx, 4);
        game2d.drawLine(mx, getHeight() - 4, mx, getHeight());
        game2d.drawLine(0, my, 4, my);
        game2d.drawLine(getWidth() - 4, my, getWidth(), my);

        hud2d.drawImage(img, null, 0, 0);
        if (hud) {
            hud2d.setColor(Color.WHITE);
            hud2d.setXORMode(Color.GRAY);
            hud2d.setFont(typeface);
            int line = 1;
            int spacing = hud2d.getFontMetrics().getHeight();
            hud2d.drawString("FPS    " + drawFps.fps() + ", UPS    " + gameUpdateThread.getGameFps().fps(), 5, spacing * line++);
            hud2d.drawString("Parts             " + csize, 5, spacing * line++);
            hud2d.drawString("GamePanel         " + getWidth() + "x" + getHeight(), 5, spacing * line++); // As in nulls within a Cell's stack[]
            hud2d.drawString(leftClickType.getName() + " || " + rightClickType.getName(), 5, spacing * line++);
            if (showHudHelp) {
                hud2d.drawString("", 5, spacing * line++);
                hud2d.drawString("KEY      ACTION         STATE", 5, spacing * line++);
                hud2d.drawString("T        mouse type     " + (mouseSquare ? "Square" : "Circle"), 5, spacing * line++);
                hud2d.drawString("F        single frame   ", 5, spacing * line++);
                hud2d.drawString("H        toggle hud     ", 5, spacing * line++);
                hud2d.drawString("[ ]      mouse size     " + drawSize, 5, spacing * line++);
                hud2d.drawString("SPACE    toggle pause   " + (gameUpdateThread.isPaused() ? "Paused" : "Playing"), 5, spacing * line++);
                hud2d.drawString("S        window size    " + (small ? "Default" : "Large"), 5, spacing * line++);
                hud2d.drawString("1-3      display type   " + view.getDisplayName(), 5, spacing * line);
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

            final var color = particle.getColor(view);
            game2d.setColor(color);
            game2d.fillRect(cell.screenX(), cell.screenY(), windowScale, windowScale);

            if (view == ViewType.FANCY && particle.getEl().isGlow()) { // "Fancy" Display; not great on fps
                game2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 64).brighter());
                var x = cell.screenX() - windowScale;
                var y = cell.screenY() - windowScale;
                var cellWidth = windowScale + windowScale * 2;
                var cellHeight = windowScale + windowScale * 2;
                game2d.fillRect(x, y, cellWidth, cellHeight);
            }
        }
        nsize += cell.nullCount();
    }

    public void drawBigCell(BigCell bigCell) {
        if (bigCell.getWall() == null) {
            return;
        }
        game2d.setColor(bigCell.getWall().getColor());
        game2d.fillRect(bigCell.screenX(), bigCell.screenY(), (windowScale) * 4, (windowScale) * 4);
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
        return new Point(p.x / windowScale, p.y / windowScale);
    }

    public void updateMouse(Point p) {
        mouse = p;
        mouseStart = new Point(mouse.x - drawSize / 2, mouse.y - drawSize / 2);
        mouseStop = new Point(mouseStart.x + drawSize, mouseStart.y + drawSize);
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
        mouseDrag = mouse;
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        updateMouse(mouseToCell(e.getPoint()));
        if (SwingUtilities.isLeftMouseButton(e)) {
            for (Point p : line(mouseDrag, mouse)) {
                place(leftClickType, p, drawSize);
            }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            for (Point p : line(mouseDrag, mouse)) {
                place(rightClickType, p, drawSize);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        updateMouse(mouseToCell(e.getPoint()));
    }

    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            place(leftClickType, mouse, drawSize);
        }
        if (SwingUtilities.isRightMouseButton(e)) {
            place(rightClickType, mouse, drawSize);
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
                togglePause();
                if (gameUpdateThread.isPaused()) {
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
                EventBus.getDefault().post(new ScaleChangeEvent(Scale.TOGGLE));
            }
        });
        addKeyBinding(KeyEvent.VK_F, "frame", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!gameUpdateThread.isPaused()) {
                    EventBus.getDefault().post(new PauseChangeEvent(true));
                }
                EventBus.getDefault().post(new NextFrameEvent());
            }
        });
        addKeyBinding(KeyEvent.VK_OPEN_BRACKET, "mouse_small", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                drawSize -= 2;
                updateMouse(mouse);
                if (drawSize < 0) {
                    drawSize = 0;
                }
            }
        });
        addKeyBinding(KeyEvent.VK_CLOSE_BRACKET, "mouse_big", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                drawSize += 2;
                updateMouse(mouse);
                if (drawSize < 0) {
                    drawSize = 0;
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
                EventBus.getDefault().post(new ViewChangeEvent(ViewType.DEFAULT));
            }
        });
        addKeyBinding(KeyEvent.VK_2, "view2", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                EventBus.getDefault().post(new ViewChangeEvent(ViewType.TEMP));
            }
        });
        addKeyBinding(KeyEvent.VK_3, "view3", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                EventBus.getDefault().post(new ViewChangeEvent(ViewType.LIFE));
            }
        });
        addKeyBinding(KeyEvent.VK_4, "view4", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                EventBus.getDefault().post(new ViewChangeEvent(ViewType.FANCY));
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
        drawSize -= e.getWheelRotation();
        if (drawSize < 0) {
            drawSize = 0;
        }
        mouseStart = new Point(mouse.x - drawSize / 2, mouse.y - drawSize / 2);
        mouseStop = new Point(mouseStart.x + drawSize, mouseStart.y + drawSize);
    }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    @Subscribe
    public void onScaleChangeEvent(ScaleChangeEvent e) {
        var value = e.getScale();
        if (value == Scale.SMALL) {
            scaleSetSmall();
        } else if (value == Scale.LARGE) {
            scaleSetLarge();
        } else if (value == Scale.TOGGLE) {
            if (windowScale == 1) {
                scaleSetLarge();
            } else {
                scaleSetSmall();
            }
        }
    }

    @Subscribe
    public void onViewChangeEvent(ViewChangeEvent e) {
        view = Optional.of(e).map(ViewChangeEvent::getViewType).orElse(ViewType.DEFAULT);
    }

    @Subscribe
    public void onToolSelectionEvent(ToolSelectionEvent e) {
        if (e.getMouseButton() == MouseEvent.BUTTON1) {
            leftClickType = e.getSelectedItem();
        } else if (e.getMouseButton() == MouseEvent.BUTTON2) {
            rightClickType = e.getSelectedItem();
        }
    }

    @Subscribe
    public void onHelpChangeEvent(HelpChangeEvent e) {
        showHudHelp = e.isDisplay();
    }

}
