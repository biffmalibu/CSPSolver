package csc460.searchalgorithms;

import csc460.searchproblems.SearchProblem;
import csc460.BoardCoordinate;
import csc460.SearchNode;
import csc460.SearchState;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Breadth First Search: states are explored based on their distance from the
 * starting state (closer states expanded earlier). Cost is not considered.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class BFS implements SearchAlgorithm {
    SearchProblem problem;
    LinkedList<SearchNode> fringe;
    int numStatesExpanded;
    int maxFringeSize;

    /**
     * Initializes the fringe so it only holds the starting state. Initiazes the 
     * seen set and all the stats.
     * 
     * @param problem The problem to solve.
     */
    public void init(SearchProblem problem){
        SearchNode root = new SearchNode(
            problem.getStartState(), 
            new ArrayList<String>(),
            new HashSet<SearchState>(), 
            new ArrayList<BoardCoordinate>(), 
            0.0);
        root.pathStates.add(problem.getStartState());
        fringe= new LinkedList<SearchNode>();
        fringe.add(root);
        this.problem = problem;
        numStatesExpanded = 0;
        maxFringeSize = 1;
    }

    /**
     * Finds the next node to expand. Previusly expanded nodes are ignored.
     * Adds unexplored successor states to the fringe.
     * 
     * @return The next node to expand. Null if there are no more nodes left 
     *         to explore.
     */
    public SearchNode nextNode(){
        if(fringe.isEmpty()){
            return null;
        }

        SearchNode node = fringe.poll();

        expandNode(node);

        // Update stats.
        numStatesExpanded++;
        maxFringeSize = Math.max(maxFringeSize, fringe.size());

        return node;
    }

    /**
     * Adds the unexplored successor states of the given node's state to the 
     * fringe.
     * 
     * @param node The state whose successors should be added to the fringe.
     */
    public void expandNode(SearchNode node){
        for(SearchState successor : problem.getSuccessors(node.state)){
            // Avoid cycles.
            if(node.pathStates.contains(successor)){
                continue;
            }

            // Make a deep copy of the path actions of the parent node, then add
            // the action that led to the successor.
            ArrayList<String> pathActions = 
                new ArrayList<String>(node.pathActions);
            pathActions.add(successor.getAction());

            // Make a deep copy of the search states.
            HashSet<SearchState> pathStates = 
                new HashSet<SearchState>(node.pathStates);
            pathStates.add(successor);

            ArrayList<BoardCoordinate> pathCoords = 
                new ArrayList<BoardCoordinate>(node.pathCoords);
            pathCoords.add(successor.getAgentCoordinates());

            fringe.add(new SearchNode(
                successor,
                pathActions,
                pathStates,
                pathCoords,
                node.cost + successor.getCost()
            ));
        }
    }

    /**
     * @return The current number of states expanded.
     */
    @Override
    public int getNumStatesExpanded() {
        return numStatesExpanded;
    }

    /**
     * @return The maximum size of the fringe so far.
     */
    @Override
    public int getMaxFringeSize() {
        return maxFringeSize;
    }
}