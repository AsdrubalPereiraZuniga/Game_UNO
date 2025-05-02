package client;

import cards.ActionCard;
import cards.Card;
import cards.NumberCard;
import cards.WildCard;
import com.mycompany.game_uno_so.App;
import controllers.MainController;
import controllers.WaitingController;
import controllers.WinnerController;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import server.Server;

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

    private boolean activeButton;
    private boolean connect;
    private boolean firstTime = true;
    private boolean forbidden;
    private boolean myTurn = false;
    private boolean ready;
    private boolean waiting;
    private int port;
    private ArrayList<Card> cards;
    private ArrayList<OtherPlayers> otherPlayers;
    private Card topCard;
    private DataInputStream input;
    private DataOutputStream output;
    private MainController mainController;
    private final Object turnLock = new Object();
    private Socket socket;
    private String host;
    private String playerName;
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
        this.forbidden = false;
        this.otherPlayers = new ArrayList<>();
        this.activeButton = false;
        this.topCard = null;
        this.waiting = false;

        initializeConnection();
        startListening();
    }        
    
    /**
     * Initialize the connecction to the server.
     */
    private void initializeConnection() {
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
    private void connectToServer() {
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
                    disconnect(playerName);
                    break;
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Processes the message received from the server.
     *
     * Handle the message of the server.
     * 
     * CARDS: refresh the players deck. 
     * READY: set the player state to ready.
     * FORBIDDEN: don´t let the player access to an already start game. 
     * START: starts the game. 
     * TOP: put the firts card to be played. 
     * PUT: change the top card played. 
     * ACTIVE: change the state of the ready button. 
     * WAIT: set the player to wait mode. 
     * TURN: notify the player that its his/her turn.
     * ACTUAL: show the actual player.
     * SAY_ONE show say one animation in view
     * WINNER show the winner view with the winner
     * DISCONNECT disconnect a client of game
     * RESTART It is used when a player restarts the game
     * @param message the message received from the server
     */
    private void processServerMessage(String message) {
        String messageCode = message.split("/")[0];
        switch (messageCode) {
            case "CARDS":
                setPlayerDeck(message);
                break;
            case "READY":
                this.ready = true;
                break;
            case "FORBIDDEN":
                this.forbidden = true;
                break;
            case "START":
                initializeOtherPlayers(message);
                break;
            case "TOP":
                String value = message.split("/")[1];
                this.topCard = getCard(value);
                break;
            case "PUT":
                setTopCard(message);
                break;
            case "ACTIVE":
                this.activeButton = true;
                break;
            case "WAIT":
                this.waiting = true;
                setWaitingMode(this.waiting);
                break;
            case "TURN":
                this.waiting = false;
                setWaitingMode(this.waiting);
                break;
            case "ACTUAL":
                setActualPlayer(message);
                break;
            case "SAY_ONE":
                showSayOne(message.split("/")[1] );
                break;
            case "WINNER":
                showWinnerScreen(message.split("/")[1] );
               break;
            case "DISCONNECT":
                this.otherPlayers.clear();
                this.cards.clear();
                disconnect( message.split("/")[1] );
                break;
            case "RESTART":
                restarGame(message.split("/")[1]);
            default:
                System.out.println(message);
        }
    }
    
    /**
     * Method to handle the game restart event for the client
     * all player are disconnected from the server
     * 
     * @param message name of a player
     */
    public void restarGame(String message){                
        this.otherPlayers.clear();
        this.cards.clear();
        HandleCards.getInstace().clear();
        ViewCardsHandler.clear();
        TurnHandler.clear();
        SayOneHandler.clear();
        this.otherPlayers.clear();             
        disconnect( message );
    }
    
    /**
     * Method to handle the event of displaying the SAY ONE animation.
     * 
     * @param name Player name that says ONE
     */     
    public void showSayOne(String name){
        Platform.runLater(() ->{
            SayOneHandler.showSayOne(name);
        });
    }
    
    /**
     * Method for managing the winner view event
     * 
     * @param winnerName winner name of game
     */
    public void showWinnerScreen(String winnerName){
        if(this.connect){
            WinnerController.setClient(
                    MainController.getInstanceController().getClient());
            
            WinnerController.setWinnerName(winnerName);
            Platform.runLater(()-> {
               App.setRoot("WinnerScreen"); 
            });                                
        }
    }
    
    /**
     * Handle the actual player chande.
     * 
     * @param message the message from the server.
     */
    private void setActualPlayer(String message) {
        String[] parts = message.split("/");
        if (parts.length > 1) {
            String currentPlayer = parts[1];
            String cardsSize = parts[2];
            TurnHandler.updateTurn(currentPlayer + " > " + cardsSize);
        } else {
            System.err.println("Formato de mensaje ACTUAL inválido: " + message);
        }
    }
    
    /**
     * Updates the player's hand when receiving new cards.
     *
     * @param message the server message containing card information
     */
    public void refreshCards(String message){
        String[] deck = message.split("/");
        this.cards.add(getCard(deck[2]));

        TurnHandler.updateTurn(deck[1] + " > " + String.valueOf(this.cards.size()));
    } 
    
    /**
     * Updates the top card placed on the table.
     *
     * @param message the server message containing the top card information
     */
    private void setTopCard(String message) {        
        String value = message.split("/")[1];        
        this.topCard = getCard(value);
        ViewCardsHandler.updateUsedViewCard(getNewCard(topCard));
    }
    
    /**
     * Send a message to the server.
     * 
     * @param message the message from the server.
     */
    public void sendMessage(String message) {
        if (connect) {
            try {
                output.writeUTF(message);
                output.flush();
            } catch (IOException e) {
                System.out.println("Error sending message: " + e.getMessage());
                disconnect(this.playerName);
            }
        }
    }

    /**
     * Close the connection with the server.
     * @param name
     */
    public void disconnect(String name) {        
        this.connect = false;
        try {
            if (socket != null && !socket.isClosed() && this.playerName.contains(name)) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Updates the player's hand with cards received from the server.
     *
     * @param message the server message containing the full player deck
     */
    private void setPlayerDeck(String message) {
        String[] deck = message.split("/");
        this.cards.clear();
        for (int i = 1; i < deck.length; i++) {
            this.cards.add(getCard(deck[i]));
        }

        if (!firstTime) {
            Platform.runLater(() -> {
                MainController.getInstanceController().refreshHand();
            });
        }
        firstTime = false;

    }

    /**
     * Converts a string code into a Card object (NumberCard, ActionCard, or WildCard).
     *
     * @param card the string representation of the card
     * @return a new Card object corresponding to the code
     */
    private Card getCard(String card) {
        String code = card.substring(0, 1);
        String value = card.substring(1);
        if (code.equals("C")) {
            return new WildCard(code, value);
        }
        if (Integer.parseInt(value) > 9) {
            return new ActionCard(code, value);
        }
        if (Integer.parseInt(value) < 10) {
            return new NumberCard(code, value);
        }
        return null;
    }

    /**
     * Initializes the list of other players in the game.
     *
     * @param message the server message containing other players' information
     */
    private void initializeOtherPlayers(String message) {
        this.otherPlayers.clear();
        String[] players = message.split("/");
        String name;
        int amountOfCards;
        String[] data;
        for (int i = 1; i < players.length; i++) {
            data = players[i].split(";");
            name = data[0];
            amountOfCards = Integer.parseInt(data[1]);
            this.otherPlayers.add(new OtherPlayers(name, amountOfCards));
        }
    }

    /**
     * Sets the waiting mode for the player (true if waiting for turn).
     *
     * @param waiting whether the player must wait for their turn
     */
    private void setWaitingMode(boolean waiting) {
        synchronized (turnLock) {
            myTurn = !waiting;
            if (!waiting) {
                turnLock.notifyAll();
                turnLock.notifyAll();
            }
        }
    }

    /**
     * Blocks the execution until it is the player's turn.
     */
    public void waitForTurn() {
        synchronized (turnLock) {
            while (!myTurn) {
                try {
                    turnLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * Creates a visual representation of a card as a VBox container.
     *
     * @param card the card to visualize
     * @return the VBox containing the card's image
     */
    private VBox getNewCard(Card card) {
        VBox cardContainer = new VBox();
        double NORMAL_WIDTH = HandleCards.getInstace().getNORMAL_WIDTH();
        double CARD_HEIGHT = HandleCards.getInstace().getCARD_HEIGTH();
        cardContainer.setPrefSize(NORMAL_WIDTH, CARD_HEIGHT);
        cardContainer.setStyle("-fx-background-color: white; -fx-border-color: "
                + "#333; -fx-border-radius: 5;");
        cardContainer.setAlignment(Pos.CENTER);

        try {
            ImageView cardImage = new ImageView(new Image(
                    card.getImagePath()));
            cardImage.setPreserveRatio(true);
            cardImage.setFitWidth(NORMAL_WIDTH - 10);
            cardContainer.getChildren().add(cardImage);
        } catch (Exception e) {
            String cardText = card.getColor() + card.getValue();
            Label label = new Label(cardText);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            cardContainer.getChildren().add(label);
        }
        return cardContainer;
    }

    /**
     * Return the player name.
     * 
     * @return the player name.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * Sets the player name.
     * 
     * @param playerName the player name.
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    /**
     * Handle the state of the connection of the player with the server.
     * 
     * @return the connection of the player with the server.
     */
    public boolean isConnect() {
        return connect;
    }

    /**
     * Change the state of the connection.
     * 
     * @param connect the new value of the connection.
     */
    public void setConnect(boolean connect) {
        this.connect = connect;
    }

    /**
     * Handle if the player is ready to start.
     * 
     * @return if the player is ready to start.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Change the state ready of the player.
     * 
     * @param ready new value of reeady
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }
    
    /**
     * Returns the player deck.
     * 
     * @return the player deck.
     */
    public ArrayList<Card> getCards() {
        return cards;
    }

    /**
     * Change the playeer deck.
     * 
     * @param cards new deck.
     */
    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    /**
     * Return the other players data.
     * 
     * @return the other players data.
     */
    public ArrayList<OtherPlayers> getOtherPlayers() {
        return otherPlayers;
    }

    /**
     * Change the other players arraylist.
     * 
     * @param otherPlayers new other player arraylist.
     */
    public void setOtherPlayers(ArrayList<OtherPlayers> otherPlayers) {
        this.otherPlayers = otherPlayers;
    }

    /**
     * Return the state of forbidden.
     * 
     * @return the state of forbidden.
     */
    public boolean isForbidden() {
        return forbidden;
    }

    /**
     * Change the state of forbidden.
     * 
     * @param forbidden new state of forbidden.
     */
    public void setForbidden(boolean forbidden) {
        this.forbidden = forbidden;
    }

    /**
     * Return the state of the active button.
     * 
     * @return the state of the active button.
     */
    public boolean isActiveButton() {
        return activeButton;
    }

    /**
     * Change the state of the active button.
     * 
     * @param activeButton new state of the active button.
     */
    public void setActiveButton(boolean activeButton) {
        this.activeButton = activeButton;
    }

    /**
     * Return the top card.
     * 
     * @return the top card.
     */
    public Card getTopCard() {
        return topCard;
    }

    /**
     * Change the top card.
     * 
     * @param topCard new top card.
     */
    public void setTopCard(Card topCard) {
        this.topCard = topCard;
    }

    /**
     * Return the state of waiting.
     * 
     * @return the state of waiting.
     */
    public boolean isWaiting() {
        return waiting;
    }

    /**
     * Change the state of waiting.
     * 
     * @param waiting new state of waiting.
     */
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    /**
     * Change the main controller.
     * 
     * @param controller main controller.
     */
    public void setMainController(MainController controller) {
        this.mainController = controller;
    }
        
}
