package view;

import model.Utility;

import javafx.scene.shape.Polygon;

/**
 * Created by Thore on 19.12.2016.
 */
public class PolygonXY extends Polygon {

    private Utility.Pair pair;

    public PolygonXY(int x, int y){pair = new Utility.Pair(x, y);}

    public int getX() {
        return pair.getX();
    }

    public int getY() {
        return pair.getY();
    }

    public Utility.Pair getCoordinates(){
        return pair;
    }
}
