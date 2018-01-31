package view;

import com.google.gson.JsonArray;
import control.Controller;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.*;
import network.NetworkTranslator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import static model.Utility.coast;

/**
 * @author Cem, Karam
 */
public class BoardViewController {

    private PolygonXY[] landpiecePolygons;
    private CircleXY[] circles = new CircleXY[54];
    private Circle robber;
    private Board board;
    public BoardView view;
    private Player currentPlayer;
    private Controller controller;
    private Line[] streetLines;
    //for drag and drop events
    private double orgSceneX, orgSceneY, orgTranslateX, orgTranslateY;
    private int activePlayer;
    private Stage stage;
    private ArrayList<Label> diceNumbers = new ArrayList<>();
    private RectangleXY [] rectangles=new RectangleXY[30];
    private Logger logger= BoardView.getLogger();
    private boolean isPlayingKnightCard = false;
    private boolean isPlayingRoadCard = false;
    private ArrayList<JsonArray> roadBuilding = new ArrayList<>();
    private boolean firstRoadBuild = false;
    private Popup acceptedTradePopup = new Popup();
    private Popup runningPopUp = new Popup();
    private boolean robberMoving = false;
    private Popup waitingDroppingCardsPopup = new Popup();
    private ArrayList<Utility.Pair> firstRoadOfCard = new ArrayList<>();
    private int settlementsBuilt = 0;
    private int citysBuilt = 0;
    private int streetsBuilt = 0;
    private ArrayList<Player> players = new ArrayList<>();

    @FXML
    Pane boardOfCatan, windowPane;
    @FXML
    VBox playerInfoBox1, playerInfoBox2, playerInfoBox3, playerInfoBox4, playerBox, buttonBox;
    @FXML
    public Button settlementButton, sendButton, drawStreetsButton, finishMove, tradeButton, cityButton, harborButton, developmentCardButton, myDevCardsButton, musicButton;
    @FXML
    ImageView dice1, dice2, shipImage, tradeRoadBadgeP1, knightForceBadgeP1, tradeRoadBadgeP2, knightForceBadgeP2, tradeRoadBadgeP3, knightForceBadgeP3, tradeRoadBadgeP4, knightForceBadgeP4, musicImage;
    @FXML
    Label grainLabel, lumberLabel, brickLabel, woolLabel, oreLabel, labelName1, labelName2, labelName3, labelName4, victoryPoint, knightLabel, roadBuildingLabel, yearOfPlentyLabel, monopolyLabel;
    @FXML
    Label cardValuePlayer1, cardValuePlayer2, cardValuePlayer3, cardValuePlayer4, knightPlayer1, knightPlayer2, knightPlayer3, knightPlayer4, victoryPointsPlayer1, victoryPointsPlayer2, victoryPointsPlayer3, victoryPointsPlayer4;
    @FXML
    Label resourcesPlayer1, resourcesPlayer2, resourcesPlayer3, resourcesPlayer4;
    @FXML
    Label cardLabelPlayer1, resourcesLabelPlayer1, knightLabelPlayer1, victoryPointsLabelPlayer1;
    @FXML
    TextFlow chatWindow;
    @FXML
    TextField chatTextInput;
    @FXML
    ScrollPane scrollPane;

    /**
     * auto called method called when boardViewController is started
     */
    @FXML
    public void initialize() {
        windowPane.getStyleClass().add("pane");
        setEnterListener();
        chatWindow.getStyleClass().add("chatWindow");
        chatWindow.getChildren().addListener((ListChangeListener<Node>) ((change) -> scrollPane.setVvalue(1.0)));
        setVictoryPointLabel(0);
        setKnightLabel(0);
        setRoadBuildingLabel(0);
        setYearOfPlentyLabel(0);
        setMonopolyLabel(0);
        myDevCardsButton.setDisable(true);
        shipImage.setImage(new Image(getClass().getClassLoader().getResource("boat.png").toString()));
        shipImage.setVisible(false);
        tradeRoadBadgeP1.setImage(new Image(getClass().getClassLoader().getResource("traderoad.png").toString()));
        tradeRoadBadgeP2.setImage(new Image(getClass().getClassLoader().getResource("traderoad.png").toString()));
        tradeRoadBadgeP3.setImage(new Image(getClass().getClassLoader().getResource("traderoad.png").toString()));
        knightForceBadgeP1.setImage(new Image(getClass().getClassLoader().getResource("knightForce.png").toString()));
        knightForceBadgeP2.setImage(new Image(getClass().getClassLoader().getResource("knightForce.png").toString()));
        knightForceBadgeP3.setImage(new Image(getClass().getClassLoader().getResource("knightForce.png").toString()));
        tradeRoadBadgeP4.setImage(new Image(getClass().getClassLoader().getResource("traderoad.png").toString()));
        knightForceBadgeP4.setImage(new Image(getClass().getClassLoader().getResource("knightForce.png").toString()));


    }

    /**
     * setter for stage
     * @param stage window parameter
     */
    void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * HexagonField drawn as polygons in specific type of landpieces
     * @param landpieces type of landpiece
     */
    public void fillPane(Landpiece[][] landpieces) {

        landpiecePolygons = new PolygonXY[19];
        byte count = 0;
        int xVector = 120;
        int yVector = 40;
        int start = 0;
        for (byte y = 0; y < 5; y++) {
            for (byte x = 0; x < 5; x++) {
                if (landpieces[x][y] != null) {

                    landpiecePolygons[count] = new PolygonXY(x, y);

                    if (count < 3) {
                        start = 240;
                    } else if (count < 7) {
                        start = 180;
                        yVector = 140;
                    } else if (count < 12) {
                        start = 120;
                        yVector = 240;
                    } else if (count < 16) {
                        start = 60;
                        yVector = 340;
                    } else if (count < 20) {
                        start = 0;
                        yVector = 440;
                    }

                    landpiecePolygons[count].getPoints().addAll(
                            0.0 + xVector * x + start, 40.0 + yVector,
                            60.0 + xVector * x + start, 0.0 + yVector,
                            120.0 + xVector * x + start, 40.0 + yVector,
                            120.0 + xVector * x + start, 100.0 + yVector,
                            60.0 + xVector * x + start, 140.0 + yVector,
                            0.0 + xVector * x + start, 100.0 + yVector);
                    landpiecePolygons[count].setStrokeWidth(1.0);
                    landpiecePolygons[count].setStroke(Color.GRAY);


                    if (landpieces[x][y] != null && landpieces[x][y].getResourceType() == LandpieceType.DESERT) {
                        Image image = new Image(getClass().getClassLoader().getResource("desert.png").toString());
                        ImagePattern pattern = new ImagePattern(image);
                        landpiecePolygons[count].setFill(pattern);
                    } else if (landpieces[x][y] != null && landpieces[x][y].getResourceType() == LandpieceType.FOREST) {
                        Image image = new Image(getClass().getClassLoader().getResource("forest.png").toString());
                        ImagePattern pattern = new ImagePattern(image);
                        landpiecePolygons[count].setFill(pattern);
                    } else if (landpieces[x][y] != null && landpieces[x][y].getResourceType() == LandpieceType.FIELDS) {
                        Image image = new Image(getClass().getClassLoader().getResource("fields.png").toString());
                        ImagePattern pattern = new ImagePattern(image);
                        landpiecePolygons[count].setFill(pattern);
                    } else if (landpieces[x][y] != null && landpieces[x][y].getResourceType() == LandpieceType.PASTURES) {
                        Image image = new Image(getClass().getClassLoader().getResource("pasture.png").toString());
                        ImagePattern pattern = new ImagePattern(image);
                        landpiecePolygons[count].setFill(pattern);
                    } else if (landpieces[x][y] != null && landpieces[x][y].getResourceType() == LandpieceType.HILLS) {
                        Image image = new Image(getClass().getClassLoader().getResource("hills.png").toString());
                        ImagePattern pattern = new ImagePattern(image);
                        landpiecePolygons[count].setFill(pattern);
                    } else if (landpieces[x][y] != null && landpieces[x][y].getResourceType() == LandpieceType.MOUNTAINS) {
                        Image image = new Image(getClass().getClassLoader().getResource("mountains.png").toString());
                        ImagePattern pattern = new ImagePattern(image);
                        landpiecePolygons[count].setFill(pattern);
                    }

                    boardOfCatan.getChildren().addAll(landpiecePolygons[count]);

                    //avoids setting dice number on desert
                    if (landpieces[x][y].getDiceNumber() != 0) {
                        if (landpieces[x][y].getDiceNumber() == 6 || landpieces[x][y].getDiceNumber() == 8) {
                            setDiceNumberLabel(landpiecePolygons[count], landpieces[x][y].getDiceNumber(), Color.RED);
                        } else {
                            setDiceNumberLabel(landpiecePolygons[count], landpieces[x][y].getDiceNumber(), Color.BLACK);
                        }
                    }
                    count++;
                }
            }
        }
        setRobber();
        setResourceNum();
        buttonBox.setAlignment(Pos.TOP_RIGHT);
    }

