package penran.zombies.core;

/**
 * Coordinate son the map, based on GPS measures.
 * 
 * @author Guillaume Alvarez
 */
public final class Coordinates {

  public final double latitude;

  public final double longitude;

  public Coordinates(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

}
