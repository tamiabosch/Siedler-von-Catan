package InfraroterServer;

import LOG.Logging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.*;
import network.JsonObjectCreator;
import network.NetworkTranslator;
import network.Protocol10;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Controls the interaction between the single clients and the server
 * @author Tamia & Kevin
 */
public class Controller {

    private Board board = new Board(false);
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<ServerController> serverControllerThreads = new ArrayList<>();
    private JsonParser parser = new JsonParser();
    private JsonObject boardJson;
    private Protocol10 protocol = new Protocol10();
    private int playersReadyWithStart1 = 0;
    private int playersReadyWithStart2 = 0;
    private int startingRound = 1;
    private int tradeId = 0;
    private boolean roundTwoJustStarted = false;
    private boolean cheatResources = false;
    private ArrayList<String> devCards = new ArrayList<>(Arrays.asList("knight", "knight", "knight", "knight", "knight", "knight", "knight", "knight", "knight", "knight", "knight", "knight", "knight", "knight", "monopoly", "monopoly", "yearofplenty", "yearofplenty", "roadbuilding", "roadbuilding", "victorypoint", "victorypoint", "victorypoint", "victorypoint", "victorypoint"));
    private Player biggestKnightForce = null;
    private Player longestTradeRoadPlayer = null;
    private int longestRoad = 0;
    private boolean tradeRoadPlayerWasSet = false;
    private Logger logger;

    // just for test
    public static void main(String[] args) {
        new Controller();
        for (int i = 0; i < 10; i++) {
            System.out.println(ThreadLocalRandom.current().nextInt(1, 7));
        }
    }

    /**
     * Checks where the robber is and creates the JSON-version of the board
     */
    public Controller() {
//        Logging.suppressConsoleOutput();
        Logging.initFileHandlerServer();
        logger = Logging.getLoggerServer();
        logger.info("server started - encoding test: Hallööchen");

        JsonObject robber = NetworkTranslator.translateLandpieceCoordinateToProtocol(board.getLandpieceWithActiveRobberCoordinates());
        boardJson = JsonObjectCreator.createBoardObject(createFieldArray(), new JsonArray(), createHarborArray(), robber);
        Collections.shuffle(devCards);
    }

    /**
     * Adds a player to the Players array, when he connects with the Server and chooses a name and color
     * @param player Player that gets added
     */
    void addPlayer(Player player) {
        logger.info("add player: " + player.getId() + "  " + player.getColor());
        if (players.size() <= 4) {
            players.add(player);
        } else {
            System.out.println("Zu viele Spieler");
        }
    }

    /**
     * Removes a player from the player array
     * @param id Player that gets removed
     */
    public void removePlayer(int id) {
        Iterator<Player> iter = players.iterator();
        while (iter.hasNext()) {
            Player player = iter.next();
            if (player.getId() == id) {
                iter.remove();
            }
        }
    }

    /**
     * Changes the state of a player in the model
     * @param id id of the player
     * @param newState new state of the player
     */
    void changeStateOfPlayer(int id, String newState) {
        logger.info(id + " " + newState);
        Player playerToChange = getPlayerById(id);
        if (playerToChange != null) {
            playerToChange.setState(newState);
        }
        checkIfEnoughPlayersAreReady();
    }

    /**
     * Checks if enough players are ready to start a game. There have to be at least 3 ready players.
     */
    private void checkIfEnoughPlayersAreReady() {
        logger.info(".");
        int joinedPlayers = 0;
        int readyPlayers = 0;
        for (Player player : players) {
            String state = player.getState();
            if (state.equals("Wartet auf Spielbeginn")) {
                readyPlayers++;
            } else if (state.equals("Spiel starten")) {
                joinedPlayers++;
            }
        }

        if (readyPlayers == 3 && joinedPlayers == 0 || readyPlayers == 4) {
            logger.info("send start message " + protocol.sendBoardToClients(boardJson));
            sendMessageToAllClients(protocol.sendBoardToClients(boardJson));
            determineStartingPlayer();
        }

    }

