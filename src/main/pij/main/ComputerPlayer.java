package pij.main;

import java.util.*;

public class ComputerPlayer {
    
    public static final class Result {
        public final Move move;
        public final Map<String, Tile> placed;
        public final int score;

        public Result(Move move, Map<String, Tile> placed, int score) {
            this.move = move;
            this.placed = placed;
            this.score = score;
        }
    }

    public static Result findBestMove(Board board, Rack rack, WordList dict) {
        List<int[]> anchors = findAnchors(board);

        int tried = 0;
        int legal =0;

        if (anchors.isEmpty()) {
            int[] start = parseLabel(board.getStartPositionLabel());
            anchors.add(start);
        }

        Result best = null;

        for (String w : dict.allWords()) {
            int len = w.length();
            if (len < 2) continue;
            if (len > Math.max(board.rows(), board.cols())) continue;

            for (Direction dir : new Direction[]{Direction.RIGHT, Direction.DOWN}) {
                for (int[] a : anchors) {
                    int ar = a[0], ac =a[1];
                    for (int i = 0; i < len; i++) {
                        int sr = ar - (dir == Direction.DOWN ? i : 0);
                        int sc = ac - (dir == Direction.RIGHT ? i : 0);

                        Move move = new Move (w, sr, sc, dir);

                        try {
                            int needed = countNeededTiles(board, move);
                            if (needed == 0 || needed > Rack.DEFAULT_SIZE) continue;
                            board.validatePlacement(move);
                            Rack tmpRack = new Rack(rack);
                            Map<String, Tile> placed = Main.takeTilesFromRackForMove(board, move, tmpRack);
                            board.validateWords(move, placed, dict);
                            int score = board.scoreMove(move, placed);

                            if (best == null || score > best.score) {
                                best = new Result(move, placed, score);
                            }
                        } catch (RuntimeException ex) {
                        }
                    }
                }
            }
        } 
        System.out.println("Computer tried = " + tried + " legal = " + legal + " best = " + (best==null ? "null" : best.move.word));
        return best;
    }

    //helpers

    private static List<int[]> findAnchors (Board board) {
        ArrayList<int[]> out = new ArrayList<>();
        int n = board.rows();
        int m = board.cols();

        boolean anyTile = false;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isEmpty()) continue; 
                if (hasAdjacentTile(board, r, c, n, m)) {
                    out.add(new int[]{r, c});
                }
            }
        }
        return out;
    }

    private static boolean hasAdjacentTile(Board board, int r, int c, int n, int m) {
        if (r > 0 && !board.getCell(r - 1, c).isEmpty ()) return true;
        if (r + 1 < n && !board.getCell(r + 1, c).isEmpty()) return true;
        if (c > 0 && !board.getCell(r, c - 1).isEmpty()) return true;
        if (c + 1 < m && !board.getCell(r, c + 1).isEmpty()) return true;
        return false;
    }
    
    private static int[] parseLabel(String s) {
        s = s.trim().toLowerCase();
        int col = s.charAt(0) - 'a';
        int row = Integer.parseInt(s.substring(1)) - 1;
        return new int[]{row, col};
    }

    private static int countNeededTiles(Board board, Move move) {
        int r = move.row;
        int c = move.col;
        int needed = 0;

        for (int i = 0; i < move.word.length(); i++) {
            if (r < 0 || r >= board.rows() || c < 0 || c >= board.cols()) return Integer.MAX_VALUE;
            if (board.getCell(r,c).isEmpty()) needed++;

            if (move.dir == Direction.RIGHT) c++;
            else r++;
        }
        return needed;
    }
}

