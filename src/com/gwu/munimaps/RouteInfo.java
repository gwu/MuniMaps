package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything you could want to know about a route.
 */
public class RouteInfo {
	private String mTag;
	private String mLineColor;  // six digit hex string rrggbb
	private String mTextColor;

	private Map<String, Direction> mDirections;
	private Map<String, Stop> mStops;
	private List<Path> mPaths;
	
	public RouteInfo(String tag) {
		mTag = tag;
		mDirections = new HashMap<String, Direction>();
		mStops = new HashMap<String, Stop>();
		mPaths = new ArrayList<Path>();
	}
	
	public void setTag(String tag) {
		mTag = tag;
	}
	
	public String getTag() {
		return mTag;
	}
	
	public void setLineColor(String color) {
		mLineColor = color;
	}
	
	public String getLineColor() {
		return mLineColor;
	}
	
	public void setTextColor(String color) {
		mTextColor = color;
	}
	
	public String getTextColor() {
		return mTextColor;
	}

	public void addPath(Path path) {
		mPaths.add(path);
	}

	public List<Path> getPaths() {
		return mPaths;
	}
	
	public void addDirection(Direction direction) {
		mDirections.put(direction.mTag, direction);
	}

	public void addStop(Stop stop) {
		mStops.put(stop.mTag, stop);
	}
}
