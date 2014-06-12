package penran.zombies.ui;

import java.io.File;

import penran.zombies.core.Coordinates;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Run extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    // create display scene
    BorderPane layout = new BorderPane();
    Scene scene = new Scene(layout);
    scene.getStylesheets().add(this.getClass().getResource("stylesheet.css").toExternalForm());
    stage.setScene(scene);
    stage.show();

    // load game data and populate display
    final Land land = new Land(Level.load(new File("etc/level.test")), 800, 350, 20, 20);
    layout.setCenter(land);

    // publish some technical information (mouse, fps...)
    final Label tech = new Label("unknown");
    // TODO please lambdas!!!
    land.setOnMouseMoved(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        Coordinates c = land.getCoordinates(event.getSceneX(), event.getSceneY());
        tech.setText("Longitude: " + (int) c.longitude + ", latitude: " + (int) c.latitude);
      }
    });
    layout.setBottom(tech);

    // start game
    land.beginGameLoop();

  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0);
  }
}
