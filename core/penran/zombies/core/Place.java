package penran.zombies.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * A place on the map.
 * 
 * @author Guillaume Alvarez
 */
public final class Place implements Iterable<Link> {

  public final String name;

  public final int size;

  public final Coordinates coordinates;

  // TODO optimize by using a common structure for all links
  private final List<Link> links = new ArrayList<>();

  private final Comparator<Link> linkOrder = new LinkClockwiseComparator();

  /** Percentage of zombies here. Must be volatile to be read from UI thread. */
  private volatile double zombies = 0;

  public Place(String name, int size, double zombies, Coordinates coordinates) {
    this.name = name;
    this.size = size;
    this.coordinates = coordinates;
    addZombies(zombies);
  }

  private final class LinkClockwiseComparator implements Comparator<Link> {
    @Override
    public int compare(final Link l1, final Link l2) {
      // extract coordinates
      final Coordinates c1 = l1.otherPlace(Place.this).coordinates;
      final Coordinates c2 = l2.otherPlace(Place.this).coordinates;
      final Coordinates c = coordinates;

      // convert to angle notation, center is this place
      final double a1 = Math.atan2(c1.latitude - c.latitude, c1.longitude - c.longitude);
      final double a2 = Math.atan2(c2.latitude - c.latitude, c2.longitude - c.longitude);

      // just compare angle (radius does not matter here)
      return Double.compare(a1, a2);
    }
  }

  /**
   * Register a link in this place. Make sure the links are registered in
   * clockwise order. It will be required when displaying it and to have
   * guaranteed update order for debugging.
   */
  /* package */void addLink(Link link) {
    final int pos = Collections.binarySearch(links, link, linkOrder);
    if (pos >= 0)
      links.add(pos, link);
    else
      links.add(-pos - 1, link);
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
  /* package */double addZombies(double percent) {
    if (percent > 0)
      zombies = Math.min(1.0, zombies + percent);
    else if (percent < 0)
      zombies = Math.max(0.0, zombies + percent);
    return zombies;
  }

  @Override
  public Iterator<Link> iterator() {
    return links.iterator();
  }

}
