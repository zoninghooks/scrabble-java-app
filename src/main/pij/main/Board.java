package pij.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Map;

/**
 * This is the Scrabble style board class.
 * <p>
 * The board is loaded from a text file. 
 * (By default it is /resources/defaultBoard.txt users can manually create their own custom boards too, 
 *  the size of the board and starting position can be changed. An example is customBoard.txt)
 * 
 * Each cell can have: 
 * <ul>
 *     <li> A letter multiplier, increases the score of the individual letter placed on that cell, but only for the turn when the tile is first placed there. </li>
 *     <li> A word multiplier, affects the total score of the entire word placed on that cell by multiplying the final word score. </li>
 *     <li> An optional placed Tile, a cell may contain a tile that has already been played, in which case it contributes its letter and value to future words 
           but no longer applies any multipliers. </li>
 * </ul>
 * This class also contains the main game logic for placement validation, word validation (including cross-words), scoring, and printing.
 */

public class Board {

    private final int m; // columns
    private final int n; // rows
    private final Cell[][] cells; // 2d grid of cells 

    /**
    * @return number of rows on the board
    */
    public int rows() {return n;}

    /**
    * @return number of columns on the board
    */
    public int cols() {return m;}

    /** Number of rows and columns on the board. (0 based) */
    private int startRow;
    private int startCol;

    /**
     * Loads a board from a file.
     * 
     * Expected file format:
     * <ol>
     *   <li> Number of columns. (m) </li>
     *   <li> Number of rows. (n) </li>
     *   <li> Start position square. (e.g. {@code a1}) </li>
     *   <li> n lines describing the board layout </li>
     * </ol>
     * 
     * Cell encoding within each row:
     * <ul>
     *   <li> {@code .} means that it is normal cell</li>
     *   <li> {@code [x]} letter multiplier x (e.g. {@code [2]} means doubling the letter score.) </li>
     *   <li> {@code <x>} word multiplier x (e.g. {@code <3>} means tripling the letter score.) </li>
     * </ul>
     *
     * @param filename board file name (Searches for the resources folder then the file.)
     * @throws RuntimeException if the file cannot be read or parsed.
     */

