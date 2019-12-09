import java.util.*;
import java.util.concurrent.Callable;


/**
 * helper class to find the 'best', 'valid' word that fits the given 'template'
 * 'best': the word with maximum score
 * 'valid': the word is valid given its placement on the board
 * 'template': string in the form "_A____".
 *         in the above example, AStarSearch will search for words that are of length 6 and that have A as their second
 *         letter. Additionally, blanks will only be filled with characters in the 'bag'
 * 'bag': A length 7 string describing the letters that can be used to create words
 *
 * 'charEval': A function that evaluates the value gained from putting a character in the board at a given location
 */

public class AStarSearch
{
    private int[] g;
    private int[] h;
    private HashMap<Character, Integer> bag;
    private String template;
    private Callable<Character> eval;
    private String bestWord;
    private int searchLen;


    public AStarSearch(String template, String hand, Callable<Character> charEval)
    {
        this.template = template;
        bag = new HashMap<>(26);

        for (char c : hand.toCharArray())
        {
            if(bag.containsKey(c)) bag.replace(c, bag.get(c)+1);

            bag.put(c,1);
        }

        eval = charEval;
        searchLen = template.length();
    }

    public Comparator<Integer> compareScores = new Comparator<Integer>()
    {
        @Override
        public int compare(Integer a, Integer b)
        {
            return (g[a]+h[a])-(g[b]+h[b]);
        }
    };

    public String search()
    {
        boolean[] finished;
        int[] cost;
        PriorityQueue<Integer> queue = new PriorityQueue<>(26, compareScores);



        return new String();
    }

    private void searchHelp()
    {

    }

    private int ScoreChar(char c)
    {
        return Board.TILE_VALUES.get(c);
    }
}
