package Controller;

import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;

/**
 * Controller class between the UI (View) and the Game model.
 */
public class GameController {

    private Game currentGame;

    /**
     * Creates a new Game instance with the selected difficulty.
     * This is the main entry point for starting a cooperative game.
     */
    public void startNewGame(Difficulty difficulty) {
        currentGame = new Game(difficulty);
    }

    /**
     * Restarts the current game using the same difficulty.
     * If no game exists yet, nothing happens.
     */
    public void restartGame() {
        if (currentGame != null) {
            currentGame.restartGame();
        }
    }

    /**
     * Returns the current Game instance so that other parts
     * of the system (e.g., GUI) can read boards, lives, score, etc.
     */
    public Game getCurrentGame() {
        return currentGame;
    }

    /**
     * Reveals a cell on the specified board.
     * For question and surprise cells, this method:
     * - Checks if the cell was already used
     * - If already used, skips the special effect and does nothing
     * - If not used, marks it as used and triggers the special effect
     * 
     * @param boardNumber 1 for board1, 2 for board2
     * @param row the row index of the cell
     * @param col the column index of the cell
     * @return true if the cell was successfully revealed/activated, false otherwise
     */
    public boolean revealCell(int boardNumber, int row, int col) {
        if (currentGame == null) {
            return false;
        }

        Board board = (boardNumber == 1) ? currentGame.getBoard1() : currentGame.getBoard2();
        if (board == null || row < 0 || row >= board.getRows() || col < 0 || col >= board.getCols()) {
            return false;
        }

        Cell cell = board.getCell(row, col);
        if (cell == null || cell.getState() == Cell.CellState.REVEALED) {
            return false;
        }

        // Handle question and surprise cells
        if (cell.getContent() == Cell.CellContent.QUESTION || 
            cell.getContent() == Cell.CellContent.SURPRISE) {
            
            // Check if cell is already used
            if (cell.isUsed()) {
                // Cell was already used, skip the special effect
                // Just reveal it (change state to REVEALED) without triggering effect
                cell.setState(Cell.CellState.REVEALED);
                return true;
            }
            
            // Cell is not used yet - activate it for the first time
            cell.setUsed(true);
            cell.setState(Cell.CellState.REVEALED);
            
            // Trigger the special effect via Game class
            currentGame.activateSpecialCell(cell.getContent(), cell.getQuestionId());
            return true;
        }

        // For other cell types (EMPTY, MINE, NUMBER), reveal normally
        cell.setState(Cell.CellState.REVEALED);
        return true;
    }
}
