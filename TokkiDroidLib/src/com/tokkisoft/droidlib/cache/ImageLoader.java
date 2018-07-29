package com.tokkisoft.droidlib.cache;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
//import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

// Crude implementation of image memory/disk cacher.
// This *must* be instantiated on the UI thread, due to use of a Handler.
// It's assumed you will only call ImageLoader.getBitmap on the UI thread.
// This class is NOT written to support multiple threads calling getBitmap. It was written this way because
// it is believed to be more important to show the first image as soon as possible, rather than have
// multiple threads eating up the phone's often limited bandwidth. Using a single thread
// also makes it far easier to debug and keep stable.
// You can optionally use external storage for cache (some phones have very limited app data storage space...
// The original HTC Incredible is a good example of a phone with very limited app data storage space).
// If you use external storage, you must include WRITE_EXTERNAL_STORAGE in the manifest: 
// <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
// To use external storage set USE_EXTERNAL_STORAGE to true in DiskCacheManager.java
public class ImageLoader {
	private static final String TAG = "ImageLoader";
	
	public static final boolean D = false;
	
	private static final int DEFAULT_CACHE_EXPIRES_MINUTES = 48 * 60; // 48 hours
	
	private static final int CONNECT_TIMEOUT_MILLIS = 30 * 1000; // 30 seconds
	private static final int READ_TIMEOUT_MILLIS = 120 * 1000; // 2 minutes
	
	private static final int BITMAP_MAX_WIDTH = 600; // max width in pixels
	private static final int BITMAP_MAX_HEIGHT = 600; // max height in pixels
	
	private static final int MAX_DOWNLOAD_FILE_SIZE = 500 * 1024; // 500KB max file size allowed for download
	
	private enum ImageLoaderStatus
	{
		Idle,
		Processing
	}
	
	private ImageLoaderStatus _loaderStatus;
	
	private synchronized ImageLoaderStatus getLoaderStatus()
	{
		return _loaderStatus;
	}
	
	private synchronized void setLoaderStatus(ImageLoaderStatus loaderStatus)
	{
		_loaderStatus = loaderStatus;
	}
	
	private final ConcurrentLinkedQueue<QueueItem> _queue;
	// access to the same QueueItems in the _queue, but via hashtable
	//private final ConcurrentHashMap<String, QueueItem> _queueHash;
	private final MemoryCacheManager _memoryCache;
	private final DiskCacheManager _diskCache;
	private final Calendar _calendar;
	
	private int _cacheExpiresInMinutes;
	private Thread _thread;
	private QueueRunner _runner;
	private final Handler _handler;
	private Context _context;
	
	private static ImageLoader _instance;
	
	public static ImageLoader getInstance(Context context)
	{
		if (_instance == null)
			_instance = new ImageLoader(context);
		return _instance;
	}
	
	private ImageLoader(Context context) {
		setCacheExpiresInMinutes(DEFAULT_CACHE_EXPIRES_MINUTES);

		_calendar = Calendar.getInstance();
		_queue = new ConcurrentLinkedQueue<ImageLoader.QueueItem>();
		//_queueHash = new ConcurrentHashMap<String, QueueItem>();
		_memoryCache = MemoryCacheManager.getInstance(context);
		setLoaderStatus(ImageLoaderStatus.Idle);
		_runner = new QueueRunner();
		_handler = new Handler();	// Assumes that this is started from the main (UI) thread
		_diskCache = DiskCacheManager.getInstance();
		_diskCache.deleteOldCache(context, _calendar, _cacheExpiresInMinutes);
	}
	
	public void setCacheExpiresInMinutes(int cacheExpiresInMinutes)
	{
		if (cacheExpiresInMinutes <= 0)
			_cacheExpiresInMinutes = DEFAULT_CACHE_EXPIRES_MINUTES;
		else
			_cacheExpiresInMinutes = cacheExpiresInMinutes;
	}
	
