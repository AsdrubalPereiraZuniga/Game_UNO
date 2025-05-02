/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import client.Client;
import client.SayOneHandler;
import com.mycompany.game_uno_so.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import server.Server;

/**
 * FXML Controller class
 *
 * @author Asdrubal
 */
public class WinnerController implements Initializable {
    
    private static Client client;
    private static String winnerName;
    public static WinnerController instanceWinner;
    
    @FXML
    private Button btnExit;
    @FXML
    private Button btnRestar;
    @FXML
    private Label lblWinnerTitle;
    @FXML
    private Label lblWinner;
    @FXML
    private AnchorPane mainPane;
    @FXML
    private ImageView imgView;
    @FXML
    private Label lblPlayerName;        

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        lblWinner.setText(winnerName);
        SayOneHandler.setImageView(imgView);        
        SayOneHandler.imageAnimation(Duration.INDEFINITE);       
        lblPlayerName.setText(client.getPlayerName());
        backAnimation();
        startBouncingIcons();                
        
    }          
    
    /**
     * method for set a winner name of game
     * @param name winner name of game
     */
    public static void setWinnerName(String name){
        winnerName = name;
    }
    
    /**
     * method for set client of Winner Controller
     * 
     * @param _client
     */
    public static void setClient(Client _client){
        client = _client;
    }
    
    /**
     * method to configure an instance of the winner controller
     * 
     * @param _instance winner controller instance
     */
    public static void setInstanceWinner(WinnerController _instance){
        instanceWinner = _instance;
    }
    
    /**
     * method responsible for displaying the animation in the Winner view
     */
    public void showAnimation(){
        Platform.runLater(() ->{
            SayOneHandler.showSayOne(winnerName);
        });
    }   
    
    /**
     * Creates and starts the bouncing icons animation in the background.
     */
    private void startBouncingIcons() {
        int rows = 5;
        int cols = 7;
        double spacing = 85;
        double iconSize = 40;

        URL iconUrl = getClass().getResource("/images/more/imgCardUno.png");

        Image icon = new Image(iconUrl.toExternalForm(), iconSize, iconSize, true, true);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                ImageView iconView = new ImageView(icon);
                iconView.setOpacity(0.18);
                iconView.setLayoutX(col * spacing + 20);
                iconView.setLayoutY(row * spacing + 20);
                iconView.setManaged(false);
                iconView.setMouseTransparent(true);

                mainPane.getChildren().add(0, iconView);

                animateBounce(iconView);
            }
        }
    }

    /**
     * Applies bouncing animation to a specific icon.
     *
     * @param icon the ImageView representing the icon to animate
     */
    private void animateBounce(ImageView icon) {
        TranslateTransition jump1 = new TranslateTransition(Duration.seconds(0.25), icon);
        jump1.setByY(-10);
        ScaleTransition stretch1 = new ScaleTransition(Duration.seconds(0.25), icon);
        stretch1.setToY(1.25);
        stretch1.setToX(0.85);

        TranslateTransition down1 = new TranslateTransition(Duration.seconds(0.25), icon);
        down1.setByY(10);
        ScaleTransition reset1 = new ScaleTransition(Duration.seconds(0.25), icon);
        reset1.setToY(1.0);
        reset1.setToX(1.0);

        TranslateTransition jump2 = new TranslateTransition(Duration.seconds(0.25), icon);
        jump2.setByY(-10);
        ScaleTransition stretch2 = new ScaleTransition(Duration.seconds(0.25), icon);
        stretch2.setToY(1.25);
        stretch2.setToX(0.85);

        TranslateTransition down2 = new TranslateTransition(Duration.seconds(0.25), icon);
        down2.setByY(10);
        ScaleTransition reset2 = new ScaleTransition(Duration.seconds(0.25), icon);
        reset2.setToY(1.0);
        reset2.setToX(1.0);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        SequentialTransition bounce = new SequentialTransition(
            new ParallelTransition(jump1, stretch1),
            new ParallelTransition(down1, reset1),
            new ParallelTransition(jump2, stretch2),
            new ParallelTransition(down2, reset2),
            pause
        );

        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.play();
    }
    
    /**
     * Creates a background pulsing animation for the login screen.
     */
    private void backAnimation(){
        mainPane.setStyle("-fx-background-color: #e63946;");

        Circle pulse = new Circle(300);
        pulse.setFill(Color.web("#f06065", 0.5));
        pulse.setEffect(new GaussianBlur(200));

        pulse.setManaged(false);
        pulse.setLayoutX(mainPane.getPrefWidth() - 30);
        pulse.setLayoutY(mainPane.getPrefHeight() + 120);

        mainPane.getChildren().add(0, pulse);

        ScaleTransition pulseAnim = new ScaleTransition(Duration.seconds(2.5), pulse);
        pulseAnim.setFromX(1.0);
        pulseAnim.setFromY(1.0);
        pulseAnim.setToX(1.7);
        pulseAnim.setToY(1.7);
        pulseAnim.setAutoReverse(true);
        pulseAnim.setCycleCount(ScaleTransition.INDEFINITE);
        pulseAnim.play();
    }
    
    public void changeScreen(){
        if(client.isConnect()){
            WaitingController.setClient(this.client);
            MainController.getInstanceController().setClient(client);            
            App.setRoot("WaitingScreen");
        }
    }
    
    /**
     * Method for handling the exit game event
     */
    @FXML
    private void exitGame(ActionEvent event) {
        try {
            
            if(SayOneHandler.getImageView() == null){
                System.out.println("SayOne image NULL");            
            }             
            else{
                
                WinnerController.instanceWinner.client.sendMessage("DISCONNECT/");
                Stage stage = (Stage) SayOneHandler.getImageView().getScene().getWindow();
                stage.close();
            }             
            
        } catch (Exception e) {
            System.out.println("Error/WinnerController/exitGame: " + e.getMessage());
        }
    }
    
    /**
     * Method for handling the game restart event
     */
    @FXML
    private synchronized void restarGame(ActionEvent event) {
        
        try {
                                                                      
            System.out.println("Cliente de reinicio: " + WinnerController.instanceWinner.client.getPlayerName());
            WinnerController.instanceWinner.client.sendMessage("RESTART/");
            App.setRoot("LoginScreen");
            
        } catch (Exception e) {
            System.out.println("ERROR/WinnerController/restartGame: " + e.getMessage());
        }
    }
    
    
}
