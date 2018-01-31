package ai;

import LOG.Logging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import model.*;
import network.NetworkTranslator;
import network.Protocol10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Handles the communication with the server
 * @author Sandra
 */
public class AINetworkController implements Runnable{

    private OutputStreamWriter writer;
    private JsonParser parser = new JsonParser();
    private int id;
    private Socket clientSocket;
    private Player currentPlayer;
    private boolean socketClosed;

    private String protocolVersion = "1.0";
    private String[] names = new String[]{"Horst", "LaserLutz", "Bötchen", "Depp", "HipsterWal", "ClubMate", "Bärbel",
        "Pippi Langstrumpf", "Robinson Cruso", "Wilson", "Pacman", "SuperMario", "Sonic", "NinjaTurtle", "Marvin42", "Guybrush",
        "Pummel Einhorn", "Garfield", "Gamon", "Das Känguru", "Karam", "Inge", "Flow", "Geralt",
        "flying Cake", "Kuschelmonster", "Rufus", "Walter White", "A Bot has no name", "Hodor", "Ygritte", "Gott", "Vincent", "Jules",
        "Donald Duck", "Rubber", "Homer Simpson", "Wade Winston Wilson", "Peter Parker", "Hans", "Clark Kent",
        "Marie Curie", "Alan Turing", "Bill Gates", "Steve Jobs", "Larry Page",
        "Linus Torvalds", "Konrad Zuse", "Steve Wozniak", "Ada Lovelace", "Einstein" };
    private String[] dirtyNames = new String[]{"Depp", "Idiot", "Gscheidschmatzer", "Dumpfbacke", "Dieb", "siehst komisch aus", "kannst gar nichts", "bist doof", "Leberwurst", "bist nicht cool", "bist vong Niceigkeit her eher unten", "bist 1 Leberwurst", "Loser", "komischer Kautz", "Pappnase", "Gesichtspommes", "Blödfisch", "Doppeldepp"};
    private AiController controller;
    private Protocol10 protocol = new Protocol10();
    private Logger logger;
    private String newLine = "\n";
    private ArrayList<String> colors = new ArrayList<>(Arrays.asList("Blau", "Orange", "Rot", "Weiß"));
    private String server;
    private String serverIP;
    private int currentTradeId = 0;
    private int playersAnsweredToTrade = 0;
    private ArrayList<Integer> currentAcceptedPlayers = new ArrayList<>();
    private boolean isTradingPlayer = false;
    private int idOfTradingPlayer = 0;
    private boolean firstHandelnOderBauen = true;
    private boolean sendResourcesFlag = false;
    private boolean finishedFlag = false;

    /**
     * constructor
     * gets the server as a input parameter
     */
    public AINetworkController(String server, String serverIP, Logger logger){
        this.logger = logger;
        this.server = server;
        this.serverIP = serverIP;
        logger.info(server + " || " + serverIP);
    }

    /**
     * generates a name for the AI
     * @return a random name from the names ArrayList
     */
    private String kiName(){return names[(int) (names.length * new Random().nextDouble())] + " (Bot)";}


    private String dirtyName(){return dirtyNames[(int) (dirtyNames.length * new Random().nextDouble())];}

    /**
     * starts the clientController
     * called when thread is started
     */
    @Override
    public void run() {
        logger.info("run");
        logger.info(Thread.currentThread().toString());
        try {
            startClientController(server, serverIP);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            logger.info("system.exit: 1");
            System.exit(1);
        }
    }

