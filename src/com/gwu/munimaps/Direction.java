package com.gwu.munimaps;

import java.util.ArrayList;
import java.util.List;

public class Direction {
	public String mTag;
	public String mTitle;
	public List<String> mStops;
	
	public Direction() {
		mStops = new ArrayList<String>();
	}

	@Override
	protected Direction clone() {
		Direction direction = new Direction();
		direction.mTag = mTag;
		direction.mTitle = mTitle;
		return direction;
	}

	public void addStop(String tag) {
		mStops.add(tag);
	}
}
