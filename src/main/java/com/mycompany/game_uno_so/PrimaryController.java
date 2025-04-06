/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.game_uno_so;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
/**
 * FXML Controller class
 *
 * @author igmml
 */
public class PrimaryController implements Initializable {


    @FXML
    private AnchorPane bgView;
    @FXML
    private AnchorPane UsedCardsView;
    @FXML
    private AnchorPane DeckView;
    @FXML
    private AnchorPane Player0;
    @FXML
    private AnchorPane Player2;
    @FXML
    private AnchorPane Player1;
    @FXML
    private AnchorPane Player3;
    @FXML
    private Button oneBtn;
    @FXML
    private Button confirmBtn;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    @FXML
    private void callOne(ActionEvent event) {
    }

    @FXML
    private void confirm(ActionEvent event) {
    }

}