    /**
     * Sending a message with pressing the enter button
     */
    private void setEnterListener() {
        chatTextInput.setOnKeyReleased((keyEvent) -> {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        sendButtonPressed();
                    }
                }
        );
    }


    /**
     * Sets a Label to show the resource quantity for the current player
     */
    public void setResourceNum() {
        grainLabel.setText("" + currentPlayer.getResource("grain").getValue());
        lumberLabel.setText("" + currentPlayer.getResource("lumber").getValue());
        brickLabel.setText("" + currentPlayer.getResource("brick").getValue());
        woolLabel.setText("" + currentPlayer.getResource("wool").getValue());
        oreLabel.setText("" + currentPlayer.getResource("ore").getValue());
    }

    /**
     * Showing a total resources counter in the playerInfoBox
     */
    public void updatePlayerInfoLabels() {
        resourcesPlayer1.setText("" + controller.getPlayers().get(0).getResourcesTotal());
        resourcesPlayer2.setText("" + controller.getPlayers().get(1).getResourcesTotal());
        resourcesPlayer3.setText("" + controller.getPlayers().get(2).getResourcesTotal());

        if (controller.getPlayers().get(0) == currentPlayer) {
            victoryPointsPlayer1.setText("" + controller.getPlayers().get(0).getVictoryPoints());
        } else {
            victoryPointsPlayer1.setText("" + controller.getPlayers().get(0).getVictoryPointsForOthers());
        }
        if (controller.getPlayers().get(1) == currentPlayer) {
            victoryPointsPlayer2.setText("" + controller.getPlayers().get(1).getVictoryPoints());
        } else {
            victoryPointsPlayer2.setText("" + controller.getPlayers().get(1).getVictoryPointsForOthers());
        }
        if (controller.getPlayers().get(2) == currentPlayer) {
            victoryPointsPlayer3.setText("" + controller.getPlayers().get(2).getVictoryPoints());
        } else {
            victoryPointsPlayer3.setText("" + controller.getPlayers().get(2).getVictoryPointsForOthers());
        }

        knightPlayer1.setText("" + controller.getPlayers().get(0).getKnightForce());
        knightPlayer2.setText("" + controller.getPlayers().get(1).getKnightForce());
        knightPlayer3.setText("" + controller.getPlayers().get(2).getKnightForce());

        cardValuePlayer1.setText("" + controller.getPlayers().get(0).getDevCards().size());
        cardValuePlayer2.setText("" + controller.getPlayers().get(1).getDevCards().size());
        cardValuePlayer3.setText("" + controller.getPlayers().get(2).getDevCards().size());
        //if there are 4 players

        if(controller.getPlayers().size() > 3) {
            cardValuePlayer4.setText("" + controller.getPlayers().get(3).getDevCards().size());
            knightPlayer4.setText("" + controller.getPlayers().get(3).getKnightForce());
            resourcesPlayer4.setText("" + controller.getPlayers().get(3).getResourcesTotal());

            if (controller.getPlayers().get(3) == currentPlayer) {
                victoryPointsPlayer4.setText("" + controller.getPlayers().get(3).getVictoryPoints());
            } else {
                victoryPointsPlayer4.setText("" + controller.getPlayers().get(3).getVictoryPointsForOthers());
            }

        }
    }
    /**
     * Setting the ID of the active player
     */
    public void setActivePlayer(int activePlayer) {
        this.activePlayer = activePlayer;
    }

    /**
     * Setting a dropShadow effect on the playerBox for the active player, and a blur effect for inactive players
     */
    public void setActivePlayerEffect() {
        VBox[] boxes = {playerInfoBox1, playerInfoBox2, playerInfoBox3, playerInfoBox4};

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.GRAY);
        dropShadow.setOffsetX(10);
        dropShadow.setOffsetY(10);
        for (int i = 0; i < controller.getPlayers().size(); i++) {
            if (controller.getPlayers().get(i).getId() == activePlayer) {
                boxes[i].setEffect(dropShadow);
                //playerImages[i].setEffect(null);
            } else {
                boxes[i].setEffect(null);
                //playerImages[i].setEffect(blurImage);
            }
        }
    }
    /**
     * Labels were set on random fields as diceNumbers
     * @param pol inputs polygon for coordinates
     * @param i input of a number
     * @param color getting color to highlight important labels
     */
    private void setDiceNumberLabel(Polygon pol, int i, Color color) {
        ObservableList<Double> points = pol.getPoints();

        double x = points.get(0);
        double y = points.get(1);

        double centerX = x + 40.0;
        double centerY = y + 12.0;

        Label diceNumber = new Label(Integer.toString(i));
        diceNumber.setMouseTransparent(true);
        diceNumber.setLayoutX(centerX);
        diceNumber.setLayoutY(centerY);
        diceNumber.setMinWidth(40);
        diceNumber.setMinHeight(40);
        diceNumber.getStyleClass().add("diceNumberLabel");
        diceNumber.setTextFill(color);
        diceNumbers.add(diceNumber);

        boardOfCatan.getChildren().add(diceNumber);
    }

    /**
     * Method to draw available streets
     */
    public void drawStreets() {
        ArrayList<Street> streets = board.getStreets();
        for (Street street : streets) {

            Line line = new Line();
            if (street.getOwner() != null) {
                line.setStroke(street.getOwner().getColor());
            } else {
                line.setStroke(Color.LIGHTGRAY);
            }

            line.setStrokeWidth(6.5);

            for (CircleXY c : circles) {
                if (c.getCoordinates().equals(street.getA().getCoordinates())) {
                    line.setStartX(c.getCenterX());
                    line.setStartY(c.getCenterY());
                }
            }

            for (CircleXY c : circles) {
                if (c.getCoordinates().equals(street.getB().getCoordinates())) {
                    line.setEndX(c.getCenterX());
                    line.setEndY(c.getCenterY());
                }
            }

            boardOfCatan.getChildren().add(line);
        }
        for (CircleXY circle : circles) {
            circle.toFront();
        }
    }

    /**
     * Handles enabling/disabling for the each buttons
     */
    public void checkButtonEnabled() {
        if (currentPlayer.getState().equals("Warten")) {
            settlementButton.setDisable(true);
            drawStreetsButton.setDisable(true);
            finishMove.setDisable(true);
            tradeButton.setDisable(true);
            cityButton.setDisable(true);
            harborButton.setDisable(true);
            developmentCardButton.setDisable(true);
            myDevCardsButton.setDisable(true);
        } else if (currentPlayer.getState().equals("Handeln oder Bauen")) {
            finishMove.setDisable(false);
            tradeButton.setDisable(false);
            myDevCardsButton.setDisable(false);

            // Street
            if (currentPlayer.getResource("lumber").getValue() > 0 &&
                    currentPlayer.getResource("brick").getValue() > 0 && streetsBuilt < 15) {
                drawStreetsButton.setDisable(false);
            } else {
                drawStreetsButton.setDisable(true);
            }

            // Settlement
            if (currentPlayer.getResource("brick").getValue() > 0 &&
                    currentPlayer.getResource("lumber").getValue() > 0 &&
                    currentPlayer.getResource("wool").getValue() > 0 &&
                    currentPlayer.getResource("grain").getValue() > 0 && settlementsBuilt < 5) {
                settlementButton.setDisable(false);
            } else {
                settlementButton.setDisable(true);
            }

            // City
            if (currentPlayer.getResource("grain").getValue() > 1 &&
                    currentPlayer.getResource("ore").getValue() > 2 && citysBuilt < 4) {
                cityButton.setDisable(false);
            }

            // development card
            if (currentPlayer.getResource("grain").getValue() >= 1 && currentPlayer.getResource("wool").getValue() >= 1
                    && currentPlayer.getResource("ore").getValue() >= 1 && !controller.getClientController().isDevCardBoughtInThisRound()
                    ) {
                developmentCardButton.setDisable(false);
            }

            // harbor trade
            if (currentPlayer.getResource("lumber").getValue() >= 2 && currentPlayer.checkIfPlayerHasHarbor(ResourceType.LUMBER)|| currentPlayer.getResource("brick").getValue() >= 2 && currentPlayer.checkIfPlayerHasHarbor(ResourceType.BRICK) || currentPlayer.getResource("wool").getValue() >= 2 && currentPlayer.checkIfPlayerHasHarbor(ResourceType.WOOL) ||
                    currentPlayer.getResource("grain").getValue() >= 2  && currentPlayer.checkIfPlayerHasHarbor(ResourceType.GRAIN)|| currentPlayer.getResource("ore").getValue() >= 2 && currentPlayer.checkIfPlayerHasHarbor(ResourceType.ORE) ) {
                harborButton.setDisable(false);
            } else if (currentPlayer.getResource("lumber").getValue() > 2 && currentPlayer.checkIfPlayerHasHarbor(null) || currentPlayer.getResource("brick").getValue() > 2 && currentPlayer.checkIfPlayerHasHarbor(null) || currentPlayer.getResource("wool").getValue() > 2 && currentPlayer.checkIfPlayerHasHarbor(null) || currentPlayer.getResource("grain").getValue() > 2 && currentPlayer.checkIfPlayerHasHarbor(null) || currentPlayer.getResource("ore").getValue() > 2 && currentPlayer.checkIfPlayerHasHarbor(null)) {
                harborButton.setDisable(false);
            } else if (currentPlayer.getResource("lumber").getValue() > 3 || currentPlayer.getResource("brick").getValue() > 3 || currentPlayer.getResource("wool").getValue() > 3 || currentPlayer.getResource("grain").getValue() > 3 || currentPlayer.getResource("ore").getValue() > 3) {
                harborButton.setDisable(false);
            } else {
                harborButton.setDisable(true);
            }
        }
    }


    /**
     * This method draws the viable streets
     * @param initial if in the first tow rows, than the viable streets are calculated different
     */
    public boolean drawViableStreets(boolean initial) {
        ArrayList<Street> viableStreets;
        // initial = first two rounds, than street can only be build on the last build settlement
        if (initial) {
            viableStreets = board.getViableStreetsInitial(currentPlayer);
        } else {
            if(!firstRoadBuild) {
                viableStreets = board.getViableStreets(currentPlayer.getStreets());
                if (viableStreets.size() == 0) {
                    return false;
                }
            }
            // if Road card and second street, calculate viable streets from temporary array with first road
            else {
                ArrayList<Street> tmpStreets = currentPlayer.getStreets();
                tmpStreets.add(new Street(board.getIntersections()[firstRoadOfCard.get(0).getX()][firstRoadOfCard.get(0).getY()],
                        board.getIntersections()[firstRoadOfCard.get(1).getX()][firstRoadOfCard.get(1).getY()]));
                viableStreets = board.getViableStreets(tmpStreets);


            }
        }

        // generates lines for all viable streets
        streetLines = new Line[viableStreets.size()];
        int k = 0;
        for (Street street : viableStreets) {
            double x = 0;
            double y = 0;
            double z = 0;
            double w = 0;
            // get the circleXY on first intersection
            for (CircleXY c : circles) {
                if (c.getCoordinates().equals(street.getA().getCoordinates())) {
                    x = c.getCenterX();
                    y = c.getCenterY();
                }
            }
            // get the circleXY on second intersection
            for (CircleXY c1 : circles) {
                if (c1.getCoordinates().equals(street.getB().getCoordinates())) {
                    z = c1.getCenterX();
                    w = c1.getCenterY();
                }
            }
            Line line = new Line();
            line.setStartX(x);
            line.setStartY(y);
            line.setEndX(z);
            line.setEndY(w);
            line.setStrokeWidth(6.5);
            line.setStroke(Color.LIGHTGRAY);

            // saved first street of road building card, because street is still available and gets green otherwise
            if (firstRoadOfCard.size() > 0) {
                if (street.getA().getCoordinates().equals(firstRoadOfCard.get(0)) && street.getB().getCoordinates().equals(firstRoadOfCard.get(1))
                        || street.getA().getCoordinates().equals(firstRoadOfCard.get(1)) && street.getB().getCoordinates().equals(firstRoadOfCard.get(0))) {
                    line.setStroke(currentPlayer.getColor());
                }
            }

            streetLines[k] = line;

            k++;
            boardOfCatan.getChildren().add(line);
            for (CircleXY circle : circles) {
                circle.toFront();
            }

            line.setOnMouseClicked(event -> {
                if (!initial && !isPlayingRoadCard) {
                    streetButtonOnAction();
                }
                stopStreetClickListeners();

                if (!isPlayingRoadCard) {
                    controller.getClientController().sendBuildRequest("Straße", NetworkTranslator.translateStreetCoordinate(new Utility.Pair[]{street.getA().getCoordinates(), street.getB().getCoordinates()}));
                }
                // road card
                else {
                    logger.info("oh you're playing a road card");
                    if (!firstRoadBuild) {
                        logger.info("first street");
                        roadBuilding.add(NetworkTranslator.translateStreetCoordinate(new Utility.Pair[]{street.getA().getCoordinates(), street.getB().getCoordinates()}));
                        firstRoadBuild = true;
                        firstRoadOfCard.add(street.getA().getCoordinates());
                        firstRoadOfCard.add(street.getB().getCoordinates());
                        if (!drawViableStreets(false)) {
                            sentMessage("Du kannst leider nur eine Straße bauen.");
                            controller.getClientController().sendRoadBuildingCard(roadBuilding.get(0), null);
                        }
                    }
                    else {
                        logger.info("second street");
                        roadBuilding.add(NetworkTranslator.translateStreetCoordinate(new Utility.Pair[]{street.getA().getCoordinates(), street.getB().getCoordinates()}));
                        firstRoadBuild = false;
                        controller.getClientController().sendRoadBuildingCard(roadBuilding.get(0), roadBuilding.get(1));
                        roadBuilding = new ArrayList<>();
                        firstRoadOfCard = new ArrayList<>();
                        isPlayingRoadCard = false;
                    }
                }
            });
        }
        return true;
    }


    /**
     * switches between "STRASSE" and "ABBRECHEN"
     */
    @FXML
    private void streetButtonOnAction(){
        if(!drawStreetsButton.isCancelButton()){
            drawStreetsButton.setText("ABBRECHEN");
            drawStreetsButton.setCancelButton(true);
            drawViableStreets(false);
        }else {
            stopStreetClickListeners();
            drawStreetsButton.setCancelButton(false);
            this.drawStreetsButton.setText("STRASSE");
        }
    }


    /**
     * After setting a street, the unnecessary ones will be hidden
     */
    private void stopStreetClickListeners() {
        System.out.println("stop listener");
        for (Line l : streetLines) {
            l.setOnMouseClicked(null);
            l.setStroke(Color.TRANSPARENT);

        }
    }

    /**
     * Generates CircleXY objects on every intersection
     */
    public void drawIntersections() {
        int j = 0;
        int count = 0;
        int x = 0, y = 0;
        double startX = 240.0;
        double startY = 80.0;
        while (x != -1) {
            circles[count] = new CircleXY(x, y);
            circles[count].setCenterX(startX);
            circles[count].setCenterY(startY);
            circles[count].setRadius(10.0);
            circles[count].setFill(Color.TRANSPARENT);

            j++;
            x++;
            boardOfCatan.getChildren().add(circles[count]);
            count++;
            if (count == 54)
                x = -1;
            if (count <= 27) {
                if (count == 7) {
                    x = 0;
                    y += 1;
                    startX = 120;
                    startY = 140;
                    j = 0;
                }
                if (count == 16) {
                    x = 0;
                    y += 1;
                    startX = 60;
                    startY = 240;
                    j = 0;
                }
                if (count == 27) {
                    x = 1;
                    y = 3;
                    startX = 60;
                    startY = 300;
                    j = 0;
                }
                if (j % 2 == 0) {
                    startX += 60;
                    startY += 40;
                } else {
                    startX += 60;
                    startY -= 40;
                }
            } else if (count > 27 && count < 54) {

                if (count == 38) {
                    x = 3;
                    y += 1;
                    startX = 120;
                    startY = 480;
                    j = 0;
                }
                if (count == 47) {
                    x = 5;
                    y += 1;
                    startX = 180;
                    startY = 580;
                    j = 0;
                }
                if (j % 2 == 0) {
                    startX += 60;
                    startY -= 40;

                } else {
                    startX += 60;
                    startY += 40;

                }
            }
        }
        setHarbors();
    }

    /**
     * draws the right color for settlements and citys
     */
    public void drawSettlements(){
        for(CircleXY circle : circles){
            if(board.getIntersections()[circle.getX()][circle.getY()].getSettlement() != null){
                // city
                if(board.getIntersections()[circle.getX()][circle.getY()].getSettlement().isCity()){
                    circle.setRadius(15.0);
                    circle.setStroke(Color.GRAY);
                    circle.setStrokeWidth(2.5);
                    circle.setFill(board.getIntersections()[circle.getX()][circle.getY()].getOwner().getColor());
                }
                // settlement
                else {
                    circle.setRadius(10.0);
                    circle.setFill(board.getIntersections()[circle.getX()][circle.getY()].getOwner().getColor());
                }
            }else {
                circle.setFill(Color.TRANSPARENT);
            }
        }
    }

    /**
     * Showing available intersections to build the first and second settlements
     * @param initial equals true, for the first and second round
     */
    public void drawViableSettlements(boolean initial) {
        ArrayList<Intersection> viableIntersections;
        if (initial) {
            viableIntersections = board.getViableSettlementsInitial();
        } else {
            this.settlementButton.setText("ABBRECHEN");
            viableIntersections = board.getViableSettlements(currentPlayer.getStreets());
        }
        if (!initial) {
            cancelIntersection();
        }
        for (Intersection in : viableIntersections) {
            for (CircleXY c : circles) {
                if (c.getCoordinates().equals(in.getCoordinates())) {
                    c.setFill(Color.GRAY);
                    c.toFront();
                    c.setOnMouseReleased((event) -> {
                        if (!initial) {
                            this.settlementButton.setText("SIEDLUNG");
                        }
                        controller.getClientController().sendBuildRequest("Dorf", NetworkTranslator.translatePairArray(NetworkTranslator.translateIntersectionCoordinateToLandpieces(new Utility.Pair(c.getX(), c.getY()))));
                        stopClickListeners();
                    });
                }
            }
        }
    }

    /**
     * Calling drawViableSettlements method with initial set "false"
     */
    @FXML
    public void drawViableSettlements() {
        drawViableSettlements(false);
    }

    /**
     * Drawing a city on settlements already set by an owner
     */
    @FXML
    public void drawViableCity() {
        for (Settlement settlement : currentPlayer.getSettlements()) {
            for (CircleXY circleElement : circles) {
                if (circleElement.getCoordinates().equals(settlement.getCoordinates())) {
                    circleElement.toFront();
                    circleElement.setRadius(15.0);
                    circleElement.setOnMouseReleased((clickEvent) -> {
                        this.cityButton.setText("STADT");
                        controller.getClientController().sendBuildRequest("Stadt", NetworkTranslator.translatePairArray(NetworkTranslator.translateIntersectionCoordinateToLandpieces(circleElement.getCoordinates())));
                        stopClickListeners();

                        this.cityButton.setText("STADT");

                        for (Settlement st : currentPlayer.getSettlements()) {
                            for (CircleXY cl : circles) {
                                if (cl.getCoordinates().equals(st.getCoordinates())) {
                                    if(!st.isCity()){
                                        cl.setRadius(10.0);
                                    }
                                }
                            }
                        }

                    });
                }
            }
        }
        cancelCity();

    }

    /**
     * ClickListener to cancel building a city
     */
    public void cancelCity(){
        cityButton.setText("ABBRECHEN");
        this.cityButton.setOnMouseReleased(event -> {
            this.cityButton.setText("STADT");

            for (Settlement settlement : currentPlayer.getSettlements()) {
                for (CircleXY circleElement : circles) {
                    if (circleElement.getCoordinates().equals(settlement.getCoordinates())) {
                        if(!settlement.isCity()){
                            circleElement.setRadius(10.0);
                        }
                    }
                }
            }

            stopClickListeners();
            this.cityButton.setOnMouseReleased(event1 ->drawViableCity());
            }
        );
    }

    /**
     * ClickListener to cancel building a settlement
     */
    private void cancelIntersection() {
        settlementButton.setText("ABBRECHEN");
        this.settlementButton.setOnMouseReleased(event -> {
            stopClickListeners();
            this.settlementButton.setText("SIEDLUNG");
            this.settlementButton.setOnMouseReleased(event1 -> drawViableSettlements(false));
        });
    }

    /**
     * ClickListener to cancel action
     */
    private void stopClickListeners() {
        System.out.println("stopClickListeners");
        for (CircleXY c : circles) {
            c.setOnMouseReleased(null);
              if(board.getIntersections()[c.getX()][c.getY()].getSettlement() == null){
                  c.setFill(Color.TRANSPARENT);
            }
        }
    }

    /**
     * setting the board object
     *
     * @param b object of board class
     */
    public void setBoard(Board b) {
        if (this.board == null) {
            this.board = b;
        }
    }

    /**
     * Moves the robber; needs the pair of a landpiece, listens for the click on a settlement
     */
    public void moveRobber() {
        robberMoving = true;
        //Popup for Client
        moveRobberPopup();
        try{
            setLandpieceDropReleaseListener();
        }catch (Exception e){
            logger.log(Level.SEVERE, e.getMessage());
        }

        robber.toFront();
        sentMessage("Ziehe den Räuber auf ein anderes Feld");

        robber.setOnMousePressed((event) -> {
            orgSceneX = event.getSceneX();
            orgSceneY = event.getSceneY();
            orgTranslateX = ((Circle) (event.getSource())).getTranslateX();
            orgTranslateY = ((Circle) (event.getSource())).getTranslateY();

            robber.setMouseTransparent(true);
            event.setDragDetect(true);
        });

        robber.setOnMouseDragged((event) -> {
            double offsetX = event.getSceneX() - orgSceneX;
            double offsetY = event.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;

            ((Circle) (event.getSource())).setTranslateX(newTranslateX);
            ((Circle) (event.getSource())).setTranslateY(newTranslateY);
        });

        robber.setOnDragDetected((event) -> robber.startFullDrag());

        robber.setOnMouseReleased((event) -> {
            robber.setMouseTransparent(false);
            robber.setOnMousePressed(null);
            robber.setOnMouseDragged(null);
            robber.setOnDragDetected(null);
            event.consume();

            if(robberMoving) {
                moveRobber();
            }
        });
    }

    /**
     * adds a release listener to the landpieces (except the one with the robber)
     */
    private void setLandpieceDropReleaseListener() {
        for (PolygonXY landpiece : landpiecePolygons) {
            if (!board.getLandpieces()[landpiece.getCoordinates().getX()][landpiece.getCoordinates().getY()].isHoldingRobber()) {
                landpiece.setOnMouseDragReleased((event) -> {
                    logger.info("robber released on: " + landpiece.getCoordinates());
                    robberMoving = false;
                    boolean hasPlayerToStealFrom = false;
                    Intersection[] neighbouringIntersections = board.getNeighbouringIntersections(landpiece.getCoordinates());
                    for (Intersection neighbouringIntersection : neighbouringIntersections) {
                        // highlighting the surrounding settlements/cities (except own buildings)
                        // if no match, than hasPlayerToStealFrom stays false
                        for (CircleXY circleElement : circles) {
                            if (board.getIntersections()[circleElement.getX()][circleElement.getY()].getSettlement() != null && circleElement.getCoordinates().equals(neighbouringIntersection.getCoordinates()) && neighbouringIntersection.getOwner() != currentPlayer) {
                                sentMessage("Wähle den Mitspieler, den du beklauen möchtest");
                                hasPlayerToStealFrom = true;

                                //highlight the buildings to choose a player to steal from
                                circleElement.toFront();
                                circleElement.setRadius(20.0);

                                // clickListener on neighboring circles
                                circleElement.setOnMouseReleased((clickEvent) -> {
                                    logger.info("player to steel from: " + board.getIntersections()[circleElement.getX()][circleElement.getY()].getOwner());

                                    // send message to the server, depending on knight card or "normal" robber movement
                                    if (!isPlayingKnightCard) {
                                        controller.getClientController().sendMoveRobber(NetworkTranslator.translateLandpieceCoordinateToProtocol(landpiece.getCoordinates()), board.getIntersections()[circleElement.getX()][circleElement.getY()].getOwner().getId());
                                    } else {
                                        controller.getClientController().sendPlayKnightCard(NetworkTranslator.translateLandpieceCoordinateToProtocol(landpiece.getCoordinates()), board.getIntersections()[circleElement.getX()][circleElement.getY()].getOwner().getId());
                                        isPlayingKnightCard = false;
                                    }

                                    // resets the settlement/ city highlights
                                    drawSettlements();
                                    stopClickListeners();

                                    clickEvent.consume();
                                });
                            }
                        }
                    }
                    // robber placed on a landpiece without foreign building
                    // send message to the server, depending on knight card or "normal" robber movement
                    if (!hasPlayerToStealFrom) {
                        logger.info("no player to steel from");
                        if(!isPlayingKnightCard) {
                            controller.getClientController().sendMoveRobber(NetworkTranslator.translateLandpieceCoordinateToProtocol(landpiece.getCoordinates()), -1);
                        }else {
                            controller.getClientController().sendPlayKnightCard(NetworkTranslator.translateLandpieceCoordinateToProtocol(landpiece.getCoordinates()), -1);
                            isPlayingKnightCard = false;
                        }
                    }
                    event.consume();
                });
            }
        }
    }

    /**
     * Drawing the robber on the field which is holding the robber
     */
    public void setRobber() {
        for (PolygonXY landpiece : landpiecePolygons) {
            landpiece.setOnMouseDragReleased(null);
        }

        Utility.Pair robberPair = board.getLandpieceWithActiveRobberCoordinates();

        PolygonXY pol = new PolygonXY(0, 0);
        for (PolygonXY poly : landpiecePolygons) {
            if (robberPair.equals(poly.getCoordinates())) {
                pol = poly;
            }
        }
        ObservableList<Double> points = pol.getPoints();
        Image rob = new Image(getClass().getClassLoader().getResource("robber.png").toString());
        rob.isPreserveRatio();
        ImagePattern robberImage = new ImagePattern(rob);

        double centerX = points.get(0) + 60.0;
        double centerY = points.get(1) + 30.0;

        boolean newRobber = false;
        if (robber == null) {
            robber = new Circle();
            robber.setRadius(40);
            robber.setCenterX(centerX);
            robber.setCenterY(centerY);
            robber.setFill(robberImage);
            newRobber = true;

        } else {
            robber.setTranslateX(centerX - robber.getCenterX());
            robber.setTranslateY(centerY - robber.getCenterY());
        }

        if (newRobber) {
            boardOfCatan.getChildren().add(robber);
        }

        for (Label dice : diceNumbers) {
            dice.toFront();
        }


    }

    /**
     * This method sets the harbors and calls getHarborColor to get a specific color for harbors
     */
    private void setHarbors() {
        Utility.Pair[] coor = new Utility.Pair[]{
                new Utility.Pair(235, 33), new Utility.Pair(310, 35), new Utility.Pair(350, 35), new Utility.Pair(430, 35), new Utility.Pair(480, 30),
                new Utility.Pair(545, 33), new Utility.Pair(587, 92), new Utility.Pair(610, 130), new Utility.Pair(647, 192), new Utility.Pair(673, 235),
                new Utility.Pair(707, 295), new Utility.Pair(673, 355), new Utility.Pair(647, 395), new Utility.Pair(610, 455), new Utility.Pair(587, 495),
                new Utility.Pair(553, 555), new Utility.Pair(475, 555), new Utility.Pair(430, 555), new Utility.Pair(350, 555), new Utility.Pair(310, 555),
                new Utility.Pair(235, 555), new Utility.Pair(200, 495), new Utility.Pair(175, 455), new Utility.Pair(137, 390), new Utility.Pair(115, 355),
                new Utility.Pair(80, 295), new Utility.Pair(115, 235), new Utility.Pair(137, 192), new Utility.Pair(175, 135), new Utility.Pair(200, 92)
        };
        //getting Images for rectangle fills

        ImagePattern[] imagePattern = new ImagePattern[6];
        Image grainHarbor = new Image(getClass().getClassLoader().getResource("grainHarbor.png").toString());
        imagePattern[0] = new ImagePattern(grainHarbor);
        Image lumberHarbor = new Image(getClass().getClassLoader().getResource("lumberHarbor.png").toString());
        imagePattern[1] = new ImagePattern(lumberHarbor );
        Image brickHarbor= new Image(getClass().getClassLoader().getResource("brickHarbor.png").toString());
        imagePattern[2] = new ImagePattern(brickHarbor);
        Image woolHarbor = new Image(getClass().getClassLoader().getResource("woolHarbor.png").toString());
        imagePattern[3] = new ImagePattern(woolHarbor);
        Image oreHarbor = new Image(getClass().getClassLoader().getResource("oreHarbor.png").toString());
        imagePattern[4] = new ImagePattern(oreHarbor);
        Image neutralHarbor = new Image(getClass().getClassLoader().getResource("neutralHarbor.png").toString());
        imagePattern[5] = new ImagePattern(neutralHarbor);

        RectangleXY r2 = new RectangleXY(1, 1, 0, 0);
        r2.setX(coor[29].getX());
        r2.setY(coor[29].getY());
        r2.setWidth(55);
        r2.setHeight(30);
        r2.setArcWidth(20);
        r2.setArcHeight(20);
        r2.setRotate(-90.0);
        r2.setFill(getHarborColor(1, 1, 0, 0));
        r2.setColor(getHarborColor(1, 1, 0, 0));
        setHarborImg(r2,imagePattern);

        rectangles[29] = r2;
        boardOfCatan.getChildren().addAll(r2);

        for (int i = 0; i < 29; i++) {
            for (CircleXY c : circles) {
                if (c.getX() == coast[i].getX() && c.getY() == coast[i].getY()) {
                    for (CircleXY c1 : circles) {
                        if ((c1.getX() == coast[i + 1].getX() && c1.getY() == coast[i + 1].getY())) {
                            RectangleXY r1 = new RectangleXY(c.getCoordinates().getX(), c.getCoordinates().getY(), c1.getCoordinates().getX(), c1.getCoordinates().getY());
                            r1.setX(coor[i].getX());
                            r1.setY(coor[i].getY());
                            r1.setWidth(55);
                            r1.setHeight(30);
                            r1.setColor(getHarborColor(c.getCoordinates().getX(), c.getCoordinates().getY(), c1.getCoordinates().getX(), c1.getCoordinates().getY()));
                            setHarborImg(r1,imagePattern);
                            int finalX = coast[i].getX();
                            int finalY = coast[i].getY();

                            if ((finalX == 0 && finalY == 0) || (finalX == 2 && finalY == 0) || (finalX == 4 && finalY == 0)||(finalX == 0 && finalY == 2) || (finalX == 0 && finalY == 1)) {
                                r1.setRotate(-32.0);
                            } else if ((finalX == 6 && finalY == 0) || (finalX == 8 && finalY == 1) || (finalX == 10 && finalY == 3)  || (finalX == 10 && finalY == 2)) {
                                r1.setRotate(+450.0);
                            } else if ((finalX == 5 && finalY == 5) || (finalX == 3 && finalY == 4) || (finalX == 1 && finalY == 3) || (finalX == 1 && finalY == 2) || (finalX == 1 && finalY == 1)||(finalX == 10 && finalY == 4)) {
                                r1.setRotate(-90.0);
                            }else if((finalX == 3 && finalY == 0) ||(finalX == 5 && finalY == 0)||(finalX == 1 && finalY == 0)||(finalX == 7 && finalY == 1)||(finalX == 9 && finalY == 2)){
                                r1.setRotate(+32.0);
                            }else if((finalX == 11 && finalY == 3) || (finalX == 11 && finalY == 4) || (finalX == 11 && finalY == 5) || (finalX == 9 && finalY == 5) || (finalX == 7 && finalY == 5)){
                                r1.setRotate(-212.0);
                            }else{
                                r1.setRotate(+212.0);
                            }
                            rectangles[i] = r1;
                            boardOfCatan.getChildren().addAll(r1);
                        }
                    }

                }

            }
        }
        for (int i = 0; i < 28; i++) {
            if (rectangles[i].getColor().equals(rectangles[i + 1].getColor()) && rectangles[i + 1].getColor().equals(rectangles[i + 2].getColor())) {
                rectangles[i + 1].setColor(Color.TRANSPARENT);
                rectangles[i + 1].setFill(Color.TRANSPARENT);
            }
        }
        if (rectangles[29].getColor().equals(rectangles[0].getColor()) && rectangles[0].getColor().equals(rectangles[1].getColor())) {
            rectangles[0].setColor(Color.TRANSPARENT);
            rectangles[0].setFill(Color.TRANSPARENT);
        }
    }

    /**
     * sets HarborImage
     * @param r rectangle from class rectangleXY
     * @param imagePattern immagepatterns for specific harbors
     */
    public void setHarborImg(RectangleXY r,ImagePattern[] imagePattern){
        if(r.getColor() == Color.BLUE){
            r.setFill(imagePattern[5]);
        } else if(r.getColor() == Color.RED){
            r.setFill(imagePattern[2]);
        } else if(r.getColor() == Color.YELLOW){
            r.setFill(imagePattern[0]);
        } else if(r.getColor() == Color.BROWN){
            r.setFill(imagePattern[1]);
        } else if(r.getColor() == Color.LIGHTGRAY){
            r.setFill(imagePattern[4]);
        }else if(r.getColor() == Color.WHITE){
            r.setFill(imagePattern[3]);
        } else {
            r.setFill(Color.TRANSPARENT);
        }

    }

    /**
     * Checking which harbors could be draw
     * @param x x-coordinate of first intersection
     * @param y y-coordinate of first intersection
     * @param z x-coordinate of second intersection
     * @param w y-coordinate of second intersection
     * @return the Color of the Harbor
     */
    private Color getHarborColor(int x, int y, int z, int w) {
        Intersection[][] intersection = board.getIntersections();
        if (intersection[x][y].getHarbor() != null && intersection[z][w].getHarbor() != null) {
            if (intersection[x][y].getHarbor().getResource() == ResourceType.WOOL && intersection[z][w].getHarbor().getResource() == ResourceType.WOOL) {
                return Color.WHITE;
            } else if (intersection[x][y].getHarbor().getResource() == ResourceType.BRICK && intersection[z][w].getHarbor().getResource() == ResourceType.BRICK) {
                return Color.RED;
            } else if (intersection[x][y].getHarbor().getResource() == ResourceType.GRAIN && intersection[z][w].getHarbor().getResource() == ResourceType.GRAIN) {
                return Color.YELLOW;
            } else if (intersection[x][y].getHarbor().getResource() == ResourceType.LUMBER && intersection[z][w].getHarbor().getResource() == ResourceType.LUMBER) {
                return Color.BROWN;
            } else if (intersection[x][y].getHarbor().getResource() == ResourceType.ORE && intersection[z][w].getHarbor().getResource() == ResourceType.ORE) {
                return Color.LIGHTGRAY;
            } else if (intersection[x][y].getHarbor().getResource() == null && intersection[z][w].getHarbor().getResource() == null) {
                return Color.BLUE;
            }
        }
        return Color.TRANSPARENT;
    }

    /**
     * Splitting the dice input into two separate numbers and shuffle them for two different images as illustration
     * @param diceNumb input of the diceNumber between 1-12
     */
    public void setDice(int diceNumb) {
        Image[] image = new Image[]{null,
                new Image(getClass().getClassLoader().getResource("one.png").toString()),
                new Image(getClass().getClassLoader().getResource("two.png").toString()),
                new Image(getClass().getClassLoader().getResource("three.png").toString()),
                new Image(getClass().getClassLoader().getResource("four.png").toString()),
                new Image(getClass().getClassLoader().getResource("five.png").toString()),
                new Image(getClass().getClassLoader().getResource("six.png").toString()),
        };
        Utility.Pair[] two = {new Utility.Pair(1, 1)};
        Utility.Pair[] three = {new Utility.Pair(1, 2), new Utility.Pair(2, 1)};
        Utility.Pair[] four = {new Utility.Pair(1, 3), new Utility.Pair(3, 1), new Utility.Pair(2, 2)};
        Utility.Pair[] five = {new Utility.Pair(1, 4), new Utility.Pair(4, 1), new Utility.Pair(2, 3), new Utility.Pair(3, 2)};
        Utility.Pair[] six = {new Utility.Pair(1, 5), new Utility.Pair(5, 1), new Utility.Pair(3, 3), new Utility.Pair(2, 4), new Utility.Pair(4, 2)};
        Utility.Pair[] seven = {new Utility.Pair(2, 5), new Utility.Pair(5, 2), new Utility.Pair(3, 4), new Utility.Pair(4, 3), new Utility.Pair(1, 6), new Utility.Pair(6, 1)};
        Utility.Pair[] eight = {new Utility.Pair(4, 4), new Utility.Pair(2, 6), new Utility.Pair(6, 2), new Utility.Pair(3, 5), new Utility.Pair(5, 3)};
        Utility.Pair[] nine = {new Utility.Pair(3, 6), new Utility.Pair(6, 3), new Utility.Pair(4, 5), new Utility.Pair(5, 4)};
        Utility.Pair[] ten = {new Utility.Pair(4, 6), new Utility.Pair(6, 4), new Utility.Pair(5, 5)};
        Utility.Pair[] eleven = {new Utility.Pair(5, 6), new Utility.Pair(6, 5)};
        Utility.Pair[] twelve = {new Utility.Pair(6, 6)};

        int number = 0;
        switch (diceNumb) {
            case 2:
                dice1.setImage(image[two[number].getX()]);
                dice2.setImage(image[two[number].getY()]);
                break;
            case 3:
                number = (int) ((three.length * new Random().nextDouble()));
                dice1.setImage(image[three[number].getX()]);
                dice2.setImage(image[three[number].getY()]);
                break;
            case 4:
                number = (int) ((four.length * new Random().nextDouble()));
                dice1.setImage(image[four[number].getX()]);
                dice2.setImage(image[four[number].getY()]);
                break;
            case 5:
                number = (int) ((five.length * new Random().nextDouble()));
                dice1.setImage(image[five[number].getX()]);
                dice2.setImage(image[five[number].getY()]);
                break;
            case 6:
                number = (int) ((six.length * new Random().nextDouble()));
                dice1.setImage(image[six[number].getX()]);
                dice2.setImage(image[six[number].getY()]);
                break;
            case 7:
                number = (int) ((seven.length * new Random().nextDouble()));
                dice1.setImage(image[seven[number].getX()]);
                dice2.setImage(image[seven[number].getY()]);
                break;
            case 8:
                number = (int) ((eight.length * new Random().nextDouble()));
                dice1.setImage(image[eight[number].getX()]);
                dice2.setImage(image[eight[number].getY()]);
                break;
            case 9:
                number = (int) ((nine.length * new Random().nextDouble()));
                dice1.setImage(image[nine[number].getX()]);
                dice2.setImage(image[nine[number].getY()]);
                break;
            case 10:
                number = (int) ((ten.length * new Random().nextDouble()));
                dice1.setImage(image[ten[number].getX()]);
                dice2.setImage(image[ten[number].getY()]);
                break;
            case 11:
                number = (int) ((eleven.length * new Random().nextDouble()));
                dice1.setImage(image[eleven[number].getX()]);
                dice2.setImage(image[eleven[number].getY()]);
                break;
            case 12:
                dice1.setImage(image[twelve[number].getX()]);
                dice2.setImage(image[twelve[number].getY()]);
                break;
        }
    }

    /**
     * setting specific images for dices
     * @param diceNum1 first dice
     * @param diceNum2 second dice
     */
    public void setDice(int diceNum1, int diceNum2) {
        Image[] image = new Image[]{null,
                new Image(getClass().getClassLoader().getResource("one.png").toString()),
                new Image(getClass().getClassLoader().getResource("two.png").toString()),
                new Image(getClass().getClassLoader().getResource("three.png").toString()),
                new Image(getClass().getClassLoader().getResource("four.png").toString()),
                new Image(getClass().getClassLoader().getResource("five.png").toString()),
                new Image(getClass().getClassLoader().getResource("six.png").toString()),
        };
        setDice(diceNum1, dice1, image);
        setDice(diceNum2, dice2, image);

    }

    /**
     * setting diceImages reffering to thrown dices
     * @param dice value of dice
     * @param diceImage image of dice
     * @param image array link of diceImage
     */
    public void setDice(int dice, ImageView diceImage, Image[] image) {
        switch (dice) {
            case 1:
                diceImage.setImage(image[1]);
                break;
            case 2:
                diceImage.setImage(image[2]);
                break;
            case 3:
                diceImage.setImage(image[3]);
                break;
            case 4:
                diceImage.setImage(image[4]);
                break;
            case 5:
                diceImage.setImage(image[5]);
                break;
            case 6:
                diceImage.setImage(image[6]);
                break;
        }
    }

    /**
     * Button to finish the move of the current play
     */
    public void finishMove() {
        controller.getClientController().sendFinishMove();
    }

    /**
     * Setter for current player
     * @param currentPlayer current acting player
     */
    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
        stage.setTitle("" + currentPlayer);
    }

    /**
     * Shows input values in the textArea
     * @param s input of text as String
     */
    public void sentMessage(String s) {

        Text message = new Text(s + "\n");
        message.setFill(Color.BLACK);
        Platform.runLater(() -> chatWindow.getChildren().add(message));
    }

    /**
     * Displaying chat messages in the chatWindow
     * @param s input of text as String
     */
    public void sentMessage(String s, int id) {
        if (s.length() > 6 && s.substring(0, 6).toLowerCase().equals("@server")) {
            ArrayList<ArrayList<Street>> allConnectedStreetsTmp = new ArrayList<>();
            ArrayList<Street> playerStreetsTmp = new ArrayList<>(currentPlayer.getStreets());
            ArrayList<ArrayList<Street>> allConnectedStreets = board.getAllConnectedStreets(playerStreetsTmp, allConnectedStreetsTmp);
            System.out.println("Player: " + currentPlayer.getId());
            int longestRoadOfPlayer = board.calculateLongestPlayerRoad(allConnectedStreets, currentPlayer);
            System.out.println("Longest Player Road :" + longestRoadOfPlayer);
        } else if (s.length() > 7 && s.substring(0, 8).toLowerCase().equals("@server2")) {
            moveRobber();
        }

        Text senderLabel = new Text("");
        if (controller.getPlayerById(id) != null) {
            senderLabel.setText("[" + controller.getPlayerById(id).getName() + "]: ");
            if (controller.getPlayerById(id).getColor() == Color.WHITE) {
                senderLabel.setFill(Color.GRAY);
            } else {
                senderLabel.setFill(controller.getPlayerById(id).getColor());
            }
        } else {
            senderLabel.setText("[ server ]: ");
            senderLabel.setFill(Color.GREEN);
        }

        Text message = new Text(s + "\n");
        message.setFill(Color.BLACK);
        Platform.runLater(() -> chatWindow.getChildren().add(senderLabel));
        Platform.runLater(() -> chatWindow.getChildren().add(message));
    }

    /**
     * Buttonlistener for sending out a message
     */
    @FXML
    public void sendButtonPressed() {
        String input = chatTextInput.getText();
        if (!input.equals("")) {
            controller.getClientController().sendChatMessage(input);
            chatTextInput.clear();
        }
    }

    /**
     * Setter for the controller
     * @param controller object of the class controller
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Puts the name as a label and the specific color of the players as background in the playerInformation boxes
     * @param players input from list of players
     */
    public void initialisePlayerBox(ArrayList<Player> players) {
        // move current player to last list item

        players.remove(currentPlayer);
        players.add(0, currentPlayer);
        this.players = players;

        labelName1.setText(currentPlayer.getName());
        playerInfoBox1.setBackground(new Background(new BackgroundFill(currentPlayer.getColor(), CornerRadii.EMPTY, Insets.EMPTY)));

        labelName2.setText(players.get(1).getName());
        labelName3.setText(players.get(2).getName());
        playerInfoBox2.setBackground(new Background(new BackgroundFill(players.get(1).getColor(), CornerRadii.EMPTY, Insets.EMPTY)));
        playerInfoBox3.setBackground(new Background(new BackgroundFill(players.get(2).getColor(), CornerRadii.EMPTY, Insets.EMPTY)));

        if (players.size() < 4) {
            playerInfoBox4.setVisible(false);
            playerBox.setSpacing(40);
        } else {
            labelName4.setText(players.get(3).getName());
            playerInfoBox4.setBackground(new Background(new BackgroundFill(players.get(3).getColor(), CornerRadii.EMPTY, Insets.EMPTY)));
            playerBox.setSpacing(10);
        }

        if (players.get(0).getColor().equals(Color.web("#ECF0F1"))) {
            labelName1.setStyle("-fx-text-fill: #000");
            System.out.println("color change");
        } else if (players.get(1).getColor().equals(Color.web("#ECF0F1"))) {
            labelName2.setStyle("-fx-text-fill: #000");
            System.out.println("color change");
        } else if (players.get(2).getColor().equals(Color.web("#ECF0F1"))) {
            labelName3.setStyle("-fx-text-fill: #000");
            System.out.println("color change");
        }else if (players.size() > 3 && players.get(3).getColor().equals(Color.web("#ECF0F1"))) {
            labelName4.setStyle("-fx-text-fill: #000");
            System.out.println("color change");
        }

        updatePlayerInfoLabels();
    }

    /**
     * Help method to call the trade when robber is active
     * @throws IOException catching input failures
     */
    public void openTradeWindow() throws IOException {
        openTradeWindowOrRobber("TradeMode",null);
    }
    /**
     * Pop-up window to start trading with other players
     * @throws IOException catching input failures
     */
    public void openTradeWindowOrRobber(String string, Color color) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(BoardView.class.getResource("tradeWindow.fxml"));
        Pane root = loader.load();
        Scene scene = new Scene(root);
        TradeWindowController tradeWindowController = loader.getController();
        if(string.equals("RobberMode")){
            tradeWindowController.dropMode(currentPlayer.getResourcesTotal());
        }else if(string.equals("HarborMode")){
            tradeWindowController.harborMode(color);
        }
        tradeWindowController.setCurrentPlayer(currentPlayer);
        tradeWindowController.setCurrentRessources();
        tradeWindowController.initialize2HarborTrade();
        tradeWindowController.disableMinusButtonsOnStart();
        tradeWindowController.setController(controller);
        Popup pop = new Popup();
        pop.getContent().add(root);
        tradeWindowController.setPopup(pop);
        pop.show(stage);
    }

    /**
     * method to mark the Harbors that a player had and to choose a harbor to trade cards
     */
    public void openHarborTrade() {
        logger.info("in open HarborTrade rein");
        Intersection[][] intersection = board.getIntersections();
        harborButton.setText("ABBRECHEN");
        if(currentPlayer.getQuantityOfAResource("Holz")>= 4 || currentPlayer.getQuantityOfAResource("Lehm")>= 4 || currentPlayer.getQuantityOfAResource("Wolle")>= 4 || currentPlayer.getQuantityOfAResource("Getreide")>= 4 || currentPlayer.getQuantityOfAResource("Erz")>= 4){
            shipImage.setVisible(true);
        }
        for (RectangleXY x : rectangles) {
            int a=x.getCoordinates1().getX();
            int b=x.getCoordinates1().getY();
            int c=x.getCoordinates2().getX();
            int d=x.getCoordinates2().getY();
            if (intersection[a][b].getHarbor() != null && intersection[c][d].getHarbor() != null) {
                if (!x.getColor().equals(Color.TRANSPARENT) && intersection[a][b].getHarbor().getOwner()==currentPlayer){
                    x.setStrokeWidth(2.5);
                    x.setStroke(Color.LIGHTGRAY);
                    x.setOnMouseReleased((clickEvent) -> {
                        try {
                            openTradeWindowOrRobber("HarborMode",x.getColor());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        stopHarborTrade();
                        this.harborButton.setText("SEEHANDEL");
                        shipImage.setVisible(false);
                        this.harborButton.setOnMouseClicked(event1 ->openHarborTrade());
                    });
                }
            }
        }
        this.harborButton.setOnMouseReleased(event -> {
            System.out.println("HANDEL BUTTON RESET!?");
            shipImage.setVisible(false);
            stopHarborTrade();
            this.harborButton.setText("SEEHANDEL");
            this.harborButton.setOnMouseReleased(event1 -> openHarborTrade());
        });
    }

    /**
     * method to cancel the request for trade and to stop listener
     */
    public void stopHarborTrade(){
        for(RectangleXY x:rectangles){
            x.setStrokeWidth(0);
            x.setStroke(Color.TRANSPARENT);
            x.setOnMouseReleased(null);
        }
    }

    /**
     * Button to buy devCards
     */
    public void buyDevelopmentCard() {
        controller.getClientController().sendBuyDevCard();
    }

    /**
     * Displaying if current player got a monopoly card
     * @param value number of monopoly cards
     */
    public void setMonopolyLabel(int value){
        monopolyLabel.setText(String.valueOf(value));
    }

    /**
     * Displayed when current player got a victoryPoint card
     * @param value number of victoryPoint cards
     */
    public void setVictoryPointLabel(int value){
        victoryPoint.setText(String.valueOf(value));
    }

    /**
     * Displayed when current player got a yearOfPlenty card
     * @param value number of yearOfPlenty cards
     */
    public void setYearOfPlentyLabel(int value){
        yearOfPlentyLabel.setText(String.valueOf(value));
    }

    /**
     * Displayed when current player got a knight card
     * @param value number of knight cards
     */
    public void setKnightLabel(int value){
        knightLabel.setText(String.valueOf(value));
    }

    /**
     * Displayed when current player got a roadBuilding card
     * @param value number of roadbuilding cards
     */
    public void setRoadBuildingLabel(int value){
        roadBuildingLabel.setText(String.valueOf(value));
    }

    /**
     * setter for playing a roadBuilding card
     * @param playingRoadCard boolean to check if it´s owned
     */
    public void setPlayingRoadCard(boolean playingRoadCard) {
        isPlayingRoadCard = playingRoadCard;
    }

    /**
     * Launcher for DevCard fxml file
     * @throws IOException
     */
    public void openMyDevelopmentCard() throws IOException{
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(BoardView.class.getResource("developCard.fxml"));
        Pane root = loader.load();
        Scene scene = new Scene(root);
        MyDevelopmentCardsController myDevelop = loader.getController();
        myDevelop.setCurrentPlayer(currentPlayer);
        myDevelop.disableButtons();
        myDevelop.setController(controller);
        myDevelop.deactivateCurrentlyBoughtCard(controller.getClientController().getLastCardBought());
        Popup pop = new Popup();
        pop.getContent().add(root);
        myDevelop.setPopup(pop);
        pop.show(stage);
    }

    /**
     * message for the existing offer
     */
    public void showOffer(Resource[] offeredResources, Resource[] demandedResources, int id, boolean canTrade){
        Popup offerPopup = new Popup();
        VBox vBox = new VBox(20);
        HBox offerBox = new HBox(5);
        HBox demandBox = new HBox(5);
        HBox hBoxButtons = new HBox(5);
        VBox offerBoxAndLabel = new VBox(5);
        VBox demandBoxAndLabel = new VBox(5);
        Button accept = new Button("ANNEHMEN");
        accept.getStyleClass().add("acceptButton");
        Button cancel = new Button("ABBRECHEN");
        cancel.getStyleClass().add("cancelButton");
        Label offerTitle = new Label("HANDELSANGEBOT VON \n" + controller.getPlayerById(id).getName());
        offerTitle.getStyleClass().add("tradeOfferH1");
        offerTitle.setStyle("-fx-text-alignment: center;");
        Label offerText = new Label("ANGEBOT");
        offerText.getStyleClass().add("tradeOfferH2");
        offerBoxAndLabel.getChildren().addAll(offerText, offerBox);
        Label demandText = new Label("NACHFRAGE");
        demandText.getStyleClass().add("tradeOfferH2");
        demandBoxAndLabel.getChildren().addAll(demandText, demandBox);
        offerBoxAndLabel.setAlignment(Pos.CENTER);
        demandBoxAndLabel.setAlignment(Pos.CENTER);
        Label notEnoughResources = new Label("Du hast leider nicht genug Rohstoffe für den Handel :(");
        notEnoughResources.getStyleClass().add("tradeOfferH2");
        ImageView[] imageView = new ImageView[5];
        for (int i = 0; i < imageView.length; i++) {
            imageView[i] = new ImageView();
            imageView[i].getStyleClass().add("tradeOfferCards");
            imageView[i].setFitWidth(80);
            imageView[i].setFitHeight(160);
            imageView[i].setPreserveRatio(true);
        }
        ArrayList<Label> labelOffer = new ArrayList<>();
        ArrayList<Label> labelDemand = new ArrayList<>();

        //get images and set them
        Image grainImage = new Image(getClass().getClassLoader().getResource("Card1.png").toString());
        imageView[0].setImage(grainImage);
        Image lumberImage = new Image(getClass().getClassLoader().getResource("Card2.png").toString());
        imageView[1].setImage(lumberImage);
        Image brickImage = new Image(getClass().getClassLoader().getResource("Card3.png").toString());
        imageView[2].setImage(brickImage);
        Image woolImage = new Image(getClass().getClassLoader().getResource("Card5.png").toString());
        imageView[3].setImage(woolImage);
        Image oreImage = new Image(getClass().getClassLoader().getResource("Card4.png").toString());
        imageView[4].setImage(oreImage);

        //only show images of offered resources
        setResourceLabelForTrade(labelOffer, offeredResources, imageView);
        //only show images of demanded resources
        setResourceLabelForTrade(labelDemand, demandedResources, imageView);

        offerBox.getChildren().addAll(labelOffer);
        demandBox.getChildren().addAll(labelDemand);
        offerBox.setAlignment(Pos.CENTER);
        demandBox.setAlignment(Pos.CENTER);

        //activate Buttons and send out Message to Server
        accept.setOnAction((event) -> {
            controller.getClientController().sendAcceptOffer(true);
            showRunningTrade();
            offerPopup.hide();
        });
        cancel.setOnAction((event) -> {
            controller.getClientController().sendAcceptOffer(false);
            offerPopup.hide();
        });
        hBoxButtons.getChildren().addAll(accept,cancel);
        hBoxButtons.setAlignment(Pos.CENTER);

        //addAll to vBox
        if (canTrade) {
            vBox.getChildren().addAll(offerTitle, offerBoxAndLabel, demandBoxAndLabel, hBoxButtons);
        } else {
            vBox.getChildren().addAll(offerTitle, offerBoxAndLabel, demandBoxAndLabel, hBoxButtons, notEnoughResources);
            accept.setDisable(true);
        }

        //style and show
        vBox.getStyleClass().add("tradeOfferRoot");
        vBox.setAlignment(Pos.CENTER);
        offerPopup.getContent().add(vBox);
        offerPopup.centerOnScreen();
        offerPopup.show(stage);

    }

    /**
     * setter for ressoruces labels in the trade window
     * @param labels input of labels shown on images
     * @param resources input of resources types
     * @param imageView input of imageViews
     */
    private void setResourceLabelForTrade(ArrayList<Label> labels, Resource[] resources, ImageView[] imageView) {
        for (Resource resource : resources) {
            if (resource.getValue() > 0) {
                switch (resource.getType()) {
                    case "grain":
                        Label label = new Label(String.valueOf(resource.getValue()));
                        label.getStyleClass().add("tradeNumberLabelBlack");
                        labels.add(label);
                        labels.get(labels.size() - 1).setGraphic(imageView[0]);
                        break;
                    case "lumber":
                        labels.add(new Label(String.valueOf(resource.getValue())));
                        labels.get(labels.size() - 1).setGraphic(imageView[1]);
                        break;
                    case "brick":
                        labels.add(new Label(String.valueOf(resource.getValue())));
                        labels.get(labels.size() - 1).setGraphic(imageView[2]);
                        break;
                    case "wool":
                        Label label2 = new Label(String.valueOf(resource.getValue()));
                        label2.getStyleClass().add("tradeNumberLabelBlack");
                        labels.add(label2);
                        labels.get(labels.size() - 1).setGraphic(imageView[3]);
                        break;
                    case "ore":
                        labels.add(new Label(String.valueOf(resource.getValue())));
                        labels.get(labels.size() - 1).setGraphic(imageView[4]);
                        break;
                }
            }
        }
        for(Label label : labels){
            label.setContentDisplay(ContentDisplay.CENTER);
            label.getStyleClass().add("tradeNumberLabel");
        }
    }

    /**
     * Popup shown, to choose the offer of a player
     * @param acceptedPlayers
     */
    public void showOfferAccepted(ArrayList<Integer> acceptedPlayers){
        acceptedTradePopup = new Popup();
        VBox vBox = new VBox();
        HBox hBox = new HBox();
        VBox vBoxButtons = new VBox();
        hBox.setSpacing(5.0);
        vBox.setSpacing(5.0);
        vBoxButtons.setSpacing(5.0);

        Button[] buttons = new Button[3];
        buttons[0] = new Button();
        buttons[1] = new Button();
        buttons[2] = new Button();

        Button cancel = new Button("Ablehnen");
        Button close = new Button("Close");
        Text text = new Text();
        text.getStyleClass().add("popupMessage");
        cancel.getStyleClass().add("popupButton");
        close.getStyleClass().add("popupButton");

        if (acceptedPlayers.size()>0) {
            text.setText("Wähle ein Angebot aus: ");
            vBox.getChildren().add(text);
            vBox.getChildren().addAll(vBoxButtons,hBox);

            for (int i = 0; i < acceptedPlayers.size(); i++) {
                buttons[i].setText(controller.getPlayerById(acceptedPlayers.get(i)).getName());
                vBoxButtons.getChildren().add(buttons[i]);
                int finalI = i;
                buttons[i].setOnAction((event) -> {
                    acceptTradeOffer(acceptedPlayers.get(finalI));
                    acceptedTradePopup.hide();
                });
            }

            hBox.getChildren().add(cancel);
            cancel.setOnAction((event) -> {
                controller.getClientController().sendTradeCancel();
                acceptedTradePopup.hide();
            });
            controller.getClientController().resetTradeVariables();
        } else {
            controller.getClientController().resetTradeVariables();
            text.setText("Niemand mag dich und will mit dir händln");
            vBox.getChildren().addAll(text, close);
            vBox.getStyleClass().add("popupDesign");
            close.setOnAction((event) -> {
                acceptedTradePopup.hide();
            });
        }
        vBox.getStyleClass().add("popupDesign");
        vBox.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        vBoxButtons.setAlignment(Pos.CENTER);
        acceptedTradePopup.getContent().add(vBox);
        acceptedTradePopup.centerOnScreen();
        acceptedTradePopup.show(stage);
    }

    /**
     * Method to hide trade pop-up
     */
    public void hideAcceptedTradePopup() {
        acceptedTradePopup.hide();
    }

    /**
     * Method to link clientcontroller
     * @param id
     */
    private void acceptTradeOffer(int id) {
        controller.getClientController().sendTradeExecution(id);
    }

    /**
     * Shown when trade is in progress
     */
    private void showRunningTrade(){
        runningPopUp = new Popup();
        VBox root = new VBox();
        Text text = new Text();
        HBox cancelBox = new HBox();
        Button cancel = new Button();
        text.getStyleClass().add("popupMessage");
        cancel.getStyleClass().add("popupButton");
        text.setText("Handel läuft ...");
        cancel.setText("ABBRECHEN");
        cancel.setOnAction((ActionEvent event) -> {
            runningPopUp.hide();
            controller.getClientController().sendTradeCancel();
        });

        cancelBox.getChildren().add(cancel);
        cancelBox.setAlignment(Pos.CENTER_RIGHT);

        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(text,cancelBox);
        root.getStyleClass().add("popupDesign");

        runningPopUp.getContent().add(root);
        runningPopUp.centerOnScreen();
        runningPopUp.show(stage);

    }

    /**
     * method to close progress pop-up
     */
    public void closeRunningPopup(){
        runningPopUp.hide();
    }

    /**
     * pops up if the player has to move the robber
     */
    public void moveRobberPopup(){
        Popup moveRobberPopup = new Popup();
        VBox vBox = new VBox();
        Text text = new Text("Bitte versetze den Räuber, um weiter zu spielen!");
        text.getStyleClass().add("popupMessage");
        Button close = new Button("Ok, Master");
        close.getStyleClass().add("popupButton");
        vBox.getChildren().addAll(text,close);
        vBox.getStyleClass().add("popupDesign");
        close.setOnAction((event) -> moveRobberPopup.hide());

        //style and show
        vBox.setAlignment(Pos.CENTER);
        moveRobberPopup.getContent().add(vBox);
        moveRobberPopup.centerOnScreen();
        moveRobberPopup.show(stage);
    }

    /**
     *
     * @param playingKnightCard boolean to check if knight card is owned
     */
    public void setPlayingKnightCard(boolean playingKnightCard) {
        isPlayingKnightCard = playingKnightCard;
    }

    /**
     * Pops up if a player has the biggest knight force or the longest trade road
     * @param tradeRoad
     */
    public void showPlayerHasLongestTradeRoadOrBiggestKnightForce(boolean tradeRoad) {
        Popup showLongestRoadPopup = new Popup();
        VBox vBox = new VBox();
        Text text = new Text();
        text.getStyleClass().add("popupMessage");
        if (tradeRoad) {
            text.setText("GLÜCKWUNSCH! \nDU HAST JETZT DIE LÄNGSTE HANDELSSTRASSE");
        } else {
            text.setText("GLÜCKWUNSCH! \nDU HAST JETZT DIE GRÖSSTE RITTERMACHT");
        }
        Button close = new Button("Ok!");
        close.getStyleClass().add("popupButton");
        vBox.getChildren().addAll(text, close);
        vBox.getStyleClass().add("popupDesign");
        close.setOnAction((event) -> showLongestRoadPopup.hide());

        //style and show
        vBox.setAlignment(Pos.CENTER);
        showLongestRoadPopup.getContent().add(vBox);
        showLongestRoadPopup.centerOnScreen();
        showLongestRoadPopup.show(stage);

    }

    /**
     * Popup to remind the player that it's his turn
     */
    public void yourTurnPopup(boolean startingPhase){
        Popup yourTurn = new Popup();
        VBox vBox = new VBox(10);
        Text text = new Text("DU BIST AM ZUG.");
        text.getStyleClass().add("popupMessage");
        Button close = new Button("WÜRFELN");
        if (startingPhase) {
            close.setText("OK");
        }
        close.getStyleClass().add("popupButton");
        vBox.getChildren().addAll(text, close);
        vBox.getStyleClass().add("popupDesign");
        close.setOnAction((event) -> {
            if (!startingPhase) {
                yourTurn.hide();
                controller.getClientController().sendThrowDice();
            } else {
                yourTurn.hide();
            }
        });

        //style and show
        vBox.setAlignment(Pos.CENTER);
        yourTurn.getContent().add(vBox);
        yourTurn.centerOnScreen();
        yourTurn.show(stage);

    }

    /**
     * Progress pop-up, shown if someone hasn´t finished cards dropping
     */
    public void waitingWhileDroppingCardsPopup() {
        waitingDroppingCardsPopup = new Popup();
        VBox vBox = new VBox(10);
        Text text = new Text("Bitte warten! \nDie anderen Spieler müssen noch ihre Karten abgeben.");
        text.getStyleClass().add("popupMessage");

        Button close = new Button("Ok!");
        close.getStyleClass().add("popupButton");
        vBox.getChildren().addAll(text, close);
        vBox.getStyleClass().add("popupDesign");
        close.setOnAction((event) -> waitingDroppingCardsPopup.hide());

        vBox.setAlignment(Pos.CENTER);
        waitingDroppingCardsPopup.getContent().add(vBox);
        //waitingDroppingCardsPopup.centerOnScreen();
        waitingDroppingCardsPopup.show(stage);

    }

    /**
     * Progress pop-up, shown if someone hasn´t finished cards dropping
     */
    public void closeWaitingDroppingCardsPopup(){
        waitingDroppingCardsPopup.hide();
    }

    /**
     * At the and of the game, this Popup shows who won
     * @param won boolean to check out if game ends
     * @param name showing the winner as string
     */
    public void winnerPopup(boolean won, String name){
        Popup winner = new Popup();
        VBox vBox = new VBox(10);
        Text text = new Text();
        text.getStyleClass().add("popupMessage");
        Button nice = new Button();
        nice.getStyleClass().add("popupButton");
        if (won){
            text.setText("Juhu, \ndu hast gewonnen!!!!11!!elf!!!");
            nice.setText("nice");
            vBox.setStyle("-fx-background-color: #C74E8B ");
        } else {
            text.setText(name+" hat gewonnen!\nVersuch's morgen nochmal :)");
            nice.setText("Ok!");
            vBox.setStyle("-fx-background-color: #1C1C1C");

        }
        nice.setOnAction((event) -> {
            winner.hide();
            settlementButton.setDisable(true);
            drawStreetsButton.setDisable(true);
            finishMove.setDisable(true);
            tradeButton.setDisable(true);
            cityButton.setDisable(true);
            harborButton.setDisable(true);
            developmentCardButton.setDisable(true);
            myDevCardsButton.setDisable(true);
        });
        vBox.getChildren().addAll(text, nice);
        vBox.getStyleClass().add("popupDesign");
        vBox.setAlignment(Pos.CENTER);
        winner.getContent().add(vBox);
        winner.show(stage);
    }

    /**
     * Ship image will be visible after checking out requirements
     */
    @FXML
    private void shipImagePressed(){
        try {
            openTradeWindowOrRobber("HarborMode", Color.LIGHTGRAY);
            stopHarborTrade();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to cancel harbor trades
     */
    public void resetHarborTradeButton(){
        this.harborButton.setText("SEEHANDEL");
        shipImage.setVisible(false);
        this.harborButton.setOnMouseClicked(event1 ->openHarborTrade());
    }

    /**
     * Message thrown if the maximum number of settlements reached
     */
    public void increaseSettlementsBuilt() {
        settlementsBuilt++;
        if (settlementsBuilt == 5) {
            sentMessage("Du hast nun 5 Siedlungen gebaut. Das ist die maximale Anzahl. Baue eine Stadt, um wieder eine Siedlung zu bauen.");
        }
    }

    /**
     * Message thrown if the maximum number of cities reached
     */
    public void increaseCitysBuilt() {
        citysBuilt++;
        settlementsBuilt--;
        if (citysBuilt == 4) {
            sentMessage("Du hast nun 4 Städte gebaut. Das ist die maximale Anzahl.");
        }
    }

    /**
     * Message thrown if the maximum number of streets reached
     */
    public void increaseStreetsBuilt() {
        streetsBuilt++;
        if (streetsBuilt == 15) {
            sentMessage("Du hast nun 15 Straßen gebaut. Das ist die maximale Anzahl.");
        }
    }

    /**
     * Method to use the cheat "nice"
     */
    public void updateColor() {
        playerInfoBox1.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        drawStreets();
        drawSettlements();
    }

    /**
     * shown pop-up after loosing connection to server
     */
    public void lostConnectionToServerPopup() {
        Popup lostConnection = new Popup();
        VBox vBox = new VBox(10);
        Text text = new Text("Verbindung zum Server verloren :( \nDas Spiel wird beendet.");
        text.getStyleClass().add("popupMessage");
        Button close = new Button("Schließen");
        close.getStyleClass().add("popupButton");
        close.setOnAction((event) -> {
            lostConnection.hide();
            System.exit(0);
        });
        vBox.getChildren().addAll(text, close);
        vBox.getStyleClass().add("popupDesign");
        vBox.setStyle("-fx-background-color: #7BFFD4; -fx-padding: 30px; -fx-font-family: 'Open Sans'");
        vBox.setAlignment(Pos.CENTER);
        lostConnection.getContent().add(vBox);
        lostConnection.show(stage);
    }

    /**
     * Method to cancel building streets, settlements and cities
     */
    public void resetSettlementCityAndStreetButton() {
        stopClickListeners();
        stopStreetClickListeners();
        settlementButton.setText("SIEDLUNG");
        cityButton.setText("STADT");
//        drawStreetsButton.setText("STRASSE");
 //       this.drawStreetsButton.setOnMouseReleased(event1 -> drawViableStreets(false));
        if(drawStreetsButton.isCancelButton()){
            streetButtonOnAction();
        }
    }

    /**
     * Badges after getting longest traderoad or biggest knightforce status
     * @param knightForce boolean for knightforce, true or false
     * @param playerId number as playerId
     */
    public void setBadges(boolean knightForce, int playerId) {
        int playerIndex = 0;
        if(playerId != currentPlayer.getId()){
            for (int i = 0; i < players.size() ; i++) {
                if(playerId == players.get(i).getId()){
                    playerIndex = i;
                }
            }
        }
        if (knightForce) {
            knightForceBadgeP1.setVisible(false);
            knightForceBadgeP2.setVisible(false);
            knightForceBadgeP3.setVisible(false);
            knightForceBadgeP4.setVisible(false);
            if(playerId == currentPlayer.getId()) {
                knightForceBadgeP1.setVisible(true);
            } else {
                switch(playerIndex) {
                    case 1:
                        knightForceBadgeP2.setVisible(true);
                        break;
                    case 2:
                        knightForceBadgeP3.setVisible(true);
                        break;
                    case 3:
                        knightForceBadgeP4.setVisible(true);
                        break;
                }
            }
        } else {
            tradeRoadBadgeP1.setVisible(false);
            tradeRoadBadgeP2.setVisible(false);
            tradeRoadBadgeP3.setVisible(false);
            tradeRoadBadgeP4.setVisible(false);
            if(playerId == currentPlayer.getId()) {
                tradeRoadBadgeP1.setVisible(true);
            } else {
                switch(playerIndex) {
                    case 1:
                        tradeRoadBadgeP2.setVisible(true);
                        break;
                    case 2:
                        tradeRoadBadgeP3.setVisible(true);
                        break;
                    case 3:
                        tradeRoadBadgeP4.setVisible(true);
                        break;
                }
            }
        }

    }

    public void disableAllTradeRoadBadges() {
        tradeRoadBadgeP1.setVisible(false);
        tradeRoadBadgeP2.setVisible(false);
        tradeRoadBadgeP3.setVisible(false);
        tradeRoadBadgeP4.setVisible(false);
    }

    /**
     * Adding animation after throwing dices
     */
    public void setDiceGif() {
        Image diceGif = new Image(getClass().getClassLoader().getResource("dice.gif").toString());
        Image diceGif2 = new Image(getClass().getClassLoader().getResource("dice2.gif").toString());
        dice1.setImage(diceGif);
        dice2.setImage(diceGif2);
    }

    /**
     * Cheats style are reffering to the css file
     */
    public void setStyleClassForCheat() {
        cardLabelPlayer1.getStyleClass().add("cheatDesign");
        resourcesLabelPlayer1.getStyleClass().add("cheatDesign");
        knightLabelPlayer1.getStyleClass().add("cheatDesign");
        victoryPointsLabelPlayer1.getStyleClass().add("cheatDesign");
        cardValuePlayer1.getStyleClass().add("cheatDesign");
        resourcesPlayer1.getStyleClass().add("cheatDesign");
        knightPlayer1.getStyleClass().add("cheatDesign");
        victoryPointsPlayer1.getStyleClass().add("cheatDesign");
        labelName1.getStyleClass().add("cheatDesign");
    }

    /**
     * Button to mute background music
     */
    public void handleMusic(){
        view.mediaPlayer.pause();
        Image playIcon = new Image(getClass().getClassLoader().getResource("play.png").toString());
        Image muteIcon = new Image(getClass().getClassLoader().getResource("mute.png").toString());
        musicImage.setImage(muteIcon);
        musicButton.setOnAction(event -> {
            view.playMusic();
            view.mediaPlayer.seek(Duration.ZERO);
            musicImage.setImage(playIcon);
            musicButton.setOnAction(event1 -> handleMusic());
        });
    }

    /**
     * Setter for boardView object
     * @param view view object to use methods of boardview
     */
    public void setView(BoardView view) {
        this.view = view;
    }

}