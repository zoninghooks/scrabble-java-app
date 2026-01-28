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

    public Tile removeLetterTile(char letter) {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (!t.isWildcard() && t.getLetter() == letter) {
                tiles.remove(i);
                return t;
            }
        }
        return null;
    }

    public Tile removeWildcardTile() {
        for (int i = 0; i < tiles.size(); i++) {
            Tile t = tiles.get(i);
            if (t.isWildcard()) {
                tiles.remove(i);
                return t;
            }
        }
        return null;
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

    public Rack(Rack other) {
        this.tiles.addAll(other.getTiles());
    }
}
