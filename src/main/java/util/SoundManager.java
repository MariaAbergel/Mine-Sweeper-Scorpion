package util;

import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {

    private static Clip clip;
    private static boolean muted = false;
    private static String currentTrack;

    private static final float DEFAULT_VOLUME_DB = -22.0f; // calm background volume

    /** Play background music in a loop (only once) */
    public static void playLoop(String resourcePath) {
        try {
            // If same track already playing → do nothing
            if (clip != null && clip.isRunning() && resourcePath.equals(currentTrack)) {
                return;
            }

            stop(); // stop previous track
            currentTrack = resourcePath;

            URL url = SoundManager.class.getResource(resourcePath);
            if (url == null) {
                System.err.println("Sound not found: " + resourcePath);
                return;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(ais);

            setVolume(DEFAULT_VOLUME_DB);

            if (!muted) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Stop music completely */
    public static void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
    }

    /** Toggle mute ON / OFF */
    public static void toggleMute() {
        muted = !muted;

        if (clip == null) return;

        if (muted) {
            clip.stop();
        } else {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    /** Is sound muted? */
    public static boolean isMuted() {
        return muted;
    }

    /** Adjust volume safely */
    private static void setVolume(float db) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(db);
        }
    }
}
