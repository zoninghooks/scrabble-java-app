package pij.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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
        // e.g. "d7"
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
                System.out.print(cellToText(cells[r][c], CELL_W));
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
}
