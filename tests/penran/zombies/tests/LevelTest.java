package penran.zombies.tests;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import penran.zombies.ui.Level;

public class LevelTest {

  @Test
  public void load() throws IOException {
    Level l = Level.load(',',
                         new File("etc/towns"),
                         new File("etc/roads"));
    System.out.println(l);
  }
}
