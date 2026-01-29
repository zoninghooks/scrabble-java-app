package pij.main;

import java.io.PrintWriter;

public class SaveGame {
    public static void save(
        String filename,
        Board board,
        Rack rack1,
        Rack rack2, 
        TileBag bag, 
        int p1score,
        int p2score,
        int currentPlayer,
        int passStreak,
        boolean openGame,
        boolean p1Human,
        boolean p2Human
    ) throws IOException {
        try (PrintWriter out = new PrintWriter(new Filewriter(filename))) {
            out.println("SCRAEBBKLE_SAVE");
            out.println();
            out.println("MODE " + (openGame ? "open" : "closed"));
            out.println("P1 " + (p1Human? "human" : "computer"));
            out.println("CURRENT PLAYER" + currentPlayer);
            out.println("PASS_STREAK " + passStreak);
            out.println();
            out.println("SCORES");
            out.println("P1 " + p1score);
            out.println("P2 " + p2score);
            out.println();
            out.println("BOARD");
            board.writeTo(out);
            out.println();
            out.println("RACKS");
            out.println("P1 " + rack1.toSaveString());
            out.println("P2 " + rack2.toSaveString());
            out.println();
            out.println("BAG");
            out.println(bag.toSaveString);
        }
    }
}
