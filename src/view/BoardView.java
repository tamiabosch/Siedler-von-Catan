package view;

import LOG.Logging;
import control.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Player;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoardView extends Application {

	Controller controller = new Controller();
	Stage primaryStage;
	static Logger logger;
	private boolean hostServer = false;
	Image logo = new Image(getClass().getClassLoader().getResource("logo.png").toString());
	Media musicFile = (new Media(getClass().getClassLoader().getResource("beat3.wav").toString()));
	MediaPlayer mediaPlayer = new MediaPlayer(musicFile);
	public static boolean noServerTest = false;

	public static void main(String[] args) {
		launch(args);
	}

	public BoardView(){

	}
	@Override
	public void init(){
//		Logging.suppressConsoleOutput();
		logger = Logging.getLoggerClient("client");
		logger.info("Client started  - encoding test: Hallööchen");
		playMusic();


    }

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			this.primaryStage = primaryStage;
			if (!noServerTest) {
				FXMLLoader loader = new FXMLLoader();
				// Load root layout from fxml file.
				loader.setLocation(BoardView.class.getResource("connectionWindow.fxml"));
				Pane root = loader.load();
				ConnectionWindowController connectionWindowController = loader.getController();
				connectionWindowController.setControllerAndView(controller, this);
				Scene scene = new Scene(root);
				scene.getStylesheets().add("https://fonts.googleapis.com/css?family=Open+Sans:400,600,700,800");
				primaryStage.setScene(scene);
				primaryStage.setTitle("InfraroteHacks");
				primaryStage.getIcons().add(logo);


				scene.getStylesheets().add(getClass().getClassLoader().getResource("styleFX.css").toExternalForm());

				primaryStage.show();
			} else {
				changeSceneToGameBoard();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		primaryStage.setOnCloseRequest(event -> Platform.exit());
	}

	/**
	 * opens the lobbyWindow
	 * sets the controller, view, server and if currentPlayer hosts the server
	 * @param server server name "TestServer" or "Nicer Server"
	 * @throws Exception
	 */
	public void changeSceneToLobby(String server) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(BoardView.class.getResource("lobbyWindow.fxml"));
		Pane root = loader.load();
		LobbyWindowController lobbyWindowController = loader.getController();
		controller.setLobbyWindowController(lobbyWindowController);
		lobbyWindowController.setControllerAndView(controller, this, server, hostServer);
		Scene scene = new Scene(root);
		//scene.getStylesheets().add(getClass().getResource("styleFX.css").toExternalForm());
		//scene.getStylesheets().add(getClass().getResource("font.css").toExternalForm());
		this.primaryStage.setScene(scene);
		primaryStage.setTitle("InfraroteHacks");
		primaryStage.getIcons().add(logo);
		this.primaryStage.show();
		this.primaryStage.centerOnScreen();
		primaryStage.setOnCloseRequest(event -> {
			Platform.exit();
			controller.getClientController().setSocketClosed(true);
			controller.getClientController().closeSocket();
		});
	}

	public void changeSceneToGameBoard() throws Exception{
	    logger.info(".");
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(BoardView.class.getResource("gameBoard.fxml"));
		Pane root = loader.load();
		BoardViewController boardViewController = loader.getController();
		controller.setBoardViewController(boardViewController);
        boardViewController.setStage(primaryStage);
        boardViewController.setController(controller);
		boardViewController.setView(this);
        controller.drawBoard();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getClassLoader().getResource("styleFX.css").toExternalForm());
        primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.centerOnScreen();
		primaryStage.getIcons().add(logo);
		primaryStage.setOnCloseRequest(event -> {
			Platform.exit();
			controller.getClientController().setSocketClosed(true);
			controller.getClientController().closeSocket();
		});
	}

	/**
	 * set if the currentPlayer hosts the server
	 */
	public void setHostServerTrue(){
		this.hostServer = true;
	}

	public static Logger getLogger(){
		return logger;
	}

	public void playMusic(){
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.seconds(-2.0)));
    }


//	public void launchBoardView(String[] args){
//		launch(args);
//	}

}
