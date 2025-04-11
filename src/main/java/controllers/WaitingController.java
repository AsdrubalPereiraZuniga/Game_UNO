package controllers;

import client.Client;
import com.mycompany.game_uno_so.App;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;


/**
 * Login controller class.
 * 
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 */
public class WaitingController implements Initializable {

    private static Client client;
    @FXML
    private Button btnReady;
    @FXML
    private Label lblWatingForPlayers;
    private Timeline animationTimeline;

    /**
     * Initialize.
     * @param url url.
     * @param rb Resource Bundle.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicialización si es necesaria
    }

    /**
     * Set the client.
     * 
     * @param client client.
     */
    public static void setClient(Client client) {
        WaitingController.client = client;
    }

    /**
     * Handle the evento of ready.
     * 
     * @param event event.
     */
    @FXML
    private void setReady(ActionEvent event) {
        if (client == null) {
            System.err.println("Player client is not set!");
            return;
        }

        this.btnReady.setDisable(true);
        WaitingController.client.sendMessage("READY/");
        startWaitingAnimation();
        startPlayerReadyCheck();
    }

    /**
     * Start the animation of the points.
     */
    private void startWaitingAnimation() {
        String originalText = lblWatingForPlayers.getText();
        
        animationTimeline = new Timeline(
            new KeyFrame(Duration.seconds(0.5), 
                    e -> lblWatingForPlayers.setText(originalText + " .")),
            new KeyFrame(Duration.seconds(1.0), 
                    e -> lblWatingForPlayers.setText(originalText + " ..")),
            new KeyFrame(Duration.seconds(1.5), 
                    e -> lblWatingForPlayers.setText(originalText + " ...")),
            new KeyFrame(Duration.seconds(2.0), 
                    e -> lblWatingForPlayers.setText(originalText))
        );
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        animationTimeline.play();
    }

    /**
     * Start verifying if all the players are ready.
     */
    private void startPlayerReadyCheck() {
        new Thread(() -> {
            try {
                while (!client.isReady() && 
                        !Thread.currentThread().isInterrupted()) {
                    TimeUnit.SECONDS.sleep(1);
                }
                
                Platform.runLater(() -> {
                    if (animationTimeline != null) {
                        animationTimeline.stop();
                    }
                   App.setRoot("MainScreen");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}