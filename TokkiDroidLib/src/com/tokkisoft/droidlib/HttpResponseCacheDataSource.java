package com.tokkisoft.droidlib;

import java.text.ParseException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class HttpResponseCacheDataSource {
	private static HttpResponseCacheDataSource instance;
	private static int cacheExpiresInMinutes = 5;
	private final long maxResponseCacheSizeBytes = 3 * 1024 * 1024; // 3 MB
	
	//private SQLiteDatabase database;
	private SQLiteHttpResponseCacheDbHelper dbHelper;
	
	private String[] allColumns = {
		SQLiteHttpResponseCacheDbHelper.COLUMN_ID,
		SQLiteHttpResponseCacheDbHelper.COLUMN_URL,
		SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSE,
		SQLiteHttpResponseCacheDbHelper.COLUMN_CREATED
		//,SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSELEN
	};

	public static synchronized HttpResponseCacheDataSource getInstance(Context context) {
		if (instance == null)
			instance = new HttpResponseCacheDataSource(context);
		
		return instance;
	}
	
	public HttpResponseCacheDataSource(Context context) {
		dbHelper = SQLiteHttpResponseCacheDbHelper.getInstance(context);
	}
	
	//public void Open() throws SQLiteException {
	//	database = dbHelper.getWritableDatabase();
	//}
	
	public void close() {
		dbHelper.close();
	}
	
	// http://www.vogella.com/articles/AndroidSQLite/article.html
	
	// Checks to see if URL already exists in the responses table.
	// If record already exists, checks date to see if it's still fresh enough to use, and if so, returns it.
	// Otherwise returns null.
	public SQLiteResponse getResponse(String url, int _cacheExpiresInMinutes) throws ParseException {
		
		if (_cacheExpiresInMinutes == 0)
			return null;
		
		SQLiteResponse response = null;
		
		Cursor cursor = dbHelper.getWritableDatabase().query(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES,
				allColumns, SQLiteHttpResponseCacheDbHelper.COLUMN_URL + " = '" + sqlEncode(url) + "'", null,
				null, null, null);
		
		if (cursor.moveToFirst()) {
			response = cursorToResponse(cursor);
			
			int cacheExpiresMinutes;
			if (_cacheExpiresInMinutes < 0)
				cacheExpiresMinutes = cacheExpiresInMinutes;
			else
				cacheExpiresMinutes = _cacheExpiresInMinutes;
			
			if (!response.isCacheFresh(cacheExpiresMinutes))
				response = null;
		}
		cursor.close();
		return response;
	}
	
	public SQLiteResponse setResponse(String url, String response) throws Exception {
		
		trimDatabase();
		
		SQLiteResponse sqliteResponse = new SQLiteResponse();
		sqliteResponse.setUrl(url);
		sqliteResponse.setResponse(response);
		sqliteResponse.setCreated();
		
		Cursor cursor = dbHelper.getWritableDatabase().query(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES,
				allColumns, SQLiteHttpResponseCacheDbHelper.COLUMN_URL + " = '" + sqlEncode(url) + "'", null,
				null, null, null);
		
		long responseId = 0;
		if (cursor.moveToFirst())
			responseId = cursor.getInt(0);
		cursor.close();
		
		if (responseId == 0) {
			// insert
			
			ContentValues values = new ContentValues();
			values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_URL, sqliteResponse.getUrl());
			values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSE, sqliteResponse.getResponse());
			values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_CREATED, sqliteResponse.getCreatedGmt());
			values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSELEN, sqliteResponse.getResponse().length());
			
			responseId = dbHelper.getWritableDatabase().insert(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES, null, values);
			if (responseId == -1) {
				throw new Exception("sqlite insert failed");
			}
			
			sqliteResponse.setId(responseId);
		}
		else {
			// update
			
			ContentValues values = new ContentValues();
			values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSE, sqliteResponse.getResponse());
			values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_CREATED, sqliteResponse.getCreatedGmt());
			values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSELEN, sqliteResponse.getResponse().length());
			
			int rowsAffected = dbHelper.getWritableDatabase().update(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES, values,
					SQLiteHttpResponseCacheDbHelper.COLUMN_URL + " = '" + sqlEncode(url) + "'", null);
			if (rowsAffected != 1) {
				throw new Exception("sqlite update failed: rowsAffected: " + rowsAffected);
			}
			sqliteResponse.setId(responseId);
		}
		
		return sqliteResponse;
	}
	
	private void trimDatabase() {
		// check to see if database content size is roughly within limit
		long size = getDatabaseSize();
		
		if (size >= maxResponseCacheSizeBytes) {
			// first try deleting only expired cache entries
			//deleteExpiredCache();
			
			//size = getDatabaseSize();
			
			// if db is still too big, wipe all cache
			//if (size >= maxResponseCacheSizeBytes) {
				deleteAllCache();
			//}
		}
	}

	public void deleteAllCache() {
		dbHelper.getWritableDatabase().delete(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES, null, null);
	}

	//private void deleteExpiredCache() {
	//	String now = "";
	//	database.delete(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES,
	//			SQLiteHttpResponseCacheDbHelper.COLUMN_CREATED + "  ", null);
	//}

	private long getDatabaseSize() {
		String sizeCheckSQL = "select sum(" + SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSELEN + ") from " + SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES;
		Cursor cursor = dbHelper.getWritableDatabase().rawQuery(sizeCheckSQL, null);
		long size = 0;
		if (cursor.moveToFirst())
			size = cursor.getInt(0);
		cursor.close();
		return size;
	}

	//public SQLiteResponse setResponse(String url, String response) throws ParseException {
	//	ContentValues values = new ContentValues();
	//	values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_URL, url);
	//	values.put(SQLiteHttpResponseCacheDbHelper.COLUMN_RESPONSE, response);
	//	long insertId = database.insert(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES, null, values);
	//	Cursor cursor = database.query(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES,
	//			allColumns, SQLiteHttpResponseCacheDbHelper.COLUMN_ID + " = " + insertId, null,
	//			null, null, null);
	//	cursor.moveToFirst();
	//	SQLiteResponse newResponse = cursorToResponse(cursor);
	//	cursor.close();
	//	return newResponse;
	//}
	
	public void deleteResponse(String url) {
		dbHelper.getWritableDatabase().delete(SQLiteHttpResponseCacheDbHelper.TABLE_RESPONSES,
				SQLiteHttpResponseCacheDbHelper.COLUMN_URL + " = '" + sqlEncode(url) + "'", null);
	}
	
	private SQLiteResponse cursorToResponse(Cursor cursor) throws ParseException {
		SQLiteResponse response = new SQLiteResponse();
		response.setId(cursor.getLong(0));
		response.setUrl(cursor.getString(1));
		response.setResponse(cursor.getString(2));
		response.setCreated(cursor.getString(3));
		return response;
	}
	
	private String sqlEncode(String str) {
		return str.replaceAll("'", "''");
	}
}
