package view;

import com.google.gson.JsonObject;
import control.Controller;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Popup;
import model.Player;
import model.Resource;
import network.JsonObjectCreator;

/**
 * Created by karamshabita on 17.01.17.
 */
public class MyDevelopmentCardsController {

    private Popup popup;
    private Player currentPlayer;
    private Controller controller;

    @FXML
    Button useMonopolyButton, useYearOfPlentyButton, useRoadBuildingButton, useKnightButton;
    @FXML
    ComboBox useYearOfPlentyCombo1, useYearOfPlentyCombo2, useMonopolyCombo;

    @FXML
    public void initialize(){
        useMonopolyCombo.getItems().addAll("HOLZ","LEHM","WOLLE","GETREIDE","ERZ");
        useYearOfPlentyCombo1.getItems().addAll("HOLZ","LEHM","WOLLE","GETREIDE","ERZ");
        useYearOfPlentyCombo2.getItems().addAll("HOLZ","LEHM","WOLLE","GETREIDE","ERZ");
        useMonopolyCombo.setValue("WÄHLEN");
        useYearOfPlentyCombo1.setValue("WÄHLEN");
        useYearOfPlentyCombo2.setValue("WÄHLEN");
        useYearOfPlentyCombo1.getStyleClass().add("comboBox");
        useYearOfPlentyCombo2.getStyleClass().add("comboBox");
        useMonopolyCombo.getStyleClass().add("comboBox");
        useMonopolyButton.setDisable(true);
        useYearOfPlentyButton.setDisable(true);
        useMonopolyCombo.valueProperty().addListener((ov, t, t1) -> useMonopolyButton.setDisable(false));
        useYearOfPlentyCombo1.valueProperty().addListener((ov, t, t1) -> {
            System.out.println(useYearOfPlentyCombo2.getValue());
            if (!useYearOfPlentyCombo2.getValue().equals("WÄHLEN")) {
                useYearOfPlentyButton.setDisable(false);
            }
        });
        useYearOfPlentyCombo2.valueProperty().addListener((ov, t, t1) -> {
            System.out.println(useYearOfPlentyCombo1.getValue());
            if (!useYearOfPlentyCombo1.getValue().equals("WÄHLEN")) {
                useYearOfPlentyButton.setDisable(false);
            }
        });
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    /**
     * Methode to handle using Knight card
     */
    public void useKnight(){
        controller.getBoardViewController().setPlayingKnightCard(true);
        controller.getBoardViewController().moveRobber();
        closeWindow();
    }

    /**
     * Methode to handle using Monopoly card
     */
    public void useMonopol(){
        String choice = useMonopolyCombo.getValue().toString();
        controller.getClientController().sendPlayMonopolyCard(choice);
        closeWindow();
    }

    /**
     * Methode to handle using Road Building
     */
    public void useRoadbuilding(){
        controller.getBoardViewController().sentMessage("Wähle bis zu 2 Straßen, die du bauen möchtest.");
        controller.getBoardViewController().setPlayingRoadCard(true);
        controller.getBoardViewController().drawViableStreets(false);
        closeWindow();

    }
    /**
     * Methode to handle using Year Of Plenty:
     */
    public void useYearOfPlenty(){

        Resource[] resources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        System.out.println("VALUE 1: " + useYearOfPlentyCombo1.getValue());
        System.out.println("VALUE 2: " + useYearOfPlentyCombo2.getValue());
        switch (useYearOfPlentyCombo1.getValue().toString()) {
            case "HOLZ":
                resources[0].setValue(1);
                break;
            case "LEHM":
                resources[1].setValue(1);
                break;
            case "WOLLE":
                resources[2].setValue(1);
                break;
            case "GETREIDE":
                resources[3].setValue(1);
                break;
            case "ERZ":
                resources[4].setValue(1);
                break;
        }

        switch (useYearOfPlentyCombo2.getValue().toString()) {
            case "HOLZ":
                resources[0].setValue(resources[0].getValue() + 1);
                break;
            case "LEHM":
                resources[1].setValue(resources[1].getValue() + 1);
                break;
            case "WOLLE":
                resources[2].setValue(resources[2].getValue() + 1);
                break;
            case "GETREIDE":
                resources[3].setValue(resources[3].getValue() + 1);
                break;
            case "ERZ":
                resources[4].setValue(resources[4].getValue() + 1);
                break;
        }

        JsonObject resourcesToServer = JsonObjectCreator.createResourcesObject(resources[0].getValue(), resources[1].getValue(), resources[2].getValue(), resources[3].getValue(), resources[4].getValue());
        controller.getClientController().sendPlayYearOfPlentyCard(resourcesToServer);
        closeWindow();

    }

    /**
     * closes POPUP
     */
    public void closeWindow(){
        this.popup.hide();
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Use buttons disabling
     */
    public void disableButtons(){
        if(currentPlayer.getQuantityOfDevCard("Ritter") < 1){
            useKnightButton.setDisable(true);
        }
        if (currentPlayer.getQuantityOfDevCard("Monopol") < 1){
            useMonopolyButton.setDisable(true);
            useMonopolyCombo.setVisible(false);
        }
        if(currentPlayer.getQuantityOfDevCard("Straßenbau") < 1){
            useRoadBuildingButton.setDisable(true);
        }
        if(currentPlayer.getQuantityOfDevCard("Erfindung") < 1){
            useYearOfPlentyButton.setDisable(true);
            useYearOfPlentyCombo1.setVisible(false);
            useYearOfPlentyCombo2.setVisible(false);
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * this methode is to controll the card that the player playing can
     * @param card
     */
    public void deactivateCurrentlyBoughtCard(String card) {
        if (currentPlayer.getQuantityOfDevCard(card) == 1) {
            switch (card) {
                case "Ritter":
                    useKnightButton.setDisable(true);
                    break;
                case "Monopol":
                    useMonopolyButton.setDisable(true);
                    useMonopolyCombo.setVisible(false);
                    break;
                case "Straßenbau":
                    useRoadBuildingButton.setDisable(true);
                    break;
                case "Erfindung":
                    useYearOfPlentyButton.setDisable(true);
                    useYearOfPlentyCombo1.setVisible(false);
                    useYearOfPlentyCombo2.setVisible(false);
                    break;
            }
        }
    }
}
