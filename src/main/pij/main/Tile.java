package pij.main;

/**
 * This class represents a single tile in the game.
 * <p>
 * A tile has a letter and a point value.
 * Wildcard tiles use {@code '_'} as their letter and can be assigned a letter when played by typing a letter in lowercase.
 */
public final class Tile {
    /** Base letter of the tile (use '_' for wildcard). */
    private final char letter; 
    /** Point value of the tile. */
    private final int value;
    /** Assigned letter for wildcard tiles (uppercase), or '\0' (null) if unassigned. */
    private char assigned = '\0';

    /**
     * Creates a new tile.
     *
     * @param letter tile letter (use '_' for wildcard).
     * @param value point value of the tile.
     */
    public Tile(char letter, int value) {
        this.letter = letter;
        this.value = value;
    }

    /**
     * @return true if this tile is a wildcard.
     */
    public boolean isWildcard() {
        return letter == '_';
    }

    /**
     * @return points value of the tile.
     */
    public int getValue() {
        return value;
    }

    public char getBoardLetter() {
        if (isWildcard()) return assigned;
        return letter;
    }

    /**
     * Returns the letter that this tile represents on the board.
     * <p>
     * For normal tiles, this is the tile's own letter.
     * For wildcard tiles, this is the letter that was assigned when the tile was played.
     *
     * @return the uppercase letter treated as placed on the board.
     */
    public void assign(char upperLetter) {
        if (!isWildcard()) return;
        this.assigned = upperLetter;
    }
    
    /**
     * @return the tile's original letter ('_' for wildcard)
     */
    public char getLetter() {
        return letter;
    }
    
    /**
     * Returns an organised string representation of tiles for display in the player's rack.
     *
     * @return string such as {@code [A1]} or {@code [_8]}
     */
    @Override
    public String toString() {
        return "[" + (isWildcard() ? "_" : letter) + value + "]";
    }

    /**
     * What (single) letter do I show here? (wildcards are in lowercase)
     * 
     * @return display character
     */
    public char getDisplayChar() {
        if (isWildcard()) {
            return Character.toLowerCase(assigned);
        }
        return letter;
    }

    /**
     * Returns the string representation used when printing the board.
     * <p>
     * Wildcards display their assigned letter (lowercase) and value. {@code "[_8]"}
     *
     * @return board display string
     */
    public String toBoardString() {
        if (isWildcard()) {
            char shown = (assigned == '\0') ? '_' : Character.toLowerCase(assigned);
            return "" + shown + value;
        }
        return "" + letter + value;
    }
}