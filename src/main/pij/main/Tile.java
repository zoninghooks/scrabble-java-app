package pij.main;

public final class Tile {
    private final char letter; 
    private final int value;
    private char assigned = '\0';

    public Tile(char letter, int value) {
        this.letter = letter;
        this.value = value;
    }

    public boolean isWildcard() {
        return letter == '_';
    }

    public int getValue() {
        return value;
    }

    public char getBoardLetter() {
        if (isWildcard()) return assigned;
        return letter;
    }

    public void assign(char upperLetter) {
        if (!isWildcard()) return;
        this.assigned = upperLetter;
    }
    
    public char getLetter() {
        return letter;
    }

    @Override
    public String toString() {
        return "[" + (isWildcard() ? "_" : letter) + value + "]";
    }

    public char getDisplayChar() {
        if (isWildcard()) {
            return Character.toLowerCase(assigned);
        }
        return letter;
    }

    public String toBoardString() {
        if (isWildcard()) {
            char shown = (assigned == '\0') ? '_' : Character.toLowerCase(assigned);
            return "" + shown + value;
        }
        return "" + letter + value;
    }

    public String toSaveString() {
        if (isWildcard()) {
            return "_" + getAssignedLetter();
        }
        return "" + getBoardLetter();
    }
}