package io.mattw.jpowder.game;

import lombok.Getter;

import static io.mattw.jpowder.game.ElementType.*;

@Getter
public enum ItemCategory {
    POWDER("Powder", DUST, STNE, SALT, BCOL, BOMB),
    LIQUID("Liquid", WATR, LAVA, LN_2, OIL),
    SOLID("Solid", METL, QRTZ, DMND, COAL, INSL, ICE, CLNE, VOID),
    GASSES("Gas", GAS, FIRE, PLSM, STM),
    RADIO("Radio", PHOT, RADP, PLUT, WARP),
    TOOLS("Tools", NONE, WARM, COOL, SPRK, FILL, ANT, WallType.NONE, WallType.WALL, WallType.AIR, WallType.WVOID),
    ;

    private final String display;
    private final Item[] items;

    ItemCategory(String display, Item... items) {
        this.display = display;
        this.items = items;
    }

}
