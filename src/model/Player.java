package model;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Player {

    private Resource[] resources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};
    private ArrayList<String> devCards = new ArrayList<>();
	private int victoryPoints = 0;
    private int id;
	private Color color;
	private String name;
	private String state;
    private ArrayList<Settlement> settlements = new ArrayList<>();
    private ArrayList<Street> streets = new ArrayList<>();
    private ArrayList<Harbor> harbors = new ArrayList<>();
    private int resourcesTotal = 0;
    private int knightForce = 0;
    private boolean hasBiggestKnightForce = false;
    private boolean hasLongestRoad = false;
    private int victoryPointsForOthers = 0;

	public Player(String n, int id, Color color){
		this.name = n;
		this.color = color;
        this.id = id;
	}

	public void changeResourceQuantity(String type, int quantity) {
        for (Resource resource : resources) {
            if (resource.getType().equals(type)) {
                resource.setValue(resource.getValue() + quantity);
            }
        }
    }

    public Resource stealRandomResource(){
        ArrayList<Resource> currentResources = new ArrayList<>();
        for (Resource randomResource : resources) {
            if (randomResource.getValue() > 0){
                currentResources.add(randomResource);
            }
        }

        if (currentResources.size() != 0){
            int randomNumber = ThreadLocalRandom.current().nextInt(0,currentResources.size());
            return currentResources.get(randomNumber);
        } else {
            return null;
        }
    }
	
	public Resource getResource(String type){
		for (Resource rtype : resources){
			if(type.equals(rtype.getType())){
				return rtype;
			}
		}
		return null;
	}

	public Resource[] getResources(){return this.resources;}

	public int getVictoryPoints(){
		return this.victoryPoints;
	}

	public void increaseVictoryPoints() {
        this.victoryPoints++;
        this.victoryPointsForOthers++;
    }

    public void decreaseVictoryPoints() {
        this.victoryPoints--;
        this.victoryPointsForOthers--;
    }

    public void adjustVictoryPointsForOthers() {
        for (String devCard : devCards) {
            if (devCard.equals("Siegpunkt")) {
                victoryPointsForOthers--;
            }
        }
    }

    public boolean hasWon() {
        return victoryPoints >= 10;
    }

    // Adds an Street to the Street[] array streets
    void addStreet(Street street){
        streets.add(street);
    }
    
    public ArrayList<Street> getStreets(){
    	return this.streets;
    }

    // Adds a Settlement to the Settlement[] array
    void addSettlement(Settlement settlement){
        settlements.add(settlement);
    }
    
    public ArrayList<Settlement> getSettlements(){
    	return this.settlements;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getResourcesTotal() {
        return resourcesTotal;
    }

    public void changeResourcesTotal(int amount) {
        resourcesTotal += amount;
    }

    public void increaseKnightForce() {
        knightForce++;
    }

    public int getKnightForce() {
        return knightForce;
    }

    public void addToDevCards(String card) {
        devCards.add(card);
    }

    public ArrayList<String> getDevCards() {
        return devCards;
    }

    public int getQuantityOfDevCard(String card) {
        int knight = 0;
        int monopoly = 0;
        int roadBuilding = 0;
        int yearOfPlenty = 0;
        int victoryPoint = 0;

        for (String devCard : devCards) {
            switch (devCard) {
                case "Ritter":
                    knight++;
                    break;
                case "Monopol":
                    monopoly++;
                    break;
                case "Straßenbau":
                    roadBuilding++;
                    break;
                case "Erfindung":
                    yearOfPlenty++;
                    break;
                case "Siegpunkt":
                    victoryPoint++;
                    break;
            }
        }

        switch (card) {
            case "Ritter":
                return knight;
            case "Monopol":
                return monopoly;
            case "Straßenbau":
                return roadBuilding;
            case "Erfindung":
                return yearOfPlenty;
            case "Siegpunkt":
                return victoryPoint;
            default:
                return 0;
        }
    }

    public int getQuantityOfAResource(String resource) {
        int toReturn = -1;
        switch (resource) {
            case "Holz":
                toReturn = resources[0].getValue();
                break;
            case "Lehm":
                toReturn = resources[1].getValue();
                break;
            case "Wolle":
                toReturn = resources[2].getValue();
                break;
            case "Getreide":
                toReturn = resources[3].getValue();
                break;
            case "Erz":
                toReturn = resources[4].getValue();
                break;
        }
        return toReturn;
    }

    public boolean hasBiggestKnightForce() {
        return hasBiggestKnightForce;
    }

    public boolean hasLongestRoad() {
        return hasLongestRoad;
    }

    public ArrayList<Harbor> getHarbors(){
        return harbors;
    }

    void addHarbor(Harbor harbor){
        harbors.add(harbor);
    }

    public int getNumberOfDevCards() {
        return devCards.size();
    }

    public int getNumberOfResourceCards() {
        int result = 0;
        for (Resource resource : resources) {
            result += resource.getValue();
        }
        return result;
    }

    public void removeDevCard(String card) {
        for (String devCard : devCards) {
            if (devCard.equals(card)) {
                devCards.remove(devCard);
                break;
            }
        }
    }

    public void setHasBiggestKnightForce(boolean hasBiggestKnightForce) {
        this.hasBiggestKnightForce = hasBiggestKnightForce;
    }

    private String getColorAsString(){
        if(Color.RED.equals(this.color)){
            return "Rot";
        }else if(Color.WHITE.equals(this.color)){
            return "Weiß";
        } else if(Color.ORANGE.equals(this.color)){
            return "Orange";
        } else {
            return "Blau";
        }
    }

    @Override
    public String toString(){
        return name;
    }

    public String toExtendedString(){return name + ": Farbe: " + getColorAsString() + " - ID: " + id;}

    public String getResourcesString(){
        return resources[0] + "| " + resources[1] + "| " + resources[2] + "| " + resources[3] + "| " + resources[4];
    }

    public void setHasLongestRoad(boolean hasLongestRoad) {
        this.hasLongestRoad = hasLongestRoad;
    }

    public int getVictoryPointsForOthers() {
        return victoryPointsForOthers;
    }

    public void setVictoryPointsForOthers(int points) {
        this.victoryPointsForOthers = points;
    }

    public boolean checkIfPlayerHasHarbor(ResourceType resource) {
        for (Harbor harbor : harbors) {
            if (harbor.getResource() == null && resource == null) {
                return true;
            } else if (harbor.getResource() != null && harbor.getResource().equals(resource)) {
                return true;
            }
        }
        return false;
    }

    public int resourcesNeededForCity(){
        if(resources[4].getValue() >= 3){
            if(resources[3].getValue() >= 2){
                return 0;
            }
            return 2 - resources[3].getValue();
        } else if(resources[3].getValue() >= 2){
            return 3 - resources[4].getValue();
        }
        return 5 - resources[3].getValue() + resources[4].getValue();
    }

    public int resourcesNeededForSettlement(){

        int neededResources = 0;

        for(Resource resource : resources){
            if(resource.getType() != "ore" && resource.getValue() < 1){
                neededResources++;
            }
        }

        return neededResources;
    }

}
