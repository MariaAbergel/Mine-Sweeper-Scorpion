package Model;

import java.util.Random;

/**
 * Represents a single board for one player.
 * Handles revealing, flagging, and tracking safe cells for victory.
 */
public class Board {

    private final int rows;
    private final int cols;
    private final int totalMines;
    private final int totalQuestionCells;
    private final int totalSurpriseCells;
    private final Cell[][] cells;
    private final Game game;

    // NEW: Counter to track how many safe cells are left to reveal
    private int safeCellsRemaining;

    public Board(Difficulty difficulty, Game game) {
        this.game = game;
        this.rows = difficulty.getRows();
        this.cols = difficulty.getCols();
        this.totalMines = difficulty.getMines();
        this.totalQuestionCells = difficulty.getQuestionCells();
        this.totalSurpriseCells = difficulty.getSurpriseCells();
        this.cells = new Cell[rows][cols];

        // Calculate total cells that must be revealed to win:
        // Total Cells - Mines = Safe Cells
        this.safeCellsRemaining = (rows * cols) - totalMines;

        // Initialize cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
            }
        }

        // Place logic
        placeMinesAndSpecialCells();
    }

    private void placeMinesAndSpecialCells() {
        placeContent(totalMines, Cell.CellContent.MINE);
        placeContent(totalQuestionCells, Cell.CellContent.QUESTION);
        placeContent(totalSurpriseCells, Cell.CellContent.SURPRISE);
        calculateNumbers();
    }

    private void placeContent(int count, Cell.CellContent type) {
        Random random = new Random();
        int placed = 0;
        while (placed < count) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (cells[r][c].getContent() == Cell.CellContent.EMPTY) {
                cells[r][c].setContent(type);
                placed++;
            }
        }
    }

    private void calculateNumbers() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine()) continue;
                int mines = countNeighborMines(r, c);
                if (mines > 0 && cells[r][c].getContent() == Cell.CellContent.EMPTY) {
                    cells[r][c].setContent(Cell.CellContent.NUMBER);
                    cells[r][c].setAdjacentMines(mines);
                }
            }
        }
    }

    private int countNeighborMines(int r, int c) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                int nr = r + i;
                int nc = c + j;
                if (isValid(nr, nc) && cells[nr][nc].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Reveals a cell and checks for Game Over / Victory conditions.
     */
    public void revealCell(int r, int c) {
        if (!isValid(r, c)) return;
        Cell cell = cells[r][c];

        // Block action if cell is already processed OR game is not running
        if (cell.isRevealed() || cell.isFlagged() || game.getGameState() != GameState.RUNNING) return;

        cell.reveal();

        // NEW: If we revealed a safe cell, decrement the counter
        if (!cell.isMine()) {
            safeCellsRemaining--;
        }

        switch (cell.getContent()) {
            case MINE:
                // SRS 2.1: Mine -> -1 life
                game.setSharedLives(game.getSharedLives() - 1);
                break;

            case EMPTY:
                // SRS 3.2.1.2.10: Recursive reveal
                autoRevealEmptyCells(r, c);
                break;

            case QUESTION:
            case SURPRISE:
                // SRS Appendix A: Deduct activation cost
                int cost = game.getDifficulty().getActivationCost();
                game.setSharedScore(game.getSharedScore() - cost);
                break;

            case NUMBER:
                break;
        }

        // After every move, check if we Won or Lost
        game.checkGameStatus();
    }


    private void autoRevealEmptyCells ( int r, int c){
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int nr = r + i;
                    int nc = c + j;
                    if (isValid(nr, nc)) {
                        Cell neighbor = cells[nr][nc];
                        if (!neighbor.isRevealed() && !neighbor.isFlagged() && !neighbor.isMine()) {
                            // Recursion flows back through revealCell to update safeCellsRemaining
                            revealCell(nr, nc);
                        }
                    }
                }
            }
        }

        public void toggleFlag ( int r, int c){
            if (!isValid(r, c) || game.getGameState() != GameState.RUNNING) return;

            Cell cell = cells[r][c];
            boolean isNowFlagged = cell.toggleFlag();
            int points = 10; // Standard points for flagging (can be moved to Difficulty later)

            if (isNowFlagged) {
                if (cell.isMine()) {
                    game.setSharedScore(game.getSharedScore() + points);
                } else {
                    game.setSharedScore(game.getSharedScore() - points);
                }
            }
        }

        private boolean isValid ( int r, int c){
            return r >= 0 && r < rows && c >= 0 && c < cols;
        }

        // NEW: Getter used by Game to check win condition
        public int getSafeCellsRemaining () {
            return safeCellsRemaining;
        }

        public int getRows () {
            return rows;
        }
        public int getCols () {
            return cols;
        }
        public int getTotalMines () {
            return totalMines;
        }
        public int getTotalQuestionCells () {
            return totalQuestionCells;
        }
        public int getTotalSurpriseCells () {
            return totalSurpriseCells;
        }
        public Cell[][] getCells () {
            return cells;
        }
        public Cell getCell ( int row, int col){
            if (isValid(row, col)) return cells[row][col];
            return null;
        }
    }
