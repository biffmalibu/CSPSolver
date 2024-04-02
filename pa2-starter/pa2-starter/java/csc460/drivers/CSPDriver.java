package csc460.drivers;

import csc460.Board;
import csc460.BoardCoordinate;
import csc460.SearchNode;
import csc460.SearchState;
import csc460.searchalgorithms.*;
import csc460.csps.CSP;
import csc460.csps.GenericCSP;

import java.io.FileNotFoundException;

/**
 * A driver for generic constraint satisfaction problems.
 * 
 * @author Hank Feild (hfeild@endicott.edu)
 */
public class CSPDriver {

    /**
     * Attempts to solve the CSP in the given file. The final assignment is
     * displayed to stdout.
     * 
     * @param csp The CSP to solve.
     * @param cspFile A CSP file containing a line of variables and optional 
     *                initial assignments, a line of domain values, a line of
     *                "different values" constraints, and a line of 
     *                "same value" constraints. See `CSP.loadFile` for details
     *                and examples.
     */
    public static <DomainType> boolean run(CSP<DomainType> cspProblem, String cspFile) throws FileNotFoundException {
        SearchAlgorithm algorithm = new DFS();
        cspProblem.loadBoardFile(cspFile);
        algorithm.init(cspProblem);

        SearchNode searchNode = algorithm.nextNode();
        while(searchNode != null){

            // Check if we found a solution.
            if(cspProblem.isGoal(searchNode.state)){
                System.out.print(
                    "\nSearch algorithm: "+ algorithm.getClass().getCanonicalName() +
                    "\nStates expanded: "+ algorithm.getNumStatesExpanded() +
                    "\nMax fringe size: "+ algorithm.getMaxFringeSize() +
                    "\nSolution path length: "+ searchNode.pathActions.size() +
                    "\nSolution path (actions):\n"+
                    cspProblem.getAssignmentsAsString(searchNode.state));
                System.out.println();
                return true;
            }

            searchNode = algorithm.nextNode();
        }

        // If we get here, no solution was found.
        System.out.println("No solution found :(");
        System.out.print(
            "\nSearch algorithm: "+ algorithm.getClass().getCanonicalName() +
            "\nStates expanded: "+ algorithm.getNumStatesExpanded() +
            "\nMax fringe size: "+ algorithm.getMaxFringeSize());
        System.out.println();
        return false;
    }

    /**
     * Kicks off the solving of the CSP provided by the user.
     * 
     * @param args See "usage" below.
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        String cspFile = null;
        String cspArg = null;
        boolean useForwardChecking = false;
        boolean useLeastConstrainingValue = false;
        boolean useMinimumRemainingValues = false;

        String usage = 
            "Usage: CSPDriver <csp type> [--help] <csp file> [-fc] [-mrv] [-lcv]\n\n"+
            "<csp type> is the type of CSP to solve:\n"+
            "    * 'generic' for a generic CSP; enter 'generic --help' for details on the expected format. \n\n"+
            "Options:\n"+
            "    -fc: Use forward checking.\n"+
            "    -mrv: Use the minimum remaining values to pick variables.\n"+
            "    -lcv: Use the least constraining value to pick values.\n";

        if (args.length < 2) {
            System.err.println(usage);
            return;
        }

        cspArg = args[0];
        cspFile = args[1];

        for(int i = 2; i < args.length; i++){
            if(args[i].equals("-fc")){
                useForwardChecking = true;
            } else if(args[i].equals("-mrv")){
                useMinimumRemainingValues = true;
            } else if(args[i].equals("-lcv")){
                useLeastConstrainingValue = true;
            } 
        }

        if (cspFile.equals("--help")){
            if(cspArg.equals("generic")){
                System.out.println(GenericCSP.getFileFormatDescription());
            } else {
                System.err.println("Unknown CSP type: "+cspArg);
                System.err.println(usage);
            }
            return;
        }

        

        if(cspArg.equals("generic")){
            CSPDriver.run(new GenericCSP(useForwardChecking, useLeastConstrainingValue, useMinimumRemainingValues), cspFile);
        }



    }

}
