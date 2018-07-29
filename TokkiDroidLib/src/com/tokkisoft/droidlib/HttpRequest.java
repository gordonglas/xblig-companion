package com.tokkisoft.droidlib;

//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;

import com.tokkisoft.droidlib.IErrorCallback.ErrorType;

import android.content.Context;

//import android.os.Build;

public class HttpRequest {
	private final int CONNECT_TIMEOUT_MILLIS = 1000 * 30;	// 30 seconds
	private final int READ_TIMEOUT_MILLIS = 1000 * 30;		// 30 seconds
	//private final long HTTP_RESPONSE_CACHE_SIZE = 3 * 1024 * 1024; // 3 MB
	
	//private File _cacheDir;
	//private boolean calledDisableConnectionReuse;
	//private boolean calledEnableResponseCache;
	
	private Context context;
	private IErrorCallback errorCallback;
	
	public HttpRequest(Context context, IErrorCallback errorCallback /*, File cacheDir*/)
	{
		//_cacheDir = cacheDir;
		//init();
		
		this.context = context;
		this.errorCallback = errorCallback;
	}
	
	//private synchronized void init() {
	//	if (!calledDisableConnectionReuse) {
	//		disableConnectionReuseIfNecessary();
	//		calledDisableConnectionReuse = true;
	//	}
		
	//	if (!calledEnableResponseCache) {
	//		enableHttpResponseCache();
	//		calledEnableResponseCache = true;
	//	}
	//}
	
	// HTTP connection reuse was buggy pre-froyo
	// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	//private void disableConnectionReuseIfNecessary() {
	//    if (Integer.parseInt(Build.VERSION.SDK) <= Build.VERSION_CODES.ECLAIR_MR1) {
	//        System.setProperty("http.keepAlive", "false");
	//    }
	//}
	
	// use reflection to enable HTTP response caching if available (in ICS)
	// http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	//private void enableHttpResponseCache() {
	//    try {
	//        long httpCacheSize = HTTP_RESPONSE_CACHE_SIZE;
	//        File httpCacheDir = new File(_cacheDir, "http");
	//        Class.forName("android.net.http.HttpResponseCache")
	//            .getMethod("install", File.class, long.class)
	//            .invoke(null, httpCacheDir, httpCacheSize);
	//    } catch (Exception httpResponseCacheNotAvailable) {
	//    }
	//}
	
	public String httpGet(String url, int cacheExpiresInMinutes)
	{
		HttpResponseCacheDataSource cache = null;
		
		if (cacheExpiresInMinutes != 0) {
			SQLiteResponse r = null;
			cache = HttpResponseCacheDataSource.getInstance(context);
	        //cache.Open();
	        try {
				r = cache.getResponse(url, cacheExpiresInMinutes);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        if (r != null) {
	        	cache.close();
	        	return r.getResponse();
	        }
		}

		String sResponse = null;
	    InputStream in = null;
	    try {
	        URL u = new URL(url);
	        HttpURLConnection httpConnection = (HttpURLConnection)u.openConnection();
	        httpConnection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
	        httpConnection.setReadTimeout(READ_TIMEOUT_MILLIS);
	        //if (DroidLib.D) Log.i("TIMEOUTS", "Connect timeout: " + httpConnection.getConnectTimeout());
	        //if (DroidLib.D) Log.i("TIMEOUTS", "Read timeout: " + httpConnection.getReadTimeout());
	        httpConnection.setUseCaches(false); // just in case future version of framework ever makes use of caching, we want to make sure it doesn't get enabled here, even though this has no affect now.
	        int responseCode = httpConnection.getResponseCode();
	        if (responseCode == HttpURLConnection.HTTP_OK) {
		        in = httpConnection.getInputStream();
		        sResponse = Utils.convertStreamToString(in);
	        }
	    }
	    catch (MalformedURLException e) {
	    	e.printStackTrace();
	    }
	    catch (SocketTimeoutException e) {
	    	e.printStackTrace();
	    	if (errorCallback != null)
	    		errorCallback.onError(ErrorType.SOCKET_TIMEOUT_EXCEPTION, e);
	    }
	    catch (IOException e) {
	    	e.printStackTrace();
	    	if (errorCallback != null)
	    		errorCallback.onError(ErrorType.IO_EXCEPTION, e);
	    }
	    finally {
	    	if (in != null) {
	    		try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    }
	    
	    if (cacheExpiresInMinutes != 0) {
	    	try {
				cache.setResponse(url, sResponse);
			} catch (Exception e) {
				e.printStackTrace();
			}
	    
	    	cache.close();
	    }
	    
	    return sResponse;
	}
}
