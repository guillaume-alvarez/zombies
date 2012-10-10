package penran.zombies.core;

/**
 * Slowly contaminates a place.
 * 
 * @author Guillaume Alvarez
 */
public final class PlaceContamination implements GameAgent {

  /** City contamination rate, here 0.01%. */
  private static final double CONTAMINATION_RATE = 0.0001;

  private final Place p;

  public PlaceContamination(Place p) {
    this.p = p;
  }

  /** If there are zombies, they gain 0.01% per turn. */
  @Override
  public boolean tick(World world) {
    if (p.addZombies(CONTAMINATION_RATE) < 1.0)
      return true;

    // finished contamination
    for (Link l : p) {
      if (l.getProgressFrom(p) < l.distance)
        world.addAgent(new LinkContamination(l, p, l.otherPlace(p)));
    }
    return false;
  }

  @Override
  public String toString() {
    return "Contaminate[" + p + "]";
  }

}
