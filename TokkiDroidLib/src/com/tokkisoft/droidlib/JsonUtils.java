package com.tokkisoft.droidlib;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonUtils {
	public static JSONObject getObj(String json) {
		if (json == null)
			return null;
		json = json.trim();
		int len = json.length();
		if (len < 2)
			return null;
		//if (json.charAt(0) == '[' && json.charAt(len - 1) == ']')
		//	json = json.substring(1, len - 1);
		
		// android json parser requires the outermost array be named and surrounded by curly braces
		if (json.charAt(0) == '[' && json.charAt(len - 1) == ']') {
			json = "{\"mainobj\":" + json + "}";
		}
		
    	JSONObject obj = null;    	
    	JSONTokener t = new JSONTokener(json);
    	try {
			obj = (JSONObject) t.nextValue();
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	
    	return obj;
	}
}
