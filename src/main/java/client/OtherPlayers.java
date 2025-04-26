package client;

/**
 * @author Ismael Marchena Méndez.
 * @author Jorge Rojas Mena.
 * @author Asdrubal Pererira Zuñiga.
 * @author Cesar Fabian Arguedas León.
 *
 * Handle the other players data.
 */
public class OtherPlayers {

    private int amountOfCards;
    private String name;

    /**
     * Create an object of type OtherPlayers.
     * 
     * @param name the player name.
     * @param amountOfCards amount of card of the player.
     */
    public OtherPlayers(String name, int amountOfCards) {
        this.name = name;
        this.amountOfCards = amountOfCards;
    }

    /**
     * Return the player name.
     *
     * @return the player name.
     */
    public String getName() {
        return name;
    }

    /**
     * Change the player name.
     *
     * @param name new player name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the amount of cards of the player.
     *
     * @return the amount of cards of the player.
     */
    public int getAmountOfCards() {
        return amountOfCards;
    }

    /**
     * Change the player amount of cards.
     *
     * @param amountOfCards the new amount.
     */
    public void setAmountOfCards(int amountOfCards) {
        this.amountOfCards = amountOfCards;
    }

    /**
     * To string of the player.
     * 
     * @return the name of the player and the amount of cards.
     */
    @Override
    public String toString() {
        return name + " : " + amountOfCards;
    }

}
