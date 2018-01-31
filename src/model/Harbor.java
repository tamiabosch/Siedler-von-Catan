package model;

public class Harbor {

    private Player owner;

    private ResourceType resource;

    private Utility.Pair[] coordinates;

    public Harbor(){
        resource = null;
    }

    public void setCoordinates(Utility.Pair[] coordinates) {
        this.coordinates = coordinates;
    }

    public Utility.Pair[] getCoordinates() {
        return coordinates;
    }
    public void setResource(ResourceType resource){
        this.resource = resource;
    }
    
    public ResourceType getResource(){
    	return this.resource;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}

    