package client;

import cards.Card;
import java.util.ArrayList;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
 *
 * @author igmml
 */
public class HandleCards {

    // Constantes de diseño
    private static final double NORMAL_WIDTH = 80;
    private static final double HOVER_WIDTH = 80;
    private static final double CARD_HEIGHT = 120;
    private static final Duration ANIMATION_DURATION = Duration.millis(200);
    private static HandleCards instance;
    private GridPane grdCards;
    private Client client;
    private GridPane grdPlayableCards;
    private ArrayList<Card> playableCards;

    private void configureGridPane() {
        instance.grdCards.setHgap(10);
        instance.grdCards.setVgap(10);
        instance.grdCards.setAlignment(Pos.CENTER);
    }

    public void initialize(GridPane grid, Client client, GridPane gridPC) {
        instance.grdCards = grid;
        instance.client = client;
        instance.grdPlayableCards = gridPC;
        instance.playableCards = new ArrayList<>();
        configureGridPane();
    }

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
            VBox cardContainer = createCardContainer(
                    instance.client.getCards().get(i).toString(),
                    i, instance.client.getCards());
            instance.grdCards.add(cardContainer, i, 0);
        }
    }

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

        // Crear cartas
        for (int i = 0; i < cardCount; i++) {
            VBox cardContainer
                    = createCardContainer(
                            cards.get(i).toString(), i, cards);
            grid.add(cardContainer, i, 0);
        }
    }

    private void removeCard(int cardIndex) {
        //add
        instance.playableCards.add(instance.client.getCards().get(cardIndex));
        //remove
        instance.client.getCards().remove(cardIndex);
        //refresh
        refreshCards(instance.grdCards, instance.client.getCards());
        refreshCards(instance.grdPlayableCards, instance.playableCards);
    }

    private boolean removeFromPlayableCards(int cardIndex, String value) {
        if (instance.playableCards.isEmpty()) {
            return false;
        }
        if (cardIndex >= instance.playableCards.size()) {
            return false;
        }
        String cardValue = instance.playableCards.get(cardIndex).getColor()
                + instance.playableCards.get(cardIndex).getValue();
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

    public static HandleCards getInstace() {
        if (HandleCards.instance == null) {
            HandleCards.instance = new HandleCards();
            return HandleCards.instance;
        }
        return HandleCards.instance;
    }

    public ArrayList<Card> getPlayCards() {
        return instance.playableCards;
    }

    public double getNORMAL_WIDTH() {
        return NORMAL_WIDTH;
    }

    public double getCARD_HEIGTH() {
        return CARD_HEIGHT;
    }

    public void setClient(Client client) {
        instance.client = client;

        refreshCards(instance.grdCards, instance.client.getCards());
        refreshCards(instance.grdPlayableCards, instance.playableCards);
    }
}