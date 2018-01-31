package network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Created by Tami on 09.01.2017.
 */
public class Protocol10 extends Protocol03 implements Protocol {

    /**
     * (9.5)
     * Send out from Server when Robber was moved, idRobbedPlayer is OPTIONAL
     * @param id
     * @param location
     * @param idRobbedPlayer
     * @return
     */
    @Override
    public JsonObject robberMovedToClient(int id, JsonObject location, int idRobbedPlayer) {
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", id);
        info.add("Ort", location);
        if (idRobbedPlayer != -1) {
            info.addProperty("Ziel", idRobbedPlayer);
        }
        response.add("Räuber versetzt", info);
        return response;
    }


    /**
     * (10.3)
     * Client sends out a message with the new position of the robber and OPTIONAL the id of the player who is being robbed
     *
     * @param location
     * @param idRobbedPlayer
     * @return Object with new robber location and optional idRobbedPlayer
     */
    //@Override
    public JsonObject moveRobberToServer(JsonObject location, int idRobbedPlayer) {
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.add("Ort", location);
        if(idRobbedPlayer != -1) {
            info.addProperty("Ziel", idRobbedPlayer);
        }
        response.add("Räuber versetzen", info);
        return response;
    }

    /**
     * (10.5)
     * the message is send out when a village or city is to be build
     * @param type
     * @param location
     * @return object with type (street, settlement, city) and it's location
     */
    //@Override
    public JsonObject build(String type, JsonArray location){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Typ", type);
        info.add("Ort", location);
        response.add("Bauen", info);
        return response;
    }

    /**
     * (11.2) accept trade offer
     * Player wants to accept an offer and sends it out to server
     * more than one Player can accept the offer
     * @param tradeId
     * @return  Object with tradeId
     */
    //@Override
    public JsonObject acceptOfferToServer(int tradeId, boolean accept){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Handel id", tradeId);
        info.addProperty("Annehmen", accept);
        response.add("Handel annehmen", info);
        return response;
    }

    /**
     * (12.2) accept trade offer
     * Server sends out the Offers from the Clients to all other Clients
     * more than one Player can accept the offer
     * @param id
     * @param tradeId
     * @return Object with PlayerId and the tradeId
     */
    //@Override
    public JsonObject acceptOfferToClient(int id, int tradeId, boolean accept){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Mitspieler", id);
        info.addProperty("Handel id", tradeId);
        info.addProperty("Annehmen", accept);
        response.add("Handelsangebot angenommen", info);
        return response;
    }

    /**
     * (12.1)
     *  Client plays knight card
     * @param location
     * @param destinationId
     * @return
     */
    //@Override
    public JsonObject playKnightCardToServer(JsonObject location, int destinationId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.add("Ort", location);
        if(destinationId != -1) {
            info.addProperty("Ziel", destinationId);
        }
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
    //@Override
    public JsonObject playKnightCardToClient(int playerId, JsonObject location, int destinationId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", playerId);
        info.add("Ort", location);
        if(destinationId != -1) {
            info.addProperty("Ziel", destinationId);
        }
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
    //@Override
    public JsonObject playBuildStreetCardToServer(JsonArray street1, JsonArray street2){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.add("Straße 1", street1);
        if (street2 != null) {
            info.add("Straße 2", street2);
        }
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
    //@Override
    public JsonObject playBuildStreetCardToClient(int playerId, JsonArray street1, JsonArray street2){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", playerId);
        info.add("Straße 1", street1);
        if (street2 != null) {
            info.add("Straße 2", street2);
        }
        response.add("Straßenbaukarte ausspielen", info);
        return response;
    }


}