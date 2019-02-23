package Maping_EarthQuakes;

import java.util.ArrayList;
import java.util.Collections;
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
import de.fhpotsdam.unfolding.marker.AbstractMarker;
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
		
	private static final long serialVersionUID = 1L;

	// IF WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = true;
	
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
	//List of all LandquakeMarkers & OceanquakeMarke  as earthQuakeMarkers
	private List<EarthquakeMarker> earthQuakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	//A Map of features(Quakes) on land and features (Quakes) sea
	private HashMap<String,ArrayList<String>> countryWithQuakes;
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	private Object [] allQuakeMarkersArray;
	
	
	int count = 0;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(1200, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 400, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// TEST WITH A LOCAL FILE
		    //earthquakesURL = "2.5_week.atom";
			earthquakesURL = "quiz2.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// EARTHQUAKE URL FOR TESTING
		// earthquakesURL = "test1.atom";
		// earthquakesURL = "test2.atom";
		
		// FOR TAKING THIS QUIZ
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
	    
	    //Initialize the has map to map countries with quakes that occurred in them 
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
	    
	    System.out.println("Check point zero" + " "  + quakeMarkers.size());	
	    
	    //for debugging		
	    //printQuakes();

	    
	    //Initialize earthQuakeMarkers and convert it to an array of objects 
	    earthQuakeMarkers = new ArrayList<EarthquakeMarker>();
	    for (Marker m : quakeMarkers ) {
	    	earthQuakeMarkers.add((EarthquakeMarker) m);
	    }	    
	    //Sorts using magnitude for Largest magnitude to smallest
	    Collections.sort(earthQuakeMarkers);
	    allQuakeMarkersArray = earthQuakeMarkers.toArray();	
	 	System.out.println("Check point One" + " "  + quakeMarkers.size() + " "  + allQuakeMarkersArray.length);	
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	    //Should print highest mag upto 10	   
	    sortAndPrint(10);
	    
	}  // End setup
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	
	//handles mouse hover
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}

		}
	
	
	//The event handler for mouse clicks

	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			lastClicked.setSelected(false);
			lastClicked = null;
			unhideMarkers();
		
		}
		
		selectMarkerIfClicked(quakeMarkers);
		selectMarkerIfClicked(cityMarkers);

	}
	
	private void selectMarkerIfClicked(List<Marker> markers)
	{
		// Abort if there's already a marker selected
//		if (lastSelected != null) {
//			return;
//		}
		
		for (Marker m : markers) 
		{
			
			if (m.isInside(map,  mouseX, mouseY)) {
				CommonMarker marker = (CommonMarker)m;
				lastClicked = marker;
				marker.setSelected(true);
				affectedMarkers(m);
			}
			else {m.setHidden(true);}
		}

		if(allhiden(quakeMarkers)&& allhiden(cityMarkers)) {
			unhideMarkers();
			lastClicked = null;
		}
		return;
		}
	//Check to see if all markers are hidDen 
	public boolean allhiden(List<Marker> markers) {
		for (Marker m : markers) {
			if(!(m.isHidden())) {
				return false;
			}
		}
		return true;
	}
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	//Unhide affected cities or affecting eartquakes 
	public void affectedMarkers(Marker m) {
		
		if(m.getClass().equals(EarthquakeMarker.class)) {
			for(Marker marker : cityMarkers) {
				EarthquakeMarker em = (EarthquakeMarker) m;
				if ( m.getLocation().getDistance(marker.getLocation()) < em.threatCircle())
				{			
					marker.setHidden(false);
				}
			}
		}
		
		if(m.getClass().equals(CityMarker.class)) {
			for(Marker marker : quakeMarkers) {
				EarthquakeMarker em = (EarthquakeMarker) marker;
				if ( m.getLocation().getDistance(marker.getLocation()) < em.threatCircle())
				{			
					marker.setHidden(false);
				}
			}
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 350, 400);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Size ~ Magnitude", xbase+25, ybase+110);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
		
		fill(0, 0, 0);
		text("3 Largest Magnitudes", xbase+50, ybase+240);
		text(allQuakeMarkersArray[1].toString(), xbase+50, ybase+280);
		text(allQuakeMarkersArray[2].toString(), xbase+50, ybase+300);
		text(allQuakeMarkersArray[3].toString(), xbase+50, ybase+320);

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
	
	
	//creates a new array from the list of earthquake markers.Then sorts the array of earthquake
	//markers in reverse order of their magnitude (highest to lowest) and then print out the top
	//numToPrint earthquakes.
	
	private void sortAndPrint(int numToPrint) {					
		for (int k =0 ; k < numToPrint; k++) {
			System.out.println(allQuakeMarkersArray[k].toString());
		}
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
