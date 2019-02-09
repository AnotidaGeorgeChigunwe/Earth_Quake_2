package Maping_EarthQuakes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Anotida George Chigunwe
 * Date: Jan 27, 2019
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	
	private static final long serialVersionUID = 1L;

	// IF WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	

	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	//A Map of features(Quakes) on land and features (Quakes) sea
	private HashMap<String,ArrayList<String>> countryWithQuakes;
	
	
	int count = 0;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// EARTHQUAKE URL FOR TESTING
		// earthquakesURL = "test1.atom";
		// earthquakesURL = "test2.atom";
		
		// WHEN TAKING THIS QUIZ: Uncomment the next line
		earthquakesURL = "quiz1.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		//     STEP 3: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    countryWithQuakes = new HashMap<String,ArrayList<String>>();
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		    
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }

	    //for debugging		
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		fill(255, 250, 240);
		rect(25, 50, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);
		fill(color(255,255, 255));
		triangle(42.5f,122.5f,50,107.5f,57.5f,122.5f);
		ellipse(50, 135, 15, 15);
		rect(42.5f,147.5f,15,15);
		fill(color(255, 0, 0));
		ellipse(50, 195, 15, 15);
		fill(color(255, 255, 0));
		ellipse(50, 215, 15, 15);
		fill(color(0, 0, 255));
		ellipse(50, 235, 15, 15);
		
		fill(0, 0, 0);
		text("City Marker", 75, 115);
		text("Land Quake", 75, 135);
		text("Ocean Quake", 75, 155);
		text("Size ~ Magnitude", 45, 175);
		text("Small", 75, 195);
		text("Intermediate", 75, 215);
		text("Deep", 75, 235);
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.
	private boolean isLand(PointFeature earthquake) {
		
		// For each, check if the earthquake PointFeature is in the 
		// country in m.  
		for (Marker m : countryMarkers) {
			if (isInCountry(earthquake, m)) {
			return true;
			}
		}	
		
		//not inside any country
				if(!countryWithQuakes.containsKey("InSea"))	{
					ArrayList<String> features = new ArrayList<String>();
					String earthQuake = earthquake.toString();
					features.add(earthQuake);
					countryWithQuakes.put("InSea", features);
				}
				else {
					ArrayList<String> features = countryWithQuakes.get("InSea");
					String earthQuake = earthquake.toString();
					features.add(earthQuake);
					countryWithQuakes.put("InSea", features);
				}

		return false;
		}
	
		//	prints countries with number of earthquakes as
		//	Country1: numQuakes1
		//	Country2: numQuakes2
		//	...
		//	OCEAN QUAKES: numOceanQuakes
	
	private void printQuakes() 
	{
		for (Marker cm : countryMarkers)  {
			if(countryWithQuakes.containsKey(cm.getProperty("name"))) {
			ArrayList<String> quakes = countryWithQuakes.get(cm.getProperty("name"));
			System.out.println(cm.getProperty("name") + " : has " + quakes.size() + " EartQuake as Listed Below");
			}
		}
	System.out.println("There are  " + countryWithQuakes.get("InSea").size() + " in the sea");
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake 
	// feature if it's in one of the countries.
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();
		
		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
					countryWithQuakes = matchingCountryAnsQuake (earthquake,country,countryWithQuakes );
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			countryWithQuakes = matchingCountryAnsQuake (earthquake,country,countryWithQuakes );
			return true;
		}
	
		return false;
	}
	
	// helper method to map countries with earthquakes that occured in that country 
	private HashMap<String,ArrayList<String>> matchingCountryAnsQuake (PointFeature earthquake, Marker country, HashMap<String,ArrayList<String>> countryWithQuakes ) 
	
	{		
		if(!countryWithQuakes.containsKey(country.getProperty("name")))	{
		ArrayList<String> features = new ArrayList<String>();
		String earthQuake = earthquake.toString();
		features.add(earthQuake);
		countryWithQuakes.put((String) country.getProperty("name"), features);
	}
	else {
		
		ArrayList<String> features = countryWithQuakes.get((String) country.getProperty("name"));
		String earthQuake = earthquake.toString();
		features.add(earthQuake);
		countryWithQuakes.put((String) country.getProperty("name"), features);
	}
		return countryWithQuakes;
	}
}
