from math import sqrt
import sys
import problem 
import search_driver

class Maze(problem.Problem):
    # Keep this updated with all avaiable heuristics.
    heuristics = set(('manhattan', 'euclidean'))

    def __init__(self, mazeFilename=None, heuristic=None):
        '''
        Parameters:
            boardFilename (str): The filename of the board to load.
            heuristic (str): The heuristic to use for informed search.
        '''        
        if mazeFilename == None:
            self.board = []
            self.start = (-1,-1)
            self.exit = (-1,-1)
        else:
            self.loadFile(mazeFilename)

        heuristics = {
            'manhattan': self.manhattanDistance,
            'euclidean': self.euclideanDistance,
        }

        if heuristic is None:
            self.heuristic = self.defaultHeuristic
        elif heuristic in heuristics:
            self.heuristic = heuristics[heuristic]
        else:
            sys.exit('Invalid heuristic: ' + heuristic)


    def loadFile(self, mazeFilename):
        '''
        Parses a maze file. Should contain one character per spot on the maze.
        Here are the character codes:
            s -- the agent's starting position
            e -- exit (if reached, the goal state has been reached)
            w -- wall (cannot be entered by the agent)
            (blank) -- a spot the agent may enter

        Parameters:
            mazeFilename (str): The path to a file containing one character per spot on the board.
        '''
        self.board = []
        for i,row in enumerate(open(mazeFilename)):
            self.board.append([])
            for j,col in enumerate(row.rstrip('\n')):
                self.board[-1].append(col)
                if col == 's':
                    self.start = (i,j)
                elif col == 'e':
                    self.exit = (i,j)
                    

    def drawableBoard(self):
        '''
        Returns (list((str, Any, float, float))): The maze as a board of RBG colors rather than maze-specific symbols.
        '''
        colorMap = {
            ' ': search_driver.WHITE,
            's': search_driver.GREEN,
            'e': search_driver.RED,
            'w': search_driver.BLACK
        }
        boardColors = []

        for row in self.board:
            boardColors.append([])
            for col in row:
                boardColors[-1].append(colorMap[col])

        return boardColors

    def successors(self, state):
        '''
        Produces a list of spots -- (i,j) pairs -- that the agent can move into,
        along with the spot's cost and estimate of how far it is from the exit.
        Returned as a list of tuples:

            [
                (move, (i,j), cost, dist),
                ...
            ]

        Parameters:
            state ((i,j)): The current state of the problem.

        Returns (list((str, (int,int), float, float))): A list of the
            states that can be reached from the given state. Each item is a
            4-tuple: (action, new state, cost of action, estimated cost to
            goal).
        '''
        (i,j) = state
        successors = []
        potentialSuccessorSpots = (
            ('left', i,j-1),
            ('up', i-1,j), 
            ('right', i,j+1), 
            ('down', i+1,j) 
        )

        for move,i,j in potentialSuccessorSpots:
            if( i >= 0 and i < len(self.board) and 
                j >= 0 and j <len(self.board[i]) and 
                self.board[i][j] != 'w'):

                successors.append((move, (i,j), 1, self.getDistance((i,j))))

        return successors

    def getDistance(self, state):
        '''
        Estimates the distance from the state to the exit using whatever the current heuristic is.

        Parameters:
            state ((int,int)): The state to check.
            
        Returns (float): The estimated distance from the given state to a goal state. This should
            use whatever heuristic is currently set for the problem.
        '''
        return self.heuristic(state)

    def isGoal(self, state):
        '''
        The goal is reached when the agent is at the exit.
        
        Parameters:
            state ((int,int)): The state to check.

        Returns (bool): True if the given state is a goal state, False otherwise.
        '''        
        return state == self.exit

    def defaultHeuristic(self, state):
        '''
        Parameters:
            state ((int,int)): The state to check.

        Returns (float): The default heuristic value for the given state: 0.
        '''
        return 0

    def manhattanDistance(self, state):
        '''
        Parameters:
            state ((int,int)): The state to check.

        Returns (float): The Manhattan distance from the given state to the exit location.
        '''
        return abs(self.exit[0]-state[0]) + abs(self.exit[1]-state[1])

    def euclideanDistance(self, state):
        '''
        Parameters:
            state ((int,int)): The state to check.

        Returns (float): The Euclidean distance from the given state to the exit location.
        '''
        return float(sqrt(pow(self.exit[0]-state[0], 2) + pow(self.exit[1]-state[1], 2)))
