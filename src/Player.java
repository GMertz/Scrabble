import java.util.ArrayList;

public class Player implements ScrabbleAI
{

    Trie trie;
    public Player()
    {
        In infile = new In("enable1.txt");
        trie = new Trie(DICTIONARY.infile);
    }

    @Override
    public void setGateKeeper(GateKeeper gateKeeper)
    {

    }

    @Override
    public ScrabbleMove chooseMove() {
        return null;
    }
}
