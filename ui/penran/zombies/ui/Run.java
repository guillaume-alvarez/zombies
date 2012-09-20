package penran.zombies.ui;

import java.io.File;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Run extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    Land land = new Land(Level.load(',', new File("etc/towns.csv"), new File("etc/roads.csv")), 800, 350, 20, 20);
    stage.setScene(new Scene(land));

    land.beginGameLoop();

    stage.show();

  }
}
