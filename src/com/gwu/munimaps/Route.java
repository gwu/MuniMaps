package com.gwu.munimaps;

public class Route {
	public String mTag;
	public String mTitle;
	public String mShortTitle;
	
	public Route() {
		this(null, null, null);
	}
	
	public Route(String tag, String title, String shortTitle) {
		mTag = tag;
		mTitle = title;
		mShortTitle = shortTitle;
	}
}