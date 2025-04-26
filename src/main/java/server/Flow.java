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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import players.Player;
import static server.Server.cardsStack;

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

    private int skipAmount = 0;
    private static boolean invertOrder = false;
    public static boolean doFunctionPlayersReady = true;
    private static String playersReadyMessage = "READY/";
    private static String responseActiveButtom = "ACTIVE/";
    private static String responseWAIT = "WAIT/";
    private static String responseTURN = "TURN/";
    private static String responseACTUAL = "ACTUAL/";

    private static String responseTOP = "TOP/";

    private static int currentPlayerIndex = 0;
    private static final Object turnLock = new Object();//para que solo un hilo a la vez lo pueda usar

    private static ArrayList<String> invertCards
            = new ArrayList<>(Arrays.asList("B12", "G12", "R12", "Y12"));
    private static ArrayList<String> skipCards
            = new ArrayList<>(Arrays.asList("B11", "G11", "R11", "Y11"));

    public Flow(Socket socket, String name) {
        this.socket = socket;
        this.name = name;
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

        enableReadyButtom();

        broadcast(responseTOP + Server.cardsQueue.peek().toString());

        sendInitialCards();

        startListening();
    }

    private synchronized ArrayList<Card> distributeCards() {
        ArrayList<Card> playerCards = new ArrayList<>();

        if (Server.cardsStack.size() >= 7 && !Server.cardsStack.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                Card card = Server.cardsStack.pop();
                playerCards.add(card);
            }
        }
        return playerCards;
    }

    private void enableReadyButtom() {
        if (Server.players.size() >= 2) {
            broadcast(responseActiveButtom);
        }
    }

    private void sendInitialCards() {
        String responseInitialCards = "CARDS/";

        for (Card card : this.player.getCards()) {
            responseInitialCards += card.toString() + "/";
        }
        sendMenssageToClient(responseInitialCards,
                "No se pudo enviar las cartas inciales, error: ");
    }

    private void startListening() {
        while (true) {
            try {
                String request = this.readFlow.readUTF();
                System.out.println("lola: " + request);
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
                sendMenssageToClient(numberOfCardsPerPlayer(),
                        "Error al envia mensaje: ");
                putPlayersOnHold();

                broadcast(responseACTUAL
                        + Server.players.get(currentPlayerIndex).getUsername() + "/" + Server.players.get(currentPlayerIndex).getCards().size() + "/");
                break;
            case "PUT":
                putCardInQueue(request);
                break;
            case "GET_TURN":
                broadcast(responseACTUAL
                        + Server.players.get(currentPlayerIndex).getUsername() + "/" + Server.players.get(currentPlayerIndex).getCards().size() + "/");
                break;
            case "DRAW":
                giveCardToPlayer();
                break;
            case "COLORSELECTED":
                broadcast("PUT/" + request.split("/")[1]);
                break;

            default:
                System.out.println("No se reccibio nah");
        }
    }

    private void giveCardToPlayer() {
        if (!Server.cardsStack.isEmpty()) {
            Card drawnCard = Server.cardsStack.pop();
            Server.players.get(currentPlayerIndex).getCards().add(drawnCard);
            sendMenssageToClient("CARDS/" + getPlayerCards() + "/", "No se pudo enviar carta robada");
        } else {
            restockStack();
        }
    }

    private String getPlayerCards() {
        String playerCards = "";
        for (Card card : Server.players.get(currentPlayerIndex).getCards()) {
            playerCards += card.toString() + "/";
        }
        return playerCards;
    }

    private void restockStack() {
        for (int i = 0; i < Server.cardsQueue.size() - 1; i++) {
            System.out.println("aaa" + i);
            Server.cardsStack.add(Server.cardsQueue.poll());
        }
        Collections.shuffle(Server.cardsStack);
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

    private void checkPlayersReady() {
        if (doFunctionPlayersReady && Server.players.size() >= 2 && playersReady()) {
            broadcast(playersReadyMessage);
            doFunctionPlayersReady = false;
        }
    }

    private String numberOfCardsPerPlayer() {
        String responseStart = "START/";

        for (Player playerAux : Server.players) {
            if (!playerAux.getUsername().equals(this.name)) {
                responseStart += playerAux.getUsername() + ";";
                responseStart += playerAux.getCards().size() + "/";
            }
        }
        return responseStart;
    }

    private void putPlayersOnHold() {
        if (playersReady()) {
            synchronized (turnLock) {
                for (int i = 0; i < Server.players.size(); i++) {
                    if (i != currentPlayerIndex) {
                        Flow playerFlow = Server.players.get(i).getFlow();
                        playerFlow.sendMenssageToClient(responseWAIT,
                                "Error al poner en espera");
                    } else {
                        Flow currentFlow
                                = Server.players.get(currentPlayerIndex).getFlow();
                        currentFlow.sendMenssageToClient(responseTURN,
                                "Error al notificar turno");
                    }
                }
            }
        }
    }

    private synchronized void putCardInQueue(String request) {
        String responsePUT = "PUT/";
        String[] cards = request.split("/");

        int index = 1;
        while (index < cards.length) {
            Server.cardsQueue.add(createObjectCard(cards[index]));
            // handlePlayerTurns(createObjectCard(cards[index]));
            index++;
        }
        responsePUT += createObjectCard(cards[index - 1]).toString();

        broadcast(responsePUT);
        changeTurn(createObjectCard(cards[index - 1]));
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

    private void removeCardOfPlayer(Card topCard) {
        for (int i = 0; i < Server.players.get(currentPlayerIndex).getCards().size(); i++) {
            if (Server.players.get(currentPlayerIndex).getCards()
                    .get(i).toString().contains(topCard.toString())) {
                Server.players.get(currentPlayerIndex).getCards().remove(i);
                break;
            }
        }
    }

    private synchronized void changeTurn(Card topCard) {

        synchronized (turnLock) {
            Server.players.get(currentPlayerIndex).getFlow()
                    .sendMenssageToClient(responseWAIT,
                            "Error al poner en espera");;

            removeCardOfPlayer(topCard);

            handlePlayerTurns(topCard);

            Flow nextPlayerFlow = Server.players.get(currentPlayerIndex).getFlow();

            nextPlayerFlow.sendMenssageToClient(responseTURN,
                    "Error al notificar turno");

            turnLock.notifyAll();
        }

        skillsCards(topCard, currentPlayerIndex);

        sendUpdatedCards();

        broadcast(responseACTUAL
                + Server.players.get(currentPlayerIndex).getUsername() + "/" + Server.players.get(currentPlayerIndex).getCards().size() + "/");
    }

    private void sendUpdatedCards() {
        String responseInitialCards = "CARDS/";

        Flow nextPlayerFlow = Server.players.get(currentPlayerIndex).getFlow();

        for (Card card : Server.players.get(currentPlayerIndex).getCards()) {
            responseInitialCards += card.toString() + "/";
        }

        nextPlayerFlow.sendMenssageToClient(responseInitialCards,
                "No se pudo enviar las cartas inciales, error: ");

        //Quitar si no sirve
        nextPlayerFlow.sendMenssageToClient(numberOfCardsPerPlayer(),
                "Error al envia mensaje: ");
    }

    private void skillsCards(Card card, int playerIndex) {

        if (card.toString().equals("C1")) {
            eatCardFromStack(playerIndex, 4);
        }
        if (Integer.parseInt(card.getValue()) == 10) {
            eatCardFromStack(playerIndex, 2);
        }
    }

    private void eatCardFromStack(int playerIndex, int amount) {
        Card card;
        for (int i = 0; i < amount; i++) {
            card = Server.cardsStack.pop();
            Server.players.get(playerIndex).getCards().add(card);
        }
    }

//    public void skillsCards(String card, int index) {
//
//        Card _card;
//        System.out.println("CARD_FLOW: " + card);
//        switch (card) {
//            case "C1":
//                System.out.println("Is +4");
//                for (int i = 0; i < 4; i++) {
//                    _card = Server.cardsStack.pop();
//                    Server.players.get(index).getCards().add(_card);
////                    sendMenssageToClient("NEWCARDS/" + Server.players.get(index).getUsername() + "/" + _card.toString() + "/",
////                            "No se pudo enviar el refresh");
//                }
//                break;
//            case "B10":
//                System.out.println("Is +2 B");
//                for (int i = 0; i < 2; i++) {
//                    _card = Server.cardsStack.pop();
//                    Server.players.get(index).getCards().add(_card);
////                    sendMenssageToClient("NEWCARDS/" + Server.players.get(index).getUsername() + "/" + _card.toString() + "/",
////                            "No se pudo enviar el refresh");
//                }
//                break;
//            case "G10":
//                System.out.println("Is +2G");
//                for (int i = 0; i < 2; i++) {
//                    _card = Server.cardsStack.pop();
//                    Server.players.get(index).getCards().add(_card);
////                    sendMenssageToClient("NEWCARDS/" + Server.players.get(index).getUsername() + "/" + _card.toString() + "/",
////                            "No se pudo enviar el refresh");
//                }
//                break;
//            case "R10":
//                System.out.println("Is +2R");
//                for (int i = 0; i < 2; i++) {
//                    _card = Server.cardsStack.pop();
//                    Server.players.get(index).getCards().add(_card);
////                    sendMenssageToClient("NEWCARDS/" + Server.players.get(index).getUsername() + "/" + _card.toString() + "/",
////                            "No se pudo enviar el refresh");
//                }
//                break;
//            case "Y10":
//                System.out.println("Is +2Y");
//                for (int i = 0; i < 2; i++) {
//                    _card = Server.cardsStack.pop();
//                    Server.players.get(index).getCards().add(_card);
////                    sendMenssageToClient("NEWCARDS/" + Server.players.get(index).getUsername() + "/" + _card.toString() + "/",
////                            "No se pudo enviar el refresh");
//                }
//                break;
//            default:
//        }
//        System.out.println("CARTAS: " + Server.players.get(index).getCards().size());
//    }
    private synchronized void handlePlayerTurns(Card topCard) {

        this.skipAmount = 1;

        checkSkipCards(topCard);

        checkInvertOrderOfPlayers(topCard);

        handlePosition();

        //check cancelation of tunr
        checkLimitsOfVectorPlayers();// falta con la de skip

    }

    private void checkSkipCards(Card topCard) {
        System.out.println("SkipCard: " + topCard.toString());
        for (String skipCard : skipCards) {
            if (skipCard.equals(topCard.toString())) {
                System.out.println("vamos a saltar un player" + this.skipAmount);
                this.skipAmount = this.skipAmount + 1;
            }
        }
    }

    private void checkInvertOrderOfPlayers(Card topCard) {
        for (String invertCard : invertCards) {
            if (invertCard.equals(topCard.toString())) {
                invertOrder = !invertOrder;
            }
        }
    }

    private void handlePosition() {
        System.out.println("al mover skipAmount tinee:" + this.skipAmount);
        if (invertOrder) {
            currentPlayerIndex -= this.skipAmount;
        } else {
            currentPlayerIndex += this.skipAmount;
        }

        // [0,1,2] 
    }

    private void checkLimitsOfVectorPlayers() {
        if (currentPlayerIndex > Server.players.size() - 1) {
            currentPlayerIndex = this.skipAmount - 1;
        } else if (currentPlayerIndex < 0) {
            currentPlayerIndex = Server.players.size() - this.skipAmount;
        }
    }

    private static boolean playersReady() {
        for (Player playerAux : Server.players) {
            if (playerAux.isReady() == false) {
                return false;
            }
        }
        return true;
    }

    private void returnCardToTheStack() {
        for (Card card : this.player.getCards()) {
            Server.cardsStack.add(card);
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