	public int getCacheExpiresInMinutes()
	{
		return _cacheExpiresInMinutes;
	}
	
	public interface ImageLoadedListener {
		public void onImageLoaded(Bitmap bitmap);
	}
	
	private enum QueueStatus
	{
		PendingProcessing,
		Processing,
		Done,
		Error
	}
	
	private class QueueItem {
		private String _url;
		//private Bitmap _bitmap;
		private QueueStatus _status;
		private boolean _bitmapExpired;
		private ArrayList<ImageLoadedListener> _listeners;
		
		public QueueItem(String url, ImageLoadedListener listener, boolean bitmapExpired)
		{
			_url = url;
			_status = QueueStatus.PendingProcessing;
			_bitmapExpired = bitmapExpired;
			_listeners = new ArrayList<ImageLoader.ImageLoadedListener>();
			_listeners.add(listener);
		}
		
		/*public synchronized QueueStatus getStatus()
		{
			return _status;
		}
		
		public synchronized void setStatus(QueueStatus status)
		{
			_status = status;
		}*/
		
		public synchronized boolean addListener(ImageLoadedListener listener)
		{
			boolean wasAddedToListeners = false;
			if (_status != QueueStatus.Done && _status != QueueStatus.Error) {
				_listeners.add(listener);
				wasAddedToListeners = true;
			}
			
			return wasAddedToListeners;
		}
		
		public synchronized boolean getBitmapExpired()
		{
			return _bitmapExpired;
		}
		
		public synchronized String getUrl()
		{
			return _url;
		}
		
		//public synchronized void setBitmap(Bitmap bitmap)
		//{
		//	_bitmap = bitmap;
		//}
		
		//public synchronized Bitmap getBitmap()
		//{
		//	return _bitmap;
		//}
		
		public synchronized void setStatus(QueueStatus status)
		{
			_status = status;
		}
		
		public synchronized void notifyListeners(Bitmap bitmap)
		{
			_status = QueueStatus.Done;
			
			if (bitmap != null)
			{
				for (ImageLoadedListener listener : _listeners) {
					listener.onImageLoaded(bitmap);
				}
			}
			_listeners.clear();
			
			// don't call bitmap.recycle here. we don't want to screw up the cache.
			// Simply null out the reference, since this object no longer needs it.
			//bitmap = null;
		}
		
		//private synchronized boolean safeToDereference()
		//{
		//	// only remove it if bitmap is done downloading or download error occurred
		//	if (_status == QueueStatus.Done || _status == QueueStatus.Error)
		//	{
		//		_listeners.clear();
		//		
		//		if (_bitmap != null)
		//		{
		//			//_bitmap.recycle();
		//			_bitmap = null;
		//		}
		//		
		//		return true;
		//	}
		//	return false;
		//}
	}
	
	// Listener will only be called if this returns null.
	public Bitmap getBitmap(Context context, String url, ImageLoadedListener listener)
	{
		_context = context;
		
		// try memory cache
		boolean bitmapExpired = false;
		final BitmapItem bitmapItem = _memoryCache.getBitmap(url);
		if (bitmapItem != null && bitmapItem.getBitmap() != null) {
			// check if bitmap too old based on created time
			if (bitmapItem.cacheExpired(_calendar, _cacheExpiresInMinutes))
			{
				if (ImageLoader.D) Log.i(TAG, "bitmapItem.cacheExpired");
				
				bitmapExpired = true;
				removeMemoryCache(url);
				removeDiskCache(context, url);
				//removeQueueItem(url);
			}
			else
			{
				if (ImageLoader.D) Log.i(TAG, "found in memory cache: " + url);
				return bitmapItem.getBitmap();	// listener won't be called
			}
		}
		
		// check if this URL is already in the queue. If so, add it's listener to the queueItem,
		// so it will be called back with the bitmap when it's ready
		boolean wasAddedToListeners = false;
		for (final QueueItem item : _queue) {
			if (item.getUrl().equals(url)) {
				wasAddedToListeners = item.addListener(listener);
				break;
			}
		}
		
		if (!wasAddedToListeners)
			queueItem(url, listener, bitmapExpired);
		
		/*
		// if this URL was already queued and is still processing, don't queue it up again,
		// but associate it with this request so it will be notified with the bitmap when
		// processing is completed.
		
		QueueItem item = _queueHash.get(url);
		if (item != null)
		{
			// This no longer does anything other than retrieve status.
			// I don't think it's necessary to add additional listeners.
			QueueStatus status = item.addListener(listener);
			
			if (status == QueueStatus.Done)
			{
				// since it's done, just return the bitmap (listener won't be called).
				return item.getBitmap();
			}
		}
		else
		{
			queueItem(url, listener);
		}
		*/
		
		return null;
	}
	
