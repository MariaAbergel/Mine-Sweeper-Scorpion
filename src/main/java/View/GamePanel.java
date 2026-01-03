package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;



/**
 * Main in-game panel: displays two boards, player info, score, lives and controls.
 * Communicates only with GameController (no direct access to the Model layer).
 */
public class GamePanel extends JPanel {

    private final GameController controller;

    private final String player1Name;
    private final String player2Name;

    private BoardPanel boardPanel1;
    private BoardPanel boardPanel2;
    // Player area labels
    private NeonInputField playerBox1;
    private NeonInputField playerBox2;
    private JLabel lblMinesLeft1;
    private JLabel lblMinesLeft2;

    // Bottom status
    private JLabel lblScore;
    private JLabel lblLives;
    private JPanel heartsPanel;
    private List<JLabel> heartLabels;

    // Control Buttons
    private JButton btnRestart;
    private JButton btnExit;
    private final long startTimeMillis;
    private JPanel wrap1;
    private JPanel wrap2;
    private JPanel centerPanel;
    private Timer resizeStabilizer;



    public GamePanel(GameController controller,
                     String player1Name, String player2Name) {
        this.controller = controller;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.startTimeMillis = System.currentTimeMillis();


        // Register the question presenter so QUESTION cells will show a popup.
        controller.registerQuestionPresenter(question ->
                QuestionDialog.showQuestionDialog(SwingUtilities.getWindowAncestor(this), question));

        initComponents();
        updateStatus();
        updateTurnUI();
    }

