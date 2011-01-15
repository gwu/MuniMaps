package com.gwu.munimaps;

import java.util.List;

import android.content.Context;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

/**
 * Manages the MapView that is exposed to the user, including things like the route and gps overlays.
 */
public class MuniMapViewManager {
	/** The map view. */
	private MapView mMapView;
	
	/** The overlay that contains the route lines. */
	private MuniRoutesOverlay mRouteOverlays;

	/** The overlay that contains the gps location of the phone. */
	private MyLocationOverlay mLocationOverlay;
	
	/** Route data. */
	private RouteData mRouteData;
	
	/** Prefs. */
	private MuniMapsPrefs mPrefs;

	/**
	 * Construct a manager to manage the given map view.
	 * @param mapView the map view to manage.
	 */
	public MuniMapViewManager(MapView mapView, RouteData routeData, MuniMapsPrefs prefs) {
		mMapView = mapView;
		mRouteData = routeData;
		mPrefs = prefs;
	}

	/**
	 * Initialize the map so it is ready to be displayed to the user.
	 */
	public void init(Context context) {
        mMapView.setBuiltInZoomControls(true);
        
        List<Overlay> overlays = mMapView.getOverlays();
        
        // Create the route overlay.
        mRouteOverlays = new MuniRoutesOverlay(mRouteData, mPrefs);
        overlays.add(mRouteOverlays);
        
        // Create the location overlay.
        mLocationOverlay = new MyLocationOverlay(context, mMapView);
        overlays.add(mLocationOverlay);
	}

	/**
	 * Call when the activity is paused.
	 */
	public void onPause() {
		// Stop updating location data.
		mLocationOverlay.disableMyLocation();
	}

	/**
	 * Call when the activity is resumed.
	 */
	public void onResume() {
		// Start updating location data.
		mLocationOverlay.enableMyLocation();
	}
	
	/**
	 * Move the map view to center around the current gps location.
	 * @return whether the current location was found.
	 */
	public boolean moveToCurrentLocation() {
		GeoPoint currentLocation = mLocationOverlay.getMyLocation();
		if (currentLocation == null) {
			return false;
		}
		mMapView.getController().animateTo(currentLocation);
		return true;
	}
	
	/**
	 * Invalidates the map (reads all prefs again and redraws everything).
	 */
	public void invalidate() {
		// Redraw the map and it's overlays.
		mMapView.invalidate();
	}

	/**
	 * @return the managed map view.
	 */
	public MapView getMapView() {
		return mMapView;
	}
}
