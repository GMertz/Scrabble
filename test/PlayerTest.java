import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    Player player;

    @BeforeEach
    void setup()
    {
        player = new Player();
    }

    @Test
    void buildTemplate()
    {
        char[] TestWindow = new char[]{'E','R','T','s','G','A','B','g','M'};
        int start = 4;
        assertTrue(player.buildTemplate(TestWindow,start).equals("GABgMERTs"));
    }
}