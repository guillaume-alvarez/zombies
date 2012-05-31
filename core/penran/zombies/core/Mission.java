package penran.zombies.core;

/**
 * Some order to be executed by a {@link Character}.
 * 
 * @author Guillaume Alvarez
 */
public interface Mission {

	/**
	 * Called to make the mission progress one tick later.
	 */
	void tick();

	/**
	 * Stops the mission.
	 */
	void stops();

}
