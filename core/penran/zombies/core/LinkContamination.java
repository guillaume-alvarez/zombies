package penran.zombies.core;

/**
 * Contaminates the link until it reaches the other {@link Place}, then launch
 * other {@link Place} contamination.
 * 
 * @author Guillaume Alvarez
 */
public final class LinkContamination implements GameAgent {

  /** Distance the infection progresses by tick, here 0.1km. */
  private static final double INFECTION_PROGRESS = 0.1;

  private Link l;

  private Place source;

  private Place target;

  public LinkContamination(Link l, Place source, Place target) {
    this.l = l;
    this.source = source;
    this.target = target;
  }

  @Override
  public boolean tick(World world) {
    if (l.addProgress(source, INFECTION_PROGRESS) < l.distance)
      return true;

    // finished contamination
    if (target.getZombies() < 1.0)
      world.addAgent(new PlaceContamination(target));
    return false;
  }

  @Override
  public String toString() {
    return "Contaminate[" + source + "=>" + target + "]";
  }

}
