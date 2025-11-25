package Model;

/**
 * Difficulty level of the game.
 * Each difficulty defines:
 * - board size (rows, cols)
 * - number of mines
 * - number of question cells
 * - number of surprise cells
 * - starting shared lives
 * - score cost to activate a question/surprise cell
 */
public enum Difficulty {
    EASY(
            9, 9,      // rows, cols
            10,        // mines
            6,         // question cells
            2,         // surprise cells
            10,        // starting lives
            5          // activation cost (score points)
    ),
    MEDIUM(
            13, 13,
            26,
            7,
            3,
            8,
            8
    ),
    HARD(
            16, 16,
            44,
            11,
            4,
            6,
            12
    );

    private final int rows;
    private final int cols;
    private final int mines;
    private final int questionCells;
    private final int surpriseCells;
    private final int startingLives;
    private final int activationCost;

    Difficulty(int rows,
               int cols,
               int mines,
               int questionCells,
               int surpriseCells,
               int startingLives,
               int activationCost) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.questionCells = questionCells;
        this.surpriseCells = surpriseCells;
        this.startingLives = startingLives;
        this.activationCost = activationCost;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMines() {
        return mines;
    }

    public int getQuestionCells() {
        return questionCells;
    }

    public int getSurpriseCells() {
        return surpriseCells;
    }

    public int getStartingLives() {
        return startingLives;
    }

    public int getActivationCost() {
        return activationCost;
    }
}
