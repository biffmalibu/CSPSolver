import heapq
import functools

@functools.total_ordering
class SearchNode:
    '''
    Represents a node in the search tree.
    '''
    def __init__(self, state, pathActions, pathStates, pathCost, priority=0):
        '''
        Parameters:
            state (Any): The state. The exact type depends on the problem definition.
            pathActions (list(str)): A list of actions that led to the state.
            pathStates (list(Any)): A list of states that led to this state. The exact state depends on the problem definition.
                                    A set is formed from this list and made available as pathStatesSet.
            pathCost (Numeric): The cost of the path that led to this state.
            priority (Numeric): The node's priority in the queue (defaults to 0).
        '''
        self.state = state
        self.pathActions = pathActions
        self.pathStates = pathStates
        self.pathStatesSet = set(pathStates)
        self.pathCost = pathCost
        self.priority = priority

    def _is_valid_operand(self, other):
        '''Ensures that other has a priority attribute. This is used for comparing two SearchNodes'''
        return hasattr(other, 'priority')

    def __eq__(self, other):
        '''
        Parameters:
            other (SearchNode): The other node to compare to.

        Returns (bool): True if the priority of this node is equal to the priority of the other node; no information
            about the state, path, or cost is used in this comparison.
        '''
        if not self._is_valid_operand(other):
            return NotImplemented
        return self.priority == other.priority

    def __lt__(self, other):
        '''
        Parameters:
            other (SearchNode): The other node to compare to.

        Returns (bool): True if the priority of this node is less than the priority of the other node; no information
            about the state, path, or cost is used in this comparison.
        '''
        if not self._is_valid_operand(other):
            return NotImplemented
        return self.priority < other.priority
    
class BFS:
    '''
    Breadth first search -- explores states based on distance from the starting
    state (closest first).
    '''
    def __init__(self, problem):
        ''''
        Constructor; readies the data members for the search.

        Parameters:
            problem (Any): The problem to solve; should have 
        '''
        self.name = "BFS"
        self.problem = problem
        self.maxFringeSize = 0
        self.statesExpanded = 0
        self.fringe = [SearchNode(problem.start, [], [problem.start], 0)]

    def nextState(self):
        '''
        Returns the next search state to consider, along with its path and total cost.
        Adds all of its successors to the fringe.

        Returns (SearchNode): The next search tree node, or None if no more states are left to explore.
        '''
        ## Check if there are no more states to explore.
        if len(self.fringe) == 0:
            return None 

        ## Pick next state off the fringe, along with accounting information
        node = self.fringe.pop(0)

        self.expandNode(node)

        self.maxFringeSize = max(self.maxFringeSize, len(self.fringe))

        return node


    def expandNode(self, node):
        '''
        Adds each of the unexpanded successors of the given node's state to the 
        fringe.

        Parameters:
            node (SearchNode): The node in the search tree to expand.
        '''
        self.statesExpanded += 1

        for move,successor,cost,dist in self.problem.successors(node.state):
            ## Skip seen states.
            if successor in node.pathStatesSet:
                continue

            self.fringe.append(SearchNode(
                successor,
                node.pathActions + [move],
                node.pathStates + [successor], 
                node.pathCost + cost
            ))

    def getName(self):
        '''Returns (str): The name of this search algorithm.
        '''
        return self.name

    def getMaxFringeSize(self):
        '''Retrurns (int): The max fringe size during the search.
        '''
        return self.maxFringeSize

    def getStatesExpanded(self):
        '''Returns (int): The number of states expanded during the search.
        '''
        return self.statesExpanded

