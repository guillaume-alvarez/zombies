package penran.zombies.core;

import java.util.Random;

/**
 * Represents a character the player can give orders to. Situated in a place it
 * can do different missions depending on its type.
 * 
 * @author Guillaume Alvarez
 */
public final class Character {

  public enum Type {
    MEDIC, SOLDIER;
  }

  private final String name;

  private final Type type;

  private Place place;

  private Mission mission;

  public Character(String name, Type type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public void setPlace(Place place) {
    this.place = place;
  }

  public Place getPlace() {
    return place;
  }

  public Mission getMission() {
    return mission;
  }

  /**
   * Gives a mission to the character. Returns false if the character already
   * has one.
   */
  public boolean givesMission(Mission mission) {
    if (this.mission == null) {
      this.mission = mission;
      return true;
    } else
      return false;

  }

  /**
   * Stops the current mission for the character.
   */
  public void endMission() {
    if (mission != null) {
      mission.stops();
      mission = null;
    }
  }

  private static final Random rand = new Random();

  private static final String[] chars = new String[26];

  static {
    for (int i = 0; i < 26; i++)
      chars[i] = String.valueOf((char) ('a' + i));
  }

  public static Character generateRandom() {
    Type[] types = Type.values();
    // TODO replace with a list of names
    return new Character(chars[rand.nextInt(26)] + chars[rand.nextInt(26)] + chars[rand.nextInt(26)],
        types[rand.nextInt(types.length)]);
  }
}
