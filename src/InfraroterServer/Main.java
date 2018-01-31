package InfraroterServer;

import java.io.*;
import java.net.*;

/**
 * @author Tamia & Kevin
 */
public class Main implements Runnable {

    @Override
    public void run() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(6789);
            int id = 0;
            Controller controller = new Controller();
            System.out.println("started");

            try {
                while (true) {
                    Socket connectionSocket = serverSocket.accept();
                    id++;
                    startHandler(connectionSocket, id, controller);
                }
            } finally {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startHandler(Socket socket, int id, Controller controller) throws IOException {
        ServerController serverController = new ServerController(socket, id);
        serverController.setController(controller);
        controller.addThreadToArray(serverController);
        Thread serverThread = new Thread(serverController);
        serverThread.start();
    }

}
