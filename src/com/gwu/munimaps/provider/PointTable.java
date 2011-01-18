package com.gwu.munimaps.provider;

import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class PointTable {
	public static final String TABLE_NAME = "Point";
	public static final Uri CONTENT_URI = Uri.parse("content://" + NextMuniContentProvider.AUTHORITY + "/point");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.gwu.munimaps.point";
	
	public static class Column implements BaseColumns {
		public static final String PATH = "path";
		public static final String LAT = "lat";
		public static final String LON = "lon";
	}
	
	public static final Map<String, String> PROJECTION_MAP;
	static {
		PROJECTION_MAP = new HashMap<String, String>();
		PROJECTION_MAP.put(Column._ID, Column._ID);
		PROJECTION_MAP.put(Column.PATH, Column.PATH);
		PROJECTION_MAP.put(Column.LAT, Column.LAT);
		PROJECTION_MAP.put(Column.LON, Column.LON);
	}
	
	public static void create(SQLiteDatabase db) {
		db.execSQL(String.format(
				"CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s)",
				TABLE_NAME,
				Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
				Column.PATH, "INTEGER",
				Column.LAT, "DECIMAL",
				Column.LON, "DECIMAL"));
	}
	
	public static void drop(SQLiteDatabase db) {
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
	}
}