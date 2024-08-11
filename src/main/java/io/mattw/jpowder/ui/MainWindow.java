package io.mattw.jpowder.ui;

import com.formdev.flatlaf.FlatLaf;
import io.mattw.jpowder.event.*;
import io.mattw.jpowder.game.Grid;
import io.mattw.jpowder.game.ViewType;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Getter
public class MainWindow extends JFrame {

    private static final Logger logger = LogManager.getLogger();

    public static BufferedImage HEAT_COLOR_STRIP;
    public static MainWindow window;
    public static Point mouse = new Point(0, 0);

    private final GamePanel gamePanel;
    private final SideMenu sideMenu;
    public static BottomMenu bottomMenu;

    private final JCheckBoxMenuItem keybindHelp;
    private final JRadioButtonMenuItem defaultView;
    private final JRadioButtonMenuItem tempView;
    private final JRadioButtonMenuItem lifeGradientView;
    private final JRadioButtonMenuItem fancyView;

    private final JCheckBoxMenuItem pauseGame;

    public MainWindow() throws Exception {
        logger.trace("MainWindow()");

        EventBus.getDefault().register(this);

        var icon = ImageIO.read(Objects.requireNonNull(getClass().getResource("/icon.png")));
        HEAT_COLOR_STRIP = ImageIO.read(Objects.requireNonNull(getClass().getResource("/heat-color-strip.png")));

        window = this;
        setIconImage(icon);
        setTitle("JPowder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);

        var centerX = (Toolkit.getDefaultToolkit().getScreenSize().width - (GamePanel.WIDTH * 2 + SideMenu.WIDTH)) / 2;
        var centerY = (Toolkit.getDefaultToolkit().getScreenSize().height - (GamePanel.HEIGHT * 2 + BottomMenu.HEIGHT)) / 2;
        setLocation(centerX, centerY);

        FlatLaf.setGlobalExtraDefaults(Collections.singletonMap("@accentColor", "#0094FF"));

        var newGame = new JMenuItem("New Game");
        newGame.addActionListener(e -> EventBus.getDefault().post(new NewGameEvent()));

        pauseGame = new JCheckBoxMenuItem("Pause Game");

        var exitButton = new JMenuItem("Exit");
        exitButton.addActionListener(e -> System.exit(0));

        var gameMenu = new JMenu("Game");
        gameMenu.add(newGame);
        gameMenu.add(pauseGame);
        gameMenu.add(new JSeparator());
        gameMenu.add(exitButton);

        keybindHelp = new JCheckBoxMenuItem("Show Keybind Help");

        var largeView = new JRadioButtonMenuItem("Large Window (2x)");
        largeView.setSelected(true);
        largeView.addActionListener(e -> EventBus.getDefault().post(new ScaleChangeEvent(Scale.LARGE)));

        var smallView = new JRadioButtonMenuItem("Small Window (1x)");
        smallView.addActionListener(e -> EventBus.getDefault().post(new ScaleChangeEvent(Scale.SMALL)));

        var group1 = new ButtonGroup();
        group1.add(largeView);
        group1.add(smallView);

        defaultView = new JRadioButtonMenuItem("Default View");
        defaultView.setSelected(true);
        defaultView.addActionListener(e -> EventBus.getDefault().post(new ViewChangeEvent(ViewType.DEFAULT)));

        tempView = new JRadioButtonMenuItem("Temp View");
        tempView.addActionListener(e -> EventBus.getDefault().post(new ViewChangeEvent(ViewType.TEMP)));

        lifeGradientView = new JRadioButtonMenuItem("Life Gradient View");
        lifeGradientView.addActionListener(e -> EventBus.getDefault().post(new ViewChangeEvent(ViewType.LIFE)));

        fancyView = new JRadioButtonMenuItem("Fancy View");
        fancyView.addActionListener(e -> EventBus.getDefault().post(new ViewChangeEvent(ViewType.FANCY)));

        var group2 = new ButtonGroup();
        group2.add(defaultView);
        group2.add(tempView);
        group2.add(lifeGradientView);
        group2.add(fancyView);

        var viewMenu = new JMenu("View");
        viewMenu.add(keybindHelp);
        viewMenu.add(new JSeparator());
        viewMenu.add(largeView);
        viewMenu.add(smallView);
        viewMenu.add(new JSeparator());
        viewMenu.add(defaultView);
        viewMenu.add(tempView);
        viewMenu.add(lifeGradientView);
        viewMenu.add(fancyView);

        var menuBar = new JMenuBar();
        menuBar.add(gameMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);

        gamePanel = new GamePanel();
        add(gamePanel, BorderLayout.CENTER);

        pauseGame.addActionListener(e -> {
            boolean paused = gamePanel.getGameUpdateThread().isPaused();
            EventBus.getDefault().post(new PauseChangeEvent(!paused));
        });

        keybindHelp.addActionListener(e -> EventBus.getDefault().post(new HelpChangeEvent(!gamePanel.isShowHudHelp())));

        sideMenu = new SideMenu();
        sideMenu.setPreferredSize(new Dimension(SideMenu.WIDTH, SideMenu.HEIGHT));
        add(sideMenu, BorderLayout.EAST);

        bottomMenu = new BottomMenu(gamePanel);
        bottomMenu.setPreferredSize(new Dimension(BottomMenu.WIDTH, BottomMenu.HEIGHT));
        add(bottomMenu, BorderLayout.SOUTH);

        resize();
    }

    public static void updateMouseInFrame(Point p, Component c) {
        SwingUtilities.convertPointToScreen(p, c);
        SwingUtilities.convertPointFromScreen(p, window);
        mouse = p;
        bottomMenu.repaint();
    }

    public void resize() {
        var scaledWidth = GamePanel.WIDTH * GamePanel.windowScale + SideMenu.WIDTH;
        var scaledHeight = GamePanel.HEIGHT * GamePanel.windowScale + BottomMenu.HEIGHT;
        var size = new Dimension(scaledWidth, scaledHeight);

        this.getContentPane().setPreferredSize(size);
        this.pack();
    }

    @Subscribe
    public void onEvent(Object e) {
        logger.trace("Received {}: {}", e.getClass().getSimpleName(), e);
    }

    @Subscribe
    public void onPauseChangeEvent(PauseChangeEvent e) {
        pauseGame.setSelected(e.isPaused());
    }

    @Subscribe
    public void onNewGameEvent(NewGameEvent e) {
        Grid.newGame();
    }

    @Subscribe
    public void onScaleChangeEvent(ScaleChangeEvent e) {
        SwingUtilities.invokeLater(this::resize);
    }

    @Subscribe
    public void onViewChangeEvent(ViewChangeEvent e) {
        var view = Optional.of(e).map(ViewChangeEvent::getViewType).orElse(ViewType.DEFAULT);

        SwingUtilities.invokeLater(() -> {
            if (view == ViewType.TEMP) {
                tempView.setSelected(true);
            } else if (view == ViewType.LIFE) {
                lifeGradientView.setSelected(true);
            } else if (view == ViewType.FANCY) {
                fancyView.setSelected(true);
            } else {
                defaultView.setSelected(true);
            }
        });
    }

    @Subscribe
    public void onHelpChangeEvent(HelpChangeEvent e) {
        keybindHelp.setSelected(e.isDisplay());
    }

}
