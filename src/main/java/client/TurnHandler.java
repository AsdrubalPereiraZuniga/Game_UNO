package client;

import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Handles updating the label that displays the current player's turn.
 */
public class TurnHandler {

    private static Label lblCurrentTurn;

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
                lblCurrentTurn.setText("Turno actual: " + username);
            });
        }
    }
}
