package powder.elements;

import powder.particles.IParticleInit;
import powder.particles.IParticleUpdate;

import java.awt.*;

public class Element {
    public int id = 0;
    public String shortName = "ELEM";
    public String description = "Element description.";
    public int weight = 10;
    public long life = 0;
    public double celcius = 0.0;
    public boolean remove = false;
    public double flammibility = 0;
    public boolean conducts = false;
    public boolean sandEffect = false;
    public boolean life_decay = true;
    public int life_dmode = 0; // 0 = Nothing, 1 = Remove, 2 = Change to Ctype
    public Color color = new Color(180, 180, 30);
    public IElementMovement movement;
    public IParticleInit init;
    public IParticleUpdate update;

    public Element(int eid, String name) {
        id = eid;
        shortName = name;
    }

    public Element(int eid, String name, Color c) {
        id = eid;
        shortName = name;
        setColor(c);
    }

    public Element(int eid, String name, String desc) {
        id = eid;
        shortName = name;
        description = desc;
    }

    public Element(int eid, String name, String desc, Color c) {
        id = eid;
        shortName = name;
        description = desc;
        setColor(c);
    }

    public boolean heavierThan(Element e) {
        return e.weight > weight;
    }

    public boolean lighterThan(Element e) {
        return e.weight < weight;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
    }

    public void setMovement(IElementMovement em) {
        movement = em;
    }

    public void setParticleInit(IParticleInit pi) {
        init = pi;
    }

    public void setParticleUpdate(IParticleUpdate pu) {
        update = pu;
    }
}
