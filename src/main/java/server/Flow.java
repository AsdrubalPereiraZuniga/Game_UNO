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
    private int playerPosition = 0;
    private static String playersReadyMessage = "READY/";
    private static String responseStart = "START/";
    private static String responsePUT = "PUT/";
    private static String responseInitialCards = "CARDS/";
    private static String responseActiveButtom = "ACTIVE";
    

    public static boolean doFunctionPlayersReady = true;

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

        enableReadyButtom();

        //envio las varas
     
       // broadcast("TOP/"+Server.cardsQueue.peek().toString());//

        sendInitialCards();

        startListening();

    }

    private void startListening() {
        while (true) {
            try {
                checkPlayersReady();
                String request = this.readFlow.readUTF();
                System.out.println("lola:" + request);
                handleMessage(request);
            } catch (IOException e) {
                if (disconectPlayer()) {
                    break;
                }
            }
        }
    }

    private void handleMessage(String request) {
        String code = request.split("/")[0];
        System.out.println("codee: " + code);
        switch (code) {
            case "READY":

                this.player.setReady(true);
                sendMenssage();
                
                //poner aqui todos los jugadores con wait menos el primero
//                if (aux()) {
//                    putPlayersOnHold();
//                }

                break;
            case "PUT":
                putCardInQueue(request);
                break;
            default:
                System.out.println("No se reccibio nah");
        }
    }
    
    public void sendMenssage(){
        try {
            this.writeFlow.writeUTF(numberOfCardsPerPlayer());
            this.writeFlow.flush();
        } catch (IOException ex) {
            System.out.println("Error al envia mensaje: " + ex);
        }
    }

    public void enableReadyButtom() {
        if (Server.players.size() >= 2) {
            broadcast(responseActiveButtom);
        }
    }

    public static boolean aux() {

        for (Player playerAux : Server.players) {
            if (playerAux.isReady() == false) {
                return false;
            }
        }
        return true;

    }

    private synchronized void putPlayersOnHold() {
        System.out.println("colocando en espera..");
        int index;
        System.out.println("tam array playeers:" + Server.players.size());
        for (index = 0; index < Server.players.size(); index++) {
            if (index != playerPosition) {
                //obtener el flujo del jugador y ponerlo wait
                Flow playerFlow = Server.players.get(index).getFlow();
                System.out.println("Playo en wait:" + Server.players.get(index).getUsername());
                System.out.println("kkkk:" + playerFlow);
                synchronized (playerFlow) {
                    try {
                        playerFlow.wait();
                    } catch (Exception e) {
                        System.out.println("Error al colocar en espera: " + e);
                    }
                }
            }
        }
    }

    private synchronized void handlePlayerTurns() {

    }

    private void checkPlayersReady() {
        System.out.println("mierda esta rady:" + playersReady());

        System.out.println("mierda esta: " + Server.players.size());
        if (Server.players.size() >= 2 && playersReady()) {
            System.out.println("entre check cantidad de players" + Server.players.size());
            broadcast(playersReadyMessage);
            doFunctionPlayersReady = false;
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

    private String numberOfCardsPerPlayer() {
        //String message = "START/";
        responseStart = "START/";

        for (Player playerAux : Server.players) {
            if(!playerAux.getUsername().equals(this.name)){
                
            responseStart += playerAux.getUsername() + ";";
      
         
            responseStart += playerAux.getCards().size() + "/";
            }

        }
        System.out.println("responseStar: " + responseStart);
        return responseStart;
    }

    private static boolean playersReady() {
        if (doFunctionPlayersReady) {
            for (Player playerAux : Server.players) {
                if (playerAux.isReady() == false) {
                    return false;
                }
            }
        }
        return true;
    }

    private void sendInitialCards() {
        //String message = "CARDS/";

        responseInitialCards = "CARDS/";

        for (Card card : this.player.getCards()) {
            responseInitialCards += card.toString() + "/";
        }
        try {

            this.writeFlow.writeUTF(responseInitialCards);
            this.writeFlow.flush();
        } catch (IOException ex) {
            System.out.println("No se pudo enviar las cartas inciales, error:" + ex);
        }

    }

    private synchronized void putCardInQueue(String request) { // Pregunta si se pueden poner mas de dos cartas a por turno
        //String message = "PUT/";

        responsePUT = "PUT/";

        //manejar el turno aqui, para ver si deja que ponga cartas
        String[] cards = request.split("/");

        int index = 1;
        while (index < cards.length) {
            Server.cardsQueue.add(createObjectCard(cards[index]));
            index++;
        }

        //broadcast para que todos vean la carta que se ppuso
        responsePUT += createObjectCard(cards[index - 1]).toString();
        broadcast(responsePUT);

    }

    private Card createObjectCard(String card) {

        String letterCard = card.substring(0, 1);
        System.out.println("letterCar:   " + letterCard);

        String number = card.substring(1);
        System.out.println("kkaka:" + number);

        if ("C".equals(letterCard)) {
            return new WildCard(letterCard, number);
        } else if (Integer.parseInt(number) <= 9) {
            return new NumberCard(letterCard, number);
        } else {
            return new ActionCard(letterCard, number);
        }

    }

    public void returnCardToTheStack() {
        for (Card card : this.player.getCards()) {
            //Server.cardsStack.addLast(card); nose pork no sirve esta pija
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

    private void printPlayerCards() { //luego quito esto
        for (Player playerAux : Server.players) {
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
