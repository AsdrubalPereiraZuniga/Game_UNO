package cards;

/**
 * Represents an action card with a specific color and value.
 */
public class ActionCard extends Card {

    /**
     * Constructs an ActionCard with the specified color and value.
     * Sets the image path based on the color and value.
     *
     * @param color the color of the card
     * @param value the action type of the card (e.g., "skip", "reverse")
     */
    public ActionCard(String color, String value) {
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
     * Defines the behavior when the action card is played.
     */
    @Override
    public void play() {

    }
}
