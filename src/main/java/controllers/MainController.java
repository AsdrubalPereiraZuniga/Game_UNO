package controllers;

import cards.Card;
import client.Client;
import client.HandleCards;
import client.OtherPlayers;
import client.TurnHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import java.io.IOException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Main screen controller class.
 *
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 */
public class MainController implements Initializable {

    private static MainController instance;

    private Client client;
    @FXML
    private AnchorPane bgView;
    @FXML
    private AnchorPane deckView;
    @FXML
    private AnchorPane player;
    @FXML
    private AnchorPane players;
    @FXML
    private AnchorPane usedCardsView;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnOne;
    @FXML
    private Label lblPlayerName;
    @FXML
    private GridPane grdCards;
    @FXML
    private HBox hbxOtherPlayers;
    @FXML
    private GridPane grdPlayableCards;

    private Card lastCard;
    @FXML
    private Label lblCurrentTurn;

    /**
     * Initialize the controller class.
     *
     * @param url URL.
     * @param rb Resource Bundle.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.lblPlayerName.setText(instance.client.getPlayerName());
        HandleCards.getInstace().setCards(this.grdCards, instance.client,
                this.grdPlayableCards);
        setOtherPlayers();
        instance.lastCard = null;
        setTopCard(instance.client.getTopCard());
        TurnHandler.setLabel(lblCurrentTurn);
        instance.client.sendMessage("GET_TURN/");
    }

    private void setOtherPlayers() {
        System.out.println("Nombre del men:" + instance.client.getPlayerName());
        for (OtherPlayers otherPlayer : instance.client.getOtherPlayers()) {
            System.out.println("LOLAAAAAAAAAAAAAAAAAAA:" + otherPlayer.toString());
            if (!otherPlayer.getName().equals(instance.client.getPlayerName())) {
                this.hbxOtherPlayers.getChildren().add(new Label(
                        otherPlayer.getName() + ": "
                        + otherPlayer.getAmountOfCards()));
            }
        }
    }

    public static MainController getInstance() {
        if (MainController.instance == null) {
            MainController.instance = new MainController();
            return MainController.instance;
        }
        return MainController.instance;
    }

    /**
     * Set the client.
     *
     * @param client client.
     */
    public void setClient(Client client) {
        MainController.instance.client = client;
    }

    /**
     * Call one.
     *
     * @param event event.
     */
    @FXML
    private void callOne(ActionEvent event) {
    }

    /**
     * Confirm.
     *
     * @param event event.
     */
    @FXML
    private void confirm(ActionEvent event) {
        if (HandleCards.getInstace().getPlayCards().isEmpty()) {
            return;
        }
        int last = HandleCards.getInstace().getPlayCards().size() - 1;
        Card card = HandleCards.getInstace().getPlayCards().get(last);

        if (canPlay(HandleCards.getInstace().getPlayCards())) {
            setTopCard(card);
            instance.client.sendMessage(createMessage());
            HandleCards.getInstace().getPlayCards().clear();
        } else {
            for (Card playCard : HandleCards.getInstace().getPlayCards()) {
                instance.client.getCards().add(playCard);
            }
            HandleCards.getInstace().getPlayCards().clear();
            HandleCards.getInstace().setClient(instance.client);
        }
    }

    public void setTopCard(Card card) {
        this.usedCardsView.getChildren().clear();
        this.usedCardsView.getChildren().add(getNewCard(card));
        this.grdPlayableCards.getChildren().clear();
        instance.lastCard = card;
    }

    private String createMessage() {
        String message = "";
        String code;

        message = "PUT/" + getCardsValue();

        System.out.println("message: " + message);
        return message;
    }

    private String getCardsValue() {
        String cardsValue = "";
        for (Card playableCard : HandleCards.getInstace().getPlayCards()) {
            cardsValue = cardsValue + playableCard.toString() + "/";
        }
        return cardsValue;
    }

    private boolean canPlay(ArrayList<Card> cards) {
        if (instance.lastCard == null) {
            return true;
        }
        if (cards.size() == 1) {
            return handleOnePlayedCard(cards.get(0));
        } else {
            return handleManyPlayedCards(cards.get(0));
        }
    }

    private boolean handleOnePlayedCard(Card card) {
        if (instance.lastCard.getColor().
                equals(card.getColor())) {
            return true;
        }
        if (instance.lastCard.getValue().
                equals(card.getValue())) {
            return true;
        }
        //verificar si la puede colocar sobre una de cambio de color o los +4
        return false;
    }

    private boolean handleManyPlayedCards(Card card) {
        if (instance.lastCard.getColor().equals(card.getColor())) {
            return true;
        }
        if (instance.lastCard.getValue().equals(card.getValue())) {
            return true;
        }
        return false;
    }

    private VBox getNewCard(Card card) {
        VBox cardContainer = new VBox();
        double NORMAL_WIDTH = HandleCards.getInstace().getNORMAL_WIDTH();
        double CARD_HEIGHT = HandleCards.getInstace().getCARD_HEIGTH();
        cardContainer.setPrefSize(NORMAL_WIDTH, CARD_HEIGHT);
        cardContainer.setStyle("-fx-background-color: white; -fx-border-color: "
                + "#333; -fx-border-radius: 5;");
        cardContainer.setAlignment(Pos.CENTER);

        try {
            ImageView cardImage = new ImageView(new Image(
                    card.getImagePath()));
            cardImage.setPreserveRatio(true);
            cardImage.setFitWidth(NORMAL_WIDTH - 10);
            cardContainer.getChildren().add(cardImage);
        } catch (Exception e) {
            // Fallback a Label si no hay imagen
            String cardText = card.getColor() + card.getValue();
            Label label = new Label(cardText);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            cardContainer.getChildren().add(label);
        }
        return cardContainer;
    }
}
