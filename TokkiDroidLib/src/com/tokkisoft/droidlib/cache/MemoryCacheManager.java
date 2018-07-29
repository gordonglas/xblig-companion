package com.tokkisoft.droidlib.cache;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

// http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
// http://developer.android.com/reference/android/util/LruCache.html
public class MemoryCacheManager {
	private static final String TAG = "MemoryCacheManager";
	
	private static final int DEFAULT_MAX_MEMORY_CACHE_SIZE = 3 * 1024 * 1024; // 3 MB
	
	private static MemoryCacheManager _instance;
	
	public static MemoryCacheManager getInstance(Context context)
	{
		if (_instance == null)
			_instance = new MemoryCacheManager(context);
		return _instance;
	}
	
	private LruCache<String, BitmapItem> _cache;
	
	private MemoryCacheManager(Context context)
	{
		// Get available memory of this device, exceeding this amount will throw an OutOfMemory exception.
		MemoryInfo memInfo = new MemoryInfo();
	    ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryInfo(memInfo);
	    long availableDeviceMemory = memInfo.availMem;
	    
	    if (ImageLoader.D) Log.i(TAG, "availableDeviceMemory: " + availableDeviceMemory);
	    
	    // Use 1/8th of the available memory for this memory cache, capped by max ram setting
	    int cacheSize = (int) (availableDeviceMemory / 8);
	    if (cacheSize > DEFAULT_MAX_MEMORY_CACHE_SIZE)
	    	cacheSize = DEFAULT_MAX_MEMORY_CACHE_SIZE;
	    
	    if (ImageLoader.D) Log.i(TAG, "cacheSize: " + cacheSize);
	    
	    _cache = new LruCache<String, BitmapItem>(cacheSize) {
	    	@Override
	        protected int sizeOf(String key, BitmapItem bitmapItem) {
	            // The cache size will be measured in bytes rather than number of items.
	    		if (bitmapItem == null)
	    			return 0;
	    		final Bitmap bitmap = bitmapItem.getBitmap();
	    		if (bitmap == null)
	    			return 0;
	    		int bitmapSize = bitmap.getRowBytes() * bitmap.getHeight();
	    		//if (ImageLoader.D) Log.i(TAG, "bitmap size: " + bitmapSize);
	    		return bitmapSize;
	        }
	    };
	}
	
	public void putBitmap(String key, BitmapItem bitmapItem)
	{
		synchronized(_cache) {
			if (_cache.get(key) == null) {
				_cache.put(key, bitmapItem);
			}
		}
	}
	
	public BitmapItem getBitmap(String key)
	{
		synchronized(_cache) {
			return _cache.get(key);
		}
	}
	
	public void removeBitmap(String key)
	{
		synchronized(_cache) {
			final BitmapItem bitmapItem = _cache.remove(key);
			if (bitmapItem != null) {
				bitmapItem.dereference();
			}
		}
	}
}
