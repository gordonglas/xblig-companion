package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.Utils;
import com.tokkisoft.AppScreens.ScreenType;
import com.tokkisoft.AppScreens.TabType;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;

public class PickActivity extends FragmentActivity {
	private static final String TAG = "PickActivity";
	
	private static final String[] CONTENT = new String[] { "Info", "Games" };
	
	PickFragmentAdapter _adapter;
	ViewPager _pager;
	PageIndicator _indicator;
	XBLIGCActionBar _actionBar;
	PickActivityInput _pickInput;
	
	public PickActivityInput getPickActivityInput() {
		return _pickInput;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.pick_activity);
		
		// get DeveloperInfo from passed intent
		_pickInput = new PickActivityInput(getIntent());
		
		_actionBar = (XBLIGCActionBar)findViewById(R.id.tokkiActionBar);
        
        _adapter = new PickFragmentAdapter(this);
        _adapter.addFragmentSpec(ScreenType.PICK, TabType.PICK_INFO, PickInfoFragment.class, null);
        _adapter.addFragmentSpec(ScreenType.PICK, TabType.PICK_GAMES, PickGamesFragment.class, null);
        
        _pager = (ViewPager)findViewById(R.id.pager);
        _pager.setAdapter(_adapter);
        _pager.setOffscreenPageLimit(CONTENT.length);	// prevent the ViewPager from destroying off-screen fragments!
		
        _indicator = (TabPageIndicator)findViewById(R.id.indicator);
        _indicator.setViewPager(_pager);
		((TabPageIndicator) _indicator).setCustomOnPageChangeListener(_adapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (Globals.D) Log.i(TAG, "onResume");
		
		AppScreens.onActivityResume(ScreenType.PICK);
    	_actionBar.onScreenChange(ScreenType.PICK, AppScreens.getCurrentTab(ScreenType.PICK));
    	_actionBar.setActionBarText(_pickInput.getPickName());
	}
	
	class PickFragmentAdapter extends FragmentPagerAdapter implements TitleProvider, ViewPager.OnPageChangeListener {
		private final Context mContext;
		private final ArrayList<FragmentSpec> mFragmentSpecs = new ArrayList<FragmentSpec>();
		
		class FragmentSpec {
			private final Class<?> clss;
			private Bundle args;
			
			FragmentSpec(ScreenType _screenType, TabType _tabType, Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
                //if (args == null)
                //	args = new Bundle();
                //args.putInt("ScreenType", _screenType.ordinal());
                //args.putInt("TabType", _tabType.ordinal());
            }
		}
		
		public PickFragmentAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
			mContext = activity;
			onPageSelected(0);		// tell actionBar what to initially show
		}
		
		public void addFragmentSpec(ScreenType screenType, TabType tabType, Class<?> clss, Bundle args) {
    		FragmentSpec spec = new FragmentSpec(screenType, tabType, clss, args);
    		mFragmentSpecs.add(spec);
    	}
    	
    	public Fragment getFragment(int position) {
    		//return GameActivity.this.getSupportFragmentManager().findFragmentById(id);
    		String tag = Utils.makeFragmentName(R.id.pager, position);
    		return getSupportFragmentManager().findFragmentByTag(tag);
    	}

		@Override
		public Fragment getItem(int position) {
			FragmentSpec spec = mFragmentSpecs.get(position);
			return Fragment.instantiate(mContext, spec.clss.getName(), spec.args);
		}

		@Override
		public int getCount() {
			return PickActivity.CONTENT.length;
		}

		@Override
		public String getTitle(int position) {
			return PickActivity.CONTENT[position % PickActivity.CONTENT.length].toUpperCase();
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		
		@Override
		public void onPageSelected(int tabPosition) {
			AppScreens.setCurrentTab(ScreenType.PICK, tabPosition);
			_actionBar.onScreenChange(ScreenType.PICK, AppScreens.getCurrentTab(ScreenType.PICK));
			_actionBar.setActionBarText(_pickInput.getPickName());
		}
	}
}
