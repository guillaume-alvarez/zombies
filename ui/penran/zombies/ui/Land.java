package penran.zombies.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import penran.zombies.core.Coordinates;
import penran.zombies.core.Link;
import penran.zombies.core.Place;
import penran.zombies.core.World;
import penran.zombies.ui.Level.Road;
import penran.zombies.ui.Level.Town;

public final class Land extends Pane {

  private static final int FRAMES_PER_SECOND = 60;

  private static final int TICKS_PER_SECOND = 100;

  private final Rectangle background;

  private final Group items;

  private final HBox ui;

  private final SimpleDoubleProperty zoom = new SimpleDoubleProperty(1.0);

  private final SimpleStringProperty selected = new SimpleStringProperty("");

  private final int marginWidth;

  private final int marginHeight;

  private final List<Updateable> toUpdate = new ArrayList<>();

  private final Timeline loop;

  private final World world;

  public Land(Level level, int width, int height, int marginWidth, int marginHeight) {

    this.marginWidth = marginWidth;
    this.marginHeight = marginHeight;

    setPrefSize(width, height);

    // create the living objects
    Map<String, Place> places = new HashMap<>();
    for (final Town t : level.towns) {
      places.put(t.name, new Place(t.name, t.size, t.infected / (double) t.size, new Coordinates(t.latitude,
          t.longitude)));
    }
    List<Link> links = new ArrayList<>();
    for (final Road r : level.roads) {
      Place first = places.get(r.endPoints.get(0).name);
      Place second = places.get(r.endPoints.get(1).name);
      Link l = new Link(r.name, first, second);
      links.add(l);
    }
    world = new World(places, links);

    // create simple UI
    ui = new HBox(10);
    ui.setFillHeight(true);
    final Font font = Font.font("arial", 20);

    final Label global = new Label("Infection: 0%");
    toUpdate.add(new Updateable() {
      @Override
      public void update() {
        global.setText("Infection: " + Math.round(world.getContamination() * 100.0) + "%");
      }
    });
    global.setTextFill(Color.WHITE);
    global.setFont(font);
    ui.getChildren().add(global);

    final Text text = new Text(0, 0, "");
    text.textProperty().bind(selected);
    text.setFill(Color.WHITE);
    text.setFont(font);
    text.setY(font.getSize());
    ui.getChildren().add(text);

    // create the towns
    Group towns = new Group();
    Group halo = new Group();
    Group infection = new Group();
    for (final Place p : places.values()) {
      final Circle circle = initTown(p);
      towns.getChildren().add(circle);
      final Polygon polygon = initInfection(p);
      infection.getChildren().add(polygon);
      halo.getChildren().add(initHalo(p));

      // always update the town
      toUpdate.add(new TownUpdate(circle, p, polygon));
    }
    infection.setEffect(new BoxBlur(5, 5, 3));
    towns.setEffect(new BoxBlur(2, 2, 2));
    halo.setEffect(new BoxBlur(2, 2, 2));

    // create the roads
    Group roads = new Group();
    for (final Link l : links) {
      roads.getChildren().add(initRoad(l));
    }

    background = new Rectangle(0, 0, width, height);
    background.setFill(Color.BLACK);
    // city halos and roads should always be visible over infection
    items = new Group(infection, halo, roads, towns);
    items.setManaged(false);

    background.setHeight(height);
    background.setWidth(width);

    Bounds bounds = items.getBoundsInParent();
    zoom.set(Math.min((background.getWidth() - marginWidth) / bounds.getWidth(),
        (background.getHeight() - marginHeight - ui.getHeight()) / bounds.getHeight()));
    items.scaleXProperty().bind(zoom);
    items.scaleYProperty().bind(zoom);
    items.setTranslateX(-items.getBoundsInParent().getMinX() + marginWidth / 2d - items.getTranslateX());
    items.setTranslateY(-items.getBoundsInParent().getMinY() + ui.getHeight() + marginWidth / 2d
        - items.getTranslateY());

    setOnScroll(new EventHandler<ScrollEvent>() {
      @Override
      public void handle(ScrollEvent event) {
        double newZoom = zoom.get() + 0.01 * event.getDeltaY();
        zoom.set(Math.min(100.0, Math.max(0.1, newZoom)));
        // TODO should zoom to the cursor point, not to the center
        // TODO should also be possible to drag'n drop to move the view
        // thus we will have to include translation
      }
    });

    getChildren().add(new Group(background, items, ui));

    loop = buildGameLoop();
  }

