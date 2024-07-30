package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class BigCell {

    private int x, y;
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

    public Color getColor() {
        if (wall != null) {
            return wall.getColor();
        }
        int c = (int) (255 * Math.abs(pressure) / GameThread.MAX_AIR);
        if (pressure < 0) {
            return new Color(0, 0, c);
        } else {
            return new Color(c, 0, 0);
        }
    }

    public int screenX() {
        return x * GamePanel.scale * 4;
    }

    public int screenY() {
        return y * GamePanel.scale * 4;
    }
}
