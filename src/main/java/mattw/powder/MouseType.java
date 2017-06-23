package mattw.powder;

public enum MouseType {
    CIRCLE, SQUARE;
    public MouseType next() {
        if(ordinal() == values().length - 1)
            return values()[0];
        return values()[ordinal()+1];
    }
}
