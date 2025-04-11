/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author igmml
 */
public class Client {
    private DataInputStream input;
    private DataOutputStream output;
    private static String character;
    private static String playerName;
    private Socket socket;
    
    private void connectToServer(String host, int port){
        try {
            //host = "192.168.127.21";
            port = 8000;
            socket = new Socket(host, port);
            output = new DataOutputStream(socket.getOutputStream());
            input = new DataInputStream(new BufferedInputStream(socket
                    .getInputStream()));

            System.out.println("Connected to server.");
            output.writeUTF(playerName + "," + character);
            output.flush();

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}
