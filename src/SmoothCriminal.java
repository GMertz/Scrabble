import java.util.*;

public class SmoothCriminal implements ScrabbleAI
{
    private GateKeeper gateKeeper;
    private Searcher searcher;

    private ArrayList<Character> hand;

    SmoothCriminal()
    {
        searcher = new Searcher(); // initialize our searcher (so it can initialize its Trie)
    }

    @Override
    public void setGateKeeper(GateKeeper gateKeeper)
    {
        this.gateKeeper = gateKeeper;
    }

    /**
     * Finds the most optimal ScrabbleMove
     * @return A ScrabbleMove
     */
    @Override
    public ScrabbleMove chooseMove()
    {
        // spots that are adjacent to at least 1 letter
        boolean[][] validSpots = new boolean[Board.WIDTH][Board.WIDTH];

        hand = gateKeeper.getHand();

        // if we are on the first move, set the center to valid (so our agent will find a word containing the middle sq.)
        if (!isLetter(gateKeeper.getSquare(Location.CENTER)))
        {
            validSpots[Location.CENTER.getRow()][Location.CENTER.getColumn()] = true;
        }

        PriorityQueue<WordChoice> maxHeap = new PriorityQueue<>(); // Store words we find

        // search vertically then horizontally for words
        for (Location direction: new Location[]{Location.VERTICAL, Location.HORIZONTAL})
        {
            /*If direction is vertical, then j is a column and k is kth square in this column
             * If direction is horizontal, then j is a row and k is kth square in this row
             */
            for (int j = 0; j < Board.WIDTH; j++)
            {
                Location square;
                /* the window represents a row or a column (dependant on our search direction) */
                char[] window = new char[Board.WIDTH];

                // we keep track of letters we find and 'validSpots' we come across to tell when we should search
                int letters = 0, adjacents = 0;

                /* Fill the window, count letters, adjacent squares */
                for (int i = 0; i < Board.WIDTH; i++)
                {
                    int x;
                    int y;

                    if(direction == Location.VERTICAL)
                    {
                        x = i;
                        y = j;
                    }
                    else
                    {
                        x = j;
                        y = i;
                    }
                    square = new Location(x,y);

                    char c =  gateKeeper.getSquare(square);

                    if(isLetter(c))
                    {
                        window[i] = c;
                        letters++;
                    }
                    else if (validSpots[x][y] || isAdjacent(x,y))
                    {
                        validSpots[x][y] = true;
                        window[i] = '*'; // mark this adjacent letter in our window (so the searcher knows about it)
                        adjacents += 1;
                    }
                    else
                    {
                        window[i] = '_';
                    }
                }

                int i = 0;

                /*
                    for each possible starting space in the window, find all 'valid' words that start at it
                    these words dont account for words in the direction perpendicular to our search, but since we
                    find all of them we can just iterate through them in the end until we find one that words
                */

                do
                {
                    // if our search window is entirely filled with letters
                    // or there are no more valid spaces in it stop searching
                    if(letters >= (Board.WIDTH - i) || adjacents <= 0) break;

                    int x;
                    int y;
                    if(direction == Location.VERTICAL)
                    {
                        x = i;
                        y = j;
                    }
                    else
                    {
                        x = j;
                        y = i;
                    }

                    // It must be the case that the square before the start of our window is open or we dont search
                    if(i == 0 || window[i-1] == '_' || window[i-1] == '*' )
                    {
                        // do a search on a segment of the window ( [i,Board.WIDTH) )
                        searcher.search(createTemplate(window, i), hand);

                        if (searcher.HasWords()) // if the search found possible words, add all of them to the heap
                        {
                            square = new Location(x,y);
                            for (String s : searcher.GetAllWords())
                            {
                                maxHeap.add(new WordChoice(s, square, direction));
                            }
                        }
                    }
                    // if incrementing makes us lose a letter or adjacent, decrement the count associated
                    if (window[i] != '_' && window[i] == '*') letters--;
                    if (validSpots[x][y]) adjacents --;

                }while(++i < Board.WIDTH-2);

            }
        }

        while(!maxHeap.isEmpty()) // poll the best move and try it until it works
        {
            try
            {
                WordChoice wc = maxHeap.poll();
                gateKeeper.verifyLegality(wc.string,wc.start,wc.dir);
                return wc.publish();
            }
            catch (IllegalMoveException ignored) { }
        }

        // if no move was found we exchange
        return DoExchange();
    }

    /**
     * Create a sub-array of window that starts at start and ends at window.length
     * @param window array to generate a subarray from
     * @param start starting index for subarray
     * @return sub-array
     */
    private char[] createTemplate(char[] window, int start)
    {
        char[] ret = new char[Board.WIDTH-start];
        int k = 0;
        for (int i = start; i < Board.WIDTH; ++i)
        {
            ret[k] = window[i];
            k++;
        }
        return ret;
    }

