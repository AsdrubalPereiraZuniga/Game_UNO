/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cards;

/**
 *
 * @author jorge
 */
public class Card {
    
    private String color;
    private String number;
    private String type;

    public Card(String color, String number, String type) {
        this.color = color;
        this.number = number;
        this.type = type;
    }

    public String getColor() {
        return color;
    }

    public String getNumber() {
        return number;
    }

    public String getType() {
        return type;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setType(String type) {
        this.type = type;
    }
    

}
