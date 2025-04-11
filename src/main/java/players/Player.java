/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package players;

import server.Flow;

/**
 *
 * @author jorge
 */
public class Player {
    
    private String username;
    private Flow flow;
    //Un array de cartas

    public Player(Flow flow,String username) {
        this.flow = flow;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }
    
    
    
    
}
