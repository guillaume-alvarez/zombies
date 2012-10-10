package penran.zombies.core;

/**
 * Some order to be executed by a {@link Character}.
 * 
 * @author Guillaume Alvarez
 */
public interface Mission extends GameAgent {

  /**
   * Stops the mission.
   */
  void stops();

}
