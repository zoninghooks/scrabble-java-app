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

public class TileBag {

    // Example line: 8 x [A1]   or 2 x [_8]
    private static final Pattern LINE =
            Pattern.compile("\\s*(\\d+)\\s*x\\s*\\[([A-Z_])\\s*(-?\\d+)\\]\\s*"); // this is so that 8 x [A1] , 2x[_8] or extra spaces added can also be read.

    private final ArrayList<Tile> tiles = new ArrayList<>();
    private final Random rng = new Random();

    public TileBag(String filename) {
        loadFromFile(filename);
        shuffle();
    }

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

    private InputStream openStream(String filename) throws IOException {
        InputStream in = TileBag.class.getClassLoader().getResourceAsStream(filename);
        if (in != null) return in;

        in = TileBag.class.getClassLoader().getResourceAsStream("resources/" + filename);
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

    public void shuffle() {
        Collections.shuffle(tiles, rng);
    }

    public int remaining() {
        return tiles.size();
    }

    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    public Tile draw() {
        if (tiles.isEmpty()) throw new NoSuchElementException("Tile bag is empty");
        return tiles.remove(tiles.size() - 1);
    }

    public List<Tile> draw(int k) {
        if (k < 0) throw new IllegalArgumentException("k must be >= 0");
        ArrayList<Tile> out = new ArrayList<>(Math.min(k, tiles.size()));
        for (int i = 0; i < k && !tiles.isEmpty(); i++) out.add(draw());
        return out;
    }

    public void putBack(Tile tile) {
        if (tile == null) throw new IllegalArgumentException("tile is null");
        tiles.add(tile);
        shuffle();
    }
}
