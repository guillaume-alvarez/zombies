package penran.zombies.ui.objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import penran.zombies.core.Place;
import penran.zombies.ui.Updateable;

/**
 * Draws a city, a circle and a halo.
 * 
 * @author Guillaume Alvarez
 */
public class City extends Group implements Updateable {

  private final Place place;

  private final Circle circle;

  public City(Place place, StringProperty selected) {
    this.place = place;
    getChildren().add(initHalo(place));
    // add circle after halo
    this.circle = initTown(place, selected);
    getChildren().add(circle);
  }

  private Circle initTown(final Place p, final StringProperty selected) {
    // draw the city
    final double radius = p.size / 2d;
    final int ift = (int) Math.round(255 * p.getZombies());
    final Circle c = new Circle(p.coordinates.longitude, p.coordinates.latitude, radius, Color.rgb(255, ift, ift));
    c.setOnMouseClicked(new EventHandler<Event>() {
      @Override
      public void handle(Event paramT) {
        selected.setValue(String.format("Town %s infected=%s%% size=%s", p.name, Math.round(p.getZombies() * 100.0),
            p.size));
      }
    });
    return c;
  }

  /** Draw the white circle around the city. */
  private Circle initHalo(Place p) {
    final double radius = p.size / 2d;
    Circle bound = new Circle(p.coordinates.longitude, p.coordinates.latitude, radius + 1);
    bound.setStrokeType(StrokeType.OUTSIDE);
    bound.setStroke(Color.web("white", 1));
    bound.setStrokeWidth(2f);
    return bound;
  }

  @Override
  public void update() {
    final double zombies = place.getZombies();

    // update the city circle
    final int ift = (int) Math.round(255 * (1.0 - zombies));
    final Color color = Color.rgb(255, ift, ift);
    circle.setFill(color);
  }

}
