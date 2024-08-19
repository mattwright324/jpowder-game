package io.mattw.jpowder.game;

import io.mattw.jpowder.PerSecondCounter;
import io.mattw.jpowder.event.NewGameEvent;
import io.mattw.jpowder.event.NextFrameEvent;
import io.mattw.jpowder.event.PauseChangeEvent;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Log4j2
@Getter
public class GameUpdateThread extends Thread {

    private final static long FRAME_NS = (long) ((1000 / 40.0) * 1000000);

    private final List<Particle> partsToAdd = new CopyOnWriteArrayList<>();
    private final List<Particle> updateParticles = new CopyOnWriteArrayList<>();
    private final PerSecondCounter gameFps = new PerSecondCounter();

    private boolean paused = false;

    public GameUpdateThread() {
        EventBus.getDefault().register(this);

        log.debug("GameUpdateThread started [FRAME_NS={}]", FRAME_NS);
    }

    private void update() {
        updateParticles.addAll(partsToAdd);
        partsToAdd.clear();
        updateParticles.removeIf(part -> part == null || part.remove());

        final var updateId = UUID.randomUUID().toString();
        updateParticles.forEach(part -> {
            if (!updateId.equals(part.getLastUpdateId())) {
                part.update(updateId);
            }
        });

        gameFps.add();
    }

    public void run() {
        gameFps.start();

        while (isAlive()) {
            var startTime = System.nanoTime();
            if (Grid.cell(0, 0) != null && !paused) {
                try {
                    update();
                } catch (Exception e) {
                    log.error("Failed to update particles", e);
                    log.error(e);
                }
            }

            var diffNanos = System.nanoTime() - startTime;
            if (diffNanos > FRAME_NS) {
                diffNanos = 0;
            } else {
                diffNanos = FRAME_NS - diffNanos;
            }

            try {
                Thread.sleep(diffNanos / 1000000, (int) diffNanos % 999999);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Subscribe(priority = 1)
    public void onNewGameEvent(NewGameEvent e) {
        e.setInitialPauseState(paused);
        paused = true;
        update();
    }

    @Subscribe(priority = 2)
    public void onNewGameEvent2(NewGameEvent e) {
        partsToAdd.clear();
        updateParticles.clear();
        update();
        EventBus.getDefault().post(new PauseChangeEvent(e.isInitialPauseState()));
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
