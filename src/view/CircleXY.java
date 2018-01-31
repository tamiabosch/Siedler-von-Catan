package view;


import javafx.scene.shape.Circle;
import model.Intersection;
import model.Utility;

/**
 * help class to save the coordinates for intersection
 */
class CircleXY extends Circle {

    private Utility.Pair pair;


    CircleXY(int x, int y){
        pair = new Utility.Pair(x, y);
    }

    int getX() { return pair.getX();}

    int getY() {
        return pair.getY();
    }

    Utility.Pair getCoordinates(){
        return pair;
    }

}
