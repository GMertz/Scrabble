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
    Trie()
    {
        head = new Node('\0');
    }

    /** read the file **/
    Trie(In inFile)
    {
        head = new Node('\0');
        for (String line : inFile.readAllLines())
        {
            insert(line.trim());
        }
    }


    int GetFrequency(int index){ return freqarr[index]; }
    Node GetHead(){return head;}

    public void insert(String s) { insertHelp(s,0, head); }

    /**
     * is the string in the dictionary?
     * @param s string to check against dictionary
     * @return
     */
    public boolean contains(String s) // used in our trie testing class
    {
        Node trav = head;
        for (int i = 0; i < s.length(); ++i)
        {
            int c = SmoothCriminal.CharToInt(s.charAt(i));

            if(trav.children[c] == null)
            {
                return false;
            }
            trav = trav.children[c];
        }
        return trav.isWord;
    }

    /**
     *
     * @param s string to be inserted
     * @param ind current spot in the string we are considering
     * @param n current node we are visiting
     * @return child node of the caller
     */
    private Node insertHelp(String s, int ind, Node n)
    {
        if(ind == s.length() && n.c == s.charAt(s.length()-1))
        {
            n.isWord = true;
        }
        else
        {
            int letter = SmoothCriminal.CharToInt(s.charAt(ind));
            if (n.children[letter] == null)
            {
                n.children[letter] = new Node(s.charAt(ind));
            }
            n.children[letter] = insertHelp(s, ind + 1, n.children[letter]);

            n.children[letter].parent = n;

            /** as we insert the nodes, we note the frequency at which each letter appears**/
            freqarr [letter] += 1;
        }
        return n;
    }

    class Node
    {
        private char c;
        private boolean isWord = false;
        private Node[] children;
        private Node parent;

       /** trie is composed of nodes. Each node has 26 children (one for each letter of the alphabet). **/
        Node(char c)
        {
            this.c = c;
            children = new Node[26]; // one slot for each english letter
        }
        Node GetChild(char c)
        {
            return children[c-'a'];
        }
        Node GetChild(int i)
        {
            return children[i];
        }
        char Get()
        {
            return c;
        }
        boolean IsWord(){return isWord;}
        Node GetParent() { return parent; }
    }
}



