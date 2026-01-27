package pij.main;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class WordList {
    private final HashSet<String> words = new HashSet<>();

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

    public boolean contains(String word) {
        if (word == null) return false;
        return words.contains(word.trim().toUpperCase());
    }

    private InputStream openStream(String filename) throws IOException {
        InputStream in = WordList.class.getClassLoader().getResourceAsStream(filename);
        if (in != null) return in;

        in = WordList.class.getClassLoader().getResourceAsStream(filename);
        if (in != null) return in;

        Path here = Paths.get("").toAbsolutePath();
        for (int up = 0; up < 12 && here != null; up++, here = here.getParent()) {
            Path p1 = here.resolve("resources").resolve(filename);
            if (Files.exists(p1)) return Files.newInputStream(p1);
        }

        throw new FileNotFoundException("Could not find " + filename);
    }
}
