package controllers;

import client.Client;
import com.mycompany.game_uno_so.App;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 * 
 * Waiting controller class, handle the waiting interface.
 * Controller class for the waiting screen.
 * Manages player readiness, animations, and transitions to the main game screen.
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
    @FXML
    private AnchorPane bgWait;

    /**
     * Initialize.
     *
     * @param url url.
     * @param rb Resource Bundle.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        backAnimation();
        startBouncingIcons();
    }
    
    /**
     * Starts the background bouncing icons animation.
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

                bgWait.getChildren().add(0, iconView);

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
     * Creates a pulsing background animation for the waiting screen.
     */
    private void backAnimation(){
        bgWait.setStyle("-fx-background-color: #e63946;");

        Circle pulse = new Circle(300);
        pulse.setFill(Color.web("#f06065", 0.5));
        pulse.setEffect(new GaussianBlur(200));

        pulse.setManaged(false);
        pulse.setLayoutX(bgWait.getPrefWidth() - 30);
        pulse.setLayoutY(bgWait.getPrefHeight() + 120);

        bgWait.getChildren().add(0, pulse);

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
     * Handles the transition depending on whether the game is ready to start
     * or the player was forbidden to join.
     */
    private void handleStatusOfTheMessage() {
        Platform.runLater(() -> {
            if (client.isForbidden()) {
                if (animationTimeline != null) {
                    animationTimeline.stop();
                }

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
     * Stops the player ready checking thread.
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
