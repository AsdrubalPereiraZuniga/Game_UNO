package controllers;

import cards.Card;
import cards.WildCard;
import client.Client;
import client.HandleCards;
import client.OtherPlayers;
import client.TurnHandler;
import client.ViewCardsHandler;
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
import javafx.scene.effect.Glow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import server.Server;

/**
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 * 
 * Main screen controller class, handle all the user events while playing.
 */
public class MainController implements Initializable {
    private String selectedColor = "";
    private Card lastCard;
    private Client client;
    private static final Duration ANIMATION_DURATION = Duration.millis(200);
    private static MainController instanceController;

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
    private Button btnBlue;
    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnGreen;
    @FXML
    private Button btnOne;
    @FXML
    private Button btnRed;
    @FXML
    private Button btnYellow;
    @FXML
    private GridPane grdCards;
    @FXML
    private GridPane grdPlayableCards;
    @FXML
    private HBox colorSelector;
    @FXML
    private HBox hbxOtherPlayers;
    @FXML
    private ImageView deckImage;
    @FXML
    private Label lblCurrentTurn;
    @FXML
    private Label lblPlayerName;
    
    
    private BackgroundMain backgroundAnimation;
    
    
    /**
     * Initialize the controller class.
     *
     * @param url URL.
     * @param rb Resource Bundle.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.backgroundAnimation = new BackgroundMain(bgView);
        this.lblPlayerName.setText(instanceController.client.getPlayerName());
        HandleCards.getInstace().setCards(this.grdCards, 
                instanceController.client, this.grdPlayableCards);
        setOtherPlayers();
        instanceController.lastCard = null;
        setTopCard(instanceController.client.getTopCard());
        TurnHandler.setLabel(lblCurrentTurn);
        ViewCardsHandler.setGridPlayerCards(grdPlayableCards);
        ViewCardsHandler.setAnchorPane(usedCardsView);
        instanceController.client.sendMessage("GET_TURN/");
        setupHoverEffects(btnRed);
        setupHoverEffects(btnGreen);
        setupHoverEffects(btnBlue);
        setupHoverEffects(btnYellow);
        setDeckImage();
    }
    
    /**
     * Set the deck Image
     */
    private void setDeckImage() {
        try {
            deckImage.setImage(new Image("/images/behind/K1.png"));

            deckImage.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), deckImage);
                st.setToX(1.1);
                st.setToY(1.1);
                st.play();

