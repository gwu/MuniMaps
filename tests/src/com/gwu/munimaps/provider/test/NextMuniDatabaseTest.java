package com.gwu.munimaps.provider.test;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.InstrumentationTestCase;

import com.gwu.munimaps.Path;
import com.gwu.munimaps.Point;
import com.gwu.munimaps.RouteInfo;
import com.gwu.munimaps.RouteListing;
import com.gwu.munimaps.provider.NextMuniDatabase;
import com.gwu.munimaps.provider.NextMuniDatabaseOpenHelper;
import com.gwu.munimaps.provider.PathTable;
import com.gwu.munimaps.provider.PointTable;
import com.gwu.munimaps.provider.RouteTable;

public class NextMuniDatabaseTest extends InstrumentationTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testDatabase() {
		// Initialize a NextMuni database.
		SQLiteDatabase db = SQLiteDatabase.create(null);
		NextMuniDatabaseOpenHelper openHelper = new NextMuniDatabaseOpenHelper(getInstrumentation().getContext());
		openHelper.onCreate(db);
		
		NextMuniDatabase nextMuniDb = new NextMuniDatabase(db);
		assertEquals(db, nextMuniDb.getDb());
		
		// The routes should not be fresh (never updated).
		assertFalse(nextMuniDb.isRoutesFresh());
		
		// Update the route listings.
		List<RouteListing> routeListings = new ArrayList<RouteListing>();
		RouteListing routeListing0 = new RouteListing("foo", "foo", "");
		routeListings.add(routeListing0);
		RouteListing routeListing1 = new RouteListing("bar", "bar", "");
		routeListings.add(routeListing1);
		nextMuniDb.updateRoutes(routeListings);
		assertTrue(nextMuniDb.isRoutesFresh());
		
		// Make sure the route listings appeared in the table.
		Cursor routeListingCursor = nextMuniDb.getDb().query(
				RouteTable.TABLE_NAME,
				new String[] { RouteTable.Column._ID, RouteTable.Column.TAG, RouteTable.Column.TITLE },
				null, null, null, null,
				RouteTable.Column.TAG);
		assertTrue(routeListingCursor.moveToFirst());
		assertEquals("bar", routeListingCursor.getString(1));
		assertEquals("bar", routeListingCursor.getString(2));
		assertTrue(routeListingCursor.moveToNext());
		assertEquals("foo", routeListingCursor.getString(1));
		assertEquals("foo", routeListingCursor.getString(2));
		
		// The foo route should not be fresh (never updated).
		assertFalse(nextMuniDb.isRouteFresh("foo"));
		
		// Update the foo route.
		RouteInfo fooRoute = new RouteInfo("foo");
		fooRoute.setLineColor("000000");
		fooRoute.setTextColor("ffffff");
		Path path0 = new Path();
		path0.addPoint(new Point(0, 0));
		path0.addPoint(new Point(1, 1));
		fooRoute.addPath(path0);
		nextMuniDb.updateRouteDetail(fooRoute);
		assertTrue(nextMuniDb.isRouteFresh("foo"));
		
		// Make sure the foo route info was put into the database.
		Cursor fooCursor = nextMuniDb.getDb().query(
				RouteTable.TABLE_NAME,
				new String[] { RouteTable.Column._ID, RouteTable.Column.TAG,
						RouteTable.Column.LINE_COLOR, RouteTable.Column.TEXT_COLOR },
				String.format("%s == ?", RouteTable.Column.TAG),
				new String[] { "foo" },
				null, null, null);
		assertTrue(fooCursor.moveToFirst());
		assertEquals("foo", fooCursor.getString(1));
		assertEquals("000000", fooCursor.getString(2));
		assertEquals("ffffff", fooCursor.getString(3));
		assertFalse(fooCursor.moveToNext());
		
		Cursor pathCursor = nextMuniDb.getDb().query(
				PathTable.TABLE_NAME,
				new String[] { PathTable.Column._ID },
				String.format("%s == ?", PathTable.Column.ROUTE),
				new String[] { "foo" },
				null, null, null);
		assertTrue(pathCursor.moveToFirst());
		long pathId0 = pathCursor.getLong(0);
		assertFalse(pathCursor.moveToNext());
		
		Cursor pointCursor = nextMuniDb.getDb().query(
				PointTable.TABLE_NAME,
				new String[] { PointTable.Column._ID, PointTable.Column.LAT, PointTable.Column.LON },
				String.format("%s == ?", PointTable.Column.PATH),
				new String[] { Long.toString(pathId0) },
				null,
				null,
				PointTable.Column._ID);
		assertTrue(pointCursor.moveToFirst());
		assertEquals(0.0, pointCursor.getDouble(1));
		assertEquals(0.0, pointCursor.getDouble(2));
		assertTrue(pointCursor.moveToNext());
		assertEquals(1.0, pointCursor.getDouble(1));
		assertEquals(1.0, pointCursor.getDouble(2));
		assertFalse(pointCursor.moveToNext());
	}
}
