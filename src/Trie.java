import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

public class Trie
{
    /** Create trie to store all the words in enable.txt **/
    private Node head;

    /** stores the number of times each letter appears in the dictionary **/
    private int [] freqarr = new int[26];

    /** make the head of the trie null **/
    public Trie()
    {
        head = new Node('\0');
    }

    /** read the file **/
    public Trie(In inFile)
    {
        head = new Node('\0');
        for (String line : inFile.readAllLines())
        {
            insert(line.trim());
        }
    }


    public Node GetHead(){return head;}

    public void insert(String s) { insertHelp(s,0, head); }

    /** check if trie contains string **/
    public boolean contains(String s)
    {
        Node trav = head;
        for (int i = 0; i < s.length(); i++)
        {
            int c = Player.CharToInt(s.charAt(i));

            if(trav.children[c] == null)
            {
                return false;
            }
            trav = trav.children[c];
        }
        return trav.isWord;
    }

    /** insert new nodes into tree recursively **/
    private Node insertHelp(String s, int ind, Node n)
    {
        if(ind == s.length() && n.c == s.charAt(s.length()-1))
        {
            n.isWord = true;
        }
        else
        {
            int letter = Player.CharToInt(s.charAt(ind));
            if (n.children[letter] == null)
            {
                n.children[letter] = new Node(s.charAt(ind));
            }
            n.children[letter] = insertHelp(s, ind + 1, n.children[letter]);

            n.children[letter].parent = n;

            /** as we insert the nodes, we note the frequency at which each letter appears**/
            ++ freqarr [letter];
        }
        return n;
    }

    public class Node
    {
        private char c;
        private boolean isWord = false;
        private Node[] children;
        private Node parent;

       /** trie is composed of nodes. Each node has 26 children. **/
        public Node(char c)
        {
            this.c = c;
            children = new Node[26]; // one slot for each english letter
        }

        public Node GetChild(char c) { return children[c-'a']; }
        public Node GetChild(int i)
        {
            return children[i];
        }
        public char Get()
        {
            return c;
        }

        public boolean IsWord(){return isWord;}


        public Node GetParent() { return parent; }
    }

    public int getFreaqy(int index){

        return freqarr[index];

    }
}



