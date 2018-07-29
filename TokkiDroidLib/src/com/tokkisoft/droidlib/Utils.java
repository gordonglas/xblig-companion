package com.tokkisoft.droidlib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import android.content.res.Resources;
import android.util.Log;

public class Utils {

	public static String makeFragmentName(int containerId, int position) {
	    return "android:switcher:" + containerId + ":" + position;
	}
	
	public static String convertStreamToString(InputStream is) {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			char[] buffer = new char[8192];
			int len;
			
			while ((len = br.read(buffer)) != -1) {
			  sb.append(buffer, 0, len);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	public static Date getDateFromDotNetJsonDate(String dotNetJsonDate) {
		// Note: doesn't support timezone data, such as "+0100" etc
		String sMillis = dotNetJsonDate.replace("/Date(", "").replace(")/", "");
		long millis = 0;
		try {
			millis = Long.parseLong(sMillis);
		}
		catch (NumberFormatException ex) {
			if (DroidLib.D) Log.i("Utils", "getDateFromDotNetJsonDate: parsing failed");
		}
		Date date = new Date();
		if (millis > 0)
			date.setTime(millis);
		return date;
	}
	
	public static int dpToPixels(Resources resources, int dp) {
		final float scale = resources.getDisplayMetrics().density;
		int pixels = (int)(dp * scale + 0.5f);
		return pixels;
	}
	
	public static String getPrettyUrl(String url) {
		//String newUrl = url.replace("http://", "").replace("https://", "");
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			return null;
		}
		return uri.getHost();
	}
	
	// http://stackoverflow.com/questions/10321854/android-twitter-intent-to-my-companies-timeline
	public static String getTwitterLink(String twitterHandle) {
		String url = twitterHandle.replace("@", "");
		return "https://twitter.com/intent/user?screen_name=" + url;
	}
	
	public static boolean isValidUrl(String url) {
		try {
			new URI(url);
		} catch (URISyntaxException e) {
			return false;
		}
		return true;
	}
	
	public static String toHex(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);
	    return String.format("%0" + (bytes.length << 1) + "X", bi);
	}
}
