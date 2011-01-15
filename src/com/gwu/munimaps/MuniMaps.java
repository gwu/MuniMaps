package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class MuniMaps extends MapActivity {
    private static final int SELECT_ROUTES_DIALOG = 0;
    
    /** Cached in-memory route data. */
    private RouteData mRouteData;
    /** Muni Maps prefs. */
    private MuniMapsPrefs mPrefs;
    /** The Muni map manager. */
    private MuniMapViewManager mMuniMap;
    
	private ProgressDialog mRoutesProgressDialog;
	private ExecutorService mExecutor;
	private Handler mHandler;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mExecutor = Executors.newCachedThreadPool();
        mHandler = new Handler();
        
        mRouteData = new RouteData(this);
        mPrefs = new MuniMapsPrefs(getApplicationContext());
        
        mMuniMap = new MuniMapViewManager((MapView) findViewById(R.id.mapview), mRouteData, mPrefs);
        mMuniMap.init(getApplicationContext());
        
        cacheSelectedRouteInfo();
    }

	@Override
    protected boolean isRouteDisplayed() {
        return false;
    }

	@Override
	protected void onPause() {
		super.onPause();
		mMuniMap.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mMuniMap.onResume();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SELECT_ROUTES_DIALOG:
			return createSelectRoutesDialog();
		}
		return super.onCreateDialog(id);
	}

	/**
	 * Create the select routes dialog box.
	 * @return the dialog.
	 */
	private Dialog createSelectRoutesDialog() {
		final List<RouteListing> routeListings = mRouteData.getRouteListings();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_routes);
		
		final ArrayList<CharSequence> routeNames = new ArrayList<CharSequence>();
		final boolean[] selected = new boolean[routeListings.size()];
		
		for (int i = 0; i < routeListings.size(); i++) {
			RouteListing route = routeListings.get(i);
			routeNames.add(route.getTitle());
			selected[i] = mPrefs.isRouteSelected(route.getTag());
		}
		if (!routeNames.isEmpty()) {
			builder.setMultiChoiceItems(
					routeNames.toArray(new CharSequence[0]),
					selected,
					new DialogInterface.OnMultiChoiceClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					mPrefs.setRouteSelected(routeListings.get(which).getTag(), isChecked);
				}
			});
		}
		builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				cacheSelectedRouteInfo();
			}
		});
		return builder.create();
	}

	/**
	 * Called when the Select Routes button is clicked.
	 * @param view
	 */
	public void selectRoutes(View view) {
		if (mRouteData.isRouteListingsCached()) {
			// We've already loaded the routes, no need to show a progress dialog.
			showDialog(SELECT_ROUTES_DIALOG);
		} else {
			// Show a progress dialog and load the routes.
			mRoutesProgressDialog = ProgressDialog.show(this, "", getString(R.string.loading));
			mExecutor.execute(new Runnable() {
				@Override
				public void run() {
					mRouteData.fetchAndCacheRouteListings();
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mRoutesProgressDialog.dismiss();
							showDialog(SELECT_ROUTES_DIALOG);
						}
					});
				}
			});
		}
	}
	
	/**
	 * Called when the Current Location button is clicked.
	 * @param view
	 */
	public void goToCurrentLocation(View view) {
		if (!mMuniMap.moveToCurrentLocation()) {
			Toast.makeText(this, "Unable to detect location", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Fetch and cache the selected route info.
	 */
	private void cacheSelectedRouteInfo() {
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				for (String routeTag : mPrefs.getSelectedRouteTags()) {
					mRouteData.fetchAndCacheRouteInfo(routeTag);
				}
			}
		});
	}
}