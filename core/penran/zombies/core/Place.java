package penran.zombies.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A place on the map.
 * 
 * @author Guillaume Alvarez
 */
public final class Place implements GameObject {

	public final String name;

	public final Coordinates coordinates;

	// TODO optimize by using a common structure for all links
	private final List<Link> links = new ArrayList<Link>();

	/** Percentage of zombies here. */
	private double zombies = 0;

	public Place(String name, Coordinates coordinates) {
		this.name = name;
		this.coordinates = coordinates;
	}

	/**
	 * Register a link in this place.
	 */
	/* package */void addLink(Link link) {
		links.add(link);
	}

	public boolean hasZombies() {
		return zombies > 0;
	}

	public double getZombies() {
		return zombies;
	}

	/**
	 * Add a percentage to current zombie presence. The percentage can be
	 * negative to avoid
	 */
	void addZombies(double percent) {
		if (percent > 0)
			zombies = Math.max(100.0, zombies + percent);
		else if (percent < 0)
			zombies = Math.min(0.0, zombies + percent);

	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub

	}

}
