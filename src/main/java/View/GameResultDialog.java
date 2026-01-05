package View;

import Model.Game;
import Model.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog that shows the game result (Win/Lose) and statistics.
 */
public class GameResultDialog extends JDialog {

    public enum ResultAction { RESTART, EXIT, CLOSE }
    private ResultAction action = ResultAction.CLOSE;

    public GameResultDialog(Window owner, Game game) {
        super(owner, "Game Result", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Title
        JLabel lblTitle = new JLabel(
                game.getGameState() == GameState.WON ? "🎉 You Won!" : "💥 Game Over",
                SwingConstants.CENTER
        );
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // Info
        String info = "<html>" +
                "<b>Shared Score:</b> " + game.getSharedScore() + "<br/>" +
                "<b>Shared Lives:</b> " + game.getSharedLives() + "<br/>" +
                "<b>Total Questions Answered:</b> " + game.getTotalQuestionsAnswered() + "<br/>" +
                "<b>Total Correct Answers:</b> " + game.getTotalCorrectAnswers() +
                "</html>";

        JLabel lblInfo = new JLabel(info);
        lblInfo.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(lblInfo, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRestart = new JButton("Restart");
        JButton btnExit = new JButton("Exit");
        JButton btnClose = new JButton("Close");

        btnRestart.addActionListener(e -> { action = ResultAction.RESTART; dispose(); });
        btnExit.addActionListener(e -> { action = ResultAction.EXIT; dispose(); });
        btnClose.addActionListener(e -> { action = ResultAction.CLOSE; dispose(); });

        btnPanel.add(btnRestart);
        btnPanel.add(btnClose);
        btnPanel.add(btnExit);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    public ResultAction getAction() {
        return action;
    }

    public static ResultAction showResultDialog(Window owner, Game game) {
        GameResultDialog dlg = new GameResultDialog(owner, game);
        dlg.setVisible(true);
        return dlg.getAction();
    }
}
