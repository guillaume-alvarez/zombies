package penran.zombies.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A place on the map.
 * 
 * @author Guillaume Alvarez
 */
public final class Place implements GameObject, Iterable<Link> {

  /** City contamination rate, here 0.01%. */
  private static final double CONTAMINATION_RATE = 0.0001;

  public final String name;

  public final int size;

  public final Coordinates coordinates;

  // TODO optimize by using a common structure for all links
  private final List<Link> links = new ArrayList<>();

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
   * to remove zombies from the city.
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
    if (zombies >= 1.0)
      // contaminate links
      for (Link l : links) {
        if (l.addProgress(this) >= l.distance)
          l.otherPlace(this).addZombies(CONTAMINATION_RATE);
      }
    else if (zombies > 0.0)
      // increase contamination in city
      addZombies(CONTAMINATION_RATE);
    else
      // links should not be infected
      for (Link l : links)
        l.removeProgress(this);
  }

  @Override
  public Iterator<Link> iterator() {
    return links.iterator();
  }

}
