package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.droidlib.IErrorCallback.ErrorType;

public class Globals {
	public static final boolean D = false;
	public static final String SUPPORT_EMAIL = "support@tokkisoft.com";
	public static final String PACKAGE_PREFIX = "com.tokkisoft.";
	
	public static final byte NEW_GAMES_ROW_COUNT = 10;
	
	public static String getErrorMsg(ErrorType errorType, boolean refreshButtonAbove)
	{
		return Globals.getErrorMsg(errorType, refreshButtonAbove, false);
	}
	
	public static String getErrorMsg(ErrorType errorType, boolean refreshButtonAbove, boolean onSearchActivity)
	{
		String msg = Globals.getGenericErrorMsg();
		if (errorType == ErrorType.SOCKET_TIMEOUT_EXCEPTION)
			msg = Globals.getRequestTimeoutErrorMsg(refreshButtonAbove, onSearchActivity);
		else if (errorType == ErrorType.IO_EXCEPTION)
			msg = Globals.getConnectionErrorMsg(refreshButtonAbove, onSearchActivity);
		else if (errorType == ErrorType.INVALID_RESPONSE_FORMAT)
			msg = Globals.getInvalidResponseFormatErrorMsg(refreshButtonAbove, onSearchActivity);
		else if (errorType == ErrorType.NO_DATA_RETURNED)
			msg = Globals.getNoDataReturnedErrorMsg(refreshButtonAbove, onSearchActivity);
		
		return msg;
	}
	
	public static String getContactMsg() {
		return "If the error persists, please contact " + Globals.SUPPORT_EMAIL;
	}
	
	public static String getGenericErrorMsg() {
		return "An error occurred. Please try again later. " + getContactMsg();
	}
	
	public static String getRequestTimeoutErrorMsg(boolean refreshButtonAbove, boolean onSearchActivity) {
		String msg = "A timeout error occurred. The request is taking too long. ";
		if (refreshButtonAbove)
			msg += "Tap the refresh button above to try again. ";
		else if (onSearchActivity)
			msg += "Try searching again. ";
		msg += getContactMsg();
		return msg;
	}
	
	public static String getConnectionErrorMsg(boolean refreshButtonAbove, boolean onSearchActivity) {
		String msg = "A connection error occurred. Make sure you are connected to the Internet";
		if (refreshButtonAbove)
			msg += " before you tap the refresh button above to try again. ";
		else if (onSearchActivity)
			msg += " before you try searching again. ";
		else
			msg += ". ";
		msg += getContactMsg();
		return msg;
	}
	
	public static String getInvalidResponseFormatErrorMsg(boolean refreshButtonAbove, boolean onSearchActivity) {
		String msg = "An error occurred. The response data format is invalid. ";
		if (refreshButtonAbove)
			msg += "Tap the refresh button above to try again. ";
		else if (onSearchActivity)
			msg += "Try searching again or search for something else. ";
		msg += getContactMsg();
		return msg;
	}
	
	public static String getNoDataReturnedErrorMsg(boolean refreshButtonAbove, boolean onSearchActivity) {
		String msg;
		if (onSearchActivity)
		{
			msg = "No results returned.";
		}
		else
		{
			msg = "An error occurred. The response data is empty. ";
			if (refreshButtonAbove)
				msg += "Tap the refresh button above to try again. ";
			msg += getContactMsg();
		}
		return msg;
	}
	
	private static ArrayList<String> ImageUrls;
	
	public static ArrayList<String> getImageUrls() {
		return Globals.ImageUrls;
	}
	
	public static void setImageUrls(ArrayList<String> imageUrls) {
		Globals.ImageUrls = imageUrls;
	}
}
