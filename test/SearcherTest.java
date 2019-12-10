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
    char[] sampleHand = {'_','A','E','_','_','_','_','_'};
    HashSet<String> DICTIONARY;
    ArrayList<Character> hand;
    private Searcher search;

    String temp = "________";

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
        search.search(temp,hand,new Location(1,1),new Location(1,1));
        assertTrue(DICTIONARY.contains(search.GetBestWord()));
    }
    @Test
    void wordIsProperLength()
    {
        search.search(temp,hand,new Location(1,1),new Location(1,1));
        StdOut.printf("|%s|",search.GetBestWord());
        StdOut.printf("|%d|",search.GetBestScore());


        assertEquals(search.GetBestWord().length(),temp.length());
    }

    //@Test
    void test()
    {
    }
    //@Test
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