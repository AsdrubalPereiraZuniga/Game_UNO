package client;

import java.net.URL;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class TurnHandler {

    private static Label lblCurrentTurn;  
    
    private static MediaPlayer mediaPlayer;

    public static void setLabel(Label label) {
        lblCurrentTurn = label;
    }    
    

    public static void updateTurn(String username) {
        if (lblCurrentTurn != null) {
            Platform.runLater(() -> {
             try {
                    // Ruta al archivo de sonido (debe estar en tu directorio de recursos)
                    URL soundFile = TurnHandler.class.getResource("/notification.mp3");
                    
                    if (soundFile != null) {
                        Media media = new Media(soundFile.toString());
                        mediaPlayer = new MediaPlayer(media);
                        mediaPlayer.play();
                    } else {
                        System.err.println("No se pudo encontrar el archivo de sonido");
                    }
                } catch (Exception e) {
                    System.err.println("Error al reproducir el sonido: " + e.getMessage());
                }
                lblCurrentTurn.setText("Turno actual: " + username);
            });
        }
    }        
}
