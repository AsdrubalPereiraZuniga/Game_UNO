package client;

import cards.Card;
import java.util.ArrayList;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 *
 * Handle the cards and give them some animations.
 * This class handles the display, interaction, and management of player cards
 * in the game UI, including hand cards and playable cards.
 */
public class HandleCards {
    private static final double CARD_HEIGHT = 120;
    private static final double HOVER_WIDTH = 80;

    private static final double NORMAL_WIDTH = 80;
    private static final Duration ANIMATION_DURATION = Duration.millis(200);
    private static HandleCards instance;
    private ArrayList<Card> playableCards;
    private Client client;
    private GridPane grdCards;
    private GridPane grdPlayableCards;
    private ScrollPane scrollPane;

    /**
     * Configures the default properties of the cards GridPane.
     */
    private void configureGridPane() {
        instance.grdCards.setHgap(10);
        instance.grdCards.setVgap(10);
        instance.grdCards.setAlignment(Pos.CENTER);
    }

    /**
     * Initializes the card handler with the main GridPane, client, and playable cards GridPane.
     *
     * @param grid the main hand GridPane
     * @param client the Client instance
     * @param gridPC the playable cards GridPane
     */
    public void initialize(GridPane grid, Client client, GridPane gridPC, ScrollPane scroll) {
        instance.grdCards = grid;
        instance.client = client;
        instance.grdPlayableCards = gridPC;
        instance.playableCards = new ArrayList<>();
        instance.scrollPane = scroll;
        configureGridPane();
    }

    /**
     * Sets up the player's hand of cards in the main GridPane.
     *
     * @param grid the GridPane for hand cards
     * @param client the Client instance
     * @param gridPC the playable cards GridPane
     */
    public void setCards(GridPane grid, Client client, GridPane gridPC, ScrollPane scroll) {
        initialize(grid, client, gridPC, scroll);
        instance.grdCards.getChildren().clear();
        instance.grdCards.getColumnConstraints().clear();

        int cardCount = instance.client.getCards().size();
        if (cardCount == 0) {
            return;
        }

        int numRows = (int) Math.ceil((double) cardCount / 7);
        
        instance.grdCards.setPrefHeight(numRows * (CARD_HEIGHT + 10)); 

        for (int i = 0; i < cardCount; i++) {
            int row = i / 7;
            int col = i % 7;
            
            VBox cardContainer = createCardContainer(
                    instance.client.getCards().get(i).toString(),
                    i, instance.client.getCards());
            instance.grdCards.add(cardContainer, col, row);
        }
        
        instance.scrollPane.setContent(instance.grdCards);
        instance.scrollPane.setFitToWidth(true);
    }

    /**
     * Creates a visual container (VBox) for a specific card.
     *
     * @param cardText the string representation of the card
     * @param cardIndex the index of the card in the list
     * @param cards the list of cards
     * @return a VBox representing the card
     */
    private VBox createCardContainer(String cardText, int cardIndex,
        ArrayList<Card> cards) {
        VBox cardContainer = new VBox();
        cardContainer.setPrefSize(NORMAL_WIDTH, CARD_HEIGHT);
        
        cardContainer.setStyle("-fx-background-color: transparent;");
        cardContainer.setAlignment(Pos.CENTER);

        try {
            ImageView cardImage = new ImageView(new Image(
                    cards.get(cardIndex).getImagePath()));
            cardImage.setPreserveRatio(true);
            cardImage.setFitWidth(NORMAL_WIDTH - 10);
            cardContainer.getChildren().add(cardImage);
        } catch (Exception e) {
            Label label = new Label(cardText);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
            cardContainer.getChildren().add(label);
        }

        setupHoverEffects(cardContainer);

        cardContainer.setOnMouseClicked(e -> handleCardClick(cardIndex,
                cardText));

        return cardContainer;
    }

