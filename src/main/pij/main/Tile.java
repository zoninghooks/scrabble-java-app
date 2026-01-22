package pij.main;

public final class Tile {
    private final char letter; 
    private final int value;

    public Tile(char letter, int value) {
        this.letter = letter;
        this.value = value;
    }

    public char getLetter() {
        return letter;
    }

    public int getValue() {
        return value;
    }

    public boolean isWildcard() {
        return letter == '_';
    }

    @Override
    public String toString() {
        return "[" + letter + value + "]";
    }
}