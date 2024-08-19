package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@ToString
@Getter
@Setter
public class Cell {

    private static final Logger logger = LogManager.getLogger();

    private final int x;
    private final int y;
    private final List<Particle> parts = new CopyOnWriteArrayList<>();

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void reset() {
        parts.forEach(part -> part.setRemove(true));
        parts.clear();
    }

    public int screenX() {
        return x * GamePanel.windowScale;
    }

    public int screenY() {
        return y * GamePanel.windowScale;
    }

    public int count() {
        return parts.size();
    }

    public boolean isStackEmpty() {
        return parts.isEmpty();
    }

    public boolean canMoveHere(Particle particle) {
        return canMoveHere(particle.getEl());
    }

    public boolean canMoveHere(Element element) {
        var wall = toWall();
        if (wall != null && !wall.isAllowParts()) {
            return false;
        }
        for (var particle : parts) {
            if (particle != null && !element.isStackable()) {
                return false;
            }
        }
        return true;
    }

    public boolean canDisplace(Particle fromPart) {
        var fromEl = fromPart.getEl();
        for (Particle toPart : parts) {
            if (toPart != null && fromEl.heavierThan(toPart.getEl())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasElement(Element element) {
        for (Particle particle : parts) {
            if (particle != null && particle.getEl() == element) {
                return true;
            }
        }
        return false;
    }

    public void removeParticle(Particle pos) {
        if (parts.isEmpty()) {
            return;
        }
        parts.remove(pos);
    }

    public Particle addNewHere(Element element) {
        var part = new Particle(element, x, y);
        moveHere(part);
        return part;
    }

    public void moveHere(Particle particle) {
        particle.setX(x);
        particle.setY(y);
        parts.add(particle);
    }

    public Wall toWall() {
        return Grid.bigcell(x / 4, y / 4).getWall();
    }

}
