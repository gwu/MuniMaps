package com.gwu.munimaps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class NextMuniContentProvider extends ContentProvider {
	public static final String AUTHORITY = "com.gwu.munimaps.nextmunicontentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	public static class RouteTable {
		public static final String TABLE_NAME = "Route";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/route");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.gwu.munimaps.route";
		public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.com.gwu.munimaps.route";
		
		public static class Column implements BaseColumns {
			public static final String TAG = "tag";
			public static final String TITLE = "title";
			public static final String SHORT_TITLE = "short_title";
		}

		static final Map<String, String> PROJECTION_MAP;
		static {
			PROJECTION_MAP = new HashMap<String, String>();
			PROJECTION_MAP.put(Column._ID, Column._ID);
			PROJECTION_MAP.put(Column.TAG, Column.TAG);
			PROJECTION_MAP.put(Column.TITLE, Column.TITLE);
			PROJECTION_MAP.put(Column.SHORT_TITLE, Column.SHORT_TITLE);
		}
		
		static void create(SQLiteDatabase db) {
			db.execSQL(String.format(
					"CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s)",
					TABLE_NAME,
					Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
					Column.TAG, "TEXT UNIQUE",
					Column.TITLE, "TEXT",
					Column.SHORT_TITLE, "TEXT"));
		}
		
		static void drop(SQLiteDatabase db) {
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
		}
	}
	
	private static final int NEXT_MUNI_ROUTE = 0;
	private static final int NEXT_MUNI_ROUTE_ID = 1;
	private static final int NEXT_MUNI_DIRECTIONS = 2;
	private static final int NEXT_MUNI_STOPS = 3;
	private static final int NEXT_MUNI_PREDICTIONS = 4;
	
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	    URI_MATCHER.addURI(AUTHORITY, "route", NEXT_MUNI_ROUTE);
	    URI_MATCHER.addURI(AUTHORITY.toString(), "route/#", NEXT_MUNI_ROUTE_ID);
	    URI_MATCHER.addURI(AUTHORITY.toString(), "directions/*", NEXT_MUNI_DIRECTIONS);
	    URI_MATCHER.addURI(AUTHORITY.toString(), "stops/*/*", NEXT_MUNI_STOPS);
	    URI_MATCHER.addURI(AUTHORITY.toString(), "predictions/#", NEXT_MUNI_PREDICTIONS);
	}
	
	private NextMuniFetcher mNextMuniFetcher;
	private NextMuniDatabase mNextMuniDatabase;

	@Override
	public boolean onCreate() {
		mNextMuniFetcher = new NextMuniFetcher();
		mNextMuniDatabase = new NextMuniDatabase(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
	    switch (URI_MATCHER.match(uri)) {
	    case NEXT_MUNI_ROUTE:
	    	return RouteTable.CONTENT_TYPE;
	    case NEXT_MUNI_ROUTE_ID:
	    	return RouteTable.ITEM_CONTENT_TYPE;
	    case NEXT_MUNI_DIRECTIONS:
	      return "vnd.android.cursor.dir/vnd.com.gwu.munimaps.direction";
	    case NEXT_MUNI_STOPS:
	      return "vnd.android.cursor.dir/vnd.com.gwu.munimaps.stop";
	    case NEXT_MUNI_PREDICTIONS:
	      return "vnd.android.cursor.dir/vnd.com.gwu.munimaps.prediction";
	    default:
	      throw new IllegalArgumentException("Unknown URI " + uri);
	    }
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		switch (URI_MATCHER.match(uri)) {
		case NEXT_MUNI_ROUTE:
			if (!mNextMuniDatabase.isRoutesFresh()) {
				List<Route> routes = mNextMuniFetcher.fetchRoutes();
				mNextMuniDatabase.updateRoutes(routes);
			}
			
			queryBuilder.setTables(RouteTable.TABLE_NAME);
			queryBuilder.setProjectionMap(RouteTable.PROJECTION_MAP);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

        // Get the database and run the query
        SQLiteDatabase db = mNextMuniDatabase.getReadableDatabase();
        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, "TAG");

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

}
