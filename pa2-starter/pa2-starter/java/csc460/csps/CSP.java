package csc460.csps;

import java.util.ArrayList;
import java.util.HashMap;
import csc460.csps.constraints.*;
import csc460.SearchState;
import csc460.searchproblems.SearchProblem;
import csc460.BoardCoordinate;

/**
 * Represents a constraint satisfaction problem (CSP). DomainType should be the type of the
 * values in the domain, e.g., for Sudoku, you might use Integer for this, while for a sheduling
 * problem, you would probably use a String representing the name of the entity being scheduled. 
 * 
 * @author Hank Feild
 */
public abstract class CSP<DomainType> implements SearchProblem {
    protected SearchState startState;
    protected ArrayList<String> variables;
    protected ArrayList<DomainType> domain;
    protected ArrayList<Constraint> constraints;
    protected HashMap<String, Integer> variableIndexLookup;
    protected boolean useForwardChecking, useLeastConstrainingValue, useMinimumRemainingValues;


    /**
     * Represents a CSP state, which is a list of assignments and domains that
     * correspond to the variable ordering (see the outer CSP class). The value
     * of unassigned variables is null.
     */
    public class CSPState extends SearchState {
        private ArrayList<DomainType> assignments;
        private ArrayList<ArrayList<DomainType>> domains;

        /**
         * Initializes the state.
         * 
         */
        public CSPState(ArrayList<DomainType> assignments, ArrayList<ArrayList<DomainType>> domains){
            this.assignments = assignments;
            this.domains = domains;
        }

        /**
         * @return The current set of variable assignments.
         */
        public ArrayList<DomainType> getAssignments(){
            return assignments;
        }

        /**
         * @return The current set of variable domains.
         */
        public ArrayList<ArrayList<DomainType>> getDomains(){
            return domains;
        }

        /**
         * @return A deep copy of this state.
         */
        public CSPState clone(){
            ArrayList<DomainType> clonedAssignments = new ArrayList<DomainType>(assignments);
            ArrayList<ArrayList<DomainType>> clonedDomains = new ArrayList<ArrayList<DomainType>>();
            for(ArrayList<DomainType> domain : domains){
                clonedDomains.add(new ArrayList<DomainType>(domain));
            }
            return new CSPState(clonedAssignments, clonedDomains);
        }

        /**
         * Unused.
         */
        @Override
        public BoardCoordinate getAgentCoordinates() {
            return null;
        }

        /**
         * Unused.
         */
        @Override
        public double getCost() {
            return 0;
        }

        /**
         * Unused.
         */
        @Override
        public double getDistance() {
            return 0;
        }

        /**
         * Unused.
         */
        @Override
        public String getAction() {
            return null;
        }

