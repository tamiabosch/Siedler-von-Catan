package InfraroterServer;

import LOG.Logging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.paint.Color;
import model.*;
import network.*;
import sun.nio.ch.Net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Tamia & Kevin
 */
class ServerController implements Runnable {

    private Socket socket;
    private String newLine = "\n";
    private int playerID;
    private Protocol10 protocol;
    private OutputStreamWriter writer;
    private boolean socketClosed;
    private JsonParser parser = new JsonParser();
    private Controller controller;
    private Player currentPlayer;
    private String colorOfCurrentPlayer;
    private boolean startingPhase = true;
    private int startRound = 0;
    boolean isStartingPlayer = false;
    private boolean devCardBoughtInThisRound = false;
    private HashMap saveTradeResource = new HashMap();
    private String lastState = "";
    private boolean isTradingPlayer = false;
    private Logger logger = Logging.getLoggerServer();
    private boolean hasLongestTradeRoad = false;
    private int settlementsBuilt = 0;
    private int citysBuilt = 0;
    private int streetsBuilt = 0;
    private String lastCardBought = null;

    // CHEATS
    private boolean noCardDrop = false;

    ServerController(Socket socket, int id) {
        this.socket = socket;
        this.playerID = id;
        this.protocol = new Protocol10();
    }

    /**
     * Initializes the writer and reader, sends the hello message and starts the loop that is looking for messages form the client
     */
    @Override
    public void run() {
        try {
            BufferedReader reader;
            writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            sendHello();
            while (!socketClosed) {
                String inputFromClient = reader.readLine();
                System.out.println(" IN: " + inputFromClient);
                logger.info("IN: " + inputFromClient);
                if (inputFromClient != null) {
                    handleInputFromClient(parser.parse(inputFromClient).getAsJsonObject()); // converts string to jsonobject
                } else {
                    closeSocket();
                    System.out.println("closed socket du to inputFromClient = null");
                    socketClosed = true;
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Handles the input the Server gets from the Client
     * @param inputFromClient inputFromClient
     * @throws Exception if something goes wrong
     */
    private void handleInputFromClient(JsonObject inputFromClient) throws Exception {
        List<String> keys = inputFromClient.entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        logger.info(keys.get(0));

        switch (keys.get(0)) {
            case "Hallo":
                handleHello();
                break;
            case "Spieler":
                handleNewPlayer(inputFromClient);
                break;
            case "Spiel starten":
                handleReadyPlayer();
                break;
            case "Chatnachricht senden":
                handleChatMessage(inputFromClient);
                break;
            case "Bauen":
                handleBuildRequest(inputFromClient, false);
                break;
            case "Zug beenden":
                handleTurnFinished();
                break;
            case "Würfeln":
                sendRollDice();
                break;
            case "Entwicklungskarte kaufen":
                handleDevCardBuy();
                break;
            case "Räuber versetzen":
                handleMoveRobber(inputFromClient);
                break;
            case "Ritter ausspielen":
                handleKnightCardPlayed(inputFromClient);
                break;
            case "Monopol":
                handleMonopolyCardPlayed(inputFromClient);
                break;
            case "Erfindung":
                handleYearOfPlentyCardPlayed(inputFromClient);
                break;
            case "Straßenbaukarte ausspielen":
                handleRoadBuildingCardPlayed(inputFromClient);
                break;
            case "Handel anbieten":
                handleTradeOffer(inputFromClient);
                break;
            case "Handel annehmen":
                handleAcceptTradeOffer(inputFromClient);
                break;
            case "Handel abschließen":
                handleTradeFinished(inputFromClient);
                break;
            case "Handel abbrechen":
                handleTradeCanceled(inputFromClient);
                break;
            case "Seehandel":
                handleHarborTradeOffer(inputFromClient);
                break;
            case "Karten abgeben":
                handleCardsDrop(inputFromClient);
                break;
            case "Anfängermodus":
                controller.createBeginnerBoard();
                break;
        }
    }

    /**
     * Checks if the protocol version matches on server and client
     */
    private void handleHello() {
        logger.info(".");
        try {
            currentPlayer = new Player(null, playerID, null);
            controller.addPlayer(currentPlayer);
            controller.changeStateOfPlayer(playerID, "Spiel starten");
            sendWelcome();
            sendStatusUpdate("Spiel starten", null, null, new JsonObject());
            controller.getStatusUpdatesWhenConnect(playerID);
        } catch (Exception e) {
            System.out.println("error in handleHello");
            e.printStackTrace();
        }
    }

    /**
     * If a client sends it's status information to the server the server will handle this information and create a new player
     * @param inputFromClient inputFromClient
     */
    private void handleNewPlayer(JsonObject inputFromClient) {
        logger.info(inputFromClient.toString());
        String name = inputFromClient.get("Spieler").getAsJsonObject().get("Name").getAsString();
        String color = inputFromClient.get("Spieler").getAsJsonObject().get("Farbe").getAsString();
        colorOfCurrentPlayer = color;

        Color colorObject = Color.BLACK;
        switch (color) {
            case "Rot":
                colorObject = Color.RED;
                break;
            case "Orange":
                colorObject = Color.ORANGE;
                break;
            case "Weiß":
                colorObject = Color.WHITE;
                break;
            case "Blau":
                colorObject = Color.AQUA;
                break;
        }
        currentPlayer.setName(name);
        currentPlayer.setColor(colorObject);
        try {
            logger.info("currentplayer name: " + currentPlayer.toExtendedString() );
        }catch (Exception e){
            logger.log(Level.SEVERE, "currentPlayer to string doesn't work");
        }
    }

    /**
     * If the client sends "Spiel starten" the server will set it's state to "Wartet auf Spielbeginn" and sends a statusupdate of this player
     */
    private void handleReadyPlayer() {
        logger.info(".");
        if (controller.checkIfColorStillAvailable(colorOfCurrentPlayer, getPlayerID())) {
            sendStatusUpdate("Wartet auf Spielbeginn");
            controller.changeStateOfPlayer(playerID, "Wartet auf Spielbeginn");
            sendOk();
        } else {
            logger.info("color not available");
            sendMistake();
        }
    }

    /**
     * Handles that the player sends a chat message
     * @param inputFromClient inputFromClient
     */
    private void handleChatMessage(JsonObject inputFromClient) {
        String message = inputFromClient.get("Chatnachricht senden").getAsJsonObject().get("Nachricht").getAsString();
        if (message.length() > 1 && message.substring(0,2).equals("//")) {
            handleCheats(message.substring(2));
            controller.sendMessageToOneClient(protocol.sendChatMessageToClients(playerID, message), playerID);
        } else {
            controller.sendMessageToAllClients(protocol.sendChatMessageToClients(playerID, message));
        }
    }

    /**
     * Handles a build request from the client. Can either be a settlement, city or road
     * @param inputFromClient inputFromClient
     * @param streetCard does the build request come from a street card?
     */
    private void handleBuildRequest(JsonObject inputFromClient, boolean streetCard) {
        System.out.println("AKTUELLER STATUS von " + currentPlayer.getName() + ": " + currentPlayer.getState());
        if (currentPlayer.getState().equals("Handeln oder Bauen") || currentPlayer.getState().equals("Dorf bauen") || currentPlayer.getState().equals("Straße bauen")) {
            String type = inputFromClient.get("Bauen").getAsJsonObject().get("Typ").getAsString();
            //String location = inputFromClient.get("Bauen").getAsJsonObject().get("Ort").getAsString();
            JsonArray location = inputFromClient.get("Bauen").getAsJsonObject().get("Ort").getAsJsonArray();
            switch (type) {
                case "Dorf":
                    if (settlementsBuilt > 5) {
                        sendNotOk("Du kannst nur maximal 5 Siedlungen bauen.");
                        sendChatMessageFromServer("Du kannst nur maximal 5 Siedlungen bauen.", true);
                    } else if (currentPlayer.getQuantityOfAResource("Holz") > 0 && currentPlayer.getQuantityOfAResource("Lehm") > 0 && currentPlayer.getQuantityOfAResource("Getreide") > 0 && currentPlayer.getQuantityOfAResource("Wolle") > 0 || startingPhase) {
                        settlementsBuilt++;
                        sendConstructionProcess(type, location);
                        Utility.Pair settlementCoordinates = NetworkTranslator.translateIntersectionCoordinate(location);
                        startRound++;
                        controller.getBoard().addSettlement(settlementCoordinates, currentPlayer);
                        if (startingPhase) {
                            sendStatusUpdate("Straße bauen");
                            //after startingPhase
                        } else {
                            JsonObject costsVillage = JsonObjectCreator.createResourcesObject(1, 1, 1, 1, 0);
                            sendCost(costsVillage);
                        }
                        if (startRound == 2) {
                            sendProfit(calculateProfit(settlementCoordinates));
                        }
                        currentPlayer.increaseVictoryPoints();
                        if (currentPlayer.hasWon()) {
                            sendGameFinished();
                        }
                        controller.calculateLongestTradeRoadPlayer();
                        controller.calculateLongestTradeRoadPlayer();
                        if (controller.isTradeRoadPlayerWasSet() && controller.getLongestTradeRoadPlayer() == null) {
                            sendLongestRoad(-1);
                        }
                    } else {
                        sendNotOk("Du hast nicht genügend Rohstoffe, um eine Siedlung zu bauen.");
                        sendChatMessageFromServer("Du hast nicht genügend Rohstoffe, um eine Siedlung zu bauen.", true);
                    }
                    break;
                case "Stadt":
                    if (citysBuilt > 4) {
                        sendNotOk("Du kannst maximal 4 Städte bauen.");
                        sendChatMessageFromServer("Du kannst maximal 4 Städte bauen.", true);
                    } else if (currentPlayer.getQuantityOfAResource("Getreide") > 1 && currentPlayer.getQuantityOfAResource("Erz") > 2) {
                        citysBuilt++;
                        settlementsBuilt--;
                        sendConstructionProcess(type, location);
                        Utility.Pair cityCoordinates = NetworkTranslator.translateIntersectionCoordinate(location);
                        controller.getBoard().addCity(cityCoordinates, currentPlayer);
                        currentPlayer.increaseVictoryPoints();
                        if (!startingPhase) {
                            JsonObject costsCity = JsonObjectCreator.createResourcesObject(0, 0, 0, 2, 3);
                            sendCost(costsCity);
                        }
                        if (currentPlayer.hasWon()) {
                            sendGameFinished();
                        }
                    } else {
                        sendNotOk("Du hast nicht genügend Rohstoffe, um eine Stadt zu bauen.");
                        sendChatMessageFromServer("Du hast nicht genügend Rohstoffe, um eine Stadt zu bauen.", true);
                    }
                    break;
                case "Straße":
                    if (streetsBuilt > 15) {
                        sendNotOk("Du kannst maximal 15 Straßen bauen.");
                        sendChatMessageFromServer("Du kannst maximal 15 Straßen bauen.", true);
                    } else if (currentPlayer.getQuantityOfAResource("Lehm") > 0 && currentPlayer.getQuantityOfAResource("Holz") > 0 || startingPhase || streetCard) {
                        streetsBuilt++;
                        sendConstructionProcess(type, location);
                        Utility.Pair[] street = NetworkTranslator.translateCoordinateToIntersections(location);
                        controller.getBoard().addStreet(street[0], street[1], currentPlayer);
                        if (!startingPhase) {
                            JsonObject costsStreet = JsonObjectCreator.createResourcesObject(1, 1, 0, 0, 0);
                            if (!streetCard) {
                                sendCost(costsStreet);
                            }
                            //check if player has longest road
                            controller.calculateLongestTradeRoadPlayer();
                            if (currentPlayer == controller.getLongestTradeRoadPlayer() && !hasLongestTradeRoad) {
                                hasLongestTradeRoad = true;
                                currentPlayer.setHasLongestRoad(true);
                                sendLongestRoad(playerID);
                            } else if (currentPlayer != controller.getLongestTradeRoadPlayer()) {
                                hasLongestTradeRoad = false;
                            }
                        }
                        if (startRound == 1) {
                            controller.increasePlayerReadyWithStart1();
                        } else if (startRound == 2) {
                            controller.increasePlayerReadyWithStart2();
                        }
                        if (startingPhase) {
                            controller.changeActivePlayerInStartingPhase(playerID);
                        }
                    } else {
                        sendNotOk("Du hast nicht genügend Rohstoffe, um eine Straße zu bauen.");
                        sendChatMessageFromServer("Du hast nicht genügend Rohstoffe, um eine Straße zu bauen.", true);
                    }
                    break;
                }
            } else {
            sendNotOk("Du bist nicht an der Reihe.");
            sendChatMessageFromServer("Du bist nicht an der Reihe.", true);
        }
    }

    /**
     * Handles the purchase of a development card
     * checks if the client has enough resources and then determines which dev card he gets
     */
    private void handleDevCardBuy() {
        Resource[] resources = currentPlayer.getResources();
        if (!currentPlayer.getState().equals("Handeln oder Bauen")) {
            sendNotOk("Du bist nicht am Zug.");
            sendChatMessageFromServer("Du bist nicht am Zug.", true);
        } else if (devCardBoughtInThisRound) {
            sendNotOk("Du hast in dieser Runde schon eine Entwicklungskarte gekauft");
            sendChatMessageFromServer("Du hast in dieser Runde schon eine Entwicklungskarte gekauft", true);
        } else if (controller.getDevCards().size() == 0) {
            sendNotOk("Keine Entwicklungskarten mehr verfügbar");
            sendChatMessageFromServer("Keine Entwicklungskarten mehr verfügbar", true);
        } else if (resources[2].getValue() >= 1 && resources[3].getValue() >= 1 && resources[4].getValue() >= 1) {
            sendCost(JsonObjectCreator.createResourcesObject(0,0,1,1,1));
            sendDevelopmentCard();
            sendOk();
            devCardBoughtInThisRound = true;
        } else {
            sendNotOk("Nicht genügend Resourcen vorhanden, Karte kann nicht gekauft werden");
            sendChatMessageFromServer("Nicht genügend Resourcen vorhanden, Karte kann nicht gekauft werden", true);
        }
    }

    /**
     * Handles the message that the client wants to move the robber to a specific place
     * @param inputFromClient inputFromClient
     */
    private void handleMoveRobber(JsonObject inputFromClient){
    JsonObject location =  inputFromClient.get("Räuber versetzen").getAsJsonObject().get("Ort").getAsJsonObject();
        if (inputFromClient.get("Räuber versetzen").getAsJsonObject().has("Ziel")){
            int targetId = inputFromClient.get("Räuber versetzen").getAsJsonObject().get("Ziel").getAsInt();
            Resource stolenResource = controller.getPlayerById(targetId).stealRandomResource();
            controller.getBoard().changeRobber(NetworkTranslator.translateLandpieceCoordinateFromProtocol(location));

            if (stolenResource != null){
                sendRobberMoved(location, targetId);

                String type = stolenResource.getType();
                JsonObject resources = getOnlyOneResourceObject(type);
                sendCostAndProfitWhenStealing(type, targetId, resources);

                String typeGerman = NetworkTranslator.translateResourceName(type);
                sendChatMessageFromServer(currentPlayer.getName()+" hat dir 1 " + typeGerman + " gestohlen",targetId);
                sendChatMessageFromServer("Du hast 1 " + typeGerman + " von " + controller.getPlayerById(targetId).getName() + " gestohlen", true);

            } else {
                sendRobberMoved(location, -1);
            }
        } else {
            //sofern kein anderer Spieler beklaut wird
            sendRobberMoved(location, -1);
        }
        tryToChangeToTradeOrBuild();
    }

    /**
     * Handles that the client played a knight card. If the the player that gets robbed has no resources, there will be no cost messages
     * @param inputFromClient inputFromClient
     */
    private void handleKnightCardPlayed(JsonObject inputFromClient) {
        if (!currentPlayer.getState().equals("Handeln oder Bauen")) {
            sendNotOk("Du bist nicht am Zug.");
            sendChatMessageFromServer("Du bist nicht am Zug.", true);
        } else if (lastCardBought != null && lastCardBought.equals("Ritter") && currentPlayer.getQuantityOfDevCard("Ritter") == 1) {
            sendNotOk("Du kannst eine Entwicklungskarte, die du in der aktuellen Runde gekauft hast, nicht in derselben Runde spielen.");
            sendChatMessageFromServer("Du kannst eine Entwicklungskarte, die du in der aktuellen Runde gekauft hast, nicht in derselben Runde spielen.", true);
        } else if (currentPlayer.getQuantityOfDevCard("Ritter") > 0){
            JsonObject location = inputFromClient.get("Ritter ausspielen").getAsJsonObject().get("Ort").getAsJsonObject();
            if(inputFromClient.get("Ritter ausspielen").getAsJsonObject().has("Ziel")) {
                int targetId = inputFromClient.get("Ritter ausspielen").getAsJsonObject().get("Ziel").getAsInt();
                sendKnightCardPlayed(location, targetId);
                Resource stolenResource = controller.getPlayerById(targetId).stealRandomResource();
                String type = stolenResource.getType();
                JsonObject resources = getOnlyOneResourceObject(type);
                sendCostAndProfitWhenStealing(type, targetId, resources);

                String typeGerman = NetworkTranslator.translateResourceName(type);
                sendChatMessageFromServer(currentPlayer.getName()+" hat dir 1 " + typeGerman + " gestohlen",targetId);
                sendChatMessageFromServer("Du hast 1 " + typeGerman + " von " + controller.getPlayerById(targetId).getName() + " gestohlen", true);

            } else {
                //only when theres no target id
                sendKnightCardPlayed(location, -1);
            }
            sendOk();
        } else {
            sendNotOk("Du hast keine Ritterkarte");
            sendChatMessageFromServer("Du hast keine Ritterkarte", true);
        }
    }

    /**
     * Handles the message from the client that he wants to play the monopoly card
     * @param inputFromClient inputFromClient
     */
    private void handleMonopolyCardPlayed(JsonObject inputFromClient) {
        if (!currentPlayer.getState().equals("Handeln oder Bauen")) {
            sendNotOk("Du bist nicht am Zug.");
            sendChatMessageFromServer("Du bist nicht am Zug.", true);
        } else if (lastCardBought != null && lastCardBought.equals("Monopol") && currentPlayer.getQuantityOfDevCard("Monopol") == 1) {
            sendNotOk("Du kannst eine Entwicklungskarte, die du in der aktuellen Runde gekauft hast, nicht in derselben Runde spielen.");
            sendChatMessageFromServer("Du kannst eine Entwicklungskarte, die du in der aktuellen Runde gekauft hast, nicht in derselben Runde spielen.", true);
        } else if (currentPlayer.getQuantityOfDevCard("Monopol") > 0) {
            String resource = inputFromClient.get("Monopol").getAsJsonObject().get("Rohstoff").getAsString();
            resource = resource.substring(0,1).toUpperCase() + resource.substring(1).toLowerCase();
            ArrayList<Player> playersWithDemandedResource = controller.getPlayersWithResource(resource, currentPlayer.getId());
            logger.info("Players with the MonopolyCard Resource " + resource + ": " + playersWithDemandedResource.size());
            int totalAmount = 0;
            String type = "";
            sendMonopolyCardPlayed(resource);
            for (Player player : playersWithDemandedResource) {
                int amount = player.getQuantityOfAResource(resource);
                totalAmount += amount;
                JsonObject costs = new JsonObject();
                switch (resource) {
                    case "Holz":
                        costs = JsonObjectCreator.createResourcesObject(amount, 0, 0, 0, 0);
                        type = "lumber";
                        break;
                    case "Lehm":
                        costs = JsonObjectCreator.createResourcesObject(0, amount, 0, 0, 0);
                        type = "brick";
                        break;
                    case "Wolle":
                        costs = JsonObjectCreator.createResourcesObject(0, 0, amount, 0, 0);
                        type = "wool";
                        break;
                    case "Getreide":
                        costs = JsonObjectCreator.createResourcesObject(0, 0, 0, amount, 0);
                        type = "grain";
                        break;
                    case "Erz":
                        costs = JsonObjectCreator.createResourcesObject(0, 0, 0, 0, amount);
                        type = "ore";
                        break;
                }
                logger.info(player + " hat " + amount + " " + type + " genommen bekommen.");

                player.changeResourceQuantity(type, -amount);
                currentPlayer.changeResourceQuantity(type, amount);

                controller.sendMessageToAllClients(protocol.sendExpenseToClient(player.getId(), costs));
                controller.sendMessageToAllClients(protocol.profitToClient(playerID, costs));
                sendOk();
            }
            logger.info(currentPlayer + " hat " + totalAmount + type + " bekommen.");
        } else {
            sendNotOk("Du hast keine Monopolkarte");
            sendChatMessageFromServer("Du hast keine Monopolkarte", true);
        }
    }

    /**
     * Handles the message from the client that the player wants to play the year of plenty card
     * @param inputFromClient inputFromClient
     */
    private void handleYearOfPlentyCardPlayed(JsonObject inputFromClient) {
        if (!currentPlayer.getState().equals("Handeln oder Bauen")) {
            sendNotOk("Du bist nicht am Zug");
            sendChatMessageFromServer("Du bist nicht am Zug", true);
        } else if (lastCardBought != null && lastCardBought.equals("Erfindung") && currentPlayer.getQuantityOfDevCard("Erfindung") == 1) {
            sendNotOk("Du kannst eine Entwicklungskarte, die du in der aktuellen Runde gekauft hast, nicht in derselben Runde spielen.");
            sendChatMessageFromServer("Du kannst eine Entwicklungskarte, die du in der aktuellen Runde gekauft hast, nicht in derselben Runde spielen.", true);
        } else if (currentPlayer.getQuantityOfDevCard("Erfindung") > 0) {
            JsonObject resources = inputFromClient.get("Erfindung").getAsJsonObject().get("Rohstoffe").getAsJsonObject();
            sendYearOfPlentyCardPlayed(resources);
        } else {
            sendNotOk("Du hast keine Erfindungskarte");
            sendChatMessageFromServer("Du hast keine Erfindungskarte", true);
        }
    }

    /**
     * Handles the message from the client that the player wants to play the road build card
     * @param inputFromClient inputFromClient
     */
    private void handleRoadBuildingCardPlayed(JsonObject inputFromClient) {
        if (!currentPlayer.getState().equals("Handeln oder Bauen")) {
            sendNotOk("Du bist nicht am Zug.");
        } else if (lastCardBought != null && lastCardBought.equals("Straßenbau") && currentPlayer.getQuantityOfDevCard("Straßenbau") == 1) {
            sendNotOk("Du kannst eine Entwicklungskarte, die du in der aktuellen Runde gekauft hast, nicht in derselben Runde spielen.");
        } else if (currentPlayer.getQuantityOfDevCard("Straßenbau") > 0) {
            JsonArray road1 = inputFromClient.get("Straßenbaukarte ausspielen").getAsJsonObject().get("Straße 1").getAsJsonArray();
            handleBuildRequest(protocol.build("Straße", road1), true);
            if (inputFromClient.get("Straßenbaukarte ausspielen").getAsJsonObject().has("Straße 2")) {
                JsonArray road2 = inputFromClient.get("Straßenbaukarte ausspielen").getAsJsonObject().get("Straße 2").getAsJsonArray();
                handleBuildRequest(protocol.build("Straße", road2), true);
                sendRoadBuildingCardPlayed(road1, road2);
            } else {
                sendRoadBuildingCardPlayed(road1, null);
            }
        } else {
            sendNotOk("Du hast keine Straßenbaukarte");
            sendChatMessageFromServer("Du hast keine Straßenbaukarte", true);
        }
    }

    /**
     * Handles the situation that the client wants to end his move
     */
    private void handleTurnFinished() {
        if (!currentPlayer.getState().equals("Handeln oder Bauen")) {
            sendNotOk("Du bist nicht am Zug.");
            sendChatMessageFromServer("Du bist nicht am Zug.", true);
        } else {
            controller.changeActivePlayer();
            lastCardBought = null;
        }
    }

    /**
     * Handles Trade Message from Client, and gives the Trade an Id
     * @param inputFromClient inputFromClient
     */
    private void handleTradeOffer(JsonObject inputFromClient){
        if (currentPlayer.getState().equals("Handeln oder Bauen")) {
            JsonObject offerFromClient = inputFromClient.get("Handel anbieten").getAsJsonObject().get("Angebot").getAsJsonObject();
            JsonObject demandFromClient = inputFromClient.get("Handel anbieten").getAsJsonObject().get("Nachfrage").getAsJsonObject();
            int tradeId = controller.generateTradeId();
            //Zwischenspeicher: Angebot, Nachfrage vom Handel
            saveTradeResource.put(tradeId, inputFromClient);
            //Zwischenspeicher isTradingPlayer
            isTradingPlayer = true;
            sendTradeOffer(tradeId, offerFromClient, demandFromClient);
            sendOk();
        } else {
            sendNotOk("Du bist nicht am Zug.");
            sendChatMessageFromServer("Du bist nicht am Zug.", true);
        }
    }

    /**
     * Handles the message from the client that he wants to accept a offer
     * @param inputFromClient inputFromClient
     */
    private void handleAcceptTradeOffer(JsonObject inputFromClient){
        int tradeId = inputFromClient.get("Handel annehmen").getAsJsonObject().get("Handel id").getAsInt();
        boolean accept = inputFromClient.get("Handel annehmen").getAsJsonObject().get("Annehmen").getAsBoolean();

        sendAcceptTradeOffer(tradeId, accept);
    }

    /**
     * Handles trade finished and spreads it to all other clients
     * sends out the cost ad profit messages from both players
     * @param inputFromClient inputFromClient
     */
    private void handleTradeFinished(JsonObject inputFromClient){
        int tradeId = inputFromClient.get("Handel abschließen").getAsJsonObject().get("Handel id").getAsInt();
        int otherPlayer = inputFromClient.get("Handel abschließen").getAsJsonObject().get("Mitspieler").getAsInt();
        //get resources from old message
        JsonObject oldTradeMessage = (JsonObject) saveTradeResource.get(tradeId);
        JsonObject offerFromClient = oldTradeMessage.get("Handel anbieten").getAsJsonObject().get("Angebot").getAsJsonObject();
        JsonObject demandFromClient = oldTradeMessage.get("Handel anbieten").getAsJsonObject().get("Nachfrage").getAsJsonObject();
        sendTradeFinished(otherPlayer);

        //reset
        isTradingPlayer = false;

        //Message for currentPlayer also changes quantity
        sendCost(offerFromClient);
        sendProfit(demandFromClient);

        //Cost and profit from other player to all players
        sendCostAndProfitFromTrade(offerFromClient, demandFromClient, otherPlayer);
        sendChatMessageFromServer(currentPlayer.getName() + " hat mit " + controller.getPlayerById(otherPlayer).getName()+" gehandelt.", false);
    }

    /**
     * handles if a trade was canceled
     * @param inputFromClient inputFromClient
     */
    private void handleTradeCanceled(JsonObject inputFromClient){
        int tradeId = inputFromClient.get("Handel abbrechen").getAsJsonObject().get("Handel id").getAsInt();
        if (isTradingPlayer){
            sendChatMessageFromServer(currentPlayer.getName() + " hat den Handel abgebrochen.", false);
        }
        isTradingPlayer = false;
        sendTradeCanceled(tradeId);
    }

    /**
     * handles the incoming harbor trade requests
     * @param inputFromClient inputFromClient
     */
    private void handleHarborTradeOffer(JsonObject inputFromClient){
        if (!currentPlayer.getState().equals("Handeln oder Bauen")) {
            sendNotOk("Du bist nicht am Zug.");
            sendChatMessageFromServer("Du bist nicht am Zug.", true);
        } else {
            JsonObject offer = inputFromClient.get("Seehandel").getAsJsonObject().get("Angebot").getAsJsonObject();
            JsonObject request = inputFromClient.get("Seehandel").getAsJsonObject().get("Nachfrage").getAsJsonObject();
            Resource[] resources = NetworkTranslator.translateResourceObject(offer);
            if (resources[0].getValue() == 2) {
                if (currentPlayer.checkIfPlayerHasHarbor(ResourceType.LUMBER)) {
                    sendHarborTradeCostsAndProfit(offer, request);
                } else {
                    sendNotOk("Du hast nicht an einem Holzhafen gebaut. Dieser Handel ist nicht möglich.");
                    sendChatMessageFromServer("Du hast nicht an einem Holzhafen gebaut. Dieser Handel ist nicht möglich.", true);
                }
            } else if (resources[1].getValue() == 2) {
                if (currentPlayer.checkIfPlayerHasHarbor(ResourceType.BRICK)) {
                    sendHarborTradeCostsAndProfit(offer, request);
                } else {
                    sendNotOk("Du hast nicht an einem Lehmhafen gebaut. Dieser Handel ist nicht möglich.");
                    sendChatMessageFromServer("Du hast nicht an einem Lehmhafen gebaut. Dieser Handel ist nicht möglich.", true);
                }
            } else if (resources[2].getValue() == 2) {
                if (currentPlayer.checkIfPlayerHasHarbor(ResourceType.WOOL)) {
                    sendHarborTradeCostsAndProfit(offer, request);
                } else {
                    sendNotOk("Du hast nicht an einem Wollehafen gebaut. Dieser Handel ist nicht möglich.");
                    sendChatMessageFromServer("Du hast nicht an einem Wollehafen gebaut. Dieser Handel ist nicht möglich.", true);
                }
            } else if (resources[3].getValue() == 2) {
                if (currentPlayer.checkIfPlayerHasHarbor(ResourceType.GRAIN)) {
                    sendHarborTradeCostsAndProfit(offer, request);
                } else {
                    sendNotOk("Du hast nicht an einem Getreidehafen gebaut. Dieser Handel ist nicht möglich.");
                    sendChatMessageFromServer("Du hast nicht an einem Getreidehafen gebaut. Dieser Handel ist nicht möglich.", true);
                }
            } else if (resources[4].getValue() == 2) {
                if (currentPlayer.checkIfPlayerHasHarbor(ResourceType.ORE)) {
                    sendHarborTradeCostsAndProfit(offer, request);
                } else {
                    sendNotOk("Du hast nicht an einem Erzhafen gebaut. Dieser Handel ist nicht möglich.");
                    sendChatMessageFromServer("Du hast nicht an einem Erzhafen gebaut. Dieser Handel ist nicht möglich.", true);
                }
            } else if (resources[0].getValue() == 3 || resources[1].getValue() == 3 || resources[2].getValue() == 3 || resources[3].getValue() == 3 || resources[4].getValue() == 3) {
                if (currentPlayer.checkIfPlayerHasHarbor(null)) {
                    sendHarborTradeCostsAndProfit(offer, request);
                } else {
                    sendNotOk("Du hast nicht an einem Hafen für 3:1 gebaut. Dieser Handel ist nicht möglich.");
                    sendChatMessageFromServer("Du hast nicht an einem Hafen für 3:1 gebaut. Dieser Handel ist nicht möglich.", true);
                }
            } else if (resources[0].getValue() == 4 || resources[1].getValue() == 4 || resources[2].getValue() == 4 || resources[3].getValue() == 4 || resources[4].getValue() == 4) {
                    sendHarborTradeCostsAndProfit(offer, request);
            } else {
                sendNotOk("Du hast nicht an einem Hafen gebaut. Dieser Handel ist nicht möglich");
                sendChatMessageFromServer("Du hast nicht an einem Hafen gebaut. Dieser Handel ist nicht möglich.", true);
            }

        }
    }

    /**
     * If the player had to drop cards because a 7 was diced, the server gets a message from the client with the cards he wants to drop
     * @param inputFromClient inputFromClient
     */
    private void handleCardsDrop(JsonObject inputFromClient) {
        JsonObject resources = inputFromClient.get("Karten abgeben").getAsJsonObject().get("Abgeben").getAsJsonObject();
        JsonObject costMessage = protocol.sendExpenseToClient(playerID, resources);
        controller.sendMessageToOneClient(costMessage, playerID);
        int lumber = 0;
        int brick = 0;
        int wool = 0;
        int grain = 0;
        int ore = 0;
        if (resources.has("Holz")) {
            lumber = resources.get("Holz").getAsInt();
        }
        if (resources.has("Lehm")) {
            brick = resources.get("Lehm").getAsInt();
        }
        if (resources.has("Wolle")) {
            wool = resources.get("Wolle").getAsInt();
        }
        if (resources.has("Getreide")) {
            grain = resources.get("Getreide").getAsInt();
        }
        if (resources.has("Erz")) {
            ore = resources.get("Erz").getAsInt();
        }
        int total = lumber + brick + wool + grain + ore;
        JsonObject anonymousCosts = JsonObjectCreator.createAnonymousResourcesObject(total);
        JsonObject anonymousCostMessage = protocol.sendExpenseToClient(playerID, anonymousCosts);
        controller.sendMessageToOtherClients(anonymousCostMessage, playerID);
        changeResourcesOfPlayer(resources, playerID, false);
        if (!lastState.equals("Räuber versetzen")) {
            sendStatusUpdate(lastState);
        }
        controller.readyWithCardsDrop();
    }

    /**
     * Sends a message to the client
     * @param message message that will be send to the client
     */
     void sendMessage(JsonObject message) {
        try {
            System.out.println("OUT: " + message);
            logger.info("OUT: " + message);
            writer.write(message + newLine);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * sends Hello message to the Client
     */
    private void sendHello() {
        controller.sendMessageToOneClient(protocol.startConnectionToClient("Nicer Server (Infrarote Hacks)", "1.0"), playerID);
    }

    /**
     * Sends a welcome message to the client to tell him his ID
     */
    private void sendWelcome() {
        controller.sendMessageToOneClient(protocol.assignIDToClient(playerID), playerID);
    }

    /**
     * Sends a state update of a player to the clients when its state changes
     * @param state state of the player
     * @param name name of the player
     * @param color color of the player
     * @param resources resources that the player holds
     */
    private void sendStatusUpdate(String state, String name, String color, JsonObject resources) {
        JsonObject player = JsonObjectCreator.createPlayerObject(playerID, name, color, state, 0, resources);
        controller.sendMessageToAllClients(protocol.statusUpdateToClient(player));
    }

    /**
     * sends out the fitting status update
     * @param state new state of the player
     */
    void sendStatusUpdate(String state) {
        switch (state) {
            case "Handeln oder Bauen":
                devCardBoughtInThisRound = false;
                lastState = "Handeln oder Bauen";
                break;
            case "Warten":
                lastState = "Warten";
                break;
            case "Räuber versetzen":
                lastState = "Räuber versetzen";
                break;
        }
        JsonObject devCards = NetworkTranslator.translateDevelopmentCards(currentPlayer.getDevCards());
        JsonObject devCardsForOthers = new JsonObject();
        devCardsForOthers.addProperty("Unbekannt", currentPlayer.getNumberOfDevCards());
        JsonObject resources = new JsonObject();
        resources.addProperty("Unbekannt", currentPlayer.getNumberOfResourceCards());
        JsonObject playerForMe = JsonObjectCreator.createPlayerObject(playerID, getColorOfCurrentPlayer(), currentPlayer.getName(), state, currentPlayer.getVictoryPoints(), NetworkTranslator.translateResourceObject(currentPlayer.getResources()), currentPlayer.getKnightForce(), devCards, currentPlayer.hasBiggestKnightForce(), currentPlayer.hasLongestRoad());
        JsonObject playerForOthers = JsonObjectCreator.createPlayerObject(playerID, getColorOfCurrentPlayer(), currentPlayer.getName(), state, currentPlayer.getVictoryPointsForOthers(), resources, currentPlayer.getKnightForce(), devCardsForOthers, currentPlayer.hasBiggestKnightForce(), currentPlayer.hasLongestRoad());
        currentPlayer.setState(state);
        controller.sendMessageToOtherClients(protocol.statusUpdateToClient(playerForOthers), playerID);
        controller.sendMessageToOneClient(protocol.statusUpdateToClient(playerForMe), playerID);
        System.out.println("RESOURCES OF " + currentPlayer.getName() + ": " + "HOLZ: " + currentPlayer.getQuantityOfAResource("Holz") + ", LEHM: " + currentPlayer.getQuantityOfAResource("Lehm") + ", WOLLE: " + currentPlayer.getQuantityOfAResource("Wolle") + ", GETREIDE: " + currentPlayer.getQuantityOfAResource("Getreide") + ", ERZ: " + currentPlayer.getQuantityOfAResource("Erz"));
        logger.info("RESOURCES OF " + currentPlayer.getName() + ": " + "HOLZ: " + currentPlayer.getQuantityOfAResource("Holz") + ", LEHM: " + currentPlayer.getQuantityOfAResource("Lehm") + ", WOLLE: " + currentPlayer.getQuantityOfAResource("Wolle") + ", GETREIDE: " + currentPlayer.getQuantityOfAResource("Getreide") + ", ERZ: " + currentPlayer.getQuantityOfAResource("Erz"));
    }

    /**
     * Sends the status update to a player when he joins the game
     * @param client client that joined the game
     */
    void sendStatusUpdateToNewPlayers(int client) {
        JsonObject devCards = new JsonObject();
        devCards.addProperty("Unbekannt", 0);
        JsonObject resources = new JsonObject();
        resources.addProperty("Unbekannt", 0);
        JsonObject player = JsonObjectCreator.createPlayerObject(playerID, getColorOfCurrentPlayer(), currentPlayer.getName(), currentPlayer.getState(), currentPlayer.getVictoryPoints(), resources, currentPlayer.getKnightForce(), devCards, currentPlayer.hasBiggestKnightForce(), currentPlayer.hasLongestRoad());
        controller.sendMessageToOneClient(protocol.statusUpdateToClient(player), client);

    }

    /**
     * Sends the result of the dice throw to all clients
     * Changes the state of the player to "Handeln oder Bauen" afterwards
     * @return returns the json object the server sends to the client
     */
    JsonObject sendRollDice() {
        int dice1 = ThreadLocalRandom.current().nextInt(1, 7);
        int dice2 = ThreadLocalRandom.current().nextInt(1, 7);
        int[] diceNumbers = new int[]{dice1, dice2};
        controller.sendMessageToAllClients(protocol.diceNumberToClient(playerID, diceNumbers));
        //falls 7 gewürfelt wird anderes StatusUpdate
        if (!startingPhase) {
            if (dice1 + dice2 != 7) {
                controller.calculateProfitFromDiceThrow(dice1+dice2);
                sendStatusUpdate("Handeln oder Bauen");
            } else {
                for (Player player : controller.getPlayers()) {
                    if (player.getNumberOfResourceCards() > 7) {
                        controller.changeStatusToCardDrop(player.getId());
                    }
                }
                sendStatusUpdate("Räuber versetzen");
            }
        }
        return protocol.diceNumberToClient(playerID, diceNumbers);

    }

    /**
     * Sends the construction progress to all clients
     * @param type type of the building
     * @param location location of the building
     */
    private void sendConstructionProcess(String type, JsonArray location) {
        JsonObject building = JsonObjectCreator.createBuildingObject(playerID, type, location);
        controller.sendMessageToAllClients(protocol.sendNewBuildingToClients(building));
    }

    /**
     * Sends out the profit a player gets from building in starting phase and dice throws
     * @param resources profit the player gets
     */
    void sendProfit(JsonObject resources) {
        changeResourcesOfPlayer(resources, playerID, true);
        int lumber = resources.get("Holz").getAsInt();
        int brick = resources.get("Lehm").getAsInt();
        int wool = resources.get("Wolle").getAsInt();
        int grain = resources.get("Getreide").getAsInt();
        int ore = resources.get("Erz").getAsInt();
        String message = "Du hast ";
        if (lumber > 0) {
            message += lumber + " Holz ";
        }
        if (brick > 0) {
            message += brick + " Lehm ";
        }
        if (wool > 0) {
            message += wool + " Wolle ";
        }
        if (grain > 0) {
            message += grain + " Getreide ";
        }
        if (ore > 0) {
            message += ore + " Erz ";
        }
        message += "bekommen!";
        if (lumber != 0 || brick != 0 || wool != 0 || grain != 0 || ore != 0) {
            controller.sendMessageToAllClients(protocol.profitToClient(playerID, resources));
            sendChatMessageFromServer(message, true);
        }
    }


    /**
     * Sends out the cost for a player if he builds or buys something
     * @param resources resources that the player has to pay
     */
    private void sendCost(JsonObject resources) {
        changeResourcesOfPlayer(resources, playerID, false);
        controller.sendMessageToAllClients(protocol.sendExpenseToClient(playerID, resources));

    }

    /**
     * Sends out the information to all players, that a player bought a development card
     * If it's not the player that bought the card, the information which card he bought is not visible
     */
    private void sendDevelopmentCard() {
        String card = controller.buyADevCard();
        lastCardBought = card;
        currentPlayer.addToDevCards(card);
        controller.sendMessageToOtherClients(protocol.playerBoughtDevCard(playerID, "Unbekannt"), playerID);
        controller.sendMessageToOneClient(protocol.playerBoughtDevCard(playerID, card), playerID);
        sendChatMessageFromServer(currentPlayer.getName() + " hat eine Entwicklungskarte gekauft.", false);
        if (card.equals("Siegpunkt")) {
            currentPlayer.increaseVictoryPoints();
            currentPlayer.adjustVictoryPointsForOthers();
            if (currentPlayer.hasWon()) {
                sendGameFinished();
            }
        }
    }

    /**
     * Sends out the information to all players, that a player bought a development card
     * If it's not the player that bought the card, the information which card he bought is not visible
     */
    private void sendDevelopmentCard(Player player, String card) {
        lastCardBought = card;
        player.addToDevCards(card);
        controller.sendMessageToOtherClients(protocol.playerBoughtDevCard(player.getId(), "Unbekannt"), player.getId());
        controller.sendMessageToOneClient(protocol.playerBoughtDevCard(player.getId(), card), player.getId());
        sendChatMessageFromServer(player.getName() + " hat eine Entwicklungskarte " + card + " bekommen.", false);
    }

    /**
     * Sends out the error that a player chose a color that's not available anymore
     */
    private void sendMistake(){
        sendChatMessageFromServer("Farbe bereits vergeben", false);
        controller.sendMessageToOneClient(protocol.accessDeniedToClient(), playerID);
    }

    /**
     * Sends the location and the robbed player when the robber was moved
     * @param location new location of the robber
     * @param idRobbedPlayer id of the robbed player
     */
    private void sendRobberMoved(JsonObject location, int idRobbedPlayer) {
        controller.sendMessageToAllClients(protocol.robberMovedToClient(playerID, location, idRobbedPlayer));

    }

    /**
     * Sends the location and the robbed player when the knight card was played and the robber was moved
     * @param location new location of the robber
     * @param targetId id of the robbed player
     */
    private void sendKnightCardPlayed(JsonObject location, int targetId) {
        sendChatMessageFromServer(currentPlayer.getName() + " hat eine Ritter-Karte gespielt.", false);
        currentPlayer.removeDevCard("Ritter");
        controller.sendMessageToAllClients(protocol.playKnightCardToClient(playerID, location, targetId));
        currentPlayer.increaseKnightForce();
        if (controller.tryToBecomeBiggestKnightForce(currentPlayer)) {
            if (!currentPlayer.hasBiggestKnightForce()) {
                currentPlayer.setHasBiggestKnightForce(true);
                sendBiggestKnightForce();
            }
        }
    }

    /**
     * Sends the resource that the player wants to have from the other players when he played the monopoly card to all players
     * @param resource resource that the player wants to have
     */
    private void sendMonopolyCardPlayed(String resource) {
        sendChatMessageFromServer(currentPlayer.getName() + " hat eine Monopol-Karte gespielt.", false);
        currentPlayer.removeDevCard("Monopol");
        controller.sendMessageToAllClients(protocol.playMonopolCardToClient(playerID, resource));
    }

    /**
     * Sends the resource that the player
     * @param resource resource the player wants to have
     */
    private void sendYearOfPlentyCardPlayed(JsonObject resource) {
        sendChatMessageFromServer(currentPlayer.getName() + " hat eine Erfindungs-Karte gespielt.", false);
        controller.sendMessageToOneClient(protocol.playYearOfPlentyCardToClient(playerID, resource), playerID);
        JsonObject anonymousResource = JsonObjectCreator.createAnonymousResourcesObject(2);
        controller.sendMessageToOtherClients(protocol.playYearOfPlentyCardToClient(playerID, anonymousResource), playerID);
        controller.sendMessageToOneClient(protocol.profitToClient(playerID, resource), playerID);
        controller.sendMessageToOtherClients(protocol.profitToClient(playerID, anonymousResource), playerID);
        currentPlayer.removeDevCard("Erfindung");
        changeResourcesOfPlayer(resource, playerID, true);
    }

    /**
     * sends out message when road building card was played
     * @param road1 first road the player wants to build
     * @param road2 second road the player wants to build
     */
    private void sendRoadBuildingCardPlayed(JsonArray road1, JsonArray road2) {
        JsonObject streetMessage;
        if (road2 != null) {
            streetMessage = protocol.playBuildStreetCardToClient(playerID, road1, road2);
        } else {
            streetMessage = protocol.playBuildStreetCardToClient(playerID, road1, null);
        }
        controller.sendMessageToAllClients(streetMessage);
        currentPlayer.removeDevCard("Straßenbau");
    }

    /**
     * Sends out the message that the player did an action that was OK
     */
    private void sendOk(){
        controller.sendMessageToOneClient(protocol.answerToClientAction(true, null), playerID);
    }

    /**
     * Sends out the message that the player did an action that was not OK
     * @param errorMessage the reason why the action was not OK
     */
    private void sendNotOk(String errorMessage){
        controller.sendMessageToOneClient(protocol.answerToClientAction(false, errorMessage), playerID);
    }

    /**
     * When the currentPlayer gets the longest road, it will be sent to all clients
     * If a player loses the longest road, it will an empty object
     */
    private void sendLongestRoad(int id) {
        controller.sendMessageToAllClients(protocol.longestRoadToClient(id));
        if (id != -1){
            currentPlayer.increaseVictoryPoints();
            currentPlayer.increaseVictoryPoints();
            sendChatMessageFromServer(controller.getPlayerById(id).getName() + " besitzt jetzt die längste Handelsstraße", false);
        } else {
            sendChatMessageFromServer("Niemand besitzt die längste Handelsstraße", false);
        }
        if (currentPlayer.hasWon()) {
            sendGameFinished();
        }
    }

    /**
     * When the currentPlayer gets the biggest knight, it will be sent to all clients
     */
    private void sendBiggestKnightForce() {
        currentPlayer.increaseVictoryPoints();
        currentPlayer.increaseVictoryPoints();
        sendChatMessageFromServer(currentPlayer.getName() + " hat jetzt die größte Rittermacht.", false);
        controller.sendMessageToAllClients(protocol.biggestKnightForceToClient(playerID));
        if (currentPlayer.hasWon()) {
            sendGameFinished();
        }
    }

    /**
     * Sends a chat message that comes from the server to all players
     * @param message content of the message
     */
    private void sendChatMessageFromServer(String message, boolean one) {
        if(!one) {
            controller.sendMessageToAllClients(protocol.sendChatMessageToClients(-1, message));
        } else {
            controller.sendMessageToOneClient(protocol.sendChatMessageToClients(-1, message), playerID);
        }
    }

    private void sendChatMessageFromServer(String message, int playerID) {
        controller.sendMessageToOneClient(protocol.sendChatMessageToClients(-1, message), playerID);
    }

    /**
     * sends out the TradeOffer from a Client to all other Clients
     * @param tradeId
     * @param offer
     * @param request
     */
    private void sendTradeOffer(int tradeId, JsonObject offer, JsonObject request){
        logger.info("send trade offer: trade ID: " + tradeId + "; offer: " + offer + "; request: " + request);
        controller.sendMessageToAllClients(protocol.requestDomesticTradeToClient(playerID, tradeId, offer, request));
    }

    /**
     * sends out message if a trade offer was accepted or denied
     * @param tradeId id of the trade
     * @param accept boolean if the player accepts the trade or not
     */
    private void sendAcceptTradeOffer(int tradeId, boolean accept){
        controller.sendMessageToAllClients(protocol.acceptOfferToClient(playerID, tradeId, accept));
    }

    /**
     * sends out message when a trade was finished
     * @param otherPlayer id of the player that was part of the trade
     */
    private void sendTradeFinished(int otherPlayer){
        controller.sendMessageToAllClients(protocol.finishTradeToClient(playerID, otherPlayer));
    }

    /**
     * sends out cost and profit for otherPlayer who traded with currentPlayer
     * @param offerFromClient offer from the client
     * @param demandFromClient demand from the client
     */
    private void sendCostAndProfitFromTrade(JsonObject offerFromClient, JsonObject demandFromClient, int otherPlayer){
        //change Resources from otherPlayer
        changeResourcesOfPlayer(offerFromClient, otherPlayer, true);
        changeResourcesOfPlayer(demandFromClient, otherPlayer, false);
        //Cost Message from otherPlayer
        controller.sendMessageToAllClients(protocol.sendExpenseToClient(otherPlayer, demandFromClient));
        //Profit Message from otherPlayer
        controller.sendMessageToAllClients(protocol.profitToClient(otherPlayer, offerFromClient));
    }

    /**
     * sends out message when a trade was canceled
     * @param tradeId id of the trade
     */
    private void sendTradeCanceled(int tradeId){
        controller.sendMessageToAllClients(protocol.cancelTradeToClient(playerID, tradeId));
    }

    /**
     * sends out cost and profit Message, when a player did a harbor trade
     * @param offer offer
     * @param request request
     */
    private void sendHarborTradeCostsAndProfit(JsonObject offer, JsonObject request) {
        sendCost(offer);
        sendProfit(request);
    }

    /**
     * If a player has 10 victory points, the server sends the message that he won
     */
    private void sendGameFinished() {
        controller.sendMessageToAllClients(protocol.sendPlayerWinsToClient("Spieler " + currentPlayer.getName() + " hat das Spiel gewonnen.", playerID));
    }


    /**
     * sends out cost and profit message when stealing
     * @param type of the resource
     * @param targetId id of the player that gets stolen from
     * @param resources resources
     */
    private void sendCostAndProfitWhenStealing(String type, int targetId, JsonObject resources) {
        //Messages for Participants
        JsonObject costMessage = protocol.sendExpenseToClient(targetId, resources);
        controller.sendMessageToOneClient(costMessage, targetId);
        controller.sendMessageToOneClient(costMessage, playerID);
        controller.getPlayerById(targetId).changeResourceQuantity(type, -1);

        JsonObject profitMessage = protocol.profitToClient(playerID, resources);
        controller.sendMessageToOneClient(profitMessage, targetId);
        controller.sendMessageToOneClient(profitMessage, playerID);
        currentPlayer.changeResourceQuantity(type, 1);

        //Messages for others concerning the robber move
        JsonObject unknownResources = JsonObjectCreator.createAnonymousResourcesObject(1);
        JsonObject costMessageForOthers = protocol.sendExpenseToClient(targetId, unknownResources);
        JsonObject profitMessageForOthers = protocol.profitToClient(playerID, unknownResources);
        controller.sendMessageToOtherClients(costMessageForOthers, playerID, targetId);
        controller.sendMessageToOtherClients(profitMessageForOthers, playerID, targetId);
    }

    /**
     * Calculates the profit for a landpiece
     * @param coordinates of the landpiece
     * @return JsonObject with the profit
     */
    private JsonObject calculateProfit(Utility.Pair coordinates) {
        ArrayList<Landpiece> landpieces = controller.getBoard().getNeighbouringLandpieces(coordinates);
        int lumber = 0;
        int brick = 0;
        int wool = 0;
        int grain = 0;
        int ore = 0;
        for (Landpiece landpiece : landpieces) {
            if (landpiece != null) {
                LandpieceType landpieceType = landpiece.getResourceType();
                ResourceType resourceType = NetworkTranslator.translateLandpieceTypeToResourceType(landpieceType);
                if (resourceType != null) {
                    switch (resourceType) {
                        case LUMBER:
                            lumber++;
                            break;
                        case BRICK:
                            brick++;
                            break;
                        case WOOL:
                            wool++;
                            break;
                        case GRAIN:
                            grain++;
                            break;
                        case ORE:
                            ore++;
                            break;
                    }
                }
            }
        }
        return JsonObjectCreator.createResourcesObject(lumber, brick, wool, grain, ore);
    }

    /**
     * changes the resources of the player in model
     * @param resources resources that get changed
     * @param id id of the player
     * @param profit boolean if it is profit or cost
     */
    private void changeResourcesOfPlayer(JsonObject resources, int id, boolean profit) {
        int lumber = resources.get("Holz").getAsInt();
        int brick = resources.get("Lehm").getAsInt();
        int wool = resources.get("Wolle").getAsInt();
        int grain = resources.get("Getreide").getAsInt();
        int ore = resources.get("Erz").getAsInt();
        if (profit) {
            controller.getPlayerById(id).changeResourceQuantity("lumber", lumber);
            controller.getPlayerById(id).changeResourceQuantity("brick", brick);
            controller.getPlayerById(id).changeResourceQuantity("wool", wool);
            controller.getPlayerById(id).changeResourceQuantity("grain", grain);
            controller.getPlayerById(id).changeResourceQuantity("ore", ore);
        } else {
            controller.getPlayerById(id).changeResourceQuantity("lumber", -lumber);
            controller.getPlayerById(id).changeResourceQuantity("brick", -brick);
            controller.getPlayerById(id).changeResourceQuantity("wool", -wool);
            controller.getPlayerById(id).changeResourceQuantity("grain", -grain);
            controller.getPlayerById(id).changeResourceQuantity("ore", -ore);
        }
    }

    /**
     * closes the socket
     */
    private void closeSocket() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sets controller
     * @param controller
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * getter for playerID
     * @return
     */
    int getPlayerID() {
        return playerID;
    }

    /**
     * getter for getColorOfCurrentPlayer
     * @return
     */
    String getColorOfCurrentPlayer() {
        return colorOfCurrentPlayer;
    }

    /**
     * getter for nameOfCurrentPlayer
     * @return
     */
    String getNameOfCurrentPlayer() {
        return currentPlayer.getName();
    }

    /**
     * setter for startingPhase
     * @param startingPhase
     */
    void setStartingPhase(boolean startingPhase) {
        this.startingPhase = startingPhase;
    }

    /**
     * getter for currentPlayer
     * @return
     */
    Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * setter for startingPlayer
     * @param startingPlayer
     */
    void setStartingPlayer(boolean startingPlayer) {
        isStartingPlayer = startingPlayer;
    }

    /**
     * getter for onlyOneResourceObject
     * @param type
     * @return
     */
    private JsonObject getOnlyOneResourceObject(String type) {
        JsonObject resources = new JsonObject();
        switch(type){
            case "lumber":
                resources = JsonObjectCreator.createResourcesObject(1,0,0,0,0);
                break;
            case "brick":
                resources = JsonObjectCreator.createResourcesObject(0,1,0,0,0);
                break;
            case "wool":
                resources = JsonObjectCreator.createResourcesObject(0,0,1,0,0);
                break;
            case "grain":
                resources = JsonObjectCreator.createResourcesObject(0,0,0,1,0);
                break;
            case "ore":
                resources = JsonObjectCreator.createResourcesObject(0,0,0,0,1);
                break;
        }
        return resources;
    }

    /**
     * Tries to change the state of the player, but doesn't if someone has to drop cards
     */
    private void tryToChangeToTradeOrBuild() {
        if (controller.checkIfSomeoneHasToDropCards(playerID)) {
            //TODO: show popup
        } else {
            sendStatusUpdate("Handeln oder Bauen");
        }
    }

    /**
     * Checks if a player has to drop cards
     * @return boolean
     */
    boolean isNoCardDrop() {
        return noCardDrop;
    }

    /**
     * handles cheats
     * @param cheatOld cheat
     */
    private void handleCheats(String cheatOld){
        String cheat = cheatOld.replaceAll("\\s+","").toLowerCase();
        System.out.println("CHEAT: " + cheat);
        switch (cheat){
            case "godmode":
                JsonObject resources = JsonObjectCreator.createResourcesObject(10,10,10,10,10);
                sendProfit(resources);
                break;
            case "giveinvention":
                for(Player player : controller.getPlayers()){
                    if(player.getId() != currentPlayer.getId()){
                        sendDevelopmentCard(player,"Erfindung");
                    }
                }
                break;
            case "giveknight":
                for(Player player : controller.getPlayers()){
                    if(player.getId() != currentPlayer.getId()){
                        sendDevelopmentCard(player,"Ritter");
                    }
                }
                break;
            case "givemonopoly":
                for(Player player : controller.getPlayers()){
                    if(player.getId() != currentPlayer.getId()){
                        sendDevelopmentCard(player,"Monopol");
                    }
                }
                break;
            case "givestreets":
                for(Player player : controller.getPlayers()){
                    if(player.getId() != currentPlayer.getId()){
                        sendDevelopmentCard(player,"Straßenbau");
                    }
                }
                break;
            case "getstreetcard":
                sendDevelopmentCard(currentPlayer, "Straßenbau");
                break;
            case "getknightcard":
                sendDevelopmentCard(currentPlayer, "Ritter");
                break;
            case "getmonopoly":
                sendDevelopmentCard(currentPlayer, "Monopol");
                break;
            case "getinvention":
                sendDevelopmentCard(currentPlayer, "Erfindung");
                break;
            case "getlumber":
                JsonObject lumber = JsonObjectCreator.createResourcesObject(5,0,0,0,0);
                sendProfit(lumber);
                break;
            case "getbrick":
                JsonObject brick = JsonObjectCreator.createResourcesObject(0,5,0,0,0);
                sendProfit(brick);
                break;
            case "getwool":
                JsonObject wool = JsonObjectCreator.createResourcesObject(0,0,5,0,0);
                sendProfit(wool);
                break;
            case "getgrain":
                JsonObject grain = JsonObjectCreator.createResourcesObject(0,0,0,5,0);
                sendProfit(grain);
                break;
            case "getore":
                JsonObject ore = JsonObjectCreator.createResourcesObject(0,0,0,0,5);
                sendProfit(ore);
                break;
            case "winnerwinnerchickendinner":
                JsonObject victory = new JsonObject();
                victory.add("Siegpunkt",new JsonObject());
                controller.sendMessageToOneClient(victory, playerID);
                currentPlayer.increaseVictoryPoints();
                if (currentPlayer.hasWon()) {
                    sendGameFinished();
                }
                break;
            case "leavebritneyalone":
                noCardDrop = true;
                break;
            case "nice":
                if(!startingPhase) {
                    currentPlayer.setColor(Color.BLACK);
                    JsonObject color = new JsonObject();
                    color.add("Farbwechsel", new JsonObject());
                    controller.sendMessageToOneClient(color, playerID);
                    break;
                } else {
                    sendChatMessageFromServer("Dieser Cheat ist erst nach der Startphase möglich", true);
                }
                break;
            case "harbor":
                JsonObject input = new JsonObject();
                JsonObject offer = JsonObjectCreator.createResourcesObject(3, 0, 0, 0, 0);
                JsonObject request = JsonObjectCreator.createResourcesObject(0, 0, 1, 0, 0);
                JsonObject res = new JsonObject();
                res.add("Angebot", offer);
                res.add("Nachfrage", request);
                input.add("Seehandel", res);
                handleHarborTradeOffer(input);
                break;
            default:
                sendChatMessageFromServer("Ungültiger Cheat", true);
                break;
        }
    }


}
