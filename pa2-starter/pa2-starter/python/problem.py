from abc import ABC, abstractmethod, abstractproperty

class Problem(ABC):
    '''
    An abstract class representing a search problem.
    '''
    heuristics = set() # This should be a set of all the available heuristics for this problem.

    @abstractmethod
    def __init__(self, boardFilename=None, heuristic=None):
        '''
        Parameters:
            boardFilename (str): The filename of the board to load.
            heuristic (str): The heuristic to use for informed search.
        '''
        pass


    @abstractmethod
    def loadFile(self, boardFilename):
        '''
        Parses a file. 
        Parameters:
            boardFilename (str): The path to a file containing one character per spot on the board.
        '''
        pass

    @abstractmethod
    def successors(self, state):
        '''
        Returns (list((str, Any, float, float))): A list of the states that can be reached from the given state. Each item
            should be a 4-tuple: (action, new state, cost of action, estimated cost to goal). The type of the
            new state is up to the problem to define.
        '''
        pass

    @abstractmethod
    def getDistance(self, state):
        '''
        Returns (float): The estimated distance from the given state to a goal state. This should
            use whatever heuristic is currently set for the problem.
        '''
        pass

    @abstractmethod
    def isGoal(self, state):
        '''
        Returns (bool): True if the given state is a goal state, False otherwise.
        '''
        pass

