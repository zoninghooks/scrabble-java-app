package pij.main;

public class Cell {
    private Tile tile;
    private final int letterMult;
    private final int wordMult;

    public Cell(int letterMult, int wordMult) {
        this.letterMult = letterMult;
        this.wordMult = wordMult;
    }

    public boolean isEmpty() {
        return tile == null;
    }

    public Tile getTile() {
        return tile;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public int getLetterMult() {
        return letterMult;
    }

    public int getWordMult() {
        return wordMult;
    }
}
