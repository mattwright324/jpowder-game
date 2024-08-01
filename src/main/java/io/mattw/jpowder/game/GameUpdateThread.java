package io.mattw.jpowder.game;

import io.mattw.jpowder.PerSecondCounter;
import io.mattw.jpowder.event.NextFrameEvent;
import io.mattw.jpowder.event.PauseChangeEvent;
import io.mattw.jpowder.ui.GamePanel;
import lombok.Getter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.UUID;

@Getter
public class GameUpdateThread extends Thread {

    @Getter
    private boolean paused = false;
    private final PerSecondCounter gameFps = new PerSecondCounter();

    public GameUpdateThread() {
        EventBus.getDefault().register(this);
    }

    private void update() {
        final var updateId = UUID.randomUUID().toString();
        for (int w = 0; w < GamePanel.WIDTH; w++) {
            for (int h = 0; h < GamePanel.HEIGHT; h++) {
                Grid.cell(w, h).update(updateId);
            }
        }
        gameFps.add();
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

    @Subscribe
    public void onPauseChangeEvent(PauseChangeEvent e) {
        paused = e.isPaused();
    }

    @Subscribe
    public void onNextFrameEvent(NextFrameEvent e) {
        update();
    }

}
