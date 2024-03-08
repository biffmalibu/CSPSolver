package csc460.csps;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import csc460.Board;
import csc460.BoardCoordinate;
import csc460.SearchState;
import csc460.csps.constraints.AllDiffConstraint;
import csc460.csps.constraints.AllSameConstraint;
import csc460.csps.constraints.MaxCountNConstraint;

/**
 * Solves a generic CSP. This will parse and solve a CSP represented in a file
 * the includes the variables (which may be partially assigned), string domain values,
 * and constraints. See the loadFile method for more information about the format.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class GenericCSP extends CSP<String> {

    public GenericCSP(boolean useForwardChecking, boolean useLeastConstrainingValue, boolean useMinimumRemainingValues){
        super(useForwardChecking, useLeastConstrainingValue, useMinimumRemainingValues);
    }

    /**
     * Parses a CSP file. It should have three or more lines:
     *     * a space separated list of variable names and their assignments (if any)
     *         - assignments should be in the form of: x=value, where x is the
     *           variable name
     *     * a space separated list of domain values (treated as strings)
     *     * 1 or more lines of constraints. Each row should be one of the following:
     *          AllDiff var1 var2 var3 ... -- all of the given variables must have different values
     *          AllSame var1 var2 var3 ... -- all of the given variables must have the same value
     *          MaxCount n var1 var2 var3 ... -- no more than n of the given variables can have the same value
     * 
     * For example:
     * 
     *     VT ME=blue NH MA RI CT
     *     blue red green
     *     AllDiff NH ME
     *     AllDiff VT NH MA
     *     AllDiff CT RI MA
     * 
     * This would give us a CSP with five variables, one of which already has an
     * assignment (ME). There are three values in the domain: blue, red, and
     * green. NH should have a different value from ME, MA, and VT. MA should
     * also have a different value from VT, CT, and RI. CT and RI should have
     * different values. There are no "same value" constraints or "max count"
     * constraints.
     * 
     * @param filename The name of the CSP file to load.
     */
    public Board loadBoardFile(String filename) throws FileNotFoundException {
        Scanner reader = new Scanner(new File(filename));
        ArrayList<ArrayList<String>> domains = new ArrayList<ArrayList<String>>();
        ArrayList<String> initialAssignments = new ArrayList<String>();
        variables = new ArrayList<String>();
        domain = new ArrayList<String>();
        variableIndexLookup = new HashMap<String, Integer>();


        // Parse variables and their initial assignments.
        for(String variable : reader.nextLine().split(" ")){
            variable = variable.trim();
            String value = null;
            int equalsIndex = variable.indexOf("=");
            if(equalsIndex >= 0){
                if(equalsIndex == variable.length()-1){
                    value = "";
                } else {
                    value = variable.substring(equalsIndex+1);
                }
                variable = variable.substring(0, equalsIndex);
            }
            variables.add(variable);
            initialAssignments.add(value);
            variableIndexLookup.put(variable, variables.size()-1);
        }

        // Parse domain values.
        //self.domain = [x.strip() for x in fd.readline().split(',')]
        for(String domainValue : reader.nextLine().split(" ")){
            domain.add(domainValue.trim());
        }

        for(int i = 0; i < variables.size(); i++){
            domains.add(new ArrayList<String>(domain));
        }

        // Parse constraints.
        String constraintLine[];
        while(reader.hasNextLine()){
            constraintLine = reader.nextLine().trim().split(" ");
            String constraintType = constraintLine[0];
            ArrayList<String> vars = new ArrayList<String>();

            // Read the variables list -- this is a little different from MaxCount compared to the others.
            int startingIndex = 1;
            if(constraintType.equals("MaxCount")){
                startingIndex = 2;
            }
            for(int i = startingIndex; i < constraintLine.length; i++){
                vars.add(constraintLine[i]);
            }

            if(constraintType.equals("AllDiff")){
                constraints.add(new AllDiffConstraint<String>(vars));
            } else if(constraintType.equals("AllSame")){
                constraints.add(new AllSameConstraint<String>(vars));
            } else if(constraintType.equals("MaxCount")){
                int n = Integer.parseInt(constraintLine[1]);
                
                constraints.add(new MaxCountNConstraint<String>(vars, n));
            }
 
        }

        reader.close();

        // Initialize the start state.
        startState = new CSPState(initialAssignments, domains);

        return null;
    }

    /**
     * Describes the file format expected for a generic CSP.
     * 
     * @return A string describing the expected file format.
     */
    public static String getFileFormatDescription() {
        return "The file for the GenericCSP should have three lines:\n"+
               "    * a space separated list of variable names and their assignments (if any)\n"+
               "    * a comma separated list of domain values\n"+
               "    * 1 or more rows of constraints. Each row should be one of the following:\n"+
               "        AllDiff var1 var2 var3 ... -- all of the given variables must have different values\n"+
               "        AllSame var1 var2 var3 ... -- all of the given variables must have the same value\n"+
               "        MaxCount n var1 var2 var3 ... -- no more than n of the given variables can have the same value\n";
    }
}