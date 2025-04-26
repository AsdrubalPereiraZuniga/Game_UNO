package cards;

/**
 * Represents a wild card that can change the current color in the game.
 */
public class WildCard extends Card {

    /**
     * Constructs a WildCard with the specified color and value.
     * Sets the image path for the back image of the wild card.
     *
     * @param color the color associated with the wild card (typically "wild")
     * @param value the type of wild card (e.g., "wild", "draw4")
     */
    public WildCard(String color, String value) {
        super(color, value, "/images/back/" + color + value + ".png");
    }

    /**
     * Defines the behavior when the wild card is played.
     */
    @Override
    public void play() {
    }
}
