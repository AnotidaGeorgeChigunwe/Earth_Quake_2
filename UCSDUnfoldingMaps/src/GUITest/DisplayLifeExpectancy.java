package GUITest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import processing.core.PApplet;

public class DisplayLifeExpectancy extends PApplet {
	

	private static final long serialVersionUID = 1L;
	UnfoldingMap map;
	Map<String,Float> lifeExpByCountry;
	List<Feature> countries;
	List<Marker> countryMarkers;
	
	public void setup() {
		size(800, 600, OPENGL);
		map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
		MapUtils.createDefaultEventDispatcher(this, map);
		//lifeExpByCountry= loadLifeExpectancyFromCSV (String fileName);
		countries = GeoJSONReader.loadData(this,  "data/countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		shadeCountries();
		map.addMarkers(countryMarkers);
	}
	
	public void draw () {
	   // background(0,0,0);
	    map.draw();
	}

	private Map<String,Float> loadLifeExpectancyFromCSV (String fileName) {
		Map<String,Float> lifeExMap = new HashMap<String, Float>();
		String[] rows = loadStrings(fileName);
		for (String s : rows) {
			String [] columns = s.split(",");
			if(columns!=null) {
				float value = Float.parseFloat(columns[5]);
				lifeExMap.put(columns[4], value);
			}
		}
				
		return lifeExMap;
	}
	
	private void shadeCountries() {
		for (Marker marker : countryMarkers) {
			String countryId = marker.getId();
			
			if(lifeExpByCountry.containsKey(countryId)) {
				float lifeExp = lifeExpByCountry.get(countryId);
				int colorLevel = (int) map(lifeExp, 40,90,10,255);
				marker.setColor(color(255-colorLevel,100,255-colorLevel));
			}
			else {
				marker.setColor(color(150,150,150));
			}
		}
		
	}
}
