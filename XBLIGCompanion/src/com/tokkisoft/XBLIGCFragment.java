package com.tokkisoft;

import com.tokkisoft.AppScreens.ScreenType;
import com.tokkisoft.AppScreens.TabType;

public interface XBLIGCFragment {
	ScreenType getScreenType();
	TabType getTabType();
	void onRefresh();
}
