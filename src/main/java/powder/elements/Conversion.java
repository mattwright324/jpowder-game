package main.java.powder.elements;

import main.java.powder.particles.Particle;

public class Conversion {
	
	public static final int CM_TYPE = 0;
    public static final int CM_CTYPE = 1;
    
	public int method = CM_TYPE;
	public int sign = Elements.CS_GTR;
	public Element el;
	public double value = 0;
	
	public Conversion(int m, Element e, int sign, double temp) {
		this.method = m;
		this.sign = sign;
		this.el = e;
		this.value = temp;
	}
	
	public boolean shouldConvert(Particle p) {
    	switch(sign) {
    	case(Elements.CS_GTR):
    		return value < p.celcius;
    	case(Elements.CS_LSS):
    		return value > p.celcius;
    	case(Elements.CS_EQ):
    		return (int) value == (int) p.celcius;
    	default:
    		return value < p.celcius;
    	}
    }
	
	public void doConversion(Particle p) {
		switch(method) {
		case(CM_TYPE):
			p.morph(el, Particle.MORPH_KEEP_TEMP, true);
			break;
		case(CM_CTYPE):
			p.morph(Elements.get(p.ctype), Particle.MORPH_KEEP_TEMP, true);
			break;
		}
	}
}
