package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Creates all demanded objects for protocol
 */
public class JsonObjectCreator {

    public static JsonObject createLandpieceObject(String location, String resource, int diceNumber) {
        JsonObject landpiece = new JsonObject();
        landpiece.addProperty("Ort", location);
        landpiece.addProperty("Typ", resource);
        landpiece.addProperty("Zahl", diceNumber);
        return landpiece;
    }

    /**
     * (6.1) overloaded method Protocol10
     * @param location
     * @param resource
     * @param diceNumber
     * @return
     */
    public static JsonObject createLandpieceObject(JsonObject location, String resource, int diceNumber) {
        JsonObject landpiece = new JsonObject();
        landpiece.add("Ort", location);
        landpiece.addProperty("Typ", resource);
        landpiece.addProperty("Zahl", diceNumber);
        return landpiece;
    }

    public static JsonObject createBuildingObject(int owner, String type, String location) {
        JsonObject building = new JsonObject();
        building.addProperty("Eigentümer", owner);
        building.addProperty("Typ", type);
        building.addProperty("Ort", location);
        return building;
    }

    /**
     * (6.2) overloaded method Protocol10
     * @param owner
     * @param type
     * @param location
     * @return
     */
    public static JsonObject createBuildingObject(int owner, String type, JsonArray location) {
        JsonObject building = new JsonObject();
        building.addProperty("Eigentümer", owner);
        building.addProperty("Typ", type);
        building.add("Ort", location);
        return building;
    }

    public static JsonObject createHarborObject(String location, String resource) {
        JsonObject harbor = new JsonObject();
        harbor.addProperty("Ort", location);
        harbor.addProperty("Typ", resource);
        return harbor;
    }

    /**
     * (6.3) overloaded method Protocol10
     * @param location
     * @param resource
     * @return
     */
    public static JsonObject createHarborObject(JsonArray location, String resource) {
        JsonObject harbor = new JsonObject();
        harbor.add("Ort", location);
        harbor.addProperty("Typ", resource);
        return harbor;
    }

    public static JsonObject createBoardObject(JsonArray landpieces, JsonArray buildings, JsonArray harbors, String robber) {
        JsonObject board = new JsonObject();
        board.add("Felder", landpieces);
        board.add("Gebäude", buildings);
        board.add("Häfen", harbors);
        board.addProperty("Räuber",robber);
        return board;
    }

    /**
     * (6.3) overloaded method Protocol10
     * @param landpieces
     * @param buildings
     * @param harbors
     * @param robber
     * @return
     */
    public static JsonObject createBoardObject(JsonArray landpieces, JsonArray buildings, JsonArray harbors, JsonObject robber) {
        JsonObject board = new JsonObject();
        board.add("Felder", landpieces);
        board.add("Gebäude", buildings);
        board.add("Häfen", harbors);
        board.add("Räuber",robber);
        return board;
    }

    public static JsonObject createPlayerObject(int id, String name, String color, String state, int victoryPoints, JsonObject resources) {
        JsonObject player = new JsonObject();
        player.addProperty("id", id);
        player.addProperty("Name", name);
        player.addProperty("Farbe", color);
        player.addProperty("Status", state);
        player.addProperty("Siegpunkte", victoryPoints);
        player.add("Rohstoffe", resources);
        return player;
    }

    public static JsonObject createPlayerObject(int id, String color, String name, String state, int victoryPoints, JsonObject resources, int knightForce, JsonObject developmentCards, boolean biggestKnightForce, boolean longestStreet) {
        JsonObject player = new JsonObject();
        player.addProperty("id", id);
        player.addProperty("Farbe", color);
        player.addProperty("Name", name);
        player.addProperty("Status", state);
        player.addProperty("Siegpunkte", victoryPoints);
        player.add("Rohstoffe", resources);
        player.addProperty("Rittermacht",knightForce);
        player.add("Entwicklungskarten", developmentCards);
        player.addProperty("Größte Rittermacht", biggestKnightForce);
        player.addProperty("Längste Handelsstraße", longestStreet);
        return player;
    }

    public static JsonObject createResourcesObject(int lumber, int brick, int wool, int grain, int ore) {
        JsonObject resources = new JsonObject();
        resources.addProperty("Holz", lumber);
        resources.addProperty("Lehm", brick);
        resources.addProperty("Wolle", wool);
        resources.addProperty("Getreide", grain);
        resources.addProperty("Erz", ore);
        return resources;
    }

    public static JsonObject createAnonymousResourcesObject(int total) {
        JsonObject resources = new JsonObject();
        resources.addProperty("Unbekannt", total);
        return resources;
    }

    public static JsonObject createDevelopmentCards(int knight, int buildStreet, int monopol, int invention, int victoryPoints){
        JsonObject developmentCards = new JsonObject();
        developmentCards.addProperty("Ritter", knight);
        developmentCards.addProperty("Straßenbau", buildStreet);
        developmentCards.addProperty("Monopol", monopol);
        developmentCards.addProperty("Erfindung", invention);
        developmentCards.addProperty("Siegpunkt", victoryPoints);
        return developmentCards;
    }

}