	private void removeMemoryCache(String url)
	{
		_memoryCache.removeBitmap(url);
	}
	
	private void removeDiskCache(Context context, String url)
	{
		_diskCache.removeBitmap(context, url);
	}
	
	//private void removeQueueItem(String url)
	//{
	//	QueueItem item = _queueHash.get(url);
	//	if (item != null)
	//	{
	//		// only remove it if bitmap is done downloading or download error occurred
	//		if (item.safeToDereference())
	//		{
	//			if (ImageLoader.D) Log.i(TAG, "Removed QueueItem: " + url);
	//			
	//			_queueHash.remove(url);
	//			item = null;
	//		}
	//	}
	//}
	
	private synchronized void queueItem(String url, ImageLoadedListener listener, boolean bitmapExpired)
	{
		QueueItem item = new QueueItem(url, listener, bitmapExpired);
		
		//_queueHash.put(url, item);
		_queue.add(item);
		
		if (getLoaderStatus() == ImageLoaderStatus.Idle)
		{
			setLoaderStatus(ImageLoaderStatus.Processing);
			_thread = new Thread(_runner);
			_thread.start();
		}
	}
	
	private class QueueRunner implements Runnable
	{
		public void run()
		{
			synchronized(this)
			{
				while(!_queue.isEmpty())
				{
					final QueueItem item = _queue.poll();
					if (_context == null)
						continue;
					String url = item.getUrl();
					
					// if bitmap hasn't expired in memory cache...
					if (!item.getBitmapExpired())
					{
						// ...try disk cache
						BitmapItem bitmapItem = _diskCache.getBitmap(_context, url, _calendar, _cacheExpiresInMinutes);
						if (bitmapItem != null && bitmapItem.getBitmap() != null) {
							if (ImageLoader.D) Log.i(TAG, "found in disk cache: " + url);
							
							// Save reference in memory cache, so we don't hit the disk every time.
							// Uses LruCache object internally, which keeps the memory cache size to a limit for us.
							_memoryCache.putBitmap(url, bitmapItem);
							
							// notify listeners on the UI thread
							final Bitmap diskBitmap = bitmapItem.getBitmap();
							_handler.post(new Runnable() {
								public void run() {
									item.notifyListeners(diskBitmap);
								}
							});
							continue;
						}
					}
					
					URL urlObj = null;
					try {
						urlObj = new URL(url);
					} catch (MalformedURLException e) {
						if (ImageLoader.D) Log.e(TAG, "Bad image URL: " + url, e);
						item.setStatus(QueueStatus.Error);
						continue;
					}
					BitmapAndType bmp = readBitmapFromNetwork(urlObj);
					if (bmp != null && bmp.bitmap != null)
					{
						// scale the image down in size, so it doesn't take up unnecessary space
						final Bitmap scaledBitmap = scaleImage(bmp.bitmap);
						
						bmp.bitmap = null;
						
						// save in memory cache
						final BitmapItem bitmapItem = new BitmapItem(scaledBitmap, bmp.mimeType);
						_memoryCache.putBitmap(url, bitmapItem);

						// save in disk cache
						_diskCache.putBitmap(_context, url, bitmapItem);
						
						//item.setBitmap(scaledBitmap);
						
						if (ImageLoader.D) Log.i(TAG, "image loaded from net: " + url);
						
						// notify listeners on the UI thread
						_handler.post(new Runnable() {
							public void run() {
								item.notifyListeners(scaledBitmap);
							}
						});
					}
					else
					{
						// error occurred. In this case, listener won't be called, which should be fine.
						item.setStatus(QueueStatus.Error);
					}
				}
				
				setLoaderStatus(ImageLoaderStatus.Idle);
			}
		}
	}
	
