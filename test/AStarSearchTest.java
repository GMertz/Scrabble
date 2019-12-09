import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class SearcherTest
{
    private Trie trie;
    private Searcher ass;
    @BeforeEach
    void setup()
    {
        trie = new Trie(new In("enable.txt"));
        //ass = new Searcher();
    }
}