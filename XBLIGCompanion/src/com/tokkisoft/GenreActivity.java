package com.tokkisoft;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.HttpRequest;
import com.tokkisoft.droidlib.IErrorCallback;
import com.tokkisoft.droidlib.JsonUtils;
import com.tokkisoft.AppScreens.ScreenType;
import com.tokkisoft.AppScreens.TabType;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GenreActivity extends ListActivity implements XBLIGCFragment {
	private static final String TAG = "GenreActivity"; 

	private XBLIGCActionBar _actionBar;
	private LinearLayout _progressLayout;
	private ListView _listView;
	private WorkerTask _task;
	private boolean _isReloading;
	private BasicListAdapter _adapter;
	
	// synchronization for reload button
	public synchronized boolean isReloading() {
		return _isReloading;
	}

	public synchronized void setIsReloading(boolean isReloading) {
		_isReloading = isReloading;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.genre_activity);
        
        _actionBar = (XBLIGCActionBar)findViewById(R.id.tokkiActionBar);
        AppScreens.setCurrentTab(ScreenType.GENRE, 0);
		_actionBar.onScreenChange(ScreenType.GENRE, AppScreens.getCurrentTab(ScreenType.GENRE));
		
		_progressLayout = (LinearLayout)findViewById(R.id.progressLayout);
		_listView = (ListView)findViewById(android.R.id.list);
		
		_progressLayout.setVisibility(View.VISIBLE);
		_listView.setVisibility(View.GONE);
		
		_task = new WorkerTask(this, false);
		_task.execute();
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
    public void onResume() {
    	super.onResume();
    	AppScreens.onActivityResume(ScreenType.GENRE);
    	_actionBar.onScreenChange(ScreenType.GENRE, AppScreens.getCurrentTab(ScreenType.NEW_GAMES));
    }
	
	@Override
	public ScreenType getScreenType() {
		return ScreenType.GENRE;
	}

	@Override
	public TabType getTabType() {
		return TabType.GENRE_GENRE;
	}

	@Override
	public void onRefresh() {
		if (!isReloading()) {
			setIsReloading(true);
			
			if (Globals.D) Log.i(TAG, "onRefresh()");
			
	        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress);
			TextView progressText = (TextView)findViewById(R.id.progressText);
	        
			progressText.setGravity(Gravity.CENTER_HORIZONTAL);
			progressText.setTextAppearance(this, android.R.style.TextAppearance_Large_Inverse);
			progressText.setText("Retrieving data...");
			progressText.setTextColor(getResources().getColor(R.color.text));
	        _progressLayout.setVisibility(View.VISIBLE);
	        progressBar.setVisibility(View.VISIBLE);
	    	_listView.setVisibility(View.GONE);
	    	
	    	_task = new WorkerTask(this, true);
	    	_task.execute();
		}
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		// id is what we return in adaptor::getItemId(position)
		if (Globals.D) Log.i(TAG, "Item clicked: " + id);
        super.onListItemClick(l, v, position, id);
        
        // pass data of selected game to GameActivity
        BasicListItem listItem = (BasicListItem)_adapter.getItem(position);
        Genre item = new Genre(listItem);
        Intent intent = new Intent(this, GamesActivity.class);
        item.populateIntent(intent);
        startActivity(intent);
	}
	
	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback {
		private Context _context;
		private ArrayList<Genre> _items;
		private boolean _isReloading;
		
		private boolean _isError;
    	private ErrorType _errorType;
    	//private Exception _exception;
		
		public WorkerTask(Context context, boolean isReloading) {
			_context = context;
			_isReloading = isReloading;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			//_items = new ArrayList<GameListItem>();
			//_items.add(new GameListItem(1, "Testing 1", "", 0, 0));
			//_items.add(new GameListItem(2, "Testing 2", "", 0, 0));
			//_items.add(new GameListItem(3, "Testing 3", "", 0, 0));
			
			HttpRequest request = new HttpRequest(_context, this);
			
			String url = String.format("http://noms.apphb.com/Xblig/GenreList");
			
			String response = request.httpGet(url, -1);
			
			if (_isError)
				return null;
			
			JSONObject json = JsonUtils.getObj(response);
			if (json == null) {
				_isError = true;
				_errorType = ErrorType.INVALID_RESPONSE_FORMAT;
				return null;
			}
			
			JSONArray nameArray = json.names();
			JSONArray valArray;
			int len = 0;
			try {
				valArray = json.toJSONArray(nameArray);
				if (valArray.length() > 0) {
					JSONArray ary = valArray.getJSONArray(0);
					len = ary.length();
					if (len > 0)
						_items = new ArrayList<Genre>(len);
					for (int i = 0; i < len; i++) {
						JSONObject o = ary.getJSONObject(i);
						_items.add(new Genre(o.getInt("Id"),
								o.getString("Name")));
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if (len == 0) {
				_isError = true;
				_errorType = ErrorType.NO_DATA_RETURNED;
				return null;
			}
			
			// simulate long load time (for testing)
			//try {
			//	Thread.sleep(5000);
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}
			
			return null;
		}
		
		@ Override
	    protected void onPostExecute(Void result) {
			// if was still loading due to a "load more" (scrolling down) while user pressed the Refresh button, then ignore these results
			boolean refreshButtonClicked = isReloading();
			if ((_isReloading && refreshButtonClicked) || (!_isReloading && !refreshButtonClicked))
			{
				if (_isError) {
					//if (_startRecord == 0)
					//{
					ProgressBar progressBar = (ProgressBar)GenreActivity.this.findViewById(R.id.progress);
					TextView progressText = (TextView)GenreActivity.this.findViewById(R.id.progressText);
					
					_progressLayout.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.GONE);
					progressText.setGravity(Gravity.LEFT);
					progressText.setTextAppearance(_context, android.R.style.TextAppearance_Large_Inverse);
					progressText.setText(Globals.getErrorMsg(_errorType, true));
					_listView.setVisibility(View.GONE);
					//}
				}
				else {
					int len = _items.size();
					if (len > 0) {
						BasicListItem[] listItems = new BasicListItem[len];
						int i = 0;
						for (Genre genre : _items) {
							listItems[i] = new BasicListItem(genre.getId(), genre.getName(), genre.getName());
							i++;
						}
						_adapter = new BasicListAdapter(_context, listItems);
						setListAdapter(_adapter);
					}
					
					_progressLayout.setVisibility(View.GONE);
					_listView.setVisibility(View.VISIBLE);
				}
			}
			setIsReloading(false);
		}
		
		@Override
		public void onError(ErrorType errorType, Exception ex) {
			// save error info so it can be displayed on UI thread later
			_isError = true;
			_errorType = errorType;
			//_exception = ex;
		}
	}
}
