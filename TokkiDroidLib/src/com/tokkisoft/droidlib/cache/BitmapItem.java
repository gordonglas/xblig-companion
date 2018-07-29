package com.tokkisoft.droidlib.cache;

import java.util.Calendar;
import java.util.Date;

import android.graphics.Bitmap;

public class BitmapItem {
	private Bitmap _bitmap;
	private long _created;
	private String _mimeType;
	
	public Bitmap getBitmap()
	{
		return _bitmap;
	}
	
	public long getCreated() {
		return _created;
	}
	
	public String getMimeType() {
		return _mimeType;
	}
	
	public BitmapItem(Bitmap bitmap, String mimeType)
	{
		_bitmap = bitmap;
		_created = new Date().getTime();
		_mimeType = mimeType;
	}
	
	public boolean cacheExpired(Calendar calendar, int cacheExpiredInMinutes)
	{
		Date createdDate = new Date(_created);
		calendar.setTime(createdDate);
		calendar.add(Calendar.MINUTE, cacheExpiredInMinutes);
		createdDate = calendar.getTime();
		
		Date now = new Date();
		
		if (createdDate.before(now))
			return true;
		return false;
	}
	
	public void dereference()
	{
		if (_bitmap != null)
		{
			//_bitmap.recycle();
			_bitmap = null;
		}
	}
}
