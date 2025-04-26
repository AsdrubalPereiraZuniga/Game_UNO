/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import cards.Card;
import java.util.ArrayList;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
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

    /**
     * Configured the grid pane.
     */
    private void configureGridPane() {
        instance.grdCards.setHgap(10);
        instance.grdCards.setVgap(10);
        instance.grdCards.setAlignment(Pos.CENTER);
    }

    /**
     * Initialize the variables.
     * @param grid the graphic players deck.
     * @param client client.
     * @param gridPC played cards.
     */
    public void initialize(GridPane grid, Client client, GridPane gridPC) {
        instance.grdCards = grid;
        instance.client = client;
        instance.grdPlayableCards = gridPC;
        instance.playableCards = new ArrayList<>();
        configureGridPane();
    }

    /**
     * Set the cards in the graphic player deck.
     * 
     * @param grid the graphic player deck.
     * @param client client.
     * @param gridPC played cards.
     */
    public void setCards(GridPane grid, Client client, GridPane gridPC) {
        initialize(grid, client, gridPC);
        instance.grdCards.getChildren().clear();
        instance.grdCards.getColumnConstraints().clear();

        int cardCount = instance.client.getCards().size();
        if (cardCount == 0) {
            return;
        }

        for (int i = 0; i < cardCount; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPrefWidth(120);
            cc.setMinWidth(80);
            cc.setMaxWidth(Region.USE_COMPUTED_SIZE);
            cc.setHgrow(Priority.NEVER);

            instance.grdCards.setAlignment(Pos.CENTER);
            instance.grdCards.getColumnConstraints().add(cc);
        }

        for (int i = 0; i < cardCount; i++) {
            VBox cardContainer
                    = createCardContainer(
                            instance.client.getCards().get(i).toString(),
                            i, instance.client.getCards());
            instance.grdCards.add(cardContainer, i, 0);
        }
    }

    /**
     * Create the card container and insert the card in it.
     * 
     * @param cardText value of the card. ex, R0, Y0, C0, B0, G0.
     * @param cardIndex the card index.
     * @param cards the player deck.
     * @return 
     */
    private VBox createCardContainer(String cardText, int cardIndex,
            ArrayList<Card> cards) {
        VBox cardContainer = new VBox();
        cardContainer.setPrefSize(NORMAL_WIDTH, CARD_HEIGHT);
        cardContainer.setStyle("-fx-background-color: white; -fx-border-color: "
                + "#333; -fx-border-radius: 5;");
        cardContainer.setAlignment(Pos.CENTER);

        try {
            ImageView cardImage = new ImageView(new Image(
                    cards.get(cardIndex).getImagePath()));
            cardImage.setPreserveRatio(true);
            cardImage.setFitWidth(NORMAL_WIDTH - 10);
            cardContainer.getChildren().add(cardImage);
        } catch (Exception e) {
            Label label = new Label(cardText);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            cardContainer.getChildren().add(label);
        }

        setupHoverEffects(cardContainer);
        cardContainer.setOnMouseClicked(e -> handleCardClick(cardIndex,
                cardText));

        return cardContainer;
    }

    /**
     * Set the hover effect to the card container.
     * 
     * @param cardContainer card conteiner.
     */
    private void setupHoverEffects(VBox cardContainer) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setSpread(0.2);
        double normalSize = cardContainer.getWidth();

        cardContainer.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(ANIMATION_DURATION,
                    cardContainer);
            st.setToX(1.2);
            st.setToY(1.1);
            st.play();

            cardContainer.setEffect(shadow);
            cardContainer.setStyle("-fx-background-color: #f8f8f8; "
                    + "-fx-border-color: #ff0000; -fx-border-width: 2;");
            cardContainer.toFront();
        });

        cardContainer.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(ANIMATION_DURATION,
                    cardContainer);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();

            cardContainer.setEffect(null);
            cardContainer.setStyle("-fx-background-color: white; "
                    + "-fx-border-color: #333;");
        });
    }

    /**
     * Refresh the player cards.
     * 
     * @param grid grid.
     * @param cards player deck.
     */
    private void refreshCards(GridPane grid, ArrayList<Card> cards) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();

        int cardCount = cards.size();
        if (cardCount == 0) {
            return;
        }
        
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
            VBox cardContainer
                    = createCardContainer(
                            cards.get(i).toString(), i, cards);
            grid.add(cardContainer, i, 0);
        }
    }

    /**
     * Remove the card from the player deck and refresh the played cards view
     * and the graphic player deck.
     * 
     * @param cardIndex card index.
     */
    private void removeCard(int cardIndex) {
        instance.playableCards.add(instance.client.getCards().get(cardIndex));
        instance.client.getCards().remove(cardIndex);
        refreshCards(instance.grdCards, instance.client.getCards());
        refreshCards(instance.grdPlayableCards, instance.playableCards);
    }

    /**
     * Remove clear the playable cards.
     * 
     * @param cardIndex card index.
     * @param value value of the card. ex, R0, Y0, C0, B0, G0.
     * @return if can continue or not.
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
     * Handle the click of the card.
     * 
     * @param cardIndex card index.
     * @param cardText value of the card. ex, R0, Y0, C0, B0, G0.
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
     * Verified if a card can be played with others.
     * 
     * @param cardIndex card index.
     * @return if can continue or not.
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
     * Return the instance of the handle cards.
     * 
     * @return the instace of the handle cards.
     */
    public static HandleCards getInstace() {
        if (HandleCards.instance == null) {
            HandleCards.instance = new HandleCards();
            return HandleCards.instance;
        }
        return HandleCards.instance;
    }

    /**
     * Return the playable cards.
     * 
     * @return the playable cards.
     */
    public ArrayList<Card> getPlayCards() {
        return instance.playableCards;
    }

    /**
     * Return the constant NORMAL_WITH.
     * 
     * @return the constant NORMAL_WITH.
     */
    public double getNORMAL_WIDTH() {
        return NORMAL_WIDTH;
    }

    /**
     * Return the constant CARD_HEIGTH.
     * @return the constant CARD_HEIGTH.
     */
    public double getCARD_HEIGTH() {
        return CARD_HEIGHT;
    }

    /**
     * Set the client.
     * 
     * @param client client.
     */
    public void setClient(Client client) {
        instance.client = client;

        refreshCards(instance.grdCards, instance.client.getCards());
        refreshCards(instance.grdPlayableCards, instance.playableCards);
    }

}
