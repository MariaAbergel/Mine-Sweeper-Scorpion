package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shows a single board (for one player).
 * לא מכיר בכלל את Board / Cell / Game – רק GameController.
 * 🔥 UPDATED: Grace period system - flagging doesn't immediately end turn.
 */
public class BoardPanel extends JPanel {

    private final GameController controller;
    private final int boardNumber;   // 1 or 2
    private final Runnable moveCallback;

    private JButton[][] buttons;
    private JLabel waitLabel;
    private boolean waiting;         // true = "WAIT FOR YOUR TURN"

    // 🔥 NEW: Grace period tracking
    private boolean inGracePeriod = false;  // True after flagging, before turn actually ends
    private int gracePeriodFlagRow = -1;
    private int gracePeriodFlagCol = -1;

    public BoardPanel(GameController controller,
                      int boardNumber,
                      boolean initiallyWaiting,
                      Runnable moveCallback) {
        this.controller = controller;
        this.boardNumber = boardNumber;
        this.waiting = initiallyWaiting;
        this.moveCallback = moveCallback;

        initComponents();
    }

    private void initComponents() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        setLayout(new OverlayLayout(this));
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

                // 1. ActionListener for standard LEFT-CLICK (Reveal)
                btn.addActionListener(e -> handleClick(rr, cc, false));

                // 2. MouseListener for RIGHT-CLICK (Flagging)
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleClick(rr, cc, true);
                        }
                    }
                });

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

    /**
     * Handles both revealing (isFlagging=false) and flagging (isFlagging=true).
     * 🔥 KEY FIX: Flagging enters a "grace period" where you can immediately unflag.
     */
    private void handleClick(int r, int c, boolean isFlagging) {
        if (!controller.isGameRunning()) return;

        // 1. Check turn and alert if necessary
        if (waiting) {
            JOptionPane.showMessageDialog(this,
                    "It is Player " + controller.getCurrentPlayerTurn() + "'s turn. Please wait.",
                    "Not Your Turn",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Handle the action based on type
        if (isFlagging) {
            boolean currentlyFlagged = controller.isCellFlagged(boardNumber, r, c);

            if (currentlyFlagged) {
                // User is trying to UNFLAG

                // Check if we're in grace period and this is the flagged cell
                if (inGracePeriod && gracePeriodFlagRow == r && gracePeriodFlagCol == c) {
                    // ✅ GRACE PERIOD UNFLAG: Unflag and stay in turn
                    controller.toggleFlagUI(boardNumber, r, c);
                    refresh();

                    // Exit grace period
                    inGracePeriod = false;
                    gracePeriodFlagRow = -1;
                    gracePeriodFlagCol = -1;

                    // Turn does NOT end - player can make another move
                    return;
                } else {
                    // ❌ REGULAR UNFLAG: Unflag and end turn
                    controller.toggleFlagUI(boardNumber, r, c);
                    refresh();

                    // Clear grace period
                    inGracePeriod = false;
                    gracePeriodFlagRow = -1;
                    gracePeriodFlagCol = -1;

                    // End turn
                    if (moveCallback != null) {
                        moveCallback.run();
                    }
                }
            } else {
                // User is FLAGGING a cell

                // If we're already in grace period, that means they're flagging a DIFFERENT cell
                // So we need to end the previous grace period and start a new one
                if (inGracePeriod) {
                    // End the previous grace period by ending the turn
                    inGracePeriod = false;
                    gracePeriodFlagRow = -1;
                    gracePeriodFlagCol = -1;

                    // Perform the new flag
                    controller.toggleFlagUI(boardNumber, r, c);
                    refresh();

                    // End turn
                    if (moveCallback != null) {
                        moveCallback.run();
                    }
                } else {
                    // Normal flag - enter grace period
                    controller.toggleFlagUI(boardNumber, r, c);
                    refresh();

                    // Enter grace period (turn doesn't end YET)
                    inGracePeriod = true;
                    gracePeriodFlagRow = r;
                    gracePeriodFlagCol = c;

                    // 🔥 KEY CHANGE: DO NOT call moveCallback yet!
                }
            }
        } else {
            // REVEAL action
            controller.revealCellUI(boardNumber, r, c);
            refresh();

            // Clear grace period
            inGracePeriod = false;
            gracePeriodFlagRow = -1;
            gracePeriodFlagCol = -1;

            // End turn
            if (moveCallback != null) {
                moveCallback.run();
            }
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

        // Clear grace period when becoming the active player
        if (!waiting) {
            inGracePeriod = false;
            gracePeriodFlagRow = -1;
            gracePeriodFlagCol = -1;
        }
    }

    /**
     * Repaint buttons according to cell state/content via controller.
     */
    public void refresh() {
        int rows = controller.getBoardRows(boardNumber);
        int cols = controller.getBoardCols(boardNumber);

        boolean gameIsRunning = controller.isGameRunning();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = buttons[r][c];
                GameController.CellViewData data =
                        controller.getCellViewData(boardNumber, r, c);

                if (!gameIsRunning) {
                    btn.setEnabled(false);
                } else {
                    btn.setEnabled(data.enabled);
                }

                btn.setText(data.text);
            }
        }
    }
}