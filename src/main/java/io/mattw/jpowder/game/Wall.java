package io.mattw.jpowder.game;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class Wall extends Item {

    // Pass-through flags.
    private boolean air = false;
    private boolean parts = false;
    private boolean remove = false;

    public Wall(String name, Color color) {
        setName(name);
        setColor(color);
    }

    public String toString() {
        return getName();
    }

}
