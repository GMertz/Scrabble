import java.util.*;
/*
    TODO:
    1. Searcher -> Gabe
    2. Evaluator + perpendicularTemplate -> Ryan
    3. Data structures in find loop + edge case verification -> Lexie
    4. DoExchange -> Falcon

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
        boolean[][] validSpots = new boolean[Board.WIDTH][Board.WIDTH];

        if (!isLetter(gateKeeper.getSquare(Location.CENTER)))
        {
            validSpots[Location.CENTER.getRow()][Location.CENTER.getColumn()] = true;
            StdOut.println("Is first turn");
        }

        PriorityQueue<WordChoice> maxHeap = new PriorityQueue<>();

        hand = gateKeeper.getHand();
        handSize = hand.size();

        String template; // "_A__" -> "LATE"

        for (Location direction: new Location[]{Location.VERTICAL, Location.HORIZONTAL})
        {
                // If direction is vertical, then j is a column and k is kth square in this column
                // If direction is horizontal, then j is a row and k is kth square in this row
                for (int j = 0; j < Board.WIDTH; j++)
                {
                    Location square;
                    // the window represents a row or a column (dependant on our search direction)
                    char[] window = new char[Board.WIDTH];
                    int letters = 0, adjacents = 0;

                    // Fill the window, count letters
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
                            window[i] = '*';
                            adjacents += 1;
                        }
                        else
                        {
                            window[i] = '_';
                        }
                    }
                    int i = 0;
                    do // generate all templates
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

                        if(letters >= (Board.WIDTH - i) || adjacents <= 0) break;
                        if(i == 0 || window[i-1] == '_' || window[i-1] == '*' )
                        {
                            square = new Location(x,y);
                            searcher.search(createTemplate(window, i), hand, square ,direction);
                            if (searcher.HasWords())
                            {
                                WordChoice wc = new WordChoice(searcher.GetBestWord(), searcher.GetBestScore(), square, direction);
                                maxHeap.add(wc);
                            }
                        }
                        if (!(window[i] == '_' || window[i] == '*')) letters--;
                        if (validSpots[x][y]) adjacents --;
                    }while(++i < Board.WIDTH);

                }
        }
        if (maxHeap.isEmpty()) return DoExchange();
        return maxHeap.poll().publish();
    }
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


    // this could probably be improved by throwing out certain letters over others
    // for now it does every tile
    private ExchangeTiles DoExchange()
    {
        boolean[] choice = new boolean[handSize];
        Arrays.fill(choice,true);

        return new ExchangeTiles(choice);
    }

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

    // returns a template string for the column/row perpendicular to location l
    // Only considers letters that are directly adjacent to l in the opposite(d) direction
    // used for validity checking
    public String perpendicularTemplate(Location l, Location d)
    {
        return new String();
    }

    class WordChoice implements Comparable
    {
        private int score;
        private String string;
        private Location start;
        private Location dir;

        public WordChoice(String s, int score, Location start, Location direction)
        {
            string = s;
            this.score = score;
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
            // IMPORTANT: We need to find a way to take blank characters into account
            return Board.TILE_VALUES.get(c);
        }
    }
}