    /**
     * Starts the connection to the server and initializes the OutputStreamWriter to give messages to the server and the BufferedReader to read messages from the Server
     * @param server the server to connect to
     * @throws Exception errors from the server connection
     */
    private void startClientController(String server, String ip) throws Exception {

        logger.info("startClientController with: " + server + " | " + ip);
        switch (server) {
            case "TestServer":
                clientSocket = new Socket("aruba.dbs.ifi.lmu.de", 10003);
                break;
            case "Nicer Server":
                clientSocket = new Socket(ip, 6789);
        }

        writer = new OutputStreamWriter(clientSocket.getOutputStream(), "UTF8");
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF8"));

        while (!socketClosed) {
            String inputFromServer = reader.readLine();
            if (inputFromServer != null) {
                handleInputFromServer(parser.parse(inputFromServer).getAsJsonObject()); // converts string to jsonObject
            } else {
                clientSocket.close();
                socketClosed = true;
                System.exit(1);
            }
        }

    }

    /**
     * Checks the first key of the Json-Object the Client received from the server and calls the appropriate method
     * @param inputFromServer the message from the server
     */
    private void handleInputFromServer(JsonObject inputFromServer) throws Exception {

        List<String> keys = inputFromServer.entrySet()
                .stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        switch (keys.get(0)) {
            case "Hallo":
                handleHello(inputFromServer);
                break;
            case "Entwicklungskarte gekauft":
                handleDevCardBought(inputFromServer);
                break;
            case "Willkommen":
                handleWelcome(inputFromServer);
                break;
            case "Erfindung":
                handleYearOfPlentyCardPlayed(inputFromServer);
                break;
            case "Straßenbaukarte ausspielen":
                handleRoadBuildCardPlayed(inputFromServer);
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
               // handleDiceThrow(inputFromServer);
                logger.info("Dice Number: " + inputFromServer.get("Würfelwurf").getAsJsonObject().toString());
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
                //do nothing
                break;
            case "Ritter ausspielen":
                handleKnightCardPlayed(inputFromServer);
                break;
            case "Monopol":
                handleMonopolyCardPlayed(inputFromServer);
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
            case "Verbindung verloren":
                logger.info("connection lost, program exit");
                System.exit(0);
        }
    }

