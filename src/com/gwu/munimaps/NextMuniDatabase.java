package com.gwu.munimaps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwu.munimaps.NextMuniContentProvider.PathTable;
import com.gwu.munimaps.NextMuniContentProvider.PointTable;
import com.gwu.munimaps.NextMuniContentProvider.RouteTable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Keeps a local cache of the NextMuni data.
 */
public class NextMuniDatabase extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 4;
	private static final long ONE_DAY_MS = 24 * 60 * 60 * 1000;
	
	public static class LastUpdatedTable {
		public static final String TABLE_NAME = "LastUpdated";
		
		public static final String ROUTES = "Routes";
		
		public static class Column implements BaseColumns {
			public static final String KEY = "key";
			public static final String TIME = "time";
		}

		static final Map<String, String> PROJECTION_MAP;
		static {
			PROJECTION_MAP = new HashMap<String, String>();
			PROJECTION_MAP.put(Column._ID, Column._ID);
			PROJECTION_MAP.put(Column.KEY, Column.KEY);
			PROJECTION_MAP.put(Column.TIME, Column.TIME);
		}
		
		static void create(SQLiteDatabase db) {
			db.execSQL(String.format(
					"CREATE TABLE %s (%s %s, %s %s, %s %s)",
					TABLE_NAME,
					Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
					Column.KEY, "TEXT UNIQUE",
					Column.TIME, "INTEGER"));
		}
		
		static void drop(SQLiteDatabase db) {
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
		}
	}

	public NextMuniDatabase(Context context) {
		super(context, "NextMuni", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			// Create all tables.
			LastUpdatedTable.create(db);
			RouteTable.create(db);
			PathTable.create(db);
			PointTable.create(db);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Since this is just a cached database (all real data is fetched from NextMuni servers, we'll just
		// drop all the tables and recreate them.
		db.beginTransaction();
		try {
			// Drop all tables.
			LastUpdatedTable.drop(db);
			RouteTable.drop(db);
			PathTable.drop(db);
			PointTable.drop(db);
			
			// Create the tables again.
			onCreate(db);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	private void markLastUpdated(SQLiteDatabase db, String key) {
        Cursor lastUpdated = db.query(
        		LastUpdatedTable.TABLE_NAME,
        		new String[] { LastUpdatedTable.Column._ID, LastUpdatedTable.Column.KEY },
        		String.format("%s == ?", LastUpdatedTable.Column.KEY), new String[] { key },
        		null, null, null);
        if (lastUpdated.moveToFirst()) {
        	ContentValues values = new ContentValues(1);
        	values.put(LastUpdatedTable.Column.TIME, System.currentTimeMillis());
        	db.update(LastUpdatedTable.TABLE_NAME, values,
        			String.format("%s == ?", LastUpdatedTable.Column.KEY),
        			new String[] { key });
        } else {
        	ContentValues values = new ContentValues(2);
        	values.put(LastUpdatedTable.Column.KEY, key);
        	values.put(LastUpdatedTable.Column.TIME, System.currentTimeMillis());
        	db.insertOrThrow(LastUpdatedTable.TABLE_NAME, LastUpdatedTable.Column.KEY, values);
        }
	}
	
	private boolean isFresh(String key, long shelfLife) {
		boolean isFresh = false;
		SQLiteDatabase db = getReadableDatabase();
		db.beginTransaction();
		try {
			Cursor lastUpdated = db.query(
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
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return isFresh;
	}
	
	/**
	 * Update the database with the given routes.
	 * @param routes
	 */
	public void updateRoutes(List<Route> routes) {
		// Index the routes by their tag.
		Map<String, Route> routesByTag = new HashMap<String, Route>(routes.size());
		for (Route route : routes) {
			routesByTag.put(route.mTag, route);
		}
		
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			// First read all the existing routes in the database.
			final String[] COLUMNS = new String[] { RouteTable.Column._ID, RouteTable.Column.TAG };
			Cursor existingRoutes = db.query(RouteTable.TABLE_NAME, COLUMNS, null, null, null, null, null);
			try {
		        for (existingRoutes.moveToFirst(); !existingRoutes.isAfterLast(); existingRoutes.moveToNext()) {
		        	long id = existingRoutes.getLong(0);
		        	String tag = existingRoutes.getString(1);
		        	
		        	if (routesByTag.containsKey(tag)) {
		        		// Update existing routes.
		        		ContentValues values = new ContentValues(2);
		        		Route newRoute = routesByTag.remove(tag);
		        		values.put(RouteTable.Column.TITLE, newRoute.mTitle);
		        		values.put(RouteTable.Column.SHORT_TITLE, newRoute.mShortTitle);
		        		db.update(RouteTable.TABLE_NAME, values,
		        				String.format("%s == ?", RouteTable.Column._ID), new String[] { Long.toString(id) });
		        	} else {
		        		// Delete route that no longer exists.
		        		db.delete(RouteTable.TABLE_NAME,
		        				String.format("%s == ?", RouteTable.Column._ID), new String[] { Long.toString(id) });
		        	}
		        }
			} finally {
				existingRoutes.close();
			}
	        
	        // Add any brand new routes.
	        for (Route newRoute : routesByTag.values()) {
	        	ContentValues values = new ContentValues(3);
	        	values.put(RouteTable.Column.TAG, newRoute.mTag);
	        	values.put(RouteTable.Column.TITLE, newRoute.mTitle);
	        	values.put(RouteTable.Column.SHORT_TITLE, newRoute.mShortTitle);
	        	db.insertOrThrow(RouteTable.TABLE_NAME, RouteTable.Column.TAG, values);
	        }
	        
	        // Mark the last updated time
	        markLastUpdated(db, LastUpdatedTable.ROUTES);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean isRoutesFresh() {
		return isFresh(LastUpdatedTable.ROUTES, ONE_DAY_MS);
	}

	public void updateRouteDetail(RouteDetail routeDetail) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			// Get all paths from the route.
			Cursor pathCursor = db.query(PathTable.TABLE_NAME, new String[] { PathTable.Column._ID },
					String.format("%s == ?", PathTable.Column.ROUTE),
					new String[] { routeDetail.mTag }, null, null, null);
			if (pathCursor.moveToFirst()) {
				int pathId = pathCursor.getInt(0);
				
				// Delete all points for this path.
				db.delete(PointTable.TABLE_NAME,
						String.format("%s == ?", PointTable.Column.PATH),
						new String[] { Integer.toString(pathId) });
			}
			// Delete the paths.
			db.delete(PathTable.TABLE_NAME,
					String.format("%s == ?", PathTable.Column.ROUTE),
					new String[] { routeDetail.mTag });
			
			// Fill the path and point tables.
			for (Path path : routeDetail.mPaths) {
				// Insert the path.
				ContentValues pathValues = new ContentValues();
				pathValues.put(PathTable.Column.ROUTE, routeDetail.mTag);
				long pathId = db.insertOrThrow(PathTable.TABLE_NAME, PathTable.Column.ROUTE, pathValues);
				
				// Insert the points in the path.
				for (Point point : path.mPoints) {
					ContentValues pointValues = new ContentValues();
					pointValues.put(PointTable.Column.PATH, pathId);
					pointValues.put(PointTable.Column.LAT, point.mLat);
					pointValues.put(PointTable.Column.LON, point.mLon);
					db.insertOrThrow(PointTable.TABLE_NAME, PointTable.Column.PATH, pointValues);
				}
			}
			
			// Fill in the line/text color.
			ContentValues detailValues = new ContentValues();
			detailValues.put(RouteTable.Column.LINE_COLOR, routeDetail.mLineColor);
			detailValues.put(RouteTable.Column.TEXT_COLOR, routeDetail.mTextColor);
			db.update(RouteTable.TABLE_NAME, detailValues,
					String.format("%s == ?", RouteTable.Column.TAG), new String[] { routeDetail.mTag });
			
			// Mark last updated time.
			markLastUpdated(db, routeDetail.mTag);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean isRouteFresh(String routeTag) {
		return isFresh(routeTag, ONE_DAY_MS);
	}
}
