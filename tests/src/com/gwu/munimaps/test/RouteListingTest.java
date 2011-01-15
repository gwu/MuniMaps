package com.gwu.munimaps.test;

import com.gwu.munimaps.RouteListing;

import android.test.AndroidTestCase;

public class RouteListingTest extends AndroidTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGettersAndSetters() {
		RouteListing listing = new RouteListing();
		
		listing.setTag("tag");
		listing.setTitle("title");
		listing.setShortTitle("shortTitle");
		
		assertEquals("tag", listing.getTag());
		assertEquals("title", listing.getTitle());
		assertEquals("shortTitle", listing.getShortTitle());
	}
	
	public void testConstructor() {
		RouteListing listing = new RouteListing("tag", "title", "shortTitle");
		
		assertEquals("tag", listing.getTag());
		assertEquals("title", listing.getTitle());
		assertEquals("shortTitle", listing.getShortTitle());
	}
}
