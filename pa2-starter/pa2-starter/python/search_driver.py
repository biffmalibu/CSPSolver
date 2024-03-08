from math import sqrt
import sys
import search 
import maze


import pygame


BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
GREEN = (0, 255, 0)
RED = (255, 0, 0)
BLUE = (0, 0, 255)
ORANGE = (255, 165, 0)

SPOT_SIZE = 60
MARGIN_SIZE = 5
FPS = 3
ALPHA_STEP = 2
ALPHA_START = 15
 
class SearchDriver:

    def __init__(self, problem, searchAlg, fps=FPS, spotSize=SPOT_SIZE, 
            marginSize=MARGIN_SIZE, graphicsOn=True):
        '''
        Parameters:
        	problem (Problem): The search Problem object (e.g., of type Maze).
        	searchAlg (Any): A search algorithm object (e.g, of type BFS, DFS, etc.)
        	fps (int): The frames per second to display the animation at.
        	spotSize (int): The size of each square in the graphical display.
        	margineSize (int): The amount of space to leave between spots in the
                           graphical display.
            graphicsOn (bool): Whether to display the search graphically. 
        '''
        self.problem = problem
        self.searchAlg = searchAlg
        self.fps = fps
        self.spotSize = spotSize 
        self.marginSize = marginSize
        self.size = (len(problem.board[0])*(spotSize+marginSize)+marginSize, 
        len(problem.board)*(spotSize+marginSize)+marginSize)
        
        self.graphicsOn = graphicsOn and hasattr(problem, 'drawableBoard') and callable(problem.drawableBoard)

        if self.graphicsOn:
            pygame.init()
            self.board = problem.drawableBoard()
            self.screen = pygame.display.set_mode(self.size)
            self.clock = pygame.time.Clock()


    def internalToExternalIndex(self, x):
        '''
        Converts a spot's row or column index to the horizontal or vertical
        position of the spot on the graphical display.

        Parameters:
        	 x (int): A spot's row or column index (0, 1, 2, ...).

        Returns (int): The horizontal or vertical position of the spot on the graphical
                display.
        '''
        return self.marginSize + x*(self.spotSize+self.marginSize)


    def drawSpot(self, x, y, color):
        '''
        Draws a spot (square) at the given position and of the given color.

        Parameters:
        	 x (int), y (int): The location on the board.
             color ((int, int, int)): An RGB color tuple.
        '''
        externalX = self.internalToExternalIndex(x)
        externalY =  self.internalToExternalIndex(y)

        s = pygame.Surface((self.spotSize, self.spotSize))
        if len(color) == 4:
            s.set_alpha(color[3])    # alpha level
        s.fill(color[0:3])           # this fills the entire surface
        self.screen.blit(s, (externalX,externalY))


    def run(self):
        '''
        Runs the search algorithm and displays the progress graphically if graphicsOn 
        is set. At the end of the search, stats are displayed to the terminal.
        '''
        ## This colors the current spot in the plan. Starts off very light blue.
        curSpotColor = [0,0,255,ALPHA_START]
        done = False
        searchOver = False

        while not done:
            # Main event loop
            if self.graphicsOn:
                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        done = True

        
            if not searchOver:
                node = self.searchAlg.nextState()

                if not node:
                    print('No solution found :(')
                    searchOver = True

                elif self.problem.isGoal(node.state):
                    if self.graphicsOn:
                        for spot in node.pathStates:
                            if spot != self.problem.start:
                                self.board[spot[0]][spot[1]] = ORANGE
                    searchOver = True
                    

                    # Print out stats about the solution.
                    print(f'Search algorithm: {self.searchAlg.getName()}')
                    print(f'States expanded: {self.searchAlg.getStatesExpanded()}')
                    print(f'Max fringe size: {self.searchAlg.getMaxFringeSize()}')
                    print(f'Solution cost: {node.pathCost}')
                    print(f'Solution path length: {len(node.pathActions)}')
                    print(f'Solution path (actions): {"".join([a[0] for a in node.pathActions])}')

                elif self.graphicsOn and node.state != self.problem.start:
                        self.board[node.state[0]][node.state[1]] = curSpotColor.copy()
                        ##  Make it a little bluer the next time around.
                        if curSpotColor[3] <= 245:
                            curSpotColor[3] += ALPHA_STEP

                # Update the graphics.
                if self.graphicsOn:
                    # Here, we clear the screen to white. Don't put other drawing
                    # commands above this, or they will be erased with this command.
                
                    # If you want a background image, replace this clear with
                    # blit'ing the background image.
                    self.screen.fill(WHITE)
            
                    # Refresh the board.
                    for j,row in enumerate(self.board):
                        for i,color in enumerate(row):
                            self.drawSpot(i, j, color)

                    # Update the screen with what we've drawn.
                    pygame.display.flip()
                
                    # Limit to FPS frames per second
                    self.clock.tick(self.fps)
            else:
                done = True

        # Close the window and quit.
        if self.graphicsOn:
            pygame.quit()
        else:
            return


