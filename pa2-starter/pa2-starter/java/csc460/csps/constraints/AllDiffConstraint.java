package csc460.csps.constraints;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A constraint that requires all of the given variables to have different values.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class AllDiffConstraint<DomainType> extends Constraint<DomainType> {

    /**
     * Creates a new AllDiffConstraint instance.
     * 
     * @param variables The names of the variables to use as operands.
     */
    public AllDiffConstraint(ArrayList<String> variables){
        super("AllDiff", variables);
    }

    /**
     * Checks if all of the operands that have assignments have different values.
     * 
     * @param variableIndexLookup A map of variable names to their list index.
     * @param assignments The current set of variable assignments.
     * 
     * @return True if all of the given variables have different values.
     */
    @Override
    public boolean isSatisfied(HashMap<String, Integer> variableIndexLookup, ArrayList<DomainType> assignments){
        ArrayList<DomainType> values = new ArrayList<DomainType>();
        for(String variable : variables){
            DomainType value = assignments.get(variableIndexLookup.get(variable));
            if(value != null){
                if(values.contains(value)){
                    return false;
                }
                values.add(value);
            }
        }
        return true;
    }

    public ArrayList<Integer> getVariableIndices() {
        HashMap<String, Integer> variableIndexLookup = new HashMap<>();
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for(String variable : variables){
            indices.add(variableIndexLookup.get(variable));
        }
        return indices;
    }
    

    public boolean getInvolvesVariable(int variableIndex) {
        return getVariableIndices().contains(variableIndex);
    }

}