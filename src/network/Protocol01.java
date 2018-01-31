package network;

import LOG.Logging;
import com.google.gson.*;
import view.BoardView;

import java.util.logging.Logger;

public class Protocol01 implements Protocol {
    Logger logger= BoardView.getLogger();

    public Protocol01(){
    }

    /**
     * creates start message from server to client (4)
     * @param versionString
     * @param protocolString
     * @return json object with version and protocol number of server
     */
    @Override
    public JsonObject startConnectionToClient(String versionString, String protocolString) {
        JsonObject response = new JsonObject();
        JsonObject version = new JsonObject();
        version.addProperty("Version", versionString);
        version.addProperty("Protokoll", protocolString);
        response.add("Hallo", version);
        return response;
    }

    /**
     * Client creates "Hallo" answer for Server (4)
     * @param versionString
     * @return jsonObject with Version of Client
     */
    @Override
    public JsonObject startConnectionToServer(String versionString) {
        JsonObject response = new JsonObject();
        JsonObject version = new JsonObject();
        version.addProperty("Version", versionString);
        response.add("Hallo", version);
        return response;
    }

    /**
     * assigns unique id to client (4)
     * @param id
     * @return json object with id for the client
     */
    @Override
    public JsonObject assignIDToClient(int id) {
        JsonObject response = new JsonObject();
        JsonObject idObject = new JsonObject();
        idObject.addProperty("id", id);
        response.add("Willkommen", idObject);
        return response;
    }

    /**
     * sends message to client to tell if his action was valid (6.1)
     * if action is valid, errorMessage can be null because its not needed
     * @param valid
     * @return
     */
    @Override
    public JsonObject answerToClientAction(boolean valid, String errorMessage) {
        JsonObject response = new JsonObject();
        if (valid) {
            response.addProperty("Serverantwort", "OK");
        } else {
            response.addProperty("Serverantwort", errorMessage);
        }
        return response;
    }


    /**
     * sends chat message from a client to the server (6.2)
     * @param messageFromUser
     * @return object with message
     */
    @Override
    public JsonObject sendChatMessageToServer(String messageFromUser) {
        JsonObject response = new JsonObject();
        JsonObject message = new JsonObject();
        message.addProperty("Nachricht", messageFromUser);
        response.add("Chatnachricht senden", message);
        return response;
    }

    /**
     * distributes the chat message from a client to the other clients (6.3)
     * @param user
     * @param messageFromUser
     * @return object with sender and message
     */
    @Override
    public JsonObject sendChatMessageToClients(int user, String messageFromUser) {
        JsonObject response = new JsonObject();
        JsonObject message = new JsonObject();
        if (user != -1) {
            message.addProperty("Absender", user);
        }
        message.addProperty("Nachricht", messageFromUser);
        response.add("Chatnachricht", message);
        return response;
    }

    /**
     * when the player chooses a color and name, this method sends the choices to the server (7)
     * @param name
     * @param color
     * @return object with name and color
     */
    @Override
    public JsonObject sendPlayerInfoToServer(String name, String color){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();

        info.addProperty("Name", name);
        info.addProperty("Farbe",color);
        response.add("Spieler", info);
        return response;
    }

    /**
     * signal to the server that the client is ready to play (7)
     * @return object with empty message
     */
    @Override
    public JsonObject playerIsReadyToServer(){
        JsonObject response = new JsonObject();
        response.add("Spiel starten",new JsonObject());
        return response;
    }

    /**
     * if color is taken, the server sends an error to the client (7)
     * @return object with error message
     */
    @Override
    public JsonObject accessDeniedToClient(){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();

        info.addProperty("Meldung", "Farbe bereits vergeben");
        response.add("Fehler", info);
        return response;

    }

    /**
     * sends the board to the clients at the beginning of the game (7)
     * @param boardFromModel
     * @return initialised board
     */
    @Override
    public JsonObject sendBoardToClients(JsonObject boardFromModel) {
        JsonObject response = new JsonObject();
        JsonObject board = new JsonObject();
        board.add("Karte", boardFromModel);
        response.add("Spiel gestartet", board);
        return response;
    }

