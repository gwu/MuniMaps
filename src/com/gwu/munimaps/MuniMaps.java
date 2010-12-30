package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MuniMaps extends MapActivity {
    private static final int SELECT_ROUTES_DIALOG = 0;
    
	private MapView mMapView;
    private List<Overlay> mMapOverlays;
    private Drawable mDrawable;
    private MuniItemizedOverlay mItemizedOverlay;
    private MuniMapsLocationManager mLocationManager;
	private ProgressDialog mRoutesProgressDialog;
	private List<Route> mRoutes;
	private ExecutorService mExecutor;
	private Handler mHandler;
    
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
        mDrawable = this.getResources().getDrawable(R.drawable.androidmarker);
        mItemizedOverlay = new MuniItemizedOverlay(mDrawable);
        
        GeoPoint point = new GeoPoint(19240000,-99120000);
        OverlayItem overlayItem = new OverlayItem(point, "", "");
        mItemizedOverlay.addOverlay(overlayItem);
        
        GeoPoint point2 = new GeoPoint(35410000, 139460000);
        OverlayItem overlayItem2 = new OverlayItem(point2, "", "");
        mItemizedOverlay.addOverlay(overlayItem2);
        
        mMapOverlays.add(mItemizedOverlay);
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
					Toast.makeText(getApplicationContext(), "You chose " + routeNames.get(which), Toast.LENGTH_SHORT).show();
				}
			});
		}
		builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
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
							null, null, null, null);
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
}