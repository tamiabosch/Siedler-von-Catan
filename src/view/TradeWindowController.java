package view;


import LOG.Logging;
import control.Controller;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import model.Player;
import model.Resource;
import network.NetworkTranslator;

import java.util.logging.Logger;

/**
 * Handles the Trades-offer and droping cards because of Robber
 * @author Karam
 */
public class TradeWindowController {

    @FXML
    Label outGoingRessource1, outGoingRessource2, outGoingRessource3, outGoingRessource4, outGoingRessource5, inLabel, tradeLabel, outLabel, countLabel, messageLabel;
    @FXML
    Label inComingRessource1, inComingRessource2, inComingRessource3, inComingRessource4, inComingRessource5;
    @FXML
    Label currentRessource1, currentRessource2, currentRessource3, currentRessource4, currentRessource5;
    @FXML
    Button plus1, plus2, plus3, plus4, plus5, minus1, minus2, minus3, minus4, minus5, cancelButton, finishButton;
    @FXML
    AnchorPane inAnchor, mainAnchor;

    public static boolean bool = true;
    public String harborType = "";
    Popup popup;
    private Player currentPlayer;
    private Controller controller;
    private int count;
    private int numOfCards;
    boolean dropMode = false;

    Logger logger = BoardView.getLogger();

    /**
     * set all the Labels with 0 value
     */
    @FXML
    public void initialize() {
        outGoingRessource1.setText("" + 0);
        outGoingRessource2.setText("" + 0);
        outGoingRessource3.setText("" + 0);
        outGoingRessource4.setText("" + 0);
        outGoingRessource5.setText("" + 0);
        inComingRessource1.setText("" + 0);
        inComingRessource2.setText("" + 0);
        inComingRessource3.setText("" + 0);
        inComingRessource4.setText("" + 0);
        inComingRessource5.setText("" + 0);
        currentRessource1.setStyle("fx-margin-top: -30px");
    }

    /**
     * dropMode is a method that makes some changes on the TradeWindow to make it available for
     * giving out cards cuz of Robber
     */
    public void dropMode(int numOfCards) {
        this.numOfCards = numOfCards;
        if (numOfCards % 2 == 0) {
            count = numOfCards / 2;
        } else {
            count = (numOfCards - 1) / 2;
        }

        hideInComingRessources();
        mainAnchor.setVisible(false);
        countLabel.setVisible(true);
        countLabel.setText(String.valueOf(count));
        countLabel.setStyle("-fx-font-size: 20px");
        messageLabel.setVisible(true);
        cancelButton.setDisable(true);
        disableAllPlusButtons();
        dropMode = true;
        if (dropMode) {
            finishButton.setDisable(true);
        }
    }

    /**
     * Trade window with harbor mode
     */
    public void harborMode(Color color) {
        bool = false;
        if (color.equals(Color.BLUE)) {
            this.harborType = ("3:1");
        } else if (color.equals(Color.YELLOW)) {
            this.harborType = ("GrainTrade");
        } else if (color.equals(Color.BROWN)) {
            this.harborType = ("LumberTrade");
        } else if (color.equals(Color.RED)) {
            this.harborType = ("BrickTrade");
        } else if (color.equals(Color.GRAY)) {
            this.harborType = ("OreTrade");
        } else if (color.equals(Color.WHITE)) {
            this.harborType = ("WoolTrade");
        } else {
            this.harborType = "4Trade";
        }
        finishButton.setDisable(true);
    }

    /**
     * droping specific number of cards and notify the server
     *
     * @param x
     */
    public void dropCards(int x) {
        int c1 = Integer.parseInt(outGoingRessource1.getText());
        int c2 = Integer.parseInt(outGoingRessource2.getText());
        int c3 = Integer.parseInt(outGoingRessource3.getText());
        int c4 = Integer.parseInt(outGoingRessource4.getText());
        int c5 = Integer.parseInt(outGoingRessource5.getText());
        Resource[] resources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        resources[0].setValue(Integer.parseInt(outGoingRessource2.getText()));
        resources[1].setValue(Integer.parseInt(outGoingRessource3.getText()));
        resources[2].setValue(Integer.parseInt(outGoingRessource5.getText()));
        resources[3].setValue(Integer.parseInt(outGoingRessource1.getText()));
        resources[4].setValue(Integer.parseInt(outGoingRessource4.getText()));

        if (x % 2 != 0) {
            x = x - 1;
        }
        if (c1 + c2 + c3 + c4 + c5 >= (x / 2)) {
            closeWindow();
            Platform.runLater(() -> controller.getClientController().sendResourceDrop(resources));
        }
    }

