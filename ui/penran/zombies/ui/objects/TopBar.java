package penran.zombies.ui.objects;

import static java.lang.Math.round;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import penran.zombies.core.World;
import penran.zombies.ui.Updateable;

public final class TopBar implements Updateable {

  private static final Font FONT = Font.font("arial", 20);

  private final HBox box;

  private final Text text;

  private final Label global;

  private final World world;

  public TopBar(World world) {
    this.world = world;

    box = new HBox(10);
    box.setFillHeight(true);

    global = new Label("Infection: 0%");
    global.setTextFill(Color.WHITE);
    global.setFont(FONT);
    box.getChildren().add(global);

    text = new Text(0, 0, "");
    text.setFill(Color.WHITE);
    text.setFont(FONT);
    text.setY(FONT.getSize());
    box.getChildren().add(text);
  }

  public StringProperty getSelected() {
    return text.textProperty();
  }

  public HBox getGraphicalNode() {
    return box;
  }

  @Override
  public void update() {
    global.setText("Infection: " + round(world.getContamination() * 100.0) + "%");
  }
}
