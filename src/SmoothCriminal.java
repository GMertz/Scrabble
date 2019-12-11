import java.util.*;

public class SmoothCriminal implements ScrabbleAI
{
    private GateKeeper gateKeeper;
    private Searcher searcher;

    private ArrayList<Character> hand;
    private boolean[][] validSpots; // spots where a letter can be placed (adjacent to at least 1 letter)

    public SmoothCriminal()
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
        /* We save a little time by valid spots when we find them*/
        validSpots = new boolean[Board.WIDTH][Board.WIDTH];

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
            catch (IllegalMoveException e) { }
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
    public char[] createTemplate(char[] window, int start)
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
    public ExchangeTiles DoExchange()
    {
        int [] letters = new int[26];
        int [] temphand = new int[hand.size()];
        int temp = 0;
        int vcount = 0;
        boolean[] choice = new boolean[hand.size()];

        /* save hand in temp array, count vowels, and tally letters to see if there are duplicates in our hand */
        for (int i = 0; i < hand.size(); i++)
        {
            temp = hand.get(i) - 'a';

            temphand[i] = temp;
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
                    if (temphand[j] == i){
                        choice[j]=true;
                        --dump;
                    }
                    j++;
                }
        }

        /*
            Checks that we have less than 3 vowels
         * (optimal is 3 vowels, 4 consonants) and if not,
         * exchange the letters with the lowest frequency in the dictionary
         * */
        boolean problem = vcount>3;
        int lowest =  Integer.MAX_VALUE;
        int lowIndex = -1;
        while ( vcount != 3)
        {
            for (int i = 0; i < hand.size(); i++)
            {
                if(temphand[i] != -2 && isVowel(temphand[i])==problem && searcher.GetFrequency(temphand[i]) < lowest)
                {
                    lowest = searcher.GetFrequency(temphand[i]);
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
            if (temphand[i] == -2)
            {
                choice[i] = false;
            }
        }

        return new ExchangeTiles(choice);
    }

    /** check if letter in hand is vowel for doExchange **/
    public boolean isVowel(int letter)
    {
        if (letter == 0 || letter == 4 || letter == 8 || letter == 14 || letter == 20){ //a, e, i, o, u
            return true;
        }
        return false;
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
    static boolean isLetter(char c)
    {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    // Convert an uppercase or lowercase character to an int
    static int CharToInt(char c) {return c < 'a' ? c-'A' : c-'a'; }

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
}

