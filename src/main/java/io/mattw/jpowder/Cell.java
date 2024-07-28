package io.mattw.jpowder;

import io.mattw.jpowder.elements.Element;
import io.mattw.jpowder.particles.Particle;
import io.mattw.jpowder.walls.Wall;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Cell {

    private int x, y;
    private Particle[] part;
    private Particle[] stack = new Particle[0];

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void reset() {
        part = null;
        stack = new Particle[1];
    }

    public int screenX() {
        return x * Display.scale;
    }

    public int screenY() {
        return y * Display.scale;
    }

    // Stacked Particles

    public void cascadeUpdateParticlePositions() {
        if (this.part == null) {
            return;
        }
        for (int i = 0; i < 9; i++) {
            if (this.part[i] == null) {
                continue;
            }
            this.part[i].setX(this.x);
            this.part[i].setY(this.y);
        }
    }

    public Wall toWall() {
        return Grid.bigcell(x / 4, y / 4).getWall();
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

    public void rem(int pos) { // Remove
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
    public void update() {
        Particle p;
        for (int i = 0; i < stack.length; i++) {
            if ((p = stack[i]) != null) {
                if (p.isRemove()) {
                    stack[i] = null;
                } else {
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
