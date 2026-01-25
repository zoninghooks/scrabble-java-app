package pij.main;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        printBanner();

        char boardChoice = askChoice(
            sc,
            "Would you like to load a board or use the default board? " +
            "Please enter your choice (l/d): ", 
            'l','d'
        );

        Board board;

        if (boardChoice == 'd'){
            board = new Board("defaultBoard.txt");
        } else {
            while (true){
                System.out.print("Please enter the file name of the board: ");
                String filename = sc.nextLine().trim();

                try {
                    board = new Board(filename);
                    break;
                } catch (RuntimeException e) {
                    System.out.println("This is not a valid file. Please enter the file name of the board:");
                }
            }   
        }   
        
        char p1Type = askChoice(
            sc, 
            "Is Player 1 a human player or a computer player? " +
            "Please enter your choice (h/c): ",
            'h', 'c'
        );

        char p2Type = askChoice(
            sc, 
            "Is Player 2 a human player or a computer player? " +
            "Please enter your choice (h/c): ",
            'h', 'c'
        );

        char openClosed = askChoice(
                sc,
                "Would you like to play an open or a closed game? " +
                "Please enter your choice (o/c): ",
                'o', 'c'
        );
        boolean openGame = (openClosed == 'o');

        System.out.println();
        board.print();

        TileBag bag = new TileBag("defaultTileBag.txt");

        Rack rack1 = new Rack();
        Rack rack2 = new Rack();
        rack1.refillFrom(bag, Rack.DEFAULT_SIZE);
        rack2.refillFrom(bag, Rack.DEFAULT_SIZE);

        System.out.println();
        System.out.println("Start position: " + board.getStartPositionLabel());

        if (openGame) {
            System.out.println("OPEN GAME: Player 2's tiles:");
            System.out.println("OPEN GAME: " + rack2);
        }

        /* 
        
        //TEMPORARY TEST

        board.getCell(3, 3).setLetter('D'); // d4
        board.getCell(3, 4).setLetter('I'); // e4
        board.getCell(3, 5).setLetter('N'); // f4
        board.getCell(3, 6).setLetter('E'); // g4
        board.getCell(3, 7).setLetter('D'); // h4

        */

        System.out.println("It's your turn, Player 1! Your tiles:");
        System.out.println(rack1);

        System.out.println("Please enter your move in the format word,square. Enter ',' to pass the turn.");
        String input = sc.nextLine().trim();

        if (!input.equals(",")){
            try {
                Move move = parseMove(input);
                board.placeWord(move);
                takeTilesFromRackForMove(board, move, rack1);
                rack1.refillFrom(bag, Rack.DEFAULT_SIZE);

                System.out.println("Your tiles are now:");
                System.out.println(rack1);

                System.out.println();
                board.print();

            } catch (IllegalArgumentException e){
                System.out.println("Invalid move: " + e.getMessage());
            }
        } else {
            System.out.println("Turn passed.");
        }
        sc.close();
    }

    private static Move parseMove(String input){
        String[] parts = input.split(",", -1);
        if (parts.length != 2) throw new IllegalArgumentException("Bad move format. Use word,square");

        String word = parts[0].trim();
        String pos = parts[1].trim().toLowerCase();

        if (word.isEmpty() || pos.isEmpty()) throw new IllegalArgumentException("Bad move format. Use word,square");

        Direction dir;
        int row, col;

        if (Character.isLetter(pos.charAt(0))) {
            dir = Direction.DOWN;
            col = pos.charAt(0) - 'a';
            row = Integer.parseInt(pos.substring(1)) - 1;
        } else {
            int i = 0;
            while (i < pos.length() && Character.isDigit(pos.charAt(i))) i++;
            if (i == 0 || i >= pos.length()) throw new IllegalArgumentException("Bad sqaure: " + pos);

            row = Integer.parseInt(pos.substring(0, i)) - 1;
            col = pos.charAt(i) - 'a';
            dir = Direction.RIGHT;
        }

        return new Move(word, row, col, dir);
    }

    private static void printBanner(){
        System.out.println();
        System.out.println("============                     ============");
        System.out.println("============ S c r a e B B K l e ============");
        System.out.println("============                     ============");
        System.out.println();
    }

    private static char askChoice(Scanner sc, String prompt, char a, char b){
        while (true){
            System.out.print(prompt);
            String input = sc.nextLine().trim().toLowerCase();

            if (input.length() == 1){
                char ch = input.charAt(0);
                if (ch == a || ch == b) {
                    return ch;
            }
        }
        System.out.println("Invalid choice. Please enter '" + a + "' or '" + b + "'.");
        }
    }

    private static void takeTilesFromRackForMove(Board board, Move move, Rack rack){
        int r = move.row;
        int c = move.col;

        for (int i = 0; i < move.word.length(); i++){
            char typed = move.word.charAt(i);
            char letter = Character.toUpperCase(typed);

            Cell cell = board.getCell(r,c);

            if (cell.isEmpty()){

                boolean ok;

                if (Character.isLowerCase(typed)) {
                    ok = rack.removeWildcard();
                } else {
                    ok = rack.removeLetter(letter);
                }

                if (!ok) {
                    throw new IllegalArgumentException("You don't have the tile needed for '" + typed + "'.");
                }
            }

            if (move.dir == Direction.RIGHT) c++;
            else r++;
        }
    }
}
