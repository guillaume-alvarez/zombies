package penran.zombies.core;

import static java.lang.Math.*;

/**
 * Link between two places.
 * <p>
 * At the moment the object manages the distance and is accessible from both its
 * destinations. However experience shows a distance matric would be quite more
 * effective...
 * </p>
 * 
 * @author Guillaume Alvarez
 */
public final class Link {

	public final Place p1;

	public final Place p2;

	public final double distance;

	public Link(Place p1, Place p2) {
		this.p1 = p1;
		p1.addLink(this);
		this.p2 = p2;
		p2.addLink(this);
		this.distance = distance(p1.coordinates, p2.coordinates);
	}

	private static double distance(Coordinates c1, Coordinates c2) {
		return sqrt(pow(c1.getLatitude() - c2.getLatitude(), 2) + pow(c1.getLongitude() - c2.getLongitude(), 2));
	}

}
