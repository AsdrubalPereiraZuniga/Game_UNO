package cards;

public class ActionCard extends Card {

    public ActionCard(String color, String value) {
        super(color, value, "/images/" + color + "_" + value + ".png");//value: "skip", "reverse", "draw2"
    }

    @Override
    public void play() {

    }
}