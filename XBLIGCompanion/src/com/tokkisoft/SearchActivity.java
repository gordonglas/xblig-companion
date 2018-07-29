package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IErrorCallback.ErrorType;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

public class SearchActivity extends FragmentActivity {
	//private static final String TAG = "SearchActivity";
	
	private static final String[] CONTENT = new String[] { "Games", "Developers" };
	
	private SearchFragmentAdapter _adapter;
	private ViewPager _pager;
	private PageIndicator _indicator;
	private XBLIGCActionBar _actionBar;
	private SearchActivityRequestSubject _requestSubject;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.search_activity);
        
        _requestSubject = new SearchActivityRequestSubject();
        
        _actionBar = (XBLIGCActionBar)findViewById(R.id.tokkiActionBar);
        
        _adapter = new SearchFragmentAdapter(this);
        _adapter.addFragmentSpec(ScreenType.SEARCH, TabType.SEARCH_GAMES, SearchGamesFragment.class, null);
        _adapter.addFragmentSpec(ScreenType.SEARCH, TabType.SEARCH_DEVELOPERS, SearchDevsFragment.class, null);
		
        _pager = (ViewPager)findViewById(R.id.pager);
        _pager.setAdapter(_adapter);
        _pager.setOffscreenPageLimit(CONTENT.length);	// prevent the ViewPager from destroying off-screen fragments!
		
        _indicator = (TabPageIndicator)findViewById(R.id.indicator);
        _indicator.setViewPager(_pager);
		((TabPageIndicator) _indicator).setCustomOnPageChangeListener(_adapter);
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
	
	public void onSearchButtonClick(String searchText) {
		// hide keyboard
		InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
		
		_requestSubject.setSearchString(searchText);
		_requestSubject.notifyObservers(ErrorType.NO_ERROR);
	}
	
    @Override
    public void onResume() {
    	super.onResume();
    	AppScreens.onActivityResume(ScreenType.SEARCH);
    	_actionBar.onScreenChange(ScreenType.SEARCH, AppScreens.getCurrentTab(ScreenType.SEARCH));
    }
    
	class SearchFragmentAdapter extends FragmentPagerAdapter implements TitleProvider, ViewPager.OnPageChangeListener {
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
        
        public SearchFragmentAdapter(FragmentActivity activity) {
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
			return SearchActivity.CONTENT.length;
		}

		@Override
		public String getTitle(int position) {
			return SearchActivity.CONTENT[position % SearchActivity.CONTENT.length].toUpperCase();
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		
		@Override
		public void onPageSelected(int tabPosition) {
			AppScreens.setCurrentTab(ScreenType.SEARCH, tabPosition);
			_actionBar.onScreenChange(ScreenType.SEARCH, AppScreens.getCurrentTab(ScreenType.SEARCH));
		}
	}
	
	public void attachObserver(IObserver observer) {
		_requestSubject.attach(observer);
	}
}
