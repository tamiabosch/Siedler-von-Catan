package model;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utility {



	static public Pair[] coast = new Pair[]{
	        new Pair(0,0),new Pair(1,0),new Pair(2,0),new Pair(3,0),new Pair(4,0),
            new Pair(5,0),new Pair(6,0),new Pair(7,1),new Pair(8,1),new Pair(9,2),
            new Pair(10,2),new Pair(11,3),new Pair(10,3),new Pair(11,4),new Pair(10,4),
            new Pair(11,5),new Pair(10,5),new Pair(9,5),new Pair(8,5),new Pair(7,5),
            new Pair(6,5),new Pair(5,5),new Pair(4,4),new Pair(3,4),new Pair(2,3),
            new Pair(1,3),new Pair(0,2),new Pair(1,2),new Pair(0,1),new Pair(1,1)
	};


	public static class Pair{
	    private final int x;
        private final int y;

        public Pair(int x, int y){
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean equals(Pair pair){
        	if(pair.getX() == this.x && pair.getY() == this.y){ return true; }
        	else { return false; }
		}
        
        public boolean equals(int[] intArr){
        	if(this.getX() == intArr[0] && this.getY() == intArr[1]){
        		return true;
        	}
        	return false;
        }
        
        public boolean equals(int x, int y){
        	if(this.getX() == y && this.getY() == y){
        		return true;
        	}
        	return false; 
        }
        
        public String toString(){
        	String s = "x: " + x + " | y: " + y;
        	return s;
        }
    }
	
	static void shuffleLandArray(LandpieceType[] ar)
	  {
	    Random rnd = ThreadLocalRandom.current();
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      LandpieceType a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }
	

	static void shuffleHarborArray(Harbor[] ar)
	{
		Random rnd = ThreadLocalRandom.current();
		for (int i = ar.length - 1; i > 0; i--)
		{
			int index = rnd.nextInt(i + 1);
			// Simple swap
			Harbor a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}

	static public int getDiceNumber(){		
		return (int) ((6 * new Random().nextDouble())+1);
	}

	static Resource translateLandpiecetype(LandpieceType lt){
		if(lt == LandpieceType.FIELDS){
			return new Resource("grain");
		}else if (lt == LandpieceType.FOREST){
			return new Resource("lumber");
		}else if (lt == LandpieceType.HILLS){
			return new Resource("brick");
		}else if (lt == LandpieceType.MOUNTAINS){
			return new Resource("ore");
		}else if (lt == LandpieceType.PASTURES){
			return new Resource("wool");
		}

		return null;
	}

	
	
}
