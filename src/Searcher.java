import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Callable;

//StdOut.printf("|%c|", tempC);

/**
 * helper class to find the 'best', 'valid' word that fits the given 'template'
 * 'best': the word with maximum score
 * 'valid': the word is valid given its placement on the board
 * 'template': string in the form "_A____".
 *         in the above example, Searcher will search for words that are of length 6 and that have A as their second
 *         letter. Additionally, blanks will only be filled with characters in the 'bag'
 * 'bag': A length 7 string describing the letters that can be used to create words
 *
 * 'charEval': A function that evaluates the value gained from putting a character in the board at a given location
 */
public class Searcher
{
    private Trie trie;

    private String bestWord;
    private int bestScore = -1;

    private int targetLen; // Length of our desired word

    PriorityQueue<Integer>[] charScores; // PriorityQueue used for traversing trie

    private HashMap<Character, Integer> bag;
    private String template;

    int[][] evaluations; // Stores evaluations of characters (same dimension as charScores)

    private Location start;
    private Location dir;

    private int layer; // which letter of the word are we currently looking for

    private Evaluator eval;

    private boolean[] layerVisited;


    public Searcher(Evaluator e)
    {
        eval = e;
        trie = new Trie(new In("enable1.txt"));
    }

    public String GetBestWord() { return bestWord; }
    public int GetBestScore() { return bestScore; }

    private void init(String temp, ArrayList<Character> hand)
    {
        this.template = temp.toLowerCase();
        targetLen = template.length();

        bag = new HashMap<>();

        for (char c : hand)
        {
            c = Character.toLowerCase(c);
            if(bag.containsKey(c)) bag.replace(c, bag.get(c)+1);

            bag.put(c,1);
        }

        layerVisited = new boolean[targetLen];
        evaluations = new int[targetLen][26];

        charScores = new PriorityQueue[targetLen];

        for (int i = 0; i < targetLen; i++)
        {
            Arrays.fill(evaluations[i], -1);
        }
    }

    public void search(String template, ArrayList<Character> hand, Location searchStart, Location direction)
    {
        start = searchStart;
        dir = direction;
        init(template,hand);
        searchHelp();
    }

    private void searchHelp()
    {
        Trie.Node trav = trie.GetHead();
        Trie.Node tempNode;
        char[] target = template.toCharArray();
        int score = 0;

        char tempC;
        int tempVal;

        while(layer <= targetLen)
        {
            if(layer == targetLen) // if we are at our target length
            {
                if(trav.IsWord())
                {
                    bestWord = new String(target);
                    bestScore = score;
                    return;
                }

                layer -= 1;
                tempC = target[layer];

                // if letter came from our hand, add it back to hand
                if (!Player.isLetter(template.charAt(layer)))
                {
                    bag.replace(tempC, bag.get(tempC)+1);
                }

                trav = trav.GetParent();
                score -= evaluations[layer][Player.CharToInt(tempC)];
            }
            else if (Player.isLetter(template.charAt(layer)))
            {
                tempC = template.charAt(layer);
                tempNode = trav.GetChild(tempC);
                if (tempNode == null || layerVisited[layer])
                {
                    layerVisited[layer] = false;
                    trav = trav.GetParent();
                }
                else
                {
                    trav = tempNode;
                }
                layerVisited[layer] = true;
                layer+=1;
            }
            else
            {
                if (!layerVisited[layer])
                {
                    // add every non-null child to the queue
                    charScores[layer] = new PriorityQueue<Integer>(26, compareScores);
                    // CHANGE ME: loop through hand instead
                    for (int i = 0; i < 26; i++)
                    {
                        tempNode = trav.GetChild(i);
                        if (tempNode != null)
                        {
                            tempC = tempNode.Get();
                            // if the character in the child is in our hand and we have haven't already used it
                            if(bag.containsKey(tempC) && bag.get(tempC) > 0)
                            {
                                // add character to queue
                                charScores[layer].add(Player.CharToInt(tempNode.Get()));
                            }
                        }
                    }
                    layerVisited[layer] = true;
                }
                if(charScores[layer].isEmpty()) // if there are no possible paths to take
                {
                    if(layer == 0) return; // no word was found :(

                    layerVisited[layer] = false;
                    layer -= 1; // move up to the previous layer

                    tempC = target[layer];
                    if (!Player.isLetter(template.charAt(layer)))
                    {
                        bag.replace(tempC, bag.get(tempC)+1);
                    }

                    trav = trav.GetParent();
                    score -= evaluations[layer][Player.CharToInt(tempC)];
                }
                else // continue to the next character
                {
                    int val = charScores[layer].poll();
                    char let = (char)('a'+val);
                    target[layer] = let; // add letter to our template

                    bag.replace(let, bag.get(let)-1);// decrement value of character in bag
                    score += evaluations[layer][val];

                    trav = trav.GetChild(val);
                    layer += 1;
                }
            }
        }
    }

    private Comparator<Integer> compareScores = new Comparator<Integer>()
    {
        @Override
        public int compare(Integer a, Integer b)
        {
            if(evaluations[layer][a] == -1 || evaluations[layer][b] == -1)
            {
                Location location;
                int sA, sB;
                if ((dir == Location.HORIZONTAL))
                {
                    location = new Location(start.getRow(), start.getColumn() + layer);
                }
                else
                {
                    location = new Location(start.getRow() + layer, start.getColumn());
                }

                if(evaluations[layer][a] == -1)
                {
                    sA = eval.charEval((char) ('a' + a), location, dir);
                    evaluations[layer][a] = sA;
                }
                if(evaluations[layer][a] == -1)
                {
                    sB = eval.charEval((char) ('a' + b), location, dir);
                    evaluations[layer][b] = sB;
                }
            }
            return evaluations[layer][a] - evaluations[layer][b];
        }
    };

    private static int ScoreChar(char c)
    {
        return Board.TILE_VALUES.get(c);
    }

    public interface Evaluator
    {
        int charEval(char c, Location l, Location dir);
    }

    public static class SimpleScorer implements Evaluator
    {
        @Override
        public int charEval(char c, Location l, Location dir)
        {
            return ScoreChar(c);
        }
    }
}
