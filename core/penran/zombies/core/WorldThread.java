package penran.zombies.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Calls for updates on the world at regular intervals.
 * 
 * @author Guillaume Alvarez
 */
public final class WorldThread {

  private final Thread thread;

  private long tick;

  private volatile boolean started = false;

  /** Default speed is 1.0, 2.0 is two times faster, 0.0 is paused. */
  private double speed = 1.0;

  /** Pause synchronization. */
  private final Object paused = new Object();

  private final World world;

  public WorldThread(World world) {
    this.world = world;
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
    } catch (InterruptedException e) {
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
      world.update();
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
