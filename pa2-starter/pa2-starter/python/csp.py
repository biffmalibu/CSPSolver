# Author: Hank Feild (hfeild@endicott.edu)

from math import sqrt
import sys
import problem 
import search
import random

class CSPState():
    def __init__(self, assignments, unassignedVariables, domains):
        '''
        Creates a CSPState out of the given assignments and per-variable domains.

        Parameters:
            assignments (dict[str]:Any): A dictionary of variable names to their assignments.
            unassignedVariables (list[str]): A list of unassigned variables.
            domains (dict[str]:list[Any]): A dictionary of variable names to their domains. Each domain is a list of possible values.
        '''
        self.assignments = assignments
        self.unassignedVariables = unassignedVariables
        self.domains = domains

    def __eq__(self, other):
        return str(self) == str(other)
    def __hash__(self):
        return hash(str(self))

    def __str__(self):
        return str((tuple(sorted(self.assignments.items())), tuple(sorted(self.unassignedVariables)), tuple(sorted(self.domains.items()))))

class CSP(problem.Problem):
    '''
    A class representing a CSP problem. Subclass this to define a specific CSP problem.
    '''

    # Keep this updated with all avaiable heuristics.
    heuristics = set()

    def __init__(self, cspConfigFilename=None, forwardChecking=False, minimumRemainingValues=False, leastConstrainingValue=False):
        '''
        Parameters:
            boardFilename (str): The filename of the board to load.
            heuristic (str): The heuristic to use for informed search.
        '''        
        self.forwardChecking = forwardChecking
        self.minimumRemainingValues = minimumRemainingValues
        self.leastConstrainingValue = leastConstrainingValue

        if cspConfigFilename == None:
            self.start = (None, None, None, None)
            self.assignments = {}
            self.domains = {}
            self.constraints = []
        else:
            self.loadFile(cspConfigFilename)


    def loadFile(self, cspConfigFilename):
        '''
        Parses a CSP config file. Should contain two or more lines:
            1. A list of variables, separated by spaces. Initially assigned values should be of the form variable=value.
            2. A list of domain values, space separated
            3. (0 or more lines) A list of constraints, one per line, each of the form 
                    CONSTRAINT_TYPE variable1 variable2 ... variableN
                - CONSTRAINT_TYPE is one of: 'AllDiff', 'AllSame', 'MaxCount n'
                - 'AllDiff' means all variables must have the same value
                - 'AllSame' means all variables must have different values
                - 'MaxCount n' means no more than n variables can have the same value; n is an integer

        Parameters:
            cspConfigFilename (str): The path to a file containing the CSP configuration.
        '''
        self.assignments = {}
        self.unassignedVariables = []
        self.domains = {}
        self.constraints = []
        with open(cspConfigFilename) as file:
            # Parse the variables.
            for variable in file.readline().rstrip().split():
                if '=' in variable:
                    self.assignments[variable.split('=')[0]] = variable.split('=')[1]
                else:
                    self.unassignedVariables.append(variable)
                    self.domains[variable] = []

            # Parse the domains.
            for domainValue in file.readline().rstrip().split():
                # Add each domain value to each unassigned variable's domain.
                for key in self.unassignedVariables:
                    self.domains[key].append(domainValue)

            # Parse the constraints.
            self.constraints = []
            for line in file:
                self.constraints.append(line.split())


        self.start = CSPState(self.assignments, self.unassignedVariables, self.domains)

    def successors(self, state):
        '''
        Produces a list of assignments to the next variable.

        Parameters:
            state ((assignments, unassignedVariables, domains)): The current state of the problem.

        Returns (list((str, (int,int), float, float))): A list of the
            states that can be reached from the given state. Each item is a
            4-tuple: (action, new state, cost of action, estimated cost to
            goal).
        '''
        successors = []
        
        if len(state.unassignedVariables) == 0:
            return successors
        
        variable = self.chooseVariable(state)

        for value in self.orderValues(state, variable):
            newAssignments = {k:v for k,v in state.assignments.items()}
            newAssignments[variable] = value
            newUnassignedVariables = state.unassignedVariables.copy()
            newUnassignedVariables.remove(variable)
            newDomains = {k:v.copy() for k,v in state.domains.items()}

            # Check constraints.
            if self.checkConstraints(newAssignments):
                newState = CSPState(newAssignments, newUnassignedVariables, newDomains)


                if self.forwardChecking:
                    newState = self.forwardCheck(newState, variable, value)

                    # If there are any empty domains, skip this successor.
                    # TODO: implement this check.

                    
                successors.append((variable + '=' + value, newState, 1, 0))
        
        return successors
    
    def chooseVariable(self, state):
        '''Selects the next unassigned variable to consider. If
        self.minimumRemainingValues is True, choose the variable with the fewest
        remaining values in its domain, otherwise an arbitrary unassigned variable
        will be selected.
        
        Parameters:
            state ((assignments, unassignedVariables, domains)): The current state of the problem.

        Returns (str): The name of the variable to assign next.
        '''
        if self.minimumRemainingValues:
            # TODO: implement minimum remaining values ordering; return the variable with the fewest remaining values in its domain.
            return state.unassignedVariables[0] # Replace this line!
        else:
            return state.unassignedVariables[0]
        
        

    def orderValues(self, state, variable):
        '''Orders the values of the given variable. If
        self.leastConstrainingValue is True, values are ordered by how many
        values they remmove from the remaining variables' domains, otherwise an
        an arbitrary ordering is selected.
        
        Parameters:
            state ((assignments, unassignedVariables, domains)): The current
            state of the problem. variable (str): The name of the variable to
            select a value for.

        Returns (list[Any]): The values to assign to the variable.
        '''
        if self.leastConstrainingValue:
            # TODO: implement least constraining value ordering; return the values in the domain of
            # the given variable, ordered by how many values they remove from the remaining variables' domains.
            # You will find the forwardCheck() method helpful here.
            return state.domains[variable] # Replace this line!
        else:
            return state.domains[variable]
    

    def checkConstraints(self, assignments, variable=None):
        '''
        Checks if the given assignment satisfies all constraints.

        Parameters:
            assignments (dict[str]:Any): The current assignment of variables.
            variable (str): Optional. If not None, only check the constraints that involve this variable.

        Returns (bool): True if the given assignment satisfies all constraints, False otherwise.
        '''
        for constraint in self.constraints:
            # Skip this constraint if necessary.
            if variable is not None and variable not in constraint[1:]:
                continue

            if constraint[0] == 'AllDiff':
                values = set()
                for var in constraint[1:]:
                    if var in assignments:
                        if assignments[var] in values:
                            return False
                        else:
                            values.add(assignments[var])

            elif constraint[0] == 'AllSame':
                values = set()
                for var in constraint[1:]:
                    if var in assignments:
                        values.add(assignments[var])

                if len(values) > 1:
                    return False
                
            elif constraint[0] == 'MaxCount':
                n = int(constraint[1])
                counts = {}
                for var in constraint[2:]:
                    if var in assignments:
                        if assignments[var] in counts:
                            counts[assignments[var]] += 1
                            if counts[assignments[var]] > n:
                                return False
                        else:
                            counts[assignments[var]] = 1

        return True

    def getDistance(self, state):
        '''Ignored. This is not an informed search problem.'''
        return 0

    def isGoal(self, state):
        '''
        The goal is reached when the agent is at the exit.
        
        Parameters:
            state ((int,int)): The state to check.

        Returns (bool): True if the given state is a goal state, False otherwise.
        '''        
        return len(state.unassignedVariables) == 0
    
    def forwardCheck(self, state, variable, value):
        '''
        Returns a new state with the given variable assigned the given value, and the domains of the other variables updated.

        Parameters:
            state ((assignments, unassignedVariables, domains)): The current state of the problem.
            variable (str): The variable to assign.
            value (Any): The value to assign to the variable.

        Returns (CSPState): A new state with the given variable assigned the given value, and the domains of the other variables updated.
        '''
        # Make deep copies of the state data.
        newAssignments = {k:v for k,v in state.assignments.items()}
        newAssignments[variable] = value
        newUnassignedVariables = [var for var in state.unassignedVariables if var != variable]
        newDomains = {k:v.copy() for k,v in state.domains.items()}
        
        # TODO: check each of the constraints, skipping those that don't involve
        # the variable being assigned. Handle each constraint type separately
        # (AllDiff, AllSame, MaxCount). For example, if the constraint is
        # AllDiff, remove the value assigned to the new variable from the
        # domains of the unassigned variables involved in the constraint.

        return CSPState(newAssignments, newUnassignedVariables, newDomains)


