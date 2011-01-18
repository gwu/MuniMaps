package com.gwu.munimaps.provider;

import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class LastUpdatedTable {
	public static final String TABLE_NAME = "LastUpdated";
	
	public static final String ROUTES = "Routes";
	
	public static class Column implements BaseColumns {
		public static final String KEY = "key";
		public static final String TIME = "time";
	}

	public static final Map<String, String> PROJECTION_MAP;
	static {
		PROJECTION_MAP = new HashMap<String, String>();
		PROJECTION_MAP.put(Column._ID, Column._ID);
		PROJECTION_MAP.put(Column.KEY, Column.KEY);
		PROJECTION_MAP.put(Column.TIME, Column.TIME);
	}
	
	public static void create(SQLiteDatabase db) {
		db.execSQL(String.format(
				"CREATE TABLE %s (%s %s, %s %s, %s %s)",
				TABLE_NAME,
				Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
				Column.KEY, "TEXT UNIQUE",
				Column.TIME, "INTEGER"));
	}
	
	public static void drop(SQLiteDatabase db) {
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
	}
}