    /**
     * Builds the UI layout: title, two player areas, and bottom status/control area.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // ===== Pick background by difficulty =====
        String levelName = controller.getDifficultyName();
        String bgPath;
        switch (levelName) {
            case "EASY" -> bgPath = "/ui/game/bg_easy.png";
            case "MEDIUM" -> bgPath = "/ui/game/bg_medium.png";
            case "HARD" -> bgPath = "/ui/game/bg_hard.png";
            default -> bgPath = "/ui/game/bg_easy.png";
        }

        BackgroundPanel bg = new BackgroundPanel(bgPath);
        bg.setLayout(new BorderLayout());
        bg.setOpaque(false);
        add(bg, BorderLayout.CENTER);

        // (No TOP panel – because image already has title/level)

        // =========================
        // CENTER: two player panels
        // =========================
        centerPanel = new JPanel(new GridLayout(1, 2, 40, 0));
        centerPanel.setOpaque(false);
        bg.add(centerPanel, BorderLayout.CENTER);

        // ----- Player 1 side -----
        // ----- Player 1 side -----
        JPanel leftSide = new JPanel(new BorderLayout());
        leftSide.setOpaque(false);

// top area (name + mines)
        JPanel leftTop = new JPanel();
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));
        leftTop.setOpaque(false);
        leftTop.setBorder(BorderFactory.createEmptyBorder(42, 0, 0, 0)); // ← ADD THIS LINE (40px top padding)

        playerBox1 = new NeonInputField(new Color(255, 80, 80));
        playerBox1.setText(player1Name);
        playerBox1.setDisplayMode(true);
        playerBox1.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox1.setFieldWidth(210); // half of 420
        leftTop.add(playerBox1);
        leftTop.add(Box.createVerticalStrut(5));

        lblMinesLeft1 = new JLabel("MINES LEFT: " + controller.getTotalMines(1), SwingConstants.CENTER);
        lblMinesLeft1.setForeground(Color.WHITE);
        lblMinesLeft1.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft1.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftTop.add(lblMinesLeft1);

        JPanel leftTopWrapper = new JPanel(new BorderLayout());
        leftTopWrapper.setOpaque(false);

        leftTopWrapper.setBorder(
                BorderFactory.createEmptyBorder(70, 0, 0, 0)
        );

        leftTopWrapper.add(leftTop, BorderLayout.CENTER);
        leftSide.add(leftTopWrapper, BorderLayout.NORTH);

// board area (expands!)
        boardPanel1 = new BoardPanel(controller, 1, false, this::handleMoveMade);
        boardPanel1.setOpaque(false);

        wrap1 = new JPanel(new GridBagLayout());
        wrap1.setOpaque(false);

        wrap1.add(boardPanel1, new GridBagConstraints()); // centered
        leftSide.add(wrap1, BorderLayout.CENTER);

        // ----- Player 2 side -----
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);

// top area (name + mines)
        JPanel rightTop = new JPanel();
        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.Y_AXIS));
        rightTop.setOpaque(false);
        rightTop.setBorder(BorderFactory.createEmptyBorder(42, 0, 0, 0)); // ← ADD THIS LINE (40px top padding)

        playerBox2 = new NeonInputField(new Color(80, 180, 255));
        playerBox2.setText(player2Name);
        playerBox2.setDisplayMode(true);
        playerBox2.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBox2.setFieldWidth(210); // half of 420
        rightTop.add(playerBox2);
        rightTop.add(Box.createVerticalStrut(5));

        lblMinesLeft2 = new JLabel("MINES LEFT: " + controller.getTotalMines(2), SwingConstants.CENTER);
        lblMinesLeft2.setForeground(Color.WHITE);
        lblMinesLeft2.setFont(new Font("Arial", Font.BOLD, 14));
        lblMinesLeft2.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightTop.add(lblMinesLeft2);

        JPanel rightTopWrapper = new JPanel(new BorderLayout());
        rightTopWrapper.setOpaque(false);

        rightTopWrapper.setBorder(
                BorderFactory.createEmptyBorder(70, 0, 0, 0)
        );

        rightTopWrapper.add(rightTop, BorderLayout.CENTER);
        rightSide.add(rightTopWrapper, BorderLayout.NORTH);

// board area (expands!)
        boardPanel2 = new BoardPanel(controller, 2, true, this::handleMoveMade);
        boardPanel2.setOpaque(false);

        wrap2 = new JPanel(new GridBagLayout());
        wrap2.setOpaque(false);

        wrap2.add(boardPanel2, new GridBagConstraints()); // centered
        rightSide.add(wrap2, BorderLayout.CENTER);

        centerPanel.removeAll();
        centerPanel.add(leftSide);
        centerPanel.add(rightSide);
        centerPanel.revalidate();
        centerPanel.repaint();

        // =========================
        // BOTTOM: score + lives + hearts + controls
        // =========================
        JPanel bottomOuter = new JPanel();
        bottomOuter.setLayout(new BoxLayout(bottomOuter, BoxLayout.Y_AXIS));
        bottomOuter.setOpaque(false);

        JPanel scoreLivesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        scoreLivesPanel.setOpaque(false);

        lblScore = new JLabel("SCORE: 0");
        lblScore.setForeground(Color.WHITE);
        lblScore.setFont(new Font("Arial", Font.BOLD, 18));

        lblLives = new JLabel("LIVES: " + controller.getSharedLives() + "/" + controller.getMaxLives());
        lblLives.setForeground(Color.WHITE);
        lblLives.setFont(new Font("Arial", Font.BOLD, 18));

        scoreLivesPanel.add(lblScore);
        scoreLivesPanel.add(lblLives);
        bottomOuter.add(scoreLivesPanel);

        heartsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        heartsPanel.setOpaque(false);
        buildHearts();
        bottomOuter.add(heartsPanel);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 5));
        controlsPanel.setOpaque(false);

        btnRestart = new JButton("Restart");
        btnExit = new JButton("Exit");
        styleControlButton(btnRestart);
        styleControlButton(btnExit);

        btnExit.addActionListener(e -> {
            controller.endGame();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof MainFrame frame) {
                frame.showMainMenu();
            }
        });

        btnRestart.addActionListener(e -> {
            controller.restartGame();
            buildHearts();
            updateStatus();
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
        });

        controlsPanel.add(btnRestart);
        controlsPanel.add(btnExit);

        bottomOuter.add(Box.createVerticalStrut(5));
        bottomOuter.add(controlsPanel);

        bg.add(bottomOuter, BorderLayout.SOUTH);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                requestResizeBoards();
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                requestResizeBoards();
            }
        });


        SwingUtilities.invokeLater(this::requestResizeBoards);


    }


    /**
     * Creates a styled label for a player's name header box.
     */
    private JLabel createPlayerBoxLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 20));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(60, 60, 80));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        return lbl;
    }

    /**
     * Applies consistent styling to control buttons.
     */
    private void styleControlButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(3, 12, 3, 12));
    }

    /**
     * Builds the row of heart icons according to the starting number of lives.
     */
    private void buildHearts() {
        heartLabels = new ArrayList<>();
        int maxLives = controller.getMaxLives();

        heartsPanel.removeAll();
        for (int i = 0; i < maxLives; i++) {
            JLabel heart = new JLabel("❤");
            heart.setFont(new Font("Dialog", Font.PLAIN, 22));
            heart.setForeground(Color.RED);
            heartLabels.add(heart);
            heartsPanel.add(heart);
        }
        heartsPanel.revalidate();
        heartsPanel.repaint();
    }

    /**
     * Called after each move from a BoardPanel.
     * Updates status, handles game over, and switches turns when appropriate.
     */
    // endedTurn = true → revealed a cell, switch player (after small delay)