	// http://stackoverflow.com/questions/6604362/vm-running-out-of-memory-while-getting-images-from-the-cache
	private Bitmap scaleImage(Bitmap bmp) {
		Bitmap returnBmp = bmp;
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		if (bmpWidth > BITMAP_MAX_WIDTH || bmpHeight > BITMAP_MAX_HEIGHT) {
			int width, height;
			if (bmpWidth > BITMAP_MAX_WIDTH) {
				height = bmpHeight * BITMAP_MAX_WIDTH / bmpWidth;
				width = BITMAP_MAX_WIDTH;
			}
			else { // bmpHeight > BITMAP_MAX_HEIGHT
				width = bmpWidth * BITMAP_MAX_HEIGHT / bmpHeight;
				height = BITMAP_MAX_HEIGHT;
			}
			
			if (width > BITMAP_MAX_WIDTH || height > BITMAP_MAX_HEIGHT) {
				if (width > BITMAP_MAX_WIDTH) {
					height = height * BITMAP_MAX_WIDTH / bmpWidth;
					width = BITMAP_MAX_WIDTH;
				}
				else { // height > BITMAP_MAX_HEIGHT
					width = width * BITMAP_MAX_HEIGHT / bmpHeight;
					height = BITMAP_MAX_HEIGHT;
				}
			}
			
			//if (ImageLoader.D) Log.i(TAG, String.format("scaleImage width:%d height:%d", width, height));
			
			returnBmp = Bitmap.createScaledBitmap(returnBmp, width, height, true);
		}
		
		return returnBmp;
	}
	
