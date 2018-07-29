package com.tokkisoft;

import android.content.Intent;
import android.os.Bundle;

public class DevActivityInput {
	private int devId;
	private String devName;
	
	public DevActivityInput(int devId, String devName) {
		this.devId = devId;
		this.devName = devName;
	}
	
	public DevActivityInput(Intent intent) {
		Bundle extras = intent.getExtras();
		this.devId = extras.getInt(Globals.PACKAGE_PREFIX + "devId");
		this.devName = extras.getString(Globals.PACKAGE_PREFIX + "devName");
	}
	
	public void populateIntent(Intent intent) {
		intent.putExtra(Globals.PACKAGE_PREFIX + "devId", devId);
		intent.putExtra(Globals.PACKAGE_PREFIX + "devName", devName);
	}
	
	public int getDevId() {
		return devId;
	}
	public void setDevId(int devId) {
		this.devId = devId;
	}
	public String getDevName() {
		return devName;
	}
	public void setDevName(String devName) {
		this.devName = devName;
	}
	
	public String toString() {
		return devName;
	}
}
