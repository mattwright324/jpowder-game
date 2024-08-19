package io.mattw.jpowder.game;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Arrays;

@Getter
@Setter
public class Element extends Item {

    private int weight = ElementType.WEIGHT_POWDER;
    private double celcius = ElementType.DEFAULT_TEMP;
    private boolean remove = false;
    private double flammibility = 0;
    private boolean conducts = false;
    private double heatTransfer = 0.3;
    private boolean stackable = false;
    private boolean display = true;
    private boolean sandEffect = false;
    private boolean glow = false;
    private long life = 0;
    private boolean lifeDecay = true;
    private DecayMode lifeDecayMode = DecayMode.NONE;
    private boolean tmpDecay = true;
    private DecayMode tmpDecayMode = DecayMode.NONE;

    private Conversion[] convs = new Conversion[0];

    private Color color = new Color(180, 180, 30);
    private ElementMovement movement;
    private ParticleBehaviour behaviour;

    public Element(int eid, String name, String desc, Color c) {
        setId(eid);
        setName(name);
        setDescription(desc);
        setColor(c);
    }

    public String toString() {
        return getName();
    }

    public void addConvert(Element e, int sign, double temp) {
        convs = Arrays.copyOf(convs, convs.length + 1);
        convs[convs.length - 1] = new Conversion(Conversion.CM_TYPE, e, sign, temp);
    }

    public void addCtypeConvert(int sign, double temp) {
        convs = Arrays.copyOf(convs, convs.length + 1);
        convs[convs.length - 1] = new Conversion(Conversion.CM_CTYPE, null, sign, temp);
    }

    public boolean heavierThan(Element e) {
        return e.weight > weight;
    }

    public void setParticleBehaviour(ParticleBehaviour bh) {
        behaviour = bh;
    }
}
