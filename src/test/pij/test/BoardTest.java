package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import pij.main.*;

public class BoardTest {

    @TempDir // Creates temporary file
    Path tempDir;

    // Helps to create the file inside the temp directory, write the content inside and returns the file path.
    private Path writeFile(String name, String content) throws IOException {
        Path p = tempDir.resolve(name);
        Files.writeString(p, content);
        return p;
    }

    /*
     * Creates a valid board file with size 16x12 (192 cells) due to BoardValidator being m * n >= 192.
     * The first row can include multipliers, and all remaining rows are normal cells.
     */
    private Board makeBoardWithFirstRow(String firstRow16Cells) throws IOException {
        int m = 16;
        int n = 12;

        StringBuilder sb = new StringBuilder();
        sb.append(m).append("\n");
        sb.append(n).append("\n");
        sb.append("a1").append("\n");
        sb.append(firstRow16Cells).append("\n");
        String normal = ".".repeat(m);
        for (int r = 1; r < n; r++) sb.append(normal).append("\n");
        Path f = writeFile("board.txt", sb.toString());
        return new Board(f.toAbsolutePath().toString());
    }

    // Creates a temporary word list file and loads it into a WordList object.
    private WordList makeWordList(String... words) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String w : words) sb.append(w).append("\n");
        Path f = writeFile("words.txt", sb.toString());
        return new WordList(f.toAbsolutePath().toString());
    }

    // Helper method for creating the map of placed tiles required by scoreMove and validateWords.
    // Each tile is mapped using the key format "row,col".
    private static Map<String, Tile> placed(Object... triples) {
        Map<String, Tile> map = new HashMap<>();
        for (int i = 0; i < triples.length; i += 3) {
            int r = (int) triples[i];
            int c = (int) triples[i + 1];
            Tile t = (Tile) triples[i + 2];
            map.put(r + "," + c, t);
        }
        return map;
    }

    @Test // Checks that the board loads correctly and reads its dimensions and start position.
    void loadsBoardAndStartLabel() throws IOException {
        Board b = makeBoardWithFirstRow(".".repeat(16));
        assertEquals(12, b.rows());
        assertEquals(16, b.cols());
        assertEquals("a1", b.getStartPositionLabel());
    }

    @Test // Checks that letter and word multipliers are parsed correctly from the board file.
    void parsesLetterAndWordMultipliers() throws IOException {
        Board b = makeBoardWithFirstRow("[2]<3>" + ".".repeat(14));
        assertEquals(2, b.getCell(0, 0).getLetterMult());
        assertEquals(1, b.getCell(0, 0).getWordMult());
        assertEquals(1, b.getCell(0, 1).getLetterMult());
        assertEquals(3, b.getCell(0, 1).getWordMult());
    }

    @Test // If a row does not contain the expected number of cells, the board file should be rejected.
    void badRowLengthThrows() throws IOException {
        String badRow = ".".repeat(15); // Only 15 cells instead of 16
        StringBuilder sb = new StringBuilder();
        sb.append("16\n12\na1\n");
        sb.append(badRow).append("\n");
        String normal = ".".repeat(16);
        for (int r = 1; r < 12; r++) sb.append(normal).append("\n");
        Path f = writeFile("badBoard.txt", sb.toString());
        assertThrows(RuntimeException.class, () -> new Board(f.toAbsolutePath().toString()));
    }

    @Test // Checks that placing a word outside the board boundaries is not allowed.
    void validatePlacementOutOfBoundsThrows() throws IOException {
        Board b = makeBoardWithFirstRow(".".repeat(16));
        Move mv = new Move("HELLO", 0, 14, Direction.RIGHT);
        assertThrows(IllegalArgumentException.class, () -> b.validatePlacement(mv));
    }

    @Test // Checks that placing a conflicting letter on an occupied cell is rejected.
    void validatePlacementDetectsConflictWithExistingTile() throws IOException {
        Board b = makeBoardWithFirstRow(".".repeat(16));
        b.getCell(0, 0).setTile(new Tile('A', 1));
        Move mv = new Move("B", 0, 0, Direction.RIGHT);
        assertThrows(IllegalArgumentException.class, () -> b.validatePlacement(mv));
    }

    @Test // Checks that scoring applies letter and word multipliers correctly for newly placed tiles.
    void scoreMoveAppliesMultipliersToNewTilesOnly() throws IOException {
        Board b = makeBoardWithFirstRow("[2]<3>" + ".".repeat(14));
        Move mv = new Move("AB", 0, 0, Direction.RIGHT);
        Map<String, Tile> placed = placed(
                0, 0, new Tile('A', 1),
                0, 1, new Tile('B', 3)
        );
        assertEquals(15, b.scoreMove(mv, placed));
    }

    @Test // Checks that a main word not found in the dictionary is rejected.
    void validateWordsRejectsMainWordNotInDictionary() throws IOException {
        Board b = makeBoardWithFirstRow(".".repeat(16));
        WordList dict = makeWordList("HELLO");

        Move mv = new Move("NOPE", 0, 0, Direction.RIGHT);
        Map<String, Tile> placed = placed(
                0, 0, new Tile('N', 1),
                0, 1, new Tile('O', 1),
                0, 2, new Tile('P', 1),
                0, 3, new Tile('E', 1)
        );
        assertThrows(IllegalArgumentException.class, () -> b.validateWords(mv, placed, dict));
    }

    @Test // Checks that an invalid cross-word formed by a move is rejected.
    void validateWordsRejectsInvalidCrossWord() throws IOException {
        Board b = makeBoardWithFirstRow(".".repeat(16));
        WordList dict = makeWordList("CAT");
        b.getCell(0, 0).setTile(new Tile('A', 1));
        Move mv = new Move("B", 1, 0, Direction.RIGHT);
        Map<String, Tile> placed = placed(1, 0, new Tile('B', 3));
        assertThrows(IllegalArgumentException.class, () -> b.validateWords(mv, placed, dict));
    }
}