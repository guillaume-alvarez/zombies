package penran.zombies.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ScaleTransitionBuilder;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.animation.TranslateTransition;
import javafx.animation.TranslateTransitionBuilder;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import penran.zombies.core.Coordinates;
import penran.zombies.core.Link;
import penran.zombies.core.Place;
import penran.zombies.core.World;
import penran.zombies.ui.Level.Road;
import penran.zombies.ui.Level.Town;
import penran.zombies.ui.objects.Boundaries;
import penran.zombies.ui.objects.City;
import penran.zombies.ui.objects.Infection;

public final class Land extends AnchorPane {

  private static final int FRAMES_PER_SECOND = 60;

  private static final int TICKS_PER_SECOND = 100;

  private final Group items;

  private final HBox ui;

  private final SimpleStringProperty selected = new SimpleStringProperty("");

  private final List<Updateable> toUpdate = new ArrayList<>();

  private final Timeline loop;

  private final World world;

  public Land(Level level, int width, int height, int marginWidth, int marginHeight) {
    setStyle("-fx-background-color: black");

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
    ui = initUI();

    // create the towns
    Group towns = new Group();
    Group infection = new Group();
    for (final Place p : places.values()) {
      towns.getChildren().add(update(new City(p, selected)));
      infection.getChildren().add(update(new Infection(p)));
    }
    infection.setEffect(new BoxBlur(5, 5, 3));
    towns.setEffect(new BoxBlur(2, 2, 2));

    // create the roads
    Group roads = new Group();
    for (final Link l : links) {
      roads.getChildren().add(new penran.zombies.ui.objects.Road(l, selected));
    }

    // create the map boundaries
    Polyline boundary = new Boundaries(level.background);

    // city halos and roads should always be visible over infection
    items = new Group(infection, roads, boundary, towns);
    items.setManaged(false);

    final Bounds bounds = items.getBoundsInParent();
    final double zoom = Math.min((width - marginWidth) / bounds.getWidth(), (height - marginHeight - ui.getHeight())
        / bounds.getHeight());

    items.setScaleX(zoom);
    items.setScaleY(zoom);
    items.setTranslateX(-items.getBoundsInParent().getMinX() + marginWidth / 2d - items.getTranslateX());
    items.setTranslateY(-items.getBoundsInParent().getMinY() + ui.getHeight() + marginWidth / 2d
        - items.getTranslateY());

    setOnScroll(new ZoomHandler(bounds));
    // TODO should also be possible to drag'n drop to move the view

    getChildren().add(new Group(items, ui));

    loop = buildGameLoop();
  }

  private HBox initUI() {
    final HBox ui = new HBox(10);
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

    return ui;
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
    }

    @Override
    public void handle(ScrollEvent event) {
      // compute the new zoom
      // (constant increment for a mouse wheel step)
      final double zoom = 0.01 * event.getDeltaY();
      ScaleTransition scale = ScaleTransitionBuilder.create().duration(new Duration(1000)).byX(zoom).byY(zoom).build();

      // move the center to the new zoomed place
      // (compute different between old group center and mouse pointer)
      final double moveX = (bounds.getMaxX() + bounds.getMinX()) / 2 - event.getSceneX();
      final double moveY = (bounds.getMaxY() + bounds.getMinY()) / 2 - event.getSceneY();
      TranslateTransition translation = TranslateTransitionBuilder.create().duration(new Duration(1000)).node(items)
          .byX(moveX).byY(moveY).build();
      System.out.printf("Move from (%s,%s) by (%s,%s) because cursor in (%s,%s)\n", items.getTranslateX(),
          items.getTranslateY(), Math.round(moveX), Math.round(moveY), Math.round(event.getSceneX()),
          Math.round(event.getSceneY()));

      new ParallelTransition(items, scale, translation).play();
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
    return TimelineBuilder.create().cycleCount(Animation.INDEFINITE).keyFrames(oneFrame).build();
  }

}
