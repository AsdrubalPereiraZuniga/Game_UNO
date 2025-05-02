/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

/**
 *
 * @author Asdrubal
 */
public class SayOneHandler {
    
    public static Image image = new Image("/images/more/SayOne.png");
    public static Label lblSayOne;
    public static ImageView imgSayOne;
    private static RotateTransition rotate;
    
    /**
     * Method to manage a label to display the username
     * 
     * @param label label of view
     */    
    public static void setLabel(Label label){
        lblSayOne = label;
    }            
    
    /**
     * Method to manage a imageView to display ONE image with rotation animation
     * 
     * @param img imageView of the view
     */    
    public static void setImageView(ImageView img){
        imgSayOne = img;
    }
    
    /**
     * method for return a imageView of the view
     * 
     * @return imageView of the view
     */
    public static ImageView getImageView(){
        return imgSayOne;
    }
    
    /**
     * method responsible of animation in ImageView when user says ONEEE
     * 
     * @param duration duration in seconds of animation
     */
    public static void imageAnimation(Duration duration){
        
        imgSayOne.setImage(image);
       
            // Fade in label
        FadeTransition fadeInLabel = new FadeTransition(Duration.millis(300), lblSayOne);
        fadeInLabel.setFromValue(0.0);
        fadeInLabel.setToValue(1.0);

        // Fade in image
        FadeTransition fadeInImage = new FadeTransition(Duration.millis(300), imgSayOne);
        fadeInImage.setFromValue(0.0);
        fadeInImage.setToValue(1.0);

        // Start rotating the image
        rotate = new RotateTransition(Duration.seconds(1), imgSayOne);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.play();

        // Stay visible for 2 seconds
        PauseTransition stay = new PauseTransition(duration);

        // Fade out label
        FadeTransition fadeOutLabel = new FadeTransition(Duration.millis(300), lblSayOne);
        fadeOutLabel.setFromValue(1.0);
        fadeOutLabel.setToValue(0.0);

        // Fade out image
        FadeTransition fadeOutImage = new FadeTransition(Duration.millis(300), imgSayOne);
        fadeOutImage.setFromValue(1.0);
        fadeOutImage.setToValue(0.0);

        // After fade out, hide and stop rotation
        fadeOutImage.setOnFinished(e -> {
            imgSayOne.setVisible(false);
            lblSayOne.setVisible(false);
            rotate.stop();
            imgSayOne.setRotate(0);
        });

        // Play the sequence
        SequentialTransition seq = new SequentialTransition(fadeInLabel, fadeInImage, stay, fadeOutLabel, fadeOutImage);
        seq.play();
    }
    
    /**
     * method used to handle the ONE event
     * 
     * @param userName that says ONEEEE
     */
    public static void showSayOne(String userName){
       lblSayOne.setText("Jugador: " + userName + " dijo UNOOO!!");       
       lblSayOne.setVisible(true);
       imgSayOne.setVisible(true);
       
       imageAnimation(Duration.seconds(2));       
    }
    
    /**
     *  method responsible for releasing resources of this class
     */ 
    public static void clear(){
        lblSayOne.setText("");
        imgSayOne.setImage(null);
        rotate = null;
    }
    
}