// endedTurn = false → only flag, same player continues
    private void handleMoveMade(boolean endedTurn) {
        updateStatus();

        String outcomeMessage = controller.getAndClearLastActionMessage();
        if (outcomeMessage != null) {
            displayOutcomePopup(outcomeMessage);
        }

        if (controller.isGameOver()) {
            handleGameOverUI();
            return;
        }

        if (endedTurn && controller.isGameRunning()) {
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();

            Timer delayTimer = new Timer(500, e -> {
                controller.processTurnEnd();
                updateTurnUI();
                boardPanel1.refresh();
                boardPanel2.refresh();
            });
            delayTimer.setRepeats(false);
            delayTimer.start();

        } else {
            updateTurnUI();
            boardPanel1.refresh();
            boardPanel2.refresh();
        }
    }


    /**
     * Displays a dialog at the end of the game (victory or game over) with final score.
     */
    private void showGameOverDialog() {
        String title;
        String message;

        // אם אין יותר לבבות – הפסד
        if (controller.getSharedLives() <= 0) {
            title = "Game Over";
            message = "All lives are gone.\nFinal score: " + controller.getSharedScore();
        } else {
            // אחרת – הנחנו שכל הלוחות נפתרו -> ניצחון
            title = "Victory!";
            message = "All safe cells are revealed!\nFinal score: " + controller.getSharedScore();
        }

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Updates score, lives, mines-left labels and heart colors.
     */
    public void updateStatus() {
        lblMinesLeft1.setText("MINES LEFT: " + controller.getMinesLeft(1));
        lblMinesLeft2.setText("MINES LEFT: " + controller.getMinesLeft(2));
        lblScore.setText("SCORE: " + controller.getSharedScore());
        lblLives.setText("LIVES: " + controller.getSharedLives() + "/" +
                controller.getMaxLives());
        updateHearts();
        revalidate();
        repaint();
    }

    /**
     * Displays the result of the Surprise tile.
     */
    private void displayOutcomePopup(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Message",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show "WAIT FOR YOUR TURN" on the board that is not active.
     */
    private void updateTurnUI() {
        int current = controller.getCurrentPlayerTurn();
        boardPanel1.setWaiting(current != 1);
        boardPanel2.setWaiting(current != 2);

        playerBox1.setActive(current == 1);
        playerBox2.setActive(current == 2);
    }

    /**
     * Colors heart icons according to current lives.
     */
    private void updateHearts() {
        int lives = controller.getSharedLives();
        int max = controller.getMaxLives();

        for (int i = 0; i < max && i < heartLabels.size(); i++) {
            JLabel heart = heartLabels.get(i);
            if (i < lives) {
                heart.setForeground(Color.RED);
            } else {
                heart.setForeground(Color.DARK_GRAY);
            }
        }
    }

    /**
     * Handles the visual changes and dialog when the game ends.
     */
    private void handleGameOverUI() {
        boardPanel1.setWaiting(true);
        boardPanel2.setWaiting(true);
        boardPanel1.refresh();
        boardPanel2.refresh();

        boolean isWin = controller.getCurrentGame().getGameState() == Model.GameState.WON;
        String title = isWin ? "Congratulations, You Won!" : "Game Over";
        String message;

        if (isWin) {
            message = String.format(
                    "VICTORY!\n\nFinal Score: %d\n\nPlease use the buttons below to Restart or Exit.",
                    controller.getSharedScore()
            );
        } else {
            message = String.format(
                    "GAME OVER!\n\nFinal Score: %d\n\nPlease use the buttons below to Restart or Exit.",
                    controller.getSharedScore()
            );
        }

        JOptionPane.showMessageDialog(
                this,
                message,
                title,
                isWin ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
        );

        long durationSeconds = (System.currentTimeMillis() - startTimeMillis) / 1000L;
        controller.recordFinishedGame(player1Name, player2Name, durationSeconds);
    }

    private void resizeBoardsToFit() {
        if (wrap1 == null || wrap2 == null) return;

        int windowHeight = getHeight();
        String difficulty = controller.getDifficultyName();

        int baseCell;

        // Different strategy: set FIXED sizes per difficulty
        switch (difficulty) {
            case "EASY" -> {
                // Easy: nice big cells
                if (windowHeight < 650) baseCell = 48;
                else if (windowHeight < 800) baseCell = 42;
                else baseCell = 36;
            }
            case "MEDIUM" -> {
                // Medium: same as Easy (both have similar grid sizes)
                if (windowHeight < 650) baseCell = 35;
                else if (windowHeight < 800) baseCell = 32;
                else baseCell = 28;
            }
            case "HARD" -> {
                // Hard: slightly smaller (16x16 grid needs to fit)
                if (windowHeight < 650) baseCell = 28;
                else if (windowHeight < 800) baseCell = 26;
                else baseCell = 24;
            }
            default -> baseCell = 36;
        }

        // No capping needed - we're using reasonable values
        boardPanel1.setCellSize(baseCell);
        boardPanel2.setCellSize(baseCell);
    }






    private void updateCenterPadding() {
        // Minimal padding to maximize board space
        int top = 15;
        int side = 15;
        int bottom = 0;

        // In large screens - almost no padding
        if (getHeight() > 800) {
            top = 5;
            side = 10;
            bottom = 0;
        }

        centerPanel.setBorder(
                BorderFactory.createEmptyBorder(top, side, bottom, side)
        );
    }


    private void requestResizeBoards() {
        if (resizeStabilizer != null && resizeStabilizer.isRunning()) {
            resizeStabilizer.restart();
            return;
        }

        resizeStabilizer = new Timer(40, e -> {
            if (wrap1 == null || wrap2 == null || boardPanel1 == null || boardPanel2 == null) return;

            // מחכים שה-layout באמת יתייצב
            if (wrap1.getWidth() <= 0 || wrap1.getHeight() <= 0 ||
                    wrap2.getWidth() <= 0 || wrap2.getHeight() <= 0) {
                return;
            }

            updateCenterPadding();
            resizeBoardsToFit();
            boardPanel1.refresh();
            boardPanel2.refresh();

            ((Timer) e.getSource()).stop();
        });

        resizeStabilizer.setRepeats(true);
        resizeStabilizer.start();
    }




}