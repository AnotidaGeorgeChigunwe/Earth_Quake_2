package Maping_EarthQuakes;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for ocean earthquakes on an earthquake map
 * 
 * @author Anotida George Chigunwe
 *
 */
public class OceanQuakeMarker extends EarthquakeMarker {
	
	
	public OceanQuakeMarker(PointFeature quake) {
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = false;
	}
	

	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		
		  pg.stroke(255);
		  pg.rect(x-this.radius/2, y-this.radius/2, this.radius, this.radius);		
	}
	
}
