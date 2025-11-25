package Model;

/**
 * Represents a player in the cooperative game.
 */
public class Player {

    // Display name of the player
    private final String name;

    /**
     * Creates a new player with the given name.
     */
    public Player(String name) {
        this.name = name;
    }

    /**
     * Returns the player's name.
     */
    public String getName() {
        return name;
    }
}
