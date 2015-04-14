package main.java.powder.elements;

import main.java.powder.Cells;
import main.java.powder.particles.Particle;
import main.java.powder.particles.ParticleBehaviour;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Element {
	
	public static final int CM_TYPE = 0; // Converts by set type.
    public static final int CM_CTYPE = 1; // Converts by ctype.
	
    public int id = 0;
    public String shortName = "ELEM";
    public String description = "Element description.";
    
    public int weight = Elements.WEIGHT_POWDER;
    public double celcius = Elements.DEFAULT_TEMP;
    public boolean remove = false;
    public double flammibility = 0;
    public boolean conducts = false;
    public boolean sandEffect = false;
    public double heatTransfer = 0.3;
    public boolean display = true;
    
    public long life = 0;
    public boolean life_decay = true;
    public int life_decay_mode = Elements.DECAY_DIE;
    
    public int tmp = 0;
    public boolean tmp_decay = true;
    public int tmp_decay_mode = Elements.DECAY_NONE;
    
    @Deprecated
    public int life_dmode = 0; // 0 = Nothing, 1 = Remove, 2 = Change to Ctype
    
    
    public boolean convert = false;
    public Element conv = this;
    public double conv_temp = 22.0;
    public int conv_method = CM_CTYPE;
    public int conv_sign = Elements.CS_GTR;
    
    
    public Color color = new Color(180, 180, 30);
    public ElementMovement movement;
    public ParticleBehaviour behaviour;
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
    
    public String toString() {
    	return shortName;
    }
    
    public void setConvert(Element e, int sign, double temp) {
    	convert = true;
    	conv = e;
    	conv_method = CM_TYPE;
    	conv_sign = sign;
    	conv_temp = temp;
    }
    
    public void setCtypeConvert(int sign, double temp) {
    	convert = true;
    	conv_method = CM_CTYPE;
    	conv_sign = sign;
    	conv_temp = temp;
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

    public void setMovement(ElementMovement em) {
        movement = em;
    }

    public void setParticleBehaviour(ParticleBehaviour bh) {
        behaviour = bh;
    }
}