    /**
     * Throws dices for each player. Player with the highest number gets starting player.
     * Method calls itself again, if the two highest numbers are the same.
     */
    private void determineStartingPlayer() {
        int maxID = 0;
        int maxDice = 0;
        int maxDice2 = 0;
        for (ServerController serverController : serverControllerThreads) {
            JsonObject diceThrow = serverController.sendRollDice();
            JsonArray diceNumbers = diceThrow.get("Würfelwurf").getAsJsonObject().get("Wurf").getAsJsonArray();
            int dice1 = diceNumbers.get(0).getAsInt();
            int dice2 = diceNumbers.get(1).getAsInt();
            int diceNumber = dice1 + dice2;
            int playerId = diceThrow.get("Würfelwurf").getAsJsonObject().get("Spieler").getAsInt();
            if (diceNumber > maxDice) {
                maxDice = diceNumber;
                maxID = playerId;
            } else if (diceNumber == maxDice) {
                maxDice2 = diceNumber;
            }

        }

        if (maxDice == maxDice2) {
            determineStartingPlayer();    //call method again if two clients have the same highest number
        } else {
            for (ServerController serverController : serverControllerThreads) {
                if (cheatResources) {
                    serverController.sendProfit(JsonObjectCreator.createResourcesObject(99, 99, 99, 99, 99));
                }
                if (maxID == serverController.getPlayerID()) {
                    serverController.setStartingPlayer(true);
                    serverController.sendStatusUpdate("Dorf bauen");

                } else {
                    serverController.sendStatusUpdate("Warten");
                }
            }
        }

    }

