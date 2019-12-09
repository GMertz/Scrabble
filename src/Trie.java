import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

public class Trie
{
    // what is everything the Trie will be used for?
    Node head;

    public Trie()
    {
        head = new Node('\0');
    }

    public Trie(In inFile)
    {
        if(true)return;
        head = new Node('\0');
        for (String line : inFile.readAllLines())
        {
            insert(inFile.readLine());
        }
    }

    public int CharToInt(char c) { return c < 'a' ? c-'A' : c-'a'; }

    public void insert(String s) { insertHelp(s,0, head); }

    public boolean contains(String s)
    {
        Node trav = head;
        for (int i = 0; i < s.length(); i++)
        {
            int c = CharToInt(s.charAt(i));

            if(trav.children[c] == null)
            {
                return false;
            }
            trav = trav.children[c];
        }
        return trav.isWord;
    }

    // Find the best word given the world template and the tileDescriptor
    public String FindWord(String template, String tileDescriptor)
    {
        return new String();
    }

    private Node insertHelp(String s, int ind, Node n)
    {
        if(ind == s.length() && n.c == s.charAt(s.length()-1))
        {
            n.isWord = true;
        }
        else
        {
            int letter = CharToInt(s.charAt(ind));
            if (n.children[letter] == null)
            {
                n.children[letter] = new Node(s.charAt(ind));
            }
            n.children[letter] = insertHelp(s, ind + 1, n.children[letter]);
        }
        return n;
    }

    private class Node
    {
        char c;
        boolean isWord;
        Node[] children;
        public Node(char c)
        {
            this.c = c;
            children = new Node[26]; // one slot for each english letter
        }
    }
}
