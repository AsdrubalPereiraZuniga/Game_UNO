package controllers;

import client.Client;
import java.net.URL;
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
import javafx.scene.layout.GridPane;
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

    private static Client client;
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
    
        // Constantes de diseño
    private static final double NORMAL_WIDTH = 80;
    private static final double HOVER_WIDTH = 80;
    private static final double CARD_HEIGHT = 120;
    private static final Duration ANIMATION_DURATION = Duration.millis(200);

    /**
     * Initialize the controller class.
     *
     * @param url URL.
     * @param rb Resource Bundle.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.lblPlayerName.setText(client.getPlayerName());
        configureGridPane();
        setCards();
    }

    private void configureGridPane() {
        grdCards.setHgap(10);
        grdCards.setVgap(10);
        grdCards.setAlignment(Pos.CENTER);
    }

    public void setCards() {
        grdCards.getChildren().clear();
        grdCards.getColumnConstraints().clear();
        
        int cardCount = client.getCards().size();
        if (cardCount == 0) return;
        
        // Configurar columnas
        for (int i = 0; i < cardCount; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.SOMETIMES);
            cc.setFillWidth(true);
            grdCards.getColumnConstraints().add(cc);
        }
        
        // Crear cartas
        for (int i = 0; i < cardCount; i++) {
            VBox cardContainer = 
                    createCardContainer(client.getCards().get(i), i);
            grdCards.add(cardContainer, i, 0);
        }
    }

    private VBox createCardContainer(String cardText, int cardIndex) {
        VBox cardContainer = new VBox();
        cardContainer.setPrefSize(NORMAL_WIDTH, CARD_HEIGHT);
        cardContainer.setStyle("-fx-background-color: white; -fx-border-color: "
                + "#333; -fx-border-radius: 5;");
        cardContainer.setAlignment(Pos.CENTER);
        
        // Usar ImageView si tienes imágenes de cartas
        try {
            ImageView cardImage = new ImageView(new Image(
                getClass().getResourceAsStream("/images/cards/" + 
                        cardText + ".png")
            ));
            cardImage.setPreserveRatio(true);
            cardImage.setFitWidth(NORMAL_WIDTH - 10);
            cardContainer.getChildren().add(cardImage);
        } catch (Exception e) {
            // Fallback a Label si no hay imagen
            Label label = new Label(cardText);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            cardContainer.getChildren().add(label);
        }
        
        // Configurar efectos hover
        setupHoverEffects(cardContainer);
        
        // Configurar evento click
        cardContainer.setOnMouseClicked(e -> handleCardClick(cardIndex));
        
        return cardContainer;
    }

    private void setupHoverEffects(VBox cardContainer) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setSpread(0.2);
        double normalSize = cardContainer.getWidth();
        
        cardContainer.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(ANIMATION_DURATION, 
                    cardContainer);
            cardContainer.setPrefWidth(HOVER_WIDTH);
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
            cardContainer.setPrefWidth(normalSize);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
            
            cardContainer.setEffect(null);
            cardContainer.setStyle("-fx-background-color: white; "
                    + "-fx-border-color: #333;");
        });
    }

    private void handleCardClick(int cardIndex) {
        System.out.println("Carta seleccionada: " + 
                client.getCards().get(cardIndex));
        // Aquí puedes agregar la lógica para seleccionar/descartar la carta
    }

    /**
     * Set the client.
     *
     * @param client client.
     */
    public static void setClient(Client client) {
        MainController.client = client;
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
    }

}
