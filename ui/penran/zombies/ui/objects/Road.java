package penran.zombies.ui.objects;

import static java.lang.Math.round;
import static java.lang.String.format;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeType;
import penran.zombies.core.Link;

/**
 * Draws a simple line for the road.
 * 
 * @author Guillaume Alvarez
 */
public final class Road {

  private final Polyline line;

  public Road(final Link link, final StringProperty selected) {
    this.line = new Polyline(new double[] { link.p1.coordinates.longitude, link.p1.coordinates.latitude,
        link.p2.coordinates.longitude, link.p2.coordinates.latitude });
    line.setOnMouseClicked(paramT -> selected.setValue(//
        format("Road %s (%s km)" + "\n infection from %s: %dkm" + "\n infection from %s: %dkm", link.name,
            round(link.distance), link.p1.name, round(link.getProgressFrom(link.p1)), link.p2.name,
            round(link.getProgressFrom(link.p2)))));

    line.setStrokeType(StrokeType.OUTSIDE);
    line.setStroke(Color.web("white", 0.8f));
    line.setStrokeWidth(1f);
  }

  public Polyline getGraphicalNode() {
    return line;
  }

}
