package Maping_EarthQuakes;

import de.fhpotsdam.unfolding.data.PointFeature;
import processing.core.PGraphics;

/** Implements a visual marker for land earthquakes on an earthquake map
 * 
 * @author Anotida George Chigunwe
 *
 */
public class LandQuakeMarker extends EarthquakeMarker {
	
	
	public LandQuakeMarker(PointFeature quake) {
		
		// calling EarthquakeMarker constructor
		super(quake);
		
		// setting field in earthquake marker
		isOnLand = true;
	}


	@Override
	public void drawEarthquake(PGraphics pg, float x, float y) {
		  			  
		  pg.stroke(255);
		  pg.ellipse(x, y, this.radius, this.radius);
				
	}

	// Get the country the earthquake is in
	public String getCountry() {
		return (String) getProperty("country");
	}
		
}