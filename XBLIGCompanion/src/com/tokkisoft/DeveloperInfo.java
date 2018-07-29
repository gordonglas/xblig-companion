package com.tokkisoft;

import java.util.ArrayList;

public class DeveloperInfo {
	private DeveloperItem devItem;
	private ArrayList<Game> games;
	
	public DeveloperInfo() {
		devItem = new DeveloperItem();
		games = new ArrayList<Game>();
	}
	
	public DeveloperItem getDevItem() {
		return devItem;
	}
	public ArrayList<Game> getGames() {
		return games;
	}
}
