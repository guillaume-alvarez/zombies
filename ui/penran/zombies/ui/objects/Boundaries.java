package penran.zombies.ui.objects;

import java.util.List;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeType;

/**
 * Draws a green boundary to give approximate shape of the played country.
 * 
 * @author Guillaume Alvarez
 */
public final class Boundaries extends Polyline {

  public Boundaries(List<Point2D> background) {
    super();
    for (Point2D p : background) {
      getPoints().add(p.getY());
      getPoints().add(p.getX());
    }
    setStrokeType(StrokeType.OUTSIDE);
    setStroke(Color.web("green", 1f));
    setStrokeWidth(0.5f);
  }

}
