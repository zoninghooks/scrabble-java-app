package pij.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the bag of tiles that players draw from.
 * <p>
 * The bag is loaded from a text file (/resources/defaultTilebag.txt) and then shuffled.
 * Tiles are drawn in batches after a player takes their turn assuming they did not pass their turn.
 * 
 * Each line of /resources/defaultTilebag.txt describes how many copies of a tile exist. 
 * 
 * For example:
 * 8 x [A1] 
 * 2 x [_8]
 * 
 * Format:
 * number : times : [tile string:value] 
 * 
 * ( _ represents a wildcard)
 */
public class TileBag {


    /**
     * Regex for parsing /resources/defaultTileBag.txt 's format.
     * Supports optional spaces, such as {@code "8 x [A1]"} or {@code "2x[_8]"}.
     */
    private static final Pattern LINE =
            Pattern.compile("\\s*(\\d+)\\s*x\\s*\\[([A-Z_])\\s*(-?\\d+)\\]\\s*"); // this is so that 8 x [A1] , 2x[_8] or extra spaces added can also be read.

    /** Internal list of tiles in the bag. */
    private final ArrayList<Tile> tiles = new ArrayList<>();
    /** RNG used to shuffle tiles. */
    private final Random rng = new Random();

    /**
     * Loads a tile bag from a file and shuffles it.
     *
     * @param filename is the tile bag's file name.
     * @throws RuntimeException if the file cannot be opened.
     * @throws IllegalArgumentException if the file contains invalid lines/values.
     */
    public TileBag(String filename) {
        loadFromFile(filename);
        shuffle();
    }


    /**
     * Reads tiles from a file into the bag.
     *
     * @param filename is the tile bag's file name.
     * @throws IllegalArgumentException if any line is malformed or values are out of range.
     * @throws RuntimeException if the file cannot be opened.
     */
    private void loadFromFile(String filename) {
        try (InputStream in = openStream(filename);
             Scanner sc = new Scanner(in)) {

            int lineNo = 0;
            while (sc.hasNextLine()) {
                lineNo++;
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                Matcher m = LINE.matcher(line);
                if (!m.matches()) {
                    throw new IllegalArgumentException("Bad tile-bag line " + lineNo + ": " + line);
                }

                int count = Integer.parseInt(m.group(1));
                char letter = m.group(2).charAt(0);
                int value = Integer.parseInt(m.group(3));

                if (count <= 0) {
                    throw new IllegalArgumentException("Count must be > 0 at line " + lineNo);
                }
                if (!(letter == '_' || (letter >= 'A' && letter <= 'Z'))) {
                    throw new IllegalArgumentException("Invalid letter '" + letter + "' at line " + lineNo);
                }
                if (value < -9 || value > 99) {
                    throw new IllegalArgumentException("Tile value out of range at line " + lineNo + ": " + value);
                }

                for (int i = 0; i < count; i++) {
                    tiles.add(new Tile(letter, value));
                }
            }

            if (tiles.isEmpty()) {
                throw new IllegalArgumentException("Tile bag file contained no tiles.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not open tile bag file: " + filename, e);
        }
    }


    /**
     * Attempts to open a tile bag file from common locations:
     * <ul>
     *   <li> classpath resource: {@code filename} </li>
     *   <li> classpath resource: {@code resources/filename} </li>
     *   <li> filesystem: searches upwards for {@code resources/filename} </li>
     * </ul>
     *
     * @param filename is tile bag's file name
     * @return input stream for the file
     * @throws IOException if a found file cannot be opened
     * @throws FileNotFoundException if no file can be found
     */
    private InputStream openStream(String filename) throws IOException {
        InputStream in = TileBag.class.getClassLoader().getResourceAsStream(filename);
        if (in != null) return in;

        in = TileBag.class.getClassLoader().getResourceAsStream("resources/" + filename);
        if (in != null) return in;

        Path here = Paths.get("").toAbsolutePath();
        for (int up = 0; up < 12 && here != null; up++, here = here.getParent()) {
            Path p = here.resolve("resources").resolve(filename);
            if (Files.exists(p)) return Files.newInputStream(p);
        }

        throw new FileNotFoundException("Could not find " + filename);
    }

    /**
     * Shuffles the tile order in the bag.
     */
    public void shuffle() {
        Collections.shuffle(tiles, rng);
    }

    /**
     * @return number of tiles remaining in the bag.
     */
    public int remaining() {
        return tiles.size();
    }

    /**
     * @return true if the bag contains no tiles.
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    
    /**
     * Draws a single tile from the bag.
     *
     * @return the drawn tile.
     * @throws NoSuchElementException if the bag is empty.
     */
    public Tile draw() {
        if (tiles.isEmpty()) throw new NoSuchElementException("Tile bag is empty");
        return tiles.remove(tiles.size() - 1);
    }

    
    /**
     * Draws up to {@code k} tiles from the bag (fewer if the bag runs out).
     * <p>
     * k is the missing amount of tiles from rack.
     *
     * @param k is the number of tiles to attempt to draw.
     * @return list of drawn tiles (size between 0 and k).
     * @throws IllegalArgumentException if {@code k < 0}.
     */
    public List<Tile> draw(int k) {
        if (k < 0) throw new IllegalArgumentException("k must be >= 0");
        ArrayList<Tile> out = new ArrayList<>(Math.min(k, tiles.size()));
        for (int i = 0; i < k && !tiles.isEmpty(); i++) out.add(draw());
        return out;
    }

    /* Reshuffle was not implemented
    
    public void putBack(Tile tile) {
        if (tile == null) throw new IllegalArgumentException("tile is null");
        tiles.add(tile);
        shuffle();
    }
    */
}
