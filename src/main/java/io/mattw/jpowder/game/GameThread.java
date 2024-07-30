package io.mattw.jpowder.game;

import io.mattw.jpowder.PerSecondCounter;
import io.mattw.jpowder.ui.GamePanel;

import java.util.UUID;

public class GameThread extends Thread {

    public static final int MAX_AIR = 256;
    private static final int MIN_AIR = -256;
    private static final int MAX_PARTS = GamePanel.WIDTH * GamePanel.HEIGHT;
    public static boolean paused = false;
    public static final PerSecondCounter gameFps = new PerSecondCounter();

    public static void update() {
        String updateId = UUID.randomUUID().toString();
        for (int w = 0; w < GamePanel.WIDTH; w++) {
            for (int h = 0; h < GamePanel.HEIGHT; h++) {
                Grid.cell(w, h).update(updateId);
            }
        }
        gameFps.add();
    }

    public void startUpdateThread() {
        start();
    }

    public void run() {
        gameFps.start();
        while (isAlive()) {
            if (Grid.cell(0, 0) != null && !paused) {
                update();
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException ignored) {
            }
        }
    }

}
