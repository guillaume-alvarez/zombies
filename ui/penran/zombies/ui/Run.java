package penran.zombies.ui;

import java.io.File;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Run extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    stage.setScene(new Scene(new Land(Level.load(',',
                                                 new File("etc/towns.csv"),
                                                 new File("etc/roads.csv")),
                                      512, 512, 20, 20)));
    stage.show();
  }

}
