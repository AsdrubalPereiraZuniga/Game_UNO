/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import client.Client;
import com.mycompany.game_uno_so.App;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

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
    @FXML
    private AnchorPane bgLogin;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.host = "localhost";
        this.port = 8000;
        
        backAnimation();
        startBouncingIcons();
    }
   
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

                bgLogin.getChildren().add(0, iconView);

                animateBounce(iconView);
            }
        }
    }

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
    
    private void backAnimation(){
        bgLogin.setStyle("-fx-background-color: #e63946;");

        Circle pulse = new Circle(300);
        pulse.setFill(Color.web("#f06065", 0.5));
        pulse.setEffect(new GaussianBlur(200));

        pulse.setManaged(false);
        pulse.setLayoutX(bgLogin.getPrefWidth() - 30);
        pulse.setLayoutY(bgLogin.getPrefHeight() + 120);

        bgLogin.getChildren().add(0, pulse);

        ScaleTransition pulseAnim = new ScaleTransition(Duration.seconds(2.5), pulse);
        pulseAnim.setFromX(1.0);
        pulseAnim.setFromY(1.0);
        pulseAnim.setToX(1.7);
        pulseAnim.setToY(1.7);
        pulseAnim.setAutoReverse(true);
        pulseAnim.setCycleCount(ScaleTransition.INDEFINITE);
        pulseAnim.play();
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
