package network;

import LOG.Logging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.scene.paint.Color;
import model.*;
import view.BoardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkTranslator {

    static Logger logger;

    public NetworkTranslator(){
        logger = BoardView.getLogger();
    }

    public NetworkTranslator(Logger log){
        this.logger = log;
    }

    /**
     * sorts a array of three pairs, first x than y
     * @param pairs array of 3 pairs
     * @return same array sorted
     */
    public static Utility.Pair[] sortPairs(Utility.Pair[] pairs){
        Utility.Pair temp;
        for(byte i = 0; i < 2; i++){
            if(pairs[i+1].getX() < pairs[i].getX()){
                temp = pairs[i];
                pairs[i] = pairs[i+1];
                pairs[i+1] = temp;
            }
        }
        for(byte i = 0; i < 2; i++){
            if(pairs[i+1].getX() < pairs[i].getX()){
                temp = pairs[i];
                pairs[i] = pairs[i+1];
                pairs[i+1] = temp;
            }
        }

        for (int i = 0; i < 2; i++) {
            if(pairs[i].getX() == pairs[i+1].getX()){
                if(pairs[i+1].getY() < pairs[i].getY()){
                    temp = pairs[i];
                    pairs[i] = pairs[i+1];
                    pairs[i+1] = temp;
                }
            }
        }

        return pairs;
    }

    /**
     * Takes a name, an id and a color and translates that to a Player object
     * @param name name of player
     * @param id id of player
     * @param color color of player
     * @return Player object
     */
    public static Player translatePlayer(String name, int id, String color) {
        Color colorElement;
        switch (color) {
            case "Rot":
                colorElement = Color.web("#B9121B");
                break;
            case "Orange":
                colorElement = Color.web("#FFB03B");
                break;
            case "Weiß":
                colorElement = Color.web("#ECF0F1");
                break;
            case "Blau":
                colorElement = Color.web("#3498DB");
                break;
            default:
                colorElement = Color.BLACK; // not gonna happen
        }

        return new Player(name, id, colorElement);
    }

    /**
     * Translates the JsonObject that the client gets from the server to a Board object
     * @param map map object from Client
     * @return Board object
     */
    public static Board translateMapToStart(JsonObject map) {

        JsonArray jsonLandpieceArray = map.get("Felder").getAsJsonArray();
        JsonArray jsonHarborArray = map.get("Häfen").getAsJsonArray();
        Landpiece[][] landpieces = new Landpiece[5][5];

            for (int i = 0; i < jsonLandpieceArray.size(); i++) {
                boolean isSeafield = jsonLandpieceArray.get(i).getAsJsonObject().get("Typ").getAsString().equals("Meer");
                if (!isSeafield) {
                    //String location = jsonLandpieceArray.get(i).getAsJsonObject().get("Ort").getAsString();
                    JsonObject location = jsonLandpieceArray.get(i).getAsJsonObject().get("Ort").getAsJsonObject();
                    String type = jsonLandpieceArray.get(i).getAsJsonObject().get("Typ").getAsString();
                    byte number;
                    if (!type.equals("Wüste")) {
                        number = jsonLandpieceArray.get(i).getAsJsonObject().get("Zahl").getAsByte();
                    } else {
                        number = 0;
                    }
                    //Utility.Pair pair = translateLandpieceCoordinate(location);
                    Utility.Pair pair = translateLandpieceCoordinateFromProtocol(location);
                    landpieces[pair.getX()][pair.getY()] = translateToLandpieceObject(number, type, pair);
                    if(number == 0){
                        landpieces[pair.getX()][pair.getY()].setHoldingRobber(true);
                    }
                }
            }
        Utility.Pair[][] coordinatesOfHarbors = new Utility.Pair[2][jsonHarborArray.size()];
        ResourceType[] typeOfHarbors = new ResourceType[jsonHarborArray.size()];

            for (int i = 0; i < jsonHarborArray.size(); i++) {
                //String location = jsonHarborArray.get(i).getAsJsonObject().get("Ort").getAsString();
                JsonArray location = jsonHarborArray.get(i).getAsJsonObject().get("Ort").getAsJsonArray();
                String type = jsonHarborArray.get(i).getAsJsonObject().get("Typ").getAsString();
                //Utility.Pair[] locationPair = translateCoordinateToIntersections(location);
                Utility.Pair[] locationPair = translateCoordinateToIntersections(location);
                coordinatesOfHarbors[0][i] = locationPair[0];
                coordinatesOfHarbors[1][i] = locationPair[1];
                typeOfHarbors[i] = translateHarborResourceType(type);
            }

        return new Board(landpieces, coordinatesOfHarbors, typeOfHarbors);

    }

    /**
     * Translates the harbor string we get from the server into a ResourceType
     * @param resource harbor string we get from the server
     * @return ResourceType
     */
    public static ResourceType translateHarborResourceType(String resource) {
        switch (resource) {
            case "Getreide Hafen":
                return ResourceType.GRAIN;
            case "Holz Hafen":
                return ResourceType.LUMBER;
            case "Erz Hafen":
                return ResourceType.ORE;
            case "Lehm Hafen":
                return ResourceType.BRICK;
            case "Wolle Hafen":
                return ResourceType.WOOL;
            default:
                return null;
        }
    }

    public static JsonObject translateLandpieceObject(Landpiece landpiece, Utility.Pair coordinate) {

        JsonObject translatedLandpiece = new JsonObject();

        // add location
        //String location = translateLandpieceCoordinate(coordinate);
        JsonObject location = translateLandpieceCoordinateToProtocol(coordinate);
        translatedLandpiece.add("Ort", location);

        // add type
        LandpieceType type = landpiece.getResourceType();
        switch (type) {
            case PASTURES:
                translatedLandpiece.addProperty("Typ", "Weideland");
                break;
            case FOREST:
                translatedLandpiece.addProperty("Typ", "Wald");
                break;
            case FIELDS:
                translatedLandpiece.addProperty("Typ", "Ackerland");
                break;
            case MOUNTAINS:
                translatedLandpiece.addProperty("Typ", "Gebirge");
                break;
            case HILLS:
                translatedLandpiece.addProperty("Typ", "Hügelland");
                break;
            case DESERT:
                translatedLandpiece.addProperty("Typ", "Wüste");
                break;
        }

        // add number
        int number = landpiece.getDiceNumber();
        translatedLandpiece.addProperty("Zahl", number);

        return translatedLandpiece;

    }

    public static JsonObject translateHarborObject(Harbor harbor) {
        JsonObject translatedHarbor = new JsonObject();

        // add type
        if (harbor.getResource() == null) {
            translatedHarbor.addProperty("Typ", "Hafen");
        } else {
            switch (harbor.getResource()) {
                case WOOL:
                    translatedHarbor.addProperty("Typ", "Wolle Hafen");
                    break;
                case LUMBER:
                    translatedHarbor.addProperty("Typ", "Holz Hafen");
                    break;
                case GRAIN:
                    translatedHarbor.addProperty("Typ", "Getreide Hafen");
                    break;
                case ORE:
                    translatedHarbor.addProperty("Typ", "Erz Hafen");
                    break;
                case BRICK:
                    translatedHarbor.addProperty("Typ", "Lehm Hafen");
                    break;
                default:
                    translatedHarbor.addProperty("Typ", "Hafen");
                    break;
            }
        }

        // add location
        Utility.Pair[] coordinate = harbor.getCoordinates();
        translatedHarbor.add("Ort", translateStreetCoordinate(coordinate));

        return translatedHarbor;
    }

    /**
     * Translates the coordinate String with two landpieces to two Intersection-Coordinates
     * At the beginning it is necessary to handle the edge-cases because we have no seapieces in our Model
     * @param coordinate coordinate string from two landpieces
     * @return two Intersection Coordinates (Streets and Harbors)
     */
    public static Utility.Pair[] translateCoordinateToIntersections(String coordinate) {

        char[] charsToSort = coordinate.toCharArray();
        Arrays.sort(charsToSort);
        String sorted = new String(charsToSort);

        switch (sorted) {
            case "Aa":
                return new Utility.Pair[]{new Utility.Pair(0,0), new Utility.Pair(1,0)};
            case "Ab":
                return new Utility.Pair[]{new Utility.Pair(1,0), new Utility.Pair(2,0)};
            case "Bb":
                return new Utility.Pair[]{new Utility.Pair(2,0), new Utility.Pair(3,0)};
            case "Bc":
                return new Utility.Pair[]{new Utility.Pair(3,0), new Utility.Pair(4,0)};
            case "Cc":
                return new Utility.Pair[]{new Utility.Pair(4,0), new Utility.Pair(5,0)};
            case "Cd":
                return new Utility.Pair[]{new Utility.Pair(5,0), new Utility.Pair(6,0)};
            case "Cf":
                return new Utility.Pair[]{new Utility.Pair(6,0), new Utility.Pair(7,1)};
            case "Gf":
                return new Utility.Pair[]{new Utility.Pair(7,1), new Utility.Pair(8,1)};
            case "Gh":
                return new Utility.Pair[]{new Utility.Pair(8,1), new Utility.Pair(9,2)};
            case "Lh":
                return new Utility.Pair[]{new Utility.Pair(9,2), new Utility.Pair(10,2)};
            case "Lj":
                return new Utility.Pair[]{new Utility.Pair(10,2), new Utility.Pair(11,3)};
            case "Ll":
                return new Utility.Pair[]{new Utility.Pair(10,3), new Utility.Pair(11,3)};
            case "Pl":
                return new Utility.Pair[]{new Utility.Pair(10,3), new Utility.Pair(11,4)};
            case "Pn":
                return new Utility.Pair[]{new Utility.Pair(10,4), new Utility.Pair(11,4)};
            case "Sn":
                return new Utility.Pair[]{new Utility.Pair(10,4), new Utility.Pair(11,5)};
            case "Sr":
                return new Utility.Pair[]{new Utility.Pair(10,5), new Utility.Pair(11,5)};
            case "Sq":
                return new Utility.Pair[]{new Utility.Pair(9,5), new Utility.Pair(10,5)};
            case "Rq":
                return new Utility.Pair[]{new Utility.Pair(8,5), new Utility.Pair(9,5)};
            case "Rp":
                return new Utility.Pair[]{new Utility.Pair(7,5), new Utility.Pair(8,5)};
            case "Qp":
                return new Utility.Pair[]{new Utility.Pair(6,5), new Utility.Pair(7,5)};
            case "Qm":
                return new Utility.Pair[]{new Utility.Pair(5,5), new Utility.Pair(6,5)};
            case "Qo":
                return new Utility.Pair[]{new Utility.Pair(4,4), new Utility.Pair(5,5)};
            case "Mm":
                return new Utility.Pair[]{new Utility.Pair(3,4), new Utility.Pair(4,4)};
            case "Mk":
                return new Utility.Pair[]{new Utility.Pair(2,3), new Utility.Pair(3,4)};
            case "Hk":
                return new Utility.Pair[]{new Utility.Pair(1,3), new Utility.Pair(2,3)};
            case "Hi":
                return new Utility.Pair[]{new Utility.Pair(0,2), new Utility.Pair(1,3)};
            case "Hg":
                return new Utility.Pair[]{new Utility.Pair(0,2), new Utility.Pair(1,2)};
            case "Dg":
                return new Utility.Pair[]{new Utility.Pair(0,1), new Utility.Pair(1,2)};
            case "De":
                return new Utility.Pair[]{new Utility.Pair(0,1), new Utility.Pair(1,1)};
            case "Ae":
                return new Utility.Pair[]{new Utility.Pair(0,0), new Utility.Pair(1,1)};
        }

        Utility.Pair landpieceCoordinate1 = translateLandpieceCoordinate(sorted.substring(0,1));
        Utility.Pair landpieceCoordinate2 = translateLandpieceCoordinate(sorted.substring(1,2));

        Intersection[] int1 = new Intersection[6];
        Intersection[] int2 = new Intersection[6];

        int1[0] = new Intersection(landpieceCoordinate1.getX()*2  , landpieceCoordinate1.getY());
        int1[1] = new Intersection(landpieceCoordinate1.getX()*2+1, landpieceCoordinate1.getY());
        int1[2] = new Intersection(landpieceCoordinate1.getX()*2+2, landpieceCoordinate1.getY());
        int1[3] = new Intersection(landpieceCoordinate1.getX()*2+3, landpieceCoordinate1.getY()+1);
        int1[4] = new Intersection(landpieceCoordinate1.getX()*2+2, landpieceCoordinate1.getY()+1);
        int1[5] = new Intersection(landpieceCoordinate1.getX()*2+1, landpieceCoordinate1.getY()+1);

        int2[0] = new Intersection(landpieceCoordinate2.getX()*2  , landpieceCoordinate2.getY());
        int2[1] = new Intersection(landpieceCoordinate2.getX()*2+1, landpieceCoordinate2.getY());
        int2[2] = new Intersection(landpieceCoordinate2.getX()*2+2, landpieceCoordinate2.getY());
        int2[3] = new Intersection(landpieceCoordinate2.getX()*2+3, landpieceCoordinate2.getY()+1);
        int2[4] = new Intersection(landpieceCoordinate2.getX()*2+2, landpieceCoordinate2.getY()+1);
        int2[5] = new Intersection(landpieceCoordinate2.getX()*2+1, landpieceCoordinate2.getY()+1);

        Utility.Pair pair1 = new Utility.Pair(0, 0);
        Utility.Pair pair2 = new Utility.Pair(0, 0);

        boolean firstPairFound = false;

        for (int i = 0; i < int1.length; i++) {
            for (int j = 0; j < int2.length; j++) {
                int x1 = int1[i].getCoordinates().getX();
                int y1 = int1[i].getCoordinates().getY();
                int x2 = int2[j].getCoordinates().getX();
                int y2 = int2[j].getCoordinates().getY();
                if (x1 == x2 && y1 == y2 && !firstPairFound) {
                    pair1 = int1[i].getCoordinates();
                    firstPairFound = true;
                } else if (x1 == x2 && y1 == y2) {
                    pair2 = int1[i].getCoordinates();
                }
            }
        }

        return new Utility.Pair[]{pair1, pair2};
    }

    public static Utility.Pair[] translateCoordinateToIntersections(JsonArray coordinates) {

        JsonObject coordinate1 = coordinates.get(0).getAsJsonObject();
        JsonObject coordinate2 = coordinates.get(1).getAsJsonObject();

        int x1 = coordinate1.get("x").getAsInt();
        int y1 = coordinate1.get("y").getAsInt();

        int x2 = coordinate2.get("x").getAsInt();
        int y2 = coordinate2.get("y").getAsInt();

        if (x1 == -3 && y1 == 3 && x2 == -2 && y2 == 2 || x1 == -2 && y1 == 2 && x2 == -3 && y2 == 3) {
            return new Utility.Pair[]{new Utility.Pair(0,0), new Utility.Pair(1,0)};
        } else if (x1 == -2 && y1 == 3 && x2 == -2 && y2 == 2 || x1 == -2 && y1 == 2 && x2 == -2 && y2 == 3) {
            return new Utility.Pair[]{new Utility.Pair(1,0), new Utility.Pair(2,0)};
        } else if (x1 == -2 && y1 == 3 && x2 == -1 && y2 == 2 || x1 == -1 && y1 == 2 && x2 == -2 && y2 == 3) {
            return new Utility.Pair[]{new Utility.Pair(2, 0), new Utility.Pair(3, 0)};
        } else if (x1 == -1 && y1 == 3 && x2 == -1 && y2 == 2 || x1 == -1 && y1 == 2 && x2 == -1 && y2 == 3) {
            return new Utility.Pair[]{new Utility.Pair(3,0), new Utility.Pair(4,0)};
        } else if (x1 == -1 && y1 == 3 && x2 == 0 && y2 == 2 || x1 == 0 && y1 == 2 && x2 == -1 && y2 == 3) {
            return new Utility.Pair[]{new Utility.Pair(4,0), new Utility.Pair(5,0)};
        } else if (x1 == 0 && y1 == 3 && x2 == 0 && y2 == 2 || x1 == 0 && y1 == 2 && x2 == 0 && y2 == 3) {
            return new Utility.Pair[]{new Utility.Pair(5,0), new Utility.Pair(6,0)};
        } else if (x1 == 0 && y1 == 2 && x2 == 1 && y2 == 2 || x1 == 1 && y1 == 2 && x2 == 0 && y2 == 2){
            return new Utility.Pair[]{new Utility.Pair(6,0), new Utility.Pair(7,1)};
        } else if (x1 == 1 && y1 == 2 && x2 == 1 && y2 == 1 || x1 == 1 && y1 == 1 && x2 == 1 && y2 == 2){
            return new Utility.Pair[]{new Utility.Pair(7,1), new Utility.Pair(8,1)};
        } else if (x1 == 1 && y1 == 1 && x2 == 2 && y2 == 1 || x1 == 2 && y1 == 1 && x2 == 1 && y2 == 1){
            return new Utility.Pair[]{new Utility.Pair(8,1), new Utility.Pair(9,2)};
        } else if (x1 == 2 && y1 == 1 && x2 == 2 && y2 == 0 || x1 == 2 && y1 == 0 && x2 == 2 && y2 == 1) {
            return new Utility.Pair[]{new Utility.Pair(9, 2), new Utility.Pair(10, 2)};
        } else if (x1 == 3 && y1 == 0 && x2 == 2 && y2 == 0 || x1 == 2 && y1 == 0 && x2 == 3 && y2 == 0) {
            return new Utility.Pair[]{new Utility.Pair(10,2), new Utility.Pair(11,3)};
        } else if (x1 == 3 && y1 == -1 && x2 == 2 && y2 == 0 || x1 == 2 && y1 == 0 && x2 == 3 && y2 == -1) {
            return new Utility.Pair[]{new Utility.Pair(10,3), new Utility.Pair(11,3)};
        } else if (x1 == 3 && y1 == -1 && x2 == 2 && y2 == -1 || x1 == 2 && y1 == -1 && x2 == 3 && y2 == -1) {
            return new Utility.Pair[]{new Utility.Pair(10,3), new Utility.Pair(11,4)};
        } else if (x1 == 2 && y1 == -1 && x2 == 3 && y2 == -2 || x1 == 3 && y1 == -2 && x2 == 2 && y2 == -1) {
            return new Utility.Pair[]{new Utility.Pair(10,4), new Utility.Pair(11,4)};
        } else if (x1 == 2 && y1 == -2 && x2 == 3 && y2 == -2 || x1 == 3 && y1 == -2 && x2 == 2 && y2 == -2) {
            return new Utility.Pair[]{new Utility.Pair(10,4), new Utility.Pair(11,5)};
        } else if(x1 == 2 && y1 == -2 && x2 == 3 && y2 == -3 || x1 == 3 && y1 == -3 && x2 == 2 && y2 == -2) {
            return new Utility.Pair[]{new Utility.Pair(10,5), new Utility.Pair(11,5)};
        } else if(x1 == 2 && y1 == -2 && x2 == 2 && y2 == -3 || x1 == 2 && y1 == -3 && x2 == 2 && y2 == -2){
            return new Utility.Pair[]{new Utility.Pair(9,5), new Utility.Pair(10,5)};
        } else if(x1 == 1 && y1 == -2 && x2 == 2 && y2 == -3 || x1 == 2 && y1 == -3 && x2 == 1 && y2 == -2){
            return new Utility.Pair[]{new Utility.Pair(8,5), new Utility.Pair(9,5)};
        } else if(x1 == 1 && y1 == -2 && x2 == 1 && y2 == -3 || x1 == 1 && y1 == -3 && x2 == 1 && y2 == -2){
            return new Utility.Pair[]{new Utility.Pair(7,5), new Utility.Pair(8,5)};
        } else if(x1 == 0 && y1 == -2 && x2 == 1 && y2 == -3 || x1 == 1 && y1 == -3 && x2 == 0 && y2 == -2){
            return new Utility.Pair[]{new Utility.Pair(6,5), new Utility.Pair(7,5)};
        } else if(x1 == 0 && y1 == -2 && x2 == 0 && y2 == -3 || x1 == 0 && y1 == -3 && x2 == 0 && y2 == -2){
            return new Utility.Pair[]{new Utility.Pair(5,5), new Utility.Pair(6,5)};
        } else if(x1 == 0 && y1 == -2 && x2 == -1 && y2 == -2 || x1 == -1 && y1 == -2 && x2 == 0 && y2 == -2){
            return new Utility.Pair[]{new Utility.Pair(4,4), new Utility.Pair(5,5)};
        } else if(x1 == -1 && y1 == -1 && x2 == -1 && y2 == -2 || x1 == -1 && y1 == -2 && x2 == -1 && y2 == -1) {
            return new Utility.Pair[]{new Utility.Pair(3, 4), new Utility.Pair(4, 4)};
        } else if(x1 == -1 && y1 == -1 && x2 == -2 && y2 == -1 || x1 == -2 && y1 == -1 && x2 == -1 && y2 == -1) {
            return new Utility.Pair[]{new Utility.Pair(2,3), new Utility.Pair(3,4)};
        } else if(x1 == -2 && y1 == 0 && x2 == -2 && y2 == -1 || x1 == -2 && y1 == -1 && x2 == -2 && y2 == 0) {
            return new Utility.Pair[]{new Utility.Pair(1,3), new Utility.Pair(2,3)};
        } else if(x1 == -2 && y1 == 0 && x2 == -3 && y2 == 0 || x1 == -3 && y1 == 0 && x2 == -2 && y2 == 0) {
            return new Utility.Pair[]{new Utility.Pair(0,2), new Utility.Pair(1,3)};
        } else if(x1 == -2 && y1 == 0 && x2 == -3 && y2 == 1 || x1 == -3 && y1 == 1 && x2 == -2 && y2 == 0) {
            return new Utility.Pair[]{new Utility.Pair(0,2), new Utility.Pair(1,2)};
        } else if(x1 == -2 && y1 == 1 && x2 == -3 && y2 == 1 || x1 == -3 && y1 == 1 && x2 == -2 && y2 == 1) {
            return new Utility.Pair[]{new Utility.Pair(0,1), new Utility.Pair(1,2)};
        } else if(x1 == -2 && y1 == 1 && x2 == -3 && y2 == 2 || x1 == -3 && y1 == 2 && x2 == -2 && y2 == 1) {
            return new Utility.Pair[]{new Utility.Pair(0,1), new Utility.Pair(1,1)};
        } else if(x1 == -2 && y1 == 2 && x2 == -3 && y2 == 2 || x1 == -3 && y1 == 2 && x2 == -2 && y2 == 2) {
            return new Utility.Pair[]{new Utility.Pair(0,0), new Utility.Pair(1,1)};
        }


        Utility.Pair landpieceCoordinate1 = translateLandpieceCoordinateFromProtocol(coordinate1);
        Utility.Pair landpieceCoordinate2 = translateLandpieceCoordinateFromProtocol(coordinate2);

        Intersection[] int1 = new Intersection[6];
        Intersection[] int2 = new Intersection[6];

        int1[0] = new Intersection(landpieceCoordinate1.getX()*2  , landpieceCoordinate1.getY());
        int1[1] = new Intersection(landpieceCoordinate1.getX()*2+1, landpieceCoordinate1.getY());
        int1[2] = new Intersection(landpieceCoordinate1.getX()*2+2, landpieceCoordinate1.getY());
        int1[3] = new Intersection(landpieceCoordinate1.getX()*2+3, landpieceCoordinate1.getY()+1);
        int1[4] = new Intersection(landpieceCoordinate1.getX()*2+2, landpieceCoordinate1.getY()+1);
        int1[5] = new Intersection(landpieceCoordinate1.getX()*2+1, landpieceCoordinate1.getY()+1);

        int2[0] = new Intersection(landpieceCoordinate2.getX()*2  , landpieceCoordinate2.getY());
        int2[1] = new Intersection(landpieceCoordinate2.getX()*2+1, landpieceCoordinate2.getY());
        int2[2] = new Intersection(landpieceCoordinate2.getX()*2+2, landpieceCoordinate2.getY());
        int2[3] = new Intersection(landpieceCoordinate2.getX()*2+3, landpieceCoordinate2.getY()+1);
        int2[4] = new Intersection(landpieceCoordinate2.getX()*2+2, landpieceCoordinate2.getY()+1);
        int2[5] = new Intersection(landpieceCoordinate2.getX()*2+1, landpieceCoordinate2.getY()+1);

        Utility.Pair pair1 = new Utility.Pair(0, 0);
        Utility.Pair pair2 = new Utility.Pair(0, 0);

        boolean firstPairFound = false;

        for (int i = 0; i < int1.length; i++) {
            for (int j = 0; j < int2.length; j++) {
                int a1 = int1[i].getCoordinates().getX();
                int b1 = int1[i].getCoordinates().getY();
                int a2 = int2[j].getCoordinates().getX();
                int b2 = int2[j].getCoordinates().getY();
                if (a1 == a2 && b1 == b2 && !firstPairFound) {
                    pair1 = int1[i].getCoordinates();
                    firstPairFound = true;
                } else if (a1 == a2 && b1 == b2) {
                    pair2 = int1[i].getCoordinates();
                }
            }
        }

        return new Utility.Pair[]{pair1, pair2};
    }

    public static Landpiece translateToLandpieceObject(byte diceNumber, String type, Utility.Pair coordinates) {
       switch (type) {
           case "Hügelland":
               return new Landpiece(diceNumber, LandpieceType.HILLS, coordinates);
           case "Ackerland":
               return new Landpiece(diceNumber, LandpieceType.FIELDS, coordinates);
           case "Wald":
               return new Landpiece(diceNumber, LandpieceType.FOREST, coordinates);
           case "Weideland":
               return new Landpiece(diceNumber, LandpieceType.PASTURES, coordinates);
           case "Gebirge":
               return new Landpiece(diceNumber, LandpieceType.MOUNTAINS, coordinates);
           case "Wüste":
               return new Landpiece(diceNumber, LandpieceType.DESERT, coordinates);
           default:
               return null;
       }
    }

    public static Utility.Pair translateLandpieceCoordinate(String coordinate) {
        switch (coordinate) {
            case ("A"):
                return new Utility.Pair(0, 0);
            case ("B"):
                return new Utility.Pair(1, 0);
            case ("C"):
                return new Utility.Pair(2, 0);
            case ("D"):
                return new Utility.Pair(0, 1);
            case ("E"):
                return new Utility.Pair(1, 1);
            case ("F"):
                return new Utility.Pair(2, 1);
            case ("G"):
                return new Utility.Pair(3, 1);
            case ("H"):
                return new Utility.Pair(0, 2);
            case ("I"):
                return new Utility.Pair(1, 2);
            case ("J"):
                return new Utility.Pair(2, 2);
            case ("K"):
                return new Utility.Pair(3, 2);
            case ("L"):
                return new Utility.Pair(4, 2);
            case ("M"):
                return new Utility.Pair(1, 3);
            case ("N"):
                return new Utility.Pair(2, 3);
            case ("O"):
                return new Utility.Pair(3, 3);
            case ("P"):
                return new Utility.Pair(4, 3);
            case ("Q"):
                return new Utility.Pair(2, 4);
            case ("R"):
                return new Utility.Pair(3, 4);
            case ("S"):
                return new Utility.Pair(4, 4);
            default:
                return null;
        }
    }

    public static String translateLandpieceCoordinate(Utility.Pair coordinate) {
        if (coordinate.equals(new Utility.Pair(0, 0))) {
            return "A";
        }else if(coordinate.equals(new Utility.Pair(1, 0))){
            return "B";
        }else if (coordinate.equals(new Utility.Pair(2, 0))){
            return "C";
        }else if (coordinate.equals(new Utility.Pair(0, 1))){
            return "D";
        }else if (coordinate.equals(new Utility.Pair(1, 1))){
            return "E";
        }else if (coordinate.equals(new Utility.Pair(2, 1))){
            return "F";
        }else if (coordinate.equals(new Utility.Pair(3, 1))){
            return "G";
        }else if (coordinate.equals(new Utility.Pair(0, 2))){
            return "H";
        }else if (coordinate.equals(new Utility.Pair(1, 2))){
            return "I";
        }else if (coordinate.equals(new Utility.Pair(2, 2))){
            return "J";
        }else if (coordinate.equals(new Utility.Pair(3, 2))){
            return "K";
        }else if (coordinate.equals(new Utility.Pair(4, 2))){
            return "L";
        }else if (coordinate.equals(new Utility.Pair(1, 3))){
            return "M";
        }else if (coordinate.equals(new Utility.Pair(2, 3))){
            return "N";
        }else if (coordinate.equals(new Utility.Pair(3, 3))){
            return "O";
        }else if (coordinate.equals(new Utility.Pair(4, 3))){
            return "P";
        }else if (coordinate.equals(new Utility.Pair(2, 4))){
            return "Q";
        }else if(coordinate.equals(new Utility.Pair(3, 4))){
            return "R";
        }else if (coordinate.equals(new Utility.Pair(4, 4))){
            return "S";
        }
        System.out.println("Error in translateLandpieceCoordinate, returns no String");
        return null;
    }

    public static Utility.Pair translateLandpieceCoordinateFromProtocol(JsonObject landpiece) {
        int x = landpiece.get("x").getAsInt();
        int y = landpiece.get("y").getAsInt();

        x = x + 2;
        y = -y + 2;

        Utility.Pair pair = new Utility.Pair(x, y);

        return pair;
    }

    public static JsonObject translateLandpieceCoordinateToProtocol(Utility.Pair pair) {
        int x = pair.getX();
        int y = pair.getY();

        x = x - 2;
        y = -y + 2;

        JsonObject translatedLandpiece = new JsonObject();
        translatedLandpiece.addProperty("x", x);
        translatedLandpiece.addProperty("y", y);

        return translatedLandpiece;

    }
    
    public static String translateStreetCoordinate(Utility.Pair a, Utility.Pair b){
    	String aString = translateIntersectionCoordinate(a);
    	String bString = translateIntersectionCoordinate(b);
    	String result = "";
    	
    	for (char c : aString.toCharArray()){
    		for(char c2 : bString.toCharArray()){
    			if (c == c2){
    				result += c;
    			}
    		}
    	}
    	
    	return result;
    }

    public static JsonArray translateStreetCoordinate(Utility.Pair[] coordinates) {
        Utility.Pair[] aPairs = translateIntersectionCoordinateToLandpieces(coordinates[0]);
        Utility.Pair[] bPairs = translateIntersectionCoordinateToLandpieces(coordinates[1]);
        ArrayList<Utility.Pair> result = new ArrayList<>();

        for (Utility.Pair a : aPairs) {
            for (Utility.Pair b : bPairs) {
                if (a.equals(b)) {
                    result.add(a);
                }
            }
        }

        return translatePairArray(result);
    }

   public static Utility.Pair translateIntersectionCoordinate(String coordinate) {

        char[] charsToSort = coordinate.toCharArray();
        Arrays.sort(charsToSort);
        String sorted = new String(charsToSort);

        // seafield-cases
        switch (sorted) {
            case "Aae":
                return new Utility.Pair(0, 0);
            case "Aab":
                return new Utility.Pair(1, 0);
            case "ABb":
                return new Utility.Pair(2, 0);
            case "Bbc":
                return new Utility.Pair(3, 0);
            case "BCc":
                return new Utility.Pair(4, 0);
            case "Ccd":
                return new Utility.Pair(5, 0);
            case "Cdf":
                return new Utility.Pair(6, 0);
            case "CGf":
                return new Utility.Pair(7, 1);
            case "Gfh":
                return new Utility.Pair(8, 1);
            case "GLh":
                return new Utility.Pair(9, 2);
            case "Lhj":
                return new Utility.Pair(10, 2);
            case "Ljl":
                return new Utility.Pair(11, 3);
            case "LPl":
                return new Utility.Pair(10, 3);
            case "Pln":
                return new Utility.Pair(11, 4);
            case "PSn":
                return new Utility.Pair(10, 4);
            case "Snr":
                return new Utility.Pair(11, 5);
            case "Sqr":
                return new Utility.Pair(10, 5);
            case "RSq":
                return new Utility.Pair(9, 5);
            case "Rpq":
                return new Utility.Pair(8, 5);
            case "QRp":
                return new Utility.Pair(7, 5);
            case "Qop":
                return new Utility.Pair(6, 5);
            case "Qmo":
                return new Utility.Pair(5, 5);
            case "MQm":
                return new Utility.Pair(4, 4);
            case "Mkm":
                return new Utility.Pair(3, 4);
            case "HMk":
                return new Utility.Pair(2, 3);
            case "Hik":
                return new Utility.Pair(1, 3);
            case "Hgi":
                return new Utility.Pair(0, 2);
            case "DHg":
                return new Utility.Pair(1, 2);
            case "Deg":
                return new Utility.Pair(0, 1);
            case "ADe":
                return new Utility.Pair(1, 1);

            case "ADE":
                return new Utility.Pair(2, 1);
            case "ABE":
                return new Utility.Pair(3, 1);
            case "BEF":
                return new Utility.Pair(4, 1);
            case "BCF":
                return new Utility.Pair(5, 1);
            case "CFG":
                return new Utility.Pair(6, 1);
            case "DHI":
                return new Utility.Pair(2, 2);
            case "DEI":
                return new Utility.Pair(3, 2);
            case "EIJ":
                return new Utility.Pair(4, 2);
            case "EFJ":
                return new Utility.Pair(5, 2);
            case "FJK":
                return new Utility.Pair(6, 2);
            case "FGK":
                return new Utility.Pair(7, 2);
            case "GKL":
                return new Utility.Pair(8, 2);
            case "HIM":
                return new Utility.Pair(3, 3);
            case "IMN":
                return new Utility.Pair(4, 3);
            case "IJN":
                return new Utility.Pair(5, 3);
            case "JNO":
                return new Utility.Pair(6, 3);
            case "JKO":
                return new Utility.Pair(7, 3);
            case "KOP":
                return new Utility.Pair(8, 3);
            case "KLP":
                return new Utility.Pair(9, 3);
            case "MNQ":
                return new Utility.Pair(5, 4);
            case "NQR":
                return new Utility.Pair(6, 4);
            case "NOR":
                return new Utility.Pair(7, 4);
            case "ORS":
                return new Utility.Pair(8, 4);
            case "OPS":
                return new Utility.Pair(9, 4);

        }
        System.out.println("KOORDINATE ÜBERGEBEN, DIE IM TRANSLATE NICHT ABGEFRAGT WIRD");
        return null;
    }
   
   
   public static String translateIntersectionCoordinate(Utility.Pair coordinates){

	   if(coordinates.equals(new Utility.Pair(0, 0))){
		   return "Aae";
	   }else if(coordinates.equals(new Utility.Pair(1, 0))){
		   return "Aab";
	   }else if(coordinates.equals(new Utility.Pair(2, 0))){
		   return "ABb";
	   }else if(coordinates.equals(new Utility.Pair(3, 0))){
		   return "Bbc";
	   }else if(coordinates.equals(new Utility.Pair(4, 0))){
		   return "BCc";
	   }else if(coordinates.equals(new Utility.Pair(5, 0))){
		   return "Ccd";
	   }else if(coordinates.equals(new Utility.Pair(6, 0))){
		   return "Cdf";
	   }else if(coordinates.equals(new Utility.Pair(7, 1))){
		   return "CGf";
	   }else if(coordinates.equals(new Utility.Pair(8, 1))){
		   return "Gfh";
	   }else if(coordinates.equals(new Utility.Pair(9, 2))){
		   return "GLh";
	   }else if(coordinates.equals(new Utility.Pair(10, 2))){
		   return "Lhj";
	   }else if(coordinates.equals(new Utility.Pair(11, 3))){
		   return "Ljl";
	   }else if(coordinates.equals(new Utility.Pair(10, 3))){
		   return "LPl";
	   }else if(coordinates.equals(new Utility.Pair(11, 4))){
		   return "Pln";
	   }else if(coordinates.equals(new Utility.Pair(10, 4))){
		   return "PSn";
	   }else if(coordinates.equals(new Utility.Pair(11, 5))){
		   return "Snr";
	   }else if(coordinates.equals(new Utility.Pair(10, 5))){
		   return "Sqr";
	   }else if(coordinates.equals(new Utility.Pair(9, 5))){
		   return "RSq";
	   }else if(coordinates.equals(new Utility.Pair(8, 5))){
		   return "Rpq";
	   }else if(coordinates.equals(new Utility.Pair(7, 5))){
		   return "QRp";
	   }else if(coordinates.equals(new Utility.Pair(6, 5))){
		   return "Qop";
	   }else if(coordinates.equals(new Utility.Pair(5, 5))){
		   return "Qmo";
	   }else if(coordinates.equals(new Utility.Pair(4, 4))){
		   return "MQm";
	   }else if(coordinates.equals(new Utility.Pair(3, 4))){
		   return "Mkm";
	   }else if(coordinates.equals(new Utility.Pair(2, 3))){
		   return "HMk";
	   }else if(coordinates.equals(new Utility.Pair(1, 3))){
		   return "Hik";
	   }else if(coordinates.equals(new Utility.Pair(0, 2))){
		   return "Hgi";
	   }else if(coordinates.equals(new Utility.Pair(1, 2))){
		   return "DHg";
	   }else if(coordinates.equals(new Utility.Pair(0, 1))){
		   return "Deg";
	   }else if(coordinates.equals(new Utility.Pair(1, 1))){
		   return "ADe";
	   }else if(coordinates.equals(new Utility.Pair(2, 1))){
		   return "ADE";
	   }else if(coordinates.equals(new Utility.Pair(3, 1))){
		   return "ABE";
	   }else if(coordinates.equals(new Utility.Pair(4, 1))){
		   return "BEF";
	   }else if(coordinates.equals(new Utility.Pair(5, 1))){
		   return "BCF";
	   }else if(coordinates.equals(new Utility.Pair(6, 1))){
		   return "CFG";
	   }else if(coordinates.equals(new Utility.Pair(2, 2))){
		   return "DHI";
	   }else if(coordinates.equals(new Utility.Pair(3, 2))){
		   return "DEI";
	   }else if(coordinates.equals(new Utility.Pair(4, 2))){
		   return "EIJ";
	   }else if(coordinates.equals(new Utility.Pair(5, 2))){
		   return "EFJ";
	   }else if(coordinates.equals(new Utility.Pair(6, 2))){
		   return "FJK";
	   }else if(coordinates.equals(new Utility.Pair(7, 2))){
		   return "FGK";
	   }else if(coordinates.equals(new Utility.Pair(8, 2))){
		   return "GKL";
	   }else if(coordinates.equals(new Utility.Pair(3, 3))){
		   return "HIM";
	   }else if(coordinates.equals(new Utility.Pair(4, 3))){
		   return "IMN";
	   }else if(coordinates.equals(new Utility.Pair(5, 3))){
		   return "IJN";
	   }else if(coordinates.equals(new Utility.Pair(6, 3))){
		   return "JNO";
	   }else if(coordinates.equals(new Utility.Pair(7, 3))){
		   return "JKO";
	   }else if(coordinates.equals(new Utility.Pair(8, 3))){
		   return "KOP";
	   }else if(coordinates.equals(new Utility.Pair(9, 3))){
		   return "KLP";
	   }else if(coordinates.equals(new Utility.Pair(5, 4))){
		   return "MNQ";
	   }else if(coordinates.equals(new Utility.Pair(6, 4))){
		   return "NQR";
	   }else if(coordinates.equals(new Utility.Pair(7, 4))){
		   return "NOR";
	   }else if(coordinates.equals(new Utility.Pair(8, 4))){
		   return "ORS";
	   }else if(coordinates.equals(new Utility.Pair(9, 4))){
		   return "OPS";
	   }
	   
	return "";	   
   }

   public static Utility.Pair translateIntersectionCoordinate(JsonArray coordinates) {
       // sort the array
       Utility.Pair[] pairs = new Utility.Pair[3];
       for (int i = 0; i < coordinates.size(); i++) {
           int x = coordinates.get(i).getAsJsonObject().get("x").getAsInt();
           int y = coordinates.get(i).getAsJsonObject().get("y").getAsInt();
           Utility.Pair pair = new Utility.Pair(x,y);
           pairs[i] = pair;
       }
       Utility.Pair[] sorted = sortPairs(pairs);

       int x1 = sorted[0].getX();
       int x2 = sorted[1].getX();
       int x3 = sorted[2].getX();

       int y1 = sorted[0].getY();
       int y2 = sorted[1].getY();
       int y3 = sorted[2].getY();

       if (x1 == -3 && y1 == 2 && x2 == -3 && y2 == 3 && x3 == -2 && y3 == 2) {
           return new Utility.Pair(0,0);
       } else if (x1 == -3 && y1 == 3 && x2 == -2 && y2 == 2 && x3 == -2 && y3 == 3) {
           return new Utility.Pair(1,0);
       } else if (x1 == -2 && y1 == 2 && x2 == -2 && y2 == 3 && x3 == -1 && y3 == 2) {
           return new Utility.Pair(2,0);
       } else if (x1 == -2 && y1 == 3 && x2 == -1 && y2 == 2 && x3 == -1 && y3 == 3) {
           return new Utility.Pair(3,0);
       } else if (x1 == -1 && y1 == 2 && x2 == -1 && y2 == 3 && x3 == 0 && y3 == 2) {
           return new Utility.Pair(4,0);
       } else if (x1 == -1 && y1 == 3 && x2 == 0 && y2 == 2 && x3 == 0 && y3 == 3) {
           return new Utility.Pair(5,0);
       } else if (x1 == 0 && y1 == 2 && x2 == 0 && y2 == 3 && x3 == 1 && y3 == 2) {
           return new Utility.Pair(6,0);
       } else if (x1 == -3 && y1 == 1 && x2 == -3 && y2 == 2 && x3 == -2 && y3 == 1) {
           return new Utility.Pair(0,1);
       } else if (x1 == -3 && y1 == 2 && x2 == -2 && y2 == 1 && x3 == -2 && y3 == 2) {
           return new Utility.Pair(1,1);
       } else if (x1 == -2 && y1 == 1 && x2 == -2 && y2 == 2 && x3 == -1 && y3 == 1) {
           return new Utility.Pair(2,1);
       } else if (x1 == -2 && y1 == 2 && x2 == -1 && y2 == 1 && x3 == -1 && y3 == 2) {
           return new Utility.Pair(3,1);
       } else if (x1 == -1 && y1 == 1 && x2 == -1 && y2 == 2 && x3 == 0 && y3 == 1) {
           return new Utility.Pair(4,1);
       } else if (x1 == -1 && y1 == 2 && x2 == 0 && y2 == 1 && x3 == 0 && y3 == 2) {
           return new Utility.Pair(5,1);
       } else if (x1 == 0 && y1 == 1 && x2 == 0 && y2 == 2 && x3 == 1 && y3 == 1) {
           return new Utility.Pair(6,1);
       } else if (x1 == 0 && y1 == 2 && x2 == 1 && y2 == 1 && x3 == 1 && y3 == 2) {
           return new Utility.Pair(7,1);
       } else if (x1 == 1 && y1 == 1 && x2 == 1 && y2 == 2 && x3 == 2 && y3 == 1) {
           return new Utility.Pair(8,1);
       } else if (x1 == -3 && y1 == 0 && x2 == -3 && y2 == 1 && x3 == -2 && y3 == 0) {
           return new Utility.Pair(0,2);
       } else if (x1 == -3 && y1 == 1 && x2 == -2 && y2 == 0 && x3 == -2 && y3 == 1) {
           return new Utility.Pair(1,2);
       } else if (x1 == -2 && y1 == 0 && x2 == -2 && y2 == 1 && x3 == -1 && y3 == 0) {
           return new Utility.Pair(2,2);
       } else if (x1 == -2 && y1 == 1 && x2 == -1 && y2 == 0 && x3 == -1 && y3 == 1) {
           return new Utility.Pair(3,2);
       } else if (x1 == -1 && y1 == 0 && x2 == -1 && y2 == 1 && x3 == 0 && y3 == 0) {
           return new Utility.Pair(4,2);
       } else if (x1 == -1 && y1 == 1 && x2 == 0 && y2 == 0 && x3 == 0 && y3 == 1) {
           return new Utility.Pair(5,2);
       } else if (x1 == 0 && y1 == 0 && x2 == 0 && y2 == 1 && x3 == 1 && y3 == 0) {
           return new Utility.Pair(6,2);
       } else if (x1 == 0 && y1 == 1 && x2 == 1 && y2 == 0 && x3 == 1 && y3 == 1) {
           return new Utility.Pair(7,2);
       } else if (x1 == 1 && y1 == 0 && x2 == 1 && y2 == 1 && x3 == 2 && y3 == 0) {
           return new Utility.Pair(8,2);
       } else if (x1 == 1 && y1 == 1 && x2 == 2 && y2 == 0 && x3 == 2 && y3 == 1) {
           return new Utility.Pair(9,2);
       } else if (x1 == 2 && y1 == 0 && x2 == 2 && y2 == 1 && x3 == 3 && y3 == 0) {
           return new Utility.Pair(10,2);
       } else if (x1 == -3 && y1 == 0 && x2 == -2 && y2 == -1 && x3 == -2 && y3 == 0) {
           return new Utility.Pair(1,3);
       } else if (x1 == -2 && y1 == -1 && x2 == -2 && y2 == 0 && x3 == -1 && y3 == -1) {
           return new Utility.Pair(2,3);
       } else if (x1 == -2 && y1 == 0 && x2 == -1 && y2 == -1 && x3 == -1 && y3 == 0) {
           return new Utility.Pair(3,3);
       } else if (x1 == -1 && y1 == -1 && x2 == -1 && y2 == 0 && x3 == 0 && y3 == -1) {
           return new Utility.Pair(4,3);
       } else if (x1 == -1 && y1 == 0 && x2 == 0 && y2 == -1 && x3 == 0 && y3 == 0) {
           return new Utility.Pair(5,3);
       } else if (x1 == 0 && y1 == -1 && x2 == 0 && y2 == 0 && x3 == 1 && y3 == -1) {
           return new Utility.Pair(6,3);
       } else if (x1 == 0 && y1 == 0 && x2 == 1 && y2 == -1 && x3 == 1 && y3 == 0) {
           return new Utility.Pair(7,3);
       } else if (x1 == 1 && y1 == -1 && x2 == 1 && y2 == 0 && x3 == 2 && y3 == -1) {
           return new Utility.Pair(8,3);
       } else if (x1 == 1 && y1 == 0 && x2 == 2 && y2 == -1 && x3 == 2 && y3 == 0) {
           return new Utility.Pair(9,3);
       } else if (x1 == 2 && y1 == -1 && x2 == 2 && y2 == 0 && x3 == 3 && y3 == -1) {
           return new Utility.Pair(10,3);
       } else if (x1 == 2 && y1 == 0 && x2 == 3 && y2 == -1 && x3 == 3 && y3 == 0) {
           return new Utility.Pair(11,3);
       } else if (x1 == -2 && y1 == -1 && x2 == -1 && y2 == -2 && x3 == -1 && y3 == -1) {
           return new Utility.Pair(3,4);
       } else if (x1 == -1 && y1 == -2 && x2 == -1 && y2 == -1 && x3 == 0 && y3 == -2) {
           return new Utility.Pair(4,4);
       } else if (x1 == -1 && y1 == -1 && x2 == 0 && y2 == -2 && x3 == 0 && y3 == -1) {
           return new Utility.Pair(5,4);
       } else if (x1 == 0 && y1 == -2 && x2 == 0 && y2 == -1 && x3 == 1 && y3 == -2) {
           return new Utility.Pair(6,4);
       } else if (x1 == 0 && y1 == -1 && x2 == 1 && y2 == -2 && x3 == 1 && y3 == -1) {
           return new Utility.Pair(7,4);
       } else if (x1 == 1 && y1 == -2 && x2 == 1 && y2 == -1 && x3 == 2 && y3 == -2) {
           return new Utility.Pair(8,4);
       } else if (x1 == 1 && y1 == -1 && x2 == 2 && y2 == -2 && x3 == 2 && y3 == -1) {
           return new Utility.Pair(9,4);
       } else if (x1 == 2 && y1 == -2 && x2 == 2 && y2 == -1 && x3 == 3 && y3 == -2) {
           return new Utility.Pair(10,4);
       } else if (x1 == 2 && y1 == -1 && x2 == 3 && y2 == -2 && x3 == 3 && y3 == -1) {
           return new Utility.Pair(11,4);
       } else if (x1 == -1 && y1 == -2 && x2 == 0 && y2 == -3 && x3 == 0 && y3 == -2) {
           return new Utility.Pair(5,5);
       } else if (x1 == 0 && y1 == -3 && x2 == 0 && y2 == -2 && x3 == 1 && y3 == -3) {
           return new Utility.Pair(6,5);
       } else if (x1 == 0 && y1 == -2 && x2 == 1 && y2 == -3 && x3 == 1 && y3 == -2) {
           return new Utility.Pair(7,5);
       } else if (x1 == 1 && y1 == -3 && x2 == 1 && y2 == -2 && x3 == 2 && y3 == -3) {
           return new Utility.Pair(8,5);
       } else if (x1 == 1 && y1 == -2 && x2 == 2 && y2 == -3 && x3 == 2 && y3 == -2) {
           return new Utility.Pair(9,5);
       } else if (x1 == 2 && y1 == -3 && x2 == 2 && y2 == -2 && x3 == 3 && y3 == -3) {
           return new Utility.Pair(10,5);
       } else if (x1 == 2 && y1 == -2 && x2 == 3 && y2 == -3 && x3 == 3 && y3 == -2) {
           return new Utility.Pair(11,5);
       } else {
           System.out.println("FEHLER IN TRANSLATE INTERSECTION COORDINATE, PROTOKOL 1.0");
           return null;
       }

   }

   public static Utility.Pair[] translateIntersectionCoordinateToLandpieces(Utility.Pair coordinates) {
       if (coordinates.equals(new Utility.Pair(0, 0))) {
           return new Utility.Pair[]{new Utility.Pair(-3, 2), new Utility.Pair(-3, 3), new Utility.Pair(-2, 2)};
       } else if(coordinates.equals(new Utility.Pair(1, 0))) {
           return new Utility.Pair[]{new Utility.Pair(-3, 3), new Utility.Pair(-2, 2), new Utility.Pair(-2, 3)};
       } else if(coordinates.equals(new Utility.Pair(2, 0))) {
           return new Utility.Pair[]{new Utility.Pair(-2, 2), new Utility.Pair(-2, 3), new Utility.Pair(-1, 2)};
       } else if(coordinates.equals(new Utility.Pair(3, 0))) {
           return new Utility.Pair[]{new Utility.Pair(-2, 3), new Utility.Pair(-1, 2), new Utility.Pair(-1, 3)};
       } else if(coordinates.equals(new Utility.Pair(4, 0))) {
           return new Utility.Pair[]{new Utility.Pair(-1, 2), new Utility.Pair(-1, 3), new Utility.Pair(0, 2)};
       } else if(coordinates.equals(new Utility.Pair(5, 0))) {
           return new Utility.Pair[]{new Utility.Pair(-1, 3), new Utility.Pair(0, 2), new Utility.Pair(0, 3)};
       } else if(coordinates.equals(new Utility.Pair(6, 0))) {
           return new Utility.Pair[]{new Utility.Pair(0, 2), new Utility.Pair(0, 3), new Utility.Pair(1, 2)};
       } else if(coordinates.equals(new Utility.Pair(0, 1))) {
           return new Utility.Pair[]{new Utility.Pair(-3, 1), new Utility.Pair(-3, 2), new Utility.Pair(-2, 1)};
       } else if(coordinates.equals(new Utility.Pair(1, 1))) {
           return new Utility.Pair[]{new Utility.Pair(-3, 2), new Utility.Pair(-2, 1), new Utility.Pair(-2, 2)};
       } else if(coordinates.equals(new Utility.Pair(2, 1))) {
           return new Utility.Pair[]{new Utility.Pair(-2, 1), new Utility.Pair(-2, 2), new Utility.Pair(-1, 1)};
       } else if(coordinates.equals(new Utility.Pair(3, 1))) {
           return new Utility.Pair[]{new Utility.Pair(-2, 2), new Utility.Pair(-1, 1), new Utility.Pair(-1, 2)};
       } else if(coordinates.equals(new Utility.Pair(4, 1))) {
           return new Utility.Pair[]{new Utility.Pair(-1, 1), new Utility.Pair(-1, 2), new Utility.Pair(0, 1)};
       } else if(coordinates.equals(new Utility.Pair(5, 1))) {
           return new Utility.Pair[]{new Utility.Pair(-1, 2), new Utility.Pair(0, 1), new Utility.Pair(0, 2)};
       } else if(coordinates.equals(new Utility.Pair(6, 1))) {
           return new Utility.Pair[]{new Utility.Pair(0, 1), new Utility.Pair(0, 2), new Utility.Pair(1, 1)};
       } else if(coordinates.equals(new Utility.Pair(7, 1))) {
           return new Utility.Pair[]{new Utility.Pair(0, 2), new Utility.Pair(1, 1), new Utility.Pair(1, 2)};
       } else if(coordinates.equals(new Utility.Pair(8, 1))) {
           return new Utility.Pair[]{new Utility.Pair(1, 1), new Utility.Pair(1, 2), new Utility.Pair(2, 1)};
       } else if(coordinates.equals(new Utility.Pair(0, 2))) {
           return new Utility.Pair[]{new Utility.Pair(-3, 0), new Utility.Pair(-3, 1), new Utility.Pair(-2, 0)};
       } else if(coordinates.equals(new Utility.Pair(1, 2))) {
           return new Utility.Pair[]{new Utility.Pair(-3, 1), new Utility.Pair(-2, 0), new Utility.Pair(-2, 1)};
       } else if(coordinates.equals(new Utility.Pair(2, 2))) {
           return new Utility.Pair[]{new Utility.Pair(-2, 0), new Utility.Pair(-2, 1), new Utility.Pair(-1, 0)};
       } else if(coordinates.equals(new Utility.Pair(3, 2))) {
           return new Utility.Pair[]{new Utility.Pair(-2, 1), new Utility.Pair(-1, 0), new Utility.Pair(-1, 1)};
       } else if(coordinates.equals(new Utility.Pair(4, 2))) {
           return new Utility.Pair[]{new Utility.Pair(-1, 0), new Utility.Pair(-1, 1), new Utility.Pair(0, 0)};
       } else if(coordinates.equals(new Utility.Pair(5, 2))) {
           return new Utility.Pair[]{new Utility.Pair(-1, 1), new Utility.Pair(0, 0), new Utility.Pair(0, 1)};
       } else if(coordinates.equals(new Utility.Pair(6, 2))) {
           return new Utility.Pair[]{new Utility.Pair(0, 0), new Utility.Pair(0, 1), new Utility.Pair(1, 0)};
       } else if(coordinates.equals(new Utility.Pair(7, 2))) {
           return new Utility.Pair[]{new Utility.Pair(0, 1), new Utility.Pair(1, 0), new Utility.Pair(1, 1)};
       } else if(coordinates.equals(new Utility.Pair(8, 2))) {
           return new Utility.Pair[]{new Utility.Pair(1, 0), new Utility.Pair(1, 1), new Utility.Pair(2, 0)};
       } else if(coordinates.equals(new Utility.Pair(9, 2))) {
           return new Utility.Pair[]{new Utility.Pair(1, 1), new Utility.Pair(2, 0), new Utility.Pair(2, 1)};
       } else if(coordinates.equals(new Utility.Pair(10, 2))) {
           return new Utility.Pair[]{new Utility.Pair(2, 0), new Utility.Pair(2, 1), new Utility.Pair(3, 0)};
       } else if(coordinates.equals(new Utility.Pair(1, 3))) {
           return new Utility.Pair[]{new Utility.Pair(-3, 0), new Utility.Pair(-2, -1), new Utility.Pair(-2, 0)};
       } else if(coordinates.equals(new Utility.Pair(2, 3))) {
           return new Utility.Pair[]{new Utility.Pair(-2, -1), new Utility.Pair(-2, 0), new Utility.Pair(-1, -1)};
       } else if(coordinates.equals(new Utility.Pair(3, 3))) {
           return new Utility.Pair[]{new Utility.Pair(-2, 0), new Utility.Pair(-1, -1), new Utility.Pair(-1, 0)};
       } else if(coordinates.equals(new Utility.Pair(4, 3))) {
           return new Utility.Pair[]{new Utility.Pair(-1, -1), new Utility.Pair(-1, 0), new Utility.Pair(0, -1)};
       } else if(coordinates.equals(new Utility.Pair(5, 3))) {
           return new Utility.Pair[]{new Utility.Pair(-1, 0), new Utility.Pair(0, -1), new Utility.Pair(0, 0)};
       } else if(coordinates.equals(new Utility.Pair(6, 3))) {
           return new Utility.Pair[]{new Utility.Pair(0, -1), new Utility.Pair(0, 0), new Utility.Pair(1, -1)};
       } else if(coordinates.equals(new Utility.Pair(7, 3))) {
           return new Utility.Pair[]{new Utility.Pair(0, 0), new Utility.Pair(1, -1), new Utility.Pair(1, 0)};
       } else if(coordinates.equals(new Utility.Pair(8, 3))) {
           return new Utility.Pair[]{new Utility.Pair(1, -1), new Utility.Pair(1, 0), new Utility.Pair(2, -1)};
       } else if(coordinates.equals(new Utility.Pair(9, 3))) {
           return new Utility.Pair[]{new Utility.Pair(1, 0), new Utility.Pair(2, -1), new Utility.Pair(2, 0)};
       } else if(coordinates.equals(new Utility.Pair(10, 3))) {
           return new Utility.Pair[]{new Utility.Pair(2, -1), new Utility.Pair(2, 0), new Utility.Pair(3, -1)};
       } else if(coordinates.equals(new Utility.Pair(11, 3))) {
           return new Utility.Pair[]{new Utility.Pair(2, 0), new Utility.Pair(3, -1), new Utility.Pair(3, 0)};
       } else if(coordinates.equals(new Utility.Pair(3, 4))) {
           return new Utility.Pair[]{new Utility.Pair(-2, -1), new Utility.Pair(-1, -2), new Utility.Pair(-1, -1)};
       } else if(coordinates.equals(new Utility.Pair(4, 4))) {
           return new Utility.Pair[]{new Utility.Pair(-1, -2), new Utility.Pair(-1, -1), new Utility.Pair(0, -2)};
       } else if(coordinates.equals(new Utility.Pair(5, 4))) {
           return new Utility.Pair[]{new Utility.Pair(-1, -1), new Utility.Pair(0, -2), new Utility.Pair(0, -1)};
       } else if(coordinates.equals(new Utility.Pair(6, 4))) {
           return new Utility.Pair[]{new Utility.Pair(0, -2), new Utility.Pair(0, -1), new Utility.Pair(1, -2)};
       } else if(coordinates.equals(new Utility.Pair(7, 4))) {
           return new Utility.Pair[]{new Utility.Pair(0, -1), new Utility.Pair(1, -2), new Utility.Pair(1, -1)};
       } else if(coordinates.equals(new Utility.Pair(8, 4))) {
           return new Utility.Pair[]{new Utility.Pair(1, -2), new Utility.Pair(1, -1), new Utility.Pair(2, -2)};
       } else if(coordinates.equals(new Utility.Pair(9, 4))) {
           return new Utility.Pair[]{new Utility.Pair(1, -1), new Utility.Pair(2, -2), new Utility.Pair(2, -1)};
       } else if(coordinates.equals(new Utility.Pair(10, 4))) {
           return new Utility.Pair[]{new Utility.Pair(2, -2), new Utility.Pair(2, -1), new Utility.Pair(3, -2)};
       } else if(coordinates.equals(new Utility.Pair(11, 4))) {
           return new Utility.Pair[]{new Utility.Pair(2, -1), new Utility.Pair(3, -2), new Utility.Pair(3, -1)};
       } else if(coordinates.equals(new Utility.Pair(5, 5))) {
           return new Utility.Pair[]{new Utility.Pair(-1, -2), new Utility.Pair(0, -3), new Utility.Pair(0, -2)};
       } else if(coordinates.equals(new Utility.Pair(6, 5))) {
           return new Utility.Pair[]{new Utility.Pair(0, -3), new Utility.Pair(0, -2), new Utility.Pair(1, -3)};
       } else if(coordinates.equals(new Utility.Pair(7, 5))) {
           return new Utility.Pair[]{new Utility.Pair(0, -2), new Utility.Pair(1, -3), new Utility.Pair(1, -2)};
       } else if(coordinates.equals(new Utility.Pair(8, 5))) {
           return new Utility.Pair[]{new Utility.Pair(1, -3), new Utility.Pair(1, -2), new Utility.Pair(2, -3)};
       } else if(coordinates.equals(new Utility.Pair(9, 5))) {
           return new Utility.Pair[]{new Utility.Pair(1, -2), new Utility.Pair(2, -3), new Utility.Pair(2, -2)};
       } else if(coordinates.equals(new Utility.Pair(10, 5))) {
           return new Utility.Pair[]{new Utility.Pair(2, -3), new Utility.Pair(2, -2), new Utility.Pair(3, -3)};
       } else if(coordinates.equals(new Utility.Pair(11, 5))) {
           return new Utility.Pair[]{new Utility.Pair(2, -2), new Utility.Pair(3, -3), new Utility.Pair(3, -2)};
       } else {
           System.out.println("FEHLER WÄHREND AUS INTERSECTION DIE DREI LANDFELDER BERECHNET WERDEN");
           return null;
       }

   }

   public static JsonObject translateResourceObject(Resource[] resources) {
       JsonObject translatedResources = new JsonObject();
       translatedResources.addProperty("Holz", resources[0].getValue());
       translatedResources.addProperty("Lehm", resources[1].getValue());
       translatedResources.addProperty("Wolle", resources[2].getValue());
       translatedResources.addProperty("Getreide", resources[3].getValue());
       translatedResources.addProperty("Erz", resources[4].getValue());
       return translatedResources;
   }

    public static Resource[] translateResourceObject(JsonObject resources) {
        Resource[] translatedResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        translatedResources[0].setValue(resources.has("Holz") ? resources.get("Holz").getAsInt() : 0);
        translatedResources[1].setValue(resources.has("Lehm") ? resources.get("Lehm").getAsInt() : 0);
        translatedResources[2].setValue(resources.has("Wolle") ? resources.get("Wolle").getAsInt() : 0);
        translatedResources[3].setValue(resources.has("Getreide") ? resources.get("Getreide").getAsInt() : 0);
        translatedResources[4].setValue(resources.has("Erz") ? resources.get("Erz").getAsInt() : 0);
        return translatedResources;
    }

   public static ResourceType translateLandpieceTypeToResourceType(LandpieceType landpieceType) {
       switch (landpieceType) {
           case PASTURES:
               return ResourceType.WOOL;
           case FOREST:
               return ResourceType.LUMBER;
           case FIELDS:
               return ResourceType.GRAIN;
           case MOUNTAINS:
               return ResourceType.ORE;
           case HILLS:
               return ResourceType.BRICK;
           default:
               return null;
       }
   }

   public static JsonArray translatePairArray(ArrayList<Utility.Pair> pairArray) {
       JsonArray toReturn = new JsonArray();

       for (Utility.Pair pair : pairArray) {
           JsonObject jsonPair = new JsonObject();
           jsonPair.addProperty("x", pair.getX());
           jsonPair.addProperty("y", pair.getY());
           toReturn.add(jsonPair);
       }

       return toReturn;
   }

    public static JsonArray translatePairArray(Utility.Pair[] pairArray) {
        JsonArray toReturn = new JsonArray();

        for (Utility.Pair pair : pairArray) {
            JsonObject jsonPair = new JsonObject();
            jsonPair.addProperty("x", pair.getX());
            jsonPair.addProperty("y", pair.getY());
            toReturn.add(jsonPair);
        }

        return toReturn;
    }

    public static JsonObject translateDevelopmentCards(ArrayList<String> cards) {
        int knight = 0;
        int monopoly = 0;
        int roadBuilding = 0;
        int yearOfPlenty = 0;
        int victoryPoint = 0;

        for (String card : cards) {
            switch (card) {
                case "knight":
                    knight++;
                    break;
                case "monopoly":
                    monopoly++;
                    break;
                case "roadbuilding":
                    roadBuilding++;
                    break;
                case "yearofplenty":
                    yearOfPlenty++;
                    break;
                case "victorypoint":
                    victoryPoint++;
                    break;
            }
        }

        JsonObject devCards = new JsonObject();
        devCards.addProperty("Ritter", knight);
        devCards.addProperty("Straßenbau", roadBuilding);
        devCards.addProperty("Monopol", monopoly);
        devCards.addProperty("Erfindung", yearOfPlenty);
        devCards.addProperty("Siegpunkt", victoryPoint);

        return devCards;

    }

    public static String translateResourceName(String type){
        String typeGerman = "";
        switch (type){
            case "wool":
                typeGerman = "Wolle";
                break;
            case "lumber":
                typeGerman = "Holz";
                break;
            case "grain":
                typeGerman = "Getreide";
                break;
            case "ore":
                typeGerman = "Erz";
                break;
            case "brick":
                typeGerman = "Lehm";
                break;
        }
        return typeGerman;
    }

}
