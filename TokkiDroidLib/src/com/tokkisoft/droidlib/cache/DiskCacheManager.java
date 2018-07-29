package com.tokkisoft.droidlib.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class DiskCacheManager {
	private static final String TAG = "DiskCacheManager";
	
	private static final int DEFAULT_MAX_DISK_CACHE_SIZE = 2 * 1024 * 1024; // 2 MB
	private static final int JPEG_QUALITY = 50;
	private static final int MAX_FILES_DELETE_ON_CACHE_TRIM = 40;
	// if you set USE_EXTERNAL_STORAGE to true, you must include WRITE_EXTERNAL_STORAGE in the manifest: 
	// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
	private static final boolean USE_EXTERNAL_STORAGE = false;
	
	private static DiskCacheManager _instance;
	
	public static DiskCacheManager getInstance()
	{
		if (_instance == null)
			_instance = new DiskCacheManager();
		return _instance;
	}
	
	public enum MimeType
	{
		MimeTypeUnknown,
		MimeTypeJpeg,
		MimeTypePng,
		MimeTypeGif,
		MimeTypeBmp
	}
	
	private ArrayList<SortableFile> _sortableFileList;
	private boolean _ready;
	
	public synchronized void setReady(boolean ready)
	{
		_ready = ready;
	}
	
	public synchronized boolean isReady()
	{
		return _ready;
	}
	
	private DiskCacheManager()
	{
		_sortableFileList = new ArrayList<DiskCacheManager.SortableFile>();
		setReady(true);
	}
	
	public BitmapItem getBitmap(Context context, String url, Calendar calendar, int cacheExpiresInMinutes)
	{
		if (!isReady())
			return null;
		
		final String file = getFilename(url);
		
		final File dir = getCacheDir(context);
		
		final File filePath = new File(dir, file);
		if (!filePath.exists())
			return null;
		
		// check if file is too old...
		Date lastModDate = new Date(filePath.lastModified());
		calendar.setTime(lastModDate);
		calendar.add(Calendar.MINUTE, cacheExpiresInMinutes);
		lastModDate = calendar.getTime();
		final Date now = new Date();
		if (lastModDate.before(now))
		{
			// file is too old, so delete it
			removeBitmap(context, url);
			return null;
		}
		
		// It's possible that this will fail with error similar to:
		//   1124000-byte external allocation too large for this process.
		//   VM won't let us allocate 1124000 bytes
		// The real cause was because was hanging on to references to Bitmap objects, so they couldn't be garbage collected (It's fixed).
		// But just to be safe and to save space anyway, within decodeBitmapFromFilePath, we shrink the bitmap before storing it in both disk and memory cache.
		// http://stackoverflow.com/questions/6604362/vm-running-out-of-memory-while-getting-images-from-the-cache
		BitmapAndType bitmap = null;
		try {
			//bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
			
			/*
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(filePath.getAbsolutePath());
				bitmap = ImageLoader.decodeBitmapFromStream(fis);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (fis != null)
						fis.close();
				} catch (IOException e) {
					if (ImageLoader.D) Log.w(TAG, "Error closing stream.");
				}
			}
			*/
			
			bitmap = ImageLoader.decodeBitmapFromFilePath(filePath.getAbsolutePath());
			
			if (bitmap == null || bitmap.bitmap == null)
			{
				if (ImageLoader.D) Log.e(TAG, "Unable to decode bitmap at: " + filePath.getAbsolutePath());
				return null;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		BitmapItem item = new BitmapItem(bitmap.bitmap, bitmap.mimeType);
		
		return item;
	}
	
	// Uses a thread because otherwise it might run for a long time and cause an ANR dialog.
	// While it's deleting files, doesn't allow disk cache to be accessed (all insert/read requests are ignored).
	public void deleteOldCache(Context context, Calendar calendar, int cacheExpiresInMinutes)
	{
		setReady(false);
		try
		{
			DeleteOldDiskCacheRunner runner = new DeleteOldDiskCacheRunner(context, calendar, cacheExpiresInMinutes);
			Thread thread = new Thread(runner);
			thread.start();
		}
		catch (Exception ex) {
			setReady(true);
		}
	}
	
	private class DeleteOldDiskCacheRunner implements Runnable
	{
		private Context context;
		private Calendar calendar;
		private int cacheExpiresInMinutes;
		
		public DeleteOldDiskCacheRunner(Context context, Calendar calendar, int cacheExpiresInMinutes) {
			this.context = context;
			this.calendar = calendar;
			this.cacheExpiresInMinutes = cacheExpiresInMinutes;
		}
		
		@Override
		public void run() {
			synchronized (this) {
				File dir = getCacheDir(context);
				File[] files = dir.listFiles();
				Date lastModDate = new Date();
				Date now = new Date();
				for(int i = 0; i < files.length; i++)
				{
					File file = files[i];
					// ignore sub folders
					if (!file.isDirectory())
					{
						lastModDate.setTime(file.lastModified());
						calendar.setTime(lastModDate);
						calendar.add(Calendar.MINUTE, cacheExpiresInMinutes);
						lastModDate = calendar.getTime();
						if (lastModDate.before(now))
						{
							// file is too old, so delete it
							file.delete();
						}
					}
				}
				setReady(true);
			}
		}
		
	}
	
	private File getCacheDir(Context context) {
		File cacheDir = null;
		if (USE_EXTERNAL_STORAGE)
		{
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			{
				if (ImageLoader.D) Log.i(TAG, "External storage mounted");
				cacheDir = getExternalCacheDir(context);
				if (ImageLoader.D) Log.i(TAG, "getExternalCacheDir returned: " + cacheDir.getAbsolutePath());
			}
			else
			{
				if (ImageLoader.D) Log.i(TAG, "External storage not mounted or read-only");
			}
		}
		
		if (cacheDir == null)
		{
			cacheDir = context.getCacheDir();
			if (ImageLoader.D) Log.i(TAG, "Using internal storage for cache: " + cacheDir.getAbsolutePath());
		}

	    return cacheDir;
	}

	private File getExternalCacheDir(Context context) {
	    // return context.getExternalCacheDir(); API level 8

	    // e.g. "<sdcard>/Android/data/<package_name>/cache/"
	    final File extCacheDir = new File(Environment.getExternalStorageDirectory(),
	        "/Android/data/" + context.getApplicationInfo().packageName + "/cache/");
	    extCacheDir.mkdirs();
	    return extCacheDir;
	}
	
	public static MimeType getMimeTypeEnum(String mimeType)
	{
		String mt = mimeType.toLowerCase();
		MimeType type = MimeType.MimeTypeUnknown;
		if (mt.equals("image/jpeg"))
			type = MimeType.MimeTypeJpeg;
		else if (mt.equals("image/png"))
			type = MimeType.MimeTypePng;
		else if (mt.equals("image/gif"))
			type = MimeType.MimeTypeGif;
		else if (mt.equals("image/bmp") || mt.equals("image/x-bmp") || mt.equals("application/bmp"))  // not sure if necessary
			type = MimeType.MimeTypeBmp;
		return type;
	}
	
	public void putBitmap(Context context, String url, BitmapItem bitmapItem)
	{
		if (!isReady())
			return;
		
		final File dir = getCacheDir(context);
		final File file = new File(dir, getFilename(url));
		//String file = context.getCacheDir() + File.separator + getFilename(url);
		
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file), 8 * 1024);
			
			final MimeType mimeType = getMimeTypeEnum(bitmapItem.getMimeType());
			// sorry.. doesn't support animated gifs...  T__T
			// if you want to add support, see http://stackoverflow.com/questions/3660209/android-display-animated-gif
			// for now just treat gif as jpeg.
			if (mimeType == MimeType.MimeTypeJpeg || mimeType == MimeType.MimeTypeGif || mimeType == MimeType.MimeTypeBmp || mimeType == MimeType.MimeTypeUnknown)
				bitmapItem.getBitmap().compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
			else if (mimeType == MimeType.MimeTypePng) {
				// quality is ignored when using PNG
				// http://stackoverflow.com/questions/649154/android-bitmap-save-to-location
				bitmapItem.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);
			}
		}
		catch (FileNotFoundException e) {
			if (ImageLoader.D) Log.e(TAG, "Unable to create file output stream: " + file.getAbsolutePath());
			return;
		}
		finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		trimDiskCacheToMaxSize(dir);
	}
	
	private class SortableFile implements Comparable<SortableFile>
	{
		public int _arrayIndex;
		public long _lastModified;
		public long _size;
		
		public SortableFile(int arrayIndex, long lastModified, long size)
		{
			_arrayIndex = arrayIndex;
			_lastModified = lastModified;
			_size = size;
		}
		
		@Override
		public int compareTo(SortableFile file) {
			if (this._lastModified < file._lastModified)
				return -1;
			else if (this._lastModified > file._lastModified)
				return 1;
			return 0;
		}
	}
	
	private void trimDiskCacheToMaxSize(File cacheDir)
	{
		if (!isReady())
			return;
		
		long size = 0;
		
		_sortableFileList.clear();
		
		final File[] files = cacheDir.listFiles();
		long fileSize;
		int i;
		for(i = 0; i < files.length; i++)
		{
			final File file = files[i];
			// ignore sub folders
			if (!file.isDirectory())
			{
				fileSize = file.length();
				_sortableFileList.add(new SortableFile(i, file.lastModified(), fileSize));
				size += fileSize;
			}
		}
		
		if (ImageLoader.D) Log.i(TAG, "Disk cache total size: " + size);

		// if cache size > max, delete half of oldest files
		if (size > DEFAULT_MAX_DISK_CACHE_SIZE)
		{
			if (ImageLoader.D) Log.i(TAG, "Trimming disk cache...");
			
			// sort by last modified date
			Collections.sort(_sortableFileList);
			
			long halfSize = size / 2;
			i = 0;
			while (size > halfSize && i < files.length && i < MAX_FILES_DELETE_ON_CACHE_TRIM)
			{
				SortableFile sf = _sortableFileList.get(i);
				files[sf._arrayIndex].delete();
				size -= sf._size;
				i++;
			}
			
			if (ImageLoader.D) Log.i(TAG, "Trimming disk cache completed");
		}
	}
	
	public void removeBitmap(Context context, String url)
	{
		final File file = new File(context.getCacheDir(), getFilename(url));
		
		if (file.exists())
		{
			if (!file.delete())
				if (ImageLoader.D) Log.e(TAG, "Failed to delete file: " + file.getAbsolutePath());
			//else
			//	if (ImageLoader.D) Log.i(TAG, "File deleted: " + file.getAbsolutePath());
		}
	}
	
	public String getFilename(String url)
	{
		URL urlObj = null;
		try {
			urlObj = new URL(url);
		} catch (MalformedURLException e) {
			if (ImageLoader.D) Log.e(TAG, "Bad image URL: " + url, e);
			return null;
		}
		
		String thePath = urlObj.getPath();
		//if (ImageLoader.D) Log.i(TAG, "thePath: " + thePath);
		String regex = "[[^a-z]&&[^A-Z]&&[^0-9]&&[^\\.\\-_]]";
		String file = thePath.replaceAll(regex, "_");
		return file;
	}
}
