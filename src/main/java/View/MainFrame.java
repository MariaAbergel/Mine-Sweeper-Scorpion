package View;

import Controller.GameController;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame.
 * Manages screen navigation between StartPanel and GamePanel using CardLayout.
 * Communicates with the Model layer only through GameController.
 */
public class MainFrame extends JFrame implements StartPanel.StartGameListener {

    private final GameController controller;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private StartPanel startPanel;
    private GamePanel gamePanel;
    /**
     * Initializes the main window and loads the initial start screen.
     */
    public MainFrame() {
        super("Scorpion Minesweeper");

        this.controller = GameController.getInstance();
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        // create screens
        startPanel = new StartPanel(this);
        cardPanel.add(startPanel, "START");

        setContentPane(cardPanel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Callback from StartPanel when the user starts a new game.
     * Initializes the game in the controller and switches to the GamePanel.
     */
    @Override
    public void onStartGame(String player1Name, String player2Name, String difficultyKey) {
        controller.startNewGame(difficultyKey);

        gamePanel = new GamePanel(controller, player1Name, player2Name);
        cardPanel.add(gamePanel, "GAME");
        cardLayout.show(cardPanel, "GAME");
    }
    /**
     * Application entry point. Launches the main frame on the EDT.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
