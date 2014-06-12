package penran.zombies.ui.objects;

import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import penran.zombies.core.Character;
import penran.zombies.core.World;
import penran.zombies.ui.Updateable;

public final class Characters implements Updateable {

  private final VBox box;

  private final World world;

  private final Map<String, Portrait> portraits = new HashMap<>();

  public Characters(World world) {
    this.world = world;

    box = new VBox(4);
    box.setFillWidth(true);
    box.setMinWidth(60);
    box.setPrefWidth(60);
    box.setMaxWidth(60);
    box.setOpacity(0.9);
    box.setAlignment(Pos.TOP_RIGHT);
  }

  public VBox getGraphicalNode() {
    return box;
  }

  @Override
  public void update() {
    Map<String, Character> characters = world.getCharacters();
    for (String name : characters.keySet())
      if (!portraits.containsKey(name)) {
        Portrait portrait = new Portrait(characters.get(name));
        box.getChildren().add(portrait.button);
        portraits.put(name, portrait);
      }
  }

  private static final class Portrait {

    @SuppressWarnings("unused")
    private final Character character;

    private final Button button;

    public Portrait(Character character) {
      this.character = character;

      button = new Button(character.getName() + "\n" + character.getType());
      button.setId("portrait-" + character.getType().name().toLowerCase());
      // permit all buttons to fill the bow width
      button.setMaxWidth(Double.MAX_VALUE);
    }

  }

}
