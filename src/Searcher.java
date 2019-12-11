import java.util.*;

/**
 * helper class to find the 'best', 'valid' word that fits the given 'template'
 * 'best': the word with maximum score
 * 'valid': the word is valid given its placement on the board
 * 'template': string in the form "_A____".
 *         in the above example, Searcher will search for words that are of length 6 and that have A as their second
 *         letter. Additionally, blanks will only be filled with characters in the 'bag'
 * 'bag': A length 7 string describing the letters that can be used to create words (the words in our hand)
 */
public class Searcher
{
    private Trie trie; // used for finding words

    private char[] current; // word we are currently building

    private int maxLen; // Length of our desired word

    private int reqChar; // index of the first character that needs to be in the word

    private Stack<Integer>[] possibleChars; // PriorityQueue array used to assist with traversal

    private HashMap<Character, Integer> bag;
    private char[] template;

    private int layer; // which letter of the word we are currently on

    private boolean[] layerVisited; // used to know if we have already calculated moves from a given character

    private Stack<String> words; // words we have found that fit the template
    private HashSet<String> wordLookup;

    /**
     * Fill our trie with the dictionary
     */
    public Searcher()
    {
        trie = new Trie(new In("enable1.txt"));
    }

    /**
     * Used to get the frequency of a letter from our dictionary
     * @param index index of the letter in the alphabet
     * @return the amount of times this letter shows up in the dictionary
     */
    int GetFrequency(int index){ return trie.GetFrequency(index); }

    /**
     * @return Stack of all words found during the search
     */
    Stack<String> GetAllWords() { return words; }

    /**
     *
     * @return were any words found during the search?
     */
    boolean HasWords() { return !words.isEmpty(); }

    /** prepares for a new search (initializes a lot of random stuff) **/
    private void init(char[] temp, ArrayList<Character> hand)
    {
        this.template = Arrays.copyOf(temp,temp.length);
        reqChar = -1;
        maxLen = template.length;
        current = new char[maxLen];
        layerVisited = new boolean[maxLen+1]; // we add one to simplify some operations later

        possibleChars = new Stack[26];
        words = new Stack<>();
        wordLookup = new HashSet<>();

        bag = new HashMap<>();

        for (int i = 0; i < template.length; i++)
        {
            /*
                if a character is '*' it means this character must be in the word for it to connect to some
                other part of the board. We only need to care about the first of such characters
            */
            if (template[i] == '*')
            {
                template[i] = '_';
                if(reqChar == -1) reqChar = i;
            }
            template[i] = Character.toLowerCase(template[i]);// Lowercase all chars to simplify conversions later on
        }

        // Initialize our bag
        for (char c : hand)
        {
            c = Character.toLowerCase(c);
            if(bag.containsKey(c)) bag.replace(c, bag.get(c)+1);
            else bag.put(c,1);
        }
    }

    /**
     * @param template template string used for search. Words found will conform to this template
     *                 '_' characters in this string will be filled with a character contained in hand,
     *                 and at least one character marked '*' will be included in any word.
     * @param hand characters that we can use to build words
     */
    public void search(char[] template, ArrayList<Character> hand)
    {
        init(template,hand);
        searchHelp();
    }

    /**
     * actual meat and potatoes of the search
     */
    private void searchHelp()
    {
        // trav is used to traverse the Trie
        Trie.Node trav = trie.GetHead();
        char tempC; //simply a scratch variable

        while(true)
        {
            if(trav.IsWord()) // if we find a word, add it
            {
                AddWord();
            }

            if(layer == maxLen)
            {
                DecrementLayer();
                trav = trav.GetParent();
            }
            else if (template[layer] == '_') // the next character needs to come from our hand
            {
                if (!layerVisited[layer])
                {
                    FindViableChildren(trav);  // fill charScores with possible next letters
                }
                if(possibleChars[layer].isEmpty()) // if there are no possible paths to take
                {
                    if(layer == 0) return; // We have exhausted our search
                    DecrementLayer();
                    trav = trav.GetParent();
                }
                else // continue to the next character
                {
                    int val = possibleChars[layer].pop(); // traverse to a character
                    IncrementLayer(val);
                    trav = trav.GetChild(val);
                }
            }
            else // If there is a letter specified in the template
            {
                tempC = template[layer];

                if (trav.GetChild(tempC) == null || layerVisited[layer])
                {
                    if(layer == 0) return; // We have exhausted our search
                    DecrementLayer();
                    trav = trav.GetParent();
                }
                else
                {
                    IncrementLayer(tempC);
                    trav = trav.GetChild(tempC);
                }
            }
        }
    }

