package model;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Board {

    private Landpiece[][] landpieces = new Landpiece[5][5];
    private ArrayList<Street> streets = new ArrayList<>();
    private Intersection[][] intersections = new Intersection[12][6];
    private Harbor[] harbors = new Harbor[9];
    private boolean beginnerMode;

	// Konstruktor für Server
    public Board(boolean beginnerMode){
		this.beginnerMode = beginnerMode;
        initialiseLandpieces();
		initialiseIntersections();
        initialiseHarbors();
    }

    // Konstruktor für Client
    public Board(Landpiece[][] landpieces, Utility.Pair[][] coordinatesOfHarbors, ResourceType[] resources) {
		this.landpieces = landpieces;
		initialiseIntersections();

		for (int i = 0; i < coordinatesOfHarbors[0].length; i++) {
			Harbor harbor = new Harbor();
			harbor.setResource(resources[i]);
			intersections[coordinatesOfHarbors[0][i].getX()][coordinatesOfHarbors[0][i].getY()].setHarbor(harbor);
			intersections[coordinatesOfHarbors[1][i].getX()][coordinatesOfHarbors[1][i].getY()].setHarbor(harbor);
		}
	}

    // Initialisation of the Landpiece array. Has to give values to the Landpieces in the array.
    // Initialise the dice number and the LandpieceType
    //@author Sandra
    private void initialiseLandpieces(){
    	
	LandpieceType[] lt = new LandpieceType[]{LandpieceType.FOREST, LandpieceType.FIELDS, LandpieceType.FOREST,
											 LandpieceType.FIELDS, LandpieceType.HILLS, LandpieceType.PASTURES,
											 LandpieceType.HILLS, LandpieceType.PASTURES, LandpieceType.FIELDS,
											 LandpieceType.FOREST, LandpieceType.PASTURES, LandpieceType.PASTURES,
											 LandpieceType.MOUNTAINS, LandpieceType.HILLS, LandpieceType.MOUNTAINS,
											 LandpieceType.FOREST, LandpieceType.MOUNTAINS, LandpieceType.FIELDS,
											 LandpieceType.DESERT};    	
    	
    	if(!beginnerMode){
    		Utility.shuffleLandArray(lt);
    	}
    	
    	byte[] coordinates = new byte[]{0,0,  0,1,  0,2,  1,3,  2,4,  3,4,  4,4,  
    									4,3,  4,2,  3,1,  2,0,  1,0,  1,1,  1,2,
    									2,3,  3,3,  3,2,  2,1,  2,2};
    	
    	if(!beginnerMode){
	    	int startField = (int) ((6 * new Random().nextDouble())+1);	
	   	
	    	if(startField == 2){
	    		coordinates = new byte[]{0,2,  1,3,  2,4,  3,4,  4,4,  4,3,  4,2,  
	    								 3,1,  2,0,  1,0,  0,0,  0,1,  1,2,  2,3,
	    								 3,3,  3,2,  2,1,  1,1,  2,2};
	    		
	    	}else if(startField == 3){
	    		coordinates = new byte[]{2,4,  3,4,  4,4,  4,3,  4,2,  3,1,  2,0,  
						 				 1,0,  0,0,  0,1,  0,2,  1,3,  2,3,  3,3,
						 				 3,2,  2,1,  1,1,  1,2,  2,2};
	    	}else if(startField ==4){
	    		coordinates = new byte[]{4,4,  4,3,  4,2,  3,1,  2,0,  1,0,  0,0,  
		 				 				 0,1,  0,2,  1,3,  2,4,  3,4,  3,3,  3,2,
		 				 				 2,1,  1,1,  1,2,  2,3,  2,2};
	    	}else if(startField ==5){
	    		coordinates = new byte[]{4,2,  3,1,  2,0,  1,0,  0,0,  0,1,  0,2,  
	    								 1,3,  2,4,  3,4,  4,4,  4,3,  3,2,  2,1,
	    								 1,1,  1,2,  2,3,  3,3,  2,2};
	    	}else if(startField ==6){
	    		coordinates = new byte[]{2,0,  1,0,  0,0,  0,1,  0,2,  1,3,  2,4,  
						 				 3,4,  4,4,  4,3,  4,2,  3,1,  2,1,  1,1,
						 				 1,2,  2,3,  3,3,  3,2,  2,2};
	    	}
    	}
    	   	
    	byte[] bt = new byte[]{5,2,6,3,8,10,9,12,11,4,8,10,9,4,5,6,3,11};   
    	if(beginnerMode){
    		bt = new byte[]{6,2,5,10,8,4,11,12,9,10,8,3,4,9,11,3,6,5};
    	}
    	byte count = 0;  
    	byte cb = 0;
   	
		for(byte y = 0; y < 37; y += 2){
				if(lt[count] == LandpieceType.DESERT){
					landpieces[coordinates[y]][coordinates[y+1]] = new Landpiece((byte) 0, lt[count], new Utility.Pair((int)coordinates[y], (int)coordinates[y+1]));
					landpieces[coordinates[y]][coordinates[y+1]].setHoldingRobber(true);
				}else{
					landpieces[coordinates[y]][coordinates[y+1]] = new Landpiece(bt[cb], lt[count], new Utility.Pair((int)coordinates[y], (int)coordinates[y+1]));
					cb++;
				}
				count++;	
		}			
    }

    // Initialisation of the Intersection array.
    private void initialiseIntersections() {
    	for(byte y = 0; y < 6; y++){
    		for(byte x = 0; x < 12; x++){
    			if(!(((x > 6) && y == 0) || ((x > 8) && y == 1) || ((x > 10) && y == 2) || (x==0 && y ==3) || (x < 3 && y==4) || (x < 5 && y==5))){
    				intersections[x][y] = new Intersection(x,y);
    			}
    		}
    	}
	}
    // initialises the Harbor array and gives certain Intersections a harbor
	//@author Thore
    private void initialiseHarbors(){

        ResourceType[] resources = ResourceType.values();
        int resourceCounter = 0;
        for(int i = 0; i < harbors.length; i++){
            harbors[i] = new Harbor();
            if((i%2) == 0){
                harbors[i].setResource(resources[resourceCounter]);
                resourceCounter++;
            }
        }
        Utility.shuffleHarborArray(harbors);

    	Utility.Pair[] coordinates = Utility.coast;
    	int harborCounter = 0;
    	for(int i = 0; i < coordinates.length; i++) {
            switch (i) {
                case 0:case 3:case 7:case 10:case 13:case 17:case 20:case 23:case 27:
                    intersections[coordinates[i].getX()][coordinates[i].getY()].setHarbor(harbors[harborCounter]);
                    intersections[coordinates[i+1].getX()][coordinates[i+1].getY()].setHarbor(harbors[harborCounter]);
					Utility.Pair[] coordinatesOfHarbor = new Utility.Pair[2];
					coordinatesOfHarbor[0] = new Utility.Pair(coordinates[i].getX(), coordinates[i].getY());
					coordinatesOfHarbor[1] = new Utility.Pair(coordinates[i+1].getX(), coordinates[i+1].getY());
					harbors[harborCounter].setCoordinates(coordinatesOfHarbor);
                    harborCounter++;
                    break;
            }
        }
    }

    // für Client:
	private void setHarbor(Utility.Pair int1, Utility.Pair int2, Harbor harbor) {
		intersections[int1.getX()][int1.getY()].setHarbor(harbor);
		intersections[int2.getX()][int2.getY()].setHarbor(harbor);
	}

	/**
	 * creates a street, defined by two Intersections
	 * @param a
	 * @param b
	 * @param owner
	 */
    public void addStreet(Intersection a, Intersection b, Player owner){
    	Street street = new Street(a, b, owner);
    	streets.add(street);
    	owner.addStreet(street);
    }
    public void addStreet(Utility.Pair p1, Utility.Pair p2, Player owner){
    	addStreet(intersections[p1.getX()][p1.getY()], intersections[p2.getX()][p2.getY()], owner);
    }

    // creates a settlement on an intersection
    public void addSettlement(Utility.Pair coordinates, Player owner){
    	Settlement settlement = new Settlement(owner, coordinates);
        intersections[coordinates.getX()][coordinates.getY()].setSettlement(settlement);
        if(intersections[coordinates.getX()][coordinates.getY()].getHarbor() != null){
			intersections[coordinates.getX()][coordinates.getY()].getHarbor().setOwner(owner);
			owner.addHarbor(intersections[coordinates.getX()][coordinates.getY()].getHarbor());
		}
    }

	/**
	 * Update settlement to a city
	 * @param coordinate coordinate of the settlement
	 * @param player player information
	 */
    public void addCity(Utility.Pair coordinate, Player player) {
		for (Settlement se : player.getSettlements()) {
			if (se.getCoordinates().equals(coordinate)) {
				se.setCityTrue();
			}
		}
	}

	/**
	 * calculates all resources a player gets for his settlements/cities for a specific dice throw
	 * first gets all the settlements, than all adjacent landpieces, if no robber on the landpiece and the dice number is correct,
	 * it adds 2 resources for a city and 1 for a settlement
	 * @param player a player object to get the settlements/cities from
	 * @param diceNumber the number on the landpiece
	 * @return an Array of the 5 resource types
	 * from: Sandra, edited: Kevin
	 */
	public Resource[] getResourcesForDiceThrow(Player player, int diceNumber){
    	Resource[] returnResources = new Resource[]{new Resource("lumber"), new Resource("brick"), new Resource("wool"), new Resource("grain"), new Resource("ore")};

		for(Settlement settlement : player.getSettlements()){
    		for(Landpiece adjacentLandpiece : getNeighbouringLandpieces(settlement.getCoordinates())){
    			if(adjacentLandpiece != null && !adjacentLandpiece.isHoldingRobber() && (adjacentLandpiece.getDiceNumber() == diceNumber)){
					int x;
					if(settlement.isCity()){
						x = 2;
					}else {
						x = 1;
					}

						for(Resource rt : returnResources){
						if(rt.getType().equals(Utility.translateLandpiecetype(adjacentLandpiece.getResourceType()).getType())){
							rt.setValue(rt.getValue() + x);
						}
					}
				}
			}
		}
		return returnResources;

	}

    public void setBeginner(){
    	this.beginnerMode = true;
    }

    public Landpiece[][] getLandpieces(){
    	return this.landpieces;
    }
    
    public ArrayList<Street> getStreets(){
    	return this.streets;
    }
    
    public Intersection[][] getIntersections(){
    	return this.intersections;
    }
    
    public Harbor[] getHarbors(){
    	return this.harbors;
    }


    /**
     * Author: Thore
     * If given the ArrayList of Street ArrayLists of a Player, it returns the longest Road of that Player.
     * @param streetNets
     * @return
     */
    public int calculateLongestPlayerRoad(ArrayList<ArrayList<Street>> streetNets, Player owner){

        int longestPlayerRoad = 0;
        ArrayList<Street> streetNetTmp = new ArrayList<>();
        ArrayList<Street> visited = new ArrayList<>();

        for(ArrayList<Street> streetNet : streetNets){
            int height = 0;
            boolean pureCircular = true;
            for(Street street : streetNet){
                streetNetTmp.add(street);
            }
            for(Street street : streetNetTmp){
                if(!hasNeighbouringStreet(street, street.getA(), streetNetTmp) || !hasNeighbouringStreet(street, street.getB(), streetNetTmp)){
                    int heightTmp = calculateLongestRoadOfNet(street, street, streetNetTmp, visited, owner);
                    height = Math.max(height, heightTmp);
                    pureCircular = false;
                }
            }
            if(pureCircular){
                height = streetNet.size();
            }
            longestPlayerRoad = Math.max(longestPlayerRoad, height);
        }
        return longestPlayerRoad;
    }


    /**
     * Author: Thore
     * Calculates the longest road of an ArrayList of connected Streets
     * @param street
     * @param lastStreet
     * @param streetNet
     * @param visited
     * @return
     */
    private int calculateLongestRoadOfNet(Street street, Street lastStreet, ArrayList<Street> streetNet, ArrayList<Street> visited, Player owner){

        if(street == null){
            return 0;
        }

        streetNet.forEach(s -> s.setOwner(owner));
        street.setOwner(owner);
        lastStreet.setOwner(owner);

        if(street.hasInterceptingSettlement(lastStreet)){
            System.out.println("Owner of Streets : " + street.getOwner() + " || Owner of Settlement: " + street.sharedIntersection(lastStreet).getSettlement().getOwner());
            return 0;
        }

        for(Street s : visited){
            if(s.equals(street)){
                System.out.println("Contained :" + street);
                return 0;
            }
        }
        visited.add(street);
        System.out.println("Added: " + street);

        Intersection direction;
        if(street.equals(lastStreet)){
            if(hasNeighbouringStreet(street, street.getA(), streetNet)){
                direction = street.getA();
            } else {
                direction = street.getB();
            }
        } else if(street.sharedIntersection(lastStreet) == street.getA()){
            direction = street.getB();
        } else {
            direction = street.getA();
        }

        return (1 + Math.max(calculateLongestRoadOfNet(neighbouringStreets(street, streetNet, direction).get(0), street, streetNet, visited, owner),
                             calculateLongestRoadOfNet(neighbouringStreets(street, streetNet, direction).get(1), street, streetNet, visited, owner)));
    }

    /**
     * Author: Thore
     * Returns Streets connected to a Street, in the direction of one of the Street-Intersections
     * @param street
     * @param streetNet
     * @param intersection
     * @return
     */
    private ArrayList<Street> neighbouringStreets(Street street, ArrayList<Street> streetNet, Intersection intersection){
        ArrayList<Street> neighbouringStreets = new ArrayList<>();

        for(Intersection neighbouringIntersection : getNeighbouringIntersections(intersection)){
            Street neighbouringStreet = new Street(intersection, neighbouringIntersection);
            if(!street.equals(neighbouringStreet) && containsStreet(neighbouringStreet, streetNet)){
                neighbouringStreets.add(neighbouringStreet);
				//System.out.println("Added Street : " + neighbouringStreet.toString());
			} else if(!containsStreet(neighbouringStreet, streetNet)) {
                neighbouringStreets.add(null);
            }
        }
        if(neighbouringStreets.size() == 1 && neighbouringStreets.get(0) == null){
            neighbouringStreets.add(null);
        } else if(neighbouringStreets.size() == 1){
            //System.out.println("First Street : " + neighbouringStreets.get(0).toString());
            neighbouringStreets.add(null);
        }
        return neighbouringStreets;
    }

    /**
     * Help-Method for neighbouringStreets(), returns true, if an ArrayList of Streets contains a given Street
     * @param street
     * @param streets
     * @return
     */
    private boolean containsStreet(Street street, ArrayList<Street> streets){
        boolean contains = false;
        for (Street st : streets){
            if (st.equals(street)){
                contains = true;
            }
        }
        return contains;
    }

    /**
     * Help-Method, returns true, if a Street has connected Streets in the direction of the direction Intersection
     * @param street
     * @param direction
     * @param streetNet
     * @return
     */
    public boolean hasNeighbouringStreet(Street street, Intersection direction, ArrayList<Street> streetNet){
        ArrayList<Street> neighbouringStreets = neighbouringStreets(street, streetNet, direction);
        neighbouringStreets.removeIf(Objects::isNull);
        if(neighbouringStreets.isEmpty()){
            return false;
        }
        return true;
    }

    /**
     * Creates an ArrayList of Street ArrayLists, every ArrayList of Streets contains connected Streets.
     * Author: Thore
     * @param playerStreets
     * @return
     */
    public ArrayList<ArrayList<Street>> getAllConnectedStreets(ArrayList<Street> playerStreets, ArrayList<ArrayList<Street>> connectedStreets){

        if(playerStreets.isEmpty()){
            return connectedStreets;
        }

        ArrayList<Street> connectedStreetsTmp = new ArrayList<>();
        connectedStreetsTmp.add(playerStreets.get(0));
        ArrayList<Street> cs = getConnectedStreets(playerStreets, connectedStreetsTmp);
		connectedStreets.add(cs);
		playerStreets.removeAll(cs);

        return getAllConnectedStreets(playerStreets, connectedStreets);
    }

    /**
     * Author: Thore
     * Help method for getAllConnectedStreets, returns all Streets connected to a single given Street
     * @param playerStreets
     * @param connectedStreets
     * @return
     */
    public ArrayList<Street> getConnectedStreets(ArrayList<Street> playerStreets, ArrayList<Street> connectedStreets){
        ArrayList<Street> connectedStreetsTmp = new ArrayList<>(connectedStreets);

        for(Street s : connectedStreets){
            for(Street st : playerStreets){
                if(st.adjacent(s) && !connectedStreetsTmp.contains(st) && !st.hasInterceptingSettlement(s)) {
                        connectedStreetsTmp.add(st);
                }
            }
        }

        if(connectedStreets.equals(connectedStreetsTmp)){
            return connectedStreets;
        }

        return getConnectedStreets(playerStreets, connectedStreetsTmp);
    }

    /**
     * Returns an ArrayList of Streets which could be build by the player in the first two rounds
     * @param owner player object to calculate the streets for
     * @return ArrayList of Streets
     */
    public ArrayList<Street> getViableStreetsInitial(Player owner){
    	ArrayList<Street> newStreets = new ArrayList<>();
    	Settlement lastSettle = owner.getSettlements().get(owner.getSettlements().size()-1);
    	ArrayList<Intersection> adjacentIntersections = getNeighbouringIntersections(intersections[lastSettle.getCoordinates().getX()][lastSettle.getCoordinates().getY()]);
    	for (Intersection intersection : adjacentIntersections){
    		newStreets.add(new Street(intersections[lastSettle.getCoordinates().getX()][lastSettle.getCoordinates().getY()], intersection));
    	}
    	
    	return newStreets;
    }

    /**
     * Returns all settlements with a building on it
     * @return
     */
    public ArrayList<Intersection> getIntersectionsWithBuilding(){
        System.out.println(" Started getting Intersections with buildings.");
        ArrayList<Intersection> developedIntersections = new ArrayList<>();
		for(Intersection[] intersectionRow : intersections){
    		for(Intersection intersection : intersectionRow){
    			if(intersection != null && intersection.getOwner() != null){
    				developedIntersections.add(intersection);
				}
			}
		}
        System.out.println("Intersections with buildings number : " + developedIntersections.size());
        return developedIntersections;
	}

    /**
     * Returns an ArrayList of Streets which could be build by the player
     * @param playerStreets needs the aktive Owner
     * @return ArrayList of Streets
     * from: Sandra
     */
	public ArrayList<Street> getViableStreets(ArrayList<Street> playerStreets){
		ArrayList<Street> newStreets = new ArrayList<>();
		ArrayList<Intersection> possibleIntersections = new ArrayList<>();

		for (Street st : playerStreets){
			if(!possibleIntersections.contains(st.getA())){
				possibleIntersections.add(st.getA());
			}
			if(!possibleIntersections.contains(st.getB())){
				possibleIntersections.add(st.getB());
			}
		}

		for (Intersection possibleIntersection : possibleIntersections){
			ArrayList<Intersection> adjacentIntersections = getNeighbouringIntersections(possibleIntersection);

			for (Intersection adjacentIntersection : adjacentIntersections){
				Street compare = new Street(possibleIntersection, adjacentIntersection);

				boolean contains = false;
				for (Street st : streets){
					if ((st.getA().getCoordinates().equals(adjacentIntersection.getCoordinates()) && st.getB().getCoordinates().equals(possibleIntersection.getCoordinates())
							|| st.getB().getCoordinates().equals(adjacentIntersection.getCoordinates()) && st.getA().getCoordinates().equals(possibleIntersection.getCoordinates()))
							){
						contains = true;
					}
				}
				if(!contains){
					newStreets.add(compare);
				}
			}
		}

		return newStreets;
	}


	/**
	 * Returns an ArrayList of Intersections on which the player could build a Settlement in the first two rounds
	 * @return ArrayList of Intersections on which a settlement can be build
	 * from: Thore Schillmann, edited: Sandra
	 */
	public ArrayList<Intersection> getViableSettlementsInitial(){
		ArrayList<Intersection> viable = new ArrayList<>();

		for(Intersection[] inArr : intersections){
			for(Intersection in : inArr){
				if(in != null){
					viable.add(in);
				}
			}
		}

		for(Intersection in : new ArrayList<>(viable)){
			if(in.getSettlement() != null){
				for(Intersection neighbouringIntersection : getNeighbouringIntersections(in)){
					if(viable.contains(neighbouringIntersection)){
						viable.remove(neighbouringIntersection);
					}
				}
				viable.remove(in);
			}
		}

		return viable;
	}


	/**
	 * Returns an ArrayList of Intersections on which the player could build a Settlement
	 * @param playerStreets the current streets of a player
	 * @return ArrayList of Intersections
	 * from: Thore Schillmann
	 */

	public ArrayList<Intersection> getViableSettlements(ArrayList<Street> playerStreets) {
		ArrayList<Intersection> possibleIntersections = new ArrayList<>();

		for (Street st : playerStreets) {
			if (!possibleIntersections.contains(st.getA()) && st.getA().getSettlement() == null && noNeighbours(st.getA())) {
				possibleIntersections.add(st.getA());
			}
			if (!possibleIntersections.contains(st.getB()) && st.getB().getSettlement() == null && noNeighbours(st.getB())) {
				possibleIntersections.add(st.getB());
			}
		}
		return possibleIntersections;
	}


	/**
	 * Calculates the neighbouring Intersections of a Landpiece
	 * @param landpiece the landpiece to get the adjacent Intersections from
	 * @return array of 6 references of the intersections around the landpiece
	 * from: Thore
	 */
    public Intersection[] getNeighbouringIntersections(Landpiece landpiece){

    	Intersection[] intersections = new Intersection[6];
		for (int i = 0; i < landpieces.length; i++){
			for (int j = 0; j < landpieces[0].length; j++){
				if(landpiece == landpieces[i][j]){
					intersections[0] = this.intersections[i*2][j];
					intersections[1] = this.intersections[i*2+1][j];
					intersections[2] = this.intersections[i*2+2][j];
					intersections[3] = this.intersections[i*2+3][j+1];
					intersections[4] = this.intersections[i*2+2][j+1];
					intersections[5] = this.intersections[i*2+1][j+1];
				}
			}
		}
		return intersections;
	}

	/**
	 * Calculates the neighbouring Intersections of a Landpiece
	 * @param pair of the landpiece to get the adjacent Intersections from
	 * @return array of 6 references of the intersections around the landpiece
     * from: Thore, Sandra
     */
    public Intersection[] getNeighbouringIntersections(Utility.Pair pair){

    	Intersection[] intersections = new Intersection[6];
					intersections[0] = this.intersections[pair.getX() * 2][pair.getY()];
					intersections[1] = this.intersections[pair.getX() * 2 + 1][pair.getY()];
					intersections[2] = this.intersections[pair.getX() * 2 + 2][pair.getY()];
					intersections[3] = this.intersections[pair.getX() * 2 + 3][pair.getY() + 1];
					intersections[4] = this.intersections[pair.getX() * 2 + 2][pair.getY() + 1];
					intersections[5] = this.intersections[pair.getX() * 2 + 1][pair.getY() + 1];

		return intersections;
	}

	/**
	 * Calculates the neighbouring Intersections of an Intersection
	 * from: Thore Schillmann
	 * @param intersection the intersection to calculate the neighbours from
	 * @return ArrayList of 2 or 3 intersections adjacent to the input intersection
 	 */
	public ArrayList<Intersection> getNeighbouringIntersections(Intersection intersection){

        ArrayList<Intersection> intersections = new ArrayList<>();

        if(intersection.getCoordinates().getX() % 2 == 0){
				intersections.add(getIntersectionQuietly(intersection.getCoordinates().getX() - 1, intersection.getCoordinates().getY()));
				intersections.add(getIntersectionQuietly(intersection.getCoordinates().getX() + 1, intersection.getCoordinates().getY()));
				intersections.add(getIntersectionQuietly(intersection.getCoordinates().getX() + 1, intersection.getCoordinates().getY() + 1));
        } else {
				intersections.add(getIntersectionQuietly(intersection.getCoordinates().getX() - 1, intersection.getCoordinates().getY() - 1));
				intersections.add(getIntersectionQuietly(intersection.getCoordinates().getX() - 1, intersection.getCoordinates().getY()));
				intersections.add(getIntersectionQuietly(intersection.getCoordinates().getX() + 1, intersection.getCoordinates().getY()));
        }
        intersections.removeIf(Objects::isNull);
        return intersections;
    }

	/**
	 * helper method to catch NullPointer and ArrayIndexOutOfBounds Exceptions
	 * @param x x coordinate of the intersection
	 * @param y y coordinate of the intersection
	 * @return intersection reference if in the array, else null
	 */
    private Intersection getIntersectionQuietly(int x, int y){
        try {
            return this.intersections[x][y];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e){
            return null;
        }
	}


	/**
	 * Returns an ArrayList of Neighbouring Landpieces of an Intersection
	 * @param intersection the intersection element you want the surrounding ones from
	 * @return ArrayList of Landpieces
	 */
	public ArrayList<Landpiece> getNeighbouringLandpieces(Intersection intersection){

		ArrayList<Landpiece> landpieces = new ArrayList<>();

		if(intersection.getCoordinates().getX() % 2 == 0){
            landpieces.add(getLandpieceQuietly((intersection.getCoordinates().getX()/2) - 1, intersection.getCoordinates().getY() - 1));
            landpieces.add(getLandpieceQuietly((intersection.getCoordinates().getX()/2) - 1, intersection.getCoordinates().getY()));
            landpieces.add(getLandpieceQuietly(intersection.getCoordinates().getX()/2, intersection.getCoordinates().getY()));
		} else {
            landpieces.add(getLandpieceQuietly((intersection.getCoordinates().getX()/2) - 1, intersection.getCoordinates().getY() - 1));
            landpieces.add(getLandpieceQuietly(intersection.getCoordinates().getX()/2, intersection.getCoordinates().getY() - 1));
            landpieces.add(getLandpieceQuietly(intersection.getCoordinates().getX()/2, intersection.getCoordinates().getY()));
		}
		return landpieces;
	}

	/**
	 * Returns an ArrayList of Neighbouring Landpieces of an Intersection
	 * @param coordinate the coordinates of the intersection element you want the surrounding ones from
	 * @return an ArrayList of max. 3 Landpieces
	 */
	public ArrayList<Landpiece> getNeighbouringLandpieces(Utility.Pair coordinate){

		ArrayList<Landpiece> landpieces = new ArrayList<>();

		if(coordinate.getX() % 2 == 0){
			landpieces.add(getLandpieceQuietly((coordinate.getX()/2) - 1, coordinate.getY() - 1));
			landpieces.add(getLandpieceQuietly((coordinate.getX()/2) - 1, coordinate.getY()));
			landpieces.add(getLandpieceQuietly(coordinate.getX()/2, coordinate.getY()));
		} else {
			landpieces.add(getLandpieceQuietly((coordinate.getX()/2) - 1, coordinate.getY() - 1));
			landpieces.add(getLandpieceQuietly(coordinate.getX()/2, coordinate.getY() - 1));
			landpieces.add(getLandpieceQuietly(coordinate.getX()/2, coordinate.getY()));
		}
		return landpieces;
	}
	/**
	 * helper method to catch NullPointer and ArrayIndexOutOfBounds Exceptions
	 * @param x x coordinate of the Landpiece
	 * @param y y coordinate of the Landpiece
	 * @return Landpiece reference if in the array, else null
	 */
    private Landpiece getLandpieceQuietly(int x, int y){
        try {
            return this.landpieces[x][y];
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e){
            return null;
        }
	}

	/**
	 * helper method to check if an intersection has no neighbours, due to the distance rule
	 * @param intersection intersection to check for
	 * @return true if no neighbour and false if blocked
	 */
	private Boolean noNeighbours(Intersection intersection){
    	boolean holdsNoSettlement = true;
		for(Intersection in : getNeighbouringIntersections(intersection)){
			if(in.getSettlement() != null ){
				holdsNoSettlement = false;
			}
		}
		return holdsNoSettlement;
	}

	/**
	 * looks fore the landpiece with the robber on it
	 * @return the coordinates of the landpiece
	 */
	public Utility.Pair getLandpieceWithActiveRobberCoordinates(){
		Utility.Pair landpieceWithRobber = new Utility.Pair(0,0);
		for (int i = 0; i < landpieces.length ; i++) {
			for (int j = 0; j <landpieces[0].length ; j++) {
				if(landpieces[i][j] != null && landpieces[i][j].isHoldingRobber()){
					landpieceWithRobber = new Utility.Pair(i,j);
				}
			}

		}
		return landpieceWithRobber;
	}

	/**
	 * looks fore the landpiece with the robber on it
	 * @return the landpiece
	 */
	public Landpiece getLandpieceWithActiveRobber(){
		for (int i = 0; i < landpieces.length ; i++) {
			for (int j = 0; j <landpieces[0].length ; j++) {
				if(landpieces[i][j] != null && landpieces[i][j].isHoldingRobber()){
					return landpieces[i][j];
				}
			}

		}
		return null;
	}

	/**
	 * looks for the current position of the robber and changes it to the new position
	 * @param newPosition coordinates to place the robber on
	 */
	public void changeRobber(Utility.Pair newPosition){
		System.out.println("changed robber from: " + getLandpieceWithActiveRobberCoordinates());
		landpieces[getLandpieceWithActiveRobberCoordinates().getX()][getLandpieceWithActiveRobberCoordinates().getY()].setHoldingRobber(false);
		landpieces[newPosition.getX()][newPosition.getY()].setHoldingRobber(true);
		System.out.println("to: " + newPosition);
	}
}
