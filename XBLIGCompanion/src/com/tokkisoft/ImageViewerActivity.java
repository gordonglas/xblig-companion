package com.tokkisoft;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.tokkisoft.R;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

// http://thepseudocoder.wordpress.com/2011/10/05/android-page-swiping-using-viewpager/
public class ImageViewerActivity extends FragmentActivity {
	//private static final String TAG = "ImageViewerActivity";
	
	private PagerAdapter _adapter;
	private ViewPager _pager;
	private String[] _imageUrls;
	private int _initialPosition;
	private PageIndicator _indicator;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_viewer_activity);
		
		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				_initialPosition = extras.getInt(Globals.PACKAGE_PREFIX + "imagePosition");
			}
		}
		
		ArrayList<String> imageUrls = Globals.getImageUrls();
		_imageUrls = new String[imageUrls.size()];
		int i = 0;
		for (String url : imageUrls) {
			_imageUrls[i] = url;
			i++;
		}
		
		List<Fragment> fragments = new Vector<Fragment>();
		for (int j = 0; j < _imageUrls.length; j++) {
			Bundle bundle = new Bundle();
			bundle.putInt("fragmentPos", j);
			bundle.putString("imageUrl", _imageUrls[j]);
			fragments.add(Fragment.instantiate(this, ImageViewerFragment.class.getName(), bundle));
		}
		_adapter = new ImageViewerPagerAdapter(getSupportFragmentManager(), fragments);
		_pager = (ViewPager)super.findViewById(R.id.imageViewPager);
		_pager.setOffscreenPageLimit(GameScreensFragment.MAX_SCREENSHOTS_SHOWN);
		_pager.setAdapter(_adapter);
		_pager.setCurrentItem(_initialPosition, false);
		
		_indicator = (CirclePageIndicator)findViewById(R.id.indicator);
		_indicator.setViewPager(_pager);
		_indicator.setCurrentItem(_initialPosition);
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
	
	class ImageViewerPagerAdapter extends FragmentPagerAdapter {

		private List<Fragment> _fragments;
		
		public ImageViewerPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			_fragments = fragments;
		}
		
		@Override
		public Fragment getItem(int position) {
			return _fragments.get(position);
		}

		@Override
		public int getCount() {
			return _fragments.size();
		}
	}
}
