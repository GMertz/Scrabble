import java.util.concurrent.Callable;

public class Trie
{
    // what is everything the Trie will be used for?
    Node head;

    public Trie()
    {
        head = new Node('\0',0);
    }

    public void insert(String s)
    {
        //Board.TILE_VALUES.get(s.charAt(0));
    }


    // Find the best word given the world template and the tileDescriptor
    public String FindWord(String template, String tileDescriptor)
    {
        return new String();
    }

    private Node insertHelp(String s, int ind, Node n, int score)
    {
        int letter = s.charAt(ind)-'a';
        score += Board.TILE_VALUES.get(letter);

        if(n.children[letter] != null)
        {
            n.children[letter] = insertHelp(s,ind+1, n.children[letter], score);
            return n;
        }
        else
        {
            Node restOfWord = new Node(s.charAt(ind), score);
            Node trav = restOfWord;
            for (int i = ind+1; i < s.length(); i++)
            {
                letter = s.charAt(ind)-'a';
                score += Board.TILE_VALUES.get(letter);
                trav.children[letter] = new Node(s.charAt(ind), score);
            }
            return restOfWord;
        }
    }

    public void AStarSearch(int v, Callable<Character> h)
    {

    }

    private class Node
    {
        char c;
        int wordScore;
        Node[] children;
        public Node(char c, int wordScore)
        {
            this.c = c;
            this.wordScore = wordScore;
            children = new Node[26]; // one slot for each english letter
        }
    }

}
