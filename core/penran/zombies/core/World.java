package penran.zombies.core;

import java.util.ArrayList;
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

  private final List<Link> links;

  /**
   * Contains all acting objects. It should be synchronized on itself when
   * accessed.
   */
  private final List<GameAgent> agents = new ArrayList<>();

  private final List<GameAgent> forNextTick = new ArrayList<>();

  private final Thread thread;

  private long tick;

  private volatile boolean started = false;

  public World(Map<String, Place> places, List<Link> links) {
    this.places = places;
    this.links = links;

    for (Place p : places.values())
      if (p.hasZombies())
        agents.add(new PlaceContamination(p));

    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        loop();
      }
    }, "Zombies");
  }

  /** The tick is the time in ms between to game world updates. */
  public void start(long tick) {
    this.tick = tick;
    this.started = true;
    thread.start();
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

  public void stop() {
    started = false;
    try {
      thread.join(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void loop() {
    long nextTick = System.currentTimeMillis() + tick;
    while (started) {
      waitTick(nextTick);
      nextTick = System.currentTimeMillis() + tick;
      synchronized (agents) {
        agents.addAll(forNextTick);
        forNextTick.clear();
        for (Iterator<GameAgent> it = agents.iterator(); it.hasNext();)
          if (!it.next().tick(this))
            it.remove();
      }
    }
  }

  private static void waitTick(long nextTick) {
    long toWait = nextTick - System.currentTimeMillis();
    if (toWait > 0)
      try {
        Thread.sleep(toWait);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    else
      System.out.println("Late by " + (-toWait) + "ms.");
  }
}
