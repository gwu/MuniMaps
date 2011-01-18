package com.gwu.munimaps.provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwu.munimaps.Path;
import com.gwu.munimaps.Point;
import com.gwu.munimaps.RouteInfo;
import com.gwu.munimaps.RouteListing;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Interface to the NextMuni data.
 */
public class NextMuniDatabase {
	private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
	
	private SQLiteDatabase mDb;
	
	public NextMuniDatabase(SQLiteDatabase db) {
		mDb = db;
	}
	
	/**
	 * @return the underlying SQLiteDatabase.
	 */
	public SQLiteDatabase getDb() {
		return mDb;
	}
	
	/**
	 * Update the database with the given routes.
	 * @param routes
	 */
	public void updateRoutes(List<RouteListing> routes) {
		// Index the routes by their tag.
		Map<String, RouteListing> routesByTag = new HashMap<String, RouteListing>(routes.size());
		for (RouteListing route : routes) {
			routesByTag.put(route.getTag(), route);
		}
		
		mDb.beginTransaction();
		try {
			// First read all the existing routes in the database.
			final String[] COLUMNS = new String[] { RouteTable.Column._ID, RouteTable.Column.TAG };
			Cursor existingRoutes = mDb.query(RouteTable.TABLE_NAME, COLUMNS, null, null, null, null, null);
			try {
		        for (existingRoutes.moveToFirst(); !existingRoutes.isAfterLast(); existingRoutes.moveToNext()) {
		        	long id = existingRoutes.getLong(0);
		        	String tag = existingRoutes.getString(1);
		        	
		        	if (routesByTag.containsKey(tag)) {
		        		// Update existing routes.
		        		ContentValues values = new ContentValues(2);
		        		RouteListing newRoute = routesByTag.remove(tag);
		        		values.put(RouteTable.Column.TITLE, newRoute.getTitle());
		        		values.put(RouteTable.Column.SHORT_TITLE, newRoute.getShortTitle());
		        		mDb.update(RouteTable.TABLE_NAME, values,
		        				String.format("%s == ?", RouteTable.Column._ID), new String[] { Long.toString(id) });
		        	} else {
		        		// Delete route that no longer exists.
		        		mDb.delete(RouteTable.TABLE_NAME,
		        				String.format("%s == ?", RouteTable.Column._ID), new String[] { Long.toString(id) });
		        	}
		        }
			} finally {
				existingRoutes.close();
			}
	        
	        // Add any brand new routes.
	        for (RouteListing newRoute : routesByTag.values()) {
	        	ContentValues values = new ContentValues(3);
	        	values.put(RouteTable.Column.TAG, newRoute.getTag());
	        	values.put(RouteTable.Column.TITLE, newRoute.getTitle());
	        	values.put(RouteTable.Column.SHORT_TITLE, newRoute.getShortTitle());
	        	mDb.insertOrThrow(RouteTable.TABLE_NAME, RouteTable.Column.TAG, values);
	        }
	        
	        // Mark the last updated time
	        markLastUpdated(LastUpdatedTable.ROUTES);
			
	        mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
	}

	/**
	 * @return whether the route listings are fresh.
	 */
	public boolean isRoutesFresh() {
		return isFresh(LastUpdatedTable.ROUTES, ONE_DAY_MS);
	}

	/**
	 * Update the database with the given route detail.
	 * @param routeDetail the route info.
	 */
	public void updateRouteDetail(RouteInfo routeDetail) {
		Log.i("updating route info:", routeDetail.getTag());
		mDb.beginTransaction();
		try {
			// Get all paths from the route.
			Cursor pathCursor = mDb.query(PathTable.TABLE_NAME, new String[] { PathTable.Column._ID },
					String.format("%s == ?", PathTable.Column.ROUTE),
					new String[] { routeDetail.getTag() }, null, null, null);
			if (pathCursor.moveToFirst()) {
				int pathId = pathCursor.getInt(0);
				
				// Delete all points for this path.
				mDb.delete(PointTable.TABLE_NAME,
						String.format("%s == ?", PointTable.Column.PATH),
						new String[] { Integer.toString(pathId) });
			}
			// Delete the paths.
			mDb.delete(PathTable.TABLE_NAME,
					String.format("%s == ?", PathTable.Column.ROUTE),
					new String[] { routeDetail.getTag() });
			
			// Fill the path and point tables.
			for (Path path : routeDetail.getPaths()) {
				// Insert the path.
				ContentValues pathValues = new ContentValues();
				pathValues.put(PathTable.Column.ROUTE, routeDetail.getTag());
				long pathId = mDb.insertOrThrow(PathTable.TABLE_NAME, PathTable.Column.ROUTE, pathValues);
				
				// Insert the points in the path.
				for (Point point : path.getPoints()) {
					ContentValues pointValues = new ContentValues();
					pointValues.put(PointTable.Column.PATH, pathId);
					pointValues.put(PointTable.Column.LAT, point.mLat);
					pointValues.put(PointTable.Column.LON, point.mLon);
					mDb.insertOrThrow(PointTable.TABLE_NAME, PointTable.Column.PATH, pointValues);
				}
			}
			
			// Fill in the line/text color.
			ContentValues detailValues = new ContentValues();
			detailValues.put(RouteTable.Column.LINE_COLOR, routeDetail.getLineColor());
			detailValues.put(RouteTable.Column.TEXT_COLOR, routeDetail.getTextColor());
			mDb.update(RouteTable.TABLE_NAME, detailValues,
					String.format("%s == ?", RouteTable.Column.TAG), new String[] { routeDetail.getTag() });
			
			// Mark last updated time.
			markLastUpdated(routeDetail.getTag());
			
			mDb.setTransactionSuccessful();
		} catch (Exception e) {
			Log.i("failed transaction", e.getMessage());
		} finally {
			mDb.endTransaction();
		}
		Log.i("finished updating routeInfo", routeDetail.getTag());
	}

	/**
	 * @param routeTag
	 * @return true if the route's info is fresh.
	 */
	public boolean isRouteFresh(String routeTag) {
		return isFresh(routeTag, ONE_DAY_MS);
	}
	
	/**
	 * Marks the key's last updated timestamp.
	 * @param key the string key.
	 */
	protected void markLastUpdated(String key) {
        Cursor lastUpdated = mDb.query(
        		LastUpdatedTable.TABLE_NAME,
        		new String[] { LastUpdatedTable.Column._ID, LastUpdatedTable.Column.KEY },
        		String.format("%s == ?", LastUpdatedTable.Column.KEY), new String[] { key },
        		null, null, null);
        if (lastUpdated.moveToFirst()) {
        	ContentValues values = new ContentValues(1);
        	values.put(LastUpdatedTable.Column.TIME, System.currentTimeMillis());
        	mDb.update(LastUpdatedTable.TABLE_NAME, values,
        			String.format("%s == ?", LastUpdatedTable.Column.KEY),
        			new String[] { key });
        } else {
        	ContentValues values = new ContentValues(2);
        	values.put(LastUpdatedTable.Column.KEY, key);
        	values.put(LastUpdatedTable.Column.TIME, System.currentTimeMillis());
        	mDb.insertOrThrow(LastUpdatedTable.TABLE_NAME, LastUpdatedTable.Column.KEY, values);
        }
	}
	
	/**
	 * Checks the LastUpdatedTable of the db.
	 * @param key the string key.
	 * @param shelfLife shelf life in ms.
	 * @return true if the key's last updated time is within the shelf-life threshold.
	 */
	protected boolean isFresh(String key, long shelfLife) {
		boolean isFresh = false;
		mDb.beginTransaction();
		try {
			Cursor lastUpdated = mDb.query(
					LastUpdatedTable.TABLE_NAME,
					new String[] { LastUpdatedTable.Column._ID, LastUpdatedTable.Column.TIME },
					String.format("%s == ?", LastUpdatedTable.Column.KEY), new String[] { key },
					null, null, null);
			if (lastUpdated.moveToFirst()) {
				long timeElapsed = lastUpdated.getLong(1) - System.currentTimeMillis();
				isFresh = timeElapsed < shelfLife;
			} else {
				isFresh = false;
			}
			mDb.setTransactionSuccessful();
		} finally {
			mDb.endTransaction();
		}
		
		return isFresh;
	}
}
