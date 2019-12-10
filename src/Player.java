import java.util.*;
/*
    TODO:
    1. Searcher -> Gabe
    2. Evaluator + perpendicularTemplate -> Ryan
    3. Data structures in find loop + edge case verification -> Lexie
    4. DoExchange _> Falcon

 */



public class Player implements ScrabbleAI
{
    private GateKeeper gateKeeper;
    private Searcher searcher;

    private ArrayList<Character> hand;
    private int handSize;

    public Player()
    {
        searcher = new Searcher(new Eval());
    }

    @Override
    public void setGateKeeper(GateKeeper gateKeeper)
    {
        this.gateKeeper = gateKeeper;
    }

    @Override
    public ScrabbleMove chooseMove()
    {
        // 2D bool array of Board.WIDTH X Board.WIDTH
        HashSet<Location> validSpots = new HashSet<>(Board.WIDTH*Board.WIDTH);
        PriorityQueue<MoveChoice> maxHeap = new PriorityQueue<MoveChoice>();

        hand = gateKeeper.getHand();
        handSize = hand.size();

        String template; // "_A__" -> "LATE"


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
                        for (int i = 0; i < windowSize-1; i++)
                        {
                            if(direction == Location.VERTICAL) square = new Location(i,j);
                            else square = new Location(j,i);
                            char c =  gateKeeper.getSquare(square);
                            window[i] = isLetter(c) ? c : ' ';
                        }
                    }

                    // May be an ISSUE
                    for (int k = windowSize; k < Board.WIDTH-windowSize; k++)
                    {
                        // index for placing the next letter into our wrapping window
                        int p = ((windowSize-1)+templateStart) % windowSize;


                        if(direction == Location.VERTICAL) square = new Location(j,k);
                        else square = new Location(k,j);

                        char c = gateKeeper.getSquare(square);

                        window[p] = isLetter(c) ? c : '_';


                        boolean flag = false;
                        for (int i = 0; i < windowSize; i++)
                        {
                            Location l;
                            if(direction == Location.VERTICAL) l = new Location(j,k+i);
                            else l = new Location(k+i,j);

                            //account for this vvv
//                            if(isLetter(gateKeeper.getSquare(l)))
//                            {
//                               flag = false;
//                            }
                            if (validSpots.contains(l))
                            {
                                flag = true;
                                break;
                            }
                            else if(isAdjacent(l))
                            {
                                validSpots.add(l);
                                flag = true;
                                break;
                            }
                        }

                        if(!flag) continue;
                        StringBuilder sb;
                        String temp = buildTemplate(window,templateStart);
                        // Account for extra letters continuing downward or upward from temp!
                        /*
                            Check if top or bottom is adjacent to letter, if so traverse downwards/upwards
                            until we find a blank, adding each char to template.
                         */

                        //maxHeap.add(new MoveChoice(searcher.search(temp,hand),new Location(),direction));

                    }
                }
                // check for best word here
            }
        }
        /*
            1. For each 'template' (e.g. "A__L__"), find the best move
            2. Once all moves have been collected, pop the highest scoring move and play it
            3. If no moves were found, shuffle letters
         */
        // if no move has been found, exchange letters, otherwise return the best move
        if (maxHeap.isEmpty()) return DoExchange();
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

    private boolean isAdjacent(Location l)
    {
        int x = l.getRow();
        int y = l.getColumn();

        Location up = new Location(x, y+1);
        Location down = new Location(x, y-1);
        Location left = new Location(x-1,y);
        Location right = new Location(x+1, y);

        return ((isOnBoard(right) && isLetter(gateKeeper.getSquare(right))) ||
                (isOnBoard(down) && isLetter(gateKeeper.getSquare(down))) ||
                (isOnBoard(up) && isLetter(gateKeeper.getSquare(up))) ||
                (isOnBoard(left) && isLetter(gateKeeper.getSquare(left))));
    }

    private boolean isOnBoard(Location l)
    {
        int c =l.getColumn(), r = l.getRow();
        return (c < Board.WIDTH && r < Board.WIDTH && c > -1 && r > -1);
    }

    public static boolean isLetter(char c)
    {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }
    public static int CharToInt(char c) {return c < 'a' ? c-'A' : c-'a'; }

    public String buildTemplate(char[] window, int start)
    {
        StringBuilder bob = new StringBuilder(window.length);
        for (int i = start; i < window.length; i++)
        {
            bob.append(window[i]);
        }
        for (int i = 0; i < start; i++)
        {
            bob.append(window[i]);
        }
        return bob.toString();
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

    public class Eval implements Searcher.Evaluator
    {
        /**
         *
         * @param c character to be evaluated
         * @param row the row in which the character is to be placed
         * @param col the column in which the character is to be placed
         * @param isHorizontal is our search horizontal?
         * @return
         */
        @Override
        public int charEval(char c, int row, int col,boolean isHorizontal)
        {
            // Takes into account special tiles (double letter, triple letter),
            // creation of other words (words that are created when c is inserted and that are perpendicular to direction)
            // and the tiles inherent value
            return Board.TILE_VALUES.get(c);
        }
    }
}

