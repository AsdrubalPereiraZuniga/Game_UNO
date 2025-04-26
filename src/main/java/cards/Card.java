package cards;

/**
 * Abstract class that represents a generic card with color, value, and image path.
 */
public abstract class Card {
    protected String color;
    protected String value;
    protected String imagePath;

    /**
     * Creates a new Card with specified color, value, and image path.
     *
     * @param color the color of the card
     * @param value the value of the card
     * @param imagePath the path to the image representing the card
     */
    public Card(String color, String value, String imagePath) {
        this.color = color;
        this.value = value;
        this.imagePath = imagePath;
    }

    /**
     * Returns the color of the card.
     *
     * @return the color
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets a new color for the card.
     *
     * @param color the new color
     */
    public void setColor(String color) {
        this.color = color;
    }

    /**
     * Returns the value of the card.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the image path of the card.
     *
     * @return the image path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Defines the action that occurs when the card is played.
     */
    public abstract void play();

    /**
     * Determines if this card can be played over the given top card.
     *
     * @param topCard the card currently on top of the discard pile
     * @return true if this card can be played over the top card, otherwise false
     */
    public boolean canBePlayedOver(Card topCard) {
        return this.color.equals(topCard.getColor()) ||
               this.value.equals(topCard.getValue()) ||
               this.color.equals("wild");
    }

    /**
     * Determines if this card is a wild card.
     *
     * @return true if the card is wild, otherwise false
     */
    public boolean isWildCard() {
        return this.color.equals("wild");
    }

    /**
     * Returns a string representation of the card combining its color and value.
     *
     * @return a string representing the card
     */
    @Override
    public String toString() {
        return this.color + this.value;
    }
}
