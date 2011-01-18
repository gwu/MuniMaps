package com.gwu.munimaps.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Keeps a local cache of the NextMuni data.
 */
public class NextMuniDatabaseOpenHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 10;
	
	private NextMuniDatabase mNextMuniDatabase;
	
	public NextMuniDatabaseOpenHelper(Context context) {
		super(context, "NextMuni", null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			// Create all tables.
			LastUpdatedTable.create(db);
			RouteTable.create(db);
			PathTable.create(db);
			PointTable.create(db);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Since this is just a cached database (all real data is fetched from NextMuni servers, we'll just
		// drop all the tables and recreate them.
		db.beginTransaction();
		try {
			// Drop all tables.
			LastUpdatedTable.drop(db);
			RouteTable.drop(db);
			PathTable.drop(db);
			PointTable.drop(db);
			
			// Create the tables again.
			onCreate(db);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}
	
	/**
	 * Get an interface to the NextMuniDatabase.
	 * @return
	 */
	public NextMuniDatabase getNextMuniDatabase() {
		if (mNextMuniDatabase == null) {
			mNextMuniDatabase = new NextMuniDatabase(getWritableDatabase());
		}
		return mNextMuniDatabase;
	}
}
