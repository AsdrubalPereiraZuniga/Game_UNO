/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package players;

import cards.Card;
import java.util.ArrayList;
import server.Flow;

/**
 *
 * @author jorge
 */
public class Player {

    private Flow flow;
    private String username;
    private ArrayList<Card> cards;

    public Player(Flow flow, String username, ArrayList<Card> cards) {
        this.username = username;
        this.flow = flow;
        this.cards = cards;
    }

    public Flow getFlow() {
        return flow;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

}
