package penran.zombies.ui;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import penran.zombies.core.Coordinates;

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
    final WorldMap map = new WorldMap(Level.load(new File("etc/level.test")), 800, 350, 20, 20);
    layout.setCenter(map);

    // publish some technical information (mouse, fps...)
    final Label tech = new Label("unknown");
    map.setOnMouseMoved(event -> {
      Coordinates c = map.getCoordinates(event.getSceneX(), event.getSceneY());
      tech.setText("Longitude: " + (int) c.longitude + ", latitude: " + (int) c.latitude);
    });
    layout.setBottom(tech);

    // start game
    map.beginGameLoop();

  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0);
  }
}