    /**
     * Server sends status update when the status of the player changes (8.1)
     * @param playerObject
     * @return new playerObject
     */
    @Override
    public JsonObject statusUpdateToClient(JsonObject playerObject){
        logger.info(".");
        JsonObject response = new JsonObject();
        JsonObject player = new JsonObject();
        player.add("Spieler",playerObject);
        response.add("Statusupdate", player);
        return response;
    }

    /**
     * sends the dice number and the current playing client id to all clients (8.2)
     * @param player (id from player)
     * @param diceNumber
     * @return object with player id and players dice number
     */
    @Override
    public JsonObject diceNumberToClient(int player, int diceNumber){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", player);
        info.addProperty("Wurf", diceNumber);
        response.add("Würfelwurf", info);
        return response;
    }

    /**
     * Server creates profit message for Client (8.3)
     * @param player
     * @param resource
     * @return object with player id and gained resources
     */
    @Override
    public JsonObject profitToClient(int player, JsonObject resource){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();
        info.addProperty("Spieler", player);
        info.add("Rohstoffe", resource);
        response.add("Ertrag", info);
        return response;
    }

    /**
     * when a client has built a new building the server sends that building to the other clients (8.4)
     * @param buildingFromClient
     * @return a message, which contains the new building object
     */
    @Override
    public JsonObject sendNewBuildingToClients(JsonObject buildingFromClient) {
        JsonObject response = new JsonObject();
        JsonObject building = new JsonObject();
        building.add("Gebäude", buildingFromClient);
        response.add("Bauvorgang", building);
        return response;
    }


    /**
     * signals to the server that the client wants to dice (9.1)
     * @return empty object
     */
    @Override
    public JsonObject throwDice(){
        JsonObject response = new JsonObject();
        response.add("Würfeln", new JsonObject());
        return response;
    }

    /**
     * (9.2)
     * the message is send out when a village or city is to be build
     * @param type
     * @param location
     * @return object with type (street, settlement, city) and it's location
     */
    @Override
    public JsonObject build(String type, String location){
        JsonObject response = new JsonObject();
        JsonObject info = new JsonObject();

        info.addProperty("Typ", type);
        info.addProperty("Ort", location);
        response.add("Bauen", info);
        return response;
    }

    /**
     * when the client wants to finish its move it sends this message to the client (9.3)
     * @return empty object
     */
    @Override
    public JsonObject finishMove(){
        JsonObject response = new JsonObject();
        response.add("Zug beenden", new JsonObject());
        return response;

    }

    //Empty methods from Protocol02----------------------------------------------
    @Override
    public JsonObject sendPlayerWinsToClient(String message, int id){
        return null;
    }

    @Override
    public JsonObject sendExpenseToClient(int id, JsonObject resources){
        return null;
    }

    @Override
    public JsonObject robberMovedToClient(int id, String location, int idRobbedPlayer){
        return null;
    }
    //--------------------------Empty methods from Protocol10
    @Override
    public JsonObject robberMovedToClient(int id, JsonObject location, int idRobbedPlayer){ return null; }

    @Override
    public JsonObject submitResourcesToServer(JsonObject resources){
        return null;
    }

    @Override
    public JsonObject moveRobberToServer(String location, int idRobbedPlayer){
        return null;
    }

    @Override
    public JsonObject offerHarborTradeToServer(JsonObject offer, JsonObject request){
        return null;
    }

    @Override
    public JsonObject offerDomesticTradeToServer(JsonObject offer, JsonObject request){
        return null;
    }

    @Override
    public JsonObject requestDomesticTradeToClient(int id, int tradeId, JsonObject offer, JsonObject request){
        return null;
    }

    @Override
    public JsonObject acceptOfferToServer(int tradeId){
        return null;
    }

    @Override
    public JsonObject acceptOfferToClient(int id, int tradeId){
        return null;
    }

    @Override
    public JsonObject finishTradeToServer(int tradeId, int otherPlayer){
        return null;
    }

    @Override
    public JsonObject finishTradeToClient(int player, int otherPlayer){
        return null;
    }

    @Override
    public JsonObject cancelTradeToServer(int tradeId){
        return null;
    }

    @Override
    public JsonObject cancelTradeToClient(int id,int tradeId){
        return null;
    }


    //Empty-Methods from Protocol03--------------------------------------------------------------
    @Override
    public JsonObject diceNumberToClient(int player, int [] diceNumber){ return null; }

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
    public JsonObject buyDevelopmentCard(){return null;}
}