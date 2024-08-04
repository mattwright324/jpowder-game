package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BigCell {

    private int x;
    private int y;
    private Wall wall;
    private double pressure = 0;

    public BigCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void reset() {
        wall = null;
        pressure = 0;
    }

    public int screenX() {
        return x * GamePanel.windowScale * 4;
    }

    public int screenY() {
        return y * GamePanel.windowScale * 4;
    }
}
