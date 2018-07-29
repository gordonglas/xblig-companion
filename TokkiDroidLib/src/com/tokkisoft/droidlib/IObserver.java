package com.tokkisoft.droidlib;

import com.tokkisoft.droidlib.IErrorCallback.ErrorType;

public interface IObserver {
	void onSubjectUpdated(Subject changedSubject, ErrorType errorType);
	void onSubjectReloading(Subject reloadingSubject);
}
