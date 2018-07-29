package com.tokkisoft;

import android.content.Intent;
import android.os.Bundle;

public class Genre {
	private int id;
	private String name;
	
	public Genre(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public Genre(BasicListItem item) {
		this.id = item.getId();
		this.name = item.getName();
	}
	
	public Genre(Intent intent) {
		Bundle extras = intent.getExtras();
		this.id = extras.getInt(Globals.PACKAGE_PREFIX + "genreId");
		this.name = extras.getString(Globals.PACKAGE_PREFIX + "genreName");
	}
	
	public void populateIntent(Intent intent) {
		intent.putExtra(Globals.PACKAGE_PREFIX + "gamesActivityType", GamesActivity.GAMES_ACTIVITY_TYPE.GENRE.ordinal());
		intent.putExtra(Globals.PACKAGE_PREFIX + "genreId", id);
		intent.putExtra(Globals.PACKAGE_PREFIX + "genreName", name);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
}
