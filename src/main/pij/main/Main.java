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

        System.out.println("It's your turn, Player 1! Your tiles:");
        System.out.println(rack1);

        System.out.println();
        board.print();
        sc.close();
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
}
