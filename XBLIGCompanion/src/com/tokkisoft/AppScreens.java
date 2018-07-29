package com.tokkisoft;

import java.util.ArrayList;
import java.util.HashMap;

public class AppScreens {
	
	public enum ScreenType {
		HOME,
		NEW_GAMES,
		GAME,
		DEV,
		GENRE,
		PICK,
		SEARCH,
		ABOUT
	}
	
	public enum TabType {
		HOME_CATALOG,
		HOME_PICKS,
		NEW_GAMES_NEW_GAMES,
		GAME_INFO,
		GAME_SCREENS,
		GAME_LINKS,
		DEV_INFO,
		DEV_GAMES,
		GENRE_GENRE,
		PICK_INFO,
		PICK_GAMES,
		SEARCH_GAMES,
		SEARCH_DEVELOPERS,
		ABOUT
	}
	
	public class TabInfo {
		public ArrayList<TabType> tabs;
		public int currentTabPosition;
		
		public TabInfo() {
			tabs = new ArrayList<AppScreens.TabType>();
		}
		
		public TabType getCurrentTab() {
			return tabs.get(currentTabPosition);
		}
	}
	
	private HashMap<ScreenType, TabInfo> appScreens;
	private ScreenType currentScreen;
	//private TabType currentTab;
	
	private static AppScreens _instance;
	
	private static AppScreens instance() {
		if (_instance == null)
			_instance = new AppScreens();
		return _instance;
	}
	
	public static void setCurrentTab(ScreenType screen, int tabPosition)
	{
		AppScreens instance = AppScreens.instance();
		
		instance.currentScreen = screen;
		
		TabInfo tabInfo = instance.appScreens.get(screen);
		tabInfo.currentTabPosition = tabPosition;
	}
	
	public static ScreenType getCurrentScreen()
	{
		return AppScreens.instance().currentScreen;
	}
	
	public static TabType getCurrentTab(ScreenType screen)
	{
		TabInfo tabInfo = AppScreens.instance().appScreens.get(screen);
		return tabInfo.getCurrentTab();
	}
	
	public static int getCurrentTabPosition()
	{
		ScreenType currentScreen = AppScreens.getCurrentScreen();
		ArrayList<TabType> tabs = AppScreens.instance().appScreens.get(currentScreen).tabs;
		int tabsSize = tabs.size();
		int i;
		TabType currentTab = AppScreens.getCurrentTab(currentScreen);
		for (i = 0; i < tabsSize; i++) {
			if (tabs.get(i) == currentTab)
				break;
		}
		return i;
	}
	
	public static void onActivityResume(ScreenType screen)
	{
		AppScreens.instance().currentScreen = screen;
	}
	
	private AppScreens()
	{
		appScreens = new HashMap<AppScreens.ScreenType, TabInfo>();
		
		TabInfo homeTabInfo = new TabInfo();
		homeTabInfo.tabs.add(TabType.HOME_CATALOG);
		homeTabInfo.tabs.add(TabType.HOME_PICKS);
		
		TabInfo newGameTabInfo = new TabInfo();
		newGameTabInfo.tabs.add(TabType.NEW_GAMES_NEW_GAMES);
		
		TabInfo gameTabInfo = new TabInfo();
		gameTabInfo.tabs.add(TabType.GAME_INFO);
		gameTabInfo.tabs.add(TabType.GAME_SCREENS);
		gameTabInfo.tabs.add(TabType.GAME_LINKS);
		
		TabInfo devInfo = new TabInfo();
		devInfo.tabs.add(TabType.DEV_INFO);
		devInfo.tabs.add(TabType.DEV_GAMES);
		
		TabInfo genreInfo = new TabInfo();
		genreInfo.tabs.add(TabType.GENRE_GENRE);
		
		TabInfo pickInfo = new TabInfo();
		pickInfo.tabs.add(TabType.PICK_INFO);
		pickInfo.tabs.add(TabType.PICK_GAMES);
		
		TabInfo searchInfo = new TabInfo();
		searchInfo.tabs.add(TabType.SEARCH_GAMES);
		searchInfo.tabs.add(TabType.SEARCH_DEVELOPERS);
		
		TabInfo aboutInfo = new TabInfo();
		aboutInfo.tabs.add(TabType.ABOUT);
		
		appScreens.put(ScreenType.HOME, homeTabInfo);
		appScreens.put(ScreenType.NEW_GAMES, newGameTabInfo);
		appScreens.put(ScreenType.GAME, gameTabInfo);
		appScreens.put(ScreenType.DEV, devInfo);
		appScreens.put(ScreenType.GENRE, genreInfo);
		appScreens.put(ScreenType.PICK, pickInfo);
		appScreens.put(ScreenType.SEARCH, searchInfo);
		appScreens.put(ScreenType.ABOUT, aboutInfo);
	}
}
