package test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import pij.main.*;

public class WordListTest {

    @TempDir // Creates temporary file
    Path tempDir;

    // Helps to create the file inside the temp directory, write the content inside and returns the file path.
    private Path writeFile(String name, String content) throws IOException {
        Path p = tempDir.resolve(name);
        Files.writeString(p, content);
        return p;
    }

    @Test // Check to see if case insensitivity works, random spaces works and if words not inside the file would appear false.
    void loadsWordsUppercasesAndIgnoresBlankLines() throws IOException {
        Path f = writeFile("words.txt", "hello\nWoRlD\n  CAT \n");
        WordList wl = new WordList(f.toAbsolutePath().toString());
        assertTrue(wl.contains("HELLO"));
        assertTrue(wl.contains("world"));
        assertTrue(wl.contains(" cat "));
        assertFalse(wl.contains("nope")); 
    }


    // The computer player relies on allWords() to see at least one valid word in the dictionary, the computer player would not be able to take a turn if it was empty.
    @Test // Checks that after loading a non-empty dictionary file, the allWords() method is not empty.
    void allWordsIsNotEmptyAfterLoading() throws IOException {
        Path f = writeFile("words.txt", "a\nbb\nccc\n");
        WordList wl = new WordList(f.toAbsolutePath().toString());
        assertTrue(wl.allWords().iterator().hasNext());
    }

    @Test // If no words are in the empty.txt file then it should give an IllegalArgumentException.
    void emptyWordFileThrowsIllegalArgumentException() throws IOException {
        Path f = writeFile("empty.txt", "\n   \n\n");
        assertThrows(IllegalArgumentException.class, () -> new WordList(f.toAbsolutePath().toString()));
    }

    @Test // File does not exist so it should give a RuntimeException.
    void missingWordFileThrowsRuntimeException() {
        String missingPath = tempDir.resolve("doesnotexist.txt").toAbsolutePath().toString();
        assertThrows(RuntimeException.class, () -> new WordList(missingPath));
    }
}