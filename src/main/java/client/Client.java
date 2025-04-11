package client;

import cards.Card;
import cards.NumberCard;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 * 
 * This class is the one that handles the connection with the server en the 
 * frontend.
 */
public class Client {
    private boolean connect;
    private boolean ready;
    private int port;
    private ArrayList<String> cards;
    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;
    private String playerName;
    private String host;
    private Thread listenerThread;
    
    /**
     * @param playerName the name of the player.
     * @param host the IP of the server.
     * @param port the port of the server.
     * 
     * Creates a client and initialize the connection to the server, after that
     * start listening for the server.
     */
    public Client(String playerName, String host, int port) {
        this.playerName = playerName; 
        this.host = host;
        this.port = port;
        this.ready = false;
        this.cards = new ArrayList<>();
        
        initializeConnection();
        startListening();
    }
    
    /**
     * Initialize the connecction to the server.
     */
    private void initializeConnection(){
        try {
            socket = new Socket(this.host, this.port);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(new BufferedInputStream(socket
                    .getInputStream()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        connectToServer();
    }
    
    /**
     * Connecto to the server.
     */
    private void connectToServer(){
        try {
            output.writeUTF(playerName + "/");
            output.flush();
            this.connect = true;
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
            this.connect = false;
        }
    }
    
    /**
     * Start listening for the server messages.
     */
     private void startListening() {
        listenerThread = new Thread(() -> {
            while (connect && !socket.isClosed()) {
                try {
                    String message = input.readUTF();
                    processServerMessage(message);
                } catch (IOException e) {
                    System.out.println("Connection lost: " + e.getMessage());
                    disconnect();
                    break;
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

     /**
      * @param message of the server.
      * 
      * Handle the message of the server.
      */
    private void processServerMessage(String message) {
        System.out.println("message: "+ message);
        String messageCode = message.split("/")[0];
        switch (messageCode) {
            case "CARDS":
                setPlayerDeck(message);
                break;
            case "READY":
                this.ready = true;
                break;
            case "":
                break;
            default:
                System.out.println(message);
        }
    }

    /**
     * @param message for the server.
     * 
     * Send a message to the server.
     */
    public void sendMessage(String message) {
        if (connect) {
            try {
                output.writeUTF(message);
                output.flush();
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
                disconnect();
            }
        }
    }

    /**
     * Close the connection with the server.
     */
    public void disconnect() {
        this.connect = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
    
    private void setPlayerDeck(String message){
        String[] cards = message.split("/");
        for (int i = 1; i< cards.length; i++){
            this.cards.add(cards[i]);
        }
    }
    
    private String getCardUrl(String card){
        String type = card.substring(0,1);
        String url;
        switch (type) {
            case "B":
                url = getClass().getResource("/images/blue/"+card+".png").toString();
                break;
            default:
                throw new AssertionError();
        }
        return url;
    }

    /**
     * @return the player name.
     * 
     * Return the player name.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName the player name.
     * 
     * Sets the player name.
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @return the connection of the player with the server.
     * 
     * Handle the state of the connection of the player with the server.
     */
    public boolean isConnect() {
        return connect;
    }

    /**
     * @param connect the new value of the connection.
     * 
     * Change the state of the connection.
     */
    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    /**
     * @return if the player is ready to start.
     * 
     * Handle if the player is ready to start.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * @param ready new value of reeady.
     * 
     * Change the state ready of the player.
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
  
}
