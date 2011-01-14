package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteDetail {
	public String mTag;
	public String mLineColor;  // six digit hex string rrggbb
	public String mTextColor;

	public Map<String, Direction> mDirections;
	public Map<String, Stop> mStops;
	public List<Path> mPaths;
	
	public RouteDetail(String tag) {
		mTag = tag;
		mDirections = new HashMap<String, Direction>();
		mStops = new HashMap<String, Stop>();
		mPaths = new ArrayList<Path>();
	}
	
	public void addDirection(Direction direction) {
		mDirections.put(direction.mTag, direction);
	}

	public void addStop(Stop stop) {
		mStops.put(stop.mTag, stop);
	}

	public void addPath(Path path) {
		mPaths.add(path);
	}
}
