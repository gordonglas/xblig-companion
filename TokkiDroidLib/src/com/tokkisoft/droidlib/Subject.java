package com.tokkisoft.droidlib;

import java.util.ArrayList;

import com.tokkisoft.droidlib.IErrorCallback.ErrorType;

public abstract class Subject
{
	private ArrayList<IObserver> _observers;
	private boolean _requestDone;
	private boolean _requestIsRunning;
	private ErrorType _lastErrorType;
	
	public Subject()
	{
		_observers = new ArrayList<IObserver>();
	}
	
	public synchronized boolean getRequestDone()
	{
		if (_requestIsRunning)
			return false;
		return _requestDone;
	}
	
	public synchronized void setRequestDone(boolean requestDone)
	{
		_requestDone = requestDone;
		if (requestDone)
			_requestIsRunning = false;
	}
	
	public synchronized boolean requestIsRunning()
	{
		return _requestIsRunning;
	}
	
	public synchronized void setRequestIsRunning(boolean requestIsRunning)
	{
		_requestIsRunning = requestIsRunning;
	}
	
	public void attach(IObserver observer)
	{
		_observers.add(observer);
		
		// if request is already done and not currently running a new request,
		// notify this observer immediately
		if (getRequestDone())
		{
			//notifyObservers(_lastErrorType);
			observer.onSubjectUpdated(this, _lastErrorType);
		}
	}
	
	public boolean detach(IObserver observer)
	{
		return _observers.remove(observer);
	}
	
	public void notifyObservers(ErrorType errorType)
	{
		_lastErrorType = errorType;
		setRequestDone(true);
		
		for (IObserver observer : _observers) {
			observer.onSubjectUpdated(this, errorType);
		}
	}
	
	public void notifyObserversDataIsReloading()
	{
		for (IObserver observer : _observers) {
			observer.onSubjectReloading(this);
		}
	}
}
