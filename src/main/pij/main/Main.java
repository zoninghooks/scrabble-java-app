package pij.main;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

/** 
 * This is the main console for the scrabble game.
 * This class loads the board, sets up both human and computer players, manages player's turns, updates scores.
 * This class also ends the game when both players pass.
 */

public class Main {
    /** 
     * Flow: 
     * <ul>
     *     <li> Show banner </li>
     *     <li> Asks user if they want default board or custom board to be use. </li>
     *     <li> Choose player types, human or computer. </li>
     *     <li> Sets up dictionary, tile bag and racks. </li>
     *     <li> Alternates between player 1 and player 2. </li>
     * </ul>
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println();
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
                    System.out.println("This is not a valid file. " + e.getMessage());
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
        WordList dict = new WordList("wordlist.txt");
        TileBag bag = new TileBag("defaultTileBag.txt");

        Rack rack1 = new Rack();
        Rack rack2 = new Rack();
        rack1.refillFrom(bag, Rack.DEFAULT_SIZE);
        rack2.refillFrom(bag, Rack.DEFAULT_SIZE);

        System.out.println();
        System.out.println("Start position: " + board.getStartPositionLabel());

        boolean p1Human = (p1Type == 'h');
        boolean p2Human = (p2Type == 'h');

        int p1Score = 0;
        int p2Score = 0;

        int passStreak = 0;
        int currentPlayer = 1;

        while (true) {
            int gained;

            if (currentPlayer == 1) {
                gained = playTurn(sc, "Player 1", p1Human, openGame, board, rack1, rack2, bag, dict);
                if (gained == Integer.MIN_VALUE) {
                    passStreak++;
                } else {
                    p1Score += gained;
                    passStreak = 0;
                }
            } else {
                gained = playTurn(sc, "Player 2", p2Human, openGame, board, rack2, rack1, bag, dict);
                if (gained == Integer.MIN_VALUE) {
                    passStreak++;
                } else {
                    p2Score += gained;
                    passStreak = 0;
                }
            }

            System.out.println();
            System.out.println("Player 1 score: " + p1Score);
            System.out.println("Player 2 score: " + p2Score);
            System.out.println();

            Rack currentRack = (currentPlayer == 1) ? rack1 : rack2;
            if (bag.isEmpty() && currentRack.getTiles().isEmpty()) {
                System.out.println("Game Over!");

                int p1Left = rackPoints(rack1);
                int p2Left = rackPoints(rack2);
                
                p1Score -= p1Left;
                p2Score -= p2Left;

                if (currentPlayer == 1) p1Score += p2Left;
                else p2Score += p1Left;

                System.out.println("Player 1 scored " + p1Score + " points.");
                System.out.println("Player 2 scored " + p2Score + " points.");
                if (p1Score > p2Score) System.out.println("Player 1 wins!");
                else if (p2Score > p1Score) System.out.println("Player 2 wins!");
                else System.out.println("It's a draw!");

                break;
            }

            if (passStreak >= 2) {
                System.out.println("Game Over!");
                System.out.println("Player 1 scored " + p1Score + " points.");
                System.out.println("Player 2 scored " + p2Score + " points.");

                if (p1Score > p2Score) {
                    System.out.println("Player 1 wins!");
                } else if (p2Score > p1Score) {
                    System.out.println("Player 2 wins!");
                } else {
                    System.out.println("It's a draw!");
                }
                break;
            }

            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }
        
        /* 
        
        //TEMPORARY TEST

        board.getCell(3, 3).setLetter('D'); // d4
        board.getCell(3, 4).setLetter('I'); // e4
        board.getCell(3, 5).setLetter('N'); // f4
        board.getCell(3, 6).setLetter('E'); // g4
        board.getCell(3, 7).setLetter('D'); // h4

        */
        sc.close();
    }

    /** The user can type a move in the format of {@code word,square}
     *
     * The square can be written in 2 ways:
     * <ul>
     *     <li> {@code a1} Putting the letter first then the number means that the move's direction is DOWN. </li>
     *     <li> {@code 1a} Putting the number first then the letter means that the move's direction is RIGHT. </li>
     * </ul>
     * <p>
     * Lower case letters is to indicate a wildcard is being used.
     * 
     * @param input raw user input (e.g. {@code "word,a1"} or {@code "word,1a"}).
     * @return a {@link Move} containing the word, start row/col, and direction.
     * @throws IllegalArgumentException if the user's input is not in the expected format.
     *
     */

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
            if (i == 0 || i >= pos.length()) throw new IllegalArgumentException("Bad square: " + pos);

            row = Integer.parseInt(pos.substring(0, i)) - 1;
            col = pos.charAt(i) - 'a';
            dir = Direction.RIGHT;
        }

        return new Move(word, row, col, dir);
    }

    /**
     * Prints the intro ASCII banner at the start of the program.
     */

    private static void printBanner(){
        System.out.println();
        System.out.println("============                     ============");
        System.out.println("============ S c r a e B B K l e ============");
        System.out.println("============                     ============");
        System.out.println();
    }

    /** 
     * Recursively prompts the user until they enter one of the valid options.
     * @param scanner sc used to read user input.
     * @param a is the first valid option.
     * @param b is the other valid option.
     * @return the character chosen {@code a} or {@code b}.
     */

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

    /**
     * Calculates the total point value of tiles remaining in a rack.
     * Used to determine the winner at the end of the game, when all the tiles have depleted.
     *
     * @param rack the rack to count points for
     * @return sum of tile values still in the rack
     */
    private static int rackPoints(Rack rack) {
        int sum = 0;
        for (Tile t : rack.getTiles()) {
            sum += t.getValue();
        }
        return sum;
    }


    /**
     * Removes the tiles the player has used from their move.
     *
     * The method: 
     * <ul>
     *     <li> Shows the rack (and the opponents rack too if the game is open). </li>
     *     <li> Read the users input (or the move the ComputerPlayer generated). </li>
     *     <li> Allows the player to pass go by typing {@code ","}. </li>
     *     <li> Validates the placement is legal and checks the word through the dictionary (wordslist.txt). </li>
     *     <li> Adds the score for the move then refills the now missing letters from the rack. </li>
     * </ul>
     * 
     * <p>
     * If the player inputs an invalid move they are recursively prompted until they make a valid move.
     * 
     * @param scanner sc used to read user input.
     * @param playerName label used in console output. (e.g. {@code "Player 1"})
     * @param isHuman is true if the player needs to type their move, this parameter is false for the computer player.
     * @param openGame true to show the opponent's rack each turn.
     * @param board the current board.
     * @param ownRack the current player's rack.
     * @param otherRack the other rack (opponent's rack) it is only displayed in open game.
     * @param bag is the tile bag used to refill racks after a successful move.
     * @param dict dictionary used to validate played words. (checks through wordslist.txt)
     * @return points gained for the move; {@link Integer#MIN_VALUE} if the player passed.
     */

    public static Map<String, Tile> takeTilesFromRackForMove(Board board, Move move, Rack rack){
        int r = move.row;
        int c = move.col;

        Map<String, Tile> placed = new HashMap<>();

        for (int i = 0; i < move.word.length(); i++){
            char typed = move.word.charAt(i);
            char letter = Character.toUpperCase(typed);

            Cell cell = board.getCell(r,c);

            if (cell.isEmpty()){
                Tile t;

                if (Character.isLowerCase(typed)) {
                    t = rack.removeWildcardTile();
                    if (t == null) {
                        throw new IllegalArgumentException("You don't have a wildcard");
                    }
                    t.assign(letter);
                } else {
                    t = rack.removeLetterTile(letter);
                    if (t == null) {
                        throw new IllegalArgumentException("You don't have the tile needed for '" + typed + "'.");
                    }
                }
                placed.put(r + "," + c, t);
            }

            if (move.dir == Direction.RIGHT) c++;
            else r++;
        }
        return placed;
    }

    private static int playTurn(
        Scanner sc,
        String playerName,
        boolean isHuman,
        boolean openGame,
        Board board,
        Rack ownRack,
        Rack otherRack,
        TileBag bag,
        WordList dict
    ) {
        if (openGame) {
            System.out.println("Open Game: " + (playerName.equals("Player 1") ? "Player 2" : "Player 1") + "'s tiles:");
            System.out.println("Open Game: " + otherRack);
        }

        System.out.println("It's your turn, " + playerName + " ! Your tiles:");
        System.out.println(ownRack);
        System.out.println();
        System.out.println("Please enter your move in the format: \"word,square\" (without the quotes)\r\n" + //
                        "For example, for suitable tile rack and board configuration, a downward move\r\n" + //
                        "could be \"HI,f4\" and a rightward move could be \"HI,4f\".\r\n" + //
                        "\r\n" + //
                        "In the word, upper-case letters are standard tiles and lower-case letters\r\n" + //
                        "are wildcards.\r\n" + //
                        "Entering \",\" passes the turn.");

        String input;

        if (isHuman) {
            input = sc.nextLine().trim();
        } else {
            ComputerPlayer.Result best = ComputerPlayer.findBestMove(board, ownRack, dict);
            if (best == null){
                input = ",";
                System.out.println(input);
            } else {
                Move mv = best.move;
                String square = (mv.dir == Direction.DOWN)
                ? "" + (char)('a' + mv.col) + (mv.row + 1)
                : "" + (mv.row + 1) + (char)('a' + mv.col);
                input = mv.word + "," + square;
                System.out.println(input);
            }
        }
        
        if (input.equals(",")) {
            System.out.println(playerName + " passed.");
            System.out.println();
            board.print();
            return Integer.MIN_VALUE;
        }

        try {
            Move move = parseMove(input);
            board.validatePlacement(move);
            String wordUpper = move.word.toUpperCase();
            if (!dict.contains(wordUpper)) {
                throw new IllegalArgumentException("Word not in dictionary: " + wordUpper);
            }
            Map<String, Tile> preview = previewPlacedTiles(board, move);
            board.validateWords(move, preview, dict);
            Rack testRack = new Rack(ownRack);
            takeTilesFromRackForMove(board, move, testRack);
            Map<String, Tile> placed = takeTilesFromRackForMove(board, move, ownRack);
            int gained = board.scoreMove(move, placed);
            board.placeWord(move, placed);
            ownRack.refillFrom(bag, Rack.DEFAULT_SIZE);

            System.out.println();
            board.print();
            return gained;
            
        } catch (IllegalArgumentException e) {
            if ("FORMAT".equals(e.getMessage())) {
                System.out.println("Illegal move format");
            } else {
                System.out.println("Invalid move: " + e.getMessage());
                System.out.println();
                board.print();
            }
            return playTurn(sc, playerName, isHuman, openGame, board, ownRack, otherRack, bag, dict);
        }
    }

    /** 
     * The preview map is used to check that a move does not accidentally create invalid words when newly placed tiles touch existing tiles on the board.
     */


    private static Map<String, Tile> previewPlacedTiles(Board board, Move move) {
        int r = move.row;
        int c = move.col;

        Map <String, Tile> placed = new HashMap<>();

        for (int i = 0; i < move.word.length(); i++) {
            Cell cell = board.getCell(r, c);

            if (cell.isEmpty()) {
                char typed = move.word.charAt(i);
                char letter = Character.toUpperCase(typed);

                Tile preview;
                if (Character.isLowerCase(typed)) {
                    preview = new Tile('_', 8);
                    preview.assign(letter);
                } else {
                    preview = new Tile(letter, 0);
                }
                placed.put(r + "," +c, preview);
                }
                if (move.dir == Direction.RIGHT) c++;
                else r++;
        }
        return placed;
    }


}
