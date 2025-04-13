/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import cards.ActionCard;
import cards.Card;
import cards.NumberCard;
import cards.WildCard;
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
import static server.Server.cardsStack;
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
    private static String playersReadyMessage = "READY/";
    public static boolean doFunctionPlayersReady = true;

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

        printPlayerCards();

        //envio las varas
        System.out.println("ultima card de la cola:" + Server.cardsQueue.peek().toString());
        broadcast(Server.cardsQueue.peek().toString());
        sendInitialCards();

        starListening();
    }

    private void starListening() {
        while (true) {
            try {
                System.out.println("mierda esta rady:" + playersReady());

                System.out.println("mierda esta: " + Server.players.size());
                if (Server.players.size() >= 2 && playersReady()) {
                    System.out.println("entre check cantidad de players" + Server.players.size());
                    broadcast(playersReadyMessage);
                    doFunctionPlayersReady = false;
                }
                String message = this.readFlow.readUTF();
                System.out.println("lola:" + message);
                handleMessage(message);
            } catch (IOException e) {
                if (disconectPlayer()) {
                    break;
                }
            }
        }
    }

    private boolean disconectPlayer() {

        try {
            returnCardToTheStack();
            Server.players.removeElement(this.player);
            broadcast("El jugador " + this.name + "se ha desconectado");
            this.socket.close();
            System.out.println("El jugador " + this.name + "se ha desconectado");
        } catch (IOException ex) {
            System.out.println("Erro cerrando la conexion :" + ex);
        }

        return true;

    }

    private void handleMessage(String message) {
        String code = message.split("/")[0];
        System.out.println("codee: " + code);
        switch (code) {
            case "READY":
                this.player.setReady(true);
                //Enviar los players con la cant de cards
                broadcast(numberOfCardsPerPlayer());
                break;
            case "PUT":
                putCardInQueue(message);
                break;
            default:
                System.out.println("Message");
        }
    }

    private String numberOfCardsPerPlayer() {
        String message = "START/";
        for (Player playerAux : Server.players) {
            message += playerAux.getUsername() + ";";
            int cont = 0;
            while (cont < playerAux.getCards().size()) {
                cont++;
            }
            message += cont + "/";
        }
        return message;
    }

    private static boolean playersReady() {
        if (doFunctionPlayersReady) {
            for (Player playerAux : Server.players) {
                System.out.println("kiki: " + playerAux.isReady());
                if (playerAux.isReady() == false) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendInitialCards() { // mejorar para que sea general luego
        String message = "CARDS/";

        for (Card card : this.player.getCards()) {
            message += card.toString() + "/";
        }

        try {

            this.writeFlow.writeUTF(message);
            this.writeFlow.flush();
        } catch (IOException ex) {
            System.out.println("No se pudo enviar las cartas inciales, error:" + ex);
        }

    }

    private void putCardInQueue(String message) { // Pregunta si se pueden poner mas de dos cartas a por turno

        //int cant = message.split("/").length -1;
        String card = message.split("/")[1];

        Server.cardsQueue.add(createObjectCard(card));
           
       

    }

    private Card createObjectCard(String card) {

        String letterCard = card.substring(0, 1);
        System.out.println("letterCar:   " + letterCard);

        String number = card.substring(1);
        System.out.println("kkaka:" + number);

        if ("C".equals(letterCard)) {
            return new WildCard(letterCard, number);
        } else {
            if (Integer.parseInt(number) <= 9) {
                return new NumberCard(letterCard, number);
            } else {
                return new ActionCard(letterCard, number);
            }
        }
    }

    public void returnCardToTheStack() {
        for (Card card : this.player.getCards()) {
            //Server.cardsStack.addLast(card); nose pork sirve esta pija
            Server.cardsStack.add(card);
        }
    }

    public synchronized ArrayList<Card> distributeCards() {

        ArrayList<Card> playerCards = new ArrayList<>();

        if (Server.cardsStack.size() >= 7 && !Server.cardsStack.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                Card card = Server.cardsStack.pop();
                playerCards.add(card);
                System.out.println("card" + card.toString());

            }
        }

        return playerCards;
    }

    private void printPlayerCards() {
        for (Player playerAux : Server.players) { //luego quito esto
            System.out.println("Cartas de un player");
            for (Card card : playerAux.getCards()) {
                System.out.println(card.toString());
            }
        }
    }

    public void broadcast(String message) {
        synchronized (Server.players) {
            Enumeration enumeration = Server.players.elements();
            while (enumeration.hasMoreElements()) {
                Player playerAux = (Player) enumeration.nextElement();
                Flow flow = (Flow) playerAux.getFlow();
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
