package view;

import ai.AiController;
import control.Controller;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import model.Board;
import model.Player;
import network.ClientController;
import LOG.Logging;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * controls the lobbyWindow, the second window to connect to the server, add ai's, ...
 */
public class LobbyWindowController{

    private Controller controller;
    private BoardView view;
    private ClientController clientController;
    private int count = 0;
    private String server;
    private String serverIP;
    private Logger logger;


    @FXML
    TextField namePlayer,chatTextInput, ipTextfield;
    @FXML
    RadioButton colorButtonRed,colorButtonOrange,colorButtonWhite,colorButtonBlue;
    @FXML
    Button playerIsReadyButton,sendButton,addKiButton, ipButton, muteButton;
    @FXML
    ToggleGroup color;
    @FXML
    Label otherPlayersLabel,kiCounter;
    @FXML
    CheckBox beginnerCheckBox;
    @FXML
    ScrollPane chatScroller;
    @FXML
    TextFlow chatWindow;
    @FXML
    ImageView musicImage;

    private String textOfOtherPlayersLabel = "";

    /**
     * initializes the window
     * set styles and handles what to see and disable
     */
    @FXML
    public void initialize() {
        playerIsReadyButton.setDisable(true);
        colorButtonOrange.setStyle("-fx-background-color: #FFB03B;");
        colorButtonWhite.setStyle("-fx-background-color: #ECF0F1;");
        colorButtonBlue.setStyle("-fx-background-color: #3498DB;");
        // TODO: background color and text color of chat
        chatWindow.getChildren().addListener((ListChangeListener<Node>) ((change) -> chatScroller.setVvalue(1.0)));
        addEnterListener();
        logger = BoardView.getLogger();
        Text infoText = new Text("Bitte verbinde dich mit einem Server. Trage dazu im Feld 'IP-Adresse' die IP-Adresse des Servers ein, mit dem du dich verbinden möchtest.\n");
        infoText.setFill(Color.GRAY);
        chatWindow.getChildren().add(infoText);
    }

    /**
     * gets the username from the text field
     * @return user name from text field
     */
    @FXML
    private String getUserName(){return namePlayer.getText();}

    /**
     * gets the color as String
     * @return color string from the selected toggle box
     */
    @FXML
    public String getColor() {
        RadioButton selectedButton = (RadioButton) color.getSelectedToggle();
        if (selectedButton == colorButtonRed) {
            return "Rot";
        } else if (selectedButton == colorButtonOrange) {
            return "Orange";
        } else if (selectedButton == colorButtonWhite){
            return "Weiß";
        } else if (selectedButton == colorButtonBlue) {
            return "Blau";
        } else {
            return null;
        }
    }

    /**
     * handles the readyButton
     * @throws Exception problem with the clientController
     */
    @FXML
    private void readyButtonPressed() throws Exception {
        Platform.runLater(() -> clientController.sendPlayerInfo(getUserName(), getColor()));
        Platform.runLater(() -> clientController.sendImReady());
        playerIsReadyButton.setDisable(true);
        disableAllColorButtons();
        if (beginnerCheckBox.isSelected()) {
            controller.getClientController().sendBeginnerMode();
        }
    }

