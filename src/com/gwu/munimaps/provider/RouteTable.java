package com.gwu.munimaps.provider;

import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

public class RouteTable {
	public static final String TABLE_NAME = "Route";
	public static final Uri CONTENT_URI = Uri.parse("content://" + NextMuniContentProvider.AUTHORITY + "/route");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.gwu.munimaps.route";
	public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.com.gwu.munimaps.route";
	
	public static class Column implements BaseColumns {
		public static final String TAG = "tag";
		public static final String TITLE = "title";
		public static final String SHORT_TITLE = "short_title";
		public static final String LINE_COLOR = "line_color";
		public static final String TEXT_COLOR = "text_color";
	}

	public static final Map<String, String> PROJECTION_MAP;
	static {
		PROJECTION_MAP = new HashMap<String, String>();
		PROJECTION_MAP.put(Column._ID, Column._ID);
		PROJECTION_MAP.put(Column.TAG, Column.TAG);
		PROJECTION_MAP.put(Column.TITLE, Column.TITLE);
		PROJECTION_MAP.put(Column.SHORT_TITLE, Column.SHORT_TITLE);
		PROJECTION_MAP.put(Column.LINE_COLOR, Column.LINE_COLOR);
		PROJECTION_MAP.put(Column.TEXT_COLOR, Column.TEXT_COLOR);
	}
	
	public static void create(SQLiteDatabase db) {
		db.execSQL(String.format(
				"CREATE TABLE %s (%s %s, %s %s, %s %s, %s %s, %s %s, %s %s)",
				TABLE_NAME,
				Column._ID, "INTEGER PRIMARY KEY AUTOINCREMENT",
				Column.TAG, "TEXT UNIQUE",
				Column.TITLE, "TEXT",
				Column.SHORT_TITLE, "TEXT",
				Column.LINE_COLOR, "TEXT",
				Column.TEXT_COLOR, "TEXT"));
	}
	
	public static void drop(SQLiteDatabase db) {
		db.execSQL(String.format("DROP TABLE IF EXISTS %s", TABLE_NAME));
	}
}