package edu.grinnell.csc207.spellchecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

/**
 * A spellchecker maintains an efficient representation of a dictionary for
 * the purposes of checking spelling and provided suggested corrections.
 */
public class SpellChecker {
    /** The number of letters in the alphabet. */
    private static final int NUM_LETTERS = 26;

    /** The path to the dictionary file. */
    private static final String DICT_PATH = "words_alpha.txt";

    /**
     * @param filename the path to the dictionary file
     * @return a SpellChecker over the words found in the given file.
     */
    public static SpellChecker fromFile(String filename) throws IOException {
        return new SpellChecker(Files.readAllLines(Paths.get(filename)));
    }

    /** A Node of the SpellChecker structure. */
    private class Node {
        /** Children nodes for each letter of the alphabet */
        private Node[] children;
        /** Whether this node represents the end of a valid word */
        private boolean isWord;

        /** Constructor for a new Node */
        public Node() {
            this.children = new Node[NUM_LETTERS];
            this.isWord = false;
        }
    }

    /** The root of the SpellChecker */
    private Node root;

    /**
     * Creates a new SpellChecker with a given list of dictionary words.
     * Each word in the dictionary will be added to the trie data structure.
     *
     * @param dict A list of words to be used as the dictionary
     */
    public SpellChecker(List<String> dict) {
        this.root = new Node();
        for (String word : dict) {
            add(word.toLowerCase());
        }
    }

    /**
     * Adds a word to the spell checker's dictionary.
     * The word is converted to lowercase before being added.
     *
     * @param word The word to add to the dictionary
     */
    public void add(String word) {
        Node current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            int index = c - 'a';
            if (current.children[index] == null) {
                current.children[index] = new Node();
            }
            current = current.children[index];
        }
        current.isWord = true;
    }

    /**
     * Checks if a given word exists in the dictionary.
     * The check is case-insensitive.
     *
     * @param word The word to check
     * @return true if the word is in the dictionary, false otherwise
     */
    public boolean isWord(String word) {
        Node current = root;
        // Convert to lowercase and follow the path for each character
        word = word.toLowerCase();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            int index = c - 'a';
            // If there's no path for this character, word is not in trie
            if (current.children[index] == null) {
                return false;
            }
            current = current.children[index];
        }
        // Word exists only if we reached a node marked as a word
        return current.isWord;
    }

    /**
     * Finds all possible valid words that can be formed by adding one character
     * to the end of the given word.
     *
     * @param word The word to complete
     * @return A list of valid words that can be formed by adding one character to
     *         the end
     */
    public List<String> getOneCharCompletions(String word) {
        List<String> completions = new ArrayList<>();
        Node current = root;

        // Convert to lowercase and follow the path for the prefix
        word = word.toLowerCase();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            int index = c - 'a';
            // If we can't follow the path, there are no completions
            if (current.children[index] == null) {
                return completions;
            }
            current = current.children[index];
        }

        // Try adding each possible character
        for (int i = 0; i < NUM_LETTERS; i++) {
            if (current.children[i] != null && current.children[i].isWord) {
                // Convert index back to character and add to completions
                char nextChar = (char) ('a' + i);
                completions.add(word + nextChar);
            }
        }

        return completions;
    }

    /**
     * Finds all valid words that can be formed by replacing the last character
     * of the given word with a different character.
     *
     * @param word The word to correct
     * @return A list of valid words that differ only in the last character
     */
    public List<String> getOneCharEndCorrections(String word) {
        List<String> corrections = new ArrayList<>();
        if (word.length() == 0) {
            return corrections;
        }

        Node current = root;
        word = word.toLowerCase();

        // Follow the path for all but the last character
        for (int i = 0; i < word.length() - 1; i++) {
            char c = word.charAt(i);
            int index = c - 'a';
            if (current.children[index] == null) {
                return corrections;
            }
            current = current.children[index];
        }

        // Try replacing the last character with every possible letter
        String prefix = word.substring(0, word.length() - 1);
        for (int i = 0; i < NUM_LETTERS; i++) {
            if (current.children[i] != null && current.children[i].isWord) {
                char replacement = (char) ('a' + i);
                corrections.add(prefix + replacement);
            }
        }

        return corrections;
    }

    /**
     * Finds all valid words that can be formed by replacing any single character
     * in the given word with a different character.
     *
     * @param word The word to correct
     * @return A list of valid words that differ by exactly one character
     */
    public List<String> getOneCharCorrections(String word) {
        List<String> corrections = new ArrayList<>();
        if (word.length() == 0) {
            return corrections;
        }

        word = word.toLowerCase();

        // Try replacing each character position
        for (int pos = 0; pos < word.length(); pos++) {
            Node current = root;

            // Follow path up to the position we want to change
            boolean pathValid = true;
            for (int i = 0; i < pos; i++) {
                char c = word.charAt(i);
                int index = c - 'a';
                if (current.children[index] == null) {
                    pathValid = false;
                    break;
                }
                current = current.children[index];
            }

            if (!pathValid) {
                continue;
            }
            // Try each possible replacement letter at this position
            for (int i = 0; i < NUM_LETTERS; i++) {
                char replacement = (char) ('a' + i);
                if (replacement == word.charAt(pos)) {
                    continue; // Skip if same as original
                }
                // Check if this replacement leads to a valid word
                Node checkNode = current;
                if (checkNode.children[i] != null) {
                    checkNode = checkNode.children[i];

                    // Try to follow the rest of the word after our replacement
                    boolean validWord = true;
                    for (int j = pos + 1; j < word.length(); j++) {
                        char c = word.charAt(j);
                        int index = c - 'a';
                        if (checkNode.children[index] == null) {
                            validWord = false;
                            break;
                        }
                        checkNode = checkNode.children[index];
                    }

                    // If we could follow the entire path and it's a word, add it
                    if (validWord && checkNode.isWord) {
                        String corrected = word.substring(0, pos) + replacement
                                + word.substring(pos + 1);
                        corrections.add(corrected);
                    }
                }
            }
        }

        return corrections;
    }

    /**
     * The main entry point for the SpellChecker program.
     * Supports three commands:
     * - check: determines if a word is spelled correctly
     * - complete: suggests completions for a word
     * - correct: suggests corrections for a misspelled word
     *
     * @param args Command line arguments: [command] [word]
     * @throws IOException If the dictionary file cannot be read
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java SpellChecker <command> <word>");
            System.exit(1);
        } else {
            String command = args[0];
            String word = args[1];
            SpellChecker checker = SpellChecker.fromFile(DICT_PATH);
            switch (command) {
                case "check": {
                    System.out.println(checker.isWord(word) ? "correct" : "incorrect");
                    System.exit(0);
                }

                case "complete": {
                    List<String> completions = checker.getOneCharCompletions(word);
                    for (String completion : completions) {
                        System.out.println(completion);
                    }
                    System.exit(0);
                }

                case "correct": {
                    List<String> corrections = checker.getOneCharCorrections(word);
                    for (String correction : corrections) {
                        System.out.println(correction);
                    }
                    System.exit(0);
                }

                default: {
                    System.err.println("Unknown command: " + command);
                    System.exit(1);
                }
            }
        }
    }
}
