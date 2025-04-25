package cards;

public abstract class Card {
    protected String color;
    protected String value;
    protected String imagePath;

    public Card(String color, String value, String imagePath) {
        this.color = color;
        this.value = value;
        this.imagePath = imagePath;
    }

    public String getColor() { return color; }
    
    public void setColor(String color) {
        this.color = color;
    }

    public String getValue() { return value; }

    public String getImagePath() { return imagePath; }

    public abstract void play();

    public boolean canBePlayedOver(Card topCard) {
        return this.color.equals(topCard.getColor()) ||
                this.value.equals(topCard.getValue()) ||
                this.color.equals("wild");
    }

    public boolean isWildCard() {
        return this.color.equals("wild");
    }

    @Override
    public String toString() {
        return this.color + this.value;
    }
}
