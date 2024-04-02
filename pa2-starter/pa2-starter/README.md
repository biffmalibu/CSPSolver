# Endicott CSC460 Search Codebase

This folder contains the following subfolders:

    csps/        -- holds constraint satisfaction problem descriptions
        sudoku-boards/ -- holds Sudoku board files; each column is space delimited with
                          a - (dash) used to indicate empty spots; these do not describe
                          a CSP (as in, they do not include variables, domains, and constraints)
        (several example CSP files)

    mazes/       -- holds all the mazes see "Maze file format" below for the format
        (several example maze files)

    python/      -- Python 3 implementation
        (several python files)

    java/        -- Java implementation
        bin/     -- folder for compiled Java class files (initially empty)
        csc460/
            (several java files and directories)

Both codebases contain programs to load a maze file from disk and find a path
for the agent to take from a starting spot to the exit using on of several
search algorithms.

## Maze file format

Mazes are defined using the following characters:

    w -- wall; the agent may not pass through these spots
    s -- the start of the maze (entry point)
    e -- the end of the maze (exit point)
      (space) -- an open spot that the agent may freely move into

## Constratint Satisfaction Problems

Generic CSP files should consist of three or more lines:
  - Variables: a space separated list of variable names and their assignments (if any)
               assignments should be in the form of: x=value, where x is the variable name
  - Domains: a space separated list of domain values (treated as strings)
  - Constraints: 1 or more lines of constraints. Each row should be one of the following:
    * AllDiff var1 var2 var3 ... -- all of the given variables must have different values
    * AllSame var1 var2 var3 ... -- all of the given variables must have the same value
    * MaxCount n var1 var2 var3 ... -- no more than n of the given variables can have the same value

Here's an example of the "color the states of New England" problem:

    VT ME=blue NH MA RI CT
    blue red green
    AllDiff NH ME
    AllDiff VT NH MA
    AllDiff CT RI MA


## Python

To run the Python version, you will need Python 3 installed along with the
`pygame` library (install with the command: `python3 -m pip install pygame`). If
you have multiple versions of Python installed, replace `python3` with the
version you want to use, e.g., that might be `python3.10`. Use `python -V` to
find out what version of Python you're using. To run, do something like:

    cd python
    python3 search_driver.py -p=maze -f=../mazes/maze01.txt -a=bfs

The supported Python drivers are below: 

  * `python3 search_driver.py` -- For path-finding search problems.
  * `python3 csp.py` -- For constraint satisfaction problems.
    For example:
        `python3 csp.py ../csps/sudoku1.txt` 
    will solve the Sudoku puzzle described as a generic CSP in ../csps/sudoku1.txt 

## Java

This section assumes you're using a Bash-like terminal to interact with Java; if
you are on Windows, please install GitBash and use that if you don't already
have it (WSL is also fine). If you choose to use an IDE, you'll need to compile
and run according to that IDE.

First, navigate to the `java/` folder:

    cd java

Then compile:

    javac -d bin csc460/*.java csc460/*/*.java csc460/*/*/*.java

To run, do:

    java -cp bin csc460.drivers.SearchDriver -p=maze -a=bfs -f=../mazes/maze01.txt


The supported Java drivers are below: 

  * `java -cp bin csc460.drivers.SearchDriver` -- For path-finding search problems.
  * `java -cp bin csc460.drivers.CSPDriver` -- For constraint satisfaction problems.
    For example:
        `java -cp bin csc460.drivers.CSPDriver generic ../csps/sudoku1.txt` 
    will solve the Sudoku puzzle described as a generic CSP in ../csps/sudoku1.txt 


If you are running out of heap space, try increasing it. You can set the maximum
heapspace using the `-Xmx<size>` command line argument, where `<size>` is
replaced with a value. The default units are bytes, but you can use a `m` or
`g` suffix for MB and GB, respectively. If I want to use a max heap size of 5 GB
(as in, 5 GB of RAM), I'd run the example above like this:

    java -Xmx5g -cp bin csc460.drivers.SearchDriver -p=maze -a=bfs -f=../mazes/maze01.txt
