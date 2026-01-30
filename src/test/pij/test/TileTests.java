package test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pij.main.Tile;


public class TileTests {
 
    @Test
    void tile_nonWildcardBoardLetterIsOriginalLetter() {
        Tile t = new Tile('A', 1);
        assertFalse(t.isWildcard());
        assertEquals('A', t.getBoardLetter());
        assertEquals('A', t.getLetter());
        assertEquals(1, t.getValue());
    }

    @Test
    void tile_wildcardIsRecognized() {
        Tile t = new Tile('_', 8);
        assertTrue(t.isWildcard());
    }

    @Test
    void tile_assignOnlyAffectsWildcards() {
        Tile normal = new Tile('B', 3);
        normal.assign('Z'); 
        assertEquals('B', normal.getBoardLetter());

        Tile wild = new Tile('_', 8);
        wild.assign('E');
        assertEquals('E', wild.getBoardLetter());
    }

    @Test
    void tile_toBoardStringShowsUnassignedWildcardAsUnderscore() {
        Tile wild = new Tile('_', 8);
        assertEquals("_8", wild.toBoardString());
    }

    @Test
    void tile_displayCharShowsAssignedWildcardLowercase() {
        Tile wild = new Tile('_', 8);
        wild.assign('K');
        assertEquals('k', wild.getDisplayChar());
    }

}
