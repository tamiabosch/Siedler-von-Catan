package ai;

import LOG.Logging;
import com.google.gson.JsonObject;
import model.*;
import network.JsonObjectCreator;
import network.NetworkTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ai.AiUtility.*;


/**
 * this class handles the behavior of the ai; all methods choose randomly what to return
 * a logger is called and handled to the NetworkTranslate to be sure to log all information needed
 * @author Sandra
 */
public class AiController implements Runnable{

    private Board board;
    private ArrayList<Player> players = new ArrayList<>();
    private AINetworkController aiNetworkController;
    private Player currentPlayer;
    private Logger logger;
    private boolean devCardsAvailable = true;
    private int devCardsBought = 0;
    private boolean firstAction = true;
    private int roundCounter = 0;
    private int lastTradedRound = 0;
    private boolean waitForResourceInput = false;

    //private Resource[] resources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};


    /**
     * constructor starts the logger and initialises a NetworkTranslator with it
     */
    public AiController(){
//        Logging.suppressConsoleOutput();
        logger = Logging.getLoggerClient("ai");
        logger.info("ai started  - encoding test: Hallööchen");
        logger.info(Thread.currentThread().toString());
        try {
            new NetworkTranslator(logger);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
        new Thread(this).start();

        logger.info(Thread.currentThread().toString());
    }

    /**
     * setter for the Board
     * @param b sets the board that is given from the server
     */
    public void setBoard(Board b){
        this.board = b;
    }

    /**
     * getter for th Board
     * @return a reference to the Board
     */
    public Board getBoard(){return this.board;}

    /**
     * saves the state of a player in the Player object
     * @param id the id form the player, given by the server
     * @param newState the state to change the players state to
     */
    public void changeStateOfPlayer(int id, String newState){
        Player playerToChange = getPlayerById(id);
        if(playerToChange != null){
            playerToChange.setState(newState);
        }
    }

    /**
     * looks fore the player with the given id
     * @param id the id form the player, given by the server
     * @return a reference to the player with the specific id
     */
    public Player getPlayerById(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    /**
     * adds a player to the ArrayList of Players, max: 4
     * @param player reference to the player that is part of the game
     */
    public void addPlayer(Player player) {
        for(Player pl : players){
            if(player.getId() == pl.getId()){
                logger.info("player id found in players");
                return;
            }
        }
        if (players.size() <= 4) {
            players.add(player);
        }else {
            logger.log(Level.SEVERE, "players full");
        }
    }

    /**
     * Checks if the Player has any Developement Cards and uses them if it is useful.
     */
    public void useDevCards(){
        logger.info("Player: " + currentPlayer + " will try to use dev card");

        //Checks if the Player would win by using his knight cards, and uses them, if the Player could
        if(!currentPlayer.hasBiggestKnightForce() && currentPlayer.getVictoryPoints() == 8 && (currentPlayer.getQuantityOfDevCard("Ritter") + currentPlayer.getKnightForce() >= 3)){
            logger.info("AI is trying to win with the biggest KnightForce");
            for(int i = 0; i < (3 - currentPlayer.getKnightForce()); i++){
                placeRobberOnDesertOrEmpty(true);
            }
        }

        logger.info("Got through KnightForce win Test with " + currentPlayer.getQuantityOfDevCard("Ritter") + " KnightCards.");

        //Checks if it can play a knight card and does so if it should
        if(currentPlayer.getQuantityOfDevCard("Ritter") > 0){
            boolean hasRobber = false;
            Intersection[] neighbouringIntersectionsToRobber = board.getNeighbouringIntersections(board.getLandpieceWithActiveRobber());
            for(Intersection intersection : neighbouringIntersectionsToRobber){
                if(intersection.getOwner() == currentPlayer){
                    hasRobber = true;
                    break;
                }
            }
            logger.info("Has the robber: " + hasRobber);
            if(hasRobber){
                logger.info(currentPlayer + " versucht den Räuber zu versetzen.");
                placeRobber(true);
                logger.info(currentPlayer + " hat den Räuber versetzt.");
            }
        }

        logger.info("Got through KnightForce Test with " + currentPlayer.getQuantityOfDevCard("Ritter") + " KnightCards.");

        //Checks if it can play a monopoly card and does so if it should
        if(currentPlayer.getQuantityOfDevCard("Monopol") > 0){
            int erz = currentPlayer.getQuantityOfAResource("Erz");
            int getreide = currentPlayer.getQuantityOfAResource("Getreide");
            if(currentPlayer.resourcesNeededForSettlement() == 1 && currentPlayer.getSettlements().size() < 5 && board.getViableSettlements(currentPlayer.getStreets()).size() > 0){
                for(Resource resource : currentPlayer.getResources()){
                    if(resource.getType() != "ore" && resource.getValue() < 1){
                        logger.info(currentPlayer + " versucht die Monopolkarte mit " + resource.getType() + " zu spielen. || " + resource.getType() + " vorher: " + currentPlayer.getQuantityOfAResource(resource.getTypeGerman()));
                        String choice = resource.getTypeGerman().toUpperCase();
                        aiNetworkController.sendPlayMonopolyCard(choice);
                        logger.info(currentPlayer + " hat die Monopolkarte mit " + resource.getType() + " gespielt. || " + resource.getType() + " nachher: " + currentPlayer.getQuantityOfAResource(resource.getTypeGerman()));
                    }
                }
            }
            if(currentPlayer.getQuantityOfDevCard("Monopol") > 0) {
                int possibleCitys = 0;
                for (Settlement settlement : currentPlayer.getSettlements()) {
                    if (settlement.isCity() == false) {
                        possibleCitys++;
                    }
                }
                if (((erz >= 3 && getreide < 2) || (erz < 3 && getreide >= 2)) && possibleCitys > 0) {
                    if ((erz >= 3 && getreide < 2)) {
                        logger.info(currentPlayer + " versucht die Monopolkarte mit Getreide zu spielen. || Getreide vorher: " + currentPlayer.getQuantityOfAResource("Getreide"));
                        aiNetworkController.sendPlayMonopolyCard("GETREIDE");
                        logger.info(currentPlayer + " hat die Monopolkarte mit Getreide gespielt. || Getreide nachher: " + currentPlayer.getQuantityOfAResource("Getreide"));
                    } else {
                        logger.info(currentPlayer + " versucht die Monopolkarte mit Erz zu spielen. || Erz vorher: " + currentPlayer.getQuantityOfAResource("Erz"));
                        aiNetworkController.sendPlayMonopolyCard("ERZ");
                        logger.info(currentPlayer + " hat die Monopolkarte mit Erz gespielt. || Erz nachher: " + currentPlayer.getQuantityOfAResource("Erz"));
                    }
                }
            }
        }

        logger.info("Got through Monopoly Test.");

        //Checks if it can play a road building card and does so if it should
        if(currentPlayer.getQuantityOfDevCard("Straßenbau") > 0){
            Street street1 = getRandomPossibleStreet(null);
            Street street2 = getRandomPossibleStreet(street1);

            if(street1 != null && street2 != null){
                logger.info(currentPlayer + " versucht eine Straßenbaukarte zu spielen.");
                aiNetworkController.sendRoadBuildingCard(NetworkTranslator.translateStreetCoordinate(new Utility.Pair[]{street1.getA().getCoordinates(), street1.getB().getCoordinates()}), NetworkTranslator.translateStreetCoordinate(new Utility.Pair[]{street2.getA().getCoordinates(), street2.getB().getCoordinates()}));
                logger.info(currentPlayer + " hat eine Straßenbaukarte gespielt.");
            }
        }

        logger.info("Got through StreetBuilding Test.");

        //Checks if it can play a year of plenty card and does so if it should
        if(currentPlayer.getQuantityOfDevCard("Erfindung") > 0){
            Resource[] resources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};

            if(currentPlayer.resourcesNeededForSettlement() == 2){
                for(int i = 0 ; i < resources.length; i++){
                    if(currentPlayer.getResources()[i].getType() != "ore" && currentPlayer.getResources()[i].getValue() < 1){
                        resources[i].setValue(1);
                    }
                }
                logger.info(currentPlayer + " versucht eine Erfindungskarte zu spielen.");
                JsonObject resourcesToServer = JsonObjectCreator.createResourcesObject(resources[0].getValue(), resources[1].getValue(), resources[2].getValue(), resources[3].getValue(), resources[4].getValue());
                aiNetworkController.sendPlayYearOfPlentyCard(resourcesToServer);
                logger.info(currentPlayer + " hat eine Erfindungskarte gespielt.");
            }

            if(currentPlayer.getQuantityOfDevCard("Erfindung") > 0 && currentPlayer.resourcesNeededForCity() == 2){
                int erz = currentPlayer.getQuantityOfAResource("Erz");
                if(erz == 1){
                    resources[4].setValue(2);
                    logger.info(currentPlayer + " versucht eine Erfindungskarte zu spielen.");
                    JsonObject resourcesToServer = JsonObjectCreator.createResourcesObject(resources[0].getValue(), resources[1].getValue(), resources[2].getValue(), resources[3].getValue(), resources[4].getValue());
                    aiNetworkController.sendPlayYearOfPlentyCard(resourcesToServer);
                    logger.info(currentPlayer + " hat eine Erfindungskarte gespielt.");
                } else {
                    resources[3].setValue(1);
                    resources[4].setValue(1);
                    logger.info(currentPlayer + " versucht eine Erfindungskarte zu spielen.");
                    JsonObject resourcesToServer = JsonObjectCreator.createResourcesObject(resources[0].getValue(), resources[1].getValue(), resources[2].getValue(), resources[3].getValue(), resources[4].getValue());
                    aiNetworkController.sendPlayYearOfPlentyCard(resourcesToServer);
                    logger.info(currentPlayer + " hat eine Erfindungskarte gespielt.");
                }
            }
        }
        logger.info("Got through Invention Test.");
        logger.info("Player: " + currentPlayer + " tried to play DevCards");
    }

    /**
     * returns a random street of the viable streets the player could build. the exception is the street that already got build before.
     * @param exception
     * @return a  random, viable street
     */
    private Street getRandomPossibleStreet(Street exception){
        ArrayList<Street> playerStreets = new ArrayList<>();
        for(Street street : currentPlayer.getStreets()){
            playerStreets.add(street);
        }
        if(exception != null){
            playerStreets.add(exception);
        }
        ArrayList <Street> streets = board.getViableStreets(playerStreets);

        for(Street street : streets){
            ArrayList<Street> tmpStreets = new ArrayList<>(playerStreets);
            tmpStreets.add(street);
            if(board.getViableSettlements(tmpStreets).size() > 0){
                return street;
            }
        }
        if(streets.size() > 0){
            Street randomStreet = streets.get((int) (streets.size() * new Random().nextDouble()));
            return randomStreet;
        }
        return null;
    }

    /**
     * Buys a Developement Card if possible and useful
     */
    public void checkDevCardBuy(){

        Resource[] playerResource = currentPlayer.getResources();

        ArrayList<Settlement> playerCitys = currentPlayer.getSettlements();
        playerCitys.removeIf(s -> !s.isCity());
        if(currentPlayer.getSettlements().size() > 4 || playerCitys.size() > 2){
            if(playerResource[2].getValue() > 0 && playerResource[3].getValue() > 0 && playerResource[4].getValue() > 0){
                aiNetworkController.sendBuyDevCard();
            }
        } else if(playerResource[2].getValue() > 0 && playerResource[3].getValue() > 0 && playerResource[4].getValue() > 0 && !(currentPlayer.resourcesNeededForCity() == 0 || currentPlayer.resourcesNeededForSettlement() == 0)){
            aiNetworkController.sendBuyDevCard();
        }
    }

    /**
     * instances a new AINetworkController as a new Thread and starts the Thread
     * gives the networkController a reference of it's own
     */
    public void setClientController(String server, String ip){
        logger.info("Verbunden mit: " + server);
        try {
            AINetworkController aiNetworkController = new AINetworkController(server, ip, logger);
            Thread clientControllerThread = new Thread(aiNetworkController);
            clientControllerThread.start();
            aiNetworkController.setController(this);
            this.aiNetworkController = aiNetworkController;
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * sets the currentPlayer; currentPlayer is the own player, who is playing this game (not active player)
     * @param currentPlayer a reference of the player who plays this game
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }


    /**
     * handles the phase "Handeln oder Bauen"
     * coordinates when to build or send a trade offer
     */
    public void handleRound() {
        logger.info("round: " + roundCounter);
        useDevCards();
        checkDevCardBuy();
        // City
        if (!cityUsefulBeforeSettlement()) {
            // Settlement
            if (!settlementRequestUseful()) { //Ausnahme Häfen hinzufügen
                // City
                if (!cityRequestUseful()) {
                    // Street
                    if (!streetRequestUseful()) {
                        // Trade; only checked (and executed if first check!!)
                        logger.info("first action: " + firstAction);
                        if (firstAction && (roundCounter - lastTradedRound > 1)) {
                            if (checkTrade()) {
                                logger.info("wait for response");
                                // leaves method
                                return;
                            }
                            checkDevCardBuy();
                        } else {
                            if (checkHarborTrade()) {
                                logger.info("wait for input");
                                // leaves method
                                return;
                            }
                            checkDevCardBuy();
                            logger.info("everything checked");
                        }
                        if(!cityUsefulBeforeSettlement()){
                            if(!settlementRequestUseful()){
                                if(!cityRequestUseful()){
                                    streetRequestUseful();
                                }
                            }
                        }
                    }
                }
            }
        }

        try{
            Thread.sleep(1000);
        }catch (Exception e){
            logger.info("sleep interrupted: " + e.getMessage());
        }
        logger.info("round ended: send finish move");
        aiNetworkController.sendFinishMove();
    }

    /**
     * sends a buildRequest for a settlement
     */
    public void choseSettlement(){
        logger.info("Initialrunde : " + currentPlayer + " baut eine Siedlung.");
        ArrayList<Intersection> viableIntersections;
        viableIntersections = new ArrayList<>(board.getViableSettlementsInitial());
        viableIntersections.removeIf(i -> board.getNeighbouringLandpieces(i).contains(null)|| board.getNeighbouringLandpieces(i).stream().anyMatch(l -> l != null && l.getResourceType() == LandpieceType.DESERT));

        if (!currentPlayer.getSettlements().isEmpty()) {
            ArrayList<Landpiece> landpiecesFirstRound = board.getNeighbouringLandpieces(currentPlayer.getSettlements().get(0).getCoordinates());
            int highestAmount = highestAmountOfDifLandpieces(landpiecesFirstRound, viableIntersections);
            logger.info("Höchste Anzahl an zu schon besessenen Landpieces unterschiedlichen Landpieces um die möglichen Intersections: " + highestAmount);
            viableIntersections.removeIf(i -> amountNewLandpieces(landpiecesFirstRound, board.getNeighbouringLandpieces(i)) != highestAmount);
        } else {
            ArrayList<Intersection> viableIntersectionsWith3Resources = getviableIntersectionsWith3Resources(viableIntersections);
            ArrayList<Intersection> viableIntersectionsWith2Resources = getviableIntersectionsWith2Resources(viableIntersections);
            logger.info("Viable Intersections with 3 Resources : " + viableIntersectionsWith3Resources.size() + " || Viable Intersections with 2 Resources : " + viableIntersectionsWith2Resources.size());

            if (!viableIntersectionsWith3Resources.isEmpty()) {
                viableIntersections = viableIntersectionsWith3Resources;
            } else if (!viableIntersectionsWith2Resources.isEmpty()) {
                viableIntersections = viableIntersectionsWith2Resources;
            }
        }

        Intersection randomIntersection = viableIntersections.get((int) (viableIntersections.size() * new Random().nextDouble()));
        aiNetworkController.sendBuildRequest("Dorf", NetworkTranslator.translatePairArray(NetworkTranslator.translateIntersectionCoordinateToLandpieces(randomIntersection.getCoordinates())));
    }

    /**
     * returns only those viable intersections, that border on three different LandpieceTypes.
     * @param viableIntersections
     * @return an ArrayList of Intersections with three different LandpieceTypes as neighbours
     */
    private ArrayList<Intersection> getviableIntersectionsWith3Resources(ArrayList<Intersection> viableIntersections){
        ArrayList<Intersection> viableIntersectionsWith3Resources = new ArrayList<>(viableIntersections);
        viableIntersectionsWith3Resources.removeIf(i -> amountOfDiffLandpieces(i) != 3);
        return viableIntersectionsWith3Resources;
    }
    /**
     * returns only those viable intersections, that border on two different LandpieceTypes.
     * @param viableIntersections
     * @return an ArrayList of Intersections with two different LandpieceTypes as neighbours
     */
    private ArrayList<Intersection> getviableIntersectionsWith2Resources(ArrayList<Intersection> viableIntersections){
        ArrayList<Intersection> viableIntersectionsWith2Resources = new ArrayList<>(viableIntersections);
        viableIntersectionsWith2Resources.removeIf(i -> amountOfDiffLandpieces(i) != 2);
        return viableIntersectionsWith2Resources;
    }

    /**
     * calculates the amount of different LandpieceTypes an Intersection is bordering on.
     * @param intersection
     * @return the amount of different LandpieceTypes an Intersection is bordering on.
     */
    private int amountOfDiffLandpieces(Intersection intersection){
        ArrayList<Landpiece> neighbouringLandpieces = board.getNeighbouringLandpieces(intersection);
        ArrayList<LandpieceType> difLandpieceTypes = new ArrayList<>();

        for(Landpiece landpiece : neighbouringLandpieces){
            if(!difLandpieceTypes.contains(landpiece.getResourceType())){
                difLandpieceTypes.add(landpiece.getResourceType());
            }
        }

        return difLandpieceTypes.size();
    }

    /**
     * calculates the highest amount of new LandpieceTypes a player can still get by building a settlement.
     * @param landpiecesFirstRound
     * @param viableIntersections
     * @return the highest amount of new LandpieceTypes a player can still get by building a settlement.
     */
    private int highestAmountOfDifLandpieces(ArrayList<Landpiece> landpiecesFirstRound, ArrayList<Intersection> viableIntersections){
        int highestAmount = 0;
        for(Intersection intersection : viableIntersections){
            ArrayList<Landpiece> landpiecesOfIntersection = board.getNeighbouringLandpieces(intersection);
            ArrayList<LandpieceType> difLandpieces = new ArrayList<>();
            for(Landpiece landpiece : landpiecesOfIntersection){
                if(!landpiecesFirstRound.stream().anyMatch(l -> l.getResourceType() == landpiece.getResourceType())){
                    if(!difLandpieces.contains(landpiece.getResourceType())) {
                        difLandpieces.add(landpiece.getResourceType());
                    }
                }
            }
            int amountDif = difLandpieces.size();
            if(amountDif > highestAmount){
                highestAmount = amountDif;
            }
        }
        return highestAmount;
    }

    /**
     * calculates the amount of new LandpieceTypes the landpieces in question offers (compared to already owned Landpiece Types).
     * @param landpiecesOriginal
     * @param landpiecesInQuestion
     * @return the amount of new LandpieceTypes the landpieces in question offers
     */
    private int amountNewLandpieces(ArrayList<Landpiece> landpiecesOriginal, ArrayList<Landpiece> landpiecesInQuestion){
        ArrayList<LandpieceType> newLandpieceTypes = new ArrayList<>();
        for(Landpiece landpiece : landpiecesInQuestion){
            if(!landpiecesOriginal.stream().anyMatch(l -> l.getResourceType() == landpiece.getResourceType())){
                if(!newLandpieceTypes.contains(landpiece.getResourceType())){
                    newLandpieceTypes.add(landpiece.getResourceType());
                }
            }
        }
        return newLandpieceTypes.size();
    }

    /**
     * sends buildRequest for a random street
     * @param initial true for the first two rounds
     */
    public void chooseStreet(boolean initial){
        ArrayList <Street> streets;
        if(initial){
            streets = board.getViableStreetsInitial(currentPlayer);
        } else {
            streets = board.getViableStreets(currentPlayer.getStreets());

            for(Street street : streets){
                ArrayList<Street> tmpStreets = new ArrayList<>(currentPlayer.getStreets());
                tmpStreets.add(street);
                if(board.getViableSettlements(tmpStreets).size() > 0){
                    aiNetworkController.sendBuildRequest("Straße", NetworkTranslator.translateStreetCoordinate(new Utility.Pair[]{street.getA().getCoordinates(), street.getB().getCoordinates()}));
                    return;
                }
            }
        }

        if(streets.size() > 0){
            Street randomStreet = streets.get((int) (streets.size() * new Random().nextDouble()));
            aiNetworkController.sendBuildRequest("Straße", NetworkTranslator.translateStreetCoordinate(new Utility.Pair[]{randomStreet.getA().getCoordinates(), randomStreet.getB().getCoordinates()}));
        }

    }

    /**
     * chooses a landpiece to place the robber on
     * never steals from a player
     */
    public void placeRobber(boolean knightCard) {
        logger.info(".");

        ArrayList<Intersection> developedIntersections = board.getIntersectionsWithBuilding();
        ArrayList<Landpiece> possibleLandpieces = new ArrayList<>();
        for (Intersection intersection : developedIntersections) {
            ArrayList<Landpiece> possibleLandpiecesTmp = board.getNeighbouringLandpieces(intersection);
            try {
                for (Landpiece possibleLandpiece : possibleLandpiecesTmp) {
                    if (possibleLandpiece.getResourceType() != LandpieceType.DESERT && !Arrays.stream(board.getNeighbouringIntersections(possibleLandpiece)).anyMatch(i -> i.getOwner() == currentPlayer)) {
                        possibleLandpieces.add(possibleLandpiece);
                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getLocalizedMessage());
            }
        }
        logger.info("Possible Landpieces : " + possibleLandpieces);
        if (!possibleLandpieces.isEmpty()) {
            try {
                Landpiece landpieceRndm = possibleLandpieces.get((new Random()).nextInt(possibleLandpieces.size()));
                ArrayList<Player> playersToRob = new ArrayList<>();
                for (Intersection intersection : board.getNeighbouringIntersections(landpieceRndm)) {
                    if (intersection.getOwner() != null) {
                        playersToRob.add(intersection.getOwner());
                    }
                }
                Player playerToRob = playersToRob.get((new Random()).nextInt(playersToRob.size()));
                logger.info("New Robber Landpiece : " + landpieceRndm + " || Robbed Player : " + playersToRob);
                if(knightCard == false){
                    aiNetworkController.sendMoveRobber(NetworkTranslator.translateLandpieceCoordinateToProtocol(landpieceRndm.getCoordinates()), playerToRob.getId());
                } else {
                    aiNetworkController.sendPlayKnightCard(NetworkTranslator.translateLandpieceCoordinateToProtocol(landpieceRndm.getCoordinates()), playerToRob.getId());
                }
                logger.info("wating for resources");
                waitForResourceInput = true;
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getStackTrace().toString());
            }
        }
        //place robber on desert or empty field
        else {
            placeRobberOnDesertOrEmpty(knightCard);
        }
    }

    /**
     * places the robber on the desert, or on a field with no owned settlements bordering on it, if the robber is already on the desert field.
     * @param knightCard
     */
    private void placeRobberOnDesertOrEmpty(boolean knightCard){
        Utility.Pair desertOrEmptyCoordinates = new Utility.Pair(0, 0);
        int playerToRobId = -1;
        if(board.getLandpieceWithActiveRobber().getResourceType() != LandpieceType.DESERT) {
            for (Landpiece[] landpieceRow : board.getLandpieces()) {
                for (Landpiece landpiece : landpieceRow) {
                    if (landpiece.getResourceType() == LandpieceType.DESERT) {
                        desertOrEmptyCoordinates = landpiece.getCoordinates();
                        ArrayList<Intersection> possibleIntersections = new ArrayList<>(Arrays.asList(board.getNeighbouringIntersections(landpiece)));
                        possibleIntersections.removeIf(i -> i.getOwner() == currentPlayer || i.getOwner() == null);
                        playerToRobId = possibleIntersections.get((new Random()).nextInt(possibleIntersections.size())).getOwner().getId();
                    }
                }
            }
        } else {
            for (Landpiece[] landpieceRow : board.getLandpieces()) {
                for (Landpiece landpiece : landpieceRow) {
                    if (Arrays.stream(board.getNeighbouringIntersections(landpiece)).allMatch(i -> i.getOwner() == null)) {
                        desertOrEmptyCoordinates = landpiece.getCoordinates();
                    }
                }
            }
        }
        if(knightCard == false){
            aiNetworkController.sendMoveRobber(NetworkTranslator.translateLandpieceCoordinateToProtocol(desertOrEmptyCoordinates), playerToRobId);
        } else {
            aiNetworkController.sendPlayKnightCard(NetworkTranslator.translateLandpieceCoordinateToProtocol(desertOrEmptyCoordinates), playerToRobId);
        }

        if(playerToRobId >= 0){
            logger.info("wating for resources");
            waitForResourceInput = true;
        }
    }

    /**
     * if robber called and player has more than 7 cards,
     * this method chooses randomly half (rounded down) of his cards to send to the server
     */
    public void dropResources(){
        try {
            for(Resource rt: currentPlayer.getResources()){
                logger.info(rt.toString());
            }
            int quantity = currentPlayer.getResourcesTotal() / 2;
            Resource[] dropResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
            Resource[] playerResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
            for (byte i = 0; i < playerResources.length; i++) {
                playerResources[i].setValue(currentPlayer.getResources()[i].getValue());
            }
            logger.info("resources to drop: " + quantity);
            while (quantity > 0){
                int x = (int) (5 * new Random().nextDouble());
                logger.info("" + x);
                if(playerResources[x].getValue() > 0){
                    logger.info("dropped resource: " + playerResources[x].getType());
                    dropResources[x].changeValue(1);
                    playerResources[x].changeValue(-1);
                    quantity--;
                    logger.info("to drop: " + quantity);
                }
            }
            for(Resource rt : dropResources){
                logger.info(rt.toString());
            }
            aiNetworkController.sendResourceDrop(dropResources);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }

        for(Resource rt: currentPlayer.getResources()){
            logger.info(rt.toString());
        }
    }

    /**
     * accepts the offer if has less than 2 of offered and more of 2 from requested
     * @param offeredResources resources the other player want to give
     * @param requestedResources resources the other player wants
     * @param playerId player who sent the offer
     */
    public void answerTradeOffer(Resource[] offeredResources, Resource[] requestedResources, int playerId){
        boolean interested = false;
        boolean accept = false;

        byte quantity = 0;
        for(Resource rt : requestedResources){
            quantity += rt.getValue();
        }
        if(quantity != 1){
            logger.info("not 1:1 trade");
            aiNetworkController.sendAcceptOffer(false);
            return;
        }

        //checks if offer is interesting
        for (byte i = 0; i < 5; i++) {
            if(offeredResources[i].getValue() > 0){
                if(currentPlayer.getResources()[i].getValue() < 2){
                    interested = true;
                }
            }
        }

        // checks if enough requested resources
        if(interested) {
            for (byte i = 0; i < 5; i++) {
                if (requestedResources[i].getValue() > 0) {
                    if(currentPlayer.getResources()[i].getValue() > 2){
                        accept = true;
                    } else {
                        accept = false;
                    }
                }
            }
        }

        if(accept){
            logger.info("accepted");
            aiNetworkController.sendAcceptOffer(true);
        } else {
            logger.info("rejected");
            aiNetworkController.sendAcceptOffer(false);
        }
    }

    public ArrayList<Player> getPlayers(){
        return this.players;
    }

    public void choosePlayerToAcceptOffer(ArrayList<Integer> currentAcceptedPlayers) {
        int lowestPlayerId = currentAcceptedPlayers.get(0);

        for(Integer playerID : currentAcceptedPlayers){
            if(getPlayerById(playerID).getVictoryPoints() < getPlayerById(lowestPlayerId).getVictoryPoints()){
                lowestPlayerId = playerID;
            }
        }

        logger.info("Choose offer from: " + getPlayerById(lowestPlayerId));
        aiNetworkController.sendTradeExecution(lowestPlayerId);

        handleRound();
    }

    /**
     * checks if a trade would make sense
     * @return boolean true if trade request was sent
     */
    private boolean checkTrade(){
        boolean tradeInterested = false;
        Resource[] requestResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        Resource[] offerResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};

        for(Resource rt : currentPlayer.getResources()){
            logger.info("" + rt);
        }

        for (byte i = 0; i < 4; i++) {
            if(currentPlayer.getResources()[i].getValue() == 0){
                logger.info("interested in trade for: " + currentPlayer.getResources()[i]);
                tradeInterested = true;
                requestResources[i].changeValue(1);
                break;
            }
        }

        if(tradeInterested) {
            for (byte i = 4; i >= 0; i--) {
                if (currentPlayer.getResources()[i].getValue() > 2) {
                    logger.info("could give: " + currentPlayer.getResources()[i]);
                    offerResources[i].changeValue(1);
                    waitForResourceInput = true;
                    lastTradedRound = roundCounter;
                    firstAction = false;
                    logger.info("send trade offer");
                    aiNetworkController.sendDomesticTrade(offerResources, requestResources);
                    logger.info("first action: " + firstAction);
                    // leaves method
                    return true;
                }
            }
        }

        logger.info("no possibility for trading");
        return false;
    }

    public void increaseDevCardsBought() {
        this.devCardsBought++;
        if (devCardsBought == 25) {
            devCardsAvailable = false;
        }
    }


    /**
     * Builds a street if possible and useful.
     * @return true, if possible and useful, false if not
     */
    private boolean streetRequestUseful(){
        logger.info(".");

        if(canBuildStreet(currentPlayer.getResources())){
            //check if has free space on the board to build a settlement (no more street is needed to build a settlement)
            if(board.getViableSettlements(currentPlayer.getStreets()).size() == 0 ||
                    currentPlayer.getResources()[0].getValue() > 1 && currentPlayer.getResources()[1].getValue() > 1){
                logger.info("canBuildStreet");
                chooseStreet(false);
                return true;
            }
        }
        return false;
    }

    /**
     * Author: Thore
     * Tests if it is useful to build a city.
     * If yes, a city is build and true is being returned. if not, false is being returned.
     * @return
     */
    private boolean cityRequestUseful(){
        boolean usefulCity = false;

        while(canBuildCity(currentPlayer.getResources())){
            logger.info("canBuildCity");
            for(Settlement settlement : currentPlayer.getSettlements()){
                if(!settlement.isCity()){
                    aiNetworkController.sendBuildRequest("Stadt", NetworkTranslator.translatePairArray(NetworkTranslator.translateIntersectionCoordinateToLandpieces(settlement.getCoordinates())));
                    usefulCity = true;
                    break;
                }
            }
            break;
        }

        return usefulCity;
    }

    /**
     * Author: Thore
     * Tests if it is useful to build a city before checking for the usefulness of building a Settlement.
     * If yes, a city is build and true is being returned. if not, false is being returned.
     * @return true if build request was sent
     */
    private boolean cityUsefulBeforeSettlement(){
        if(canBuildCity(currentPlayer.getResources())){
            if(currentPlayer.getSettlements().size() > 3){
                if(currentPlayer.getSettlements().stream().allMatch(s -> s.isCity())){
                    return false;
                }
                for(Settlement settlement : currentPlayer.getSettlements()){
                    if(!settlement.isCity()){
                        logger.info("city build");
                        aiNetworkController.sendBuildRequest("Stadt", NetworkTranslator.translatePairArray(NetworkTranslator.translateIntersectionCoordinateToLandpieces(settlement.getCoordinates())));
                        return true;
                    }
                }
            } else if(currentPlayer.getResources()[4].getValue() >= 5 && currentPlayer.getResources()[3].getValue() >= 2 && currentPlayer.getSettlements().size() >= 3){
                    if(currentPlayer.getSettlements().stream().allMatch(s -> s.isCity())){
                        return false;
                    }
                    for(Settlement settlement : currentPlayer.getSettlements()){
                        if(!settlement.isCity()){
                            logger.info("city build");
                            aiNetworkController.sendBuildRequest("Stadt", NetworkTranslator.translatePairArray(NetworkTranslator.translateIntersectionCoordinateToLandpieces(settlement.getCoordinates())));
                            return true;
                        }
                    }
            }
        }
        return false;
    }

    /**
     * Author: Thore
     * Sends a buildRequest to the Server if there is a useful option and than returns true.
     * Returns false if no useful option was found.
     * @return  true if build request was sent
     */
    private boolean settlementRequestUseful(){
        logger.info(".");

        if(canBuildSettlement(currentPlayer.getResources())){
            if(board.getViableSettlements(currentPlayer.getStreets()).size() > 0){
                if(currentPlayer.resourcesNeededForSettlement() == 0) {
                    Intersection in = board.getViableSettlements(currentPlayer.getStreets()).get((int) ((board.getViableSettlements(currentPlayer.getStreets()).size() * new Random().nextDouble())));
                    aiNetworkController.sendBuildRequest("Dorf", NetworkTranslator.translatePairArray(NetworkTranslator.translateIntersectionCoordinateToLandpieces(in.getCoordinates())));
                    return true;
                }
            }
        }
        return false;

    }


    /**
     * checks if a sea trade is possible
     * (harbors should follow)
     * @return true if trade was sent
     */
    public boolean checkHarborTrade(){
        Resource[] offerResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        Resource[] requestResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        boolean tradeInterested = false;


        for (byte i = 0; i < 5; i++) {
            if(currentPlayer.getResources()[i].getValue() > 5){
                logger.info("could give: " + currentPlayer.getResources()[i]);
                offerResources[i].changeValue(4);
                tradeInterested = true;
                break;
            }

        }

        if(tradeInterested){
            for (byte i = 4; i >= 0; i--) {
                if(currentPlayer.getResources()[i].getValue() < 3){
                    logger.info("interested in: " + currentPlayer.getResources()[i]);
                    requestResources[i].changeValue(1);
                    logger.info("send trade offer");
                    waitForResourceInput = true;
                    aiNetworkController.sendHarborTrade(NetworkTranslator.translateResourceObject(offerResources),NetworkTranslator.translateResourceObject(requestResources));
                    // leaves method
                    return true;
                }
            }
        }
        logger.info("no interest in sea trading");
        return false;
    }




    public void resetFirstAction(){
        firstAction = true;
    }

    public void increaseRoundCounter(){
        this.roundCounter++;
    }

    public int getRound(){
        return this.roundCounter;
    }

    public boolean isWaitingForResourceInput(){
        return waitForResourceInput;
    }

    public void gotResources(){
        this.waitForResourceInput = false;
    }

    @Override
    public void run() {

    }
}
