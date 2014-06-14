package penran.zombies.ui.objects;

import static java.lang.Math.round;
import static java.lang.String.format;
import javafx.beans.property.StringProperty;
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
public final class City implements Updateable {

  private final Place place;

  private final Circle circle;

  private final Group group;

  public City(Place place, StringProperty selected) {
    this.place = place;
    this.group = new Group();
    group.getChildren().add(initHalo(place));
    // add circle after halo
    this.circle = initTown(place, selected);
    group.getChildren().add(circle);
  }

  private Circle initTown(final Place p, final StringProperty selected) {
    // draw the city
    final double radius = p.size / 2d;
    final int ift = (int) round(255 * p.getZombies());
    final Circle c = new Circle(p.coordinates.longitude, p.coordinates.latitude, radius, Color.rgb(255, ift, ift));
    c.setOnMouseClicked(paramT -> selected.setValue(//
        format("Town %s infected=%s%% size=%s", p.name, round(p.getZombies() * 100.0), p.size)));
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

  public Group getGraphicalNode() {
    return group;
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
