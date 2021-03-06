package module4;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
//import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;


public class EarthquakeCityMap extends PApplet {
	
		private static final long serialVersionUID = 1L;

	private static final boolean offline = false;

	// Feed 
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	// The map
	private UnfoldingMap map;

	// The earthquakes
	private List<PointFeature> earthquakes;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	public void setup() {		
		
		size(1400, 900, OPENGL);
		if (offline) {
			String mbTilesString = "blankLight-1-3.mbtiles";
			map = new UnfoldingMap(this, 250, 50, 1100, 800,
					new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  
		} else {
			AbstractMapProvider provider = new OpenStreetMap.OpenStreetMapProvider();
			map = new UnfoldingMap(this, 250, 50, 1100, 800, provider);
			
		}
		map.zoomToLevel(3);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		String countryFile = "countries.geo.json";
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		String cityFile = "city-data.json";
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		List<Marker> cityMarkers = new ArrayList<>();
		for (Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
	    earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
		List<Marker> quakeMarkers = new ArrayList<>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  } else { // OceanQuakes
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
// for debugging
	    printQuakes();
	 		
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    
	}
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	private void addKey() {	
		fill(255, 250, 240);
		rect(10, 100, 230, 700);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(22);
		text("Earthquake Key", 45, 200);

		// City Marker
		fill(255, 0, 0, 204);
		triangle(55, 310, 68, 310, 62, 295);

		// LandQuake Marker
		fill(color(255, 255, 255));
		ellipse(63, 352, 15, 15);

		// OceanQuake Marker
		fill(color(255, 255, 255));
		rect(56, 395, 15, 15);

		// Low Magnitude
		fill(color(255, 255, 0));
		ellipse(58, 548, 15, 15);

		// Average Magnitude
		fill(color(0, 0, 255));
		ellipse(58, 602, 15, 15);

		// deep
		fill(color(255, 0, 0));
		ellipse(58, 652, 15, 15);

		fill(0, 0, 0);
		textSize(18);
		text("City Marker", 80, 300);
		text("Land Quake", 80, 350);
		text("Ocean Quake", 80, 400);
		textSize(22);
		text("Size ~ Magnitude", 45, 480);
		textSize(18);
		text("Shallow", 80, 550);
		text("Intermediate", 80, 600);
		text("Deep", 80, 650);
	}

	private boolean isLand(PointFeature earthquake) {
				for (Marker country: countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}

		// Not inside any country
		return false;
	}

	/**
	 * Print countries with number of earthquakes
	 */
	private void printQuakes() {
		
		int earthquakesCount = 0;
		System.out.println();

		for (Marker country: countryMarkers) {
			String name = (String) country.getProperty("name");
			for (PointFeature quake: earthquakes) {
				String quakeCountry = (String) quake.getProperty("country");
				if (name.equals(quakeCountry)) {
					earthquakesCount++;
				}
			}
			if (earthquakesCount > 0) {
				System.out.println(name + ": " + earthquakesCount);
			}
			earthquakesCount = 0;
		}

		int quakesInOcean = 0;
		for (PointFeature quake: earthquakes) {
			if (! isLand(quake)) {
				quakesInOcean++;
			}
		}
		System.out.println("Ocean quakes: " + quakesInOcean);
	}

	
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		
		Location checkLoc = earthquake.getLocation();

		if(country.getClass() == MultiMarker.class) {
			//  MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {	
				// inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));	
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}