  private Circle initTown(final Place p) {
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

  /** Draw the contamination polygon around the city. */
  private Polygon initInfection(Place p) {
    Polygon poly = new Polygon();
    return poly;
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

  private Polyline initRoad(final Link l) {
    final Polyline line = new Polyline(new double[] { l.p1.coordinates.longitude, l.p1.coordinates.latitude,
        l.p2.coordinates.longitude, l.p2.coordinates.latitude });
    line.setOnMouseClicked(new EventHandler<Event>() {
      @Override
      public void handle(Event paramT) {
        selected.setValue(String.format(
            "Road %s (%s km)" + "\n infection from %s: %dkm" + "\n infection from %s: %dkm", l.name,
            Math.round(l.distance), l.p1.name, Math.round(l.getProgressFrom(l.p1)), l.p2.name,
            Math.round(l.getProgressFrom(l.p2))));
      }
    });

    line.setStrokeType(StrokeType.OUTSIDE);
    line.setStroke(Color.web("white", 0.8f));
    line.setStrokeWidth(1f);
    return line;
  }

  /** Start all animations. */
  public void beginGameLoop() {
    loop.play();
    world.start(1000 / TICKS_PER_SECOND);
  }

  /** Builds and sets the game loop ready to be started. */
  private Timeline buildGameLoop() {
    final Duration oneFrameAmt = Duration.millis(1000 / (float) FRAMES_PER_SECOND);
    final KeyFrame oneFrame = new KeyFrame(oneFrameAmt, new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        for (Updateable u : toUpdate)
          u.update();
      }
    }); // oneFrame

    // creates the game world's game loop (Timeline)
    return TimelineBuilder.create().cycleCount(Animation.INDEFINITE).keyFrames(oneFrame).build();
  }

  /**
   * Recompute the town color from the infected rate.
   * 
   * @author Guillaume Alvarez
   */
  private static final class TownUpdate implements Updateable {

    private final Circle c;

    private final Place p;

    private final Polygon poly;

    private TownUpdate(Circle c, Place p, Polygon poly) {
      this.c = c;
      this.p = p;
      this.poly = poly;
    }

    @Override
    public void update() {
      final double zombies = p.getZombies();

      // update the city circle
      final int ift = (int) Math.round(255 * (1.0 - zombies));
      final Color color = Color.rgb(255, ift, ift);
      c.setFill(color);

      // update its contamination polygon
      if (zombies >= 1.0) {
        // draw the polygon
        final List<Double> points = new ArrayList<>();
        for (Link l : p) {
          double linkInfection = l.getProgressFrom(p) / l.distance;
          points.add(infectionPointLongitude(p, l, linkInfection));
          points.add(infectionPointLatitude(p, l, linkInfection));
        }
        if (points.size() == 4) {
          // when only connected to 2 other towns, use the orinal one
          points.add(p.coordinates.longitude);
          points.add(p.coordinates.latitude);
        }
        poly.getPoints().setAll(points);
        poly.setFill(color);
      } else if (zombies <= 0.0)
        // no longer any contamination
        poly.getPoints().clear();
      else
        // just update the color from the city one
        poly.setFill(color);
    }

    private static Double infectionPointLongitude(Place p, Link l, double infection) {
      return p.coordinates.longitude + infection * (l.otherPlace(p).coordinates.longitude - p.coordinates.longitude);
    }

    private static Double infectionPointLatitude(Place p, Link l, double infection) {
      return p.coordinates.latitude + infection * (l.otherPlace(p).coordinates.latitude - p.coordinates.latitude);
    }
  }
}
