package mattw.powder;

import javafx.scene.paint.Color;

public class Cell {
    public Color color = Color.BEIGE;
    public double blue = 0;
    public long uid = 0;
    public Cell() {}
    public Cell(double blue) { this.blue = blue; }

    public void update(long uid) { this.uid = uid; }
    public Color getColor() { return color; }
}
