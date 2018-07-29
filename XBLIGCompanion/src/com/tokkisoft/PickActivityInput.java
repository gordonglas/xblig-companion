package com.tokkisoft;

import android.content.Intent;
import android.os.Bundle;

public class PickActivityInput {
	private int pickId;		//  promoId
	private String pickName;
	
	public PickActivityInput(int pickId, String pickName) {
		this.setPickId(pickId);
		this.setPickName(pickName);
	}
	
	public PickActivityInput(Intent intent) {
		Bundle extras = intent.getExtras();
		this.setPickId(extras.getInt(Globals.PACKAGE_PREFIX + "pickId"));
		this.setPickName(extras.getString(Globals.PACKAGE_PREFIX + "pickName"));
	}
	
	public void populateIntent(Intent intent) {
		intent.putExtra(Globals.PACKAGE_PREFIX + "pickId", getPickId());
		intent.putExtra(Globals.PACKAGE_PREFIX + "pickName", getPickName());
	}

	public int getPickId() {
		return pickId;
	}

	public void setPickId(int pickId) {
		this.pickId = pickId;
	}

	public String getPickName() {
		return pickName;
	}

	public void setPickName(String pickName) {
		this.pickName = pickName;
	}
	
	public String toString() {
		return pickName;
	}
}
