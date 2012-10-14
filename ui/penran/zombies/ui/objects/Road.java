package penran.zombies.ui.objects;

import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeType;
import penran.zombies.core.Link;

/**
 * Draws a simple line for the road.
 * 
 * @author Guillaume Alvarez
 */
public final class Road extends Polyline {

  public Road(final Link link, final StringProperty selected) {
    super(new double[] { link.p1.coordinates.longitude, link.p1.coordinates.latitude, link.p2.coordinates.longitude,
        link.p2.coordinates.latitude });
    setOnMouseClicked(new EventHandler<Event>() {
      @Override
      public void handle(Event paramT) {
        selected.setValue(String.format(
            "Road %s (%s km)" + "\n infection from %s: %dkm" + "\n infection from %s: %dkm", link.name,
            Math.round(link.distance), link.p1.name, Math.round(link.getProgressFrom(link.p1)), link.p2.name,
            Math.round(link.getProgressFrom(link.p2))));
      }
    });

    setStrokeType(StrokeType.OUTSIDE);
    setStroke(Color.web("white", 0.8f));
    setStrokeWidth(1f);
  }

}
