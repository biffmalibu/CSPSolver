package csc460;

/**
 * Represents a generic search states.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public abstract class SearchState implements Comparable<SearchState>{
    public abstract BoardCoordinate getAgentCoordinates();
    public abstract double getCost();
    public abstract double getDistance();
    public abstract String getAction();
    public abstract String toString();
    public boolean equals(Object other){
        return this.toString().equals(other.toString());
    }
    public int compareTo(SearchState other){
        return this.toString().compareTo(other.toString());
    }
    public int hashCode(){
        return this.toString().hashCode();
    }
}