def main():
    '''
    Parses command line arguments and runs the search algorithm. Call this file without any arguments
    to see the help message.
    '''
    boardFile = None 
    searchAlgorithm = None 
    searchProblem = None
    fps = FPS 
    spotSize = SPOT_SIZE 
    marginSize = MARGIN_SIZE
    heuristic = None
    graphicsOn = True

    # Map of search algorithm names to their classes.
    searchAlgorithms = {
        'bfs': search.BFS,
        'dfs': search.DFS,
        'id': search.IterativeDeepening,
        'ucs': search.UCS,
        'greedy': search.Greedy
    }

    # Map of search problem names to their classes.
    searchProblems = {
        'maze': maze.Maze
    }

    USAGE =('Usage: search_driver.py [arguments]\n\n'+
            'REQUIRED arguments:\n'+
            '   -f=F -- F is the filename of the board to read in; settings:\n'+
            '           maze:\n'+
            '             s -- the agent\'s starting position\n'+
            '             e -- the exit from the maze (only one)\n'+
            '             w -- a wall (agent cannot enter)\n'+
            '             [space] -- an open spot where the agent is allowed to enter\n'+
            '   -a=A -- A is the search algorithm. Options:\n'+
            '            * bfs -- breadth first search\n'+
            '            * dfs -- depth first search\n'+
            '            * id  -- iterative deepening\n'+
            '            * ucs -- uniform cost search\n'+
            '            * greedy -- greedy search\n'+
            '   -p=P -- P is the search problem; current options:\n'+
            '            * maze (find a path from entrance to exit)\n'+
            'OPTIONAL arguments:\n'+
            '   -h=H -- H is the heuristic. Possible values for H:\n'+
            '            for p=maze:\n'+
            '               * manhattan -- Manhattan distance to the exit\n'+
            '               * euclidean -- Euclidean distance to the exit\n'+
            '   -fps=FPS -- FPS is the frames per second; default: 3\n'+
            '   -spotSize=S -- S is the width and height of the board spots\n'+
            '           drawn to the screen; default is 60\n'+
            '   -marginSize=S -- S is the space between spots; default is 5\n'+
            '   -graphics=on|off -- if on (default), a window will appear showing\n'+
            '             the progress of the search\n')
           
    ## Read in arguments.
    for arg in sys.argv[1:]:
        if arg.startswith('-p='):
            searchProblem = arg[3:]
            if searchProblem not in searchProblems:
                sys.stderr.write('Invalid search problem: '+searchProblem+'\n')
                sys.stderr.write(USAGE)
                sys.exit()

        ## Board file (the starting state for the search problem).
        elif arg.startswith('-f='):
            boardFile = arg[3:]

        ## Search algorithm
        elif arg.startswith('-a='):
            searchAlgorithm = arg[3:]
            if searchAlgorithm not in searchAlgorithms:
                sys.stderr.write('Invalid search algorithm: '+searchAlgorithm+'\n')
                sys.stderr.write(USAGE)
                sys.exit()

        ## Heuristic
        elif arg.startswith('-h='):
            heuristic = arg[3:]

        ## FPS
        elif arg.startswith('-fps='):
            fps = int(arg[5:])

        ## Spot size
        elif arg.startswith('-spotSize='):
            spotSize = int(arg[10:])

        ## Margin size
        elif arg.startswith('-marginSize='):
            marginSize = int(arg[12:])

        ## Graphics
        elif arg.startswith('-graphics='):
            graphicsOn = arg[10:] == 'on'

    if boardFile == None or searchProblem == None or searchAlgorithm == None:
        sys.stderr.write('Too few args.\n\n'+ USAGE)
        sys.exit()

    if heuristic != None and heuristic not in searchProblems[searchProblem].heuristics:
        sys.stderr.write('Invalid heuristic: '+heuristic+'\n')
        sys.stderr.write(USAGE)
        sys.exit()

    problemInstance = searchProblems[searchProblem](boardFile, heuristic)
    searchInstance = searchAlgorithms[searchAlgorithm](problemInstance)

    (SearchDriver(problemInstance, searchInstance, fps, spotSize, marginSize, graphicsOn)).run()

if __name__ == '__main__':
    main()