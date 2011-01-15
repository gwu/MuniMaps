package com.gwu.munimaps;

/**
 * Summary data about a route.
 */
public class RouteListing {
	/** Unique id for a route. */
	private String mTag;
	
	/** Title of the route. */
	private String mTitle;
	
	/** Shortname for the route. */
	private String mShortTitle;
	
	public RouteListing() {
		this(null, null, null);
	}
	
	public RouteListing(String tag, String title, String shortTitle) {
		setTag(tag);
		mTitle = title;
		mShortTitle = shortTitle;
	}

	public void setTag(String tag) {
		mTag = tag;
	}

	public String getTag() {
		return mTag;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setShortTitle(String shortTitle) {
		mShortTitle = shortTitle;
	}
	
	public String getShortTitle() {
		return mShortTitle;
	}
}