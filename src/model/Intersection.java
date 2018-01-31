package model;

public class Intersection {

    private Settlement settlement;
    private Harbor harbor;
    private Player owner;
    private Utility.Pair coordinates;

    public Intersection(int x, int y){
    	this.settlement = null;
    	this.harbor = null;
    	this.owner = null;
    	this.coordinates = new Utility.Pair(x, y);
    }
    
    public Intersection(Utility.Pair p){
    	this(p.getX(), p.getY());
    }
    
    public Harbor getHarbor() {
        return harbor;
    }

    public void setHarbor(Harbor harbor) {
        this.harbor = harbor;
    }

    public Settlement getSettlement() {
        return settlement;
    }

    public void setSettlement(Settlement settlement) {
        this.settlement = settlement;
        this.owner = settlement.getOwner();
        owner.addSettlement(settlement);
    }
    
    public Player getOwner(){
    	return this.owner;
    }
    
    protected void setOwner(Player ow){
    	this.owner = ow;
    }
    
    public Utility.Pair getCoordinates(){
    	return this.coordinates;
    }
    
}
