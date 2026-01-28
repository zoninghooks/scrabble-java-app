package pij.main;

import java.util.*;

public class ComputerPlayer {
    
    public static final class Result {
        public final Move move;
        public final Map<String, Tile> placed;
        public final int score;

        public Result(Move move, Map<String, Tile>, int score) {
            this.move = move;
            this.placed = placed;
            this.score = score;
        }
    }
}
