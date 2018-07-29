package com.tokkisoft;

public class DeveloperItem {
	private int id;
	private String name;
	private String bio;
	private String website;
	private String twitterHandle;
	private String facebookPage;
	private String youtubeChannel;
	
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
	public String getBio() {
		return bio;
	}
	public void setBio(String bio) {
		// Carriage returns show up as a square on android
		this.bio = bio.replace("\r", "");
	}
	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}
	public String getTwitterHandle() {
		return twitterHandle;
	}
	public void setTwitterHandle(String twitterHandle) {
		this.twitterHandle = twitterHandle;
	}
	public String getFacebookPage() {
		return facebookPage;
	}
	public void setFacebookPage(String facebookPage) {
		this.facebookPage = facebookPage;
	}
	public String getYoutubeChannel() {
		return youtubeChannel;
	}
	public void setYoutubeChannel(String youtubeChannel) {
		this.youtubeChannel = youtubeChannel;
	}
	
	public String toString() {
		return name;
	}
}
