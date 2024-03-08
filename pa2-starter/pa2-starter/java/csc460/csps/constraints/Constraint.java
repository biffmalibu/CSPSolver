package csc460.csps.constraints;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * An abstract class representing a generic CSP constraint.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public abstract class Constraint<DomainType> {
    public String name;
    public ArrayList<String> variables;

    /**
     * Creates a new CSP constraint.
     * 
     * @param name The name of the constraint (usually the operatotr, e.g., "AllDiff").
     * @param variables A list of variables to use as operands.
     */
    public Constraint(String name, ArrayList<String> variables){
        this.variables = variables;
        this.name = name;
    }

    /** 
     * This should check all of the operands that have assignments and
     * return true if the constraint is satisfied for those assignments.
     *
     * @return True if the constraint is satisfied for the assigned
     * variables (unassigned variables, which should a null value in the
     * assignments list, are ignored). 
     */
    public abstract boolean isSatisfied(HashMap<String, Integer> variableIndexLookup, ArrayList<DomainType> assignments);

}