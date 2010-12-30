package com.gwu.munimaps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gwu.munimaps.NextMuniContentProvider.RouteTable;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Keeps a local cache of the NextMuni data.
 */
public class NextMuniDatabase extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
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
			
			// Create the tables again.
			onCreate(db);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
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
			// First read all the existing routes in the db.
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
	        Cursor lastUpdated = db.query(
	        		LastUpdatedTable.TABLE_NAME,
	        		new String[] { LastUpdatedTable.Column._ID, LastUpdatedTable.Column.KEY },
	        		String.format("%s == ?", LastUpdatedTable.Column.KEY), new String[] { LastUpdatedTable.ROUTES },
	        		null, null, null);
	        if (lastUpdated.moveToFirst()) {
	        	ContentValues values = new ContentValues(1);
	        	values.put(LastUpdatedTable.Column.TIME, System.currentTimeMillis());
	        	db.update(LastUpdatedTable.TABLE_NAME, values,
	        			String.format("%s == ?", LastUpdatedTable.Column.KEY),
	        			new String[] { LastUpdatedTable.ROUTES });
	        } else {
	        	ContentValues values = new ContentValues(2);
	        	values.put(LastUpdatedTable.Column.KEY, LastUpdatedTable.ROUTES);
	        	values.put(LastUpdatedTable.Column.TIME, System.currentTimeMillis());
	        	db.insertOrThrow(LastUpdatedTable.TABLE_NAME, LastUpdatedTable.Column.KEY, values);
	        }
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	public boolean isRoutesFresh() {
		boolean isFresh = false;
		SQLiteDatabase db = getReadableDatabase();
		db.beginTransaction();
		try {
			Cursor lastUpdated = db.query(
					LastUpdatedTable.TABLE_NAME,
					new String[] { LastUpdatedTable.Column._ID, LastUpdatedTable.Column.TIME },
					String.format("%s == ?", LastUpdatedTable.Column.KEY), new String[] { LastUpdatedTable.ROUTES },
					null, null, null);
			if (lastUpdated.moveToFirst()) {
				long timeElapsed = lastUpdated.getLong(1) - System.currentTimeMillis();
				isFresh = timeElapsed < ONE_DAY_MS;
			} else {
				isFresh = false;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return isFresh;
	}
}