	private static BitmapAndType readBitmapFromNetwork(URL url) {
		InputStream is = null;
		//BufferedInputStream bis = null;
		final BitmapAndType bmp = new BitmapAndType();
		try {
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
			conn.setReadTimeout(READ_TIMEOUT_MILLIS);
			conn.setUseCaches(false); // just in case future version of framework ever makes use of caching, we want to make sure it doesn't get enabled here, even though this has no affect now.
			//conn.setRequestProperty("Pragma", "no-cache");
			//conn.setRequestProperty("Cache-Control", "no-cache");
			conn.connect();
			
			// try to get file size from server response. If the file is too big, then don't bother to download it.
			int fileSize = conn.getContentLength();
			if (fileSize == -1) {
				if (ImageLoader.D) Log.w(TAG, "Content-length not set on server side. Unable to determine image size!");
			}
			else if (fileSize > MAX_DOWNLOAD_FILE_SIZE) {
				// file is huge. don't download it.
				if (ImageLoader.D) Log.w(TAG, String.format("image too big (%d) %s", fileSize, url.toString()));
				return null;
			}
			
			if (ImageLoader.D) Log.i(TAG, String.format("image download size: %d: %s", fileSize, url.toString()));
			
			is = conn.getInputStream();
			//bis = new BufferedInputStream(is);
			//bmp = BitmapFactory.decodeStream(bis);	// very bad! can cause out of memory errors very fast!
			
			// Avoid out of memory issues by loading scaled down versions of images.
			// First, get bitmap dimensions and calculate inSampleSize (also gets mime-type)
			BitmapFactory.Options options = getBitmapSize(is);
			if (options == null) {
				conn.disconnect();
				return null;
			}
			bmp.mimeType = options.outMimeType;
			if (ImageLoader.D) Log.i(TAG, String.format("image mime type: '%s'", bmp.mimeType));
			conn.disconnect();
			// have to "reopen" the connection to "reset" the stream (should be quick, due to keep-alive connection pool usage under the hood).
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
			conn.setReadTimeout(READ_TIMEOUT_MILLIS);
			conn.setUseCaches(false); // just in case future version of framework ever makes use of caching, we want to make sure it doesn't get enabled here, even though this has no affect now.
			//conn.setRequestProperty("Pragma", "no-cache");
			//conn.setRequestProperty("Cache-Control", "no-cache");
			conn.connect();
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException e) {
				}
			}
			is = conn.getInputStream();
			bmp.bitmap = BitmapFactory.decodeStream(is, null, options);
		} catch (MalformedURLException e) {
			if (ImageLoader.D) Log.e(TAG, "Bad image URL", e);
			return null;
		} catch (SocketTimeoutException e) {
			if (ImageLoader.D) Log.e(TAG, "Attempt to get remote image timed out", e);
			return null;
		} catch (IOException e) {
			if (ImageLoader.D) Log.e(TAG, "Could not get remote ad image", e);
			return null;
		} catch (Exception e) {
			if (ImageLoader.D) Log.e(TAG, e.getMessage());
			return null;
		} finally {
			try {
				//if( bis != null )
				//	bis.close();
				if( is != null )
					is.close();
			} catch (IOException e) {
				if (ImageLoader.D) Log.w(TAG, "Error closing stream.");
			}
		}
		return bmp;
	}
	
	// Note: This advances the input stream, so you can't use the same input stream to download the image.
	// We call this to get image size and calculate inSampleSize, then create a new UrlConnection to download the image using the returned options.
	// Using another connection is ok, because abstracted away under the hood is a pool of connections. That is, HTTPUrlConnection is "keep-alive" by default.
	// When you create a new HTTPUrlConnection it will re-use a connection if it is available in the pool and hasn't timed out.
	private static BitmapFactory.Options getBitmapSize(InputStream is) {
		//BufferedInputStream bis = null;
		BitmapFactory.Options options = null;
		try {
			options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			//bis = new BufferedInputStream(is);
			BitmapFactory.decodeStream(is, null, options);
			if (options.outMimeType == null)
				return null;
			options.inSampleSize = calculateInSampleSize(options, BITMAP_MAX_WIDTH, BITMAP_MAX_HEIGHT);
			options.inJustDecodeBounds = false;
			return options;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
//		finally {
//			if (bis != null) {
//				
//				try {
//					bis.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
	}
	
	// Avoid out of memory issues by loading scaled down versions of images.
	// http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue-while-loading-an-image-to-a-bitmap-object
	// http://developer.android.com/training/displaying-bitmaps/index.html
	public static BitmapAndType decodeBitmapFromFilePath(String filePath) {
		FileInputStream fis = null;
		BitmapAndType bmp = new BitmapAndType();
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			fis = new FileInputStream(filePath);
			BitmapFactory.decodeStream(fis, null, options); // fills opts with image dimensions and mime type
			bmp.mimeType = options.outMimeType;
			options.inSampleSize = calculateInSampleSize(options, BITMAP_MAX_WIDTH, BITMAP_MAX_HEIGHT);
			options.inJustDecodeBounds = false;
			fis = new FileInputStream(filePath);
			bmp.bitmap = BitmapFactory.decodeStream(fis, null, options);
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        return bmp;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    
	    if (height > reqHeight || width > reqWidth) {
	    	//inSampleSize = (int)Math.pow(2, (int)Math.round(Math.log(Math.max(reqWidth, reqHeight) / (double)Math.max(height, width)) / Math.log(0.5)));
	    	
	    	if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    if (ImageLoader.D) Log.i(TAG, String.format("inSampleSize: %d", inSampleSize));
	    return inSampleSize;
	}
}
