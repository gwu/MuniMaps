package com.gwu.munimaps.provider;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.gwu.munimaps.RouteInfo;
import com.gwu.munimaps.RouteListing;

public class NextMuniContentProvider extends ContentProvider {
	public static final String AUTHORITY = "com.gwu.munimaps.provider.nextmunicontentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	private static final int NEXT_MUNI_ROUTE = 0;
	private static final int NEXT_MUNI_ROUTE_ID = 1;
	private static final int NEXT_MUNI_PATH = 2;
	private static final int NEXT_MUNI_POINT = 3;
	private static final int NEXT_MUNI_DIRECTIONS = 4;
	private static final int NEXT_MUNI_STOPS = 5;
	private static final int NEXT_MUNI_PREDICTIONS = 6;
	
	private static final UriMatcher URI_MATCHER;
	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	    URI_MATCHER.addURI(AUTHORITY, "route", NEXT_MUNI_ROUTE);
	    URI_MATCHER.addURI(AUTHORITY, "route/*", NEXT_MUNI_ROUTE_ID);
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
		return true;
	}

	@Override
	public String getType(Uri uri) {
	    switch (URI_MATCHER.match(uri)) {
	    case NEXT_MUNI_ROUTE:
	    	return RouteTable.CONTENT_TYPE;
	    case NEXT_MUNI_ROUTE_ID:
	    	return RouteTable.ITEM_CONTENT_TYPE;
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
			if (!getNextMuniDb().isRoutesFresh()) {
				List<RouteListing> routes = getFetcher().fetchRoutes();
				getNextMuniDb().updateRoutes(routes);
			}
			
			queryBuilder.setTables(RouteTable.TABLE_NAME);
			queryBuilder.setProjectionMap(RouteTable.PROJECTION_MAP);
			break;
		case NEXT_MUNI_ROUTE_ID:
			String routeId = uri.getLastPathSegment();
			queryBuilder.setTables(RouteTable.TABLE_NAME);
			queryBuilder.setProjectionMap(RouteTable.PROJECTION_MAP);
			selection = String.format("%s == ?", RouteTable.Column.TAG);
			selectionArgs = new String[] { routeId };
			break;
		case NEXT_MUNI_PATH:
			String routeTag = uri.getLastPathSegment();
			if (!getNextMuniDb().isRouteFresh(routeTag)) {
				Log.i("refreshing", "paths");
				// Update route in database.
				RouteInfo routeDetail = getFetcher().fetchRouteDetail(routeTag);
				getNextMuniDb().updateRouteDetail(routeDetail);
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
        SQLiteDatabase db = getNextMuniDb().getDb();
        Cursor c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method may take a long time to return, so don't call it from the main thread.
	 * @return the NextMuni database interface.
	 */
	protected NextMuniDatabase getNextMuniDb() {
		if (mNextMuniDatabase == null) {
			mNextMuniDatabase = new NextMuniDatabaseOpenHelper(getContext()).getNextMuniDatabase();
		}
		return mNextMuniDatabase;
	}
	
	/**
	 * Create a NextMuni data fetcher.
	 * @return the fetcher
	 */
	protected NextMuniFetcher getFetcher() {
		if (mNextMuniFetcher == null) {
			mNextMuniFetcher = new NextMuniFetcher();
		}
		return mNextMuniFetcher;
	}
}