    /**
     * Smart tile exchange.
     * @return an Exchange Tiles Scrabble Move
     */
    private ExchangeTiles DoExchange()
    {
        int [] letters = new int[26];
        int [] tempHand = new int[hand.size()];
        int temp = 0;
        int vcount = 0;
        boolean[] choice = new boolean[hand.size()];

        /* save hand in temp array, count vowels, and tally letters to see if there are duplicates in our hand */
        for (int i = 0; i < hand.size(); i++)
        {
            temp = hand.get(i) - 'a';

            tempHand[i] = temp;
            if (0 <= temp && temp <= 26)
            {
                letters[temp] += 1;
            }
            if (isVowel(temp))
            {
                vcount += 1;
            }
        }

        /* if there are duplicates of a letter, dump one of them */
        for (int i = 0; i < letters.length; i++)
        {
            int dump = letters[i] - 1;
            int j = 0;

            while (dump > 0 && j<hand.size())
            {
                if (tempHand[j] == i){
                    choice[j]=true;
                    --dump;
                }
                j++;
            }
        }

        /*
             Checks that we have less than 3 vowels
             (optimal is 3 vowels, 4 consonants) and if not,
             exchange the letters with the lowest frequency in the dictionary
         */
        boolean problem = vcount>3;
        int lowest =  Integer.MAX_VALUE;
        int lowIndex = -1;
        while ( vcount != 3)
        {
            for (int i = 0; i < hand.size(); i++)
            {
                if(tempHand[i] != -2 && isVowel(tempHand[i])==problem && searcher.GetFrequency(tempHand[i]) < lowest)
                {
                    lowest = searcher.GetFrequency(tempHand[i]);
                    lowIndex = i;

                }
            }
            if (lowIndex >= 0)
            {
                choice[lowIndex] = true;
            }
            vcount += problem ? -1 :1;
        }

        for (int i = 0; i < hand.size(); i++)
        {
            if (tempHand[i] == -2)
            {
                choice[i] = false;
            }
        }

        return new ExchangeTiles(choice);
    }

    /** check if letter in hand is vowel for doExchange **/
    private boolean isVowel(int letter)
    {
        return letter == 0 || letter == 4 || letter == 8 || letter == 14 || letter == 20;//a, e, i, o, u
    }

    /* Check to see if the square at x,y is adjacent to a letter*/
    private boolean isAdjacent(int x, int y)
    {
        Location up = new Location(x, y+1);
        Location down = new Location(x, y-1);
        Location left = new Location(x-1,y);
        Location right = new Location(x+1, y);

        return ((isOnBoard(right) && isLetter(gateKeeper.getSquare(right))) ||
                (isOnBoard(down) && isLetter(gateKeeper.getSquare(down))) ||
                (isOnBoard(up) && isLetter(gateKeeper.getSquare(up))) ||
                (isOnBoard(left) && isLetter(gateKeeper.getSquare(left))));
    }

    /* Boundary checking helper */
    private boolean isOnBoard(Location l)
    {
        int c = l.getColumn(), r = l.getRow();
        return (c < Board.WIDTH && r < Board.WIDTH && c > -1 && r > -1);
    }

    /**
     * @param c char to check
     * @return Is the letter c an alphabet letter?
     */
    private static boolean isLetter(char c)
    {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // Convert an uppercase or lowercase character to an int
    private static int charToInt(char c) {return c < 'a' ? c-'A' : c-'a'; }

    /* Used for storing possible moves which we find during our search */
    class WordChoice implements Comparable
    {
        private int score;
        private String string;
        private Location start;
        private Location dir;

        WordChoice(String s, Location start, Location direction)
        {
            string = s;
            this.score = gateKeeper.score(s,start,direction);
            this.start = start;
            this.dir = direction;
        }

        @Override
        public int compareTo(Object o)
        {
            return ((WordChoice)o).score - this.score;
        }

        public PlayWord publish()
        {
            return new PlayWord(string, start, dir);
        }
    }

    public static class Trie
    {
        private Node head;

        // stores the number of times each letter appears in the dictionary
        private int [] freqArr = new int[26];


        /**
         * initialize a trie that contains all line separated words in the infile
         * @param inFile specified 'dictionary'
         */
        Trie(In inFile)
        {
            head = new Node('\0');
            for (String line : inFile.readAllLines())
            {
                insert(line.trim());
            }
        }

        int GetFrequency(int index){ return freqArr[index]; }
        Node GetHead(){return head;}

        public void insert(String s) { insertHelp(s,0, head); }

        /**
         * @param s string to check against dictionary
         * @return is the string in the dictionary?
         */
        public boolean contains(String s) // used in our trie testing class
        {
            Node trav = head;
            for (int i = 0; i < s.length(); ++i)
            {
                int c = SmoothCriminal.charToInt(s.charAt(i));

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
                int letter = SmoothCriminal.charToInt(s.charAt(ind));
                if (n.children[letter] == null)
                {
                    n.children[letter] = new Node(s.charAt(ind));
                }
                n.children[letter] = insertHelp(s, ind + 1, n.children[letter]);

                n.children[letter].parent = n;

                // as we insert the nodes, we note the frequency at which each letter appears
                freqArr [letter] += 1;
            }
            return n;
        }

        // building blocks for the Trie
        static class Node
        {
            private char c;
            private boolean isWord = false;
            private Node[] children;
            private Node parent;

            // trie is composed of nodes. Each node has 26 children (one for each letter of the alphabet).
            // we keep track of parent to allow us to back track easily in our search
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

    /**
     * helper class to find the 'best', 'valid' word that fits the given 'template'
     * 'best': the word with maximum score
     * 'valid': the word is valid given its placement on the board
     * 'template': string in the form "_A____".
     *         in the above example, Searcher will search for words that are of length 6 and that have A as their second
     *         letter. Additionally, blanks will only be filled with characters in the 'bag'
     * 'bag': A length 7 string describing the letters that can be used to create words (the words in our hand)
     */
    public static class Searcher
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
        Searcher()
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
}

