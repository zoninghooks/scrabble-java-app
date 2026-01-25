package pij.main;

public class Move {
    public final String word;
    public final int row;
    public final int col;
    public final Direction dir;

    public Move(String word, int row, int col, Direction dir) {
        this.word = word;
        this.row = row;
        this.col = col;
        this.dir = dir;
    }
}
