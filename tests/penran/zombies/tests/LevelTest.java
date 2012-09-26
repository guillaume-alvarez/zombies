package penran.zombies.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import penran.zombies.ui.Level;

public class LevelTest {

  @Test
  public void load() throws IOException {
    System.out.println(Level.load(new File("etc/level.test")));
  }
}
