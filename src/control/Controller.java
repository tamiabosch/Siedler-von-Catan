package control;

import LOG.Logging;
import javafx.application.Platform;
import model.*;
import network.ClientController;
import network.NetworkTranslator;
import view.BoardViewController;
import view.LobbyWindowController;
import view.MyDevelopmentCardsController;
import view.TradeWindowController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

public class Controller {
	
	private Board board = new Board(false);
	private BoardViewController boardViewController;
    private LobbyWindowController lobbyWindowController;
    private ClientController clientController;
    private TradeWindowController tradeWindowController;
    private MyDevelopmentCardsController myDevelopmentCardsController;
    private ArrayList<Player> players = new ArrayList<>();
    private String text;
    public Player currentPlayer;
    Logger logger;
    private boolean devCardsAvailable = true;
    private int devCardsBought = 0;

    public Controller() {

        //drawBoard();
        new NetworkTranslator();
    }

    public void drawBoard(){
        boardViewController.setBoard(board);
        boardViewController.setCurrentPlayer(currentPlayer);
    	boardViewController.fillPane(board.getLandpieces());
        boardViewController.drawIntersections();
        boardViewController.drawStreets();
        boardViewController.initialisePlayerBox(players);
        while(currentPlayer.getState() == null){
            try{
                Thread.sleep(100);
            }catch (InterruptedException e) {
                e.printStackTrace();
            } {

            }
        }
        initialiseTurn();
        boardViewController.sentMessage("Aktueller Status: " + currentPlayer.getState());

        if (currentPlayer.getState().equals("Warten")){
            boardViewController.checkButtonEnabled();
        }

	}

	private void initialiseTurn() {
		boardViewController.sentMessage("Willkommen: " + currentPlayer);
        boardViewController.settlementButton.setDisable(true);
        boardViewController.drawStreetsButton.setDisable(true);
        boardViewController.finishMove.setDisable(true);
        boardViewController.tradeButton.setDisable(true);
        boardViewController.cityButton.setDisable(true);
        boardViewController.harborButton.setDisable(true);
        boardViewController.developmentCardButton.setDisable(true);
        boardViewController.myDevCardsButton.setDisable(true);

        boardViewController.setActivePlayerEffect();
        if (currentPlayer.getState().equals("Dorf bauen")) {
            boardViewController.drawViableSettlements(true);           
        }
    }

    public ClientController getClientController() {
        return clientController;
    }

    public void addPlayer(Player player) {
        if (players.size() <= 4) {
            players.add(player);
            lobbyWindowController.addToOtherPlayersLabel(player.getName());
        }
    }

    public void removePlayer(int id) {
        Iterator<Player> iter = players.iterator();
        while (iter.hasNext()) {
            Player player = iter.next();
            if (player.getId() == id) {
                iter.remove();
            }
        }
    }

    public void disableColorChoices(String color) {
        if (lobbyWindowController != null) {
            lobbyWindowController.disableColorChoices(color);
        }
    }

    public void changeStateOfPlayer(int id, String newState) {
        Player playerToChange = getPlayerById(id);
        if (playerToChange != null) {
            playerToChange.setState(newState);
        }
    }

    public Player getPlayerById(int id) {
        for (Player player : players) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    /**
     * starts the ClientController in a new Thread and returns a reference to the ClientController
     * @param server the server to connect to ("TestServer" of "Nicer Server")
     * @param serverIP the ip address to connect to (not relevant for TestServer so should be null)
     * @return a reference to the started clientController
     * @throws Exception Thread couldn't be started
     */
    public ClientController startClientController(String server, String serverIP) throws Exception {
        clientController = new ClientController(server, serverIP);
        Thread clientControllerThread = new Thread(clientController);
        clientControllerThread.start();
        clientController.setController(this);
        return clientController;
    }

    public void setBoardViewController(BoardViewController bvc) {
        this.boardViewController = bvc;
    }
    public void setLobbyWindowController(LobbyWindowController lwc) {
        this.lobbyWindowController = lwc;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setTradeWindowController(TradeWindowController tradeWindowController) {
        this.tradeWindowController= tradeWindowController;
    }


    public LobbyWindowController getLobbyWindowController() {
        return lobbyWindowController;
    }

    public BoardViewController getBoardViewController() {
        return boardViewController;
    }

    public Board getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers(){
        return this.players;
    }

    public TradeWindowController getTradeWindowController() {
        return tradeWindowController;
    }

    public boolean isDevCardsAvailable() {
        return devCardsAvailable;
    }

    public void increaseDevCardsBought() {
        this.devCardsBought++;
        if (devCardsBought == 25) {
            devCardsAvailable = false;
            Platform.runLater(() -> boardViewController.developmentCardButton.setDisable(true));
        }
    }

    public void setMyDevelopmentCardsController(MyDevelopmentCardsController myDevelopmentCardsController) {
        this.myDevelopmentCardsController = myDevelopmentCardsController;
    }

    public MyDevelopmentCardsController getMyDevelopmentCardsController() {
        return myDevelopmentCardsController;
    }


}
