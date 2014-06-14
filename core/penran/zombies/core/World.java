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

  private final Thread thread;

  private long tick;

  private volatile boolean started = false;

  /** Default speed is 1.0, 2.0 is two times faster, 0.0 is paused. */
  private double speed = 1.0;

  /** Pause synchronization. */
  private final Object paused = new Object();

  public World(Map<String, Place> places, List<Link> links) {
    this.places = places;
    this.links = links;

    for (Place p : places.values())
      if (p.hasZombies())
        addAgent(new PlaceContamination(p));

    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          loop();
        } catch (Exception e) {
          if (started)
            e.printStackTrace();
        }
      }
    }, "Zombies");
  }

  /** The tick is the time in ms between to game world updates. */
  public void start(@SuppressWarnings("hiding") long tick) {
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

  /** Get the list of all defined characters. */
  public Map<String, Character> getCharacters() {
    return characters;
  }

  /** Register a new character. */
  public void addCharacter(Character character) {
    characters.put(character.getName(), character);
  }

  /** Default speed is 1.0, 2.0 is two times faster, 0.0 is paused. */
  public void setSpeed(double speed) {
    synchronized (paused) {
      this.speed = Math.max(0.0, speed);
      if (speed > 0.0)
        paused.notifyAll();
    }
  }

  public void stop() {
    started = false;
    synchronized (paused) {
      paused.notifyAll();
    }
    try {
      thread.join(10000);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void loop() throws InterruptedException {
    long lastExecution = System.currentTimeMillis();
    while (started) {
      // wait while paused
      synchronized (paused) {
        while (speed == 0.0)
          paused.wait();
      }
      long nextTick = lastExecution + (long) (tick / speed);
      waitTick(nextTick);
      // finally execute the turn computation
      lastExecution = System.currentTimeMillis();
      runTurn();
    }
  }

  private void runTurn() {
    synchronized (agents) {
      agents.addAll(forNextTick);
      forNextTick.clear();
      for (Iterator<GameAgent> it = agents.iterator(); it.hasNext();)
        if (!it.next().tick(this))
          it.remove();
    }
  }

  private static void waitTick(long nextTick) {
    long toWait = nextTick - System.currentTimeMillis();
    if (toWait > 0) {
      try {
        Thread.sleep(toWait);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else
      System.out.println("Late by " + (-toWait) + "ms.");
  }

}
