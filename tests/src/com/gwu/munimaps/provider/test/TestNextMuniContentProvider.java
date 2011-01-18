package com.gwu.munimaps.provider.test;

import com.gwu.munimaps.provider.NextMuniContentProvider;
import com.gwu.munimaps.provider.NextMuniDatabase;
import com.gwu.munimaps.provider.NextMuniFetcher;

public class TestNextMuniContentProvider extends NextMuniContentProvider {
	private NextMuniDatabase mNextMuniDb;
	private NextMuniFetcher mFetcher;
	
	public void setNextMuniDb(NextMuniDatabase db) {
		mNextMuniDb = db;
	}
	
	public void setNextMuniFetcher(NextMuniFetcher fetcher) {
		mFetcher = fetcher;
	}
	
	@Override
	protected NextMuniDatabase getNextMuniDb() {
		return mNextMuniDb;
	}

	@Override
	protected NextMuniFetcher getFetcher() {
		return mFetcher;
	}
	
}
