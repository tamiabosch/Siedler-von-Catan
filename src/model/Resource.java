package model;

public class Resource {

    private String type;
    private int value;

    public Resource(String type) {
        this.type = type;
        this.value = 0;
    }

    public String getTypeGerman(){
        switch(type) {
            case "lumber":
                return "Holz";
            case "brick":
                return "Lehm";
            case "wool":
                return "Wolle";
            case "grain":
                return "Getreide";
            case "ore":
                return "Erz";
            default:
                return null;
        }
    }

    public String getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void changeValue(int number) { this.value += number; }

    @Override
    public String toString(){
        return type + ": "  + value;
    }

}
