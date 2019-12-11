import java.util.*;

public class Player implements ScrabbleAI
{
    private GateKeeper gateKeeper;
    private Searcher searcher;

    private ArrayList<Character> hand;
    private int handSize;
    boolean[][] validSpots;

    public Player()
    {
        searcher = new Searcher();
    }

    @Override
    public void setGateKeeper(GateKeeper gateKeeper)
    {
        this.gateKeeper = gateKeeper;
    }

    @Override
    public ScrabbleMove chooseMove()
    {
        validSpots = new boolean[Board.WIDTH][Board.WIDTH];

        if (!isLetter(gateKeeper.getSquare(Location.CENTER)))
        {
            validSpots[Location.CENTER.getRow()][Location.CENTER.getColumn()] = true;
        }

        PriorityQueue<WordChoice> maxHeap = new PriorityQueue<>();

        hand = gateKeeper.getHand();
        handSize = hand.size();

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
        /*
            1. For each 'template' (e.g. "A__L__"), find the best move
            2. Once all moves have been collected, pop the highest scoring move and play it
            3. If no moves were found, shuffle letters
         */
        // if no move has been found, exchange letters, otherwise return the best move

        while(!maxHeap.isEmpty())
        {
            try
            {
                WordChoice wc = maxHeap.poll();
                gateKeeper.verifyLegality(wc.string,wc.start,wc.dir);
                return wc.publish();
            }
            catch (IllegalMoveException e) { }
        }
        return DoExchange();

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
    public ExchangeTiles DoExchange()
    {
        int [] letters = new int[26];
        int [] temphand = new int[hand.size()];
        int temp = 0;
        int vcount = 0;
        boolean[] choice = new boolean[hand.size()];

        for (int i = 0; i < hand.size(); i++) {

            temp = hand.get(i) - 'a';

            temphand[i] = temp;
            if (0 <= temp && temp <= 26) {
                ++letters[temp];
            }
            if (isVowel(temp)){
             ++vcount;
            }
        }

        for (int i = 0; i < letters.length; i++) {
                int dump = letters[i] - 1;
                int j = 0;

                while (dump > 0 && j<hand.size()){
                    if (temphand[j] == i){
                        choice[j]=true;
                        --dump;
                    }
                    j++;
                }

        }
        boolean problem = vcount>3;
        int lowest =  Integer.MAX_VALUE;
        int lowIndex = -1;
        while (vcount!=3){
            for (int i = 0; i < hand.size(); i++) {
                if(temphand[i] != -2 && isVowel(temphand[i])==problem && searcher.getFreaqy(temphand[i])<lowest){
                    lowest=searcher.getFreaqy(temphand[i]);
                    lowIndex = i;

                }
            }
            if (lowIndex>=0){
                choice[lowIndex] = true;
            }
            vcount += problem ? -1 :1;
        }

        for (int i = 0; i < hand.size(); i++) {
            if (temphand[i] == -2){
                choice[i] = false;
            }
        }

        return new ExchangeTiles(choice);

    }

    public boolean isVowel(int letter) {
        if (letter == 0 || letter == 4 || letter == 8 || letter == 14 || letter == 20){ //0, 4, 8, 14, 20
            return true;
        }
        return false;
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
    public char[] perpendicularTemplate(int row, int col, Location d)
    {
        if (!validSpots[row][col]) return new char[]{' '}; //no adjacent characters

        Stack<Character> sb1 = new Stack<>();
        Queue<Character> sb2 = new LinkedList<>();

        char c;
        Location perp = d.opposite();
        int dX = d.getRow();
        int dY = d.getColumn();

        int x;
        int y;
        x = row-dX;
        y = row-dY;
        while ((dX > 0 && dY > 0) && isLetter(c = gateKeeper.getSquare(new Location(x, y))))
        {
            sb1.push(c);
            x -= dX;
            y -=dY;
        }

        x = row+dX;
        y = row+dY;
        while ((dX < Board.WIDTH-1 && dY < Board.WIDTH-1) && isLetter(c = gateKeeper.getSquare(new Location(x, y))))
        {
            sb2.add(c);
            x += dX;
            y += dY;
        }

        char[] ret = new char[sb1.size()+sb1.size()+2];

        ret[0] = (char)sb1.size();
        int i = 1;
        while(!sb1.isEmpty())
        {
            ret[i] = sb1.pop();
            i+=1;
        }
        ret[i] = ' ';
        while(!sb2.isEmpty())
        {
            ret[i] = sb2.poll();
            i+=1;
        }
        return ret;
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

