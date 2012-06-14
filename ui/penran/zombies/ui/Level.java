package penran.zombies.ui;

import static penran.zombies.ui.CSVParser.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import penran.zombies.ui.CSVParser.Builder;
import penran.zombies.ui.CSVParser.Column;

/**
 * Temporary representation of a level to use for initial dev of the main view.
 *
 * @author gaetan
 */
public class Level {

  public static class Town {
    public final double latitude;

    public final double longitude;

    public final int size;

    public final String name;

    private Town(String name, double latitude, double longitude, int size) {
      this.name = name;
      this.latitude = latitude;
      this.longitude = longitude;
      this.size = size;
    }

    @Override
    public String toString() {
      return "Town [name=" + name + ", size=" + size + ", latitude=" + latitude
          + ", longitude=" + longitude + "]";
    }

    @Builder
    public static Town make(@Column("name") String name,
                            @Column("latitude") double latitude,
                            @Column("longitude") double longitude,
                            @Column("size") int size) {
      return new Town(name, latitude, longitude, size);
    }
  }

  public static class Road {
    public final String name;

    // there can be many town connected by one road (think crossroads), may not
    // be useful, and currently consider only 2
    public final List<Town> endPoints;

    private Road(String name, List<Town> endPoints) {
      this.name = name;
      this.endPoints = Collections.unmodifiableList(new ArrayList<>(endPoints));
    }

    @Override
    public int hashCode() {
      return endPoints.hashCode();
    }

    @Override
    public boolean equals(Object b) {
      return b instanceof Road && endPoints.equals(((Road) b).endPoints);
    }

    @Override
    public String toString() {
      return "Road [name=" + name + ", endPoints=" + endPoints + "]";
    }
  }

  public static class RoadBuilder {
    public final Map<String, Town> towns;

    private RoadBuilder(Map<String, Town> towns) {
      this.towns = towns;
    }

    @Builder
    public Road make(@Column(value = "name") String name,
                     @Column(value = "endpoint1") String endPoint1,
                     @Column(value = "endpoint2") String endPoint2) {
      return new Road(name, Arrays.asList(towns.get(endPoint1),
                                          towns.get(endPoint2)));
    }
  }

  public final Set<Town> towns;

  public final Set<Road> roads;

  public Level(Collection<Road> roads) {
    this.roads = Collections.unmodifiableSet(new HashSet<>(roads));
    Set<Town> alls = new HashSet<>();
    for (Road r : roads)
      for (Town t : r.endPoints)
        alls.add(t);
    towns = Collections.unmodifiableSet(alls);
  }

  public static Level load(String separator, File towns, File roads) throws IOException {

    try (Scanner sct = new Scanner(towns); Scanner scr = new Scanner(roads)) {
      Map<String, Town> t = map(builder(Town.class), "name", separator, sct);
      return new Level(list(CSVParser.<Road> builder(new RoadBuilder(t)),
                          separator, scr));
    }
  }
}
