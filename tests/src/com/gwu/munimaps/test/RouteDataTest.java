package com.gwu.munimaps.test;

import java.util.List;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase;

import com.gwu.munimaps.Path;
import com.gwu.munimaps.RouteData;
import com.gwu.munimaps.RouteInfo;
import com.gwu.munimaps.RouteListing;
import com.gwu.munimaps.provider.NextMuniDatabase;
import com.gwu.munimaps.provider.NextMuniDatabaseOpenHelper;
import com.gwu.munimaps.provider.test.TestNextMuniContentProvider;
import com.gwu.munimaps.provider.test.TestNextMuniFetcher;

@SuppressWarnings("deprecation")
public class RouteDataTest extends ProviderTestCase<TestNextMuniContentProvider> {
	
	public RouteDataTest() {
		super(TestNextMuniContentProvider.class, TestNextMuniContentProvider.AUTHORITY);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// Inject an memory backed nextmuni database.
		SQLiteDatabase db = SQLiteDatabase.create(null);
		NextMuniDatabaseOpenHelper openHelper = new NextMuniDatabaseOpenHelper(getMockContext());
		openHelper.onCreate(db);
		getProvider().setNextMuniDb(new NextMuniDatabase(db));
		getProvider().setNextMuniFetcher(new TestNextMuniFetcher(getInstrumentation().getContext()));
	}

	/**
	 * A testable version of RouteData that doesn't depend on Activity.
	 */
	private static class TestRouteData extends RouteData {
		private ContentResolver mContentResolver;

		public TestRouteData(ContentResolver contentResolver) {
			super(null);
			mContentResolver = contentResolver;
		}

		@Override
		protected Cursor query(Uri uri, String[] projection, String selection,
				String[] selectionArgs, String sortOrder) {
			return mContentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
		}
	}
	
	public void testRouteData() {
		RouteData routeData = new TestRouteData(getMockContentResolver());
		
		assertFalse(routeData.isRouteListingsCached());
		
		// Test route listings.
		routeData.fetchAndCacheRouteListings();
		assertTrue(routeData.isRouteListingsCached());
		
		List<RouteListing> routeListings = routeData.getRouteListings();
		assertEquals(79, routeListings.size());
		
		
		// Test route info (detail).
		assertFalse(routeData.isRouteInfoCached("N"));
		
		routeData.fetchAndCacheRouteInfo("N");
		assertTrue(routeData.isRouteInfoCached("N"));
		
		RouteInfo routeInfo = routeData.getRouteInfo("N");
		assertEquals("N", routeInfo.getTag());
		assertEquals(16, routeInfo.getPaths().size());
		
		Path path = routeInfo.getPaths().get(0);
		assertEquals(17, path.getPoints().size());
	}
}
