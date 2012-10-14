package penran.zombies.ui.objects;

import java.util.List;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeType;

/**
 * Draws a green boundary to give approximate shape of the played country.
 * 
 * @author Guillaume Alvarez
 */
public final class Boundary {

  private final Polyline line;

  public Boundary(List<Point2D> background) {
    this.line = new Polyline();
    final ObservableList<Double> points = line.getPoints();
    for (Point2D p : background) {
      points.add(p.getY());
      points.add(p.getX());
    }
    line.setStrokeType(StrokeType.OUTSIDE);
    line.setStroke(Color.web("green", 1f));
    line.setStrokeWidth(0.5f);
  }

  public Polyline getGraphicalNode() {
    return line;
  }

}
