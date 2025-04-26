package server;

import cards.ActionCard;
import cards.Card;
import cards.NumberCard;
import cards.WildCard;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;
import players.Player;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/**
 * Server class that handles TCP socket connections.
 * It listens on a specified port, manages incoming client connections,
 * and initializes the game deck and game state.
 * 
 * It uses Flow to manage communication with each connected client.
 * 
 * Authors: Jorge
 */
public class Server {

    /**
     * Port number on which the server listens for incoming connections
     */
    private static final int PORT = 8000;

    public static Vector<Player> players = new Vector();
    public static Stack<Card> cardsStack = new Stack<>();
    public static Queue<Card> cardsQueue = new LinkedList<>();
    private static String responseForbidden = "FORBIDDEN/";

    public static ArrayList<String> colors = new ArrayList<>(Arrays.asList("R", "G", "B", "Y"));
    public ArrayList<String> values = new ArrayList<>(Arrays.asList(""));
    public static int numberOfCardsByColor = 12;

    /**
     * initializes the servesr and starts listening for client connections.
     *
     * @param args arguments not used.
     */
    public static void main(String args[]) {

        ServerSocket serverSocket = initServer(PORT);

        if (serverSocket != null) {
            initConnection(serverSocket);
        }

    }

    /**
     * Initializes the server on the given port.
     *
     * @param PORT The port number on which the server should listen.
     * @return A ServerSocket instance if initialization succeeds, or null
     * otherwise.
     */
    public static ServerSocket initServer(int PORT) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("The server has stared...");
            initDeck();
            System.out.println("Waiting for players");
            return serverSocket;
        } catch (IOException ioe) {
            System.out.println("Connection refused." + ioe);
            System.exit(1);
        }
        return null;
    }

    /**
     * Continuously waits for client connections and starts a new thread to
     * handle each connection using the Flow class.
     *
     * @param serverSocket The ServerSocket instance to accept client
     * connections from.
     */
    public static void initConnection(ServerSocket serverSocket) {

        while (true) {

            try {
                Socket socket = serverSocket.accept();
                DataInputStream readFlow = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                String playerName = readFlow.readUTF().split("/")[0];
                System.out.println("Connection accepted by " + playerName);

                createNewFlow(socket, playerName);

            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

        }

    }

    /**
     * Creates a new Flow thread for a player if the game has not already started.
     *
     * @param socket the socket for communication with the player
     * @param playerName the name of the connecting player
     */
    public static void createNewFlow(Socket socket, String playerName) {
        if (Flow.doFunctionPlayersReady) {
            Thread flow = new Thread(new Flow(socket, playerName));

            flow.start();
        } else {
            gameHasAlreadyBugun(socket);
        }
    }

    /**
     * Sends a forbidden response to a client trying to join after the game has started.
     *
     * @param socket the socket to send the forbidden message to
     */
    public static void gameHasAlreadyBugun(Socket socket) {

        try {
            DataOutputStream writeFlow = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));
            writeFlow.writeUTF(responseForbidden);
            writeFlow.flush();
        } catch (IOException ex) {
            System.out.println("Error al indicar que el juego incio");
            ex.printStackTrace();
        }

    }

    /**
     * Initializes the deck by adding cards to the stack, shuffling them,
     * and setting the first card into the queue.
     */
    public static void initDeck() {

        System.out.println("Vamo a crear el mazo");

        addCardsToStackAndShuffle();

        getTopCardOfStack();

        showStack();
    }

    /**
    * Selects the first valid card to start the game from the deck.
    * 
    * It ensures that the top card is not a wild card ("C") or an action card (value >= 10).
    * If the top card is invalid, the deck is shuffled until a valid card is found.
    * Once a valid card is found, it is moved from the stack to the queue to begin the game.
    */
    private static void getTopCardOfStack() {

        Card card = cardsStack.peek();

        while (card.getColor().equals("C") || Integer.parseInt(card.getValue()) >= 10) {
            System.out.println("Pasoooooooooooooooooooooooooooooooooooooooooo");
            Collections.shuffle(cardsStack);
            card = cardsStack.peek();
        }
        cardsQueue.add(cardsStack.pop());

    }

    /**
     * Displays all cards currently in the stack (for debugging).
     */
    public static void showStack() {
        for (int i = 0; i < cardsStack.size(); i++) {

            System.out.println(cardsStack.get(i).toString());
        }
    }

    /**
     * Adds all game cards (number, action, and wild cards) to the stack and shuffles it.
     */
    public static void addCardsToStackAndShuffle() {

        for (String color : colors) {
            for (int i = 1; i <= numberOfCardsByColor; i++) {
                if (i <= 9) {
                    Card numberCard = new NumberCard(color, Integer.toString(i));
                    cardsStack.push(numberCard);
                } else {
                    Card actionCard = new ActionCard(color, Integer.toString(i));
                    cardsStack.push(actionCard);
                }
            }
            Card numberCardZero = new NumberCard(color, "0");
            cardsStack.push(numberCardZero);
            Card changeColor = new WildCard("C", "0");
            cardsStack.push(changeColor);
            Card addFour = new WildCard("C", "1");
            cardsStack.push(addFour);
        }

        Collections.shuffle(cardsStack);
    }
}
