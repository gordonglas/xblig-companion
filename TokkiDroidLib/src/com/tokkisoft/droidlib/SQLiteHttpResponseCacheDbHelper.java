package com.tokkisoft.droidlib;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHttpResponseCacheDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "httpResponseCache.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_RESPONSES = "responses";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_URL = "url";
	public static final String COLUMN_RESPONSE = "response";
	public static final String COLUMN_CREATED = "created";
	public static final String COLUMN_RESPONSELEN = "responselen";
	
	private static final String DATABASE_CREATE = "create table " + TABLE_RESPONSES + "( " +
			COLUMN_ID + " integer primary key autoincrement, " +
			COLUMN_URL + " text not null, " +
			COLUMN_RESPONSE + " text not null, " +
			COLUMN_CREATED + " text not null, " +
			COLUMN_RESPONSELEN + " integer not null);";
	
	private static SQLiteHttpResponseCacheDbHelper instance;
	
	public static synchronized SQLiteHttpResponseCacheDbHelper getInstance(Context context) {
		if (instance == null)
			instance = new SQLiteHttpResponseCacheDbHelper(context);
		
		return instance;
	}
	
	public SQLiteHttpResponseCacheDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (DroidLib.D) Log.w(SQLiteHttpResponseCacheDbHelper.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESPONSES);
		onCreate(db);
	}
	
}
