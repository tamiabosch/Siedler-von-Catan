package model;

public class Settlement {

    private boolean isCity;
    private Player owner;
    private Utility.Pair coordinates;
    
    public Settlement(Player owner, Utility.Pair coordinates) {
        this.owner = owner;
        this.coordinates = coordinates;
    }

    public boolean isCity() {
        return isCity;
    }

    public void setCityTrue() {
        isCity = true;
    }

    public Player getOwner() {
        return owner;
    }

    public Utility.Pair getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString(){
    	return "Settlement from " + owner + "(id: " + owner.getId() + "): " + coordinates;
    }

}
