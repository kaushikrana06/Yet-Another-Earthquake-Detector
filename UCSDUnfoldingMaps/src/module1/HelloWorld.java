package module1;

import processing.core.PApplet;
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.providers.AbstractMapProvider;
//import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
//import de.fhpotsdam.unfolding.providers.Microsoft;
import de.fhpotsdam.unfolding.providers.OpenStreetMap;
import de.fhpotsdam.unfolding.utils.MapUtils;


public class HelloWorld extends PApplet
{
	private static final long serialVersionUID = 1L;
	// offline
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	//  OFFLINE
	private static final boolean offline = false;
	UnfoldingMap map1;
	UnfoldingMap map2;
	
	public void setup() {
		size(5000, 1000, P2D);		
		
		this.background(220, 200, 210);
		
		// map provider
		AbstractMapProvider provider = new OpenStreetMap.OpenStreetMapProvider();
		int zoomLevel = 13;
		
		if (offline) 
		{
			provider = new MBTilesMapProvider(mbTilesString);
			zoomLevel = 3;
		}
		map2 = new UnfoldingMap(this, 50,50, 1800, 900, provider);
		// The next line zooms in and centers the map at 
	    map2.zoomAndPanTo(zoomLevel, new Location(22.82f, 86.22f));
		// This line makes the map interactive
		MapUtils.createDefaultEventDispatcher(this, map2);

	}

	public void draw() {
	
		map2.draw();
	}

	
}
