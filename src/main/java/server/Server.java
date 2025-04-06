/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;
import players.Player;
import java.io.*;
import java.net.*;


/**
 *
 * @author jorge
 */
public class Server {

    public static Vector<Player> players = new Vector();

    public static void main(String args[]) {

        ServerSocket serverSocket = initServer();

        if (serverSocket != null) {
            initConnection(serverSocket);
        }

    }

    public static ServerSocket initServer() {

        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("The server has stared...");
            System.out.println("Waiting for players");
            return serverSocket;
        } catch (IOException ioe) {
            System.out.println("Connection refused." + ioe);
            System.exit(1);
        }
        return null;
    }

    public static void initConnection(ServerSocket serverSocket) {

        while (true) {

            try {
                Socket socket = serverSocket.accept();
                DataInputStream readFlow = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                String name = readFlow.readUTF();
                System.out.println("Connection accepted " + name);
                
                Thread flow = new Thread(new Flow(socket, name));
                
                flow.start();
               

            } catch (Exception e) {
                System.out.println("Error: " + e);
            }

        }

    }
    
    

}
