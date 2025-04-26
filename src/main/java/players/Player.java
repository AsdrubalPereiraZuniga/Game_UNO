/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package players;

import cards.Card;
import java.util.ArrayList;
import server.Flow;

/**
 * Represents a player in the game, including their username, cards,
 * communication flow, and readiness state.
 */
public class Player {

    private Flow flow;
    private String username;
    private ArrayList<Card> cards;
    private boolean ready;

    /**
     * Creates a new Player instance with the specified flow, username, and cards.
     *
     * @param flow the communication flow with the server
     * @param username the player's username
     * @param cards the list of cards the player holds
     */
    public Player(Flow flow, String username, ArrayList<Card> cards) {
        this.flow = flow;
        this.username = username;
        this.cards = cards;
        this.ready = false;
    }

    /**
     * Returns the communication flow of the player.
     *
     * @return the Flow instance
     */
    public Flow getFlow() {
        return flow;
    }

    /**
     * Returns the username of the player.
     *
     * @return the player's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the list of cards the player holds.
     *
     * @return the list of cards
     */
    public ArrayList<Card> getCards() {
        return cards;
    }

    /**
     * Returns whether the player is ready to start.
     *
     * @return true if ready, false otherwise
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Sets the communication flow for the player.
     *
     * @param flow the new Flow instance
     */
    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    /**
     * Sets the player's username.
     *
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the list of cards the player holds.
     *
     * @param cards the new list of cards
     */
    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    /**
     * Sets the player's readiness state.
     *
     * @param ready the new readiness value
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Returns a string representation of the player.
     *
     * @return a string containing flow, username, cards, and ready state
     */
    @Override
    public String toString() {
        return "Player{" + "flow=" + flow + ", username=" + username + ", cards=" + cards + ", ready=" + ready + '}';
    }
}