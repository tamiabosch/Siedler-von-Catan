package network;

import LOG.Logging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import control.Controller;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import model.*;
import view.BoardView;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles the communication with the server
 * @author Tamia & Kevin
 */
public class ClientController implements Runnable {

    private OutputStreamWriter writer;
    private JsonParser parser = new JsonParser();
    private int id;
    private Socket clientSocket;
    private Player currentPlayer;
    private boolean socketClosed;
    private String server;
    private Controller controller;
    private Protocol10 protocol = new Protocol10();
    private Logger logger  = BoardView.getLogger();
    private String newLine = "\n";
    private String serverIP;
    private int currentTradeId = 0;
    private ArrayList <Integer> currentAcceptedPlayers = new ArrayList<>();
    private int playersAnsweredToTrade = 0;
    private boolean isTradingPlayer = false;
    private int idOfTradingPlayer = 0;
    private boolean devCardBoughtInThisRound = false;
    private boolean wasRobberMoving = false;
    private boolean hasToWait = false;
    private boolean wasHoldingLongestRoad = false;
    private boolean wasBiggestKnightForce = false;
    private String lastCardBought = "";

    /**
     * Tells the client controller which server and which protocol version it has to start
     * @param server server the client connects with
     * @throws Exception exception
     */
    public ClientController(String server, String serverIP) throws Exception {
        this.server = server;
        this.serverIP = serverIP;
    }

    /**
     * Starts the client controller as a thread
     */
    @Override
    public void run() {
        try {
            startClientController(server, serverIP);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * just a test method, does nothing to the actual game
     * @param args just test
     * @throws Exception test
     */
    public static void main(String[] args) throws Exception {
        String cheat = "//       ichbineincheat";
        String replaced = cheat.replaceAll("\\s+","");
        System.out.println(replaced);
        System.out.println(replaced.substring(0, 2));
        System.out.println(replaced.substring(2));
    }

    /**
     * Starts the connection to the server and initializes the OutputStreamWriter to give messages to the server and the BufferedReader to read messages from the Server
     * @param server String contains the server the clients wants to connect to
     * @throws Exception if connection not possible
     */
    public void startClientController(String server, String serverIP) throws Exception {
        logger.info("connecting to: " + server + "  " + serverIP);

        BufferedReader reader;
        switch (server) {
            case "TestServer":
                clientSocket = new Socket("aruba.dbs.ifi.lmu.de", 10003);
                break;

            case "Nicer Server":
                clientSocket = new Socket(serverIP, 6789);
                logger.info("socket startet: " + serverIP);
        }

        writer = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8");
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));

        try {
        while (!socketClosed) {
            String inputFromServer = reader.readLine();
            System.out.println(" IN: " + inputFromServer);
            if (inputFromServer != null) {
                handleInputFromServer(parser.parse(inputFromServer).getAsJsonObject()); // converts string to jsonobject
            } else {
                clientSocket.close();
                System.out.println("closed socket due to inputFromServer = null");
                Platform.runLater(() -> controller.getBoardViewController().lostConnectionToServerPopup());
                socketClosed = true;
            }
        }} catch (SocketException e){
            System.out.println("Programm beendet");
            logger.log(Level.SEVERE, e.getMessage());
        }

    }

    /**
     * Checks the first key of the Json-Object the Client received from the server and calls the appropriate method
     * @param inputFromServer JsonObject that the server sends to the client
     */
    private void handleInputFromServer(JsonObject inputFromServer) throws Exception {

        List<String> keys = inputFromServer.entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        logger.info(keys.get(0) + inputFromServer.toString());

        switch (keys.get(0)) {
            case "Hallo":
                handleHello(inputFromServer);
                break;
            case "Willkommen":
                handleWelcome(inputFromServer);
                break;
            case "Serverantwort":
                handleActionValidation(inputFromServer);
                break;
            case "Chatnachricht":
                handleChatMessage(inputFromServer);
                break;
            case "Fehler":
                handleError(inputFromServer);
                break;
            case "Spiel gestartet":
                handleGameStarted(inputFromServer);
                break;
            case "Statusupdate":
                handleStatusUpdate(inputFromServer);
                break;
            case "Würfelwurf":
                handleDiceThrow(inputFromServer);
                break;
            case "Ertrag":
                handleProfit(inputFromServer);
                break;
            case "Bauvorgang":
                handleNewBuilding(inputFromServer);
                break;
            case "Kosten":
                handleCosts(inputFromServer);
                break;
            case "Räuber versetzt":
                handleRobberMoved(inputFromServer);
                break;
            case "Spiel beendet":
                handleGameFinished(inputFromServer);
                break;
            case "Handelsangebot":
                handleTradeOffer(inputFromServer);
                break;
            case "Handelsangebot angenommen":
                handleTradeAccepted(inputFromServer);
                break;
            case "Handelsangebot abgebrochen":
                handleTradeCancel(inputFromServer);
                break;
            case "Handel ausgeführt":
                handleTradeFinish(inputFromServer);
                break;
            case "Entwicklungskarte gekauft":
                handleDevCardBought(inputFromServer);
                break;
            case "Ritter ausspielen":
                handleKnightCardPlayed(inputFromServer);
                break;
            case "Monopol":
                handleMonopolyCardPlayed(inputFromServer);
                break;
            case "Erfindung":
                handleYearOfPlentyCardPlayed(inputFromServer);
                break;
            case "Längste Handelsstraße":
                handleLongestTradeRoad(inputFromServer);
                break;
            case "Größte Rittermacht":
                handleBiggestKnightForce(inputFromServer);
                break;
            case "Straßenbaukarte ausspielen":
                handleRoadBuildCardPlayed(inputFromServer);
                break;
            // CHEATS:
            case "Siegpunkt":
                handleVictoryPoint();
                break;
            case "Farbwechsel":
                handleColorChange();
                break;
        }
    }

