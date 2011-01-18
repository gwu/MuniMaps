package com.gwu.munimaps.test;

import java.util.List;

import android.test.InstrumentationTestCase;

import com.gwu.munimaps.MuniMapsPrefs;

public class MuniMapsPrefsTest extends InstrumentationTestCase {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testRouteTags() {
		MuniMapsPrefs prefs = new MuniMapsPrefs(getInstrumentation().getContext());
		
		assertFalse(prefs.isRouteSelected("foo"));
		
		List<String> emptySelection = prefs.getSelectedRouteTags();
		assertTrue(emptySelection.isEmpty());
		
		prefs.setRouteSelected("bar", true);
		assertTrue(prefs.isRouteSelected("bar"));
		
		List<String> barSelection = prefs.getSelectedRouteTags();
		assertEquals(1, barSelection.size());
		assertEquals("bar", barSelection.get(0));

		prefs.setRouteSelected("baz", true);
		List<String> bothSelection = prefs.getSelectedRouteTags();
		assertEquals(2, bothSelection.size());
		assertTrue(prefs.isRouteSelected("baz"));

		prefs.setRouteSelected("bar", false);
		List<String> bazSelection = prefs.getSelectedRouteTags();
		assertEquals(1, bazSelection.size());
		assertFalse(prefs.isRouteSelected("bar"));
		assertTrue(prefs.isRouteSelected("baz"));
	}
}