    public Board(String filename) {
        try (InputStream in = openBoardStream(filename);
             Scanner sc = new Scanner(in)) {

            this.m = Integer.parseInt(sc.nextLine().trim());
            this.n = Integer.parseInt(sc.nextLine().trim());

            BoardValidator.validate(m, n);

            parseStartSquare(sc.nextLine().trim());

            this.cells = new Cell[n][m];
            for (int r = 0; r < n; r++) {
                parseRow(sc.nextLine(), r);
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not open board file: " + filename, e);
        }
    }

    /**
     * Attempts to open a board file from multiple locations:
     * <ol>
     *   <li> classpath resource: {@code filename} </li>
     *   <li> classpath resource: {@code resources/filename} </li>
     *   <li> filesystem path: {@code resources/filename} </li>
     * </ol>
     *
     * @param filename of the board.
     * @return input stream for the board file.
     * @throws IOException if the file exists but cannot be opened.
     * @throws FileNotFoundException if the file cannot be found anywhere.
     */

    // Search for the defaultBoard.txt file

    private InputStream openBoardStream(String filename) throws IOException {

        InputStream in = Board.class.getClassLoader().getResourceAsStream(filename);
        if (in != null) return in;

        in = Board.class.getClassLoader().getResourceAsStream("resources/" + filename);
        if (in != null) return in;

        Path p = Paths.get("resources").resolve(filename);
        if (Files.exists(p)) return Files.newInputStream(p);

        throw new FileNotFoundException("Could not find " + filename);
    }

    
    /**
     * Parses a start square label like {@code a1} into 0 based coordinates.
     *
     * @param s start square string (letter then number).
     * @throws NumberFormatException if the number portion cannot be parsed by someone typing something like "aX", "a", "a-1", etc.
     */

    private void parseStartSquare(String s) {
        this.startCol = s.charAt(0) - 'a';
        this.startRow = Integer.parseInt(s.substring(1)) - 1;
    }

    /**
     * Parses a single row of the board definition file into {@link Cell} objects.
     *
     * @param line row definition line (containing either '.', '[x]', '<x>' tokens.)
     * @param row 0 based row index being filled.
     * @throws IllegalArgumentException if an unknown token is found.
     * @throws IllegalStateException if the row does not contain exactly {@code m} cells.
     */

    private void parseRow(String line, int row) {
        int col = 0;

        for (int i = 0; i < line.length();) {
            char ch = line.charAt(i);

            if (ch == '.') {
                cells[row][col++] = new Cell(1, 1);
                i++;
            } else if (ch == '[') { // letter multiplier [x]
                int end = line.indexOf(']', i);
                int lm = Integer.parseInt(line.substring(i + 1, end));
                cells[row][col++] = new Cell(lm, 1);
                i = end + 1;
            } else if (ch == '<') { // word multiplier <x>
                int end = line.indexOf('>', i);
                int wm = Integer.parseInt(line.substring(i + 1, end));
                cells[row][col++] = new Cell(1, wm);
                i = end + 1;
            } else {
                throw new IllegalArgumentException(
                        "Invalid character '" + ch + "' in row " + (row + 1));
            }
        }

        if (col != m) {
            throw new IllegalStateException(
                    "Row " + (row + 1) + " has " + col + " cells, expected " + m);
        }
    }

    /**
     * Returns the board cell at a given position.
     *
     * @param row 0 based row index.
     * @param col 0 based column index.
     * @return the {@link Cell} at that position.
     */

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    
    /**
     * Places a move onto the board using the provided tile map.
     * <p>
     * Only empty cells will be filled, cells that already contain tiles must match and are left unchanged.
     *
     * @param move is the move being played, containing the word starting position and direction.
     * @param placed are the tiles to place, each tile is store using {@code "row,col"}.
     * @throws IllegalStateException if a required tile is missing from {@code placed}.
     */

    public void placeWord(Move move, Map<String, Tile> placed) {
        int r = move.row;
        int c = move.col;

        for (int i = 0; i < move.word.length(); i++) {
            Cell cell = cells[r][c];

            if (cell.isEmpty()) {
                String key = r + "," + c;
                Tile t = placed.get(key);
                if (t == null) throw new IllegalStateException("Missing tile for " + key);
                cell.setTile(t);
            }

            if (move.dir == Direction.RIGHT) c++; 
            else r++;
        }
    }
    
    /**
     * Checks where the word is placed on the board and validates that the move follows placement rules (bounds, overlap, and connectivity).
     * <p>
     * Uses a preview (simulated) map of placed tiles so this can be checked before permanently placing tiles.
     *
     * @param move proposed move (contains the word, starting position and direction right or down).
     * @param placed preview tiles by {@code "row,col"} (typically only newly placed tiles).
     * @param dict dictionary to check words against.
     * @throws IllegalArgumentException if any created word with 2+ length is not in the dictionary.
     */

    public void validatePlacement(Move move) {
        int r = move.row;
        int c = move.col;

        boolean boardHasTiles = hasAnyPlacedTiles();
        boolean touchesExisting = false;
        boolean placedNewTile = false;
        boolean coversStart = false;

        for (int i = 0; i < move.word.length(); i++) {
            char ch = Character.toUpperCase(move.word.charAt(i));

            if (r < 0 || r >= n || c < 0 || c >= m) {
                throw new IllegalArgumentException("Move goes out of bounds.");
            }

            if (!boardHasTiles && r == startRow && c == startCol) {
                coversStart = true;
            }

            Cell cell = cells[r][c];

            if (!cell.isEmpty()) {
                if (cell.getTile().getBoardLetter() != ch) {
                    throw new IllegalArgumentException("Cell conflict at " + (char)('a' + c) + (r + 1));
                }
                touchesExisting = true;
            } else {
                placedNewTile = true;
                if (boardHasTiles && hasAdjacentTile(r, c)) {
                    touchesExisting = true;
                }
            }

            if (move.dir == Direction.RIGHT) c++;
            else r++;

        }

        if (!placedNewTile) {
            throw new IllegalArgumentException("Move must place at least one new tile.");
        }

        if (boardHasTiles && !touchesExisting) {
            throw new IllegalArgumentException("Move is not connected to existing tiles.");
        }

        if (!boardHasTiles && !coversStart) {
            throw new IllegalArgumentException(
            "First move must cover the start square " + getStartPositionLabel() + ".");
            }
    }

    public int scoreMove (Move move, Map<String, Tile> placed) {
        int r = move.row;
        int c = move.col;

        int sum = 0;
        int wordFactor = 1;
        int newTiles = 0;

        for (int i = 0; i < move.word.length(); i++){
            Cell cell = cells[r][c];

            if (!cell.isEmpty()) {
                sum += cell.getTile().getValue();
            } else{
                String key = r + "," + c;
                Tile t = placed.get(key);

                if (t == null) {
                    throw new IllegalStateException("Missing placed tile at " + key);
                }

                sum += t.getValue() * cell.getLetterMult();
                wordFactor *= cell.getWordMult();
                newTiles++;
            }

            if (move.dir == Direction.RIGHT) c++;
            else r++;
        }

        int total = sum * wordFactor;

        if (newTiles == 7) total += 60;

        return total;
    }

    /**
     * Prints the board to the console with row/column labels.
     * 
     * Empty cells display multipliers:
     * <ul>
     *   <li> {@code .} = normal, letters here have their normal score</li>
     *   <li> {@code 2.} = double letter, letters here have their score doubled </li>
     *   <li> {@code 3!} = triple word, letters here have their score tripled </li>
     *   <li> {@code 2.3!} = combined display, both letter and word multiplier. (e.g. letter x 2 and word x 3.) </li>
     * </ul>
     */

    // Printing

    public void print() {
        final int CELL_W = 4;
        int rowLabelW = Integer.toString(n).length();

        printColumnHeader(rowLabelW, CELL_W);
        System.out.println();

        for (int r = 0; r < n; r++) {
            int rowNum = r + 1;

            System.out.print(padLeft(Integer.toString(rowNum), rowLabelW));
            System.out.print("  ");

            for (int c = 0; c < m; c++) {
                Cell cell = cells[r][c];
                if (!cell.isEmpty()) {
                    System.out.print(padRight(cell.getTile().toBoardString(), CELL_W));
                } else {
                    System.out.print(cellToText(cell, CELL_W));
                }
            }

            System.out.print(" ");
            System.out.print(padLeft(Integer.toString(rowNum), rowLabelW));
            System.out.println();
        }

        System.out.println();
        printColumnHeader(rowLabelW, CELL_W);
    }

    /**
     * Prints the column letter header (a, b, c, etc) aligned above the board.
     *
     * @param rowLabelW is the width used for row labels.
     * @param cellW is the width of each printed cell.
     */

    private void printColumnHeader(int rowLabelW, int cellW) {
        System.out.print(repeat(' ', rowLabelW + 2));
        for (int c = 0; c < m; c++) {
            char colChar = (char) ('a' + c);
            System.out.print(colChar);
            System.out.print(repeat(' ', cellW - 1));
        }
        System.out.println();
    }

    /**
     * Shows the type of cell it is. e.g. Is it a normal, double, triple or combined display? 
     * <p>
     * A cell is a position that hasn't had a tile placed on it.
     *
     * @param cell cell the board cell being displayed. (it does not contain a tile).
     * @param width width the fixed width (in characters) used when printing the cell.
     * @return a string representing the cell's multipliers, padded to the given width.
     */

    private String cellToText(Cell cell, int width) {
        String s;

        if (cell.getLetterMult() != 1 && cell.getWordMult() != 1) {
            s = cell.getLetterMult() + "." + cell.getWordMult() + "!";
        } else if (cell.getLetterMult() != 1) {
            s = cell.getLetterMult() + ".";
        } else if (cell.getWordMult() != 1) {
            s = cell.getWordMult() + "!";
        } else {
            s = ".";
        }

        return padRight(s, width);
    }

    /**
     * Checks what words are formed by a move and validates that they are real dictionary words.
     * <p>
     * Builds the main word created by the move, and also builds any cross-words created where
     * newly placed tiles touch existing tiles. Only words of length 2+ are checked.
     * Uses a preview (simulated) map of placed tiles so this can be checked before permanently placing tiles.
     *
     * @param move proposed move (contains the word, starting position and direction right or down).
     * @param placed preview tiles by {@code "row,col"} (typically only newly placed tiles).
     * @param dict dictionary to check words against.
     * @throws IllegalArgumentException if the main word or any cross-word with 2+ length is not in the dictionary.
     */

    public void validateWords(Move move, Map<String, Tile> placed, WordList dict) {
        int mainDr = (move.dir == Direction.DOWN) ? 1 : 0;
        int mainDc = (move.dir == Direction.RIGHT) ? 1 : 0;

        String mainWord = buildWord(move.row, move.col, mainDr, mainDc, placed);
        if (mainWord.length() >= 2 && !dict.contains(mainWord)) {
            throw new IllegalArgumentException("Word not in dictionary: " + mainWord);
        }

        for (String posKey : placed.keySet()) {
            String[] parts = posKey.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);

            int crossDr = (move.dir == Direction.RIGHT) ? 1 : 0;
            int crossDc = (move.dir == Direction.DOWN) ? 1 : 0;

            String cross = buildWord(r, c, crossDr, crossDc, placed);

            if (cross.length() >= 2 && !dict.contains(cross)) {
                throw new IllegalArgumentException("Cross-word not in dictionary: " + cross);
            }
        }

    }

