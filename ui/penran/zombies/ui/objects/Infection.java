package penran.zombies.ui.objects;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import penran.zombies.core.Link;
import penran.zombies.core.Place;
import penran.zombies.ui.Updateable;

/**
 * Draws a blurred polygon around a contaminated city.
 * 
 * @author Guillaume Alvarez
 */
public class Infection extends Polygon implements Updateable {

  private final Place place;

  public Infection(Place place) {
    this.place = place;
  }

  @Override
  public void update() {
    final double zombies = place.getZombies();
    final int ift = (int) Math.round(255 * (1.0 - zombies));
    final Color color = Color.rgb(255, ift, ift);

    // update its contamination polygon
    if (zombies >= 1.0) {
      // draw the polygon
      final List<Double> points = new ArrayList<>();
      for (Link l : place) {
        double linkInfection = l.getProgressFrom(place) / l.distance;
        points.add(infectionPointLongitude(place, l, linkInfection));
        points.add(infectionPointLatitude(place, l, linkInfection));
      }
      if (points.size() == 4) {
        // when only connected to 2 other towns, use the orinal one
        points.add(place.coordinates.longitude);
        points.add(place.coordinates.latitude);
      }
      getPoints().setAll(points);
      setFill(color);
    } else if (zombies <= 0.0)
      // no longer any contamination
      getPoints().clear();
    else
      // just update the color from the city one
      setFill(color);
  }

  private static Double infectionPointLongitude(Place p, Link l, double infection) {
    return p.coordinates.longitude + infection * (l.otherPlace(p).coordinates.longitude - p.coordinates.longitude);
  }

  private static Double infectionPointLatitude(Place p, Link l, double infection) {
    return p.coordinates.latitude + infection * (l.otherPlace(p).coordinates.latitude - p.coordinates.latitude);
  }
}
