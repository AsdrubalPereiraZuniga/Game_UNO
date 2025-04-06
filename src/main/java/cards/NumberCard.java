package cards;

public class NumberCard extends Card {

    public NumberCard(String color, String value) {
        super(color, value, "/images/" + color + "_" + value + ".png"); // ejemplo: red_5.png
    }

    @Override
    public void play() {
    }
}