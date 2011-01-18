package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwu.munimaps.provider.PathTable;
import com.gwu.munimaps.provider.PointTable;
import com.gwu.munimaps.provider.RouteTable;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * The in-memory data about routes.
 */
public class RouteData {
	private Activity mActivity;
	private List<RouteListing> mRouteListings;
	private Map<String, RouteInfo> mRouteInfoMap;
	
	public RouteData(Activity activity) {
		mActivity = activity;
		mRouteListings = null;
		mRouteInfoMap = new HashMap<String, RouteInfo>();
	}
	
	/**
	 * @return whether the route listings have already been fetched and cached.
	 */
	public boolean isRouteListingsCached() {
		return mRouteListings != null;
	}
	
	public void fetchAndCacheRouteListings() {
		if (isRouteListingsCached()) {
			// Already cached.  Do nothing.
			return;
		}
		mRouteListings = new ArrayList<RouteListing>();
		Cursor cursor = query(
				RouteTable.CONTENT_URI,
				null,  // Select all columns.
				null, null,  // Select all rows.
				RouteTable.Column.TAG);
		if (cursor.moveToFirst()) {
			do {
				RouteListing routeListing = new RouteListing();
				routeListing.setTag(cursor.getString(cursor.getColumnIndex(
						RouteTable.Column.TAG)));
				routeListing.setTitle(cursor.getString(cursor.getColumnIndex(
						RouteTable.Column.TITLE)));
				routeListing.setShortTitle(cursor.getString(cursor.getColumnIndex(
						RouteTable.Column.SHORT_TITLE)));
				mRouteListings.add(routeListing);
			} while (cursor.moveToNext());
		}
	}
	
	/**
	 * Get the list of routes available.
	 * @return
	 */
	public List<RouteListing> getRouteListings() {
		if (mRouteListings == null) {
			fetchAndCacheRouteListings();
		}
		return mRouteListings;
	}
	
	/**
	 * @param routeTag the route tag.
	 * @return whether the route info has been fetched and cached.
	 */
	public boolean isRouteInfoCached(String routeTag) {
		return mRouteInfoMap.containsKey(routeTag);
	}
	
	/**
	 * Fetch and cache the route info given the tag, if necessary.
	 * @param routeTag the route tag.
	 */
	public void fetchAndCacheRouteInfo(String routeTag) {
		if (isRouteInfoCached(routeTag)) {
			// Already cached, don't do anything.
			Log.i("Route info cached", "for " + routeTag);
			return;
		}
		
		RouteInfo routeInfo = new RouteInfo(routeTag);

		Log.i("Fetching routeInfo", "");
		// Get the paths for the route.
		Cursor pathCursor = query(
				Uri.withAppendedPath(PathTable.CONTENT_URI, routeTag),
				null,  // Select all columns.
				null, null,  // Select all rows.
				null);  // No sort order.
		if (pathCursor.moveToFirst()) {
			do {
				long pathId = pathCursor.getLong(
						pathCursor.getColumnIndex(PathTable.Column._ID));
				Path path = new Path();
				routeInfo.addPath(path);
				Log.i("Path found", "p");
		
				// Get the points for the path.
				Cursor pointCursor = query(
						Uri.withAppendedPath(PointTable.CONTENT_URI, Long.toString(pathId)),
						null,  // Select all columns.
						null, null,  // Select all rows.
						PointTable.Column._ID);  // No sort order.
				if (pointCursor.moveToFirst()) {
					do {
						double lat = pointCursor.getDouble(
								pointCursor.getColumnIndex(PointTable.Column.LAT));
						double lon = pointCursor.getDouble(
								pointCursor.getColumnIndex(PointTable.Column.LON));
						path.addPoint(new Point(lat, lon));
						Log.i("Point found", "p");
					} while (pointCursor.moveToNext());
				}
				pointCursor.close();
						
			} while (pathCursor.moveToNext());
		}
		pathCursor.close();
		
		// Get the route details (line/text color).
		Cursor routeCursor = query(
				Uri.withAppendedPath(RouteTable.CONTENT_URI, routeTag),
				null,  // All columns.
				null, null,  // All rows.
				null);  // No sort order.
		if (routeCursor.moveToFirst()) {
			routeInfo.setLineColor(routeCursor.getString(
					routeCursor.getColumnIndex(RouteTable.Column.LINE_COLOR)));
			routeInfo.setTextColor(routeCursor.getString(
					routeCursor.getColumnIndex(RouteTable.Column.TEXT_COLOR)));
		}
		routeCursor.close();
		
		Log.i("done fetching route info", "");
		
		// Finally, update the cache.
		mRouteInfoMap.put(routeTag, routeInfo);
	}
	
	/**
	 * Return all the info about a route.
	 * @param routeTag the route tag.
	 * @return the route info.
	 */
	public RouteInfo getRouteInfo(String routeTag) {
		if (!isRouteInfoCached(routeTag)) {
			fetchAndCacheRouteInfo(routeTag);
		}
		return mRouteInfoMap.get(routeTag);
	}
	
	/**
	 * Query a content provider.  Wrapper around Activity.managedQuery().
	 * @return a cursor.
	 */
	protected Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return mActivity.managedQuery(uri, projection, selection, selectionArgs, sortOrder);
	}
}
