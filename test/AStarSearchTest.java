import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class AStarSearchTest
{
    private Trie trie;
    private AStarSearch ass;
    @BeforeEach
    void setup()
    {
        trie = new Trie(new In("enable.txt"));
        ass = new AStarSearch();
    }
}