    /**
     * checks if the protocol version matches on server and client
     * @param inputFromServer inputFromServer
     */
    private void handleHello(JsonObject inputFromServer) {
        try {
            JsonObject nestedInput = inputFromServer.get("Hallo").getAsJsonObject();
            if (nestedInput.get("Protokoll").getAsString().equals("1.0")) {
                sendHello();
                controller.getLobbyWindowController().sentMessage("Server erfolgreich verbunden.");
            } else {
                System.out.println("Protokoll stimmt nicht überein.");
                logger.log(Level.SEVERE, "Protokoll stimmt nicht überein");
                socketClosed = true;
                clientSocket.close();
            }
        } catch (Exception e) {
            System.out.println("error in handleHello");
            logger.log(Level.SEVERE, "error in handleHello");

        }
    }

    /**
     * saves the id the client gets from the server
     * @param inputFromServer inputFromServer
     */
    private void handleWelcome(JsonObject inputFromServer) {
        logger.info(".");
        JsonObject nestedInput = inputFromServer.get("Willkommen").getAsJsonObject();
        id = nestedInput.get("id").getAsInt();
    }

    /**
     * checks if the action the client sent to the server was valid
     * @return boolean true = valid
     */
    private boolean handleActionValidation(JsonObject inputFromServer) {
        // if okay, send true, if not ok send false
        return inputFromServer.get("Serverantwort").getAsString().equals("OK");
    }

    /**
     * (6.3)
     * Handels a Chat Message from a Client, which was distributed by the Server
     * @param inputFromServer inputFromServer
     */
    private void handleChatMessage(JsonObject inputFromServer) throws IOException {
        int id = 0;
        if (inputFromServer.get("Chatnachricht").getAsJsonObject().has("Absender")) {
            id = inputFromServer.get("Chatnachricht").getAsJsonObject().get("Absender").getAsInt();
        }
        String chatMessage = inputFromServer.get("Chatnachricht").getAsJsonObject().get("Nachricht").getAsString();
        if (controller.getBoardViewController() != null) {
            controller.getBoardViewController().sentMessage(chatMessage, id);
        } else {
            controller.getLobbyWindowController().sentMessage(chatMessage, id);
        }
    }

    /**
     * (7)
     * Server sends out an error, since the chosen color was already taken
     *
     * @param inputFromServer inputFromServer
     */
    private void handleError(JsonObject inputFromServer) {
        String errorMessage = inputFromServer.get("Fehler").getAsJsonObject().get("Meldung").toString();
        controller.getLobbyWindowController().enableButtonsAfterColorError();
        System.out.println(errorMessage);
        logger.log(Level.SEVERE, errorMessage);

    }

    /**
     * (7)
     * all of the players started, so the Server sends out the board
     *
     * @param inputFromServer inputFromServer
     */
    private void handleGameStarted(JsonObject inputFromServer) throws Exception {
        logger.info(inputFromServer.toString());
        JsonObject map = inputFromServer.get("Spiel gestartet").getAsJsonObject().get("Karte").getAsJsonObject();
        Board boardFromServer = NetworkTranslator.translateMapToStart(map);
        controller.setBoard(boardFromServer);
        controller.getLobbyWindowController().changeSceneToGameBoard();
    }

