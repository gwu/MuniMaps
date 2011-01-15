package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.List;

public class Path {
	private List<Point> mPoints;
	
	public Path() {
		mPoints = new ArrayList<Point>();
	}

	@Override
	protected Path clone() {
		Path path = new Path();
		path.mPoints = new ArrayList<Point>(mPoints);
		return path;
	}

	public void addPoint(Point point) {
		mPoints.add(point);
	}
	
	public List<Point> getPoints() {
		return mPoints;
	}
}
