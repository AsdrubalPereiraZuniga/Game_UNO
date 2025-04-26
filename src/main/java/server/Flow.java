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
 * Class that manages the flow of communication with a single client/player.
 * Handles message processing, card distribution, turn management, and special card effects.
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

    /**
     * Constructs a Flow instance for a specific player.
     *
     * @param socket the client's socket connection
     * @param name the name of the player
     */
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

    /**
     * Main method executed by the thread to initialize the player and start listening for messages.
     */
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

    /**
     * Distributes the initial 7 cards to a player.
     *
     * @return the list of distributed cards
     */
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

    /**
     * Enables the Ready button for all players once the minimum number of players is connected.
     */
    private void enableReadyButtom() {
        if (Server.players.size() >= 2) {
            broadcast(responseActiveButtom);
        }
    }

    /**
     * Sends the player's initial hand to the client.
     */
    private void sendInitialCards() {
        String responseInitialCards = "CARDS/";

        for (Card card : this.player.getCards()) {
            responseInitialCards += card.toString() + "/";
        }
        sendMenssageToClient(responseInitialCards,
                "No se pudo enviar las cartas inciales, error: ");
    }

    /**
     * Starts listening for messages from the player.
     */
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

    /**
     * Processes a received message from the client.
     *
     * @param request the received message
     */
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

    /**
    * Gives a card from the deck to the current player.
    * If the stack is empty, restocks it from the queue.
    */
    private void giveCardToPlayer() {
        if (!Server.cardsStack.isEmpty()) {
            Card drawnCard = Server.cardsStack.pop();
            Server.players.get(currentPlayerIndex).getCards().add(drawnCard);
            sendMenssageToClient("CARDS/" + getPlayerCards() + "/", "No se pudo enviar carta robada");
        } else {
            restockStack();
        }
    }

    /**
    * Returns a string representation of the current player's cards.
    *
    * @return a formatted string listing all cards of the player
    */
    private String getPlayerCards() {
        String playerCards = "";
        for (Card card : Server.players.get(currentPlayerIndex).getCards()) {
            playerCards += card.toString() + "/";
        }
        return playerCards;
    }

    /**
    * Refills the card stack from the cards queue, shuffling the cards.
    */
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

    /**
     * Checks if all players are ready to start the game.
     */
    private void checkPlayersReady() {
        if (doFunctionPlayersReady && Server.players.size() >= 2 && playersReady()) {
            broadcast(playersReadyMessage);
            doFunctionPlayersReady = false;
        }
    }

    /**
     * Prepares a message listing the number of cards each other player has.
     *
     * @return the formatted message
     */
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

    /**
     * Puts all players on hold except the one whose turn it is.
     */
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

    /**
     * Adds the card(s) played to the queue and processes the turn change.
     *
     * @param request the request containing the card(s) played
     */
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

    /**
     * Creates a Card object from a string representation.
     *
     * @param card the string representation
     * @return the created Card object
     */
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

    /**
    * Removes the played card from the current player's hand.
    *
    * @param topCard the card that was played
    */
    private void removeCardOfPlayer(Card topCard) {
        for (int i = 0; i < Server.players.get(currentPlayerIndex).getCards().size(); i++) {
            if (Server.players.get(currentPlayerIndex).getCards()
                    .get(i).toString().contains(topCard.toString())) {
                Server.players.get(currentPlayerIndex).getCards().remove(i);
                break;
            }
        }
    }

    /**
    * Updates the turn to the next player based on the last played card,
    * handling skips and reverses if necessary.
    *
    * @param topCard the last card played
    */
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

    /**
    * Sends the updated cards of the current player to the client.
    */
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

    /**
    * Applies special effects (+2, +4) based on the played card.
    *
    * @param card the card played
    * @param playerIndex the index of the affected player
    */
    private void skillsCards(Card card, int playerIndex) {

        if (card.toString().equals("C1")) {
            eatCardFromStack(playerIndex, 4);
        }
        if (Integer.parseInt(card.getValue()) == 10) {
            eatCardFromStack(playerIndex, 2);
        }
    }

    /**
    * Forces a player to draw a certain number of cards from the deck.
    *
    * @param playerIndex the index of the player
    * @param amount the number of cards to draw
    */
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
    
    /**
    * Handles the turn change logic, considering skip and invert cards.
    *
    * @param topCard the last played card
    */
    private synchronized void handlePlayerTurns(Card topCard) {

        this.skipAmount = 1;

        checkSkipCards(topCard);

        checkInvertOrderOfPlayers(topCard);

        handlePosition();

        //check cancelation of tunr
        checkLimitsOfVectorPlayers();// falta con la de skip

    }

    /**
    * Checks if the last played card is a skip card and adjusts skipAmount accordingly.
    *
    * @param topCard the last played card
    */
    private void checkSkipCards(Card topCard) {
        System.out.println("SkipCard: " + topCard.toString());
        for (String skipCard : skipCards) {
            if (skipCard.equals(topCard.toString())) {
                System.out.println("vamos a saltar un player" + this.skipAmount);
                this.skipAmount = this.skipAmount + 1;
            }
        }
    }

    /**
    * Adjusts the order of player turns if an invert card was played.
    *
    * @param topCard the last played card
    */
    private void checkInvertOrderOfPlayers(Card topCard) {
        for (String invertCard : invertCards) {
            if (invertCard.equals(topCard.toString())) {
                invertOrder = !invertOrder;
            }
        }
    }

    /**
    * Moves the currentPlayerIndex according to skipAmount and invert order.
    */
    private void handlePosition() {
        System.out.println("al mover skipAmount tinee:" + this.skipAmount);
        if (invertOrder) {
            currentPlayerIndex -= this.skipAmount;
        } else {
            currentPlayerIndex += this.skipAmount;
        }

        // [0,1,2] 
    }

    /**
    * Ensures that currentPlayerIndex stays within valid range after movement.
    */
    private void checkLimitsOfVectorPlayers() {
        if (currentPlayerIndex > Server.players.size() - 1) {
            currentPlayerIndex = this.skipAmount - 1;
        } else if (currentPlayerIndex < 0) {
            currentPlayerIndex = Server.players.size() - this.skipAmount;
        }
    }

    /**
     * Checks if all players are ready to start the game.
     *
     * @return true if all players are ready, false otherwise
     */
    private static boolean playersReady() {
        for (Player playerAux : Server.players) {
            if (playerAux.isReady() == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the player's cards to the deck if they disconnect.
     */
    private void returnCardToTheStack() {
        for (Card card : this.player.getCards()) {
            Server.cardsStack.add(card);
        }
    }

    /**
     * Sends a message to the current client.
     *
     * @param message the message to send
     * @param error the error message to display if sending fails
     */
    public void sendMenssageToClient(String message, String error) {
        try {
            this.writeFlow.writeUTF(message);
            this.writeFlow.flush();
        } catch (IOException ex) {
            System.out.println(error + ex);
        }
    }

    /**
     * Sends a message to all connected clients (broadcast).
     *
     * @param message the message to broadcast
     */
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
