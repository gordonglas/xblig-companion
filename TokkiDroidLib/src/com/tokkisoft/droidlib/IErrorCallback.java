package com.tokkisoft.droidlib;

public interface IErrorCallback {
	public enum ErrorType {
		NO_ERROR,
		GENERIC,
		SOCKET_TIMEOUT_EXCEPTION,
		IO_EXCEPTION,
		INVALID_RESPONSE_FORMAT,
		NO_DATA_RETURNED
	}
	
	public void onError(ErrorType errorType, Exception ex);
}
