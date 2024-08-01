package io.mattw.jpowder.game;

import io.mattw.jpowder.ui.GamePanel;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

@Getter
@Setter
public class Cell {

    private static final Logger logger = LogManager.getLogger();

    private int x;
    private int y;
    private Particle[] stack = new Particle[0];

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void reset() {
        stack = new Particle[0];
    }

    public int screenX() {
        return x * GamePanel.scale;
    }

    public int screenY() {
        return y * GamePanel.scale;
    }

    public int count() {
        int s = 0;
        for (var particle : stack) {
            if (particle != null) {
                s++;
            }
        }
        return s;
    }

    public int nullCount() {
        return stack.length - count();
    }

    public boolean empty() {
        for (var particle : stack) {
            if (particle != null) {
                return false;
            }
        }
        return true;
    }

    public boolean addable(Particle p) {
        return addable(p.getEl());
    }

    public boolean addable(Element e) {
        for (var particle : stack) {
            if (particle != null && !particle.getEl().isStackable()) {
                return false;
            }
        }
        return true;
    }

    public boolean displaceable(Particle p) {
        return displaceable(p.getEl());
    }

    public boolean displaceable(Element e) {
        for (Particle particle : stack) {
            if (particle != null && e.heavierThan(particle.getEl())) {
                return false;
            }
        }
        return true;
    }

    public boolean contains(Element e) {
        for (Particle particle : stack) {
            if (particle != null && particle.getEl() == e) {
                return true;
            }
        }
        return false;
    }

    public Particle part(int pos) {
        if (empty()) {
            return null;
        }
        return stack[pos];
    }

    public void rem(int pos) {
        if (stack.length <= pos) {
            return;
        }
        stack[pos] = null;
    }

    public void add(Particle p) {
        for (int i = 0; i < stack.length; i++) {
            if (stack[i] == null) {
                p.setX(x);
                p.setY(y);
                p.setPos(i);
                stack[i] = p;
                return;
            }
        }

        stack = Arrays.copyOf(stack, stack.length + 1);
        stack[stack.length - 1] = p;
        p.setPos(stack.length - 1);
        p.setX(x);
        p.setY(y);
    }

    public void add(Element e) {
        add(new Particle(e, x, y));
    }

    /**
     * Update the entire stack.
     */
    public void update(String updateId) {
        Particle p;
        for (int i = 0; i < stack.length; i++) {
            if ((p = stack[i]) != null) {
                if (p.isRemove()) {
                    stack[i] = null;
                } else if (!updateId.equals(p.getLastUpdateId())) {
                    p.setLastUpdateId(updateId);
                    p.update();
                }
            }
            if (stack.length > 0 && empty()) {
                stack = new Particle[1];
            }
        }
    }

    /**
     * Moves all particles up and nulls down then cuts off the nulls at the bottom.
     * Check for affect on performance.
     * Should not be used often, preferred on pause.
     */
    public void cleanStack() {
        if (stack.length > 0 && empty()) {
            stack = new Particle[0];
        } else {
            for (int i = 0; i < stack.length; i++) {
                if (stack[i] == null) {
                    for (int n = i; n < stack.length; n++) {
                        if (stack[n] != null) {
                            stack[i] = stack[n];
                            stack[n] = null;
                            break;
                        }
                    }
                }
            }
        }
    }
}
