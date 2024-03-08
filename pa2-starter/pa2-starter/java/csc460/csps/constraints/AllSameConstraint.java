package csc460.csps.constraints;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A constraint that requires all of the given variables to have the same value.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class AllSameConstraint<DomainType> extends Constraint<DomainType> {

    /**
     * Creates a new AllSameConstraint instance.
     * @param variables The names of the variables to use as operands.
     */
    public AllSameConstraint(ArrayList<String> variables){
        super("AllSame", variables);
    }

    /**
     * Checks if all of the operands that have assignments have the same value.
     * 
     * @param variableIndexLookup A map of variable names to their list index.
     * @param assignments The current set of variable assignments.
     * 
     * @return True if all of the given variables have the same value.
     */
    @Override
    public boolean isSatisfied(HashMap<String, Integer> variableIndexLookup, ArrayList<DomainType> assignments){
        DomainType targetValue = null;
        for(String variable : variables){
            DomainType value = assignments.get(variableIndexLookup.get(variable));
            if(value != null){
                if(targetValue == null){
                    targetValue = value;
                } else if(!targetValue.equals(value)){
                    return false;
                }
            }
        }
        return true;
    }
}