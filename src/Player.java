import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Player implements ScrabbleAI
{

    Trie trie;
    GateKeeper gateKeeper;
    ArrayList<Character> hand;
    int handSize;
    public Player()
    {
        In infile = new In("enable1.txt");
        trie = new Trie(infile);
    }

    @Override
    public void setGateKeeper(GateKeeper gateKeeper)
    {
        this.gateKeeper = gateKeeper;
        hand = gateKeeper.getHand();
        handSize = hand.size();
    }

    @Override
    public ScrabbleMove chooseMove()
    {
        hand = gateKeeper.getHand();
        String template;

        PriorityQueue<String> maxHeap = new PriorityQueue<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });

        for (Location direction: new Location[]{Location.VERTICAL, Location.HORIZONTAL})
        {
            for (int windowSize = handSize; windowSize >= 2 ; windowSize++)
            {
                // If direction is vertical, then j is a column and k is kth square in this column
                // If direction is horizontal, then j is a row and k is kth square in this row
                for (int j = 0; j < Board.WIDTH; j++)
                {
                    /*
                        Each window will be almost identical to the last except that the the second character is now the
                        first and the last character is a new character if one exists
                    */
                    Location square;
                    char[] templateCharacters = new char[windowSize]; // Initialize a 'window'
                    // denotes where the actual start of our template is, using this removes the need to rewrite elements
                    // of the template
                    int templateStart = 0;

                    {// initialize template
                        for (int i = 0; i < windowSize; i++)
                        {
                            if(direction == Location.VERTICAL) square = new Location(i,j);
                            else square = new Location(j,i);
                            char c =  gateKeeper.getSquare(square);
                            templateCharacters[i] = isLetter(c) ? c : '_';
                        }
                    }
                    for (int k = 0; k < Board.WIDTH-windowSize; k++)
                    {
                        if(direction == Location.VERTICAL) square = new Location(k,j);
                        else square = new Location(j,k);
                        // put this square at the location behind templateStart

                        // Create template string from templateCharacters then perform AStarSearch on it

                        //AStarSearch search = new AStarSearch(template, letterIsValid);

                    }
                }

            }
        }
        /*
            1. For each 'template' (e.g. "A__L__"), find the best move
            2. Once all moves have been collected, pop the highest scoring move and play it
            3. If no moves were found, shuffle letters
         */

        // If no word is found, shuffle hand
        return null;

    }

    /**
     * Scores a character based on the current board state
     * @param c Character to be scored
     * @param l Location of proposed placement
     * @param direction current searching direction
     * @return The total score gained from placing the character on the board
     */
    private int scoreChar(char c, Location l, Location direction)
    {
        // Takes into account special tiles (double letter, triple letter),
        // creation of other words (words that are created when c is inserted and that are perpendicular to direction)
        // and the tiles inherent value
        return Board.TILE_VALUES.get(c);
    }

    /**
     * checks if placing a character in a location is valid. It is invalid if placing the character in this spot creates
     * an invalid word in the direction adjacent to direction
     * @param c character to be checked
     * @param l location character is to placed
     * @param direction current searching direction
     * @return true or false if the tile is valid or not
     */
    private boolean letterIsValid(char c, Location l, Location direction)
    {
        return false;
    }

    private boolean isLetter(char c)
    {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public String buildTemplate(int i, int j, int length, Location direction)
    {
        return new String();
    }

    // returns a template string for the column/row perpendicular to location l
    // Only considers letters that are directly adjacent to l in the opposite(d) direction
    // used for validity checking
    public String perpendicularTemplate(Location l, Location d)
    {
        return new String();
    }

    class WordEntry implements Comparable
    {
        int score;
        String string;
        public WordEntry(String s, int i)
        {
            string = s;
            score = i;
        }

        @Override
        public int compareTo(Object o)
        {
            return 0;
        }

    }
}

