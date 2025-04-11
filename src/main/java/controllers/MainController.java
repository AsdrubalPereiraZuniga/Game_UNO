package controllers;

import client.Client;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

/**
 * Main screen controller class.
 * 
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 */
public class MainController implements Initializable {
    private static Client client;
    @FXML
    private AnchorPane bgView;
    @FXML
    private AnchorPane deckView;
    @FXML
    private AnchorPane player;
    @FXML
    private AnchorPane players;
    @FXML
    private AnchorPane usedCardsView;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnOne;
    
    /**
     * Initialize the controller class.
     * 
     * @param url URL.
     * @param rb Resource Bundle.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }   
    
    /**
     * Set the client.
     * 
     * @param client client.
     */
    public static void setClient(Client client){
        MainController.client = client;
    }
    
    /**
     * Call one.
     * 
     * @param event event.
     */
    @FXML
    private void callOne(ActionEvent event) {
    }

    /**
     * Confirm.
     * 
     * @param event event.
     */
    @FXML
    private void confirm(ActionEvent event) {
    }

}
