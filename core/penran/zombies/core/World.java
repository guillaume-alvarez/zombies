package penran.zombies.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manages and update the game world at a certain rate.
 * 
 * @author Guillaume Alvarez
 */
public final class World {

  private final Map<String, Place> places;

  @SuppressWarnings("unused")
  private final List<Link> links;

  /**
   * Contains all acting objects. It should be synchronized on itself when
   * accessed.
   */
  private final List<GameAgent> agents = new ArrayList<>();

  private final Map<String, Character> characters = new HashMap<>();

  private final List<GameAgent> forNextTick = new ArrayList<>();

  public World(Map<String, Place> places, List<Link> links) {
    this.places = places;
    this.links = links;

    for (Place p : places.values())
      if (p.hasZombies())
        addAgent(new PlaceContamination(p));
  }

  public void addAgent(GameAgent ga) {
    synchronized (agents) {
      forNextTick.add(ga);
    }
  }

  /** Get a global contamination percentage. */
  public double getContamination() {
    double contaminated = 0;
    double size = 0;
    for (Place p : places.values()) {
      contaminated += p.getZombies() * p.size;
      size += p.size;
    }
    return Math.max(0.0, Math.min(1.0, contaminated / size));
  }

  /** Get the list of all defined characters. */
  public Map<String, Character> getCharacters() {
    return characters;
  }

  /** Register a new character. */
  public void addCharacter(Character character) {
    characters.put(character.getName(), character);
  }

  public void update() {
    synchronized (agents) {
      agents.addAll(forNextTick);
      forNextTick.clear();
      for (Iterator<GameAgent> it = agents.iterator(); it.hasNext();)
        if (!it.next().tick(this))
          it.remove();
    }
  }

}
