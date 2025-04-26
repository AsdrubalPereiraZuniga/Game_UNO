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
 * Manages the animated background for the MainController.
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
     * Constructs a BackgroundMain instance.
     *
     * @param parentPane The parent AnchorPane where the animated background will be added.
     */
    public BackgroundMain(AnchorPane parentPane) {
        this.parentPane = parentPane;
        this.backgroundPane = new AnchorPane();
        this.backgroundPane.setPickOnBounds(false);
        initializeBackground();
    }

    /**
     * Initializes the background by creating and setting up background elements.
     */
    private void initializeBackground() {
        Rectangle background = createBackgroundRectangle();
        Circle blurredCircle = createBlurredCircle();

        backgroundPane.getChildren().addAll(background, blurredCircle);
        parentPane.getChildren().add(0, backgroundPane);

        AnchorPane.setTopAnchor(backgroundPane, 0.0);
        AnchorPane.setRightAnchor(backgroundPane, 0.0);
        AnchorPane.setBottomAnchor(backgroundPane, 0.0);
        AnchorPane.setLeftAnchor(backgroundPane, 0.0);

        startColorAnimation(background, blurredCircle);
    }

    /**
     * Creates a Rectangle that covers the entire background.
     *
     * @return Rectangle element used as the base background.
     */
    private Rectangle createBackgroundRectangle() {
        Rectangle background = new Rectangle();
        background.setFill(BACKGROUND_COLORS[0]);
        background.widthProperty().bind(parentPane.widthProperty());
        background.heightProperty().bind(parentPane.heightProperty());
        return background;
    }

    /**
     * Creates a blurred Circle placed at the center of the screen.
     *
     * @return Circle element with a blur and glow effect.
     */
    private Circle createBlurredCircle() {
        Circle circle = new Circle();
        circle.radiusProperty().bind(
            Bindings.min(parentPane.widthProperty(), parentPane.heightProperty()).multiply(0.3)
        );
        circle.centerXProperty().bind(parentPane.widthProperty().divide(2));
        circle.centerYProperty().bind(parentPane.heightProperty().divide(2));
        circle.setFill(CIRCLE_COLORS[0]);

        GaussianBlur blur = new GaussianBlur(120);
        circle.setEffect(blur);

        DropShadow glow = new DropShadow();
        glow.setBlurType(BlurType.GAUSSIAN);
        glow.setColor(CIRCLE_COLORS[0]);
        glow.setRadius(40);
        glow.setSpread(0.3);
        blur.setInput(glow);

        addPulseAnimation(circle);

        return circle;
    }

    /**
     * Adds a pulsing animation to the given Circle, creating a scale oscillation effect.
     *
     * @param circle The Circle to which the pulse animation will be applied.
     */
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
     * Starts the color transition animation for both the background and the blurred circle.
     *
     * @param background The Rectangle whose color will animate.
     * @param blurredCircle The Circle whose color and glow will animate.
     */
    private void startColorAnimation(Rectangle background, Circle blurredCircle) {
        colorAnimation = new Timeline();

        double cycleDuration = 15000 * BACKGROUND_COLORS.length;

        for (int i = 0; i < BACKGROUND_COLORS.length; i++) {
            double startTime = (i * 15000) / cycleDuration;
            double endTime = ((i + 1) * 15000) / cycleDuration;
            int nextIndex = (i + 1) % BACKGROUND_COLORS.length;

            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(startTime * cycleDuration),
                    new KeyValue(background.fillProperty(), BACKGROUND_COLORS[i]))
            );
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(endTime * cycleDuration),
                    new KeyValue(background.fillProperty(), BACKGROUND_COLORS[nextIndex]))
            );

            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(startTime * cycleDuration),
                    new KeyValue(blurredCircle.fillProperty(), CIRCLE_COLORS[i]))
            );
            colorAnimation.getKeyFrames().add(
                new KeyFrame(Duration.millis(endTime * cycleDuration),
                    new KeyValue(blurredCircle.fillProperty(), CIRCLE_COLORS[nextIndex]))
            );

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

        colorAnimation.setCycleCount(Animation.INDEFINITE);
        colorAnimation.play();
    }

    /**
     * Stops the background animation and releases resources.
     */
    public void stopAnimation() {
        if (colorAnimation != null) {
            colorAnimation.stop();
        }
    }
}