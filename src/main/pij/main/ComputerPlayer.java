package pij.main;

import java.util.*;

/**
 * This class is a simple AI player that searches the dictionary for a legal move and picks the highest scoring one.
 * <p>
 * Strategy:
 * <ul>
 *   <li> Find "anchor" squares (empty board square that is directly next to an already-placed tile (up, down, left, or right)). </li>
 *   <li> For each dictionary word, try placing it across each anchor in both directions (Down and right). </li>
 *   <li> Quickly skip placements that can't be covered by the rack. (To prevent bruteforcing the entire wordslist.txt) </li>
 *   <li> Validate placement rules and dictionary legality, including cross words. </li>
 *   <li> Score the move and keep the best-scoring result. </li>
 * </ul>
 * This is a brute-force approach.
 */

public class ComputerPlayer {
    
    /**
     * Holds the result of the computer's move search:
     * The chosen move, the tiles placed, and the resulting score.
     */

    public static final class Result {
        /** Proposed move (word, start square, direction). */
        public final Move move;
        /** Tiles to place for the move, keyed by {@code "row,col"}. */
        public final Map<String, Tile> placed;
        /** Score that would be gained by playing this move. */
        public final int score;

        
        /**
         * Creates a result object.
         *
         * @param move is the move found.
         * @param placed is tiles that would be placed for the move.
         * @param score is score for the move.
         */
        public Result(Move move, Map<String, Tile> placed, int score) {
            this.move = move;
            this.placed = placed;
            this.score = score;
        }
    }

    /**
     * Finds the best scoring legal move for the current rack.
     * <p>
     * If the board is empty (no anchors found), the start square is used as an anchor.
     *
     * @param board is the game board.
     * @param rack is the computer player's rack.
     * @param dict is the dictionary used to generate and validate words.
     * @return best legal move found, or {@code null} if no legal move exists, it passes.
     */
    public static Result findBestMove(Board board, Rack rack, WordList dict) {
        List<int[]> anchors = findAnchors(board);

        int tried = 0;
        int legal = 0;

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
                        tried++;

                        try {
                            int needed = countNeededTiles(board, move);
                            if (needed == 0 || needed > Rack.DEFAULT_SIZE) continue;
                            if (!rackCanCover(board, move, rack)) continue; // avoid checking impossible moves to not bruteforce entire wordlist.txt
                            board.validatePlacement(move);
                            Rack tmpRack = new Rack(rack); // Use the temporary rack so the real rack does not mutate during search
                            Map<String, Tile> placed = Main.takeTilesFromRackForMove(board, move, tmpRack);
                            board.validateWords(move, placed, dict);
                            legal++;
                            int score = board.scoreMove(move, placed);

                            if (best == null || score > best.score) {
                                best = new Result(move, placed, score);
                            }
                        } catch (RuntimeException ex) {
                            // Any validation/scoring failure just means it is "not a legal move"
                        }
                    }
                }
            }
        } 
        System.out.println("Computer tried = " + tried + " legal = " + legal + " best = " + (best==null ? "null" : best.move.word));
        return best;
    }

    //helpers

    /**
     * The AI only tries moves that touch the current board. 
     * The anchor squares mark all legal connection points so the computer doesn’t waste time trying impossible placements.
     *
     * @param board is the game board.
     * @return list of coordinates as {@code int[]{row, col}}.
     */

    private static List<int[]> findAnchors (Board board) {
        ArrayList<int[]> out = new ArrayList<>();
        int n = board.rows();
        int m = board.cols();

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

    /**
     * Checks whether a square has any adjacent placed tile (up, down, left, right neighbors only).
     *
     * @param board is the game board.
     * @param r row.
     * @param c column.
     * @param n number of rows.
     * @param m number of columns.
     * @return true if any neighbor contains a tile.
     */

    private static boolean hasAdjacentTile(Board board, int r, int c, int n, int m) {
        if (r > 0 && !board.getCell(r - 1, c).isEmpty ()) return true;
        if (r + 1 < n && !board.getCell(r + 1, c).isEmpty()) return true;
        if (c > 0 && !board.getCell(r, c - 1).isEmpty()) return true;
        if (c + 1 < m && !board.getCell(r, c + 1).isEmpty()) return true;
        return false;
    }
    
    /**
     * Parses a label like {@code d7} into 0 based coordinates.
     *
     * @param s is the position label
     * @return {@code int[]{row, col}}
     */

    private static int[] parseLabel(String s) {
        s = s.trim().toLowerCase();
        int col = s.charAt(0) - 'a';
        int row = Integer.parseInt(s.substring(1)) - 1;
        return new int[]{row, col};
    }

    /**
     * Counts how many new tiles a move would need to place. If any part goes out of bounds, it returns a very large number to force skipping.
     *
     * @param board checks against the board.
     * @param move possible move.
     * @return number of new tiles required, or {@link Integer#MAX_VALUE} if out of bounds.
     */

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

    /**
     * Fast feasibility check: determines whether the rack has enough letters (including wildcards) to cover all new tiles required by a possible move.
     * <p>
     * This does not check if the placement is legal or if it is valid in the dictionary, it only answers "can the rack supply the needed letters for the possible move?"
     *
     * @param board is the game board to check against which letters would be newly placed.
     * @param move is the possible move.
     * @param rack is the computer player's rack.
     * @return true if the rack (including wildcards) can cover the required letters.
     */

    private static boolean rackCanCover(Board board, Move move, Rack rack) {
        int[] need = new int[26];

        int r = move.row, c = move.col;
        for (int i = 0; i < move.word.length(); i++) {
            if (r < 0 || r >= board.rows() || c < 0 || c >= board.cols()) return false;

            if (board.getCell(r, c).isEmpty()) {
                char ch = Character.toUpperCase(move.word.charAt(i));
                if (ch >= 'A' && ch <= 'Z') need[ch - 'A']++;
            }

            if (move.dir == Direction.RIGHT) c++;
            else r++;
        }

        int[] have = rackLetterCounts(rack);
        int wild = rackWildcardCounts(rack);

        int wildNeeded = 0;
        for (int i = 0; i < 26; i++) {
            int missing = Math.max(0, need[i] - have[i]);
            wildNeeded += missing;
        }
        return wildNeeded <= wild;
    }

    /**
     * Counts how many of each letter tile (A-Z) exist in the rack, ignoring wildcards.
     *
     * @param rack computer player's rack.
     * @return array of size 26 containing counts for A to Z.
     */
    private static int[] rackLetterCounts(Rack rack) {
        int[] cnt = new int[26];
        for (Tile t : rack.getTiles()) {
            if (t.isWildcard()) continue;
            char ch = t.getBoardLetter();
            if (ch >= 'A' && ch <= 'Z') cnt[ch - 'A']++;
        }
        return cnt;
    }

    
    /**
     * Counts how many wildcard tiles are in the rack.
     *
     * @param rack computer player's rack.
     * @return the number of wildcards.
     */
    private static int rackWildcardCounts(Rack rack) {
        int w = 0;
        for (Tile t : rack.getTiles()) if (t.isWildcard()) w++;
        return w;
    }
}

