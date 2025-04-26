package cards;

/**
 * Represents a numbered card with a specific color and value.
 */
public class NumberCard extends Card {

    /**
     * Constructs a NumberCard with the specified color and value.
     * Sets the image path based on the color and value.
     *
     * @param color the color of the card
     * @param value the numeric value of the card
     */
    public NumberCard(String color, String value) {
        super(color, value, "/images/" + getFolderValue(color) + "/" + color + value + ".png");
    }

    /**
     * Returns the folder name associated with a color abbreviation.
     *
     * @param color the color abbreviation (B, G, R, Y)
     * @return the full folder name as a string
     */
    private static String getFolderValue(String color) {
        switch (color) {
            case "B":
                return "blue";
            case "G":
                return "green";
            case "R":
                return "red";
            case "Y":
                return "yellow";
            default:
                return "";
        }
    }

    /**
     * Defines the behavior when the number card is played.
     */
    @Override
    public void play() {
    }
}
