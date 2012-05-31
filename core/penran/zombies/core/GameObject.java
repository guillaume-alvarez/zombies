package penran.zombies.core;

/**
 * Identifies all game objects and permit to make them 'tick' each turn (as a
 * real-time game is made of micro 'turns').
 * 
 * @author Guillaume Alvarez
 */
public interface GameObject {

	void tick();

}
