package View;

import Controller.GameController;
import Model.Difficulty;
import Model.Game;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame implements StartPanel.StartGameListener {

    private final GameController controller;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private StartPanel startPanel;
    private GamePanel gamePanel;

    public MainFrame() {
        super("Scorpion Minesweeper");

        this.controller = new GameController();
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);

        // ----- create screens -----
        startPanel = new StartPanel(this); // this implements StartGameListener

        cardPanel.add(startPanel, "START");

        setContentPane(cardPanel);

        // ----- frame settings -----
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Called by StartPanel when the user clicks "Start Game".
     */
    @Override
    public void onStartGame(String player1Name, String player2Name, Difficulty difficulty) {
        controller.startNewGame(difficulty);
        Game game = controller.getCurrentGame();

        gamePanel = new GamePanel(controller, game, player1Name, player2Name);
        cardPanel.add(gamePanel, "GAME");
        cardLayout.show(cardPanel, "GAME");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
