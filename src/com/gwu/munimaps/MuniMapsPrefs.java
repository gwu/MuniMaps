package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Stores the cross-session persistent user prefs.
 */
public class MuniMapsPrefs {
	/**
	 * Name of the pref file that stores the selected routes.
	 * 
	 * This pref file stores nothing special in the values.  The presence
	 * of a key means that it is selected.
	 */
	private static final String ROUTE_PREFS = "SelectedRoutes";
	
	/** App context. */
	private Context mContext;
	
	/**
	 * Constructor
	 * @param context the application context.
	 */
	public MuniMapsPrefs(Context context) {
		mContext = context;
	}
	
	/**
	 * Get the set of route tags the user has selected to display.
	 * @return list of route tags.
	 */
	public List<String> getSelectedRouteTags() {
		SharedPreferences prefs = getPrefs(ROUTE_PREFS);
		return new ArrayList<String>(prefs.getAll().keySet());
	}
	
	/**
	 * Selects/deselects a route.
	 * @param routeTag the route tag.
	 * @param selected whether it should be selected (false to deselect).
	 */
	public void setRouteSelected(String routeTag, boolean selected) {
		SharedPreferences prefs = getPrefs(ROUTE_PREFS);
		Editor editor = prefs.edit();
		if (selected) {
			editor.putBoolean(routeTag, true);
		} else {
			editor.remove(routeTag);
		}
		editor.commit();
	}

	/**
	 * @param routeTag the route tag.
	 * @return whether the route is selected.
	 */
	public boolean isRouteSelected(String routeTag) {
		SharedPreferences prefs = getPrefs(ROUTE_PREFS);
		return prefs.contains(routeTag);
	}
	
	/**
	 * Return the shared preferences file by name in private mode.
	 * @param prefFile the name of the prefs file.
	 * @return the prefs file.
	 */
	private SharedPreferences getPrefs(String prefFile) {
		return mContext.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
	}
}
