package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;


public class Protocol03 extends Protocol02 implements Protocol{

    /**
     * (9.2)
     *
     * @param player
     * @param diceNumber
     * @return
     */
    @Override
    public JsonObject diceNumberToClient(int player, int[] diceNumber){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        JsonArray diceArray = new JsonArray();
        diceArray.add(diceNumber[0]);
        diceArray.add(diceNumber[1]);
        info.addProperty("Spieler", player);
        info.add("Wurf", diceArray);
        response.add("Würfelwurf", info);
        return response;
    }

    /**
     * (9.7)
     * when a player buys a development card, he gets a message
     * @param id
     * @param developmentCard
     * @return
     */
    @Override
    public JsonObject playerBoughtDevCard(int id, String developmentCard){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", id);
        info.addProperty("Entwicklungskarte", developmentCard);
        response.add("Entwicklungskarte gekauft", info);
        return response;
    }

    /**
     * (9.10)
     * when a player gets the longest road
     * @param id
     * @return
     */
    @Override
    public JsonObject longestRoadToClient(int id){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        if (id != -1) {
            info.addProperty("Spieler", id);
        }
        response.add("Längste Handelsstraße", info);
        return response;
    }

    /**
     * (9.10)
     * @param id
     * @return
     */
    @Override
    public JsonObject biggestKnightForceToClient(int id){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", id);
        response.add("Größte Rittermacht", info);
        return response;
    }

    /**
     * (9.10)
     * when a client loses the longest road and no other player gets the longest road
     * @return
     */
    @Override
    public JsonObject lostLongestRoadToClient(){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        response.add("Längste Handelsstraße", info);
        return response;
    }

    /**
     * (10.5)
     * when a client wants to buy a development card
     * @return
     */
    @Override
    public JsonObject buyDevelopmentCard(){
        JsonObject response = new JsonObject();
        response.add("Entwicklungskarte kaufen", new JsonObject());
        return response;
    }

    /**
     * (12.1)
     * Client plays knight card
     * @param location
     * @param destinationId
     * @return
     */
    @Override
    public JsonObject playKnightCardToServer(String location, int destinationId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Ort", location);
        info.addProperty("Ziel", destinationId);
        response.add("Ritter ausspielen", info);
        return response;
    }

    /**
     * (12.1)
     * Client plays knight card and Server adds the field Spieler
     * @param location
     * @param destinationId
     * @param playerId
     * @return
     */
    @Override
    public JsonObject playKnightCardToClient(int playerId, String location, int destinationId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", playerId);
        info.addProperty("Ort", location);
        info.addProperty("Ziel", destinationId);
        response.add("Ritter ausspielen", info);
        return response;
    }

    /**
     * (12.2)
     * Client plays build street card, street 2 is optional
     * @param street1
     * @param street2
     * @return
     */
    @Override
    public JsonObject playBuildStreetCardToServer(String street1, String street2){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Straße 1", street1);
        info.addProperty("Straße 2", street2);
        response.add("Straßenbaukarte ausspielen", info);
        return response;
    }

    /**
     * (12.2)
     * Client plays build street card, street 2 is optional
     * Server adds player id
     * @param street1
     * @param street2
     * @param playerId
     * @return
     */
    @Override
    public JsonObject playBuildStreetCardToClient(int playerId, String street1, String street2){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", playerId);
        info.addProperty("Straße 1", street1);
        info.addProperty("Straße 2", street2);
        response.add("Straßenbaukarte ausspielen", info);
        return response;
    }

    /**
     *(12.3)
     * @param resource
     * @return
     */
    public JsonObject playMonopolCardToServer(String resource) {
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Rohstoff", resource);
        response.add("Monopol", info);
        return response;
    }

    /**
     * (12.3)
     * Server adds info playerId
     * @param resource
     * @param playerId
     * @return
     */
    public JsonObject playMonopolCardToClient(int playerId, String resource) {
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", playerId);
        info.addProperty("Rohstoff", resource);
        response.add("Monopol", info);
        return response;
    }

    /**
     * (12.4)
     * @param resources
     * @return
     */
    @Override
    public JsonObject playYearOfPlentyCardToServer(JsonObject resources){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.add("Rohstoffe", resources);
        response.add("Erfindung", info);
        return response;
    }

    /**
     * (12.4)
     * Server adds info playerId
     * @param resources
     * @param playerId
     * @return
     */
    @Override
    public JsonObject playYearOfPlentyCardToClient(int playerId, JsonObject resources){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", playerId);
        info.add("Rohstoffe", resources);
        response.add("Erfindung", info);
        return response;
    }

    //--------------------------Empty methods from Protocol10
    @Override
    public JsonObject robberMovedToClient(int id, JsonObject location, int idRobbedPlayer){ return null; }

}
