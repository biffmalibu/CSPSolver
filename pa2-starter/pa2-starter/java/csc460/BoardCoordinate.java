package csc460;

/**
 * Stores an (x,y) coordinate.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class BoardCoordinate implements Comparable<BoardCoordinate> {
    public int x, y;

    /**
     * Initializes the coordinates.
     * 
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public BoardCoordinate(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Compares two coordinates.
     * 
     * @param other Another BoardCoordinate.
     * 
     * @return -1 if this instance is less than the other coordiante, 0 if they are the same, 
     *          and otherwise.
     */
    public int compareTo(BoardCoordinate other){
        if(this.x < other.x || this.x == other.x && this.y < other.y){
            return -1;
        } else if(this.x == other.x && this.y == other.y){
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Checks if two coordinates are the same.
     * 
     * @param other Another BoardCoordinate.
     * @return True if the x and y coordinates of this and other are the same.
     */
    public boolean equals(Object other){
        BoardCoordinate o = (BoardCoordinate) other;
        return other != null && o.x == x && o.y == y;
    }

    /**
     * @return The coordinate in the form: (x,y).
     */
    public String toString(){
        return "("+ x +","+ y +")";
    }
}