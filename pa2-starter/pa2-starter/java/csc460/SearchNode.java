package csc460;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A wrapper for a search state, the path of actions that led to that state,
 * the coordinates of each spot along that path, and the cost of that path.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class SearchNode implements Comparable<SearchNode> {
    public SearchState state;
    public ArrayList<String> pathActions;
    public HashSet<SearchState> pathStates;
    public ArrayList<BoardCoordinate> pathCoords;
    public double cost;
    public double priority;
    public int id;

    /**
     * @param state The search state.
     * @param pathActions The path of actions that led the agent to the state.
     * @param pathStates The set of states on the path that led to this state.
     * @param pathCoords The coordinates of each path that led to the state.
     * @param cost The cost of the path that led to the state.
     */
    public SearchNode(SearchState state, ArrayList<String> pathActions, 
            HashSet<SearchState> pathStates,
            ArrayList<BoardCoordinate> pathCoords, double cost){
        this(state, pathActions, pathStates, pathCoords, cost, 0);
    }

    /**
     * @param state The search state.
     * @param pathActions The path of actions that led the agent to the state.
     * @param pathStates The set of states on the path that led to this state.
     * @param pathCoords The coordinates of each path that led to the state.
     * @param cost The cost of the path that led to the state.
     */
    public SearchNode(SearchState state, ArrayList<String> pathActions,
            HashSet<SearchState> pathStates, 
            ArrayList<BoardCoordinate> pathCoords, double cost, double priority){
        this.state = state;
        this.pathActions = pathActions;
        this.pathStates = pathStates;
        this.pathCoords = pathCoords;
        this.cost = cost;
        this.priority = priority;
        this.id = 0;
    }

    public SearchNode(SearchState state, ArrayList<String> pathActions,
            HashSet<SearchState> pathStates, 
            ArrayList<BoardCoordinate> pathCoords, double cost, double priority, int id){
        this.state = state;
        this.pathActions = pathActions;
        this.pathStates = pathStates;
        this.pathCoords = pathCoords;
        this.cost = cost;
        this.priority = priority;
        this.id = id;
    }

    /**
     * Compares two SearchNodes based on their priority.
     */
    @Override
    public int compareTo(SearchNode other) {
        // return Double.valueOf(priority).compareTo(
        //     Double.valueOf(other.priority));
        if(this.priority < other.priority){
            return -1;
        } else if(this.priority == other.priority){
            if(this.id < other.id){
                return -1;
            } else if(this.id > other.id) {
                return 1;
            }
            return 0;
        } else {
            return 1;
        }
    }


}