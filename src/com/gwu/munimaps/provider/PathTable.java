package com.gwu.munimaps.provider;

import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class PathTable {
	public static final String TABLE_NAME = "Path";
	public static final Uri CONTENT_URI = Uri.parse("content://" + NextMuniContentProvider.AUTHORITY + "/path");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.gwu.munimaps.path";
	
	public static class Column implements BaseColumns {
		public static final String ROUTE = "route";
	}

	public static final Map<String, String> PROJECTION_MAP;
	static {
		PROJECTION_MAP = new HashMap<String, String>();
		PROJECTION_MAP.put(Column._ID, Column._ID);
		PROJECTION_MAP.put(Column.ROUTE, Column.ROUTE);
	}
	
	public static void create(SQLiteDatabase db) {
		db.execSQL(String.format(
				"CREATE TABLE %s (%s %s, %s %s)",
				TABLE_NAME,
				Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
				Column.ROUTE, "TEXT"));
	}
	
	public static void drop(SQLiteDatabase db) {
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
	}
}