package module3;

import java.util.ArrayList;
//import java.util.Comparator;
import java.util.List;

//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
//import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;


public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	//  OFFLINE
	private static final boolean offline = false;
	
	// light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	
	public void setup() {
		size(1050, 600, OPENGL);

		if (offline) {
		    map = new UnfoldingMap(this, 320, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			map = new UnfoldingMap(this, 320, 50, 700, 500, new OpenStreetMap.OpenStreetMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    List<Marker> markers = new ArrayList<Marker>();

	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    if (earthquakes.size() > 0) {
	    	PointFeature f = earthquakes.get(0);
	    	System.out.println(f.getProperties());
	    	Object magObj = f.getProperty("magnitude");
	    	float mag = Float.parseFloat(magObj.toString());
	    	// PointFeatures also have a getLocation method
			System.out.println(f.getLocation() + " | " + mag);
	    }
	    for (PointFeature feature: earthquakes) {
			Marker marker = createMarker(feature);
			markers.add(marker);
		}
	    
	    // Add the markers to the map so that they are displayed
	    map.addMarkers(markers);
	}
		
	private SimplePointMarker createMarker(PointFeature feature)
	{  
		float magnitude = getMagnitude(feature);
	    int yellow = color(255, 255, 0);
	    int radius = 15;
	    if (magnitude < THRESHOLD_LIGHT) {
			yellow = color(0, 0, 255);
			radius = 5;
		} else if (magnitude < THRESHOLD_MODERATE) {
			yellow = color(255, 0, 0);
			radius = 10;
		}

		SimplePointMarker marker1 = new SimplePointMarker(feature.getLocation(), feature.getProperties());
		marker1.setRadius(radius);
		marker1.setColor(yellow);
		return marker1;
	   
	}
	
	public void draw() {
	    background(10);
	    map.draw();
	    addKey();
	}

	private void addKey() 
	{	
		fill(color(255, 210, 231));
		float keyX = 10, keyY = 50;
		float keyWidth = 300, keyHeight = 500;
		rect(keyX, keyY, keyWidth, keyHeight);

		// Key title
		fill(13, 16, 122, 204);
		textSize(35);
		text("Legends :", 30, 150);

		// yellow markers
		fill(255, 255, 0, 204);
		ellipse(55, 220, 20, 20);
		fill(0, 0, 0, 204);
		textSize(19);
		text("5.0+ Magnitude", 80, 228);

		// red markers
		fill(255, 0, 0, 204);
		ellipse(55, 300, 15, 15);
		fill(0, 0, 0, 204);
		textSize(19);
		text("4.0+ Magnitude", 80, 308);

		// blue markers
		fill(0, 0, 255, 204);
		ellipse(55, 380, 10, 10);
		fill(0, 0, 0, 204);
		textSize(19);
		text("< 4.0 Magnitude", 80, 385);

		
	}
	private float getMagnitude(PointFeature feature) {
		Object magObj = feature.getProperty("magnitude");
		float magnitude;
		magnitude = Float.parseFloat(magObj.toString());
		return magnitude;
	}
}
