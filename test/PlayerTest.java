import edu.princeton.cs.algs4.StdOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    Player player;

    @BeforeEach
    void setup()
    {
        player = new Player();
    }

    //@Test
    void buildTemplate()
    {
        char[] TestWindow = new char[]{'E','R','T','s','G','A','B','g','M'};
        int start = 4;
        //assertTrue(player.buildTemplate(TestWindow,start).equals("GABgMERTs"));
    }

    @Test
    void createTemplate() {
    }

    @Test
    void doExchange() {
        char[] sampleHand = new char[] {'h', 'i', 'p', 'e', 't', 'e', 'r'};

        player.hand = new ArrayList<Character>();//Arrays.asList(sampleHand);
        for (char c :
                sampleHand) {
            player.hand.add(c);
        }
        boolean[] choices = player.DoExchange();


        for (int i = 0; i < choices.length; i++) {
            StdOut.print(choices[i]);
        }

    }

    @Test
    void isVowel() {
        int letter = 'A'-'a';

        assertTrue(player.isVowel(letter));

    }
}