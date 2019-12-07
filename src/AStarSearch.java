import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
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
    private String bag;
    private String template;
    private int nWords = 1;
    private Callable<Character> eval;

    private String[] foundWords;
    private String bestWord;


    public AStarSearch(String template, String bag, Callable<Character> charEval)
    {
        if (nWords > 1)
        {
            foundWords = new String[nWords];
        }
        this.template = template;
        this.bag = bag;
        eval = charEval;
    }

    public AStarSearch(String template, String bag, Callable<Character> charEval, int nWords)
    {
        if (nWords > 1)
        {
            foundWords = new String[nWords];
        }
        this.template = template;
        this.bag = bag;
        this.nWords = nWords;
        eval = charEval;
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
}
