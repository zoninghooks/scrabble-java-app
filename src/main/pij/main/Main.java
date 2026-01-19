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
        
        askChoice(
            sc, 
            "Is Player 1 a human player or a computer player? " +
            "Please enter your choice (h/c): ",
            'h', 'c'
        );

        askChoice(
            sc, 
            "Is Player 2 a human player or a computer player? " +
            "Please enter your choice (h/c): ",
            'h', 'c'
        );

        askChoice(
                sc,
                "Would you like to play an open or a closed game? " +
                "Please enter your choice (o/c): ",
                'o', 'c'
        );

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
