package com.gwu.munimaps.provider.test;

import java.util.List;

import android.test.InstrumentationTestCase;

import com.gwu.munimaps.Path;
import com.gwu.munimaps.Point;
import com.gwu.munimaps.RouteInfo;
import com.gwu.munimaps.RouteListing;
import com.gwu.munimaps.provider.NextMuniFetcher;

public class NextMuniFetcherTest extends InstrumentationTestCase {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testFetchRoutes() {
		NextMuniFetcher fetcher = new TestNextMuniFetcher(getInstrumentation().getContext());
		List<RouteListing> listings = fetcher.fetchRoutes();
		
		assertEquals(79, listings.size());
		RouteListing listing0 = listings.get(0);
		assertEquals("1", listing0.getTag());
		assertEquals("1-California", listing0.getTitle());
	}
	
	public void testFetchRouteDetail() {
		NextMuniFetcher fetcher = new TestNextMuniFetcher(getInstrumentation().getContext());
		RouteInfo routeInfo = fetcher.fetchRouteDetail("N");
		
		assertEquals("N", routeInfo.getTag());
		assertEquals("003399", routeInfo.getLineColor());
		assertEquals("ffffff", routeInfo.getTextColor());
		
		List<Path> paths = routeInfo.getPaths();
		assertEquals(16, paths.size());
		
		Path path0 = paths.get(0);
		List<Point> points = path0.getPoints();
		assertEquals(17, points.size());
		
		Point point0 = points.get(0);
		assertEquals(37.765, point0.mLat);
		assertEquals(-122.45656, point0.mLon);
		
		Point point1 = points.get(1);
		assertEquals(37.76482, point1.mLat);
		assertEquals(-122.45771, point1.mLon);
	}
}