    // Helpers

    
    /**
     * Helper here is used to format top/bottom letters and numbers coordinates when printing the board
     *
     * @param ch character to repeat
     * @param count number of times
     * @return resulting string
     */

    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(ch);
        return sb.toString();
    }

    /**
     * Pads the left side of the coordinates with spaces to ensure alignment when printing the board and row labels.
     *
     * @param s original string
     * @param width desired width
     * @return left-padded string
     */

    private static String padLeft(String s, int width) {
        if (s.length() >= width) return s;
        return repeat(' ', width - s.length()) + s;
    }


    /**
     * Pads the right side of the coordinates with spaces to ensure alignment when printing the board and row labels.
     * If the string is too long, it is truncated.
     *
     * @param s original string
     * @param width desired width
     * @return right-padded or truncated string.
     */

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + repeat(' ', width - s.length());
    }

    /**
     * @return the start square label in the format e.g. {@code d7}.
     */

    public String getStartPositionLabel() {
        char col = (char) ('a' + startCol);
        int row = startRow + 1;
        return "" + col + row;
    }

    /**
     * @return true if any tiles has already been placed on the board.
     */

    private boolean hasAnyPlacedTiles() {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                if (!cells[r][c].isEmpty()) return true;
            }
        }
        return false;
    }

    /**
     * Checks the four cell neighbors (up, down, left, right) for an existing tile.
     *
     * @param r row
     * @param c column
     * @return true if any adjacent cell contains a tile.
     */

    private boolean hasAdjacentTile(int r, int c) {
        if (r > 0 && !cells[r - 1][c].isEmpty()) return true;
        if (r < n - 1 && !cells[r + 1][c].isEmpty()) return true;
        if (c > 0 && !cells[r][c - 1].isEmpty()) return true;
        if (c < m - 1 && !cells[r][c + 1].isEmpty()) return true;
        return false;
    }

    /**
     * Creates a consistent map key for tile positions.
     *
     * @param r row
     * @param c column
     * @return key in the form {@code "row,col"}.
     */

    private String key(int r, int c) {
        return r + "," + c;
    }

    /**
     * Checks whether a position should be treated as empty when validating words.
     * A position is "not empty" if it already has a tile on the board OR if the move preview places a tile there.
     *
     * @param r row
     * @param c column
     * @param placed preview placed tiles.
     * @return true if there is no existing tile and no preview tile at the position.
     */

    private boolean isEmptyAt(int r, int c, Map<String, Tile> placed) {
        if (!cells[r][c].isEmpty()) return false;
        return !placed.containsKey(key(r,c));
    }

    /**
     * Returns the letter at a given position, checking real board tiles first, otherwise using the preview map.
     *
     * @param r row
     * @param c column
     * @param placed preview placed tiles.
     * @return board letter at the position.
     * @throws IllegalArgumentException if no tile exists in either the board or preview map.
     */

    private char letterAt(int r, int c, Map<String, Tile> placed) {
        if (!cells[r][c].isEmpty()) {
            return cells[r][c].getTile().getBoardLetter();
        }
        Tile t = placed.get(key(r,c));
        if (t == null) throw new IllegalArgumentException("Missing tile at " + key(r, c));
        return t.getBoardLetter();
    }

    /**
     * Constructs the full main word or cross-word formed by a move by scanning the board in a direction, 
     * treating newly placed tiles as if they were already on the board, so the words can be validated against the dictionary.
     *
     * @param r starting row.
     * @param c starting column.
     * @param dr row step (0 or 1).
     * @param dc column step (0 or 1).
     * @param placed preview placed tiles.
     * @return the constructed word (uppercase letters).
     */

    private String buildWord(int r, int c, int dr, int dc, Map<String, Tile> placed) {
        int rr = r, cc = c;
        while (rr - dr >= 0 && rr - dr < n && cc - dc >= 0 && cc - dc < m
                && !isEmptyAt(rr - dr, cc - dc, placed)) {
            rr -= dr;
            cc -= dc;
        }
        StringBuilder sb = new StringBuilder();
        while (rr >= 0 && rr < n && cc >= 0 && cc < m && !isEmptyAt(rr, cc, placed)) {
            sb.append(letterAt(rr, cc, placed));
            rr += dr;
            cc += dc;
        }
        return sb.toString();
    }
}