        /**
         * @returns The assignments as a string.
         */
        public String toString(){
            return assignments.toString();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes an empty CSP.
     */
    public CSP(){
        this(false, false, false);
    }

    /**
     * Initializes an empty CSP.
     * 
     * @param useForwardChecking Set to true to use forward checking on successors.
     * @param useLeastConstrainingValue Set to true to use the least constraining value for domain value selection.
     * @param useMinimumRemainingValues Set to true to use minimum remaining values for variable selection.
     */
    public CSP(boolean useForwardChecking, boolean useLeastConstrainingValue, boolean useMinimumRemainingValues){
        this.variables = new ArrayList<String>();
        this.domain = new ArrayList<DomainType>();
        this.constraints = new ArrayList<Constraint>();
        this.variableIndexLookup = new HashMap<String, Integer>();
        this.useForwardChecking = useForwardChecking;
        this.useLeastConstrainingValue = useLeastConstrainingValue;
        this.useMinimumRemainingValues = useMinimumRemainingValues;
    }

    /**
     * Initializes the CSP. If there are initial assignments, get the starting
     * state after the constructor has been called and add the assignments
     * there.
     *
     * @param variables A list of variable names.
     * @param domain A list of domain values; all variables are initially given the same domain.
     * @param constraints A list of constraints that define the problem.
     * @param useForwardChecking Set to true to use forward checking on successors.
     * @param useLeastConstrainingValue Set to true to use the least constraining value for domain value selection.
     * @param useMinimumRemainingValues Set to true to use minimum remaining values for variable selection.
     */
    public CSP(ArrayList<String> variables, ArrayList<DomainType> domain, ArrayList<Constraint> constraints,
               boolean useForwardChecking, boolean useLeastConstrainingValue, boolean useMinimumRemainingValues){
        this.variables = variables;
        this.domain = domain;
        this.constraints = constraints;
        this.variableIndexLookup = new HashMap<String, Integer>();
        this.useForwardChecking = useForwardChecking;
        this.useLeastConstrainingValue = useLeastConstrainingValue;
        this.useMinimumRemainingValues = useMinimumRemainingValues;
        ArrayList<ArrayList<DomainType>> domains = new ArrayList<ArrayList<DomainType>>();
        ArrayList<DomainType> initialAssignments = new ArrayList<DomainType>();

        // Set each variables list index, domain, and initial assignment (null).
        for(int i = 0; i < variables.size(); i++){
            variableIndexLookup.put(variables.get(i), i);
            domains.add(new ArrayList<DomainType>(domain));
            initialAssignments.add(null);
        }

        startState = new CSPState(initialAssignments, domains);
    }

    /**
     * Implements backtracking; only possible assignment to the next variable
     * are emitted, and those that are must be consistent with previous
     * assignments.
     * 
     * @param state The state to find successors of.
     * @return A collection of successor states.
     */
    @Override
    public Iterable<SearchState> getSuccessors(SearchState state) {
        CSPState currentState = (CSPState) state;
        ArrayList<SearchState> successors = new ArrayList<SearchState>();

        ArrayList<DomainType> assignments = currentState.getAssignments();
        // If there are no missing assignments, there are no successors, so 
        // stop here.
        if(assignments.indexOf(null) < 0){
            return successors;
        }

        // Get the index of the next empty assignment.
        // int i = assignments.indexOf(null);
        int variableIndex = selectNextVariableIndex(currentState);

        // Add any consistent assignment to that variable to the list of
        // successors.
        for(DomainType value : orderValues(currentState, variableIndex)){
            CSPState successorState = currentState.clone();
            successorState.getAssignments().set(variableIndex, value);
            if(constraintsSatisfied(successorState.getAssignments())){

                if(useForwardChecking){
                    successorState = forwardCheck(successorState, variableIndex, value);
                    // Skip this successor if any domain is empty.
                    // TODO: implement this check.
                }

                successors.add(successorState);
            }
        }
        return successors;
    }

    /**
     * Selects the next value to assign to the variable at the given index. If useMinimumRemainingValues
     * is set to true for this CSP, the value with the fewest remaining values in the domain is selected.
     * Otherwise, the first unassigned variable is returned.
     * 
     * @param state The current state of the CSP.
     * 
     * @return The index of the next variable to assign a value to.
     */
    public int selectNextVariableIndex(CSPState state){
        if(useMinimumRemainingValues){
            // TODO: implement minimum remaining values ordering; return the variable with the fewest remaining values in its domain.
            return state.getAssignments().indexOf(null); // Replace this line!
        }
        return state.getAssignments().indexOf(null);
    }

    /**
     * Returns an ordering of the values in the domain of the specified variable. If useLeastConstrainingValue
     * is set to true, the values are ordered by the number of values that would be eliminated from the domains
     * of other unassigned variables if the value were assigned to the variable at the given index. Otherwise, 
     * the initial ordering of the domain is returned.
     * 
     * @param state The current state of the CSP.
     * @param variableIndex The index of the variable to order the values of.
     * @return An ordered list of the values in the domain of the variable at the given index.
     */
    public ArrayList<DomainType> orderValues(CSPState state, int variableIndex){
        if(useLeastConstrainingValue){
            // TODO: implement least constraining value ordering; return the values in the domain of
            // the given variable, ordered by how many values they remove from the remaining variables' domains.
            // You will find the forwardCheck() method helpful here.
            return state.getDomains().get(variableIndex); // Replace this line!

        }
        return state.getDomains().get(variableIndex);
    }

    /**
     * Returns a new state with the given variable assigned the given value, and the domains of the other variables updated.
     *
     * @param state The current state of the CSP.
     * @param variableIndex The index of the variable to assign the value to.
     * @param value The value to assign to the variable.
     * 
     * @return A new state with the given variable assigned the given value, and the domains of the other variables updated to
     *        be consistent with the new assignment.
     */
    public CSPState forwardCheck(CSPState state, int variableIndex, DomainType value){
        // Deep copy.
        CSPState newState = state.clone();
        newState.getAssignments().set(variableIndex, value);

        // Convienience references so we don't have to write:
        // newState.getAssignments().get(i) and newState.getDomains().get(i)
        // over and over...
        ArrayList<DomainType> assignments = newState.getAssignments();
        ArrayList<ArrayList<DomainType>> domains = newState.getDomains();

        // TODO: check each of the constraints, skipping those that don't involve
        // the variable being assigned. Handle each constraint type separately
        // (AllDiff, AllSame, MaxCount). For example, if the constraint is
        // AllDiff, remove the value assigned to the new variable from the
        // domains of the unassigned variables involved in the constraint.

        return newState;
    }

    /**
     * 
     * @param assignments
     * @return
     */
    public boolean constraintsSatisfied(ArrayList<DomainType> assignments){
        for(Constraint constraint : constraints){
            if(!constraint.isSatisfied(variableIndexLookup, assignments)){
                return false;
            }
        }
        return true;
    }

    /**
     * @param state The state to test.
     * @return True if the agent has reached the exit.
     */
    @Override
    public boolean isGoal(SearchState state) {
        ArrayList<DomainType> assignments = ((CSPState) state).getAssignments();
        return assignments.indexOf(null) < 0 && constraintsSatisfied(assignments);
    }

    /**
     * @return A string representation of variables and their assignments, as
     *         specified by the given search state. The format is:
     *         \t[variable]\t[value]
     */
    public String getAssignmentsAsString(SearchState state){
        ArrayList<DomainType> assignments = ((CSPState) state).getAssignments();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < variables.size(); i++){
            sb.append('\t').append(variables.get(i))
              .append('\t').append(assignments.get(i))
              .append('\n');
        }

        return sb.toString();
    }

    /**
     * Unused.
     */
    @Override
    public void setHeuristic(String heuristic) {
    }

    /**
     * @return The starting state (the initial set of assignments).
     */
    @Override
    public SearchState getStartState() {
        return startState;
    }
    
    /**
     * This should be overridden.
     * @return A description of the format of the file that this class can read.
     *      This should be written so that it appears nicely on the command line.
     */
    public static String getFileFormatDescription(){
        return "This method should be overridden in the subclass; replace this message with a helpful CLI description.";
    }
}
