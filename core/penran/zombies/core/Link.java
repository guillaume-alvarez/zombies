package penran.zombies.core;

import static java.lang.Math.*;

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

  public final double distance;

  public final String name;

  private volatile double progressFromP1 = 0;

  private volatile double progressFromP2 = 0;

  public Link(String name, Place p1, Place p2) {
    this.name = name;
    this.p1 = p1;
    p1.addLink(this);
    this.p2 = p2;
    p2.addLink(this);
    this.distance = distance(p1.coordinates, p2.coordinates);
  }

  @Override
  public String toString() {
    return name + "[" + p1 + "->" + p2 + "]";
  }

  private static double distance(Coordinates c1, Coordinates c2) {
    return sqrt(pow(c1.latitude - c2.latitude, 2) + pow(c1.longitude - c2.longitude, 2));
  }

  public double getProgressFromP1() {
    return progressFromP1;
  }

  public double getProgressFromP2() {
    return progressFromP2;
  }

  void addProgress(Place p, double progress) {
    if (p == p1)
      progressFromP1 = Math.max(0.0, Math.min(1.0, progressFromP1 + progress));
    else if (p == p2)
      progressFromP2 = Math.max(0.0, Math.min(1.0, progressFromP2 + progress));
    else
      throw new IllegalStateException("Unknown place " + p + " for " + this);
  }

}
