package network;


import com.google.gson.JsonObject;

import java.util.Arrays;

public class Protocol02 extends Protocol01 implements Protocol{

    /**
     * (9)
     * Server sends out message, when a player has won the game
     * @param message
     * @param id
     * @return object with winner and message
     */
    @Override
    public JsonObject sendPlayerWinsToClient(String message, int id){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Nachricht", message);
        info.addProperty("Sieger",id);
        response.add("Spiel beendet", info);
        return response;
    }

    /**
     * (10.4)
     * Server sends Client a Message about the costs for a move like building a Settlement or Robber
     * @param id
     * @param resources
     * @return object with player id, resources and the expenses
     */
    @Override
    public JsonObject sendExpenseToClient(int id, JsonObject resources){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", id);
        info.add("Rohstoffe", resources);
        response.add("Kosten", info);
        return response;
    }

    /**
     * (10.5)
     * when Robber is moved a Object with the new location, the thiev and the one who is being robbed is send out to Client
     * the idRobbedPlayer is OPTIONAL
     * @param id
     * @param location
     * @param idRobbedPlayer
     * @return object with player id, new location for robber and the one who is being robbed
     */
    @Override
    public JsonObject robberMovedToClient(int id, String location, int idRobbedPlayer){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", id);
        info.addProperty("Ort",location);
        if(idRobbedPlayer != -1) {
            info.addProperty("Ziel", idRobbedPlayer);
        }
        response.add("Räuber versetzt", info);
        return response;
    }

    //--------------------------Empty method from Protocol10
    @Override
    public JsonObject robberMovedToClient(int id, JsonObject location, int idRobbedPlayer){ return null; }


    /**
     * (11.2)
     * send out to Server after the "Statusupdate": "Karten wegen Räuber abgeben"
     * @param resources
     * @return Object with the Cards which the Player has to give up because of the robber moving
     */
    @Override
    public JsonObject submitResourcesToServer(JsonObject resources){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.add("Abgeben", resources);
        response.add("Karten abgeben", info);
        return response;
    }

    /**
     * (11.3)
     * Client sends out a message with the new position of the robber and OPTIONAL the id of the player who is being robbed
     * @param location
     * @param idRobbedPlayer
     * @return Object with new robber location and optional idRobbedPlayer
     */
    @Override
    public JsonObject moveRobberToServer(String location, int idRobbedPlayer){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Ort", location);
        if(idRobbedPlayer != -1) {
            info.addProperty("Ziel", idRobbedPlayer);
        }
        response.add("Räuber versetzen", info);
        return response;
    }

    /**
     * (11.5)
     * Client requests a trade trough habors
     * @param offer
     * @param request
     * @return Object with requested trade (offer, request)
     */
    @Override
    public JsonObject offerHarborTradeToServer(JsonObject offer, JsonObject request){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.add("Angebot", offer);
        info.add("Nachfrage", request);
        response.add("Seehandel", info);
        return response;
    }

    /**
     * (12.1)
     * Player sends domestic trade offer to server
     * @param offer
     * @param request
     * @return
     */
    @Override
    public JsonObject offerDomesticTradeToServer(JsonObject offer, JsonObject request){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.add("Angebot", offer);
        info.add("Nachfrage", request);
        response.add("Handel anbieten", info);
        return response;
    }

    /**
     * (12.1)
     *
     * @param id
     * @param tradeId
     * @param offer
     * @param request
     * @return
     */
    @Override
    public JsonObject requestDomesticTradeToClient(int id, int tradeId, JsonObject offer, JsonObject request){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", id);
        info.addProperty("Handel id", tradeId);
        info.add("Angebot", offer);
        info.add("Nachfrage", request);
        response.add("Handelsangebot",info);
        return response;
    }

    /**
     * (12.2) accept trade offer
     * Player wants to accept an offer and sends it out to server
     * more than one Player can accept the offer
     * @param tradeId
     * @return  Object with tradeId
     */
    @Override
    public JsonObject acceptOfferToServer(int tradeId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Handel id", tradeId);
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
    @Override
    public JsonObject acceptOfferToClient(int id, int tradeId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", id);
        info.addProperty("Handel id", tradeId);
        response.add("Handelsangebot angenommen", info);
        return response;
    }

    /**
     * (12.3)
     * now the active Player can choose which trade he wants to make
     * @param tradeId
     * @param otherPlayer
     * @return Object with tradeId and otherPlayer
     */
    @Override
    public JsonObject finishTradeToServer(int tradeId, int otherPlayer){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Handel id", tradeId);
        info.addProperty("Mitspieler", otherPlayer);
        response.add("Handel abschließen",info);
        return response;
    }

    /**
     * (12.3)
     * when the trade happpend, server sends out approval to all players
     * @param player
     * @param otherPlayer
     * @return Object with the two Players who finished the deal
     */
    @Override
    public JsonObject finishTradeToClient(int player, int otherPlayer){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", player);
        info.addProperty("Mitspieler", otherPlayer);
        response.add("Handel ausgeführt",info);
        return response;
    }

    /**
     * (12.4) cancel trade
     * when the player or the one who offered the deal wants to cancel the trade
     * @param tradeId
     * @return Object with tradeId
     */
    @Override
    public JsonObject cancelTradeToServer(int tradeId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Handel id", tradeId);
        response.add("Handel abbrechen",info);
        return response;
    }

    /**
     * (12.4) cancel trade
     *sends out a message that a trade was canceled
     * @param tradeId
     * @return object with player and tradeId
     */
    @Override
    public JsonObject cancelTradeToClient(int id,int tradeId){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler",id);
        info.addProperty("Handel id", tradeId);
        response.add("Handelsangebot abgebrochen",info);
        return response;
    }

    //Empty-Methods from Protocol03--------------------------------------------------------------
    @Override
    public JsonObject diceNumberToClient(int player, int [] diceNumber){
        return null;
    }

    @Override
    public JsonObject playerBoughtDevCard(int id, String developmentCard){ return null; }

    @Override
    public JsonObject longestRoadToClient(int id){ return null; }

    @Override
    public JsonObject biggestKnightForceToClient(int id){ return null; }

    @Override
    public JsonObject lostLongestRoadToClient(){ return null; }

    @Override
    public JsonObject playKnightCardToServer(String location, int destinationId){ return null; }

    @Override
    public JsonObject playKnightCardToClient(int playerId, String location, int destinationId){ return null; }

    @Override
    public JsonObject playBuildStreetCardToServer(String street1, String street2){ return null; }

    @Override
    public JsonObject playBuildStreetCardToClient(int playerId, String street1, String street2){ return null; }

    @Override
    public JsonObject playYearOfPlentyCardToServer(JsonObject resources) { return null; }

    @Override
    public JsonObject playYearOfPlentyCardToClient(int playerId, JsonObject resources){ return null; }

    @Override
    public JsonObject buyDevelopmentCard() {return null;}
}
