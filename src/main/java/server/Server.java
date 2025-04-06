/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import cards.Card;
import cards.NumberCard;
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
import java.util.Stack;

/**
 * Server class that handles TCP socket connections. It listens on a specified
 * port and creates a new thread to manage each incoming client connection.
 *
 * @author jorge
 */
public class Server {

    /**
     * Port number on which the server listens for incoming connections
     */
    private static final int PORT = 8000;

    public static Vector<Player> players = new Vector();
    public static Stack<Card> cardsStack = new Stack<>();
    public static ArrayList<String> colors = new ArrayList<>(Arrays.asList("R", "G", "B", "Y"));
    public ArrayList<String> values = new ArrayList<>(Arrays.asList(""));//POCUPO SABER QUE VA TENER LOS VALUES PARA VER SI DEJO ESTA KK O QUE SE QUEDE EN UN FOR
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
                String name = readFlow.readUTF();
                System.out.println("Connection accepted " + name);

                // Thread flow = new Thread(new Flow(socket, name, cardsStack));
                Thread flow = new Thread(new Flow(socket, name));

                flow.start();

            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

        }

    }

    public static void initDeck() {

        System.out.println("Vamo a crear el mazo");
        
        addCardsToStack();
        
        while(!cardsStack.isEmpty()){
            System.out.println(cardsStack.pop());
        }

    }

    
    public static void addCardsToStack() { //Falta las cartas especiales que el culiao de fabian no me dice

        for (String color : colors) {
            for (int i = 0; i <= numberOfCardsByColor; i++) {
                Card card = new NumberCard(color, Integer.toString(i));
                cardsStack.push(card);
            }
        }
        
        Collections.shuffle(cardsStack);

    }

}
