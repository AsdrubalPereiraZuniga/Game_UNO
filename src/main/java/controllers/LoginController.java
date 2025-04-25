/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import client.Client;
import com.mycompany.game_uno_so.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;

/**
 * Login controller class.
 * 
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 */
public class LoginController implements Initializable {

    private int port;
    private Client client;
    private String host;    
    @FXML
    private Button btnStart;
    @FXML
    private TextField playerName;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.host = "localhost";
        this.port = 8000;
    }
    
    /**
     * Handle the event of the button.
     * 
     * @param event event.
     */
    @FXML
    private void start(ActionEvent event) {
        if (!this.playerName.getText().isEmpty()) {
            String name = this.playerName.getText();
            this.client = new Client(name, this.host, this.port);
            changeScreen();
        }
    }

    /**
     * Change the screen.
     */
    private void changeScreen() {
        if (client.isConnect()) {
            WaitingController.setClient(this.client);
            MainController.getInstanceController().setClient(client);            
            App.setRoot("WaitingScreen");
        }
    }

}
