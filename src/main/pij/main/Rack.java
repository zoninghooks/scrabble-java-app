package pij.main;

import java.util.ArrayList;
import java.util.List;

public class Rack {
    public static final int DEFAULT_SIZE = 7;

    private final ArrayList<Tile> tiles = new ArrayList<>();

    public void refillFrom(TileBag bag, int maxSize) {
        while (tiles.size() < maxSize && !bag.isEmpty()) {
            tiles.add(bag.draw());
        }
    }

    public List<Tile> getTiles() {
        return List.copyOf(tiles);
    }

    public boolean removeLetter(char letter) {
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).getLetter() == letter) {
                tiles.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean removeWildcard() {
        return removeLetter('_');
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiles.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tiles.get(i));
        }
        return sb.toString();
    }
}
