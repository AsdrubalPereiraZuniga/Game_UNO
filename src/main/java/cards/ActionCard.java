package cards;

public class ActionCard extends Card {

    public ActionCard(String color, String value) {
        super(color, value, "/images/" +getFolderValue(color)+"/"+ color + value + ".png");
    }

    private static String getFolderValue(String color){
        switch (color) {
            case "B":
                return "blue";
            case "G":
                return "green";
            case "R": 
                return "red";
            case "Y": 
                return "yellow";
            default:
                return "";
        }
    }
    @Override
    public void play() {

    }
}