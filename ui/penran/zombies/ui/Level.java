package penran.zombies.ui;

import static penran.utils.CSVParser.list;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javafx.geometry.Point2D;
import penran.utils.CSVParser;
import penran.utils.CSVParser.Builder;
import penran.utils.CSVParser.Column;
import penran.utils.Util;

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

  public static class PointBuilder {
    @Builder
    public Point2D make(@Column("latitude") double latitude,
                     @Column("longitude") double longitude,
                     @Column("size") int size) {
      return new Point2D(latitude, longitude);
    }
  }

  public final Set<Town> towns;

  public final Set<Road> roads;

	public final List<Point2D> background;

  public Level(Collection<Road> roads, List<Point2D> background) {
    this.background = background;
		this.roads = Collections.unmodifiableSet(new HashSet<>(roads));
    Set<Town> alls = new HashSet<>();
    for (Road r : roads) {
      for (Town t : r.endPoints) {
        alls.add(t);
      }
    }
    towns = Collections.unmodifiableSet(alls);
  }

  public static Level load(File folder) throws IOException {
  	File towns = new File(folder, "towns.csv");
  	File roads = new File(folder, "roads.csv");
  	File boundaries = new File(folder, "boundaries.csv");
  	
		try (Scanner sct = new Scanner(towns); Scanner scr = new Scanner(roads); Scanner bnd = new Scanner(boundaries)) {
			List<Town> t = list(CSVParser.<Town> builder(Town.class), ',', sct);
			Map<String, Town> m = Util.asMap(t, Town.class.getDeclaredField("name"));
			List<Road> r = list(CSVParser.<Road> builder(new RoadBuilder(m)), ',', scr);

			List<Point2D>  b = list(CSVParser.<Point2D> builder(new PointBuilder()), ',', bnd);

			return new Level(r, b);
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
