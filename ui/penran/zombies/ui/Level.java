package penran.zombies.ui;

import static penran.utils.CSVParser.list;

import java.io.File;
import java.io.IOException;
import java.util.*;

import penran.utils.*;
import penran.utils.CSVParser.Builder;
import penran.utils.CSVParser.Column;

/**
 * Temporary representation of a level to use for initial dev of the main view.
 *
 * @author gaetan
 */
public class Level {

  public static class ToList implements penran.utils.CSVParser.Converter {
    @Override
    public Object convert(String column, Class<?> type, boolean required, String value) throws Exception {
      if (value.isEmpty())
        return Collections.emptyList();
      return Arrays.asList(value.split("\\s+"));
    }
  }

  public static class Town {
    public final double latitude;

    public final double longitude;

    public final int size;

    public final String name;

    public final int infected;

    private Town(String name, double latitude, double longitude, int size, int infected) {
      this.name = name;
      this.latitude = latitude;
      this.longitude = longitude;
      this.size = size;
      this.infected = infected;
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
                            @Column(value = "size", required = false) Integer size,
                            @Column(value = "infected count", required = false) Integer infected) {
      return new Town(name,
                      latitude,
                      longitude,
                      size == null ? 0 : size,
                      infected == null ? 0 : infected);
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
    public Road make(@Column("name") String name,
                     @Column("endpoint1") String endPoint1,
                     @Column("endpoint2") String endPoint2) {
      return new Road(name,
                      Arrays.asList(towns.get(endPoint1),
                                    towns.get(endPoint2)));
    }
  }

  public final Set<Town> towns;

  public final Set<Road> roads;

  public Level(Collection<Road> roads) {
    this.roads = Collections.unmodifiableSet(new HashSet<>(roads));
    Set<Town> alls = new HashSet<>();
    for (Road r : roads) {
      for (Town t : r.endPoints) {
        alls.add(t);
      }
    }
    towns = Collections.unmodifiableSet(alls);
  }

  public static Level load(int separator, File towns, File roads) throws IOException {

    try (Scanner sct = new Scanner(towns); Scanner scr = new Scanner(roads)) {
      List<Town> t = list(CSVParser.<Town> builder(Town.class), separator, sct);
      Map<String, Town> m = Util.asMap(t, Town.class.getDeclaredField("name"));
      return new Level(list(CSVParser.<Road> builder(new RoadBuilder(m)),
                            separator,
                            scr));
    }
    catch (Exception e) {
      throw new IOException("Cannot load data: " + e, e);
    }
  }

  @Override
  public String toString() {
    String s = "";
    for (Road r : roads) {
      s += r.name + ": " + r.endPoints.get(0).name + " -> "
          + r.endPoints.get(1).name + "\n";
    }
    return s;
  }
}
