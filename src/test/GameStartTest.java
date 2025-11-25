import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;

public class GameStartTest {

    public static void main(String[] args) {
        GameController controller = new GameController();

        // =============================================================
        // PART 1: Initialization & Restart Tests
        // =============================================================

        // ----- 1) Start new game on EASY -----
        controller.startNewGame(Difficulty.EASY);
        Game game = controller.getCurrentGame();

        System.out.println("=== TEST SET 1: Initialization & Restart ===");
        check("Game should not be null", game != null);
        assert game != null;
        check("Difficulty should be EASY", game.getDifficulty() == Difficulty.EASY);

        Board b1 = game.getBoard1();
        Board b2 = game.getBoard2();

        check("Board1 should not be null", b1 != null);
        check("Board2 should not be null", b2 != null);

        // Board size mapping
        assert b1 != null;
        check("Board1 rows == EASY rows", b1.getRows() == Difficulty.EASY.getRows());
        check("Board1 cols == EASY cols", b1.getCols() == Difficulty.EASY.getCols());

        // Mines / question / surprise mapping
        check("Board1 mines == EASY mines", b1.getTotalMines() == Difficulty.EASY.getMines());
        check("Board1 question cells == EASY question cells",
                b1.getTotalQuestionCells() == Difficulty.EASY.getQuestionCells());

        // Shared lives / score
        check("Shared lives == EASY startingLives",
                game.getSharedLives() == Difficulty.EASY.getStartingLives());
        check("Shared score starts at 0", game.getSharedScore() == 0);

        // ----- 2) Modify lives & score, then restart -----
        game.setSharedLives(game.getSharedLives() - 3);
        game.setSharedScore(25);

        Board oldBoard1 = game.getBoard1();

        controller.restartGame();
        Game restarted = controller.getCurrentGame();

        System.out.println("\n--- Restart Verification ---");
        check("Shared lives reset to EASY startingLives",
                restarted.getSharedLives() == Difficulty.EASY.getStartingLives());
        check("Shared score reset to 0",
                restarted.getSharedScore() == 0);
        check("Board1 should be a new instance after restart",
                restarted.getBoard1() != oldBoard1);


        // =============================================================
        // PART 2: Board Logic Tests (Revealing, Flagging, Scoring)
        // =============================================================
        System.out.println("\n=== TEST SET 2: Game Rules & Logic ===");

        // Get a fresh game reference and board to test on
        Game activeGame = controller.getCurrentGame();
        Board testBoard = activeGame.getBoard1();

        // --- TEST A: Flagging a Mine (Should Increase Score) ---
        int scoreBeforeFlag = activeGame.getSharedScore(); // 0
        Cell mineCell = findCellWithContent(testBoard, Cell.CellContent.MINE);

        if (mineCell != null) {
            testBoard.toggleFlag(mineCell.getRow(), mineCell.getCol());
            // Assuming +10 points for correct flag
            check("Flagging a Mine should INCREASE score",
                    activeGame.getSharedScore() > scoreBeforeFlag);
        } else {
            System.out.println("[SKIP] Could not find a Mine to test flagging.");
        }

        // --- TEST B: Flagging a Safe Cell (Should Decrease Score) ---
        int scoreAfterGoodFlag = activeGame.getSharedScore();
        Cell safeCell = findCellWithContent(testBoard, Cell.CellContent.EMPTY);

        if (safeCell != null) {
            testBoard.toggleFlag(safeCell.getRow(), safeCell.getCol());
            // Assuming penalty for incorrect flag
            check("Flagging a Safe Cell should DECREASE score",
                    activeGame.getSharedScore() < scoreAfterGoodFlag);
        }

        // --- TEST C: Revealing a Mine (Should Lose Life) ---
        // Note: We need an unflagged mine. Let's find a new one or unflag the old one.
        // For simplicity, let's just manually reset the cell we flagged earlier if needed,
        // but finding a new mine is safer logic.

        // Reset game to ensure clean state for next test
        controller.restartGame();
        activeGame = controller.getCurrentGame();
        testBoard = activeGame.getBoard1();

        int livesBeforeExplosion = activeGame.getSharedLives();
        Cell explodeCell = findCellWithContent(testBoard, Cell.CellContent.MINE);

        if (explodeCell != null) {
            testBoard.revealCell(explodeCell.getRow(), explodeCell.getCol());
            check("Revealing a Mine should SUBTRACT 1 life",
                    activeGame.getSharedLives() == livesBeforeExplosion - 1);
        }

        // --- TEST D: Revealing Question (Should Deduct Activation Cost) ---
        controller.restartGame();
        activeGame = controller.getCurrentGame();
        testBoard = activeGame.getBoard1();

        int scoreBeforeQuestion = activeGame.getSharedScore(); // 0
        int cost = Difficulty.EASY.getActivationCost(); // 5

        Cell questionCell = findCellWithContent(testBoard, Cell.CellContent.QUESTION);

        if (questionCell != null) {
            testBoard.revealCell(questionCell.getRow(), questionCell.getCol());
            check("Revealing Question should DEDUCT activation cost",
                    activeGame.getSharedScore() == scoreBeforeQuestion - cost);
        }
    }

    // -------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------


    /**
     * Helper method to print test results.
     */
    private static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
        } else {
            System.out.println("[FAIL] " + description);
        }
    }

    /**
     * Helper to find a specific cell type on the board because placement is random.
     */
    private static Cell findCellWithContent(Board board, Cell.CellContent type) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).getContent() == type) {
                    return board.getCell(r, c);
                }
            }
        }
        return null;
    }
}