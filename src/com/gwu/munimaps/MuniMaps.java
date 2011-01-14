package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.gwu.munimaps.NextMuniContentProvider.PathTable;
import com.gwu.munimaps.NextMuniContentProvider.PointTable;
import com.gwu.munimaps.NextMuniContentProvider.RouteTable;

public class MuniMaps extends MapActivity {
    private static final int SELECT_ROUTES_DIALOG = 0;
    
	private MapView mMapView;
    private List<Overlay> mMapOverlays;
    private MuniPathsOverlay mPathsOverlay;
    private MuniMapsLocationManager mLocationManager;
	private ProgressDialog mRoutesProgressDialog;
	private List<Route> mRoutes;
	private ExecutorService mExecutor;
	private Handler mHandler;
	private Set<Route> mSelectedRoutes;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mExecutor = Executors.newCachedThreadPool();
        mHandler = new Handler();
        
        // Immediately start listening for location information.
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
        	mLocationManager = new MuniMapsLocationManager(locationManager);
        }
        
        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.setBuiltInZoomControls(true);
		
        mMapOverlays = mMapView.getOverlays();
        mPathsOverlay = new MuniPathsOverlay();
        mMapOverlays.add(mPathsOverlay);
        
        mSelectedRoutes = new HashSet<Route>();
        
        // Draw the route path overlays.
        updateRouteOverlays();
    }

	@Override
    protected boolean isRouteDisplayed() {
        return false;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.options, menu);
		MenuItem selectLinesItem = menu.findItem(R.id.select_routes);
		selectLinesItem.getSubMenu().add("foo");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SELECT_ROUTES_DIALOG:
			return createSelectRoutesDialog();
		}
		return super.onCreateDialog(id);
	}

	private Dialog createSelectRoutesDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_routes);
		final ArrayList<CharSequence> routeNames = new ArrayList<CharSequence>();
		for (Route route : mRoutes) {
			routeNames.add(route.mTitle);
		}
		if (!routeNames.isEmpty()) {
			builder.setMultiChoiceItems(routeNames.toArray(new CharSequence[0]), null,
					new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					if (isChecked) {
						mSelectedRoutes.add(mRoutes.get(which));
					} else {
						mSelectedRoutes.remove(mRoutes.get(which));
					}
				}
			});
		}
		builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				updateRouteOverlays();
			}
		});
		return builder.create();
	}

	public void selectRoutes(View view) {
		// Show a progress dialog and load the routes.
		if (mRoutes == null) {
			mRoutesProgressDialog = ProgressDialog.show(this, "", getString(R.string.loading));
			mExecutor.execute(new Runnable() {
				@Override
				public void run() {
					final List<Route> routes = new ArrayList<Route>();
					Cursor routeCursor = managedQuery(
							NextMuniContentProvider.RouteTable.CONTENT_URI,
							null, null, null, RouteTable.Column.TAG);
					if (routeCursor.moveToFirst()) {
						do {
							Route route = new Route();
							route.mTag = routeCursor.getString(
									routeCursor.getColumnIndex(NextMuniContentProvider.RouteTable.Column.TAG));
							route.mTitle = routeCursor.getString(
									routeCursor.getColumnIndex(NextMuniContentProvider.RouteTable.Column.TITLE));
							route.mShortTitle = routeCursor.getString(
									routeCursor.getColumnIndex(NextMuniContentProvider.RouteTable.Column.SHORT_TITLE));
							routes.add(route);
						} while (routeCursor.moveToNext());
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mRoutes = routes;
							mRoutesProgressDialog.dismiss();
							showDialog(SELECT_ROUTES_DIALOG);
						}
					});
				}
			});
		} else {
			// We've already loaded the routes, so there's no need to show a progress dialog, just show the routes.
			showDialog(SELECT_ROUTES_DIALOG);
		}
	}
	
	public void goToCurrentLocation(View view) {
		if (mLocationManager == null) {
			return;
		}
		Location currentLocation = mLocationManager.getBestLocation();
		if (currentLocation == null) {
			Toast.makeText(this, "Unable to detect location", Toast.LENGTH_SHORT).show();
			return;
		}
		Double lat = currentLocation.getLatitude() * 1e6;
		Double lon = currentLocation.getLongitude() * 1e6;
		GeoPoint geoPoint = new GeoPoint(lat.intValue(), lon.intValue());
		mMapView.getController().animateTo(geoPoint);
	}

    /**
     * Update the map overlays that display selected routes.
     */
    private void updateRouteOverlays() {
    	mPathsOverlay.clearRoutes();
    	// Start a thread to update the route details and draw them.
    	mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				final List<RouteDetail> routeDetails = new ArrayList<RouteDetail>();
		    	for (Route route : mSelectedRoutes) {
		    		RouteDetail routeDetail = new RouteDetail(route.mTag);
		    		routeDetails.add(routeDetail);
		    		
		    		// Get the paths for the route.
					Cursor pathCursor = managedQuery(
							Uri.withAppendedPath(NextMuniContentProvider.PathTable.CONTENT_URI, route.mTag),
							null, null, null, null);
					if (pathCursor.moveToFirst()) {
						do {
							long pathId = pathCursor.getLong(
									pathCursor.getColumnIndex(PathTable.Column._ID));
							Path path = new Path();
							routeDetail.addPath(path);
							
							// Get the points for the path.
							Cursor pointCursor = managedQuery(
									Uri.withAppendedPath(NextMuniContentProvider.PointTable.CONTENT_URI,
											Long.toString(pathId)),
									null, null, null, null);
							if (pointCursor.moveToFirst()) {
								do {
									double lat = pointCursor.getDouble(
											pointCursor.getColumnIndex(PointTable.Column.LAT));
									double lon = pointCursor.getDouble(
											pointCursor.getColumnIndex(PointTable.Column.LON));
									path.addPoint(new Point(lat, lon));
								} while (pointCursor.moveToNext());
							}
						} while (pathCursor.moveToNext());
					}
		    	}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						// Add new overlays for the updated paths.
						for (RouteDetail routeDetail : routeDetails) {
							mPathsOverlay.addRoute(routeDetail);
						}
						mMapView.invalidate();
					}
				});
			}
		});
	}
}