package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Clase que maneja la animación de fondo para el MainController
 */
public class BackgroundMain {
    
    private static final Color[] BACKGROUND_COLORS = {
        Color.web("#e63946"), 
        Color.web("#457B9D"),
        Color.web("#F4A261"),
        Color.web("#2A9D8F")
    };

    private static final Color[] CIRCLE_COLORS = {
        Color.web("#FF6B70"),
        Color.web("#6B99B7"),
        Color.web("#FFBB7F"),
        Color.web("#4DCDBF")
    };

    
    private final AnchorPane backgroundPane;
    private final AnchorPane parentPane;
    private Timeline colorAnimation;
    
    /**
     * Constructor para el administrador de animación de fondo
     * 
     * @param parentPane El AnchorPane principal donde se añadirá el fondo
     */
    public BackgroundMain(AnchorPane parentPane) {
        this.parentPane = parentPane;
        this.backgroundPane = new AnchorPane();
        
        // Asegurarse que el backgroundPane se coloque detrás de todo
        this.backgroundPane.setPickOnBounds(false);
        
        initializeBackground();
    }
    
    /**
     * Inicializa el fondo y comienza la animación
     */
    private void initializeBackground() {
        // Crear el rectángulo de fondo
        Rectangle background = createBackgroundRectangle();
        
        // Crear el círculo difuminado
        Circle blurredCircle = createBlurredCircle();
        
        // Añadir elementos al pane de fondo
        backgroundPane.getChildren().addAll(background, blurredCircle);
        
        // Añadir el pane de fondo al padre y asegurarse que esté al fondo
        parentPane.getChildren().add(0, backgroundPane);
        
        // Asegurar que el backgroundPane se ajusta al tamaño del parentPane
        AnchorPane.setTopAnchor(backgroundPane, 0.0);
        AnchorPane.setRightAnchor(backgroundPane, 0.0);
        AnchorPane.setBottomAnchor(backgroundPane, 0.0);
        AnchorPane.setLeftAnchor(backgroundPane, 0.0);
        
        // Iniciar la animación de color
        startColorAnimation(background, blurredCircle);
    }
    
    /**
     * Crea un rectángulo que cubre todo el fondo
     */
    private Rectangle createBackgroundRectangle() {
        Rectangle background = new Rectangle();
        background.setFill(BACKGROUND_COLORS[0]);
        
        // Vincular el tamaño del rectángulo al tamaño del pane padre
        background.widthProperty().bind(parentPane.widthProperty());
        background.heightProperty().bind(parentPane.heightProperty());
        
        return background;
    }
    
    /**
     * Crea un círculo difuminado en el centro de la pantalla
     */
    private Circle createBlurredCircle() {
        Circle circle = new Circle();

        // Tamaño relativo
        circle.radiusProperty().bind(
            Bindings.min(parentPane.widthProperty(), parentPane.heightProperty()).multiply(0.3)
        );

        // Centro
        circle.centerXProperty().bind(parentPane.widthProperty().divide(2));
        circle.centerYProperty().bind(parentPane.heightProperty().divide(2));

        // Color inicial
        circle.setFill(CIRCLE_COLORS[0]);

        // Más desenfoque
        GaussianBlur blur = new GaussianBlur(120); // ⬅️ más difuso
        circle.setEffect(blur);

        DropShadow glow = new DropShadow();
        glow.setBlurType(BlurType.GAUSSIAN);
        glow.setColor(CIRCLE_COLORS[0]);
        glow.setRadius(40); // más halo
        glow.setSpread(0.3);
        blur.setInput(glow);

        // Animación de pulsación
        addPulseAnimation(circle);

        return circle;
    }
    
    private void addPulseAnimation(Circle circle) {
        Timeline pulse = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(circle.scaleXProperty(), 1.0),
                new KeyValue(circle.scaleYProperty(), 1.0)
            ),
            new KeyFrame(Duration.seconds(2.5),
                new KeyValue(circle.scaleXProperty(), 1.4),
                new KeyValue(circle.scaleYProperty(), 1.4)
            ),
            new KeyFrame(Duration.seconds(5),
                new KeyValue(circle.scaleXProperty(), 1.0),
                new KeyValue(circle.scaleYProperty(), 1.0)
            )
        );

        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }   

    
    /**
     * Inicia la animación de cambio de color
     */
    private void startColorAnimation(Rectangle background, Circle blurredCircle) {
        colorAnimation = new Timeline();
        
        // Duración total para completar todo el ciclo de colores
        double cycleDuration = 15000 * BACKGROUND_COLORS.length; // 15 segundos por color
        
        for (int i = 0; i < BACKGROUND_COLORS.length; i++) {
            // Calcular el tiempo de inicio para este color
            double startTime = (i * 15000) / cycleDuration;
            
            // Calcular el tiempo de fin (que es el inicio del siguiente color)
            double endTime = ((i + 1) * 15000) / cycleDuration;
            
            // El color siguiente (volviendo al primero si es necesario)
            int nextIndex = (i + 1) % BACKGROUND_COLORS.length;
            
            // Añadir keyframes para el fondo
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(startTime * cycleDuration),
                    new KeyValue(background.fillProperty(), BACKGROUND_COLORS[i]))
            );
            
            // Transición suave al siguiente color
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(endTime * cycleDuration),
                    new KeyValue(background.fillProperty(), BACKGROUND_COLORS[nextIndex]))
            );
            
            // Añadir keyframes para el círculo
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(startTime * cycleDuration),
                    new KeyValue(blurredCircle.fillProperty(), CIRCLE_COLORS[i]))
            );
            
            // Transición suave al siguiente color para el círculo
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(endTime * cycleDuration),
                    new KeyValue(blurredCircle.fillProperty(), CIRCLE_COLORS[nextIndex]))
            );
            
            // Actualizar también el color del efecto de brillo
            DropShadow glow = (DropShadow) ((GaussianBlur) blurredCircle.getEffect()).getInput();
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(startTime * cycleDuration),
                    new KeyValue(glow.colorProperty(), CIRCLE_COLORS[i]))
            );
            
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(endTime * cycleDuration),
                    new KeyValue(glow.colorProperty(), CIRCLE_COLORS[nextIndex]))
            );
        }
        
        // Hacer que la animación se repita indefinidamente
        colorAnimation.setCycleCount(Animation.INDEFINITE);
        
        // Iniciar la animación
        colorAnimation.play();
    }
    
    /**
     * Detiene la animación y libera recursos
     */
    public void stopAnimation() {
        if (colorAnimation != null) {
            colorAnimation.stop();
        }
    }
}