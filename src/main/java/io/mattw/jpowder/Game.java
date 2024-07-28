package io.mattw.jpowder;

public class Game extends Thread {

    public static final int MAX_AIR = 256;
    private static final int MIN_AIR = -256;
    private static final int MAX_PARTS = Display.WIDTH * Display.HEIGHT;
    public static boolean paused = false;
    public static final Counter gameFps = new Counter();

    static void update() {
        for (int w = 0; w < Display.WIDTH; w++) {
            for (int h = 0; h < Display.HEIGHT; h++) {
                Grid.cell(w, h).update();
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
