package penran.zombies.editor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import penran.utils.Util;
import penran.zombies.ui.WorldMap;
import penran.zombies.ui.Level;
import penran.zombies.ui.Level.Road;

public class LevelEditor implements Initializable {

  @FXML
  private TextField item_edit_name;

  @FXML
  private Slider item_edit_size;

  @FXML
  private TextField item_edit_xcoord;

  @FXML
  private TextField item_edit_ycoord;

  @FXML
  private Menu item_menu_delete;

  @FXML
  private Menu item_menu_duplicate;

  @FXML
  private ComboBox<?> road_endpoint;

  @FXML
  private Button road_select_endpoint;

  @FXML
  private Button road_select_startpoint;

  @FXML
  private ComboBox<?> road_startpoint;

  @FXML
  private MenuItem scene_create;

  @FXML
  private MenuItem scene_load;

  @FXML
  private MenuItem scene_save;

  @FXML
  private MenuItem scene_save_as;

  @FXML
  private MenuItem scene_close;

  @FXML
  private GridPane sheet_town;

  @FXML
  private GridPane sheet_road;

  @FXML
  private Pane scene_actual;

  /** The application window. */
  private Stage stage;

  public LevelEditor(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
    scene_close.setOnAction(event -> onSceneClose(event));
    scene_create.setOnAction(event -> onSceneCreate());
    scene_load.setOnAction(event -> onSceneLoad());

    loadFile(new File("etc/level.test2"));
  }

  private void onSceneClose(ActionEvent event) {
    System.out.println(event);
    Util.die("TODO offers to save before quitting. Ah ah.");
  }

  private void onSceneCreate() {
    loadLevel(new Level(new ArrayList<Road>(), new ArrayList<Point2D>()));
  }

  private void onSceneLoad() {
    DirectoryChooser dc = new DirectoryChooser();
    dc.setTitle("Choose level directory...");
    dc.setInitialDirectory(new File("etc/"));
    File dir = dc.showDialog(stage);
    if (dir != null && dir.isDirectory())
      loadFile(dir);
  }

  private void loadFile(File file) {
    try {
      loadLevel(Level.load(file));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void loadLevel(Level level) {
    WorldMap land = new WorldMap(level, 800, 350, 20, 20);
    land.setVisible(true);
    scene_actual.getChildren().clear();
    scene_actual.getChildren().add(land);
  }
}
