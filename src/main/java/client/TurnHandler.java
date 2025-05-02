package client;

import java.net.URL;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Handles updating the label that displays the current player's turn.
 */
public class TurnHandler {

    private static Label lblCurrentTurn;  
    
    private static MediaPlayer mediaPlayer;

    /**
     * Sets the label used to display the current turn.
     *
     * @param label the Label that will show the current turn
     */
    public static void setLabel(Label label) {
        lblCurrentTurn = label;
    }    
    

    /**
     * Updates the turn label with the given username.
     *
     * @param username the name of the player whose turn it is
     */
    public static void updateTurn(String username) {
        if (lblCurrentTurn != null) {
            Platform.runLater(() -> {
             try {
                    // Ruta al archivo de sonido (debe estar en tu directorio de recursos)
                    URL soundFile = TurnHandler.class.getResource("/notification.mp3");
                    
                    if (soundFile != null) {
                        Media media = new Media(soundFile.toString());
                        mediaPlayer = new MediaPlayer(media);
                        //mediaPlayer.play();
                    } else {
                        System.err.println("No se pudo encontrar el archivo de sonido");
                    }
                } catch (Exception e) {
                    System.err.println("Error al reproducir el sonido: " + e.getMessage());
                }
                lblCurrentTurn.setText("Turno: " + username);
            });
        }
    }
    
    /**
     *  method responsible for releasing resources of this class
     */ 
    public static void clear(){
        lblCurrentTurn.setText("");
        mediaPlayer = null;
    }
}
