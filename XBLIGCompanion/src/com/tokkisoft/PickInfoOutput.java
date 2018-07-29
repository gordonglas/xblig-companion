package com.tokkisoft;

public class PickInfoOutput {
	private int pickId;
	private String pickName;
	private String promotionDisplayHeader;
	private String webSite;
	private String twitterHandle;
	private String info;
	
	public PickInfoOutput(int pickId, String pickName, String promotionDisplayHeader,
			String webSite, String twitterHandle, String info) {
		this.pickId = pickId;
		this.pickName = pickName;
		this.promotionDisplayHeader = promotionDisplayHeader;
		this.webSite = webSite;
		this.twitterHandle = twitterHandle;
		this.info = info.replace("\r", "");
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
	public String getWebSite() {
		return webSite;
	}
	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}
	public String getTwitterHandle() {
		return twitterHandle;
	}
	public void setTwitterHandle(String twitterHandle) {
		this.twitterHandle = twitterHandle;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		// Carriage returns show up as a square on android
		this.info = info.replace("\r", "");
	}
	
	public String toString() {
		return pickName;
	}

	public String getPromotionDisplayHeader() {
		return promotionDisplayHeader;
	}

	public void setPromotionDisplayHeader(String promotionDisplayHeader) {
		this.promotionDisplayHeader = promotionDisplayHeader;
	}
}
