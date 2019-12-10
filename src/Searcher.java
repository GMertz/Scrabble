import java.util.*;


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

    private char[] bestWord;
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

    public String GetBestWord() { return new String(bestWord); }
    public int GetBestScore() { return bestScore; }

    private void init(String temp, ArrayList<Character> hand)
    {
        this.template = temp.toLowerCase();
        targetLen = template.length();
        bestWord = template.toCharArray();

        bag = new HashMap<>();

        for (char c : hand)
        {
            c = Character.toLowerCase(c);
            if(bag.containsKey(c)) bag.replace(c, bag.get(c)+1);
            else bag.put(c,1);
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
        char tempC;

        while(layer <= targetLen)
        {
            //StdOut.printf("\n----------Layer: %d [%s]\n---------",layer,new String(bestWord));
            if(layer == targetLen) // if we are at our target length
            {
                if(trav.IsWord())
                {
                    return;
                }
                DecrementLayer();
                trav = trav.GetParent();
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
                    charScores[layer] = new PriorityQueue<Integer>(26, new CompareScores());
                    ScoreLetters(trav);
                    layerVisited[layer] = true;
                }
                if(charScores[layer].isEmpty()) // if there are no possible paths to take
                {
                    if(layer == 0) return; // no word was found :(

                    layerVisited[layer] = false;
                    DecrementLayer();
                    trav = trav.GetParent();
                }
                else // continue to the next character
                {
                    int val = charScores[layer].poll();
                    IncrementLayer(val);
                    trav = trav.GetChild(val);
                }
            }
        }
    }

    private void evaluate(int a)
    {
        if(evaluations[layer][a] == -1)
        {
            Location location;
            int sA;
            if ((dir == Location.HORIZONTAL))
            {
                location = new Location(start.getRow(), start.getColumn() + layer);
            }
            else
            {
                location = new Location(start.getRow() + layer, start.getColumn());
            }
            sA = eval.charEval((char) ('a' + a), location, dir);
            evaluations[layer][a] = sA;
            //StdOut.printf("|%d, %d| ",a, sA);
        }
        else
        {
            //StdOut.printf("{%d} ",evaluations[layer][a]);
        }
    }


    private void ScoreLetters(Trie.Node trav)
    {
        Trie.Node tempNode;
        int t;  
        for (Character piece : bag.keySet())
        {
            if(bag.get(piece) <= 0) continue;

            if (piece == '_')
            {
                for (int i = 0; i < 26; i++)
                {
                    tempNode = trav.GetChild(i);
                    if (tempNode != null)
                    {
                        t = Player.CharToInt(tempNode.Get());
                        evaluate(t);
                        charScores[layer].add(t);
                    }
                }
                break;
            }
            else
            {
                tempNode = trav.GetChild(Player.CharToInt(piece));
                if (tempNode != null)
                {
                    t = Player.CharToInt(tempNode.Get());
                    evaluate(t);
                    charScores[layer].add(t);
                }
            }
        }
    }

    private void DecrementLayer()
    {
        layer -= 1;
        char tempC = bestWord[layer];

        if (!Player.isLetter(template.charAt(layer)))
        {
            if (bag.containsKey(tempC))
            {
                bag.replace(tempC, bag.get(tempC)+1);
            }
            else
            {
                bag.replace('_', bag.get('_')+1);
            }
        }
        bestScore -= evaluations[layer][Player.CharToInt(tempC)];
    }

    private void IncrementLayer(int val)
    {
        char let = (char)('a'+val);
        bestWord[layer] = let; // add letter to our template

        // if letter is in our hand (bag) then decrement it, otherwise decrement '_'
        if (bag.containsKey(let))
        {
            bag.replace(let, bag.get(let)-1);
        }
        else
        {
            bag.replace('_', bag.get('_')-1);
        }

        bestScore += evaluations[layer][val];
        layer += 1;
    }

    class CompareScores implements Comparator<Integer>
    {
        @Override
        public int compare(Integer a, Integer b)
        {
            return evaluations[layer][b] - evaluations[layer][a];
        }
    }

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