    /**
     * Sets up hover effects (glow and zoom) for a card container.
     *
     * @param cardContainer the VBox container of the card
     */
    private void setupHoverEffects(VBox cardContainer) {
        Glow glow = new Glow();
        glow.setLevel(0.5);
        
        cardContainer.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(ANIMATION_DURATION,
                    cardContainer);
            st.setToX(1.2);
            st.setToY(1.2);
            st.play();

            cardContainer.setEffect(glow);
            //cardContainer.setStyle("-fx-background-color: transparent;");
            cardContainer.toFront();
        });

        cardContainer.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(ANIMATION_DURATION,
                    cardContainer);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            cardContainer.setEffect(null);

            //cardContainer.setStyle("-fx-background-color: transparent;");
        });
    }

    /**
     * Refreshes the cards displayed in the specified GridPane.
     *
     * @param grid the GridPane to refresh
     * @param cards the list of cards to display
     */
    private void refreshCards(GridPane grid, ArrayList<Card> cards) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        int cardCount = cards.size();
        if (cardCount == 0) {
            return;
        }
        
        if (grid == instance.grdCards) {
            int numRows = (int) Math.ceil((double) cardCount / 7);
            
            grid.setPrefHeight(numRows * (CARD_HEIGHT + 10)); 

            for (int i = 0; i < cardCount; i++) {
                int row = i / 7;
                int col = i %7;
                
                VBox cardContainer = createCardContainer(
                        cards.get(i).toString(), i, cards);
                grid.add(cardContainer, col, row);
            }
        } else {

            for (int i = 0; i < cardCount; i++) {
                ColumnConstraints cc = new ColumnConstraints();
                cc.setPrefWidth(120);
                cc.setMinWidth(80);
                cc.setMaxWidth(Region.USE_COMPUTED_SIZE);
                cc.setHgrow(Priority.NEVER);
                grid.setAlignment(Pos.CENTER);
                grid.getColumnConstraints().add(cc);
            }

            for (int i = 0; i < cardCount; i++) {
                VBox cardContainer = createCardContainer(
                        cards.get(i).toString(), i, cards);
                grid.add(cardContainer, i, 0);
            }
        }
    }


    /**
     * Moves a card from the hand to the playable cards list after clicking.
     *
     * @param cardIndex the index of the card to remove
     */
    private void removeCard(int cardIndex) {
        instance.playableCards.add(instance.client.getCards().get(cardIndex));
        instance.client.getCards().remove(cardIndex);
        refreshCards(instance.grdCards, instance.client.getCards());
        refreshCards(instance.grdPlayableCards, instance.playableCards);
    }

    /**
     * Tries to remove a card from the playable cards list.
     *
     * @param cardIndex the index of the card to check
     * @param value the expected value of the card
     * @return true if the card was removed, false otherwise
     */
    private boolean removeFromPlayableCards(int cardIndex, String value) {
        if (instance.playableCards.isEmpty()) {
            return false;
        }
        if (cardIndex >= instance.playableCards.size()) {
            return false;
        }
        String cardValue = instance.playableCards.get(cardIndex).toString();
        if (instance.playableCards.get(cardIndex) != null
                && cardValue.equals(value)) {
            instance.client.getCards().add(instance.playableCards.
                    get(cardIndex));
            instance.playableCards.remove(cardIndex);
            refreshCards(instance.grdCards, instance.client.getCards());
            refreshCards(instance.grdPlayableCards, instance.playableCards);
            return true;
        }
        return false;
    }

    /**
     * Handles the logic executed when a card is clicked.
     *
     * @param cardIndex the index of the clicked card
     * @param cardText the string representation of the clicked card
     */
    private void handleCardClick(int cardIndex, String cardText) {
        if (instance.client.isWaiting()) {
            return;
        }
        if (removeFromPlayableCards(cardIndex, cardText)) {
            return;
        }
        if (instance.playableCards.isEmpty()) {
            removeCard(cardIndex);
            return;
        }
        if (!instance.playableCards.isEmpty()
                && verifiedCanPlayWithOtherCards(cardIndex)) {
            removeCard(cardIndex);
            return;
        }
    }

    /**
     * Verifies if the selected card can be played based on the value
     * compared to the playable cards.
     *
     * @param cardIndex the index of the card to verify
     * @return true if the card can be played, false otherwise
     */
    private boolean verifiedCanPlayWithOtherCards(int cardIndex) {
        boolean canPlay = false;
        for (Card playableCard : instance.playableCards) {
            if (instance.client.getCards().get(cardIndex).getValue()
                    .equals(playableCard.getValue())) {
                canPlay = true;
            }
        }
        return canPlay;
    }

    /**
     * Returns the singleton instance of HandleCards.
     *
     * @return the HandleCards instance
     */
    public static HandleCards getInstace() {
        if (HandleCards.instance == null) {
            HandleCards.instance = new HandleCards();
            return HandleCards.instance;
        }
        return HandleCards.instance;
    }

    /**
     * Returns the list of currently playable cards.
     *
     * @return the list of playable cards
     */
    public ArrayList<Card> getPlayCards() {
        return instance.playableCards;
    }

    /**
     * Returns the normal width of a card.
     *
     * @return the normal card width
     */
    public double getNORMAL_WIDTH() {
        return NORMAL_WIDTH;
    }

    /**
     * Returns the normal height of a card.
     *
     * @return the normal card height
     */
    public double getCARD_HEIGTH() {
        return CARD_HEIGHT;
    }
    
    /**
     * Sets the client instance and refreshes the cards shown on screen.
     *
     * @param client the Client instance
     */
    public void setClient(Client client) {
        instance.client = client;

        refreshCards(instance.grdCards, instance.client.getCards());
        refreshCards(instance.grdPlayableCards, instance.playableCards);
    }
}