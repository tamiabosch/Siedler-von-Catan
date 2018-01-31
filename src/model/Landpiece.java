package model;

public class Landpiece {
	
	private byte diceNumber;
	private boolean holdingRobber;
	private LandpieceType landpieceType;
    private Utility.Pair coordinates;
	
	public Landpiece(byte diceN, LandpieceType lt, Utility.Pair coordinates){
		this.diceNumber = diceN;
		this.landpieceType = lt;
		this.coordinates = coordinates;
	}

    public Utility.Pair getCoordinates() {
        return coordinates;
    }

    public int getDiceNumber(){
		return this.diceNumber;
	}
	
	public void setHoldingRobber(boolean hr){
		this.holdingRobber = hr;
	}
	
	public boolean isHoldingRobber(){
		return this.holdingRobber;
	}
		
	public LandpieceType getResourceType(){
		return this.landpieceType;
	}

}
