package com.tokkisoft;

import java.util.ArrayList;
import java.util.Date;

public class Game {
	private int id;
	private String name;
	private String boxArt;
	private double score;
	private int votes;
	private int developerId;
	private String developerName;
	private int genreId;
	private String genreName;
	private String info;
	private String marketPlaceLink;
	private String yahooLink;
	private int msPointsCost;
	private String devInfo;
	private Date releasedOn;
	private Date updatedOn;
	private ArrayList<GameImage> gameImages;
	
	public class GameImage
	{
		private int id;
		private int xbligGameId;
		private String imageLink;
		private int imageType;
		
		public GameImage(int id, int xbligGameId, String imageLink, int imageType)
		{
			this.id = id;
			this.xbligGameId = xbligGameId;
			this.imageLink = imageLink;
			this.imageType = imageType;
		}
		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getXbligGameId() {
			return xbligGameId;
		}
		public void setXbligGameId(int xbligGameId) {
			this.xbligGameId = xbligGameId;
		}
		public String getImageLink() {
			return imageLink;
		}
		public void setImageLink(String imageLink) {
			this.imageLink = imageLink;
		}
		public int getImageType() {
			return imageType;
		}
		public void setImageType(int imageType) {
			this.imageType = imageType;
		}
	}
	
	public Game()
	{
		gameImages = new ArrayList<Game.GameImage>();
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

	public int getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(int developerId) {
		this.developerId = developerId;
	}

	public String getDeveloperName() {
		return developerName;
	}

	public void setDeveloperName(String developerName) {
		this.developerName = developerName;
	}

	public int getGenreId() {
		return genreId;
	}

	public void setGenreId(int genreId) {
		this.genreId = genreId;
	}

	public String getGenreName() {
		return genreName;
	}

	public void setGenreName(String genreName) {
		this.genreName = genreName;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getMarketPlaceLink() {
		return marketPlaceLink;
	}

	public void setMarketPlaceLink(String marketPlaceLink) {
		this.marketPlaceLink = marketPlaceLink;
	}

	public int getMsPointsCost() {
		return msPointsCost;
	}

	public void setMsPointsCost(int msPointsCost) {
		this.msPointsCost = msPointsCost;
	}

	public String getDevInfo() {
		return devInfo;
	}

	public void setDevInfo(String devInfo) {
		this.devInfo = devInfo.replace("\r", "");
	}

	public Date getReleasedOn() {
		return releasedOn;
	}

	public void setReleasedOn(Date releasedOn) {
		this.releasedOn = releasedOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public ArrayList<Game.GameImage> getGameImages() {
		return gameImages;
	}
	
	public void addGameImage(GameImage gameImage) {
		if (gameImage.getImageType() == 0) // boxArt image
		{
			this.boxArt = gameImage.getImageLink();
		}
		
		this.gameImages.add(gameImage);
	}

	public String getYahooLink() {
		return yahooLink;
	}

	public void setYahooLink(String yahooLink) {
		this.yahooLink = yahooLink;
	}
}
