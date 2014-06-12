package penran.zombies.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;
import penran.zombies.core.Coordinates;
import penran.zombies.core.Link;
import penran.zombies.core.Place;
import penran.zombies.core.World;
import penran.zombies.ui.Level.Road;
import penran.zombies.ui.Level.Town;
import penran.zombies.ui.objects.Boundary;
import penran.zombies.ui.objects.Characters;
import penran.zombies.ui.objects.City;
import penran.zombies.ui.objects.Infection;
import penran.zombies.ui.objects.TopBar;

public final class WorldMap extends BorderPane {

  private static final int FRAMES_PER_SECOND = 60;

  private static final int TICKS_PER_SECOND = 100;

  private final Group items;

  private final TopBar ui;

  private final List<Updateable> toUpdate = new ArrayList<>();

  private final Timeline loop;

  private final World world;

  public WorldMap(Level level, int width, int height, int marginWidth, int marginHeight) {
    setId("world-map");

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
    ui = update(new TopBar(world));

    // create the towns
    Group towns = new Group();
    Group infection = new Group();
    for (final Place p : places.values()) {
      towns.getChildren().add(update(new City(p, ui.getSelected())).getGraphicalNode());
      infection.getChildren().add(update(new Infection(p)).getGraphicalNode());
    }
    infection.setEffect(new BoxBlur(5, 5, 3));
    towns.setEffect(new BoxBlur(2, 2, 2));

    // create the roads
    Group roads = new Group();
    for (final Link l : links) {
      roads.getChildren().add(new penran.zombies.ui.objects.Road(l, ui.getSelected()).getGraphicalNode());
    }

    // city halos and roads should always be visible over infection
    Polyline boundary = new Boundary(level.background).getGraphicalNode();
    items = new Group(infection, roads, boundary, towns);
    items.setManaged(false);

    final Bounds bounds = items.getBoundsInParent();
    final double zoom = Math.min((width - marginWidth) / bounds.getWidth(), (height - marginHeight - ui
        .getGraphicalNode().getHeight()) / bounds.getHeight());

    items.setScaleX(zoom);
    items.setScaleY(zoom);
    items.setTranslateX(-items.getBoundsInParent().getMinX() + marginWidth / 2d - items.getTranslateX());
    items.setTranslateY(-items.getBoundsInParent().getMinY() + ui.getGraphicalNode().getHeight() + marginWidth / 2d
        - items.getTranslateY());

    new ZoomHandler(bounds);
    new DragHandler();

    VBox characters = update(new Characters(world)).getGraphicalNode();

    setCenter(items);
    setTop(ui.getGraphicalNode());
    setRight(characters);

    loop = buildGameLoop();
  }

  private <T extends Updateable> T update(T t) {
    toUpdate.add(t);
    return t;
  }

  /** Zoom on mouse wheel and translate image center to mouse cursor. */
  private final class ZoomHandler implements EventHandler<ScrollEvent> {

    private final Bounds bounds;

    private ZoomHandler(Bounds bounds) {
      this.bounds = bounds;
      WorldMap.this.setOnScroll(this);
    }

    @Override
    public void handle(ScrollEvent event) {
      // compute the new zoom
      // (constant increment for a mouse wheel step)
      final double zoom = 0.01 * event.getDeltaY();
      ScaleTransition scale = new ScaleTransition(new Duration(1000));
      scale.setByX(zoom);
      scale.setByY(zoom);

      // move the center to the new zoomed place:
      // - convert mouse coordinates to map coordinates
      Point2D target = items.sceneToLocal(event.getSceneX(), event.getSceneY());
      // - compute different between old group center and mouse pointer
      double moveX = (bounds.getMaxX() + bounds.getMinX()) / 2. - target.getX();
      double moveY = (bounds.getMaxY() + bounds.getMinY()) / 2. - target.getY();
      // - create the corresponding translation
      TranslateTransition translation = new TranslateTransition(new Duration(1000), items);
      translation.setByX(moveX);
      translation.setByY(moveY);

      new ParallelTransition(items, scale, translation).play();
    }
  }

  /** Slowly translate image center to mouse cursor. */
  private final class DragHandler {

    private double origSceneX;

    private double origSceneY;

    private double origTranslateX;

    private double origTranslateY;

    public DragHandler() {
      WorldMap.this.setOnMousePressed(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          mousePressed(event);
        }
      });
      WorldMap.this.setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
          mouseDragged(event);
        }
      });
    }

    private void mousePressed(MouseEvent event) {
      origSceneX = event.getSceneX();
      origSceneY = event.getSceneY();
      origTranslateX = items.getTranslateX();
      origTranslateY = items.getTranslateY();
    }

    private void mouseDragged(MouseEvent event) {
      double offsetX = event.getSceneX() - origSceneX;
      double offsetY = event.getSceneY() - origSceneY;
      items.setTranslateX(origTranslateX + offsetX);
      items.setTranslateY(origTranslateY + offsetY);
    }
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
    Timeline timeline = new Timeline(oneFrame);
    timeline.setCycleCount(Animation.INDEFINITE);
    return timeline;
  }

  public Coordinates getCoordinates(double x, double y) {
    Point2D p = items.sceneToLocal(x, y);
    return new Coordinates(p.getY(), p.getX());
  }

}
