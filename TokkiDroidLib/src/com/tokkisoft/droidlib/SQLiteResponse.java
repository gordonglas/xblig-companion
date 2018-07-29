package com.tokkisoft.droidlib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class SQLiteResponse {
	private long id;
	private String url;
	private String response;
	private Date created;
	
	private static SimpleDateFormat dateFormatGmt = null;
	private static Calendar calendar = null;
	
	public SQLiteResponse() {
		if (SQLiteResponse.dateFormatGmt == null) {
			dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
			TimeZone gmt = TimeZone.getTimeZone("GMT");
			dateFormatGmt.setTimeZone(gmt);
			calendar = Calendar.getInstance(gmt);
		}
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	// set this.created to now
	public void setCreated() {
		created = new Date();
	}
	
	// set this.created from a GMT string in the database
	public void setCreated(String createdGmt) throws ParseException {
		created = SQLiteResponse.dateFormatGmt.parse(createdGmt);
	}

	// get this.created date as a GMT string for storing in the database
	public String getCreatedGmt() {
		return dateFormatGmt.format(created);
	}
	
	// get this.created date
	public Date getCreated() {
		return created;
	}
	
	// get this.created as a local time string for display purposes
	public String getCreatedLocal() {
		return created.toString();	// toString function converts to local time
	}
	
	public boolean isCacheFresh(int cacheForMinutes) {
		if (cacheForMinutes <= 0)
			return false;
		calendar.setTime(new Date());
		calendar.add(Calendar.MINUTE, -cacheForMinutes);
		Date cacheExpires = calendar.getTime();
		if (created.compareTo(cacheExpires) <= 0)
			return false;
		return true;
	}
}