    /**
     * method for the finish Button
     */
    public void finishAction() {
        if (!dropMode && harborType.equals("")) {
            finishTrade();
            logger.info("finishTrade");
        } else if (!harborType.equals("")) {
            sendHarborOffer();
            logger.info("harborTrade");
        } else {
            dropCards(currentPlayer.getResourcesTotal());
        }
    }

    /**
     * set current ressources for the player
     */
    public void setCurrentRessources() {
        currentRessource1.setText("" + currentPlayer.getResource("grain").getValue());
        currentRessource2.setText("" + currentPlayer.getResource("lumber").getValue());
        currentRessource3.setText("" + currentPlayer.getResource("brick").getValue());
        currentRessource4.setText("" + currentPlayer.getResource("ore").getValue());
        currentRessource5.setText("" + currentPlayer.getResource("wool").getValue());
    }

    /**
     * method to handle the plus buttons
     *
     * @param in
     * @param out
     * @param current
     */
    public void plusButton(Label in, Label out, Label current, int resource) {
        if (!dropMode) {
            if (Integer.parseInt(out.getText()) > 0) {
                out.setText("" + (Integer.parseInt(out.getText()) - 1));
                current.setText("" + (Integer.parseInt(current.getText()) + 1));
                enablePlusMinusButtons(out, false);
            } else {
                in.setText("" + (Integer.parseInt(in.getText()) + 1));
                current.setText("" + (Integer.parseInt(current.getText()) + 1));
            }
            if (Integer.parseInt(in.getText()) == 1) {
                enableMinusButtons();
            }
            if (!bool && Integer.parseInt(in.getText()) == 1) {
                disableAllPlusButtons();
            }

            //4:1 Hafen, abschicken button enabled after 4 resources
            if (Integer.parseInt(out.getText()) == 3 && this.harborType.equals("4Trade")) {
                finishButton.setDisable(true);
            } else if (Integer.parseInt(out.getText()) == 2 && this.harborType.equals("3:1")) {
                finishButton.setDisable(true);
            }
            //2:1
            if (!harborType.equals("4Trade") && !harborType.equals("3:1") && !harborType.equals("") && Integer.parseInt(out.getText()) == 0) {
                finishButton.setDisable(false);
                disableAllMinusButtons();
                enableSpecificMinusButton(resource);
            }
            if (harborType.equals("4Trade")) {
                if (outGoingRessource1.getText().equals("4") || outGoingRessource2.getText().equals("4") || outGoingRessource3.getText().equals("4") || outGoingRessource4.getText().equals("4") || outGoingRessource5.getText().equals("4")) {
                    if (in.getText().equals("1")) {
                        finishButton.setDisable(false);
                    }
                }
            }
            if (harborType.equals("3:1")) {
                if (outGoingRessource1.getText().equals("3") || outGoingRessource2.getText().equals("3") || outGoingRessource3.getText().equals("3") || outGoingRessource4.getText().equals("3") || outGoingRessource5.getText().equals("3")) {
                    if (in.getText().equals("1")) {
                        finishButton.setDisable(false);
                    }
                }
            }

        } else {
            count++;
            countLabel.setText(String.valueOf(count));
            if (Integer.parseInt(out.getText()) > 0) {
                out.setText("" + (Integer.parseInt(out.getText()) - 1));
                current.setText("" + (Integer.parseInt(current.getText()) + 1));
            }
            if (Integer.parseInt(current.getText()) == 1) {
                enablePlusMinusButtons(out, false);
            }
            if (Integer.parseInt(out.getText()) == 0) {
                disablePlusMinusButtons(out, true);
            }
            if (count == 1) {
                enableMinusButtons();
                finishButton.setDisable(true);
            }
        }
    }

