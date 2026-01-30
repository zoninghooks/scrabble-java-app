package pij.main;

/**
 * Represents a move played on the board.
 */

public class Move {
    /** Word to be placed on the board includes lowercase letters for wildcards. */
    public final String word;
    /** Starting row for the word 0 based. */
    public final int row;
    /** Starting column for the word 0 based. */
    public final int col;
    /** Direction in which the word is placed. */
    public final Direction dir;

    /**
     * Creates a new move.
     *
     * @param word word to place.
     * @param row starting row (0 based).
     * @param col starting column (0 based).
     * @param dir direction of placement.
     */
    public Move(String word, int row, int col, Direction dir) {
        this.word = word;
        this.row = row;
        this.col = col;
        this.dir = dir;
    }
}
