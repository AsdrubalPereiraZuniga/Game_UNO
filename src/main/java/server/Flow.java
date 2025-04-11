/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import cards.Card;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Stack;
import players.Player;
import server.Server;
import static server.Server.players;

/**
 *
 * @author jorge
 */
public class Flow implements Runnable {

    private Socket socket;
    private DataInputStream readFlow;
    private DataOutputStream writeFlow;
    private String name;
    private Player player;
    private static String playersReady = "READY/";
    private static boolean doFunctionPlayersReady = true;

    // private Stack<Card> cardsStack = new Stack<>();
    public Flow(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
        // this.cardsStack = cardsStack;
        try {
            readFlow = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            writeFlow = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException ioe) {
            System.out.println("IOException(Flujo): " + ioe);
        }

    }

    @Override
    public void run() {
        this.player = new Player(this, this.name, distributeCards());

        Server.players.add(this.player);
        System.out.println("player added");

        for (Player player : Server.players) {
            System.out.println("Cartas de un player");
            for (Card card : player.getCards()) {
                System.out.println(card.toString());
            }
        }

        //envio las varas
        sendInitialCards();

        while (true) {
            if (Server.players.size() >= 2 && playersReady()) {
                broadcast(playersReady);
            }
        }

    }

    private static boolean playersReady() {
        if (doFunctionPlayersReady) {
            for (Player player : players) {
                if (player.isReady() == false) {
                    return false;
                }
            }
            doFunctionPlayersReady = false;
        }
        return true;
    }

    private void sendInitialCards() { // mejorar para que sea general luego
        String message = "CARDS/";

        for (Card card : this.player.getCards()) {
            message += card.toString() + "/";

        }
        System.out.println("kkkkk: " + message);

        try {

            this.writeFlow.writeUTF(message);
            this.writeFlow.flush();
        } catch (IOException ex) {
            System.out.println("No se pudo enviar las cartas inciales, error:" + ex);
        }

    }

    public synchronized ArrayList<Card> distributeCards() {

        ArrayList<Card> playerCards = new ArrayList<>();

        System.out.println("entro a dis");
        System.out.println(Server.cardsStack.size());

        System.out.println(Server.cardsStack.size() >= 7);
        System.out.println(!Server.cardsStack.isEmpty());
        if (Server.cardsStack.size() >= 7 && !Server.cardsStack.isEmpty()) {
            System.out.println("Entro al if");
            for (int i = 0; i < 7; i++) {
                Card card = Server.cardsStack.pop();
                playerCards.add(card);
                System.out.println("card" + card.toString());

            }
        }

        return playerCards;
    }

    public void broadcast(String message) {
        synchronized (Server.players) {
            Enumeration enumeration = Server.players.elements();
            while (enumeration.hasMoreElements()) {
                Player player = (Player) enumeration.nextElement();
                Flow flow = (Flow) player.getFlow();
                try {
                    synchronized (flow.writeFlow) {
                        flow.writeFlow.writeUTF(message);
                        flow.writeFlow.flush();
                    }
                } catch (IOException ioe) {
                    System.out.println("Error: " + ioe);
                }
            }
        }
    }

}
