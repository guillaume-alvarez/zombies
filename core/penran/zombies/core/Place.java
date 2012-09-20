package penran.zombies.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A place on the map.
 * 
 * @author Guillaume Alvarez
 */
public final class Place implements GameObject {

  public final String name;

  public final int size;

  public final Coordinates coordinates;

  // TODO optimize by using a common structure for all links
  private final List<Link> links = new ArrayList<Link>();

  /** Percentage of zombies here. Must be volatile to be read from UI thread. */
  private volatile double zombies = 0;

  public Place(String name, int size, double zombies, Coordinates coordinates) {
    this.name = name;
    this.size = size;
    this.coordinates = coordinates;
    addZombies(zombies);
  }

  /**
   * Register a link in this place.
   */
  /* package */void addLink(Link link) {
    links.add(link);
  }

  public boolean hasZombies() {
    return zombies > 0;
  }

  public double getZombies() {
    return zombies;
  }

  /**
   * Add a percentage to current zombie presence. The percentage can be negative
   * to avoid
   */
  void addZombies(double percent) {
    if (percent > 0)
      zombies = Math.min(1.0, zombies + percent);
    else if (percent < 0)
      zombies = Math.max(0.0, zombies + percent);
  }

  /** If there are zombies, they gain 0.01% per turn. */
  @Override
  public void tick() {
    if (zombies > 0 && zombies < 1)
      addZombies(0.0001);
  }

}
