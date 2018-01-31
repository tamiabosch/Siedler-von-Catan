package view;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Utility;

/**
 * Created by karamshabita on 09.01.17.
 * help Class to save the current Color of a harbor
 */
public class RectangleXY extends Rectangle{
    private Color color;
    private Utility.Pair coordinates1;
    private Utility.Pair coordinates2;


    public RectangleXY(int x, int y, int z, int b) {
        coordinates1 = new Utility.Pair(x, y);
        coordinates2 = new Utility.Pair(z, b);
    }

    public Utility.Pair getCoordinates1() {
        return coordinates1;
    }
    public Utility.Pair getCoordinates2() {
        return coordinates2;
    }
    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

}
