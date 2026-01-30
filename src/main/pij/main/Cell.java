package pij.main;

/**
 * This class represents the single squares on the board.
 */

public class Cell {

    /** Tile is placed on this cell. */
    private Tile tile;

    /** Integer for letter multiplier. */
    private final int letterMult;

    /** Integer for word multiplier. */
    private final int wordMult;

    /**
     * Creates a new board cell with given multipliers.
     *
     * @param letterMult letter multiplier (e.g. 2 for double letter).
     * @param wordMult word multiplier (e.g. 3 for triple word).
     */

    public Cell(int letterMult, int wordMult) {
        this.letterMult = letterMult;
        this.wordMult = wordMult;
    }
    
    /**
     * @return true if this cell does not currently contain a tile.
     */

    public boolean isEmpty() {
        return tile == null;
    }

    /**
     * @return the tile placed on this cell, or {@code null} if empty.
     */

    public Tile getTile() {
        return tile;
    }

    /**
     * Places a tile on this cell.
     *
     * @param tile is the tile to place
     */

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    /**
     * @return the letter multiplier for this cell
     */

    public int getLetterMult() {
        return letterMult;
    }

     /**
     * @return the word multiplier for this cell
     */

    public int getWordMult() {
        return wordMult;
    }
}
