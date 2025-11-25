import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Difficulty;
import Model.Game;

/**
 * Unit tests to verify that question and surprise cells can only be used once.
 */
public class UsedCellTest {

    public static void main(String[] args) {
        System.out.println("=== TEST: Used Question/Surprise Cells ===\n");

        GameController controller = new GameController();
        controller.startNewGame(Difficulty.EASY);
        Game game = controller.getCurrentGame();

        Board board = game.getBoard1();
        
        // Get a cell and manually set it as a question cell for testing
        Cell questionCell = board.getCell(0, 0);
        questionCell.setContent(Cell.CellContent.QUESTION);
        questionCell.setQuestionId(1);
        questionCell.setState(Cell.CellState.HIDDEN);
        questionCell.setUsed(false);
        
        // Set initial score to allow activation
        game.setSharedScore(20);
        int initialScore = game.getSharedScore();
        int activationCost = game.getDifficulty().getActivationCost();

        // ----- Test 1: First activation of question cell -----
        System.out.println("Test 1: First activation of question cell");
        check("Question cell should not be used initially", !questionCell.isUsed());
        check("Cell should be HIDDEN initially", questionCell.getState() == Cell.CellState.HIDDEN);
        
        boolean firstActivation = controller.revealCell(1, 0, 0);
        check("First reveal should succeed", firstActivation);
        check("Question cell should be marked as used after first activation", questionCell.isUsed());
        check("Cell should be REVEALED after activation", questionCell.getState() == Cell.CellState.REVEALED);
        check("Score should be deducted by activation cost", 
                game.getSharedScore() == initialScore - activationCost);

        // ----- Test 2: Second activation attempt (should skip effect) -----
        System.out.println("\nTest 2: Second activation attempt (should skip effect)");
        int scoreBeforeSecondClick = game.getSharedScore();
        
        // Manually reset state to HIDDEN to simulate another click (in real game, 
        // the cell would already be revealed, but we test the logic anyway)
        questionCell.setState(Cell.CellState.HIDDEN);
        
        boolean secondActivation = controller.revealCell(1, 0, 0);
        check("Second reveal should succeed (cell becomes revealed)", secondActivation);
        check("Question cell should still be marked as used", questionCell.isUsed());
        check("Score should NOT be deducted again (effect skipped)", 
                game.getSharedScore() == scoreBeforeSecondClick);
        check("Cell should be REVEALED after second click", 
                questionCell.getState() == Cell.CellState.REVEALED);

        // ----- Test 3: Surprise cell -----
        System.out.println("\nTest 3: Surprise cell usage");
        Cell surpriseCell = board.getCell(1, 1);
        surpriseCell.setContent(Cell.CellContent.SURPRISE);
        surpriseCell.setState(Cell.CellState.HIDDEN);
        surpriseCell.setUsed(false);
        
        int scoreBeforeSurprise = game.getSharedScore();
        
        boolean surpriseActivation = controller.revealCell(1, 1, 1);
        check("First surprise cell activation should succeed", surpriseActivation);
        check("Surprise cell should be marked as used", surpriseCell.isUsed());
        check("Score should be deducted for surprise cell", 
                game.getSharedScore() == scoreBeforeSurprise - activationCost);
        
        // Try to activate again
        surpriseCell.setState(Cell.CellState.HIDDEN);
        int scoreBeforeSecondSurprise = game.getSharedScore();
        boolean secondSurpriseActivation = controller.revealCell(1, 1, 1);
        check("Second surprise activation should skip effect", secondSurpriseActivation);
        check("Surprise cell should still be used", surpriseCell.isUsed());
        check("Score should NOT be deducted again", 
                game.getSharedScore() == scoreBeforeSecondSurprise);

        // ----- Test 4: Normal cells (not question/surprise) should work normally -----
        System.out.println("\nTest 4: Normal cells should work normally");
        Cell normalCell = board.getCell(2, 2);
        normalCell.setContent(Cell.CellContent.EMPTY);
        normalCell.setState(Cell.CellState.HIDDEN);
        normalCell.setUsed(false);
        
        boolean normalReveal = controller.revealCell(1, 2, 2);
        check("Normal cell reveal should succeed", normalReveal);
        check("Normal cell should be REVEALED", normalCell.getState() == Cell.CellState.REVEALED);
        check("Normal cell should not be marked as used", !normalCell.isUsed());

        System.out.println("\n=== All tests completed ===");
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
        } else {
            System.out.println("[FAIL] " + description);
        }
    }
}
