package com.gwu.munimaps.test;

import com.gwu.munimaps.MuniMaps;

import android.test.ActivityInstrumentationTestCase2;

public class MuniMapsTest extends ActivityInstrumentationTestCase2<MuniMaps> {

	public MuniMapsTest() {
		super("com.gwu.munimaps", MuniMaps.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testPreconditions() {
		// TODO
	}
	
	public void testFoo() {
		assertNotNull(getActivity());
	}
}