    /**
     * checks if the protocol version matches on server and client
     * @param inputFromServer the message from the server
     */
    private void handleHello(JsonObject inputFromServer) {
        try {
            JsonObject nestedInput = inputFromServer.get("Hallo").getAsJsonObject();
            logger.info(nestedInput.toString());
            if (nestedInput.get("Protokoll").getAsString().equals(protocolVersion)) {
                sendHello();
            } else {
                socketClosed = true;
                clientSocket.close();
                System.exit(1);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * saves the id the client gets from the server
     * sends the name and color to the server
     * @param inputFromServer the message from the server
     */
    private void handleWelcome(JsonObject inputFromServer) {
        JsonObject nestedInput = inputFromServer.get("Willkommen").getAsJsonObject();
        id = nestedInput.get("id").getAsInt();

        sendPlayerInfo(kiName(), colors.get((int) (colors.size() * new Random().nextDouble())));
        sendImReady();
    }

    /**
     * if server sends color not valid, than sends new name and new color
     * checks if the action the client sent to the server was valid
     * @return boolean true = valid
     */
    private boolean handleActionValidation(JsonObject inputFromServer) {
        logger.info(inputFromServer.get("Serverantwort").getAsString());
        if(inputFromServer.get("Serverantwort").getAsString().equals("Farbe bereits vergeben")){
            sendPlayerInfo(kiName(), colors.get((int) (colors.size() * new Random().nextDouble())));
            sendImReady();
        }
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
        if (chatMessage.length() > 1 && chatMessage.substring(0,2).equals("/*")) {
            String cleanCheat = chatMessage.substring(2).replaceAll("\\s+","").toLowerCase();
            handleCheats(cleanCheat);
        }

    }

    void handleCheats(String cheat){
        logger.info("dein cheat: " + cheat);
        if(cheat.equals("showyourcards")){
            logger.info("you cheater");
            sendResourcesFlag = true;
        }

    }

    /**
     * (7)
     * Server sends out an error, since the chosen color was already taken
     *
     * @param inputFromServer the message from the server
     */
    private void handleError(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        String errorMessage = inputFromServer.get("Fehler").getAsJsonObject().get("Meldung").getAsString();
        if(errorMessage.equals("Farbe bereits vergeben")){
            try{
                for(Player pl : controller.getPlayers()){
                    logger.info(pl.toExtendedString());
                }
            }catch (Exception e){
                logger.info("controller.getPlayers doesn't work");
            }
            sendPlayerInfo(kiName(), colors.get((int) (colors.size() * new Random().nextDouble())));
            sendImReady();
        }
    }

    /**
     * (7)
     * all of the players started, so the Server sends out the board
     * gives the board to the controller
     * @param inputFromServer the message from the server
     */
    private void handleGameStarted(JsonObject inputFromServer) throws Exception {
        try {
            JsonObject map = inputFromServer.get("Spiel gestartet").getAsJsonObject().get("Karte").getAsJsonObject();
            Board boardFromServer = NetworkTranslator.translateMapToStart(map);
            controller.setBoard(boardFromServer);
        }catch (Exception e){
            logger.log(Level.SEVERE, "board not initialized");
        }
    }

    /**
     * Calls the methods that are needed when the server changed the status of a player
     * @param inputFromServer the message from the server
     * @throws Exception error from handling the status update
     */
    private void handleStatusUpdate(JsonObject inputFromServer) throws Exception {
        logger.info(inputFromServer.toString());
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

        String status = playerObject.get("Status").getAsString();
       // int victoryPoints = playerObject.get("Siegpunkte").getAsInt();
       // JsonObject resources = playerObject.get("Rohstoffe").getAsJsonObject();

        /*JsonObject developmentCards = new JsonObject();
        if(playerObject.has("Entwicklungskarten")) {
            if(!playerObject.get("Entwicklungskarten").isJsonNull()) {
                developmentCards = playerObject.get("Entwicklungskarten").getAsJsonObject();
            }
        }*/

        controller.changeStateOfPlayer(id, status);

        switch (status) {
            case "Spiel starten":
                break;
            case "Wartet auf Spielbeginn":
                if (this.id != id) {
                    controller.addPlayer(NetworkTranslator.translatePlayer(name, id, color));
                } else {
                    currentPlayer = NetworkTranslator.translatePlayer(name, id, color);
                    controller.addPlayer(currentPlayer);
                    controller.setCurrentPlayer(currentPlayer);
                }
                break;
            case "Dorf bauen":
            	 if(this.id == id){
                     controller.choseSettlement();
                 }
                break;
            case "Straße bauen":
                if(this.id == id){
                    controller.chooseStreet(true);
                }
                break;
            case "Würfeln":
                if(this.id == id){
                    firstHandelnOderBauen = true;
                    finishedFlag = false;
                    sendThrowDice();
                }
                break;
            case "Räuber versetzen":
                if(this.id == id){
                    controller.placeRobber(false);
                }
                break;
            case "Handeln oder Bauen":
                if (this.id == id && !controller.isWaitingForResourceInput() && !finishedFlag) {
                    logger.info("firstHoB: " + firstHandelnOderBauen);
                    if(firstHandelnOderBauen) {
                        firstHandelnOderBauen = false;
                        logger.info("reset first action");
                        if(sendResourcesFlag) {
                            sendChatMessage(currentPlayer.getResourcesString());
                            for(String devCard : currentPlayer.getDevCards()){
                                sendChatMessage("Player: " + currentPlayer + " ; DevCard: " + devCard);
                            }
                        }
                        controller.resetFirstAction();
                        controller.increaseRoundCounter();
                        controller.handleRound();
                }
                    break;
            }
            case "Warten":
                break;
            case "Verbindung verloren":
                System.exit(0);
                break;
            case "Karten wegen Räuber abgeben":
               if(this.id == id){
                   controller.dropResources();
                }
                break;
            default:
                logger.info("case not cached: " + status);
                break;
        }
    }

    /**
     * handles the incoming and outgoing resources of a player
     * @param inputFromServer the message from the server
     */
    private void handleProfit(JsonObject inputFromServer) {
        int id = inputFromServer.get("Ertrag").getAsJsonObject().get("Spieler").getAsInt();
        JsonObject resourceObject = inputFromServer.get("Ertrag").getAsJsonObject().get("Rohstoffe").getAsJsonObject();
        int totalResources = 0;
        if (this.id == id) {
            logger.info("got resources: " + inputFromServer);

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
            if((currentPlayer.getState().equals("Handeln oder Bauen") || currentPlayer.getState().equals("Räuber versetzen"))
                    && controller.isWaitingForResourceInput()){
                controller.gotResources();
                logger.info("was waiting for resources, now: " + controller.isWaitingForResourceInput());
                if(!currentPlayer.getState().equals("Räuber versetzen")){
                    controller.handleRound();
                }
            }

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
    }

    /**
     * handles the message that another player built a street or building
     * and sends it to the controller to update the board
     * (8.4)
     *
     * @param inputFromServer the message from the server
     */
    private void handleNewBuilding(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        JsonObject building = inputFromServer.get("Bauvorgang").getAsJsonObject().get("Gebäude").getAsJsonObject();
        JsonArray location = building.get("Ort").getAsJsonArray();
        int owner = building.get("Eigentümer").getAsInt();
        logger.info(building + " at " + location);
        switch (building.get("Typ").getAsString()) {
            case "Straße":
                // translate location of street and build it in controller
            	Utility.Pair[] pairs = NetworkTranslator.translateCoordinateToIntersections(location);
            	controller.getBoard().addStreet(pairs[0], pairs[1], controller.getPlayerById(owner));
                break;
            case "Dorf":
                if (owner == id) {
                    currentPlayer.increaseVictoryPoints();
                }
                // translate location of settlement and build it in controller
                controller.getBoard().addSettlement(NetworkTranslator.translateIntersectionCoordinate(location), controller.getPlayerById(owner));
                break;
            case "Stadt":
                if (owner == id) {
                    currentPlayer.increaseVictoryPoints();
                }
                // translate location of city and build it in controller
                controller.getBoard().addCity(NetworkTranslator.translateIntersectionCoordinate(location), controller.getPlayerById(owner));
                break;
        }
    }

    /**
     * Handles the message that the currentPlayer needs to give away some resources
     * @param inputFromServer the message from the server
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

            if(totalResources == 1){
                sendChatMessage("Du " + dirtyName());
            }
        }
        else {
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
            // decrease quantity of resources of other player
            controller.getPlayerById(id).changeResourcesTotal(-amount);
            controller.getPlayerById(id).changeResourcesTotal(-bugAmount);
        }
    }

    /**
     * Robber was moved, sends out Object with Player, new Location and robbed Player
     * @param inputFromServer the message from the server
     */
    private void handleRobberMoved(JsonObject inputFromServer){
        JsonObject location = inputFromServer.get("Räuber versetzt").getAsJsonObject().get("Ort").getAsJsonObject();
        Utility.Pair pair = NetworkTranslator.translateLandpieceCoordinateFromProtocol(location);
        controller.getBoard().changeRobber(pair);
    }

    /**
     * (12.1)
     * TradeOffer is send out from Server
     * @param inputFromServer the message from the server
     */
    private void handleTradeOffer(JsonObject inputFromServer){

        logger.info(inputFromServer.toString());
        int playerId = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Spieler").getAsInt();
        int tradeId = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Handel id").getAsInt();
        this.currentTradeId = tradeId;
        JsonObject offer = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Angebot").getAsJsonObject();
        JsonObject request = inputFromServer.get("Handelsangebot").getAsJsonObject().get("Nachfrage").getAsJsonObject();
        Resource[] offeredResources = NetworkTranslator.translateResourceObject(offer);
        Resource[] requestedResources = NetworkTranslator.translateResourceObject(request);
        if(playerId != id) {
            controller.answerTradeOffer(offeredResources, requestedResources, playerId);
            resetTradeVariables();
        }
    }

    /**
     * (12.2)
     * Handles the message from the server, that a trade was accepted
     * @param inputFromServer the message from the server
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

            logger.info("answered players: " + playersAnsweredToTrade);
            logger.info("accepted: " + currentAcceptedPlayers.size());
            logger.info("players: " + controller.getPlayers().size());
            if (playersAnsweredToTrade == controller.getPlayers().size() - 1) {
                if (currentAcceptedPlayers.size() > 0) {
                    controller.choosePlayerToAcceptOffer(currentAcceptedPlayers);
                } else {
                    logger.info("no trade accepted players, check sea trade");
                    if (!controller.checkHarborTrade()) {
                        logger.info("no harbor Trade");
                        sendFinishMove();
                    }
                }
            }
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
        if(player == currentPlayer.getId()){
        }
    }


    /**
     * (12.4)
     * Handles the message form the server, that the trade was canceled
     * @param inputFromServer the message from the server
     */
    private void handleTradeCancel(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        int playerId = inputFromServer.get("Handelsangebot abgebrochen").getAsJsonObject().get("Spieler").getAsInt();
        int tradeId = inputFromServer.get("Handelsangebot abgebrochen").getAsJsonObject().get("Handel id").getAsInt();
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
        }
    }

    private void handleDevCardBought(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        int playerID = inputFromServer.get("Entwicklungskarte gekauft").getAsJsonObject().get("Spieler").getAsInt();
        String card = inputFromServer.get("Entwicklungskarte gekauft").getAsJsonObject().get("Entwicklungskarte").getAsString();
        controller.increaseDevCardsBought();
        if (playerID == id) {
            currentPlayer.addToDevCards(card);
            switch (card) {
                case "Ritter":
                    System.out.println("RITTER: " + currentPlayer.getQuantityOfDevCard("Ritter"));
                    break;
                case "Straßenbau":
                    System.out.println("STRASSENBAU: " + currentPlayer.getQuantityOfDevCard("Straßenbau"));
                    break;
                case "Monopol":
                    System.out.println("MONOPOL: " + currentPlayer.getQuantityOfDevCard("Monopol"));
                    break;
                case "Erfindung":
                    System.out.println("ERFINDUNG: " + currentPlayer.getQuantityOfDevCard("Erfindung"));
                    break;
                case "Siegpunkt":
                    currentPlayer.increaseVictoryPoints();
                    System.out.println("SIEGPUNKT: " + currentPlayer.getQuantityOfDevCard("Siegpunkt"));
                    break;
            }
        }
    }

    private void handleKnightCardPlayed(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        JsonObject location = inputFromServer.get("Ritter ausspielen").getAsJsonObject().get("Ort").getAsJsonObject();
        int playerID = inputFromServer.get("Ritter ausspielen").getAsJsonObject().get("Spieler").getAsInt();
        Utility.Pair pair = NetworkTranslator.translateLandpieceCoordinateFromProtocol(location);
        controller.getBoard().changeRobber(pair);

        if (playerID == id) {
            currentPlayer.removeDevCard("Ritter");
        }

        controller.getPlayerById(playerID).increaseKnightForce();
        logger.info("increase knight force works");
    }

    private void handleMonopolyCardPlayed(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        int playerID = inputFromServer.get("Monopol").getAsJsonObject().get("Spieler").getAsInt();
        if (playerID == id) {
            currentPlayer.removeDevCard("Monopol");
     //       Platform.runLater(() -> controller.getBoardViewController().setKnightLabel(currentPlayer.getQuantityOfDevCard("Monopol")));
        }

        //TODO
    }

    /**
     * Handles the message from the server that a player played a road building card
     * @param inputFromServer
     */
    private void handleRoadBuildCardPlayed(JsonObject inputFromServer) {
        int playerID = inputFromServer.get("Straßenbaukarte ausspielen").getAsJsonObject().get("Spieler").getAsInt();
        if (playerID == id) {
            currentPlayer.removeDevCard("Straßenbau");
            //Platform.runLater(() -> controller.getBoardViewController().setRoadBuildingLabel(currentPlayer.getQuantityOfDevCard("Straßenbau")));
        } else {
            controller.getPlayerById(id).removeDevCard("Unbekannt");
        }
    }

    private void handleYearOfPlentyCardPlayed(JsonObject inputFromServer) {
        logger.info(inputFromServer.toString());
        int playerID = inputFromServer.get("Erfindung").getAsJsonObject().get("Spieler").getAsInt();
        if (playerID == id) {
            currentPlayer.removeDevCard("Erfindung");
     //       Platform.runLater(() -> controller.getBoardViewController().setKnightLabel(currentPlayer.getQuantityOfDevCard("Erfindung")));
        }

        //TODO
    }

    /**
     * sends Hello to Server
     * starts the connection with the server
     */
    private void sendHello() {
        try {
            logger.info("OUT: " + protocol.startConnectionToServer("JavaFXClient " + protocolVersion + " (InfraroteHacks) (KI)").toString());
            writer.write(protocol.startConnectionToServer("JavaFXClient " + protocolVersion + " (KI)").toString() + newLine);
            writer.flush();
            try {
                Thread.sleep(1000);
            }catch (Exception e){
                logger.log(Level.SEVERE, e.getMessage());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Sends the player info to the Server
     * @param name name that the player chose
     * @param color color that the player chose
     * @return boolean
     */
    private boolean sendPlayerInfo(String name, String color) {
        try {
            logger.info("OUT: " + protocol.sendPlayerInfoToServer(name, color).toString());
            writer.write(protocol.sendPlayerInfoToServer(name, color).toString() + newLine);
            writer.flush();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    /**
     * Sends the message that the player is ready
     * @return true if ready
     */
    private boolean sendImReady() {
        try {
            logger.info("OUT: " + protocol.playerIsReadyToServer().toString());
            writer.write(protocol.playerIsReadyToServer().toString() + newLine);
            writer.flush();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            return false;
        }
    }

    /**
     * Just sends the message that the Client wants to throw the dice
     */
    private void sendThrowDice() {
        try {
            logger.info("OUT: " + protocol.throwDice().toString());
            writer.write(protocol.throwDice().toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());

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
            logger.info("OUT: " + protocol.build(type, location).toString());
            writer.write(protocol.build(type, location).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());

        }

    }

    /**
     * This method is finishing the current Move
     */
    public void sendFinishMove() {
        try {
            controller.gotResources();
            logger.info("OUT: " + protocol.finishMove().toString());
            writer.write(protocol.finishMove().toString() + newLine);
            writer.flush();
            finishedFlag = true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Sends the chat message from the user to the Server
     * @param message message the user wants to send to other players
     */
    public void sendChatMessage(String message) {
        try {
            logger.info("OUT: " + protocol.sendChatMessageToServer(message));
            writer.write(protocol.sendChatMessageToServer(message).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * (11.2)
     * Sends the message with resources to drop to the Server
     * @param resourcesFromClient resources to send to the server
     */
    public void sendResourceDrop(Resource[] resourcesFromClient) {
        try {
            JsonObject resources = NetworkTranslator.translateResourceObject(resourcesFromClient);
            logger.info("OUT: " + protocol.submitResourcesToServer(resources));
            writer.write(protocol.submitResourcesToServer(resources).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     *
     * (11.5)
     * @param offer resources the user wants to give to the bank
     * @param request resources the user wants to get
     */
    public void sendHarborTrade(JsonObject offer, JsonObject request) {
        try {
            logger.info("OUT: " + protocol.offerHarborTradeToServer(offer, request));
            writer.write(protocol.offerHarborTradeToServer(offer, request).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        //TODO
    }

    /**
     * (11.1)
     * @param offerFromClient resources the user offers other clients
     * @param requestFromClient resources the user wants to get from the other clients
     */
    public void sendDomesticTrade(Resource[] offerFromClient, Resource[] requestFromClient) {
        resetTradeVariables();
        try {
            JsonObject offer = NetworkTranslator.translateResourceObject(offerFromClient);
            JsonObject request = NetworkTranslator.translateResourceObject(requestFromClient);
            isTradingPlayer = true;
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
            logger.info("OUT: " + protocol.moveRobberToServer(location, idRobbedPlayer).toString());
            writer.write(protocol.moveRobberToServer(location, idRobbedPlayer).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Message the client sends if the trade will be accepted
     * @param accept the offer or not
     */
    public void sendAcceptOffer(boolean accept){
        try {
            logger.info("OUT: "+ protocol.acceptOfferToServer(currentTradeId, accept));
            writer.write(protocol.acceptOfferToServer(currentTradeId, accept).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * (12.3)
     * Sends the trade execution to the server with tradeId + playerId of the player the current player trades with
     * @param playerId id of the player the current player trades with
     */
    public void sendTradeExecution(int playerId) {
        try {
            int tradeId = currentTradeId;
            logger.info("OUT: " + protocol.finishTradeToServer(tradeId, playerId));
            writer.write(protocol.finishTradeToServer(tradeId, playerId).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        //TODO
    }

    /**
     * (12.4)
     * Sends message to the server that the current player wants to cancel the trade
     * @param tradeId id of the trade
     */
    public void sendTradeCancel(int tradeId) {
        try {
            logger.info("OUT: " + protocol.cancelTradeToServer(tradeId));
            writer.write(protocol.cancelTradeToServer(tradeId).toString() + newLine);
            writer.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        //TODO
    }

    /**
     * Sends a message to the server that the client wants to buy a development card
     */
    public void sendBuyDevCard() {
        try {
            logger.info("OUT: " + protocol.buyDevelopmentCard());
            writer.write(protocol.buyDevelopmentCard().toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * Sends a message to the server that the client wants to play its knight card
     * @param location where the robber will be placed
     * @param targedId id of the player that gets robbed
     */
    public void sendPlayKnightCard(JsonObject location, int targedId) {
        try {
            logger.info("OUT: " + protocol.playKnightCardToServer(location, targedId));
            writer.write(protocol.playKnightCardToServer(location, targedId).toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * Sends a message to the server that the client wants to play a monopoly card
     * @param resource resource the player wants to have from other players
     */
    public void sendPlayMonopolyCard(String resource) {
        try {
           logger.info("OUT: " + protocol.playMonopolCardToServer(resource));
            writer.write(protocol.playMonopolCardToServer(resource).toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * Sends a message to the server that the client wants to play a year of plenty card
     * @param resources resources that the player wants
     */
    public void sendPlayYearOfPlentyCard(JsonObject resources) {
        try {
            logger.info("OUT: " + protocol.playYearOfPlentyCardToServer(resources));
            writer.write(protocol.playYearOfPlentyCardToServer(resources).toString() + newLine);
            writer.flush();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
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
                logger.info("OUT: " + protocol.playBuildStreetCardToServer(road1, road2));
                writer.write(protocol.playBuildStreetCardToServer(road1, road2).toString() + newLine);
                writer.flush();
            } else {
                logger.info("OUT: " + protocol.playBuildStreetCardToServer(road1, null));
                writer.write(protocol.playBuildStreetCardToServer(road1, null).toString() + newLine);
                writer.flush();
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString());
        }
    }

    /**
     * setter for the AiController
     * @param controller a reference of the AiController
     */
    public void setController(AiController controller) {
        this.controller = controller;
    }

    /**
     * clears all variables for the trade
     */
    public void resetTradeVariables(){
        currentTradeId = 0;
        currentAcceptedPlayers = new ArrayList<>();
        playersAnsweredToTrade = 0;
        isTradingPlayer = false;
    }
}