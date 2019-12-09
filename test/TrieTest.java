import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest
{
    private Trie trie;
    private Board board;

    HashSet<String> DICTIONARY;
    @BeforeEach
    void setup()
    {
        In in = new In("enable1.txt");
        DICTIONARY = new HashSet<>();
        trie = new Trie();
        board = new Board();

        DICTIONARY.addAll(Arrays.asList(in.readAllLines()));
    }


    @Test
    void CharToIntWorksWithCases()
    {
        assertTrue(trie.CharToInt('a') == trie.CharToInt('A'));
    }

    @Test
    void CharToIntFindsTheRightValue()
    {
        assertTrue(trie.CharToInt('a') == 0);
    }

    @Test
    void ContainsFindsInsertedWord()
    {
        trie.insert("Wordasd");
        assertTrue(trie.contains("Wordasd"));
    }
    @Test
    void ContainsDoesntFindUnaddedWord()
    {
        assertFalse(trie.contains("Word"));
    }

    @Test
    void TrieIsCaseInsensitive()
    {
        trie.insert("words");
        assertTrue(trie.contains("WORDS"));
    }
}