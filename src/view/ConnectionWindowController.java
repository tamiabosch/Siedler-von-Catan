package view;

import InfraroterServer.Main;
import LOG.Logging;
import control.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Cem, Kevin
 */
public class ConnectionWindowController {

    private Logger logger;
    private Controller controller;
    private BoardView view;

    @FXML
    ComboBox<String> serverChoiceBox;
    @FXML
    Button startServerButton;
    @FXML
    Button connectButton;
    @FXML
    Label serverMessageLabel;

    public ConnectionWindowController(){
//        logger= Logging.getLoggerClient();
    }

    /**
     * initializing starting values for server and protocol versions comboBoxes
     */
    @FXML
    public void initialize() {
        serverMessageLabel.setVisible(false);
        startServerButton.setStyle("-fx-font-weight: 800;");
        serverChoiceBox.setValue("Nicer Server");
        serverChoiceBox.getItems().addAll("TestServer", "Nicer Server");
        serverChoiceBox.valueProperty().addListener((obj, oldValue, newValue) -> {
            if (newValue.equals("TestServer")) {
                startServerButton.setDisable(true);
            } else {
                startServerButton.setDisable(false);
            }

        });
    }

    /**
     * After choosing server reference and protocol version,
     * if "TestServer", connection will be started
     *
     * @throws Exception thrown if connection fails
     */
    @FXML
    private void startConnectionToServer() throws Exception {

        if(serverChoiceBox.getValue().equals("TestServer")) {
            controller.startClientController("TestServer", null);
            logger.info("Connected to TestServer");
        }
        this.view.changeSceneToLobby(serverChoiceBox.getValue());
    }

    /**
     * Setter for controller and view objects
     * @param controller reference to the InfraroterServer.Controller class
     * @param view reference to the BoardView class
     */
    void setControllerAndView(Controller controller, BoardView view) {
        this.controller = controller;
        this.view = view;
    }

    @FXML
    private void handleStartServer(){
        startServerButton.setDisable(true);
        view.setHostServerTrue();
       try {
            Main serverMain = new Main();
            new Thread(serverMain).start();

            Thread.sleep(100);
        }

        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }

        try {
            startConnectionToServer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

}
