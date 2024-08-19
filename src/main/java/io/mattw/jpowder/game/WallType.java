package io.mattw.jpowder.game;

import java.awt.*;

public class WallType {

    public static final Wall NONE, WALL, AIR, WVOID;

    static {
        NONE = new Wall("WNone", Color.BLACK);
        // NONE.setRemove(true);

        WALL = new Wall("Wall", Color.LIGHT_GRAY);

        AIR = new Wall("WAir", new Color(128, 128, 255, 64));
        AIR.setAllowParts(true);
        AIR.setRemoveParts(false);
        // AIR.setAir(true);

        WVOID = new Wall("WVoid", new Color(255, 128, 128, 64));
        WVOID.setAllowParts(true);
    }
}
