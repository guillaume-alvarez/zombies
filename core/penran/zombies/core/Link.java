package penran.zombies.core;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Link between two places.
 * <p>
 * At the moment the object manages the distance and is accessible from both its
 * destinations. However experience shows a distance matric would be quite more
 * effective...
 * </p>
 * 
 * @author Guillaume Alvarez
 */
public final class Link {

  public final Place p1;

  public final Place p2;

  /** Distance between the two places, in km. */
  public final double distance;

  public final String name;

  /** In {@link #distance} unit. */
  private volatile double progressFromP1 = 0;

  /** In {@link #distance} unit. */
  private volatile double progressFromP2 = 0;

  public Link(String name, Place p1, Place p2) {
    this.name = name;
    this.p1 = p1;
    this.p2 = p2;
    this.distance = distance(p1.coordinates, p2.coordinates);
    p1.addLink(this);
    p2.addLink(this);
  }

  @Override
  public String toString() {
    return name + "[" + p1 + "->" + p2 + "]";
  }

  private static double distance(Coordinates c1, Coordinates c2) {
    return sqrt(pow(c1.latitude - c2.latitude, 2) + pow(c1.longitude - c2.longitude, 2));
  }

  public double getProgressFrom(Place p) {
    if (p == p1)
      return progressFromP1;
    else if (p == p2)
      return progressFromP2;
    else
      throw new IllegalStateException("Unknown place " + p + " for " + this);
  }

  /** Make the infection progress through the link. */
  /* package */double addProgress(Place p, double progress) {
    if (p == p1)
      return progressFromP1 = Math.max(0.0, Math.min(distance, progressFromP1 + progress));
    else if (p == p2)
      return progressFromP2 = Math.max(0.0, Math.min(distance, progressFromP2 + progress));
    else
      throw new IllegalStateException("Unknown place " + p + " for " + this);
  }

  /* package */void removeProgress(Place p) {
    if (p == p1)
      progressFromP1 = 0.0;
    else if (p == p2)
      progressFromP2 = 0.0;
    else
      throw new IllegalStateException("Unknown place " + p + " for " + this);
  }

  public Place otherPlace(Place p) {
    if (p == p1)
      return p2;
    else if (p == p2)
      return p1;
    else
      throw new IllegalStateException("Unknown place " + p + " for " + this);
  }

}
