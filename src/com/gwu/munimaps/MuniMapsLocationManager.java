package com.gwu.munimaps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class MuniMapsLocationManager {
	private LocationManager mLocationManager;
	private LocationListener mCourseListener;
	private LocationListener mPreciseListener;
	
	public MuniMapsLocationManager(LocationManager manager) {
		mLocationManager = manager;
		registerListeners();
	}

	private void registerListeners() {
		mCourseListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			
		};
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mCourseListener);
		mPreciseListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub
				
			}
		
		};
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mPreciseListener);
	}
	
	public Location getBestLocation() {
		Location courseLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location preciseLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		// TODO: check for new updates.
		return betterLocation(courseLocation, preciseLocation);
	}
	
	private Location betterLocation(Location loc1, Location loc2) {
		return loc1;
	}
}
