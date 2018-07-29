package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IObserver;
import com.tokkisoft.droidlib.Utils;
import com.tokkisoft.AppScreens.ScreenType;
import com.tokkisoft.AppScreens.TabType;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public class DevActivity extends FragmentActivity implements XBLIGCFragment {
	private static final String TAG = "DevActivity";
	
	private static final String[] CONTENT = new String[] { "Info", "Games" };
	
	private DevFragmentAdapter _adapter;
	private ViewPager _pager;
	private PageIndicator _indicator;
	private XBLIGCActionBar _actionBar;
	private DevActivityInput _devInput;
	private DevActivityRequestSubject _requestSubject;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.dev_activity);
		
		// get DeveloperInfo from passed intent
		_devInput = new DevActivityInput(getIntent());
		
		_actionBar = (XBLIGCActionBar)findViewById(R.id.tokkiActionBar);
        
        _adapter = new DevFragmentAdapter(this);
        _adapter.addFragmentSpec(ScreenType.DEV, TabType.DEV_INFO, DevInfoFragment.class, null);
        _adapter.addFragmentSpec(ScreenType.DEV, TabType.DEV_GAMES, DevGamesFragment.class, null);
        
        _pager = (ViewPager)findViewById(R.id.pager);
        _pager.setAdapter(_adapter);
        _pager.setOffscreenPageLimit(CONTENT.length);	// prevent the ViewPager from destroying off-screen fragments!
		
        _indicator = (TabPageIndicator)findViewById(R.id.indicator);
        _indicator.setViewPager(_pager);
		((TabPageIndicator) _indicator).setCustomOnPageChangeListener(_adapter);
		
		// data subject (Observer pattern)
		_requestSubject = new DevActivityRequestSubject(this);
		_requestSubject.setRequestIsRunning(true);
		_requestSubject.asyncRequest(_devInput);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.aboutApp:
    			Intent intent = new Intent(this, AboutActivity.class);
    	        this.startActivity(intent);
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
	
	@Override
	public void onRefresh() {
		if (!_requestSubject.requestIsRunning()) {
			_requestSubject.setRequestIsRunning(true);
			
			if (Globals.D) Log.i(TAG, "onRefresh()");
			
			_requestSubject.notifyObserversDataIsReloading();
			_requestSubject.asyncRequest(_devInput);
		}
	}
	
	@Override
	public ScreenType getScreenType() {
		return ScreenType.DEV;
	}

	@Override
	public TabType getTabType() {
		return null;
	}
	
	public void attachObserver(IObserver observer) {
		_requestSubject.attach(observer);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (Globals.D) Log.i(TAG, "onResume");
		
		AppScreens.onActivityResume(ScreenType.DEV);
    	_actionBar.onScreenChange(ScreenType.DEV, AppScreens.getCurrentTab(ScreenType.DEV));
    	_actionBar.setActionBarText(_devInput.getDevName());
	}
	
	class DevFragmentAdapter extends FragmentPagerAdapter implements TitleProvider, ViewPager.OnPageChangeListener {
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
        
        public DevFragmentAdapter(FragmentActivity activity) {
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
			return DevActivity.CONTENT.length;
		}

		@Override
		public String getTitle(int position) {
			return DevActivity.CONTENT[position % DevActivity.CONTENT.length].toUpperCase();
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		
		@Override
		public void onPageSelected(int tabPosition) {
			AppScreens.setCurrentTab(ScreenType.DEV, tabPosition);
			_actionBar.onScreenChange(ScreenType.DEV, AppScreens.getCurrentTab(ScreenType.DEV));
			_actionBar.setActionBarText(_devInput.getDevName());
		}
	}
}
