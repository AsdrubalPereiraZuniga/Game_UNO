package cards;

public class WildCard extends Card {

    public WildCard(String color, String value) {
        super(color, value, "/images/back/" + color + value + ".png");//value: "wild", "draw4"
    }

    @Override
    public void play() {
    }
}