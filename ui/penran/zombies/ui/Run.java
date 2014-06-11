package penran.zombies.ui;

import java.io.File;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Run extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    // load game data
    Land land = new Land(Level.load(new File("etc/level.test")), 800, 350, 20, 20);

    // launch display with mouse coordinates
    VBox layout = new VBox();
    layout.getChildren().setAll(land, techInfo(land));
    Scene scene = new Scene(layout);
    scene.getStylesheets().add(this.getClass().getResource("stylesheet.css").toExternalForm());
    stage.setScene(scene);

    // start game
    land.beginGameLoop();

    stage.show();
  }

  private Node techInfo(Land land) {
    final Label mouse = new Label("unknown");
    land.setOnMouseMoved(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        mouse.setText("Longitude: " + event.getSceneX() + ", latitude: " + event.getSceneY());
      }
    });
    return mouse;
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0);
  }
}
