package model;

/**
 * contains all possible types of resources
 */
public enum ResourceType {
    WOOL, LUMBER, GRAIN, ORE, BRICK;
    
    private int quantity;
    
    ResourceType(){
    	this.quantity = 0;
    }

    ResourceType(int initialiseQuantity){
        this.quantity = initialiseQuantity;
    }
    
    public void changeQuantity(int i) {
        this.quantity += i;
    }

    public void setQuantity(int bt){
    	this.quantity = bt;
    }
    
    public int getQuantity(){
    	return this.quantity;
    }
    
}
