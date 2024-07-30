package io.mattw.jpowder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerSecondCounter extends Thread {

    private double seconds = 0;
    private long total = 0;
    private double avg = 0;

    private long count = 0;
    private long fps = 0;
    private long lastFps = System.currentTimeMillis();

    public void add() {
        count++;
    }

    public void run() {
        while (isAlive()) {
            if (System.currentTimeMillis() - lastFps > 1000) {
                seconds += (System.currentTimeMillis() - lastFps) / 1000.0;

                fps = count;
                count = 0;
                lastFps = System.currentTimeMillis();

                total += fps;
                avg = total / seconds;
                if (seconds > 60) {
                    resetAverage();
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public long fps() {
        return fps;
    }

    public void resetAverage() {
        seconds = 0;
        total = 0;
        avg = 0;
    }

    public double average() {
        return Math.round(avg * 100.0) / 100.0;
    }
}
