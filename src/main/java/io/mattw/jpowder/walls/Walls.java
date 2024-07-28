package io.mattw.jpowder.walls;

import java.awt.*;

public class Walls {
    public static final Wall none, wall, air, wvoid;

    static {
        none = new Wall("WNone", Color.BLACK);
        none.remove = true;

        wall = new Wall("Wall", Color.LIGHT_GRAY);

        air = new Wall("WAir", new Color(128, 128, 255, 64));
        air.air = true;

        wvoid = new Wall("WVoid", new Color(255, 128, 128, 64));
    }
}
