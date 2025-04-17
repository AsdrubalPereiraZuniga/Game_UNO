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
import java.util.Arrays;
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
    private boolean invertOrder = true;
    
    private static String playersReadyMessage = "READY/";
    private static String responseStart = "START/";
    private static String responsePUT = "PUT/";
    private static String responseInitialCards = "CARDS/";

    private static String responseActiveButtom = "ACTIVE/";
    private static String responseWAIT = "WAIT/";

    private static ArrayList<String> invertCards
            = new ArrayList<>(Arrays.asList("B12", "G12", "R12", "Y12"));
    private static ArrayList<String> skipCards
            = new ArrayList<>(Arrays.asList("B12", "G12", "R12", "Y12"));

    public static boolean doFunctionPlayersReady = true;

    private static final Object turnLock = new Object();
    private static int currentPlayerIndex = 0;

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

        broadcast("TOP/" + Server.cardsQueue.peek().toString());//
        sendInitialCards();

        startListening();

    }

    private void startListening() {
        while (true) {
            try {
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
                checkPlayersReady();
                sendMenssageToClient(numberOfCardsPerPlayer(), "Error al envia mensaje: ");

                if (playersReady()) {
                    System.out.println("sppopo" + this.currentPlayerIndex);
                    putPlayersOnHold();
                }
                break;
            case "PUT":
                putCardInQueue(request);
                break;
            default:
                System.out.println("No se reccibio nah");
        }
    }

    public void sendMenssageToClient(String message, String error) {
        try {
            this.writeFlow.writeUTF(message);
            this.writeFlow.flush();
        } catch (IOException ex) {
            System.out.println(error + ex);
        }
    }

    public void enableReadyButtom() {
        if (Server.players.size() >= 2) {
            broadcast(responseActiveButtom);
        }
    }

    private void putPlayersOnHold() {
        synchronized (turnLock) {
            for (int i = 0; i < Server.players.size(); i++) {
                if (i != currentPlayerIndex) {
                    Flow playerFlow = Server.players.get(i).getFlow();
                    playerFlow.sendMenssageToClient(responseWAIT, "Error al poner en espera");
                } else {
                    
                    Flow currentFlow = Server.players.get(currentPlayerIndex).getFlow();
                    currentFlow.sendMenssageToClient("YOUR_TURN/", "Error al notificar turno");
                }
            }
        }
    }

    private synchronized void changeTurn(Card playedCard) {
        synchronized (turnLock) {
            Server.players.get(currentPlayerIndex).getFlow()
                    .sendMenssageToClient(responseWAIT, "Error al poner en espera");
           
            handlePlayerTurns(playedCard);

            Flow nextPlayerFlow = Server.players.get(currentPlayerIndex).getFlow();
            nextPlayerFlow.sendMenssageToClient("TURN/", "Error al notificar turno");

            turnLock.notifyAll();
        }
    }

    private synchronized void handlePlayerTurns(Card topCard) {

        int aux = 1;

        checkSkipCards(topCard, aux);

        checkInvertOrderOfPlayers(topCard);

        handlePosition(aux);

        //check cancelation of tunr
        checkLimitsOfVectorPlayers();// falta con la de skip

        //  putPlayersOnHold();
    }

    private void checkSkipCards(Card topCard, int aux) {
        for (String skipCard : skipCards) {
            if (skipCard.equals(topCard.toString())) {
                aux = aux + 1;
            }
        }
    }

    private void checkInvertOrderOfPlayers(Card topCard) {

        for (String invertCard : invertCards) {
            if (invertCard.equals(topCard.toString())) {
                this.invertOrder = !this.invertOrder;
            }
        }
    }

    private void handlePosition(int aux) {
        if (this.invertOrder) {
            this.currentPlayerIndex -= aux;
        } else {
            this.currentPlayerIndex += aux;
        }
    }

    private void checkLimitsOfVectorPlayers() { //esto le falta que si esata con el ultimo y es un skip se brinque al 0
        if (this.currentPlayerIndex > Server.players.size()) {
            this.currentPlayerIndex = 0;
        } else if (this.currentPlayerIndex < 0) {
            this.currentPlayerIndex = Server.players.size();
        }
    }

    private synchronized void putCardInQueue(String request) {
        responsePUT = "PUT/";

        //manejar el turno aqui, para ver si deja que ponga cartas
        String[] cards = request.split("/");

        int index = 1; //1 pork 0 es la peticion
        while (index < cards.length) {
            Server.cardsQueue.add(createObjectCard(cards[index]));
            index++;
        }

        //broadcast para que todos vean la carta que se ppuso
        responsePUT += createObjectCard(cards[index - 1]).toString();
        broadcast(responsePUT);

        //O despues de colocar una carta cambiar de turno dependiendo de la carta puesta
//        handlePlayerTurns(createObjectCard(cards[index - 1]));
        changeTurn(createObjectCard(cards[index - 1]));

    }

    private void checkPlayersReady() {

        if (doFunctionPlayersReady && Server.players.size() >= 2 && playersReady()) {
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
        responseStart = "START/";

        for (Player playerAux : Server.players) {
            if (!playerAux.getUsername().equals(this.name)) {
                responseStart += playerAux.getUsername() + ";";
                responseStart += playerAux.getCards().size() + "/";
            }
        }
        return responseStart;
    }

    private static boolean playersReady() {
        for (Player playerAux : Server.players) {
            if (playerAux.isReady() == false) {
                return false;
            }
        }
        return true;
    }

    private void sendInitialCards() {
        responseInitialCards = "CARDS/";

        for (Card card : this.player.getCards()) {
            responseInitialCards += card.toString() + "/";
        }
        sendMenssageToClient(responseInitialCards, "No se pudo enviar las cartas inciales, error: ");

    }

    private Card createObjectCard(String card) {

        String letterCard = card.substring(0, 1);
        String number = card.substring(1);

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
            Server.cardsStack.add(card);
        }
    }

    public synchronized ArrayList<Card> distributeCards() {

        ArrayList<Card> playerCards = new ArrayList<>();

        if (Server.cardsStack.size() >= 7 && !Server.cardsStack.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                Card card = Server.cardsStack.pop();
                playerCards.add(card);
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