                Glow glow = new Glow();
                glow.setLevel(0.5);
                deckImage.setEffect(glow);
            });

            deckImage.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), deckImage);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();

                deckImage.setEffect(null);
            });

            deckImage.setOnMouseClicked(e -> drawCardIfNeeded());
        } catch (Exception e) {
            System.out.println("No se pudo cargar la imagen del mazo.");
        }
    }

    /**
     * Draw cards.
     */
    private void drawCardIfNeeded() {
        if (!instanceController.client.isWaiting()) {
            if (HandleCards.getInstace().getPlayCards().isEmpty()) {
                instanceController.client.sendMessage("DRAW/");
            }
        }
    }

    /**
     * Refresh the deck.
     */
    public void refreshHand() {
        HandleCards.getInstace().setClient(instanceController.client);
    }

    /**
     * Sets the view displaying other players.
     */
    /**
     * Set the other players.
     */
    private void setOtherPlayers() {
        for (OtherPlayers otherPlayer :
                instanceController.client.getOtherPlayers()) {
            if (!otherPlayer.getName().equals(instanceController.
                    client.getPlayerName())) {
                this.hbxOtherPlayers.getChildren().add(new Label(
                        otherPlayer.getName() + ": "
                        + otherPlayer.getAmountOfCards()));
            }
        }
    }

    /**
     * Return the main controller instance.
     * 
     * @return the main controller instance.
     */
    public static MainController getInstanceController() {
        if (MainController.instanceController == null) {
            MainController.instanceController = new MainController();
            return MainController.instanceController;
        }
        return MainController.instanceController;
    }

    /**
     * Set the client.
     *
     * @param client client.
     */
    public void setClient(Client client) {
        MainController.instanceController.client = client;
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
     * Confirm the cards that the player will be play.
     * Handles the confirm button event when playing a card.
     *
     * @param event the button click event
     */
    @FXML
    private void confirm(ActionEvent event) {
        Platform.runLater(() -> {
            MainController.getInstanceController().refreshHand();
        });
        if (HandleCards.getInstace().getPlayCards().isEmpty()) {
            return;
        }

        Card card = HandleCards.getInstace().getPlayCards().get(0);

        if (card instanceof WildCard && card.getColor().equals("C")) {
            showColorSelector();
            return;
        }

        proceedWithCard();
    }

    /**
     * Show the selector of colors.
     */
    private void showColorSelector() {
        colorSelector.setVisible(true);
    }

    /**
     * Handle the card played.
     */
    public void proceedWithCard() {
        Card card = HandleCards.getInstace().getPlayCards().get(0);
        instanceController.lastCard = instanceController.client.getTopCard();    
        
        if (canPlay(HandleCards.getInstace().getPlayCards())) {                                        
            ViewCardsHandler.updateUsedViewCard(getNewCard(card));            
            instanceController.client.sendMessage(createMessage());
            instanceController.lastCard = 
                    instanceController.client.getTopCard();

            HandleCards.getInstace().getPlayCards().clear();
        } else {
            for (Card playCard : HandleCards.getInstace().getPlayCards()) {
                instanceController.client.getCards().add(playCard);
            }
            HandleCards.getInstace().getPlayCards().clear();
            HandleCards.getInstace().setClient(instanceController.client);
        }
    }


    /**
     * Updates the visual representation of the top card on the discard pile.
     *
     * @param card the card to display
     */
    public void setTopCard(Card card) {        
        this.usedCardsView.getChildren().clear();
        this.usedCardsView.getChildren().add(getNewCard(card));
        this.grdPlayableCards.getChildren().clear();
        instanceController.lastCard = instanceController.client.getTopCard();
    }


    /**
     * Creates the message to send to the server when a card is played.
     *
     * @return the message string
     */
    private String createMessage() {
        String message = "";
        String code;

        message = "PUT/" + getCardsValue();
        return message;
    }


    /**
     * Generates the string representation of the played cards.
     *
     * @return the string of card values
     */
    private String getCardsValue() {
        String cardsValue = "";
        for (Card playableCard : HandleCards.getInstace().getPlayCards()) {
            cardsValue = cardsValue + playableCard.toString() + "/";
        }
        return cardsValue;
    }


    /**
     * Determines if the current cards can be legally played.
     *
     * @param cards the list of cards to check
     * @return true if playable, false otherwise
     */
    private boolean canPlay(ArrayList<Card> cards) {
        if (instanceController.lastCard == null) {
            return true;
        }
        if (cards.size() == 1) {
            return handleOnePlayedCard(cards.get(0));
        } else {
            return handleManyPlayedCards(cards.get(0));
        }
    }


    
    /**
     * Validates if a single card can be played based on color or value.
     *
     * @param card the card to validate
     * @return true if playable, false otherwise
     */
    private boolean handleOnePlayedCard(Card card) {
        if (card instanceof WildCard) {
            return true;
        }

        if (instanceController.lastCard.getColor().equals(card.getColor())) {
            return true;
        }
        if (instanceController.lastCard.getValue().equals(card.getValue())) {
            return true;
        }
        return false;
    }


    /**
     * Validates if a sequence of cards can be played.
     *
     * @param card the starting card
     * @return true if playable, false otherwise
     */
    private boolean handleManyPlayedCards(Card card) {

        if (instanceController.lastCard.getColor().equals(card.getColor())) {
            return true;
        }

        if (instanceController.lastCard.getValue().equals(card.getValue())) {
            return true;
        }
        return false;
    }

 
    /**
     * Creates a visual VBox representation of a card.
     *
     * @param card the card to display
     * @return the VBox containing the card view
     */
    private VBox getNewCard(Card card) {
        VBox cardContainer = new VBox();
        double NORMAL_WIDTH = HandleCards.getInstace().getNORMAL_WIDTH();
        double CARD_HEIGHT = HandleCards.getInstace().getCARD_HEIGTH();
        cardContainer.setPrefSize(NORMAL_WIDTH, CARD_HEIGHT);
        cardContainer.setStyle("-fx-background-color: transparent;");
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
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
            cardContainer.getChildren().add(label);
        }

        return cardContainer;
    }


    /**
     * Handles the color selection for a Wild Card and proceeds with the move.
     *
     * @param e the button click event
     */
    @FXML
    private void selectColor(ActionEvent e) {
        Button source = (Button) e.getSource();
        String color = "";

        if (source == btnRed) {
            color = "R";
        } else if (source == btnGreen) {
            color = "G";
        } else if (source == btnBlue) {
            color = "B";
        } else if (source == btnYellow) {
            color = "Y";
        }

        Card wildCard = HandleCards.getInstace().getPlayCards().get(0);

        colorSelector.setVisible(false);
        proceedWithCard();
        instanceController.client.sendMessage("COLORSELECTED/" + color + "0/");
    }


    
    /**
     * Sets up hover effects for the color selection buttons.
     *
     * @param btn the button to apply effects to
     */
    private void setupHoverEffects(Button btn) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setSpread(0.2);
        double normalSize = btn.getWidth();

        btn.setOnMouseEntered(e -> {
            btn.setStyle(btn.getStyle() + " -fx-scale-x: 1.5; -fx-scale-y: 1.5;");
            btn.setEffect(shadow);
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(btn.getStyle() + " -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
            btn.setEffect(null);
        });
    }
}
