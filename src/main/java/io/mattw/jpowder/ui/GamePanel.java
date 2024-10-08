package io.mattw.jpowder.ui;

import io.mattw.jpowder.*;
import io.mattw.jpowder.event.*;
import io.mattw.jpowder.game.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

@Log4j2
@Getter
public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

    public final static int WIDTH = 612; // 612
    public final static int HEIGHT = 384; // 384
    public static int windowScale = 1; // fillRect vs drawRect

    private final Font typeface = new Font("Monospaced", Font.PLAIN, 11);
    private final PerSecondCounter drawFps = new PerSecondCounter();
    private final Timer timer = new Timer(5, this);
    private final GameUpdateThread gameUpdateThread = new GameUpdateThread();
    private final InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    private final ActionMap am = getActionMap();

    private ViewType view = ViewType.FANCY;
    private boolean showHudHelp = false;
    private boolean small = true;
    private Graphics2D hud2d;
    private BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
    private Graphics2D game2d = img.createGraphics();
    private boolean hud = true;
    private Item leftClickType = ElementType.DUST;
    private Item rightClickType = ElementType.NONE;
    private int totalParts = 0;
    private int drawSize = 15;
    private Point mouse = new Point(0, 0);
    private Point mouseDrag = new Point(0, 0);
    private Point mouseStart = new Point(0, 0);
    private Point mouseStop = new Point(0, 0);
    private boolean mouseSquare = false;

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
        totalParts = 0;
        for (int w = 0; w < WIDTH; w++) {
            for (int h = 0; h < HEIGHT; h++) {
                drawCell(Grid.cell(w, h));
            }
        }

        final var cellWidth = windowScale;
        final var cellHeight = windowScale;
        final var sx = mouseStart.x * windowScale;
        final var w = (mouseStop.x - mouseStart.x) * windowScale;
        final var sy = mouseStart.y * windowScale;
        final var h = (mouseStop.y - mouseStart.y) * windowScale;
        if (mouseSquare) {
            game2d.setColor(new Color(244, 244, 244, 32));
            game2d.fillRect(sx, sy, w, h); // Size overlay
            for (int x = mouseStart.x; x <= mouseStop.x; x++) {
                for (int y = mouseStart.y; y <= mouseStop.y; y++) {
                    var edgeOfSquare = x - 1 < mouseStart.x || y - 1 < mouseStart.y || x + 1 > mouseStop.x || y + 1 > mouseStop.y;
                    if (edgeOfSquare) {
                        game2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
                        game2d.setColor(Color.LIGHT_GRAY);
                        game2d.fillRect(x * windowScale, y * windowScale, cellWidth, cellHeight);
                    }
                }
            }
        } else {
            game2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.13f));
            game2d.setColor(new Color(244, 244, 244));
            game2d.fillOval(sx, sy, w, h);
            for (int x = mouseStart.x; x <= mouseStop.x; x++) {
                for (int y = mouseStart.y; y <= mouseStop.y; y++) {
                    var inCircle = isWithinDrawCircle(mouse, x, y);
                    var edgeOfCircle = !isWithinDrawCircle(mouse, x + 1, y) || !isWithinDrawCircle(mouse, x, y + 1) ||
                            !isWithinDrawCircle(mouse, x - 1, y) || !isWithinDrawCircle(mouse, x, y - 1);
                    if (inCircle && edgeOfCircle) {
                        game2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
                        game2d.setColor(Color.LIGHT_GRAY);
                        game2d.fillRect(x * windowScale, y * windowScale, cellWidth, cellHeight);
                    }
                }
            }
        }
        game2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        game2d.setColor(Color.LIGHT_GRAY);

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
            hud2d.drawString("Parts             " + totalParts, 5, spacing * line++);
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
                hud2d.drawString("1-4      display type   " + view.getDisplayName(), 5, spacing * line);
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

    public boolean isWithinDrawCircle(Point centerPoint, int x, int y) {
        return Math.sqrt(Math.pow(x - centerPoint.x, 2) + Math.pow(y - centerPoint.y, 2)) <= (double) drawSize / 2;
    }

    public void drawCell(Cell cell) {
        Particle particle;
        if (!cell.isStackEmpty() && (particle = Grid.getStackTop(cell.getX(), cell.getY())) != null) {
            totalParts += cell.count();

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
                if (mouseSquare || Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2)) <= (double) size / 2) {
                    placeAt(element, x, y);
                }
            }
        }
    }

    public void placeAt(Item mouseClickType, int x, int y) {
        if (mouseClickType instanceof Element && Grid.validCell(x, y, 0)) {
            Element el = (Element) mouseClickType;

            Grid.cell(x, y).placeNewHere(el);
        }
        if (mouseClickType instanceof Wall && Grid.validBigCell(x / 4, y / 4, 0)) {
            Wall wall = (Wall) mouseClickType;
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
                EventBus.getDefault().post(new PauseChangeEvent(!gameUpdateThread.isPaused()));
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
        if (drawSize < 1) {
            drawSize = 1;
        }
        mouseStart = new Point(mouse.x - drawSize / 2, mouse.y - drawSize / 2);
        mouseStop = new Point(mouseStart.x + drawSize, mouseStart.y + drawSize);
    }

    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(this::repaint);
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
