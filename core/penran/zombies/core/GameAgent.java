package penran.zombies.core;

/**
 * Identifies all game agents/actions and permit to make them 'tick' each turn
 * (as a real-time game is made of micro 'turns').
 * 
 * @author Guillaume Alvarez
 */
public interface GameAgent {

  /** Make the agent act. Stop if returns false. */
  boolean tick(World world);

}
