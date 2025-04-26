package client;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
/**
 * Handles the visualization and updating of the player's cards on the screen.
 */
public class ViewCardsHandler {
    
    private static GridPane gridPlayer;
    private static AnchorPane anchorPane;
    /**
     * Sets the GridPane that contains the player's cards.
     *
     * @param _grid the GridPane to be set
     */
    public static void setGridPlayerCards(GridPane _grid) {
        gridPlayer = _grid;
    }
    /**
     * Sets the AnchorPane that displays the used card.
     *
     * @param _anchor the AnchorPane to be set
     */
    public static void setAnchorPane(AnchorPane _anchor) {
        anchorPane = _anchor;
    }
    /**
     * Updates the used card view by displaying the specified VBox container.
     *
     * @param container the VBox representing the card to display
     */
    public static void updateUsedViewCard(VBox container) {
        if (container != null) {
            Platform.runLater(() -> {
                anchorPane.getChildren().clear();
                container.setStyle("-fx-background-color: transparent;");
                
                container.setAlignment(Pos.CENTER);
                
                double containerWidth = container.getPrefWidth();
                double containerHeight = container.getPrefHeight();
                double anchorWidth = anchorPane.getPrefWidth();
                double anchorHeight = anchorPane.getPrefHeight();
                
                AnchorPane.setTopAnchor(container, (anchorHeight - containerHeight) / 2);
                AnchorPane.setLeftAnchor(container, (anchorWidth - containerWidth) / 2);
                AnchorPane.setRightAnchor(container, (anchorWidth - containerWidth) / 2);
                AnchorPane.setBottomAnchor(container, (anchorHeight - containerHeight) / 2);
                
                anchorPane.getChildren().add(container);
                gridPlayer.getChildren().clear();
            });
        }
    }
}