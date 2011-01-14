package com.gwu.munimaps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class MuniMapsLocationManager {
	private static final int REQUIRED_ACCURACY_METERS = 20;
	private LocationManager mLocationManager;
	private LocationListener mCourseListener;
	private LocationListener mPreciseListener;
	private Location mBestLocation;
	
	public MuniMapsLocationManager(LocationManager manager) {
		mLocationManager = manager;
		registerListeners();
	}

	private void registerListeners() {
		mCourseListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				mBestLocation = betterLocation(mBestLocation, location);
				if (mBestLocation.hasAccuracy() && mBestLocation.getAccuracy() < REQUIRED_ACCURACY_METERS) {
					mLocationManager.removeUpdates(mCourseListener);
					mCourseListener = null;
				}
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
				mBestLocation = betterLocation(mBestLocation, location);
				if (mBestLocation.hasAccuracy() && mBestLocation.getAccuracy() < REQUIRED_ACCURACY_METERS) {
					mLocationManager.removeUpdates(mPreciseListener);
					mPreciseListener = null;
				}
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
		if (mBestLocation != null) {
			return mBestLocation;
		}
		Location courseLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		Location preciseLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		return betterLocation(courseLocation, preciseLocation);
	}
	
	private Location betterLocation(Location loc1, Location loc2) {
		if (loc1 == null || !loc1.hasAccuracy()) {
			return loc2;
		}
		if (loc2 == null || !loc2.hasAccuracy()) {
			return loc1;
		}
		return loc1.getAccuracy() < loc2.getAccuracy() ? loc1 : loc2;
	}
}
