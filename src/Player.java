import java.util.*;

public class Player implements ScrabbleAI
{

    private Trie trie;
    private GateKeeper gateKeeper;
    private ArrayList<Character> hand;
    private int handSize;
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

        PriorityQueue<MoveChoice> maxHeap = new PriorityQueue<MoveChoice>();

        for (Location direction: new Location[]{Location.VERTICAL, Location.HORIZONTAL})
        {
            for (int windowSize = handSize; windowSize >= 2 ; windowSize--)
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
                    char[] window = new char[windowSize]; // Initialize a 'window'
                    // denotes where the actual start of our template is, using this removes the need to rewrite elements
                    // of the template
                    int templateStart = 0;

                    {// initialize window
                        for (int i = 0; i < windowSize; i++)
                        {
                            if(direction == Location.VERTICAL) square = new Location(i,j);
                            else square = new Location(j,i);
                            char c =  gateKeeper.getSquare(square);
                            window[i] = isLetter(c) ? c : ' ';
                        }
                    }

                    for (int k = 0; k < Board.WIDTH-windowSize; k++)
                    {
                        if(direction == Location.VERTICAL) square = new Location(k,j);
                        else square = new Location(j,k);
                        // put this square at the location behind templateStart

                        // Create template string from templateCharacters then perform AStarSearch on it

                        //AStarSearch search = new AStarSearch(template, letterIsValid);
                        // if we find a word, to save time we can break out of
                    }
                }

            }
        }
        /*
            1. For each 'template' (e.g. "A__L__"), find the best move
            2. Once all moves have been collected, pop the highest scoring move and play it
            3. If no moves were found, shuffle letters
         */
        // if no move has been found, exchange letters, otherwise return the best move
        if (maxHeap.isEmpty()) return  DoExchange();
        return maxHeap.poll().publish();
    }

    // this could probably be improved by throwing out certain letters over others
    // for now it does every tile
    private ExchangeTiles DoExchange()
    {
        boolean[] choice = new boolean[handSize];
        Arrays.fill(choice,true);

        return new ExchangeTiles(choice);
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

    class MoveChoice implements Comparable
    {
        private int score;
        private String string;
        private Location start;
        private Location dir;

        public MoveChoice(String s, int score, Location start, Location direction)
        {
            string = s;
            this.score = score;
            this.start = start;
            this.dir = direction;
        }

        @Override
        public int compareTo(Object o)
        {
            return ((MoveChoice)o).score - this.score;
        }

        public PlayWord publish()
        {
            return new PlayWord(string, start, dir);
        }

    }
}

