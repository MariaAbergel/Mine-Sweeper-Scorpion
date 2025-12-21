/*
 * Test Class for TC-BB-WONGAME-001
 * Verifies win detection, score calculation, and board state after victory.
 */
import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Game;
import Model.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WinGameTest {

    private GameController controller;
    private Game game;

    @BeforeEach
    void setup() {
        controller = GameController.getInstance();
        // Start an EASY game to minimize the number of cells to process
        controller.startNewGame("EASY");
        game = controller.getCurrentGame();
        assertNotNull(game, "Game should be initialized");
    }

    @Test
    @DisplayName("TC-BB-WONGAME-001: Verify win by flagging all mines (Successful Mine Identification)")
    void verifyWinByFlaggingMines() {
        Board board1 = game.getBoard1();
        
        // 1. Identify all mines on Board 1 to simulate player knowledge
        List<int[]> mineCoordinates = new ArrayList<>();
        for (int r = 0; r < board1.getRows(); r++) {
            for (int c = 0; c < board1.getCols(); c++) {
                if (board1.getCell(r, c).isMine()) {
                    mineCoordinates.add(new int[]{r, c});
                }
            }
        }

        assertFalse(mineCoordinates.isEmpty(), "Board should have mines generated");
        int totalMines = mineCoordinates.size();

        // 2. Pre-condition: Flag all mines except one
        for (int i = 0; i < totalMines - 1; i++) {
            int[] coords = mineCoordinates.get(i);
            controller.toggleFlagUI(1, coords[0], coords[1]);
        }

        // Verify game is still running before the final flag
        assertTrue(controller.isGameRunning(), "Game should be running before identifying the last mine");
        assertFalse(controller.isGameOver(), "Game Over state should be false initially");

        // 3. Critical Input: Player performs a Flag action on the last remaining mine
        int[] lastMine = mineCoordinates.get(totalMines - 1);
        controller.toggleFlagUI(1, lastMine[0], lastMine[1]);

        // 4. Verify Win State ("The team Wins!..." popup logic triggered by state change)
        assertFalse(controller.isGameRunning(), "Game should stop running after winning");
        assertTrue(controller.isGameOver(), "Game should be in Game Over state");
        assertEquals(GameState.WON, game.getGameState(), "GameState should be WON");

        // 5. Verify Scoring
        // "Final score = Player 1 Base Score + (remaining lives*N)"
        assertTrue(controller.getSharedScore() > 0, "Final score should be positive and calculated");

        // 6. Verify Boards State: Fully Revealed and Frozen
        
        // Check Frozen: Attempt to reveal a safe cell (should fail)
        // Find a safe cell coordinates
        int safeRow = -1, safeCol = -1;
        for (int r = 0; r < board1.getRows(); r++) {
            for (int c = 0; c < board1.getCols(); c++) {
                if (!board1.getCell(r, c).isMine()) {
                    safeRow = r; safeCol = c; break;
                }
            }
            if (safeRow != -1) break;
        }
        
        boolean revealResult = controller.revealCellUI(1, safeRow, safeCol);
        assertFalse(revealResult, "Board should be frozen; reveal action should return false");

        // Check Revealed: The safe cell should be revealed automatically upon win
        Cell safeCell = board1.getCell(safeRow, safeCol);
        assertTrue(safeCell.isRevealed(), "Safe cells should be automatically revealed after win");
    }
}