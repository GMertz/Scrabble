import com.sun.source.tree.AssertTree;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SearcherTest
{
    char[] sampleHand = {'_','_','_','_','_','_','_','_','_'};
    HashSet<String> DICTIONARY;
    ArrayList<Character> hand;
    private Searcher search;

    PriorityQueue<String> que;
    HashMap<String,Integer> scores;

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

        search.search(temp.toCharArray(),hand,new Location(1,1),new Location(1,1));
        que = search.GetAllWords();
        scores = search.GetAllWordScores();
        String s = que.poll();
    }

    @Test
    void searchFindsAllValidWords()
    {
        search.search(temp.toCharArray(),hand,new Location(1,1),new Location(1,1));
        for (String s : que)
        {
            if (DICTIONARY.contains(s))
            {
                assertTrue(true);
            }
            else
            {
                assertTrue(false);
            }
        }
    }

    //@Test
    void wordIsProperLength()
    {
        for (String s : que)
        {
            assertEquals(s.length(),temp.length());
        }
    }

    //@Test
    void printAllWords()
    {
        for (String s : que)
        {
            StdOut.printf("|%s|",s);
            StdOut.printf("|%d|\n",scores.get(s));
        }
    }
    //@Test
    void searchedWordIsCorrect()
    {
        String temp = "A_R__";
        int aIndex = 0;
        int rIndex = 2;

        search.search(temp.toCharArray(),hand,new Location(1,1),new Location(1,1));

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