import com.sun.source.tree.AssertTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class SearcherTest
{
    char[] sampleHand = {'a','c','r','e','s'};
    HashSet<String> DICTIONARY;
    ArrayList<Character> hand;
    private Trie trie;
    private Searcher search;
    @BeforeEach
    void setup()
    {
        In in = new In("enable1.txt");
        DICTIONARY = new HashSet<>();
        DICTIONARY.addAll(Arrays.asList(in.readAllLines()));


        search = new Searcher(new Searcher.SimpleScorer());


        hand = new ArrayList<Character>();
        for (char c: sampleHand)
        {
            hand.add(c);
        }
    }

    @Test
    void searchFindsAValidWord()
    {
        String temp = "A____";

        search.search(temp,hand,new Location(1,1),new Location(1,1));
        assertTrue(DICTIONARY.contains(search.GetBestWord()));

    }
    @Test
    void wordIsProperLength()
    {
        String temp = "A____";

        search.search(temp,hand,new Location(1,1),new Location(1,1));
        StdOut.printf("|%s|",search.GetBestWord());
        assertEquals(search.GetBestWord().length(),temp.length());
    }

    @Test
    void searchedWordIsCorrect()
    {
        String temp = "A_R__";
        int aIndex = 0;
        int rIndex = 2;

        search.search(temp,hand,new Location(1,1),new Location(1,1));

        String ret = search.GetBestWord();

        boolean flag = true;
        for (int i = 0; i < ret.length(); i++)
        {
            if(i == aIndex || i == rIndex) continue;
            if(!hand.contains(ret.charAt(i))) flag = false;
        }
        assertTrue(ret.charAt(aIndex) == 'a' &&ret.charAt(rIndex) == 'r' && flag);

    }
}