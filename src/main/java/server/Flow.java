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

/**
 *
 * @author jorge
 */
public class Flow implements Runnable {

    Socket socket;
    DataInputStream readFlow;
    DataOutputStream writeFlow;
    String name;
    ArrayList<Card> playerCards = new ArrayList<>();
    Stack<Card> cardsStack = new Stack<>();

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

        Server.players.add(new Player(this, this.name, distributeCards()));

    }

    public ArrayList<Card> distributeCards() {

        this.playerCards.clear();

        if (cardsStack.size() >= 7 && !cardsStack.isEmpty()) {
            for (int i = 0; i < 7; i++) {
                Card card = cardsStack.pop();
                this.playerCards.add(card);
            }
        }

        return this.playerCards;
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
