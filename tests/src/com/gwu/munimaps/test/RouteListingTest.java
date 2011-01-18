package com.gwu.munimaps.test;

import junit.framework.TestCase;

import com.gwu.munimaps.RouteListing;

public class RouteListingTest extends TestCase {

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