    /**
     * Calls the methods that are needed when the server changed the status of a player
     * @param inputFromServer inputFromServer
     * @throws Exception because of thread change
     */
    private void handleStatusUpdate(JsonObject inputFromServer) throws Exception {
        JsonObject playerObject = inputFromServer.get("Statusupdate").getAsJsonObject().get("Spieler").getAsJsonObject();

        int id = playerObject.get("id").getAsInt();
        String color = "";
        if (playerObject.has("Farbe")) {
            if (!playerObject.get("Farbe").isJsonNull()) {
                color = playerObject.get("Farbe").getAsString();
            }
        }

        String name = "";
        if (playerObject.has("Name")) {
            if (!(playerObject.get("Name").isJsonNull())) {
                name = playerObject.get("Name").getAsString();
            }
        }

        String oldStatus = "";
        if(currentPlayer != null && currentPlayer.getState() != null){
            oldStatus = currentPlayer.getState();
        }
        String status = playerObject.get("Status").getAsString();
        //JsonObject resources = playerObject.get("Rohstoffe").getAsJsonObject();

        JsonObject developmentCards = new JsonObject();

        if (id != this.id) {
            if (playerObject.has("Siegpunkte")) {
                int points = playerObject.get("Siegpunkte").getAsInt();
                if (controller.getPlayerById(id) != null) {
                    controller.getPlayerById(id).setVictoryPointsForOthers(points);
                }
            }
        }

        controller.changeStateOfPlayer(id, status);

        if (controller.getBoardViewController() != null) {
            if(!oldStatus.equals("") && !oldStatus.equals(currentPlayer.getState())){
                controller.getBoardViewController().sentMessage("Aktueller Status: " + currentPlayer.getState());
            }
                if(!status.equals("Warten")) controller.getBoardViewController().setActivePlayer(id);
                Platform.runLater(() -> controller.getBoardViewController().setActivePlayerEffect());
        }

     //   logger.info(status);

        switch (status) {
            case "Spiel starten":
                break;
            case "Wartet auf Spielbeginn":
                if (this.id != id) {
                    controller.disableColorChoices(color);
                    controller.addPlayer(NetworkTranslator.translatePlayer(name, id, color));
                } else {
                    currentPlayer = NetworkTranslator.translatePlayer(name, id, color);
                    controller.addPlayer(currentPlayer);
                    controller.setCurrentPlayer(currentPlayer);
                }
                break;
            case "Dorf bauen":
            	 if(this.id == id){
                     Platform.runLater(() -> controller.getBoardViewController().yourTurnPopup(true));
                     Platform.runLater(() -> controller.getBoardViewController().drawViableSettlements(true));
                 }
                break;
            case "Straße bauen":
                if(this.id == id){
                    Platform.runLater(() -> controller.getBoardViewController().drawViableStreets(true));
                }
                break;
            case "Würfeln":
                if(this.id == id){
                    Platform.runLater(() -> {
                        controller.getBoardViewController().yourTurnPopup(false);
                        controller.getBoardViewController().setDiceGif();
                    });
                } else {
                    Platform.runLater(() -> controller.getBoardViewController().setDiceGif());
                }
                break;
            case "Räuber versetzen":
                if(this.id == id){
                    wasRobberMoving = true;
                    Platform.runLater(() -> controller.getBoardViewController().moveRobber());
                }
            case "Handeln oder Bauen":
                if(this.id == id && !wasRobberMoving){
                    wasRobberMoving = false;
                }
                if (this.id == id && hasToWait) {
                    hasToWait = false;
                    Platform.runLater(() -> controller.getBoardViewController().closeWaitingDroppingCardsPopup());
                }
                break;
            case "Warten":
                break;
            case "Verbindung verloren":
                controller.removePlayer(id);
                //clientSocket.close();
                //socketClosed = true;
                break;
            case "Karten wegen Räuber abgeben":
                if(this.id == id){
                    Platform.runLater(() -> {
                        try {
                            controller.getBoardViewController().openTradeWindowOrRobber("RobberMode",null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
        }

        if (controller.getBoardViewController() != null) {
        	Platform.runLater(()-> controller.getBoardViewController().checkButtonEnabled());
        }
    }

    /**
     * handles the dice throw of the server and gives it to the model and view
     * @param inputFromServer inputFromServer
     */
    private void handleDiceThrow(JsonObject inputFromServer) {
        //int playerID = inputFromServer.get("Würfelwurf").getAsJsonObject().get("Spieler").getAsInt();
        //int diceNumber = inputFromServer.get("Würfelwurf").getAsJsonObject().get("Wurf").getAsInt();
        int dice1 = inputFromServer.get("Würfelwurf").getAsJsonObject().get("Wurf").getAsJsonArray().get(0).getAsInt();
        int dice2 = inputFromServer.get("Würfelwurf").getAsJsonObject().get("Wurf").getAsJsonArray().get(1).getAsInt();

        // first dice throws to determine who starts won't be shown (nullpointerexception)
        if (controller.getBoardViewController() != null) {
            controller.getBoardViewController().setDice(dice1, dice2);
        }
    }

    /**
     * handles the incoming and outgoing resources of a player
     * @param inputFromServer inputFromServer
     */
    private void handleProfit(JsonObject inputFromServer) {
        int id = inputFromServer.get("Ertrag").getAsJsonObject().get("Spieler").getAsInt();
        JsonObject resourceObject = inputFromServer.get("Ertrag").getAsJsonObject().get("Rohstoffe").getAsJsonObject();
        int totalResources = 0;
        if (this.id == id) {       	
            if (resourceObject.has("Holz")) {
                currentPlayer.changeResourceQuantity("lumber", resourceObject.get("Holz").getAsInt());
                totalResources += resourceObject.get("Holz").getAsInt();
            }

            if (resourceObject.has("Lehm")) {
                currentPlayer.changeResourceQuantity("brick", resourceObject.get("Lehm").getAsInt());
                totalResources += resourceObject.get("Lehm").getAsInt();
            }

            if (resourceObject.has("Wolle")) {
                currentPlayer.changeResourceQuantity("wool", resourceObject.get("Wolle").getAsInt());
                totalResources += resourceObject.get("Wolle").getAsInt();
            }

            if (resourceObject.has("Getreide")) {
                currentPlayer.changeResourceQuantity("grain", resourceObject.get("Getreide").getAsInt());
                totalResources += resourceObject.get("Getreide").getAsInt();
            }

            if (resourceObject.has("Erz")) {
                currentPlayer.changeResourceQuantity("ore", resourceObject.get("Erz").getAsInt());
                totalResources += resourceObject.get("Erz").getAsInt();
            }
            currentPlayer.changeResourcesTotal(totalResources);
            Platform.runLater(() -> {
                controller.getBoardViewController().setResourceNum();
                controller.getBoardViewController().updatePlayerInfoLabels();
                controller.getBoardViewController().checkButtonEnabled();
            });



        } else {
            // mal wieder Protokollfehler (man bekommt am Anfang auch von fremden Spielern die Rohstoffe), deswegen für den Testserver:
            int bugAmount = 0;
            if (resourceObject.has("Holz")) {
                bugAmount += resourceObject.get("Holz").getAsInt();
            }

            if (resourceObject.has("Lehm")) {
                bugAmount += resourceObject.get("Lehm").getAsInt();
            }

            if (resourceObject.has("Wolle")) {
                bugAmount += resourceObject.get("Wolle").getAsInt();
            }

            if (resourceObject.has("Getreide")) {
                bugAmount += resourceObject.get("Getreide").getAsInt();
            }

            if (resourceObject.has("Erz")) {
                bugAmount += resourceObject.get("Erz").getAsInt();
            }
            int amount = 0;
            if (resourceObject.has("Unbekannt")) {
                amount = resourceObject.get("Unbekannt").getAsInt();
            }
            // increase/decrease quantity of resources of other player
            controller.getPlayerById(id).changeResourcesTotal(amount);
            controller.getPlayerById(id).changeResourcesTotal(bugAmount);
        }
        Platform.runLater(() -> controller.getBoardViewController().updatePlayerInfoLabels());
    }

    /**
     * handles the message that another player built a street or building and sends it to the controller so it can be build in the model and view
     * (8.4)
     *
     * @param inputFromServer inputFromServer
     */
    private void handleNewBuilding(JsonObject inputFromServer) {
        JsonObject building = inputFromServer.get("Bauvorgang").getAsJsonObject().get("Gebäude").getAsJsonObject();
        //String location = building.get("Ort").getAsString();
        JsonArray location = building.get("Ort").getAsJsonArray();
        int owner = building.get("Eigentümer").getAsInt();
        switch (building.get("Typ").getAsString()) {
            case "Straße":
                // translate location of street and build it in controller
                Utility.Pair[] pairs = NetworkTranslator.translateCoordinateToIntersections(location);
            	controller.getBoard().addStreet(pairs[0], pairs[1], controller.getPlayerById(owner));
            	Platform.runLater(() -> controller.getBoardViewController().drawStreets());
                if (owner == id) {
                    controller.getBoardViewController().increaseStreetsBuilt();
                }
                break;
            case "Dorf":
                if (owner == id) {
                    currentPlayer.increaseVictoryPoints();
                    controller.getBoardViewController().increaseSettlementsBuilt();
                } else {
                    controller.getPlayerById(owner).increaseVictoryPoints();
                }
                Platform.runLater(() -> controller.getBoardViewController().updatePlayerInfoLabels());
                // translate location of settlement and build it in controller
                controller.getBoard().addSettlement(NetworkTranslator.translateIntersectionCoordinate(location), controller.getPlayerById(owner));
                Platform.runLater(() -> controller.getBoardViewController().drawSettlements());
                break;
            case "Stadt":
                if (owner == id) {
                    currentPlayer.increaseVictoryPoints();
                    controller.getBoardViewController().increaseCitysBuilt();
                } else {
                    controller.getPlayerById(owner).increaseVictoryPoints();
                }
                Platform.runLater(() -> controller.getBoardViewController().updatePlayerInfoLabels());
                // translate location of city and build it in controller
                controller.getBoard().addCity(NetworkTranslator.translateIntersectionCoordinate(location), controller.getPlayerById(owner));
                Platform.runLater(() -> controller.getBoardViewController().drawSettlements());
        }
    }

    /**
     * Handles the message that the currentPlayer needs to give away some resources
     * @param inputFromServer inputFromServer
     */
    private void handleCosts(JsonObject inputFromServer) {
        JsonObject costs = inputFromServer.get("Kosten").getAsJsonObject();
        int id = costs.get("Spieler").getAsInt();
        JsonObject resources = costs.get("Rohstoffe").getAsJsonObject();
        int totalResources = 0;

        if(this.id == id) {
            if (resources.has("Holz")) {
                currentPlayer.changeResourceQuantity("lumber", -resources.get("Holz").getAsInt());
                totalResources += resources.get("Holz").getAsInt();
            }

            if (resources.has("Lehm")) {
                currentPlayer.changeResourceQuantity("brick", -resources.get("Lehm").getAsInt());
                totalResources += resources.get("Lehm").getAsInt();
            }

            if (resources.has("Wolle")) {
                currentPlayer.changeResourceQuantity("wool", -resources.get("Wolle").getAsInt());
                totalResources += resources.get("Wolle").getAsInt();
            }

            if (resources.has("Getreide")) {
                currentPlayer.changeResourceQuantity("grain", -resources.get("Getreide").getAsInt());
                totalResources += resources.get("Getreide").getAsInt();
            }

            if (resources.has("Erz")) {
                currentPlayer.changeResourceQuantity("ore", -resources.get("Erz").getAsInt());
                totalResources += resources.get("Erz").getAsInt();
            }
            currentPlayer.changeResourcesTotal(-totalResources);
        } else {
            int bugAmount = 0;
            if (resources.has("Holz")) {
                bugAmount += resources.get("Holz").getAsInt();
            }

            if (resources.has("Lehm")) {
                bugAmount += resources.get("Lehm").getAsInt();
            }

            if (resources.has("Wolle")) {
                bugAmount += resources.get("Wolle").getAsInt();
            }

            if (resources.has("Getreide")) {
                bugAmount += resources.get("Getreide").getAsInt();
            }

            if (resources.has("Erz")) {
                bugAmount += resources.get("Erz").getAsInt();
            }
            int amount = 0;
            if (resources.has("Unbekannt")) {
                amount = resources.get("Unbekannt").getAsInt();
            }
            // increase/decrease quantity of resources of other player
            controller.getPlayerById(id).changeResourcesTotal(-amount);
            controller.getPlayerById(id).changeResourcesTotal(-bugAmount);
        }
        Platform.runLater(() -> {
            controller.getBoardViewController().setResourceNum();
            controller.getBoardViewController().updatePlayerInfoLabels();
            controller.getBoardViewController().checkButtonEnabled();
        });
    }

    /**
     * Robber was moved, sends out Object with Player, new Location and robbed Player
     * @param inputFromServer inputFromServer
     */
    private void handleRobberMoved(JsonObject inputFromServer){
        int playerID = inputFromServer.get("Räuber versetzt").getAsJsonObject().get("Spieler").getAsInt();
        //String location = inputFromServer.get("Räuber versetzt").getAsJsonObject().get("Ort").getAsString();
        JsonObject location = inputFromServer.get("Räuber versetzt").getAsJsonObject().get("Ort").getAsJsonObject();
        Utility.Pair pair = NetworkTranslator.translateLandpieceCoordinateFromProtocol(location);
        controller.getBoard().changeRobber(pair);
        Platform.runLater(() -> controller.getBoardViewController().setRobber());
        for(Player player : controller.getPlayers()){
            if (player.getState().equals("Karten wegen Räuber abgeben")){
                hasToWait = true;
            }
        }
        if (playerID == id && hasToWait){
            Platform.runLater(() -> controller.getBoardViewController().waitingWhileDroppingCardsPopup());
        }
    }

    /**
     *Server sends out a Message, that a Player has won
     * @param inputFromServer inputFromServer
     */
    private void handleGameFinished(JsonObject inputFromServer){
        String message = inputFromServer.get("Spiel beendet").getAsJsonObject().get("Nachricht").getAsString();
        int winnerId = -1;
        if (inputFromServer.get("Spiel beendet").getAsJsonObject().has("Sieger")) {
            winnerId = inputFromServer.get("Spiel beendet").getAsJsonObject().get("Sieger").getAsInt();
        }
        if (controller.getBoardViewController() != null) {
            controller.getBoardViewController().sentMessage(message);
        }
        if (this.id == winnerId){
            Platform.runLater(() -> controller.getBoardViewController().winnerPopup(true, null));
        } else {
            int finalWinnerId = winnerId;
            Platform.runLater(() -> controller.getBoardViewController().winnerPopup(false, controller.getPlayerById(finalWinnerId).getName()));
        }
        System.out.println(winnerId + "has won");
    }

    /**
     * (12.1)
     * TradeOffer is send out from Server
     * @param inputFromServer inputFromServer
     */
    private void handleTradeOffer(JsonObject inputFromServer){
        logger.info(inputFromServer.toString());
        int playerId = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Spieler").getAsInt();
        int tradeId = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Handel id").getAsInt();
        this.currentTradeId = tradeId;
        this.idOfTradingPlayer = playerId;
        JsonObject offer = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Angebot").getAsJsonObject();
        JsonObject request = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Nachfrage").getAsJsonObject();
        Resource[] offeredResources = NetworkTranslator.translateResourceObject(offer);
        Resource[] requestedResources = NetworkTranslator.translateResourceObject(request);
        if (playerId != id) {
            boolean canTrade = true;
            for (int i = 0; i < currentPlayer.getResources().length; i++) {
                if (currentPlayer.getResources()[i].getValue() < requestedResources[i].getValue()) {
                    canTrade = false;
                }
            }
            boolean finalCanTrade = canTrade;
            Platform.runLater(() -> controller.getBoardViewController().showOffer(offeredResources, requestedResources, playerId, finalCanTrade));
        }
    }

    /**
     * (12.2)
     * Handles the message from the server, that a trade was accepted
     * @param inputFromServer inputFromServer
     */
    private void handleTradeAccepted(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        int playerId = inputFromServer.get("Handelsangebot angenommen").getAsJsonObject().get("Mitspieler").getAsInt();
        int tradeId = inputFromServer.get("Handelsangebot angenommen").getAsJsonObject().get("Handel id").getAsInt();
        boolean accepted = inputFromServer.get("Handelsangebot angenommen").getAsJsonObject().get("Annehmen").getAsBoolean();
        if (playerId != id) {
            playersAnsweredToTrade++;
            if (accepted) {
                currentAcceptedPlayers.add(playerId);
            }
        }
        if (playersAnsweredToTrade == controller.getPlayers().size() - 1 && playerId != id && isTradingPlayer) {
            Platform.runLater(() -> controller.getBoardViewController().showOfferAccepted(currentAcceptedPlayers));
        }

    }

    /**
     * (12.3)
     * Handles message from Server when a trade was successful
     * @param inputFromServer inputFromServer
     */
    private void handleTradeFinish(JsonObject inputFromServer){
        logger.info(inputFromServer.toString());
        int player = inputFromServer.get("Handel ausgeführt").getAsJsonObject().get("Spieler").getAsInt();
        int otherPlayer = inputFromServer.get("Handel ausgeführt").getAsJsonObject().get("Mitspieler").getAsInt();
        //reset variables for new trade
        resetTradeVariables();
        Platform.runLater(() -> controller.getBoardViewController().closeRunningPopup());
        controller.getBoardViewController().checkButtonEnabled();
    }

    /**
     * (12.4)
     * Handles the message form the server, that the trade was canceled
     * @param inputFromServer inputFromServer
     */
    private void handleTradeCancel(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        int playerId = inputFromServer.get("Handelsangebot abgebrochen").getAsJsonObject().get("Spieler").getAsInt();
        int tradeId = inputFromServer.get("Handelsangebot abgebrochen").getAsJsonObject().get("Handel id").getAsInt();
        if (idOfTradingPlayer == playerId && playerId != id){
            Platform.runLater(() -> controller.getBoardViewController().closeRunningPopup());
        }
        if(idOfTradingPlayer == playerId){
            resetTradeVariables();
        }
        if (isTradingPlayer && playerId != id){
            Iterator<Integer> iter = currentAcceptedPlayers.iterator();
            while (iter.hasNext()) {
                Integer i = iter.next();
                if (i == playerId) {
                    iter.remove();
                }
            }
            Platform.runLater(() -> {
                controller.getBoardViewController().hideAcceptedTradePopup();
                logger.info("popup trade canceld");
                controller.getBoardViewController().showOfferAccepted(currentAcceptedPlayers);
            });
        }

    }

    /**
     * Handles the message from the server that a player bought a dev card
     * @param inputFromServer inputFromServer
     */
    private void handleDevCardBought(JsonObject inputFromServer) {
        int playerID = inputFromServer.get("Entwicklungskarte gekauft").getAsJsonObject().get("Spieler").getAsInt();
        String card = inputFromServer.get("Entwicklungskarte gekauft").getAsJsonObject().get("Entwicklungskarte").getAsString();
        controller.increaseDevCardsBought();
        if (playerID == id) {
            logger.info("Player: " + currentPlayer + " bought DevCard: " + card);
            currentPlayer.addToDevCards(card);
            Platform.runLater(() -> {
                controller.getBoardViewController().developmentCardButton.setDisable(true);
                controller.getBoardViewController().checkButtonEnabled();
            });
            lastCardBought = card;
            devCardBoughtInThisRound = true;
            switch (card) {
                case "Ritter":
                    Platform.runLater(() -> controller.getBoardViewController().setKnightLabel(currentPlayer.getQuantityOfDevCard("Ritter")));
                    System.out.println("RITTER: " + currentPlayer.getQuantityOfDevCard("Ritter"));
                    break;
                case "Straßenbau":
                    Platform.runLater(() -> controller.getBoardViewController().setRoadBuildingLabel(currentPlayer.getQuantityOfDevCard("Straßenbau")));
                    System.out.println("STRASSENBAU: " + currentPlayer.getQuantityOfDevCard("Straßenbau"));
                    break;
                case "Monopol":
                    Platform.runLater(() -> controller.getBoardViewController().setMonopolyLabel(currentPlayer.getQuantityOfDevCard("Monopol")));
                    System.out.println("MONOPOL: " + currentPlayer.getQuantityOfDevCard("Monopol"));
                    break;
                case "Erfindung":
                    Platform.runLater(() -> controller.getBoardViewController().setYearOfPlentyLabel(currentPlayer.getQuantityOfDevCard("Erfindung")));
                    System.out.println("ERFINDUNG: " + currentPlayer.getQuantityOfDevCard("Erfindung"));
                    break;
                case "Siegpunkt":
                    currentPlayer.increaseVictoryPoints();
                    Platform.runLater(() -> {
                        controller.getBoardViewController().setVictoryPointLabel(currentPlayer.getQuantityOfDevCard("Siegpunkt"));
                        controller.getBoardViewController().updatePlayerInfoLabels();
                    });
                    System.out.println("SIEGPUNKT: " + currentPlayer.getQuantityOfDevCard("Siegpunkt"));
                    break;
            }
        } else {
            controller.getPlayerById(playerID).addToDevCards(card);
            Platform.runLater(() ->controller.getBoardViewController().updatePlayerInfoLabels());
        }
    }

    /**
     * Handles the message that a player played a knight card
     * @param inputFromServer inputFromServer
     */
    private void handleKnightCardPlayed(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        JsonObject location = inputFromServer.get("Ritter ausspielen").getAsJsonObject().get("Ort").getAsJsonObject();
        int playerID = inputFromServer.get("Ritter ausspielen").getAsJsonObject().get("Spieler").getAsInt();
        Utility.Pair pair = NetworkTranslator.translateLandpieceCoordinateFromProtocol(location);
        controller.getBoard().changeRobber(pair);

        if (playerID == id) {
            currentPlayer.removeDevCard("Ritter");
            Platform.runLater(() -> {
                controller.getBoardViewController().setKnightLabel(currentPlayer.getQuantityOfDevCard("Ritter"));
                System.out.println("HIER MÜSSTE ICH DEN BUTTON DISABLEN");
                controller.getBoardViewController().myDevCardsButton.setDisable(true);
                System.out.println("PASSIERT?");
            });
        } else {
            controller.getPlayerById(playerID).removeDevCard("Unbekannt");
        }

        controller.getPlayerById(playerID).increaseKnightForce();
        logger.info("increase knight force works");

        Platform.runLater(() -> {
            controller.getBoardViewController().setRobber();
            logger.info("robber set");
            controller.getBoardViewController().updatePlayerInfoLabels();
            logger.info("player info labels updated");
        });
    }

    /**
     * Handles the message from the server that a player played the monopoly card
     * @param inputFromServer inputFromServer
     */
    private void handleMonopolyCardPlayed(JsonObject inputFromServer) {
        int playerID = inputFromServer.get("Monopol").getAsJsonObject().get("Spieler").getAsInt();
        if (playerID == id) {
            currentPlayer.removeDevCard("Monopol");
            Platform.runLater(() -> controller.getBoardViewController().setMonopolyLabel(currentPlayer.getQuantityOfDevCard("Monopol")));
        } else {
            controller.getPlayerById(id).removeDevCard("Unbekannt");
        }
    }

    /**
     * Handles the message from the server that a player played the year of plenty card
     * @param inputFromServer inputFromServer
     */
    private void handleYearOfPlentyCardPlayed(JsonObject inputFromServer) {
        int playerID = inputFromServer.get("Erfindung").getAsJsonObject().get("Spieler").getAsInt();
        if (playerID == id) {
            currentPlayer.removeDevCard("Erfindung");
            Platform.runLater(() -> controller.getBoardViewController().setYearOfPlentyLabel(currentPlayer.getQuantityOfDevCard("Erfindung")));
        } else {
            controller.getPlayerById(id).removeDevCard("Unbekannt");
        }
    }

    /**
     * Handles the message from the server that a player got the longest handle road
     * if it is the current player it increases two victory points
     * if it is not the current player and the current player had it before it decreases
     * @param inputFromServer inputFromServer
     */
    private void handleLongestTradeRoad(JsonObject inputFromServer) {
        if (inputFromServer.get("Längste Handelsstraße").getAsJsonObject().has("Spieler")) {
            int playerId = inputFromServer.get("Längste Handelsstraße").getAsJsonObject().get("Spieler").getAsInt();
            if (playerId == id) {
                if (!wasHoldingLongestRoad) {
                    currentPlayer.increaseVictoryPoints();
                    currentPlayer.increaseVictoryPoints();
                    wasHoldingLongestRoad = true;
                }
                Platform.runLater(()-> {
                    controller.getBoardViewController().setBadges(false, playerId);
                    controller.getBoardViewController().showPlayerHasLongestTradeRoadOrBiggestKnightForce(true);
                });
            } else {
                if (wasHoldingLongestRoad) {
                    currentPlayer.decreaseVictoryPoints();
                    currentPlayer.decreaseVictoryPoints();
                    wasHoldingLongestRoad = false;
                }
                Platform.runLater(()-> controller.getBoardViewController().setBadges(false, playerId));
            }
            Platform.runLater(() -> controller.getBoardViewController().updatePlayerInfoLabels());
        } else {
            if (wasHoldingLongestRoad) {
                currentPlayer.decreaseVictoryPoints();
                currentPlayer.decreaseVictoryPoints();
                wasHoldingLongestRoad = false;
            }
            Platform.runLater(()-> controller.getBoardViewController().disableAllTradeRoadBadges());
        }
    }

    /**
     * Handles the message from the server that a player has the biggest knight force
     * if it is the current player it increases two victory points
     * if it is not the current player and the current player had it before it decreases
     * @param inputFromServer inputFromServer
     */
    private void handleBiggestKnightForce(JsonObject inputFromServer) {
        if(inputFromServer.get("Größte Rittermacht").getAsJsonObject().has("Spieler")){
            int playerId = inputFromServer.get("Größte Rittermacht").getAsJsonObject().get("Spieler").getAsInt();
            if (playerId == id) {
                if (!wasBiggestKnightForce) {
                    currentPlayer.increaseVictoryPoints();
                    currentPlayer.increaseVictoryPoints();
                    wasBiggestKnightForce = true;
                }
                Platform.runLater(() -> {
                    controller.getBoardViewController().showPlayerHasLongestTradeRoadOrBiggestKnightForce(false);
                    controller.getBoardViewController().updatePlayerInfoLabels();
                    controller.getBoardViewController().setBadges(true, playerId);
                }
                );
            } else {
                if (wasBiggestKnightForce) {
                    wasBiggestKnightForce = false;
                }
                Platform.runLater(()-> controller.getBoardViewController().setBadges(true, playerId));
            }
        }
    }

    /**
     * Handles the message from the server that a player played a road building card
     * @param inputFromServer
     */
    private void handleRoadBuildCardPlayed(JsonObject inputFromServer) {
        int playerID = inputFromServer.get("Straßenbaukarte ausspielen").getAsJsonObject().get("Spieler").getAsInt();
        if (playerID == id) {
            currentPlayer.removeDevCard("Straßenbau");
            Platform.runLater(() -> controller.getBoardViewController().setRoadBuildingLabel(currentPlayer.getQuantityOfDevCard("Straßenbau")));
        } else {
            controller.getPlayerById(id).removeDevCard("Unbekannt");
        }
    }

    /**
     * CHEAT
     * Handles the message from the server that the current player got a victory point
     */
    private void handleVictoryPoint() {
        currentPlayer.increaseVictoryPoints();
        Platform.runLater(() -> controller.getBoardViewController().updatePlayerInfoLabels());
    }

    private void handleColorChange() {
        currentPlayer.setColor(Color.BLACK);
        Platform.runLater(() -> {
            controller.getBoardViewController().updateColor();
            controller.getBoardViewController().setStyleClassForCheat();
        });
    }

    /**
     * sends Hello to Server
     */
    private void sendHello() {
        try {
            System.out.println("OUT: " + protocol.startConnectionToServer("JavaFXClient " + "1.0" + " (InfraroteHacks)").toString());
            logger.info("OUT: " + protocol.startConnectionToServer("JavaFXClient " + "1.0" + " (InfraroteHacks)").toString());
            writer.write(protocol.startConnectionToServer("JavaFXClient " + "1.0" + " (InfraroteHacks)").toString()+ newLine);
            writer.flush();
        } catch (Exception e) {
            System.out.println("error in send hello");
            logger.log(Level.SEVERE, "error in send hello");
        }
    }

    /**
     * Sends the player info to the Server
     * @param name name that the player chose
     * @param color color that the player chose
     * @return boolean
     */
    public boolean sendPlayerInfo(String name, String color) {
        try {
            System.out.println("OUT: " + protocol.sendPlayerInfoToServer(name, color).toString());
            logger.info("OUT: " + protocol.sendPlayerInfoToServer(name, color).toString());
            writer.write(protocol.sendPlayerInfoToServer(name, color).toString() + newLine);
            writer.flush();
            return true;
        } catch (Exception e) {
            System.out.println("error in send player info");
            //logger.log(Level.SEVERE, "error in send player info");

            return false;
        }
    }

    /**
     * Sends the message that the player is ready
     */
    public void sendImReady() {
        try {
            System.out.println("OUT: " + protocol.playerIsReadyToServer().toString());
            logger.info("OUT: " + protocol.playerIsReadyToServer().toString());
            writer.write(protocol.playerIsReadyToServer().toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Just sends the message that the Client wants to throw the dice
     */
    public void sendThrowDice() {
        try {
            System.out.println("OUT: " + protocol.throwDice().toString());
            logger.info("OUT: " + protocol.throwDice().toString());
            writer.write(protocol.throwDice().toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            System.out.println("error in sendThrowDice");
            logger.log(Level.SEVERE, "error in sendThrowDice");

        }
    }

    /**
     * Sends a build request with type of building and location to the Server
     * @param type type of the building (settlement, city, street)
     * @param location location of the building
     */
    public void sendBuildRequest(String type, JsonArray location) {
        try {
            //JsonArray type, String location
            System.out.println("OUT: " + protocol.build(type, location).toString());
            logger.info("OUT: " + protocol.build(type, location).toString());
            writer.write(protocol.build(type, location).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            System.out.println("error in sendBuildRequest");
            logger.log(Level.SEVERE, "error in sendBuildRequest");

        }

    }

    /**
     * This method is finishing the current Move
     */
    public void sendFinishMove() {
        try {
            Platform.runLater(() -> controller.getBoardViewController().resetSettlementCityAndStreetButton());
            devCardBoughtInThisRound = false;
            lastCardBought = "";
            System.out.println("OUT: " + protocol.finishMove().toString());
            logger.info("OUT: " + protocol.finishMove().toString());
            writer.write(protocol.finishMove().toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            System.out.println("error in sendFinishMove");
            logger.log(Level.SEVERE, "error in sendFinishMove");

        }
    }

    /**
     * Sends the chat message from the user to the Server
     * @param message message the user wants to send to other players
     */
    public void sendChatMessage(String message) {
        try {
            System.out.println("OUT: " + protocol.sendChatMessageToServer(message));
            logger.info("OUT: " + protocol.sendChatMessageToServer(message));
            writer.write(protocol.sendChatMessageToServer(message).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * (11.2)
     * Sends the message with resources to drop to the Server
     * @param resourcesFromClient resources that the client chose to drop
     */
    public void sendResourceDrop(Resource[] resourcesFromClient) {
        try {
            JsonObject resources = NetworkTranslator.translateResourceObject(resourcesFromClient);
            System.out.println("OUT: " + protocol.submitResourcesToServer(resources));
            logger.info("OUT: " + protocol.submitResourcesToServer(resources));
            writer.write(protocol.submitResourcesToServer(resources).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * (11.5)
     * @param offer resources the user wants to give to the bank
     * @param request resources the user wants to get
     */
    public void sendHarborTrade(JsonObject offer, JsonObject request) {
        try {
            System.out.println("OUT: " + protocol.offerHarborTradeToServer(offer, request));
            logger.info("OUT: " + protocol.offerHarborTradeToServer(offer, request));
            writer.write(protocol.offerHarborTradeToServer(offer, request).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * (11.1)
     * @param offerFromClient resources the user offers other clients
     * @param requestFromClient resources the user wants to get from the other clients
     */
    public void sendDomesticTrade(Resource[] offerFromClient, Resource[] requestFromClient) {
        try {
            JsonObject offer = NetworkTranslator.translateResourceObject(offerFromClient);
            JsonObject request = NetworkTranslator.translateResourceObject(requestFromClient);
            isTradingPlayer = true;
            System.out.println("OUT: " + protocol.offerDomesticTradeToServer(offer, request));
            logger.info("OUT: " + protocol.offerDomesticTradeToServer(offer, request));
            writer.write(protocol.offerDomesticTradeToServer(offer, request).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sends out message to server with new robber location and OPTIONAL with the id of the robbed player
     * @param location new location of the robber
     * @param idRobbedPlayer id of the player the client wants to rob
     */
    public void sendMoveRobber(JsonObject location, int idRobbedPlayer){
        try {
            System.out.println("OUT: "+ protocol.moveRobberToServer(location, idRobbedPlayer).toString());
            logger.info("OUT: " + protocol.moveRobberToServer(location, idRobbedPlayer).toString());
            writer.write(protocol.moveRobberToServer(location, idRobbedPlayer).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Message the client sends if the trade will be accepted
     */
    public void sendAcceptOffer(boolean accept){
        try {
            System.out.println("OUT: "+ protocol.acceptOfferToServer(currentTradeId, accept));
            logger.info("OUT: "+ protocol.acceptOfferToServer(currentTradeId, accept));
            writer.write(protocol.acceptOfferToServer(currentTradeId, accept).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * (12.3)
     * Sends the trade execution to the server with currentTradeId + playerId of the player the current player trades with
     * @param playerId id of the player the current player trades with
     */
    public void sendTradeExecution( int playerId) {
        try {
            int tradeId = currentTradeId;
            System.out.println("OUT: " + protocol.finishTradeToServer(tradeId, playerId));
            logger.info("OUT: " + protocol.finishTradeToServer(tradeId, playerId));
            writer.write(protocol.finishTradeToServer(tradeId, playerId).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * (12.4)
     * Sends message to the server that the current player wants to cancel the trade
     */
    public void sendTradeCancel() {
        try {
            System.out.println("OUT: " + protocol.cancelTradeToServer(currentTradeId));
            logger.info("OUT: " + protocol.cancelTradeToServer(currentTradeId));
            writer.write(protocol.cancelTradeToServer(currentTradeId).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server that the client wants to buy a development card
     */
    public void sendBuyDevCard() {
        try {
            System.out.println("OUT: " + protocol.buyDevelopmentCard());
            logger.info("OUT: " + protocol.buyDevelopmentCard());
            writer.write(protocol.buyDevelopmentCard().toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server that the client wants to play its knight card
     * @param location where the robber will be placed
     * @param targetId id of the player that gets robbed
     */
    public void sendPlayKnightCard(JsonObject location, int targetId) {
        try {
            System.out.println("OUT: " + protocol.playKnightCardToServer(location, targetId));
            logger.info("OUT: " + protocol.playKnightCardToServer(location, targetId));
            writer.write(protocol.playKnightCardToServer(location, targetId).toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server that the client wants to play a monopoly card
     * @param resource resource the player wants to have from other players
     */
    public void sendPlayMonopolyCard(String resource) {
        try {
            System.out.println("OUT: " + protocol.playMonopolCardToServer(resource));
            logger.info("OUT: " + protocol.playMonopolCardToServer(resource));
            writer.write(protocol.playMonopolCardToServer(resource).toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server that the client wants to play a year of plenty card
     * @param resources resources that the player wants
     */
    public void sendPlayYearOfPlentyCard(JsonObject resources) {
        try {
            System.out.println("OUT: " + protocol.playYearOfPlentyCardToServer(resources));
            logger.info("OUT: " + protocol.playYearOfPlentyCardToServer(resources));
            writer.write(protocol.playYearOfPlentyCardToServer(resources).toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the server that the client wants to play a roadbuilding card
     * @param road1 first road the player wants to build
     * @param road2 second road the player wants to build (can be null, if he only can build one)
     */
    public void sendRoadBuildingCard(JsonArray road1, JsonArray road2) {
        try {
            if (road2 != null) {
                System.out.println("OUT: " + protocol.playBuildStreetCardToServer(road1, road2));
                logger.info("OUT: " + protocol.playBuildStreetCardToServer(road1, road2));
                writer.write(protocol.playBuildStreetCardToServer(road1, road2).toString() + newLine);
                writer.flush();
            } else {
                System.out.println("OUT: " + protocol.playBuildStreetCardToServer(road1, null));
                logger.info("OUT: " + protocol.playBuildStreetCardToServer(road1, null));
                writer.write(protocol.playBuildStreetCardToServer(road1, null).toString() + newLine);
                writer.flush();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    public void sendBeginnerMode() {
        JsonObject response = new JsonObject();
        response.add("Anfängermodus", new JsonObject());
        try {
            writer.write(response.toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Sets the controller
     * @param controller InfraroterServer.Controller
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * gets the server as string to give it to the ai
     */
    public String getServer(){
        return server;
    }

    /**
     * returns the current trade id
     * @return
     */
    public int getCurrentTradeId(){
        return currentTradeId;
    }
    /**
     * closes the socket
     */
    public void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the socketClosed boolean
     * @param socketClosed tells the client that it shouldn't look for new messages from server
     */
    public void setSocketClosed(boolean socketClosed) {
        this.socketClosed = socketClosed;
    }

    public void resetTradeVariables(){
        currentAcceptedPlayers = new ArrayList<>();
        playersAnsweredToTrade = 0;
        isTradingPlayer = false;
        idOfTradingPlayer = 0;
    }

    public boolean isDevCardBoughtInThisRound() {
        return devCardBoughtInThisRound;
    }

    public String getLastCardBought() {
        return lastCardBought;
    }
}
