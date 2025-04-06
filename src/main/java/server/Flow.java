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
    //Mazo de cartas para cada jugador

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

        Server.players.add(new Player(this, this.name));

    }

    public void distributeCards() {
        
       Stack<Card> stack = new Stack<>();
        
        

        
        
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
