package model;

public class Street {

    private Player owner;
    private Intersection a;
    private Intersection b;

    public Street(Intersection a, Intersection b, Player owner) {
        this.a = a;
        this.b = b;
        this.owner = owner;
    }
    
    public Street(Intersection a, Intersection b){
        this.a = a;
        this.b = b;
    }

    public boolean equals(Street street){
        if((street.getA() == this.a && street.getB() == this.b) || (street.getA() == this.b && street.getB() == this.a)) { return true; }
        else { return false; }
    }

    @Override
    public String toString(){
        return "Street from A: " + a.getCoordinates() + " || to B: " + b.getCoordinates();
    }

    public boolean adjacent(Street street){
        if(street.getA() == this.a || street.getB() == this.b || street.getA() == this.b || street.getB() == this.a && !street.equals(this)) { return true; }
        else { return false; }
    }

    public boolean hasInterceptingSettlement(Street street){
        if(this.sharedIntersection(street) != null && this.sharedIntersection(street).getSettlement() != null && sharedIntersection(street).getSettlement().getOwner() != owner){
                return true;
        }
        return false;
    }

    public Intersection sharedIntersection(Street street) {
        if(street.equals(this) || !street.adjacent(this)){
            return null;
        } else if(street.getA() == this.a){
            return this.a;
        } else if(street.getB() == this.b){
            return this.b;
        } else if(street.getA() == this.b){
            return this.b;
        } else if(street.getB() == this.a){
            return this.a;
        }
        return null;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public Intersection getA() {
        return a;
    }

    public Intersection getB() {
        return b;
    }

    public void setA(Intersection a) {
        this.a = a;
    }

    public void setB(Intersection b) {
        this.b = b;
    }
}



