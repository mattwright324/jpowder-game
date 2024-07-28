package io.mattw.jpowder.elements;

import io.mattw.jpowder.particles.Particle;

public class Conversion {

    public static final int CM_TYPE = 0;
    public static final int CM_CTYPE = 1;

    private final int method;
    private final int sign;
    private final Element el;
    private final double value;

    public Conversion(int m, Element e, int sign, double temp) {
        this.method = m;
        this.sign = sign;
        this.el = e;
        this.value = temp;
    }

    public boolean shouldConvert(Particle p) {
        switch (sign) {
            case (Elements.CS_EQ):
                return (int) value == (int) p.getCelcius();
            case (Elements.CS_LSS):
                return value > p.getCelcius();
            case (Elements.CS_GTR):
            default:
                return value < p.getCelcius();
        }
    }

    public void doConversion(Particle p) {
        switch (method) {
            case (CM_TYPE):
                p.morph(el, Particle.MORPH_KEEP_TEMP, true);
                break;
            case (CM_CTYPE):
                p.morph(Elements.get(p.getCtype()), Particle.MORPH_KEEP_TEMP, true);
                break;
        }
    }
}
