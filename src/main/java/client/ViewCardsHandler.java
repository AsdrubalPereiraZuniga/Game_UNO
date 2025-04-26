/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Asdrubal
 */
public class ViewCardsHandler {
    
    private static GridPane gridPlayer;
    private static AnchorPane anchorPane;
    
    public static void setGridPlayerCards(GridPane _grid){
        gridPlayer = _grid;
    }
    
    public static void setAnchorPane (AnchorPane _anchor){
        anchorPane = _anchor;
    }
    
    public static void updateUsedViewCard(VBox container){
        if(container != null){
            Platform.runLater(() ->{
                anchorPane.getChildren().clear();
                container.setStyle("-fx-background-color: transparent;");
                anchorPane.getChildren().add(container);  
                gridPlayer.getChildren().clear();
            });
        }
    }    
}
