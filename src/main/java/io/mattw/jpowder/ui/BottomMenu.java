package io.mattw.jpowder.ui;

import io.mattw.jpowder.event.*;
import io.mattw.jpowder.game.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Log4j2
public class BottomMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    public static final int WIDTH = GamePanel.WIDTH + SideMenu.WIDTH;
    public static final int HEIGHT = 50;

    private final int btnWidth = 40;
    private final int btnHeight = 18;
    private final int btnPosY = HEIGHT - btnHeight - 5;
    private final Rectangle clearRect = new Rectangle(5, btnPosY, btnWidth, btnHeight);
    private final Rectangle resizeRect = new Rectangle(5 + (btnWidth + 5), btnPosY, btnWidth, btnHeight);
    private final Rectangle pauseRect = new Rectangle(5 + (btnWidth + 5) * 2, btnPosY, btnWidth, btnHeight);
    private final Rectangle viewRect = new Rectangle(5 + (btnWidth + 5) * 3, btnPosY, btnWidth, btnHeight);
    private final Rectangle helpRect = new Rectangle(5 + (btnWidth + 5) * 4, btnPosY, btnWidth, btnHeight);
    private final List<Button> buttons = new ArrayList<>();
    private final GamePanel game;

    private Graphics2D graphics;
    private Point mouse = new Point(0, 0);

    public BottomMenu(GamePanel game) {
        EventBus.getDefault().register(this);

        this.game = game;
        setFocusable(true);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        graphics = (Graphics2D) g;

        graphics.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, HEIGHT, Color.WHITE));
        graphics.fillRect(0, 0, GamePanel.WIDTH * 2 + SideMenu.WIDTH, HEIGHT);

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
        if (game.getGameUpdateThread().isPaused()) {
            graphics.setColor(new Color(255, 64, 128, 128));
        }
        graphics.fill(pauseRect);
        createButtons();
        drawButtons();
        graphics.setColor(Color.WHITE);
        int b_txt_center = btnPosY + btnHeight / 2 + 5;
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
            EventBus.getDefault().post(new NewGameEvent());
        }
        if (resizeRect.contains(mouse)) {
            EventBus.getDefault().post(new ScaleChangeEvent(Scale.TOGGLE));
        }
        if (pauseRect.contains(mouse)) {
            boolean paused = game.getGameUpdateThread().isPaused();
            EventBus.getDefault().post(new PauseChangeEvent(!paused));
        }
        if (viewRect.contains(mouse)) {
            EventBus.getDefault().post(new ViewChangeEvent(game.getView().next()));
        }
        for (Button b : buttons) {
            if (b.contains(mouse)) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    EventBus.getDefault().post(new ToolSelectionEvent(b.getItem(), MouseEvent.BUTTON1));
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    EventBus.getDefault().post(new ToolSelectionEvent(b.getItem(), MouseEvent.BUTTON2));
                }
            }
        }
        if (helpRect.contains(mouse)) {
            EventBus.getDefault().post(new HelpChangeEvent(!game.isShowHudHelp()));
        }
    }

    public void createButtons() {
        buttons.clear();
        int i = 0;
        for (Item item : SideMenu.selectedCategory.getItems()) {
            int x = MainWindow.mouse.x;
            if (x > MainWindow.window.getWidth() / 2) {
                x = MainWindow.window.getWidth() / 2;
            }
            Button button = new Button(getWidth() - btnWidth - (5 + (btnWidth + 5) * i++) + (getWidth() - x - (getWidth() / 2)), 5, btnWidth, btnHeight);
            button.setItem(item);
            buttons.add(button);
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
            if (b.getItem() == game.getLeftClickType()) {
                graphics.setColor(new Color(255, 0, 0));
                graphics.drawRect(b.x, b.y, b.width, b.height);
            }
            if (b.getItem() == game.getRightClickType()) {
                graphics.setColor(new Color(0, 0, 255));
                graphics.drawRect(b.x, b.y, b.width, b.height);
            }
            graphics.setColor(Color.WHITE);
            graphics.drawString(b.getItem().getName(), b.x + 2, b.y + btnHeight / 2 + 5);
        }
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void actionPerformed(ActionEvent e) {
        repaintLater();
    }

    public void mouseDragged(MouseEvent e) {
        mouse = e.getPoint();
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        repaintLater();
    }

    public void mouseMoved(MouseEvent e) {
        mouse = e.getPoint();
        MainWindow.updateMouseInFrame(e.getPoint(), this);
        repaintLater();
    }

    @Subscribe
    public void onPauseChangeEvent(PauseChangeEvent e) {
        repaintLater();
    }

    @Subscribe(priority = 1)
    public void onToolSelectionEvent(ToolSelectionEvent e) {
        repaintLater();
    }

    @Subscribe
    public void onCategorySelectEvent(CategorySelectEvent e) {
        repaintLater();
    }

    public void repaintLater() {
        SwingUtilities.invokeLater(this::repaint);
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