    /**
     * given our hand, calculate all children that can be traversed to
     * @param trav our current Trie traversal node
     */
    private void FindViableChildren(Trie.Node trav)
    {
        layerVisited[layer] = true;
        possibleChars[layer] = new Stack<>();

        // scratch variables
        Trie.Node tempNode;
        int t;

        //for each character in the hand, see if we can add it to current word
        for (Character piece : bag.keySet())
        {
            // character is in bag but we have already used it in current word
            if(bag.get(piece) <= 0) continue;

            // if its a space (wildcard), add all characters
            if (piece == '_')
            {
                for (int i = 0; i < 26; i++)
                {
                    tempNode = trav.GetChild(i);
                    if (tempNode != null)
                    {
                        t = lowerCaseToInt(tempNode.Get());
                        possibleChars[layer].push(t);
                    }
                }
                break;
            }
            else
            {
                tempNode = trav.GetChild(piece);
                if (tempNode != null)
                {
                    t = lowerCaseToInt(tempNode.Get());
                    possibleChars[layer].add(t);
                }
            }
        }
    }

    /**
     * Back track to the previous character in our search. This happens when we have exhausted all possibilities
     * for the current word
     */
    private void DecrementLayer()
    {
        layerVisited[layer] = false;
        layer -= 1;
        char tempC = Character.toLowerCase(current[layer]);

        if (!SmoothCriminal.isLetter(template[layer]))
        {
            if (bag.containsKey(tempC))
            {
                bag.replace(tempC, bag.get(tempC) + 1);
            }
            else
            {
                bag.replace('_', bag.get('_') + 1);
            }
        }
    }

    /**
     * Traverse deeper into our search
     * @param c character that is specified in the template
     */
    private void IncrementLayer(char c)
    {
        layerVisited[layer] = true;
        current[layer] = c; // add letter to our template

        layer += 1;
    }

    /**
     * Traverse deeper into our search
     * @param val the alphabetical index of the letter to traverse to
     */
    private void IncrementLayer(int val)
    {
        char let = (char)('a'+val);

        // if letter is in our hand (bag) then decrement it, otherwise we must be using our blank '_'
        if (bag.containsKey(let))
        {
            bag.replace(let, bag.get(let)-1);
        }
        else
        {
            bag.replace('_', bag.get('_')-1);
            let = Character.toUpperCase(let); // if we're using our blank then uppercase it
        }

        current[layer] = let; // add letter to our current search
        layer += 1;
    }

    private int lowerCaseToInt(char c) { return c-'a'; }

    /**
     * Add the word in 'current' (up to length 'layer') to our found words stack if possible.
     * If the word does not fulfill a few conditions it will not be added
     */
    private void AddWord()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < layer; ++i)
        {
            sb.append(template[i] =='_'? current[i] : ' ');
        }

        String s = sb.toString();

        /*
            Conditions that stop us from adding a word at this point:
                1. Our word doesnt contain the first reqChar -> this means the word is not attached to anything.
                2. We have already added the word.
                3. The character below us is in the template. If we allow such a word it is highly likely it is invalid
         */
        if (layer <= reqChar || wordLookup.contains(s) || ((layer < maxLen-1) && template[layer+1] != '_'))
        {
            return;
        }

        wordLookup.add(s); // add word so we dont add it again
        words.add(s);
    }
}
