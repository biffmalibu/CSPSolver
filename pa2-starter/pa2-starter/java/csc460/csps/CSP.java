package csc460.csps;

import java.util.ArrayList;
import java.util.Collections;
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
 * This class was modified to include the use of forward checking, least constraining value, and minimum remaining values. 
 * The methods getSuccessors, selectNextVariableIndex, orderValues, and forwardCheck were modified to include these features. 
 * 
 * Generative AI: Some methods in this class were modified with the use of generative AI. A section in the orderValues method
 * was produced by ChatGPT to help me correctly sort the orderedValues list. forwardCheck was in large part produced by
 * Github Copilot, and further modifications were made by me to ensure that the method was correctly implemented.
 * 
 * @author Hank Feild
 * @author Bradford Torpey
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
                if(useForwardChecking){ // Forward checking
                    successorState = forwardCheck(successorState, variableIndex, value); // Update successor state with forward checking
                    boolean hasEmptyDomain = false; 
                    for(DomainType domainValue : successorState.getDomains().get(variableIndex)){ // Check if any domain is empty
                        if(domainValue == null){ // If domain is empty, break and continue to next successor
                            hasEmptyDomain = true;
                            break;
                        }
                    }
                    if(hasEmptyDomain){ // If domain is empty, continue to next successor
                        continue;
                    }
                }

                successors.add(successorState); // Add successor to list of successors
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
        if (useMinimumRemainingValues) { // Minimum remaining values
            int minRemainingValues = Integer.MAX_VALUE; // Initialize minRemainingValues to maximum value (2^31 - 1)
            int minRemainingValuesIndex = -1; // Initialize minRemainingValuesIndex to -1
            ArrayList<ArrayList<DomainType>> domains = state.getDomains(); // Get the domains from state
            for (int i = 0; i < domains.size(); i++) { // For each domain
                ArrayList<DomainType> domain = domains.get(i);
                if (state.getAssignments().get(i) == null && domain.size() < minRemainingValues) { // If the variable is unassigned and the domain size is less than minRemainingValues
                    minRemainingValues = domain.size(); // Update minRemainingValues
                    minRemainingValuesIndex = i; 
                }
            }
            return minRemainingValuesIndex; // Return the index of the variable with the minimum remaining values
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
        if(useLeastConstrainingValue){ // Least constraining value
            ArrayList<DomainType> domain = state.getDomains().get(variableIndex); // Get the domain of the variable at the given index
            ArrayList<Integer> removalCounts = new ArrayList<>(); // Initialize removalCounts to store # of values eliminated from domains of other unassigned variables
            for (DomainType value : domain) { // For each value in the domain
                int count = 0; // Reset count to 0
                for (int i = 0; i < state.getDomains().size(); i++) { 
                    if (i != variableIndex) { // If the index is not the variable index
                        CSPState newState = forwardCheck(state, variableIndex, value); // Get the new state with the value assigned to the variable at the given index
                        if (newState.getDomains().get(i).size() < state.getDomains().get(i).size()) { // If the domain size is less than the original domain size
                            count++; // Increment count
                        }
                    }
                }
                removalCounts.add(count); // Add count to removalCounts
            }
            // This section of code below was in part produced by ChatGPT to help me correctly sort the orderedValues list 
            // I had initially been unsure of how to use Collections.sort() and ChatGPT was able to help me understand how to use it correctly
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ArrayList<DomainType> orderedValues = new ArrayList<>(domain); // Initialize orderedValues to a copy of the domain
            orderedValues.sort((v1, v2) -> { // Sort orderedValues by the # of values eliminated from domains of other unassigned variables
                int index1 = domain.indexOf(v1); 
                int index2 = domain.indexOf(v2);
                return Integer.compare(removalCounts.get(index1), removalCounts.get(index2)); // Compare the # of values eliminated from the domains of other unassigned variables
            });
            return orderedValues; // Return orderedValues
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    public CSPState forwardCheck(CSPState state, int variableIndex, DomainType value) {
        // Deep copy.
        CSPState newState = state.clone();
        newState.getAssignments().set(variableIndex, value);

        // Convenience references
        ArrayList<DomainType> assignments = newState.getAssignments();
        ArrayList<ArrayList<DomainType>> domains = newState.getDomains();

        // This section of code below was produced by Github Copilot, and further modifications were made by me to ensure that the method was correctly implemented
        // I was unsure of how to correctly implement forward checking on some of the constraint types, and Github Copilot was able to help me understand how to implement it
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        for (Constraint constraint : constraints) { // Iterate through constraints
            ArrayList<Integer> variableIndices = new ArrayList<>(); // Initialize variableIndices 
            for (Object variable : constraint.variables) { // For each variable in the constraint
                String variableString = (String) variable;
                Integer index = variableIndexLookup.get(variableString); // Get the index of the variable
                if (index != null) { // If the index is not null
                    variableIndices.add(index); // Add the index to variableIndices
                }
            }

            if (variableIndices.contains(variableIndex)) { // If variableIndices contains the variable index
                if (constraint instanceof AllDiffConstraint) { // AllDiffConstraint
                    for (int index : variableIndices) { // For each index in variableIndices
                        if (index != variableIndex) { // If the index is not the variable index
                            ArrayList<DomainType> domain = domains.get(index); 
                            domain.remove(value); // Remove the value from the domain
                        }
                    }
                } else if (constraint instanceof AllSameConstraint) { // AllSameConstraint
                    for (int index : variableIndices) { // For each index in variableIndices
                        if (index != variableIndex) { // If the index is not the variable index
                            ArrayList<DomainType> domain = domains.get(index);
                            domain.retainAll(Collections.singletonList(value)); // Retain only the value in the domain
                        }
                    }
                } else if (constraint instanceof MaxCountNConstraint) { // MaxCountNConstraint
                    MaxCountNConstraint maxCountConstraint = (MaxCountNConstraint) constraint; // Cast constraint to MaxCountNConstraint
                    int maxCount = maxCountConstraint.getMaxCount(); // Get the max count
                    int count = 0; // Initialize count to 0
                    for (int index : variableIndices) { // For each index in variableIndices
                        if (index != variableIndex) { // If the index is not the variable index
                            ArrayList<DomainType> domain = domains.get(index);
                            if (domain.contains(value)) { // If the domain contains the value
                                count++;
                                if (count > maxCount) { // If the count is greater than the max count
                                    domain.remove(value); // Remove the value from the domain
                                }
                            }
                        }
                    }
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
