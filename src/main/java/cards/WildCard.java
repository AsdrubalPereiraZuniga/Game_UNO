package cards;

public class WildCard extends Card {

    public WildCard(String color, String value) {
        super(color, value, "/images/" + color + "_" + value + ".png");//value: "wild", "draw4"
    }

    @Override
    public void play() {
    }
}