    /**
     * method to handle the minus buttons
     *
     * @param in
     * @param out
     * @param current
     */
    public void minusButton(Label in, Label out, Label current, int resource) {

        if (Integer.parseInt(current.getText()) > 0) {
            enablePlusMinusButtons(out, true);
            if (Integer.parseInt(in.getText()) > 0) {
                in.setText("" + (Integer.parseInt(in.getText()) - 1));
                current.setText("" + (Integer.parseInt(current.getText()) - 1));
            } else {
                out.setText("" + (Integer.parseInt(out.getText()) + 1));
                current.setText("" + (Integer.parseInt(current.getText()) - 1));
            }
            if (Integer.parseInt(current.getText()) == 0) {
                disablePlusMinusButtons(out, false);
            }
            if (dropMode) {
                count--;
                countLabel.setText("" + count);
            }
            if (count == 0 && dropMode) {
                disableAllMinusButtons();
                finishButton.setDisable(false);
            }
        }
        if (Integer.parseInt(out.getText()) == 4 && this.harborType.equals("4Trade")) {
            if (inComingRessource1.getText().equals("1") || inComingRessource2.getText().equals("1") || inComingRessource3.getText().equals("1") || inComingRessource4.getText().equals("1") || inComingRessource5.getText().equals("1")) {
                finishButton.setDisable(false);
            }
            disableAllMinusButtons();
        }
        if (Integer.parseInt(out.getText()) == 3 && this.harborType.equals("3:1")) {
            if (inComingRessource1.getText().equals("1") || inComingRessource2.getText().equals("1") || inComingRessource3.getText().equals("1") || inComingRessource4.getText().equals("1") || inComingRessource5.getText().equals("1")) {
                finishButton.setDisable(false);
            }
            disableAllMinusButtons();
        }
        if (Integer.parseInt(in.getText()) == 0) {
            enableAllPlusButtons();
        }
        if (!harborType.equals("4Trade") && !harborType.equals("3:1") && !harborType.equals("") && Integer.parseInt(in.getText()) == 0) {
            finishButton.setDisable(true);
            disableAllMinusButtons();
        }
    }

    /**
     * plus Grain Button to handle in/out-coming resources
     */
    public void plusGrainButton() {
        plusButton(inComingRessource1, outGoingRessource1, currentRessource1, 1);
    }

    /**
     * plus Lumber Button to handle in/out-coming resources
     */
    public void plusLumberButton() {
        plusButton(inComingRessource2, outGoingRessource2, currentRessource2, 2);
    }

    /**
     * plus Brick Button to handle in/out-coming resources
     */
    public void plusBrickButton() {
        plusButton(inComingRessource3, outGoingRessource3, currentRessource3, 3);

    }

    /**
     * plus Ore Button to handle in/out-coming ressources
     */
    public void plusOreButton() {
        plusButton(inComingRessource4, outGoingRessource4, currentRessource4, 4);

    }

    /**
     * plus Wool Button to handle in/out-coming ressources
     */
    public void plusWoolButton() {
        plusButton(inComingRessource5, outGoingRessource5, currentRessource5, 5);
    }

    /**
     * minus Grain Button to handle in/out-coming ressources
     */
    public void minusGrainButton() {
        minusButton(inComingRessource1, outGoingRessource1, currentRessource1, 1);
    }

    /**
     * minus Lumber Button to handle in/out-coming ressources
     */
    public void minusLumberButton() {
        minusButton(inComingRessource2, outGoingRessource2, currentRessource2, 2);
    }

    /**
     * minus Brick Button to handle in/out-coming ressources
     */
    public void minusBrickButton() {
        minusButton(inComingRessource3, outGoingRessource3, currentRessource3, 3);
    }

    /**
     * minus Ore Button to handle in/out-coming ressources
     */
    public void minusOreButton() {
        minusButton(inComingRessource4, outGoingRessource4, currentRessource4, 4);
    }