class DFS:
    '''
    Depth first search -- explores states based on distance from the starting
    state (farthest first).
    '''
    def __init__(self, problem):
        self.name = "DFS"
        self.problem = problem
        self.maxFringeSize = 0
        self.statesExpanded = 0
        self.fringe = [SearchNode(problem.start, [], [problem.start], 0)]

    def nextState(self):
        '''
        Returns the next search state to consider, along with its path and total cost.
        Adds all of its successors to the fringe.

        Returns (SearchNode): The next search tree node, or None if no more states are left to explore.
        '''
        ## Check if there are no more states to explore.
        if len(self.fringe) == 0:
            return None 

        ## Pick next state off the fringe, along with accounting information
        node = self.fringe.pop()

        ## Exapand the node
        self.expandNode(node)

        self.maxFringeSize = max(self.maxFringeSize, len(self.fringe))

        return node


    def expandNode(self, node):
        '''
        Adds each of the unexpanded successors of the given node's state to the 
        fringe.

        Parameters:
            node (SearchNode): The node in the search tree to expand.
        '''
        self.statesExpanded += 1

        for move,successor,cost,dist in self.problem.successors(node.state):
            ## Skip seen states.
            if successor in node.pathStatesSet:
                continue

            self.fringe.append(SearchNode(
                successor,
                node.pathActions + [move],
                node.pathStates + [successor], 
                node.pathCost + cost
            ))

    def getName(self):
        '''Returns (str): The name of this search algorithm.
        '''
        return self.name

    def getMaxFringeSize(self):
        '''Retrurns (int): The max fringe size during the search.
        '''
        return self.maxFringeSize

    def getStatesExpanded(self):
        '''Returns (int): The number of states expanded during the search.
        '''
        return self.statesExpanded

class IterativeDeepening:
    '''
    Iterative deepening (ID) -- explores states using DFS at increasing depths.
    '''
    def __init__(self, problem):
        self.name = "Iterative deepening"
        self.problem = problem
        self.maxFringeSize = 0
        self.statesExpanded = 0
        self.fringe = [SearchNode(problem.start, [], [problem.start], 0)]
        self.depth = 0
        self.depthReached = 0

    def nextNodeOfNextDepth(self):
        '''
        Checks if the depth can be increased; if so, gets the first node of 
        the next pass. Otherwise, all nodes have been explored and there is no
        solution.
        
        Returns (SearchNode): None if no solution; first node of next pass if the depth can
                be increased.
        '''
        ## No more states to explore in the entire problem state space.
        if self.depthReached < self.depth:
            return None 

        ## No more states to explore at the current depth, but there may be
        ## more at the next depth level.
        else:
            self.depth += 1
            self.depthReached = 0
            self.fringe = [SearchNode(self.problem.start, [], [self.problem.start], 0)]
            return self.nextState()

    def nextState(self):
        '''
        Returns the next search state to consider, along with its path and total cost.
        Adds all of its successors to the fringe.

        Returns (SearchNode): The next search tree node, or None if no more states are left to explore.
        '''
        ## Check if no more states; progress to next depth level if necessary.
        if len(self.fringe) == 0:
            return self.nextNodeOfNextDepth()

        ## Pick next state off the fringe, along with accounting information
        node = self.fringe.pop()

        if len(node.pathActions) < self.depth:
            self.expandNode(node)

        self.maxFringeSize = max(self.maxFringeSize, len(self.fringe))
        self.depthReached = max(self.depthReached, len(node.pathActions))

        return node


    def expandNode(self, node):
        '''
        Adds each of the unexpanded successors of the given node's state to the 
        fringe.

        Parameters:
            node (SearchNode): The node in the search tree to expand.
        '''
        self.statesExpanded += 1

        for move,successor,cost,dist in self.problem.successors(node.state):
            ## Skip seen states.
            if successor in node.pathStatesSet:
                continue

            self.fringe.append(SearchNode(
                successor,
                node.pathActions + [move],
                node.pathStates + [successor],
                node.pathCost + cost
            ))

    def getName(self):
        '''Returns (str): The name of this search algorithm.
        '''
        return self.name

    def getMaxFringeSize(self):
        '''Retrurns (int): The max fringe size during the search.
        '''
        return self.maxFringeSize

    def getStatesExpanded(self):
        '''Returns (int): The number of states expanded during the search.
        '''
        return self.statesExpanded


