package ensemble.samples.graphics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Run extends Application {
  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    final ColorfulCirclesSample cc = new ColorfulCirclesSample();
    stage.setScene(new Scene(cc));
    stage.show();
    cc.play();
  }

}
