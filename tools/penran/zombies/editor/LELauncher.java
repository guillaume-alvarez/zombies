package penran.zombies.editor;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LELauncher extends Application {

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader(
				new URL("file:etc/scene-editor.fxml"));
		loader.setController(new LevelEditor());
		stage.setScene(new Scene((Parent) loader.load()));
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}

}
