package com.tokkisoft;

public class BasicListItem {
	private int id;
	private String name;
	private String displayHeader;
	
	public BasicListItem(int id, String name, String displayHeader) {
		this.id = id;
		this.name = name;
		this.displayHeader = displayHeader;
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
	public String getDisplayHeader() {
		return displayHeader;
	}
	public void setDisplayHeader(String displayHeader) {
		this.displayHeader = displayHeader;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