class UCS:
    '''
    Uniform cost search -- explores states based on cost from the starting
    state (lowest cost first).
    '''
    def __init__(self, problem):
        self.name = "UCS"
        self.problem = problem
        self.maxFringeSize = 0
        self.statesExpanded = 0
        self.fringe = [SearchNode(problem.start, [], [problem.start], 0)]

    def nextState(self):
        '''
        Returns the next search state to consider, along with its path and total cost.
        Adds all of its successors to the fringe.

        Returns (SearchNode): The next search tree node, or None if no more states are left to explore.
        '''
        ## Check if there are no more states to explore.
        if len(self.fringe) == 0:
            return None 

        ## Pick next state off the fringe, along with accounting information
        node = heapq.heappop(self.fringe)

        self.expandNode(node)

        self.maxFringeSize = max(self.maxFringeSize, len(self.fringe))

        return node


    def expandNode(self, node):
        '''
        Adds each of the unexpanded successors of the given node's state to the 
        fringe.

        Parameters:
            node (SearchNode): The node in the search tree to expand.
        '''
        self.statesExpanded += 1

        for move,successor,cost,dist in self.problem.successors(node.state):
            ## Skip seen states.
            if successor in node.pathStatesSet:
                continue

            heapq.heappush(self.fringe, SearchNode(
                successor,
                node.pathActions + [move],
                node.pathStates + [successor], 
                node.pathCost + cost,
                node.pathCost + cost # Priority for UCS: g(x)
            ))

    def getName(self):
        '''Returns (str): The name of this search algorithm.
        '''
        return self.name

    def getMaxFringeSize(self):
        '''Retrurns (int): The max fringe size during the search.
        '''
        return self.maxFringeSize

    def getStatesExpanded(self):
        '''Returns (int): The number of states expanded during the search.
        '''
        return self.statesExpanded



class Greedy:
    '''
    Greedy search -- explores states based on their estimated distance to the
    goal state (closer to the goal = expanded sooner)
    '''
    def __init__(self, problem):
        self.name = "Greedy"
        self.problem = problem
        self.maxFringeSize = 0
        self.statesExpanded = 0
        ## Set the priority of the start state to h(x) -- we're calling it the
        ## "distance" to the goal in this impelementation.
        self.fringe = [SearchNode(problem.start, [], [problem.start], 0, problem.getDistance(problem.start))]

    def nextState(self):
        '''
        Returns the next search state to consider, along with its path and total cost.
        Adds all of its successors to the fringe.

        Returns (SearchNode): The next search tree node, or None if no more states are left to explore.
        '''
        ## Check if there are no more states to explore.
        if len(self.fringe) == 0:
            return None 

        ## Pick next state off the fringe, along with accounting information
        node = heapq.heappop(self.fringe)

        self.expandNode(node)

        self.maxFringeSize = max(self.maxFringeSize, len(self.fringe))

        return node


    def expandNode(self, node):
        '''
        Adds each of the unexpanded successors of the given node's state to the 
        fringe.

        Parameters:
            node (SearchNode): The node in the search tree to expand.
        '''
        self.statesExpanded += 1

        ## dist is h(x) (the estimated distance to the goal state)
        for move,successor,cost,dist in self.problem.successors(node.state):
            ## Skip seen states.
            if successor in node.pathStatesSet:
                continue

            heapq.heappush(self.fringe, SearchNode(
                successor,
                node.pathActions + [move],
                node.pathStates + [successor], 
                node.pathCost + cost,
                dist ## Priority for Greedy search: h(x)
            ))

    def getName(self):
        '''Returns (str): The name of this search algorithm.
        '''
        return self.name

    def getMaxFringeSize(self):
        '''Retrurns (int): The max fringe size during the search.
        '''
        return self.maxFringeSize

    def getStatesExpanded(self):
        '''Returns (int): The number of states expanded during the search.
        '''
        return self.statesExpanded
