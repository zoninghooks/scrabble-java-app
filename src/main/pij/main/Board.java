package pij.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Map;

public class Board {

    private final int m;        // columns
    private final int n;        // rows
    private final Cell[][] cells;

    private int startRow;
    private int startCol;

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

    // Search for the defaultBoard.txt file

    private InputStream openBoardStream(String filename) throws IOException {

        InputStream in = Board.class.getClassLoader().getResourceAsStream(filename);
        if (in != null) return in;

        in = Board.class.getClassLoader().getResourceAsStream("resources/" + filename);
        if (in != null) return in;

        Path here = Paths.get("").toAbsolutePath();
        for (int up = 0; up < 12 && here != null; up++, here = here.getParent()) {
            Path p1 = here.resolve("resources").resolve(filename);
            if (Files.exists(p1)) return Files.newInputStream(p1);

            Path p2 = here.resolve("src").resolve("main").resolve("resources").resolve(filename);
            if (Files.exists(p2)) return Files.newInputStream(p2);
        }

        throw new FileNotFoundException("Could not find " + filename);
    }

    private void parseStartSquare(String s) {
        this.startCol = s.charAt(0) - 'a';
        this.startRow = Integer.parseInt(s.substring(1)) - 1;
    }

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

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

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
    
    public void validatePlacement(Move move) {
        int r = move.row;
        int c = move.col;

        boolean boardHasTiles = hasAnyPlacedTiles();
        boolean touchesExisting = false;
        boolean placedNewTile = false;

        for (int i = 0; i < move.word.length(); i++) {
            char ch = Character.toUpperCase(move.word.charAt(i));

            if (r < 0 || r >= n || c < 0 || c >=m) {
                throw new IllegalArgumentException("Move goes out of bounds.");
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

            if (!placedNewTile) {
                throw new IllegalArgumentException("Move must place at least one new tile.");
            }

            if (boardHasTiles && !touchesExisting) {
                throw new IllegalArgumentException("Move is not connected to existing tiles.");
            }
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
                    System.out.print(padRight(String.valueOf(cell.getTile()), CELL_W));
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

    private void printColumnHeader(int rowLabelW, int cellW) {
        System.out.print(repeat(' ', rowLabelW + 2));
        for (int c = 0; c < m; c++) {
            char colChar = (char) ('a' + c);
            System.out.print(colChar);
            System.out.print(repeat(' ', cellW - 1));
        }
        System.out.println();
    }

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

    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(ch);
        return sb.toString();
    }

    private static String padLeft(String s, int width) {
        if (s.length() >= width) return s;
        return repeat(' ', width - s.length()) + s;
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + repeat(' ', width - s.length());
    }

    public String getStartPositionLabel() {
        char col = (char) ('a' + startCol);
        int row = startRow + 1;
        return "" + col + row;
    }

    private boolean hasAnyPlacedTiles() {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                if (!cells[r][c].isEmpty()) return true;
            }
        }
        return false;
    }

    private boolean hasAdjacentTile(int r, int c) {
        if (r > 0 && !cells[r - 1][c].isEmpty()) return true;
        if (r < n - 1 && !cells[r + 1][c].isEmpty()) return true;
        if (c > 0 && !cells[r][c - 1].isEmpty()) return true;
        if (c < m - 1 && !cells[r][c + 1].isEmpty()) return true;
        return false;
    }

    private String key(int r, int c) {
        return r + "," + c;
    }

    private boolean isEmptyAt(int r, int c, Map<String, Tile> placed) {
        if (!cells[r][c].isEmpty()) return false;
        return !placed.containsKey(key(r,c));
    }

    private char letterAt(int r, int c, Map<String, Tile> placed) {
        if (!cells[r][c].isEmpty()) {
            return cells[r][c].getTile().getBoardLetter();
        }
        Tile t = placed.get(key(r,c));
        if (t == null) throw new IllegalArgumentException("Missing tile at " + key(r, c));
        return t.getBoardLetter();
    }

    private String buildWord(int r, int c, int dr, int dc, Map<String, Tile> placed) {
        int rr = r, cc = c;
        while (rr - dr >= 0 && rr - dr < n && cc - dc >= 0 && cc - dc < m
            && !isEmptyAt(rr - dc, cc - dc, placed)) {
                rr -= dr;
                cc -= dc;
            }
        StringBuilder sb = new StringBuilder();
        while (rr >= 0 && rr < n & cc >= 0 && cc < m && !isEmptyAt(rr, cc, placed)) {
            sb.append(letterAt(rr, cc, placed));
            rr += dr;
            cc += dc;
        }
        return sb.toString();
    }
}
