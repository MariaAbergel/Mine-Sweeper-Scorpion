package util;

import View.IconButton;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public final class SoundToggleOverlay {

    private SoundToggleOverlay() {}

    /** Attach a sound toggle icon to any JFrame (top-right, responsive). */
    public static void attach(JFrame frame) {
        // create button
        IconButton btnSound = new IconButton("/ui/icons/sound_on.png", true);
        btnSound.setSafePadPx(3);
        btnSound.setPressedScale(1.07);

        // click behavior
        btnSound.setOnClick(() -> {
            SoundManager.toggleMute();
            updateIcon(btnSound);
        });

        // add above everything
        frame.getRootPane().getLayeredPane().add(btnSound, JLayeredPane.DRAG_LAYER);

        // layout + icon init
        updateIcon(btnSound);

        // position after shown (when sizes are real)
        SwingUtilities.invokeLater(() -> layout(frame, btnSound));

        // keep positioned on resize
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layout(frame, btnSound);
            }
        });
    }

    private static void layout(JFrame frame, IconButton btnSound) {
        JLayeredPane lp = frame.getRootPane().getLayeredPane();
        int W = lp.getWidth();
        int H = lp.getHeight();
        if (W <= 0 || H <= 0) return;

        //  fixed size so it’s identical everywhere
        int size = 50;   // change to 56/60/64/72 if you want
        int margin = 30; // same margin as before

        btnSound.setBounds(W - size - margin, margin, size, size);
        btnSound.repaint();
    }


    private static void updateIcon(IconButton btnSound) {
        if (SoundManager.isMuted()) {
            btnSound.setImage("/ui/icons/sound_off.png");
        } else {
            btnSound.setImage("/ui/icons/sound_on.png");
        }
    }
}
