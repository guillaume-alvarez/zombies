package penran.zombies.ui;

import java.util.List;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import penran.zombies.ui.Level.Road;
import penran.zombies.ui.Level.Town;

public class Land extends Pane {

  private final Rectangle background;

  private final Group items;

  private final int marginWidth;

  private final int marginHeight;

  private final Font font;

  public Land(Level level, int width, int height, int marginWidth, int marginHeight) {

    this.marginWidth = marginWidth;
    this.marginHeight = marginHeight;

    setPrefSize(width, height);

    font = Font.font("arial", 20);
    final Text text = new Text(0, 0, "Item: ");
    text.setFill(Color.WHITE);
    text.setFont(font);
    text.setY(font.getSize());

    // create the towns
    Group towns = new Group();
    Group halo = new Group();
    for (final Town t : level.towns) {
      if (t.control)
        continue;
      double radius = t.size / 2d;

      int ift = (int) Math.round(255 * (t.size - t.infected) / (double) t.size);
      Circle c = new Circle(t.longitude, t.latitude, radius, Color.rgb(255, ift, ift));
      c.setOnMouseClicked(new EventHandler<Event>() {
        @Override
        public void handle(Event paramT) {
          text.setText("Item: " + t.name + " infected=" + t.infected + "/" + t.size);
        }
      });

      towns.getChildren().add(c);

      Circle bound = new Circle(t.longitude, t.latitude, radius + 1);
      bound.setStrokeType(StrokeType.OUTSIDE);
      bound.setStroke(Color.web("white", 1));
      bound.setStrokeWidth(2f);
      halo.getChildren().add(bound);
    }
    towns.setEffect(new BoxBlur(2, 2, 2));
    halo.setEffect(new BoxBlur(2, 2, 2));

    // create the roads
    Group roads = new Group();
    for (final Road r : level.roads) {
      List<Town> ep = r.endPoints;
      List<Town> ct = r.control;

      double[] points = new double[ep.size() * 2 + ct.size() * 2];
      points[0] = ep.get(0).longitude;
      points[1] = ep.get(0).latitude;
      for (int i = 0; i < ct.size(); i++) {
        points[i * 2 + 2] = ct.get(i).longitude;
        points[i * 2 + 3] = ct.get(i).latitude;
      }
      points[ct.size() * 2 + 2] = ep.get(1).longitude;
      points[ct.size() * 2 + 3] = ep.get(1).latitude;

      Polyline l = new Polyline(points);
      l.setOnMouseClicked(new EventHandler<Event>() {
        @Override
        public void handle(Event paramT) {
          text.setText("Item: " + r.name);
        }
      });

      l.setStrokeType(StrokeType.OUTSIDE);
      l.setStroke(Color.web("white", 0.8f));
      l.setStrokeWidth(1f);
      roads.getChildren().add(l);
    }

    background = new Rectangle(0, 0, width, height);
    background.setFill(Color.BLACK);
    items = new Group(halo, roads, towns);
    items.setManaged(false);

    background.setHeight(height);
    background.setWidth(width);

    Bounds bounds = items.getBoundsInParent();
    double scale = Math.min((width - marginWidth) / bounds.getWidth(), (height - marginHeight - font.getSize())
        / bounds.getHeight());
    items.setScaleX(scale);
    items.setScaleY(scale);
    items.setTranslateX(-items.getBoundsInParent().getMinX() + marginWidth / 2d - items.getTranslateX());
    items.setTranslateY(-items.getBoundsInParent().getMinY() + font.getSize() + marginWidth / 2d
        - items.getTranslateY());

    getChildren().add(new Group(background, items, text));
  }

}
