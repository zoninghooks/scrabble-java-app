package pij.main;

import java.io.*;
import java.nio.file.*;
import java.util.*;


/**
 * This class represents the dictionary of valid words.
 * <p>
 * Words are loaded from a resources/wordlist.txt, one word per line, every word read from the file is converted into uppercase.
 */
public final class WordList {
    /** New set of valid words (stored in uppercase). */
    private final HashSet<String> words = new HashSet<>();

    /**
     * Loads a word list from a file.
     * <p>
     * Blank lines are ignored. All words are converted to uppercase.
     *
     * @param filename wordlist.txt file name.
     * @throws RuntimeException if the file cannot be opened.
     * @throws IllegalArgumentException if the file contains no words.
     */
    public WordList(String filename) {
        try (InputStream in = openStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                words.add(line.toUpperCase());
            }

            if (words.isEmpty()) {
                throw new IllegalArgumentException("Word list file contained no words: " + filename );
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not open word list file: " + filename, e);
        }
    }

    /**
     * Checks whether a word exists in the dictionary.
     * <p>
     * The check is case-insensitive and removes whitespace.
     *
     * @param word is the word to check.
     * @return true if the word exists in the dictionary.
     */
    public boolean contains(String word) {
        if (word == null) return false;
        return words.contains(word.trim().toUpperCase());
    }

    /**
     * Attempts to open the word list file from common locations:
     * <ul>
     *   <li> classpath resource: {@code filename} </li>
     *   <li> classpath resource: {@code resources/filename} </li>
     *   <li> filesystem: {@code resources/filename} (searching upward) </li>
     * </ul>
     *
     * @param filename is the wordlist.txt file name.
     * @return input stream for the file.
     * @throws IOException if a found file cannot be opened.
     * @throws FileNotFoundException if the file cannot be found.
     */
    private InputStream openStream(String filename) throws IOException {
        InputStream in = WordList.class.getClassLoader().getResourceAsStream(filename);
        if (in != null) return in;

        in = WordList.class.getClassLoader().getResourceAsStream("resources/" + filename);
        if (in != null) return in;

        Path here = Paths.get("").toAbsolutePath();
        for (int up = 0; up < 12 && here != null; up++, here = here.getParent()) {
            Path p1 = here.resolve("resources").resolve(filename);
            if (Files.exists(p1)) return Files.newInputStream(p1);
        }

        throw new FileNotFoundException("Could not find " + filename);
    }
    
    /**
     * Returns an iterable over all words in the dictionary.
     * <p>
     * Used by the computer player to search for possible moves.
     *
     * The dictionary stores all words in memory. 
     * allWords() returns an Iterable, which allows the computer player to loop over the same set of words multiple times using a fresh iterator each time.
     * 
     * @return iterable collection of dictionary words.
     */
    public Iterable<String> allWords() {
        return words;
    }
}
