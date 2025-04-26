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
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 * 
 * Waiting controller class, handle the waiting interface.
 */
public class WaitingController implements Initializable {

    private volatile boolean stopChecking = false;
    private static Client client;
    private Thread readyCheckThread;
    private Timeline animationTimeline;
    @FXML
    private Button btnReady;
    @FXML
    private Label lblWatingForPlayers;

    /**
     * Initialize.
     *
     * @param url url.
     * @param rb Resource Bundle.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
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
     * Handle the event of ready.
     *
     * @param event event.
     */
    @FXML
    private void setReady(ActionEvent event) {
        if (client == null) {
            System.err.println("Player client is not set!");
            return;
        }
        if (client.isActiveButton()) {
            this.btnReady.setDisable(true);
            String message = "READY/" + WaitingController.client.getPlayerName() + "/";
            WaitingController.client.sendMessage(message);
            startWaitingAnimation();
            startPlayerReadyCheck();
        }
    }

    /**
     * Start verifying if all the players are ready.
     */
    private void startPlayerReadyCheck() {
        stopChecking = false;
        readyCheckThread = new Thread(() -> {
            try {
                while (!stopChecking && !client.isReady() && !client.isForbidden()) {
                    TimeUnit.SECONDS.sleep(1);
                }
                handleStatusOfTheMessage();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        readyCheckThread.setDaemon(true);
        readyCheckThread.start();
    }

    /**
     * Handle the status of the message.
     */
    private void handleStatusOfTheMessage() {
        Platform.runLater(() -> {
            if (client.isForbidden()) {
                if (animationTimeline != null) {
                    animationTimeline.stop();
                }
                lblWatingForPlayers.setText("El juego ya inició");

                new Timeline(new KeyFrame(
                        Duration.seconds(5),
                        e -> App.setRoot("LoginScreen")
                )).play();
            } else {
                if (animationTimeline != null) {
                    animationTimeline.stop();
                }
                App.setRoot("MainScreen");
            }
        });
    }
    
    /**
     * Stop the verification.
     */
    public void stopPlayerReadyCheck() {
        stopChecking = true;
        if (readyCheckThread != null) {
            readyCheckThread.interrupt();
        }
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
}
