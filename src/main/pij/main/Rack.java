package pij.main;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a player's rack of tiles.
 * <p>
 * A rack holds up to 7 tiles, allows tiles to be played. (removed) 
 * When a move is played, and the rack be refilled.
 */

public class Rack {
    /** Default number of tiles in a rack. */
    public static final int DEFAULT_SIZE = 7;
    /** Tiles currently in the rack. */
    private final ArrayList<Tile> tiles = new ArrayList<>();

    
    /**
     * Refills the rack from the tile bag until it reaches the 7 tiles or the bag becomes empty.
     *
     * @param bag is the tile bag to draw from.
     * @param maxSize is the maximum size of the rack. (7 tiles max per rack.)
     */
    public void refillFrom(TileBag bag, int maxSize) {
        while (tiles.size() < maxSize && !bag.isEmpty()) {
            tiles.add(bag.draw());
        }
    }

    /**
     * Returns a read-only view of the tiles in the rack.
     *
     * @return list of tiles currently held.
     */

    public List<Tile> getTiles() {
        return List.copyOf(tiles);
    }

    /**
     * Removes and returns a tile with the given letter from the rack.
     * <p>
     * Wildcards are ignored by this method.
     *
     * @param letter letter to remove (uppercase).
     * @return matching tile, or {@code null} if not found
     */

    public Tile removeLetterTile(char letter) {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (!t.isWildcard() && t.getLetter() == letter) {
                tiles.remove(i);
                return t;
            }
        }
        return null;
    }

    /**
     * Removes and returns a wildcard tile from the rack.
     *
     * @return a wildcard tile, or {@code null} if none exist.
     */

    public Tile removeWildcardTile() {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (t.isWildcard()) {
                tiles.remove(i);
                return t;
            }
        }
        return null;
    }

    /**
     * Returns a comma-separated string of tiles for display in the console.
     *
     * @return string representation of the rack
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiles.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tiles.get(i));
        }
        return sb.toString();
    }

    /**
     * Creates an empty rack.
     */
    public Rack() {
    }

    /**
     * Copy constructor.
     * <p>
     * Used by the computer player to simulate moves without modifying the real player's rack.
     *
     * @param other rack to copy.
     */
    public Rack(Rack other) {
        this.tiles.addAll(other.getTiles());
    }
}
