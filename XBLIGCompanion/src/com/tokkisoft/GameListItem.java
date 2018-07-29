package com.tokkisoft;

import android.content.Intent;
import android.os.Bundle;

public class GameListItem {
	private int id;
	private String name;
	private String boxArt;
	private double score;
	private int votes;
	
	public GameListItem(int id, String name, String boxArt, double score, int votes) {
		this.id = id;
		this.name = name;
		this.boxArt = boxArt;
		this.score = score;
		this.votes = votes;
	}
	
	public GameListItem(Intent intent) {
		Bundle extras = intent.getExtras();
		this.id = extras.getInt(Globals.PACKAGE_PREFIX + "id");
		this.name = extras.getString(Globals.PACKAGE_PREFIX + "name");
		this.boxArt = extras.getString(Globals.PACKAGE_PREFIX + "boxArt");
		this.score = extras.getDouble(Globals.PACKAGE_PREFIX + "score");
		this.votes = extras.getInt(Globals.PACKAGE_PREFIX + "votes");
	}
	
	public void populateIntent(Intent intent) {
		intent.putExtra(Globals.PACKAGE_PREFIX + "id", id);
		intent.putExtra(Globals.PACKAGE_PREFIX + "name", name);
		intent.putExtra(Globals.PACKAGE_PREFIX + "boxArt", boxArt);
		intent.putExtra(Globals.PACKAGE_PREFIX + "score", score);
		intent.putExtra(Globals.PACKAGE_PREFIX + "votes", votes);
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
	public String getBoxArt() {
		return boxArt;
	}
	public void setBoxArt(String boxArt) {
		this.boxArt = boxArt;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getVotes() {
		return votes;
	}
	public void setVotes(int votes) {
		this.votes = votes;
	}
	
	public String toString() {
		return name;
	}
}