    /**
     * minus Wool Button to handle in/out-coming ressources
     */
    public void minusWoolButton() {
        minusButton(inComingRessource5, outGoingRessource5, currentRessource5, 5);
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void setPopup(Popup popup) {
        this.popup = popup;
    }

    /**
     * closes POPUP
     */
    public void closeWindow() {
        controller.getBoardViewController().resetHarborTradeButton();
        bool = true;
        this.popup.hide();
    }

    /**
     * after trade offer ,array for the outgoing and incoming cards an server senden
     */
    public void finishTrade() {
        int out1 = Integer.parseInt(outGoingRessource2.getText());
        int out2 = Integer.parseInt(outGoingRessource3.getText());
        int out3 = Integer.parseInt(outGoingRessource5.getText());
        int out4 = Integer.parseInt(outGoingRessource1.getText());
        int out5 = Integer.parseInt(outGoingRessource4.getText());

        int in1 = Integer.parseInt(inComingRessource2.getText());
        int in2 = Integer.parseInt(inComingRessource3.getText());
        int in3 = Integer.parseInt(inComingRessource5.getText());
        int in4 = Integer.parseInt(inComingRessource1.getText());
        int in5 = Integer.parseInt(inComingRessource4.getText());

        Resource[] outRessources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        outRessources[0].setValue(out1);
        outRessources[1].setValue(out2);
        outRessources[2].setValue(out3);
        outRessources[3].setValue(out4);
        outRessources[4].setValue(out5);

        Resource[] inRessources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        inRessources[0].setValue(in1);
        inRessources[1].setValue(in2);
        inRessources[2].setValue(in3);
        inRessources[3].setValue(in4);
        inRessources[4].setValue(in5);
        if ((out1 > 0 || out2 > 0 || out3 > 0 || out4 > 0 || out5 > 0)) {
            controller.setTradeWindowController(this);
            Platform.runLater(() -> controller.getClientController().sendDomesticTrade(outRessources, inRessources));
            closeWindow();
        } else {
            closeWindow();
        }
    }

    /**
     * checks if the Client did the right harborTrade, that he is allowed to do
     */
    public void sendHarborOffer() {
        sendOutIn();
        closeWindow();
        controller.getBoardViewController().resetHarborTradeButton();
    }

    /**
     *after choosing harbor trade , array for the outgoing and incoming cards an server senden
     */
    public void sendOutIn(){
        Resource[] outRessources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        Resource[] inRessources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
        outRessources[0].setValue(Integer.parseInt(outGoingRessource2.getText()));
        outRessources[1].setValue(Integer.parseInt(outGoingRessource3.getText()));
        outRessources[2].setValue(Integer.parseInt(outGoingRessource5.getText()));
        outRessources[3].setValue(Integer.parseInt(outGoingRessource1.getText()));
        outRessources[4].setValue(Integer.parseInt(outGoingRessource4.getText()));
        inRessources[0].setValue(Integer.parseInt(inComingRessource2.getText()));
        inRessources[1].setValue(Integer.parseInt(inComingRessource3.getText()));
        inRessources[2].setValue(Integer.parseInt(inComingRessource5.getText()));
        inRessources[3].setValue(Integer.parseInt(inComingRessource1.getText()));
        inRessources[4].setValue(Integer.parseInt(inComingRessource4.getText()));
        controller.getClientController().sendHarborTrade(NetworkTranslator.translateResourceObject(outRessources),NetworkTranslator.translateResourceObject(inRessources));
    }

    public void disableMinusButtonsOnStart() {
        if (Integer.parseInt(currentRessource1.getText()) == 0) {
            minus1.setDisable(true);
        }
        if (Integer.parseInt(currentRessource2.getText()) == 0) {
            minus2.setDisable(true);
        }
        if (Integer.parseInt(currentRessource3.getText()) == 0) {
            minus3.setDisable(true);
        }
        if (Integer.parseInt(currentRessource4.getText()) == 0) {
            minus4.setDisable(true);
        }
        if (Integer.parseInt(currentRessource5.getText()) == 0) {
            minus5.setDisable(true);
        }
    }

    private void disablePlusMinusButtons(Label out, boolean plus) {
        if (out == outGoingRessource1) {
            if (plus) {
                plus1.setDisable(true);
            } else {
                minus1.setDisable(true);
            }
        } else if (out == outGoingRessource2) {
            if (plus) {
                plus2.setDisable(true);
            } else {
                minus2.setDisable(true);
            }
        } else if (out == outGoingRessource3) {
            if (plus) {
                plus3.setDisable(true);
            } else {
                minus3.setDisable(true);
            }
        } else if (out == outGoingRessource4) {
            if (plus) {
                plus4.setDisable(true);
            } else {
                minus4.setDisable(true);
            }
        } else if (out == outGoingRessource5) {
            if (plus) {
                plus5.setDisable(true);
            } else {
                minus5.setDisable(true);
            }
        }
    }

    private void enablePlusMinusButtons(Label out, boolean plus) {
        if (out == outGoingRessource1) {
            if (plus) {
                plus1.setDisable(false);
            } else {
                minus1.setDisable(false);
            }
        } else if (out == outGoingRessource2) {
            if (plus) {
                plus2.setDisable(false);
            } else {
                minus2.setDisable(false);
            }
        } else if (out == outGoingRessource3) {
            if (plus) {
                plus3.setDisable(false);
            } else {
                minus3.setDisable(false);
            }
        } else if (out == outGoingRessource4) {
            if (plus) {
                plus4.setDisable(false);
            } else {
                minus4.setDisable(false);
            }
        } else if (out == outGoingRessource5) {
            if (plus) {
                plus5.setDisable(false);
            } else {
                minus5.setDisable(false);
            }
        }
    }

    private void disableAllPlusButtons() {
        plus1.setDisable(true);
        plus2.setDisable(true);
        plus3.setDisable(true);
        plus4.setDisable(true);
        plus5.setDisable(true);
    }
    private void disableAllMinusButtons() {
        minus1.setDisable(true);
        minus2.setDisable(true);
        minus3.setDisable(true);
        minus4.setDisable(true);
        minus5.setDisable(true);
    }

    private void enableAllPlusButtons() {
        plus1.setDisable(false);
        plus2.setDisable(false);
        plus3.setDisable(false);
        plus4.setDisable(false);
        plus5.setDisable(false);
    }

    private void enableSpecificMinusButton(int button) {
        switch (button) {
            case 1:
                minus1.setDisable(false);
                break;
            case 2:
                minus2.setDisable(false);
                break;
            case 3:
                minus3.setDisable(false);
                break;
            case 4:
                minus4.setDisable(false);
                break;
            case 5:
                minus5.setDisable(false);
                break;
        }
    }

    private void disableSpecificMinusButton(int button) {
        switch (button) {
            case 1:
                minus1.setDisable(true);
                break;
            case 2:
                minus2.setDisable(true);
                break;
            case 3:
                minus3.setDisable(true);
                break;
            case 4:
                minus4.setDisable(true);
                break;
            case 5:
                minus5.setDisable(true);
                break;
        }
    }

    private void enableMinusButtons() {
        if (Integer.parseInt(currentRessource1.getText()) > 0) {
            minus1.setDisable(false);
        }
        if (Integer.parseInt(currentRessource2.getText()) > 0) {
            minus2.setDisable(false);
        }
        if (Integer.parseInt(currentRessource3.getText()) > 0) {
            minus3.setDisable(false);
        }
        if (Integer.parseInt(currentRessource4.getText()) > 0) {
            minus4.setDisable(false);
        }
        if (Integer.parseInt(currentRessource5.getText()) > 0) {
            minus5.setDisable(false);
        }
    }
    private void hideInComingRessources(){
        inComingRessource1.setVisible(false);
        inComingRessource2.setVisible(false);
        inComingRessource3.setVisible(false);
        inComingRessource4.setVisible(false);
        inComingRessource5.setVisible(false);
    }

    public void initialize2HarborTrade() {
        System.out.println("HARBOR TYPE: " + harborType);
        switch (harborType) {
            case "GrainTrade":
                outGoingRessource1.setText(String.valueOf(2));
                currentRessource1.setText("" + (Integer.parseInt(currentRessource1.getText()) - 2));
                disableAllMinusButtons();
                break;
            case "LumberTrade":
                outGoingRessource2.setText(String.valueOf(2));
                currentRessource2.setText("" + (Integer.parseInt(currentRessource2.getText()) - 2));
                disableAllMinusButtons();
                break;
            case "BrickTrade":
                outGoingRessource3.setText(String.valueOf(2));
                currentRessource3.setText("" + (Integer.parseInt(currentRessource3.getText()) - 2));
                disableAllMinusButtons();
                break;
            case "OreTrade":
                outGoingRessource4.setText(String.valueOf(2));
                currentRessource4.setText("" + (Integer.parseInt(currentRessource4.getText()) - 2));
                disableAllMinusButtons();
                break;
            case "WoolTrade":
                outGoingRessource5.setText(String.valueOf(2));
                currentRessource5.setText("" + (Integer.parseInt(currentRessource5.getText()) - 2));
                disableAllMinusButtons();
                break;
        }
    }
}