    /**
     * Get a player object by his id
     *
     * @param id id
     * @return the player with this id
     */
    Player getPlayerById(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    /**
     * adds a Thread that handles the server communication to the list of all threads
     *
     * @param serverController thread that is going to be added
     */
    void addThreadToArray(ServerController serverController) {
        serverControllerThreads.add(serverController);
    }

    // SEND INFORMATION TO OTHER CLIENTS:

    /**
     * Sends a message from one Client to the others but not to itself
     *
     * @param message  can be anything
     * @param senderID playerID of the Client that sends the message
     */
    void sendMessageToOtherClients(JsonObject message, int senderID) {
        for (ServerController serverController : serverControllerThreads) {
            if (senderID != serverController.getPlayerID()) {
                serverController.sendMessage(message);
            }
        }
    }

    /**
     * Sends the message to all other clients except the clients that are parameters
     * @param message message that gets sent
     * @param senderID id that doesn't get the message
     * @param otherID id that doesn't get the message
     */
    void sendMessageToOtherClients(JsonObject message, int senderID, int otherID) {
        for (ServerController serverController : serverControllerThreads) {
            if (senderID != serverController.getPlayerID() && otherID != serverController.getPlayerID()) {
                serverController.sendMessage(message);
            }
        }
    }

    /**
     * Sends a message from the Server to all clients
     *
     * @param message can be anything
     */
    void sendMessageToAllClients(JsonObject message) {
        for (ServerController serverController : serverControllerThreads) {
            serverController.sendMessage(message);
        }
    }

    /**
     * Sends a message from the Server only to the id given as a parameter
     * @param message message that gets sent
     * @param client client that gets the message
     */
    void sendMessageToOneClient(JsonObject message, int client) {
        for (ServerController serverController : serverControllerThreads) {
            if (serverController.getPlayerID() == client) {
                serverController.sendMessage(message);
            }
        }
    }

    /**
     * A client-connection (InfraroterServer.ServerController) calls this method when it connects to the server to get the status updates of all joined players
     * @param senderID client that connects
     */
    void getStatusUpdatesWhenConnect(int senderID) {
        for (ServerController serverController : serverControllerThreads) {
            if (senderID != serverController.getPlayerID()) {
                serverController.sendStatusUpdateToNewPlayers(senderID);
            }
        }
    }

    /**
     * Creates a JSON-Array of the field, used for the map
     *
     * @return JSON-Array of the field
     */
    private JsonArray createFieldArray() {
        Landpiece[][] landpieces = board.getLandpieces();
        JsonArray landpiecesJson = new JsonArray();
        for (int i = 0; i < landpieces.length; i++) {
            for (int j = 0; j < landpieces[0].length; j++) {
                if (landpieces[i][j] != null) {
                    JsonObject landpiece = NetworkTranslator.translateLandpieceObject(landpieces[i][j], new Utility.Pair(i, j));
                    landpiecesJson.add(landpiece);
                }
            }
        }

        // add standard seapieces
        JsonArray seapieces = parser.parse("[{\"Ort\":\"a\",\"Typ\":\"Meer\"},{\"Ort\":\"b\",\"Typ\":\"Meer\"},{\"Ort\":\"c\",\"Typ\":\"Meer\"},{\"Ort\":\"d\",\"Typ\":\"Meer\"},{\"Ort\":\"e\",\"Typ\":\"Meer\"},{\"Ort\":\"f\",\"Typ\":\"Meer\"},{\"Ort\":\"g\",\"Typ\":\"Meer\"},{\"Ort\":\"h\",\"Typ\":\"Meer\"},{\"Ort\":\"i\",\"Typ\":\"Meer\"},{\"Ort\":\"j\",\"Typ\":\"Meer\"},{\"Ort\":\"k\",\"Typ\":\"Meer\"},{\"Ort\":\"l\",\"Typ\":\"Meer\"},{\"Ort\":\"m\",\"Typ\":\"Meer\"},{\"Ort\":\"n\",\"Typ\":\"Meer\"},{\"Ort\":\"o\",\"Typ\":\"Meer\"},{\"Ort\":\"p\",\"Typ\":\"Meer\"},{\"Ort\":\"q\",\"Typ\":\"Meer\"},{\"Ort\":\"r\",\"Typ\":\"Meer\"}]").getAsJsonArray();
        for (int i = 0; i < seapieces.size(); i++) {
            landpiecesJson.add(seapieces.get(i));
        }

        return landpiecesJson;
    }

    /**
     * Changes the active player while the game is running and not in start mode
     */
    void changeActivePlayer() {
        for (int i = 0; i < serverControllerThreads.size(); i++) {
            if (!serverControllerThreads.get(i).getCurrentPlayer().getState().equals("Warten")) {
                System.out.println(serverControllerThreads.get(i).getNameOfCurrentPlayer() + ": " + i);
                serverControllerThreads.get(i).sendStatusUpdate("Warten");
                if (i + 1 < serverControllerThreads.size()) {
                    serverControllerThreads.get(i + 1).sendStatusUpdate("Würfeln");
                    break;
                } else {
                    serverControllerThreads.get(0).sendStatusUpdate("Würfeln");
                }
            }
        }
    }

    /**
     * Changes the active player in the starting phase
     * @param id id of the player that was currently active
     */
    void changeActivePlayerInStartingPhase(int id) {

        ServerController callingSC = null;
        for (ServerController serverController : serverControllerThreads) {
            if (serverController.getPlayerID() == id) {
                callingSC = serverController;
            }
        }
        if (callingSC != null) {
            if (!roundTwoJustStarted) {
                callingSC.sendStatusUpdate("Warten");
            }

            for (int i = 0; i < serverControllerThreads.size(); i++) {
                if (callingSC.getPlayerID() == serverControllerThreads.get(i).getPlayerID()) {
                    if (startingRound == 1) {
                        if (i + 1 == serverControllerThreads.size()) {
                            serverControllerThreads.get(0).sendStatusUpdate("Dorf bauen");
                        } else {
                            serverControllerThreads.get(i + 1).sendStatusUpdate("Dorf bauen");
                        }
                    } else if (startingRound == 2) {
                        if (roundTwoJustStarted) {
                            callingSC.sendStatusUpdate("Dorf bauen");
                            roundTwoJustStarted = false;
                        } else {
                            if (i - 1 < 0) {
                                serverControllerThreads.get(serverControllerThreads.size() - 1).sendStatusUpdate("Dorf bauen");
                            } else {
                                serverControllerThreads.get(i - 1).sendStatusUpdate("Dorf bauen");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Increases the number of players that are ready with their start phase round 1
     */
    void increasePlayerReadyWithStart1() {
        playersReadyWithStart1++;
        handleStartPhase();
    }

    /**
     * Increases the number of players that are ready with their start phase round 2
     */
    void increasePlayerReadyWithStart2() {
        playersReadyWithStart2++;
        handleStartPhase();
    }

    /**
     * Client tells InfraroterServer.Controller that it is ready with the start phase. If all players are ready, the start phase is over
     */
    private void handleStartPhase() {
        if (playersReadyWithStart2 == players.size()) {
            finishStartingPhase();
            for (ServerController sc : serverControllerThreads) {
                if (sc.isStartingPlayer) {
                    sc.sendStatusUpdate("Würfeln");
                }
            }

        } else if (playersReadyWithStart1 == players.size()) {
            startingRound = 2;
            roundTwoJustStarted = true;
            playersReadyWithStart1++; // hack
        }
    }

    /**
     * Creates a JSON-Array of the harbors, used for the map
     *
     * @return JSON-Array of the harbors
     */
    private JsonArray createHarborArray() {
        JsonArray harborJson = new JsonArray();
        Harbor[] harbors = board.getHarbors();
        for (Harbor harbor : harbors) {
            JsonObject harborObject = NetworkTranslator.translateHarborObject(harbor);
            harborJson.add(harborObject);
        }
        return harborJson;
    }

    /**
     * Returns the board
     * @return board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Gets called when the starting phase is over
     */
    private void finishStartingPhase() {
        for (ServerController sc : serverControllerThreads) {
            sc.setStartingPhase(false);
        }
    }

    /**
     * Checks if a color is still available or if another player has chosen it
     * @param color color
     * @param id id of the player that asks to choose the color
     * @return if the color is available
     */
    boolean checkIfColorStillAvailable(String color, int id) {
        boolean available = true;
        for (ServerController sc : serverControllerThreads) {
            if (id != sc.getPlayerID()) {
                if(color.equals(sc.getColorOfCurrentPlayer())){
                    available = false;
                }
            }
        }
        logger.info("Available: " + available);
        return available;
    }

    /**
     * gets called when the client bought a dev card
     * takes the first element of the shuffled devcards-array and removes it
     *
     * @return the devcard the client gets
     */
    String buyADevCard() {
        String cardToBuy = devCards.get(0);
        devCards.remove(0);
        String toReturn = "";
        switch (cardToBuy) {
            case "victorypoint":
                toReturn = "Siegpunkt";
                break;
            case "monopoly":
                toReturn = "Monopol";
                break;
            case "yearofplenty":
                toReturn = "Erfindung";
                break;
            case "roadbuilding":
                toReturn = "Straßenbau";
                break;
            case "knight":
                toReturn = "Ritter";
                break;
        }
        return toReturn;
    }

    /**
     * Calculates the profit that a player gets when a number was diced
     * @param number number that was diced
     */
    void calculateProfitFromDiceThrow(int number) {
        for (ServerController sc : serverControllerThreads) {
            System.out.println("DICE: " + sc.getCurrentPlayer().getName() + ": " + NetworkTranslator.translateResourceObject(board.getResourcesForDiceThrow(sc.getCurrentPlayer(), number)));
            sc.sendProfit(NetworkTranslator.translateResourceObject(board.getResourcesForDiceThrow(sc.getCurrentPlayer(), number)));
        }
    }

    /**
     * Generates an id for a trade
     * @return tradeId
     */
    int generateTradeId(){
        tradeId++;
        return tradeId;
    }

    /**
     * Returns the available dev cards
     * @return dev cards
     */
    ArrayList<String> getDevCards() {
        return devCards;
    }

    /**
     * Creates an array with all players that have a specific resource
     * @param resource resource
     * @param demandingID player that wants the info (doesnt get added)
     * @return list with players that hold the resource
     */
    ArrayList<Player> getPlayersWithResource(String resource, int demandingID) {
        ArrayList<Player> playerWithDemandedResource = new ArrayList<>();
        for (Player player : players) {
            if (player.getQuantityOfAResource(resource) > 0 && player.getId() != demandingID) {
                playerWithDemandedResource.add(player);
            }
        }
        return playerWithDemandedResource;
    }

    /**
     * If a player plays a knight card it calls that method to check if he can become biggest knight force
     * @param player player that asks
     * @return if he can become biggest knight force or not
     */
    boolean tryToBecomeBiggestKnightForce(Player player) {
        if (biggestKnightForce == player) {
            return true;
        } else if (biggestKnightForce == null && player.getKnightForce() >= 3) {
            biggestKnightForce = player;
            return true;
        } else if (biggestKnightForce != null && biggestKnightForce.getKnightForce() < player.getKnightForce() && player.getKnightForce() >= 3) {
            biggestKnightForce.decreaseVictoryPoints();
            biggestKnightForce.decreaseVictoryPoints();
            biggestKnightForce = player;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the players array
     * @return player array
     */
    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Sets the player that has the longest trade road
     */
    void calculateLongestTradeRoadPlayer() {
        for (Player player : players) {
            ArrayList<ArrayList<Street>> allConnectedStreetsTmp = new ArrayList<>();
            ArrayList<Street> playerStreetTmp = new ArrayList<>(player.getStreets());
            ArrayList<ArrayList<Street>> allConnectedStreets = board.getAllConnectedStreets(playerStreetTmp, allConnectedStreetsTmp);
            int longestRoadOfPlayer = board.calculateLongestPlayerRoad(allConnectedStreets, player);
            if(longestRoadOfPlayer>=5 && longestRoadOfPlayer > longestRoad){
                tradeRoadPlayerWasSet = true;
                longestRoad = longestRoadOfPlayer;
                if (player != longestTradeRoadPlayer && longestTradeRoadPlayer != null){
                    longestTradeRoadPlayer.decreaseVictoryPoints();
                    longestTradeRoadPlayer.decreaseVictoryPoints();
                    longestTradeRoadPlayer.setHasLongestRoad(false);
                    longestTradeRoadPlayer = player;
                } else if (player != longestTradeRoadPlayer) {
                    longestTradeRoadPlayer = player;
                }
            } else if (player == longestTradeRoadPlayer && longestRoadOfPlayer < longestRoad && longestRoadOfPlayer >= 5) {
                longestRoad = longestRoadOfPlayer;
            } else if (player == longestTradeRoadPlayer && longestRoadOfPlayer < longestRoad && longestRoadOfPlayer < 5) {
                longestRoad = 0;
                longestTradeRoadPlayer.decreaseVictoryPoints();
                longestTradeRoadPlayer.decreaseVictoryPoints();
                longestTradeRoadPlayer.setHasLongestRoad(false);
                longestTradeRoadPlayer = null;
            }
        }
    }

    /**
     * Changes the state of all players that have more than 7 resources so that they have to drop cards
     * @param playerID id of the player that has to change its state
     * @param playerID id of the player that has to change its state
     */
    void changeStatusToCardDrop(int playerID) {
        for (ServerController sc : serverControllerThreads) {
            if (sc.getPlayerID() == playerID) {
                if (!sc.isNoCardDrop()) {
                    sc.sendStatusUpdate("Karten wegen Räuber abgeben");
                }
            }
        }
    }

    /**
     * gets longestTradeRoadPlayer
     * @return longestTradeRoadPlayer
     */
    Player getLongestTradeRoadPlayer() {
        return longestTradeRoadPlayer;
    }

    /**
     * returns boolean if tradeRoadPlayer is set
     * @return tradeRoadPlayer
     */
    boolean isTradeRoadPlayerWasSet() {
        return tradeRoadPlayerWasSet;
    }

    boolean checkIfSomeoneHasToDropCards(int playerWhoAsks) {
        for (Player player : players) {
            if (player.getId() != playerWhoAsks) {
                System.out.println("CHECK DROP: " + player.getState());
                if (player.getState().equals("Karten wegen Räuber abgeben")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if all players are ready with their card drop
     */
    void readyWithCardsDrop() {
        boolean allReady = true;
        for (ServerController sc : serverControllerThreads) {
            if (sc.getCurrentPlayer().getState().equals("Karten wegen Räuber abgeben")) {
                allReady = false;
            }
        }
        if (allReady) {
            for (ServerController sc : serverControllerThreads) {
                if (sc.getCurrentPlayer().getState().equals("Räuber versetzen")) {
                    sc.sendStatusUpdate("Handeln oder Bauen");
                }
            }
        }
    }

    /**
     * Creates the official settlers of catan beginner board
     */
    void createBeginnerBoard() {
        board = new Board(true);
        JsonObject robber = NetworkTranslator.translateLandpieceCoordinateToProtocol(board.getLandpieceWithActiveRobberCoordinates());
        boardJson = JsonObjectCreator.createBoardObject(createFieldArray(), new JsonArray(), createHarborArray(), robber);
    }

}