    /**
     * starts the game
     * @throws Exception change of scene didn't work
     */
    public void changeSceneToGameBoard() throws Exception {
        logger.info(".");
        Platform.runLater(() -> {
            try {
                this.view.changeSceneToGameBoard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * disables color buttons
     */
    private void disableAllColorButtons() {
        colorButtonBlue.setDisable(true);
        colorButtonRed.setDisable(true);
        colorButtonWhite.setDisable(true);
        colorButtonOrange.setDisable(true);
    }

    /**
     * disable color buttons if unavailable
     * @param colorOfPlayer color to disable
     */
    public void disableColorChoices(String colorOfPlayer) {
        switch (colorOfPlayer) {
            case "Rot":
                colorButtonRed.setDisable(true);
                break;
            case "Orange":
                colorButtonOrange.setDisable(true);
                break;
            case "Weiß":
                colorButtonWhite.setDisable(true);
                break;
            case "Blau":
                colorButtonBlue.setDisable(true);
                break;
        }
        changeChoiceOfColor();
    }

    /**
     *  sets property to selected if disabled
     */
    private void changeChoiceOfColor() {
        if (!colorButtonRed.isDisabled()) {
            colorButtonRed.selectedProperty().set(true);
        } else if (!colorButtonOrange.isDisabled()) {
            colorButtonOrange.selectedProperty().set(true);
        } else if (!colorButtonWhite.isDisabled()) {
            colorButtonWhite.selectedProperty().set(true);
        } else if (!colorButtonBlue.isDisabled()) {
            colorButtonBlue.selectedProperty().set(true);
        }
    }

    /**
     * sets the controller, the boardView, and if the currentPlayer hosts the server
     * @param co reference to the controller
     * @param view reference to the boardView
     * @param hostServer if currentPlayer hosts the server
     */
    void setControllerAndView(Controller co, BoardView view, String serverName, boolean hostServer) {
        this.controller = co;
        this.view = view;
        this.server = serverName;

        if(hostServer){
            serverIP = "localhost";

            Platform.runLater(() -> {
                try {
                    clientController = controller.startClientController(server, serverIP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            ipButton.setDisable(true);
            setNamePlayerTextFieldListener();
            try{
                ipTextfield.setText(InetAddress.getLocalHost().toString().substring(InetAddress.getLocalHost().toString().indexOf("/") + 1));
            }catch(Exception e){
                e.getStackTrace();
            }
        }

        if(server.equals("TestServer")){
            setNamePlayerTextFieldListener();
            clientController = controller.getClientController();
        }
    }

    /**
     * disables playerIsReadyButton if namePlayer text field is empty
     */
    private void setNamePlayerTextFieldListener(){
        namePlayer.textProperty().addListener(((observable, oldValue, newValue) -> {
            if(!namePlayer.getText().isEmpty()){
                playerIsReadyButton.setDisable(false);
            } else {
                playerIsReadyButton.setDisable(true);
            }
        }));
    }

    /**
     * adds other players who joined the the game
     * @param newText name of the player
     */
    public void addToOtherPlayersLabel(String newText){
        textOfOtherPlayersLabel += newText + ": BEREIT \n";
        Platform.runLater(() -> otherPlayersLabel.setText(textOfOtherPlayersLabel));
    }

    /**
     * Shows chatText of a specific player
     * @param s input of text
     */
    public void sentMessage(String s, int id){
        Text message = new Text(id + ": " +s+ "\n");
        Platform.runLater(() -> chatWindow.getChildren().add(message));
    }

    /**
     * Shows chatText of a server
     * @param s input of text
     */
    public void sentMessage(String s){
        Text message;
        if (s.equals("Server erfolgreich verbunden.")) {
            message = new Text(s+ "\n");
            message.setStyle("-fx-font-weight: 800;");
            message.setFill(Color.GREEN);
        } else {
            message = new Text(s + "\n");
        }
        Platform.runLater(() -> chatWindow.getChildren().add(message));
    }

    /**
     * ButtonListener which puts the input of textField in chatWindow
     */
    @FXML
    public void sendButtonPressed(){
        String input = chatTextInput.getText();
        if (!input.equals("")) {
            if (controller.getClientController() != null) {
                controller.getClientController().sendChatMessage(input);
            }
            chatTextInput.clear();
        }
        playerIsReadyButton.setDisable(true);
    }

    /**
     *  sets the enterListener for the chat
     */
    private void addEnterListener() {
        chatTextInput.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                sendButtonPressed();
            }
        });
    }

    /**
     *  saves the input of ipTextfield in serverIP
     *  starts the clientController
     *  disables/ enables buttons
     */
    @FXML
    private void handleIpButton(){
        serverIP = ipTextfield.getText();
        Platform.runLater(() -> {
            try {
                clientController = controller.startClientController(server, serverIP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        setNamePlayerTextFieldListener();
        ipButton.setDisable(true);
        if (!namePlayer.getText().equals("")) {
            playerIsReadyButton.setDisable(false);
        }
    }

    /**
     * Adds Ai´s to the game
     */
    @FXML
    private void handleAddKiButton(){

        try {
            AiController aiController = new AiController();
            aiController.setClientController(server, serverIP);
            count++;
            if(count==1){
                kiCounter.setText(" " +count+" BOT HINZUGEFÜGT");
            } else {
                kiCounter.setText(" " +count+ " BOTS HINZUGEFÜGT");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(count >= 3){
            addKiButton.setDisable(true);
        }
    }

    /**
     * After selected Color, the buttons´ll be disabled
     */
    public void enableButtonsAfterColorError() {
        colorButtonBlue.setDisable(false);
        colorButtonRed.setDisable(false);
        colorButtonWhite.setDisable(false);
        colorButtonOrange.setDisable(false);
        for (Player player : controller.getPlayers()) {
            String color = "";
            if (player.getColor() == Color.RED) {
                color = "Rot";
            } else if (player.getColor() == Color.ORANGE) {
                color = "Orange";
            } else if (player.getColor() == Color.WHITE) {
                color = "Weiß";
            } else if (player.getColor() == Color.AQUA) {
                color = "Blau";
            }
            disableColorChoices(color);
        }
        playerIsReadyButton.setDisable(false);
    }
    /**
     * Button to mute background music
     */
    @FXML
    public void muteMusic(){
        view.mediaPlayer.pause();
        Image playIcon = new Image(getClass().getClassLoader().getResource("play.png").toString());
        Image muteIcon = new Image(getClass().getClassLoader().getResource("mute.png").toString());
        musicImage.setImage(muteIcon);
        muteButton.setOnAction(event -> {
            view.playMusic();
            view.mediaPlayer.seek(Duration.ZERO);
            musicImage.setImage(playIcon);
            muteButton.setOnAction(event1 -> muteMusic());
        });
    }
}
