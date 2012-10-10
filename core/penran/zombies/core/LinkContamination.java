package penran.zombies.core;

/**
 * Contaminates the link until it reaches the other {@link Place}, then launch
 * other {@link Place} contamination.
 * 
 * @author Guillaume Alvarez
 */
public final class LinkContamination implements GameAgent {

  /** Road contamination rate, here 0.01%. */
  private static final double CONTAMINATION_RATE = 0.0001;

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
    if (l.addProgress(source) < l.distance)
      return true;

    // finished contamination
    world.addAgent(new PlaceContamination(target));
    return false;
  }

}
