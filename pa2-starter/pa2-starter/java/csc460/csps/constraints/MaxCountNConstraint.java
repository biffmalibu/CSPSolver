package csc460.csps.constraints;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A constraint that requires that no more than n of the given variables have the same value.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class MaxCountNConstraint<DomainType> extends Constraint<DomainType> {
    public int n;

    /**
     * Creates a new MaxCountNConstraint.
     * @param variables The names of the variables to use as operands.
     * @param n The maximum number of variables that can share the same value.
     */
    public MaxCountNConstraint(ArrayList<String> variables, int n){
        super("MaxCountN", variables);
        this.n = n;
    }

    /**
     * Checks if no more than n of the operands that have assignments have the same value.
     * 
     * @param variableIndexLookup A map of variable names to their list index.
     * @param assignments The current set of variable assignments.
     * 
     * @return True if no more than n of the given variables have the same value.
     */
    @Override
    public boolean isSatisfied(HashMap<String, Integer> variableIndexLookup, ArrayList<DomainType> assignments){
        HashMap<DomainType, Integer> counts = new HashMap<DomainType, Integer>();
        for(String variable : variables){
            DomainType value = assignments.get(variableIndexLookup.get(variable));
            if(value != null){
                if(counts.containsKey(value)){
                    if(counts.get(value) == n){
                        return false;
                    }
                    counts.put(value, counts.get(value) + 1);
                } else {
                    counts.put(value, 1);
                }
            }
        }
        return true;
    }
    public int getMaxCount(){
        return n;
    }
}