package com.tokkisoft;

import com.tokkisoft.droidlib.Subject;

public class SearchActivityRequestSubject extends Subject {

	private String _searchString;
	
	public SearchActivityRequestSubject()
	{
		super();
	}

	public String getSearchString() {
		return _searchString;
	}

	public void setSearchString(String searchString) {
		this._searchString = searchString;
	}
	
	
}
