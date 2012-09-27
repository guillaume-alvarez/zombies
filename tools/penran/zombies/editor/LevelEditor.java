package penran.zombies.editor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.sun.javafx.css.Size;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import penran.utils.Util;
import penran.zombies.ui.Land;
import penran.zombies.ui.Level;

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

	@Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		scene_close.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println(event);
				Util.die("TODO offers to save before quitting. Ah ah.");
			}
		});
		
		try {
			Land land = new Land(Level.load(new File("etc/level.test2")), 800, 350, 20, 20);
			land.setVisible(true);
			scene_actual.getChildren().add(land);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
