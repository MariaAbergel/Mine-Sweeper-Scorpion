package View;

import Controller.GameController;
import Model.Board;
import Model.Cell;
import Model.Game;
import Model.GameState;

import javax.swing.*;
import java.awt.*;

public class BoardPanel extends JPanel {

    private final GameController controller;
    private final Board board;
    private final int boardNumber;              // 1 or 2
    private final Runnable moveCallback;       // called after a successful move

    private JButton[][] buttons;
    private JLabel waitLabel;
    private boolean waiting;                   // true = "WAIT FOR YOUR TURN"

    public BoardPanel(GameController controller,
                      Board board,
                      int boardNumber,
                      boolean initiallyWaiting,
                      Runnable moveCallback) {
        this.controller = controller;
        this.board = board;
        this.boardNumber = boardNumber;
        this.waiting = initiallyWaiting;
        this.moveCallback = moveCallback;

        initComponents();
    }

    private void initComponents() {
        int rows = board.getRows();
        int cols = board.getCols();

        setLayout(new OverlayLayout(this));      // grid + overlay text
        setBackground(Color.BLACK);

        JPanel gridPanel = new JPanel(new GridLayout(rows, cols));
        gridPanel.setBackground(Color.BLACK);

        buttons = new JButton[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final int rr = r;
                final int cc = c;

                JButton btn = new JButton();
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setFocusable(false);
                btn.setPreferredSize(new Dimension(25, 25));

                btn.addActionListener(e -> handleClick(rr, cc));

                buttons[r][c] = btn;
                gridPanel.add(btn);
            }
        }

        add(gridPanel);

        // Overlay label for "WAIT FOR YOUR TURN"
        waitLabel = new JLabel("WAIT FOR YOUR TURN", SwingConstants.CENTER);
        waitLabel.setFont(new Font("Arial", Font.BOLD, 14));
        waitLabel.setForeground(Color.BLACK);
        waitLabel.setOpaque(true);
        waitLabel.setBackground(new Color(255, 255, 255, 170));
        waitLabel.setAlignmentX(0.5f);
        waitLabel.setAlignmentY(0.5f);
        waitLabel.setVisible(waiting);

        add(waitLabel);

        refresh();
    }

    private void handleClick(int r, int c) {
        Game game = controller.getCurrentGame();
        if (game == null) return;

        // Game over? no moves.
        if (game.getGameState() != GameState.RUNNING) return;

        // Not this board's turn? ignore.
        if (game.getCurrentPlayerTurn() != boardNumber) return;

        // Also ignore if this panel is marked as waiting
        if (waiting) return;

        board.revealCell(r, c);   // uses your existing Board logic

        refresh();

        // notify parent (GamePanel) that a move happened
        if (moveCallback != null) {
            moveCallback.run();
        }
    }

    /**
     * Called by GamePanel when the turn changes.
     */
    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
        if (waitLabel != null) {
            waitLabel.setVisible(waiting);
        }
    }

    /**
     * Repaint buttons according to cell state/content.
     */
    public void refresh() {
        int rows = board.getRows();
        int cols = board.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = board.getCell(r, c);
                JButton btn = buttons[r][c];

                if (cell == null) continue;

                switch (cell.getState()) {
                    case HIDDEN:
                        btn.setText("");
                        btn.setEnabled(true);
                        break;

                    case FLAGGED:
                        btn.setText("F");
                        btn.setEnabled(true);
                        break;

                    case REVEALED:
                        btn.setEnabled(false);
                        switch (cell.getContent()) {
                            case MINE:
                                btn.setText("M");
                                break;
                            case NUMBER:
                                btn.setText(String.valueOf(cell.getAdjacentMines()));
                                break;
                            case QUESTION:
                                btn.setText("Q");
                                break;
                            case SURPRISE:
                                btn.setText("S");
                                break;
                            case EMPTY:
                            default:
                                btn.setText("");
                                break;
                        }
                        break;
                }
            }
        }
    }
}