def main():
    if len(sys.argv) < 2:
        print("Usage: python3 csp.py <cspConfigFilename> [-fc] [-mrv] [-lcv]\n\n"+
              "Options:\n"+
                "  -fc: Use forward checking.\n"+
                "  -mrv: Use minimum remaining values ordering method.\n"+
                "  -lcv: Use least constraining value ordering method.\n")
        return
    
    forwardChecking = '-fc' in sys.argv
    minimumRemainingValues = '-mrv' in sys.argv
    leastConstrainingValue = '-lcv' in sys.argv

    print(f'Foward checking: {forwardChecking}')
    print(f'Minimum remaining values: {minimumRemainingValues}')
    print(f'Least constraining value: {leastConstrainingValue}')

    csp = CSP(sys.argv[1], forwardChecking, minimumRemainingValues, leastConstrainingValue)
    dfs = search.DFS(csp)

    node = dfs.nextState()
    while node is not None:
        if csp.isGoal(node.state):
            print(f'Search algorithm: {dfs.getName()}')
            print(f'States expanded: {dfs.getStatesExpanded()}')
            print(f'Max fringe size: {dfs.getMaxFringeSize()}')
            print(f'Solution path length: {len(node.pathActions)}')
            print(f'Solution path (actions): {", ".join(node.pathActions)}')
            break
        
        node = dfs.nextState()

    if node is None:
        print(f'Search algorithm: {dfs.getName()}')
        print(f'States expanded: {dfs.getStatesExpanded()}')
        print(f'Max fringe size: {dfs.getMaxFringeSize()}')
        print("No solution found.")



if __name__ == "__main__":
    main()