package pij.main;

public class Cell {
    private char letter = '\0';
    private final int letterMult;
    private final int wordMult;

    public Cell(int letterMult, int wordMult) {
        this.letterMult = letterMult;
        this.wordMult = wordMult;
    }

    public boolean isEmpty() {
        return letter == '\0';
    }

    public char getLetter() {
        return letter;
    }

    public void setLetter(char letter) {
        this.letter = letter;
    }

    public int getLetterMult() {
        return letterMult;
    }

    public int getWordMult() {
        return wordMult;
    }
}
