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
			public static final String LINE_COLOR = "line_color";
			public static final String TEXT_COLOR = "text_color";
		}

		static final Map<String, String> PROJECTION_MAP;
		static {
			PROJECTION_MAP = new HashMap<String, String>();
			PROJECTION_MAP.put(Column._ID, Column._ID);
			PROJECTION_MAP.put(Column.TAG, Column.TAG);
			PROJECTION_MAP.put(Column.TITLE, Column.TITLE);
			PROJECTION_MAP.put(Column.SHORT_TITLE, Column.SHORT_TITLE);
			PROJECTION_MAP.put(Column.LINE_COLOR, Column.LINE_COLOR);
			PROJECTION_MAP.put(Column.TEXT_COLOR, Column.TEXT_COLOR);
		}
		
		static void create(SQLiteDatabase db) {
			db.execSQL(String.format(
					"CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s, %s %s, %s %s)",
					TABLE_NAME,
					Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
					Column.TAG, "TEXT UNIQUE",
					Column.TITLE, "TEXT",
					Column.SHORT_TITLE, "TEXT",
					Column.LINE_COLOR, "TEXT",
					Column.TEXT_COLOR, "TEXT"));
		}
		
		static void drop(SQLiteDatabase db) {
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
		}
	}

	public static class PathTable {
		public static final String TABLE_NAME = "Path";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/path");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.gwu.munimaps.path";
		
		public static class Column implements BaseColumns {
			public static final String ROUTE = "route";
		}

		static final Map<String, String> PROJECTION_MAP;
		static {
			PROJECTION_MAP = new HashMap<String, String>();
			PROJECTION_MAP.put(Column._ID, Column._ID);
			PROJECTION_MAP.put(Column.ROUTE, Column.ROUTE);
		}
		
		static void create(SQLiteDatabase db) {
			db.execSQL(String.format(
					"CREATE TABLE %s (%s %s, %s %s)",
					TABLE_NAME,
					Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
					Column.ROUTE, "TEXT"));
		}
		
		static void drop(SQLiteDatabase db) {
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
		}
	}
	
	public static class PointTable {
		public static final String TABLE_NAME = "Point";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/point");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.gwu.munimaps.point";
		
		public static class Column implements BaseColumns {
			public static final String PATH = "path";
			public static final String LAT = "lat";
			public static final String LON = "lon";
		}
		
		static final Map<String, String> PROJECTION_MAP;
		static {
			PROJECTION_MAP = new HashMap<String, String>();
			PROJECTION_MAP.put(Column._ID, Column._ID);
			PROJECTION_MAP.put(Column.PATH, Column.PATH);
			PROJECTION_MAP.put(Column.LAT, Column.LAT);
			PROJECTION_MAP.put(Column.LON, Column.LON);
		}
		
		static void create(SQLiteDatabase db) {
			db.execSQL(String.format(
					"CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s)",
					TABLE_NAME,
					Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
					Column.PATH, "INTEGER",
					Column.LAT, "DECIMAL",
					Column.LON, "DECIMAL"));
		}
		
		static void drop(SQLiteDatabase db) {
			db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
		}
	}
	
	private static final int NEXT_MUNI_ROUTE = 0;
	private static final int NEXT_MUNI_PATH = 1;
	private static final int NEXT_MUNI_POINT = 2;
	private static final int NEXT_MUNI_DIRECTIONS = 3;
	private static final int NEXT_MUNI_STOPS = 4;
	private static final int NEXT_MUNI_PREDICTIONS = 5;
	
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	    URI_MATCHER.addURI(AUTHORITY, "route", NEXT_MUNI_ROUTE);
	    URI_MATCHER.addURI(AUTHORITY, "path/*", NEXT_MUNI_PATH);
	    URI_MATCHER.addURI(AUTHORITY, "point/*", NEXT_MUNI_POINT);
	    URI_MATCHER.addURI(AUTHORITY, "directions/*", NEXT_MUNI_DIRECTIONS);
	    URI_MATCHER.addURI(AUTHORITY, "stops/*/*", NEXT_MUNI_STOPS);
	    URI_MATCHER.addURI(AUTHORITY, "predictions/#", NEXT_MUNI_PREDICTIONS);
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
	    case NEXT_MUNI_PATH:
	    	return PathTable.CONTENT_TYPE;
	    case NEXT_MUNI_POINT:
	    	return PointTable.CONTENT_TYPE;
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
		case NEXT_MUNI_PATH:
			String routeTag = uri.getLastPathSegment();
			if (!mNextMuniDatabase.isRouteFresh(routeTag)) {
				// Update route in database.
				RouteDetail routeDetail = mNextMuniFetcher.fetchRouteDetail(routeTag);
				mNextMuniDatabase.updateRouteDetail(routeDetail);
			}
			
			// Get the List of paths from the route.
			queryBuilder.setTables(PathTable.TABLE_NAME);
			queryBuilder.setProjectionMap(PathTable.PROJECTION_MAP);
			selection = String.format("%s == ?", PathTable.Column.ROUTE);
			selectionArgs = new String[] { routeTag };
			break;
		case NEXT_MUNI_POINT:
			String pathId = uri.getLastPathSegment();
			queryBuilder.setTables(PointTable.TABLE_NAME);
			queryBuilder.setProjectionMap(PointTable.PROJECTION_MAP);
			selection = String.format("%s == ?", PointTable.Column.PATH);
			selectionArgs = new String[] { pathId };
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

        // Get the database and run the query
        SQLiteDatabase db = mNextMuniDatabase.getReadableDatabase();
        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

}
