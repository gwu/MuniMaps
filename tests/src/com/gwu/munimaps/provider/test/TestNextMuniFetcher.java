package com.gwu.munimaps.provider.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import android.content.Context;

import com.gwu.munimaps.provider.NextMuniFetcher;

public class TestNextMuniFetcher extends NextMuniFetcher {
	private Context mContext; 
	
	public TestNextMuniFetcher(Context context) {
		mContext = context;
	}
	
	/**
	 * Does some dependency injection to avoid really doing URL fetches.
	 */
	@Override
	protected InputStream openUrl(String url) throws MalformedURLException, IOException {
		if (url.equals(ROUTES_URL)) {
			return mContext.getAssets().open("route_list.xml");
		} else if (url.equals(String.format(ROUTE_CONFIG_URL, "N"))) {
			return mContext.getAssets().open("route_config.xml");
		}
		return null;
	}
}
