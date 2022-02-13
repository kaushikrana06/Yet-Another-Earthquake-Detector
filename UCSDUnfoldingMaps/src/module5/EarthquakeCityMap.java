package module5;

import java.util.*;

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
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
//import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;


public class EarthquakeCityMap extends PApplet {
	
	private static final long serialVersionUID = 1L;

	private static final boolean offline = false;
	
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed 
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
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
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {		
		size(1400, 900, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";
		}
		else {
			map = new UnfoldingMap(this, 250, 50, 1100, 800, new OpenStreetMap.OpenStreetMapProvider());
		}
		map.zoomToLevel(3);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		// city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		}
	    
		// earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
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
	
	@Override
	public void mouseMoved()
	{
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
	}
	 
	private void selectMarkerIfHover(List<Marker> markers)
	{
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

	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			if (lastSelected != null) {
				lastSelected.setSelected(false);
				lastSelected = null;
			}
			unhideMarkers();
			lastClicked = null;
		} else {
			selectMarkerIfHover(cityMarkers);
			selectMarkerIfHover(quakeMarkers);
			if (lastSelected != null) {
				lastClicked = lastSelected;
				hideMarkers(lastClicked);
			}
		}
	}
	
	
	
	// loop over and show all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
	}
	private void hideMarkers(Marker refMarker) {
		for (Marker marker: quakeMarkers) {
			if (! marker.equals(refMarker)) {
				if (refMarker instanceof CityMarker) {
					if (! isThreat((EarthquakeMarker) marker,
							(CityMarker) refMarker)) {
						marker.setHidden(true);
					} else {
						marker.setHidden(false);
					}
				} else {
					marker.setHidden(true);
				}
			}
		}
		for (Marker marker: cityMarkers) {
			if (! marker.equals(refMarker)) {
				if (refMarker instanceof EarthquakeMarker) {
					if (! isThreatened((CityMarker)marker,
							(EarthquakeMarker) refMarker)) {
						marker.setHidden(true);
					} else {
						marker.setHidden(false);
					}
				} else {
					marker.setHidden(true);
				}
			}
		}
	}
	private boolean isThreatened(CityMarker marker, EarthquakeMarker threat) {
		double distance = threat.threatCircle();
		return marker.getDistanceTo(threat.getLocation()) < distance;
	}
	private boolean isThreat(EarthquakeMarker threat, CityMarker marker) {
		double distance = threat.threatCircle();
		return threat.getDistanceTo(marker.getLocation()) < distance;
	}

	
	private void addKey() {	
		
		fill(255, 250, 240);
		rect(10, 100, 230, 700);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(22);
		text("Earthquake Key", 45, 200);

		// City
		fill(255, 0, 0, 204);
		triangle(55, 310, 68, 310, 62, 295);

		// Land
		fill(color(255, 255, 255));
		ellipse(63, 352, 15, 15);

		// Ocean
		fill(color(255, 255, 255));
		rect(56, 395, 15, 15);

		// shallow
		fill(color(255, 255, 0));
		ellipse(58, 548, 15, 15);

		// intermediate
		fill(color(0, 0, 255));
		ellipse(58, 602, 15, 15);

		// deep
		fill(color(255, 0, 0));
		ellipse(58, 652, 15, 15);
		// past hour
		strokeWeight(2);
		line(52, 695,65 ,715);
		line(52, 715, 65, 695);


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
		text("Past hour", 79, 702);
					
	}
	private boolean isLand(PointFeature earthquake) {

		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		return false;
	}
	
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	private boolean isInCountry(PointFeature earthquake, Marker country) {

		Location checkLoc = earthquake.getLocation();

		if(country.getClass() == MultiMarker.class) {
		
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					return true;
				}
			}
		}
			
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}
