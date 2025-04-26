package client;

/**
 * Represents another player in the game, storing their name and the number of cards they hold.
 */
public class OtherPlayers {
    private String name;
    private int amountOfCards;

    /**
     * Returns the name of the other player.
     *
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the other player.
     *
     * @param name the new name of the player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the number of cards the player has.
     *
     * @return the amount of cards
     */
    public int getAmountOfCards() {
        return amountOfCards;
    }

    /**
     * Sets the number of cards the player has.
     *
     * @param amountOfCards the new amount of cards
     */
    public void setAmountOfCards(int amountOfCards) {
        this.amountOfCards = amountOfCards;
    }

    /**
     * Creates a new OtherPlayers instance with the specified name and card amount.
     *
     * @param name the player's name
     * @param amountOfCards the number of cards the player has
     */
    public OtherPlayers(String name, int amountOfCards) {
        this.name = name;
        this.amountOfCards = amountOfCards;
    }

    /**
     * Returns a string representation combining the player's name and card count.
     *
     * @return a string representing the player and their cards
     */
    @Override
    public String toString() {
        return name + " : " + amountOfCards;
    }
}
