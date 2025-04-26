package client;

import cards.ActionCard;
import cards.Card;
import cards.NumberCard;
import cards.WildCard;
import controllers.MainController;
import controllers.WaitingController;
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

    private boolean connect;
    private boolean ready;
    private int port;
    private ArrayList<Card> cards;
    private DataInputStream input;
    private DataOutputStream output;
    private Socket socket;
    private String playerName;
    private String host;
    private Thread listenerThread;
    private ArrayList<OtherPlayers> otherPlayers;
    private boolean forbidden;
    private boolean activeButton;
    private Card topCard;
    private boolean waiting;
    private boolean myTurn = false;
    private final Object turnLock = new Object();
    private boolean firstTime = true;

    private MainController mainController;

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
    /**
     * *
     *
     * cuando entra el mensaje ACTUAL, mostrar el nombre del turno del jugador
     * en la pantalla
     */
    private void processServerMessage(String message) {
        System.out.println("message2222: " + message);
        String messageCode = message.split("/")[0];
        System.out.println("wait: " + this.waiting);
        switch (messageCode) {
            case "NEWCARDS":
                refreshCards(message);
                break;
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
                System.out.println("patodooooosxxxxxxxxxxxxxxxxx");
                String[] parts = message.split("/");
                System.out.println("nose: " + message);
                if (parts.length > 1) {
                    String currentPlayer = parts[1];
                    String cardsSize = parts[2];
                    //TurnHandler.updateTurn(currentPlayer);
                    TurnHandler.updateTurn(currentPlayer + ": " + cardsSize);
                } else {
                    System.err.println("Formato de mensaje ACTUAL inválido: " + message);
                }
                break;
            default:
                System.out.println(message);
        }
    }

    public void refreshCards(String message) {
        String[] deck = message.split("/");
        System.out.println("DECK: " + deck.length);

        this.cards.add(getCard(deck[2]));

        TurnHandler.updateTurn(deck[1] + ": " + String.valueOf(this.cards.size()));
    }

    private void setTopCard(String message) {
        String value = message.split("/")[1];
        this.topCard = getCard(value);
        ViewCardsHandler.updateUsedViewCard(getNewCard(topCard));
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

    private void setPlayerDeck(String message) {
        String[] deck = message.split("/");
        System.out.println("DECK: " + deck.length);
        this.cards.clear();
        boolean isNewCard = (deck.length == 2); // Solo una carta nueva, se usó DRAW/
        for (int i = 1; i < deck.length; i++) {
            this.cards.add(getCard(deck[i]));
        }

        if (!firstTime) {
            Platform.runLater(() -> {
                MainController.getInstanceController().refreshHand(); // ⬅️ nuevo método
            });
        }
        firstTime = false;

    }

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

    private void initializeOtherPlayers(String message) {
        this.otherPlayers.clear();
        String[] players = message.split("/");
        String name;
        int amountOfCards;
        String[] data;
        for (int i = 1; i < players.length; i++) {
            System.out.println("i: " + i);
            data = players[i].split(";");
            name = data[0];
            amountOfCards = Integer.parseInt(data[1]);
            this.otherPlayers.add(new OtherPlayers(name, amountOfCards));
        }
    }

    private void setWaitingMode(boolean waiting) {
        synchronized (turnLock) {
            myTurn = !waiting;
            if (!waiting) {
                turnLock.notifyAll(); // Despertar al hilo principal
            }
        }
        // Actualizar UI (disable/enable controles)
    }

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
            // Fallback a Label si no hay imagen
            String cardText = card.getColor() + card.getValue();
            Label label = new Label(cardText);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            cardContainer.getChildren().add(label);
        }
        return cardContainer;
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

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public ArrayList<OtherPlayers> getOtherPlayers() {
        return otherPlayers;
    }

    public void setOtherPlayers(ArrayList<OtherPlayers> otherPlayers) {
        this.otherPlayers = otherPlayers;
    }

    public boolean isForbidden() {
        return forbidden;
    }

    public void setForbidden(boolean forbidden) {
        this.forbidden = forbidden;
    }

    public boolean isActiveButton() {
        return activeButton;
    }

    public void setActiveButton(boolean activeButton) {
        this.activeButton = activeButton;
    }

    public Card getTopCard() {
        return topCard;
    }

    public void setTopCard(Card topCard) {
        this.topCard = topCard;
    }

    public boolean isWaiting() {
        return waiting;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

}
