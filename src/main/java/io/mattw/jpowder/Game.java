package io.mattw.jpowder;

public class Game extends Thread {

    public static final int MAX_AIR = 256;
    private static final int MIN_AIR = -256;
    private static final int MAX_PARTS = Display.width * Display.height;
    public static boolean paused = false;
    public static final Counter gfps = new Counter();

    static void update() {
        for (int w = 0; w < Display.width; w++) {
            for (int h = 0; h < Display.height; h++) {
                Grid.cell(w, h).update();
            }
        }
        gfps.add();
    }

    public void startUpdateThread() {
        start();
    }

    public void run() {
        gfps.start();
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
