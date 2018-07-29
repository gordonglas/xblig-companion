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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

// not only used for New Games screen, but also GenreGameList etc
public class GamesActivity extends ListActivity implements XBLIGCFragment, OnScrollListener {
	private static final String TAG = "GamesActivity"; 
	
	public enum GAMES_ACTIVITY_TYPE {
		NEW_GAME,
		RECENTLY_UPDATED,
		GENRE,
		BY_RATING
	}
	
	private XBLIGCActionBar _actionBar;
	private LinearLayout _progressLayout;
	private ListView _listView;
	private boolean _isReloading;
	private boolean _isNextPageLoading;
	private GameListAdapter _adapter;
	private int _listType;
	private int _startRecord;
	private String _actionBarText;
	private WorkerTask _task;
	private GAMES_ACTIVITY_TYPE _gamesActivityType;
	private int _genreId;
	private String _genreName;
	
	// synchronization for reload button
	public synchronized boolean isReloading() {
		return _isReloading;
	}

	public synchronized void setIsReloading(boolean isReloading) {
		_isReloading = isReloading;
	}
	
	// synchronization for loading next set of records for infinite scrolling
	public synchronized boolean isNextPageLoading() {
		return _isNextPageLoading;
	}

	public synchronized void setIsNextPageLoading(boolean isNextPageLoading) {
		_isNextPageLoading = isNextPageLoading;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.games_activity);
        
        _gamesActivityType = GAMES_ACTIVITY_TYPE.NEW_GAME;
        _listType = 1;
        _actionBarText = "New Games";
        
        Intent intent = getIntent();
        if (intent != null) {
        	Bundle extras = intent.getExtras();
        	if (extras != null) {
        		_gamesActivityType = GAMES_ACTIVITY_TYPE.values()[extras.getInt(Globals.PACKAGE_PREFIX + "gamesActivityType")];
        		if (_gamesActivityType == GAMES_ACTIVITY_TYPE.GENRE) {
        			_genreId = extras.getInt(Globals.PACKAGE_PREFIX + "genreId");
        			_genreName = extras.getString(Globals.PACKAGE_PREFIX + "genreName");
        			_actionBarText = _genreName;
        		}
        		else {
        			if (_gamesActivityType == GAMES_ACTIVITY_TYPE.NEW_GAME) {
        				_listType = 1;
        				_actionBarText = "New Games";
        			}
        			else if (_gamesActivityType == GAMES_ACTIVITY_TYPE.RECENTLY_UPDATED) {
        				_listType = 0;
        				_actionBarText = "Games Recently Updated";
        			}
        			else if (_gamesActivityType == GAMES_ACTIVITY_TYPE.BY_RATING) {
        				_listType = 2;
        				_actionBarText = "Games By Rating";
        			}
        		}
        	}
        }
        
        _actionBar = (XBLIGCActionBar)findViewById(R.id.tokkiActionBar);
        AppScreens.setCurrentTab(ScreenType.NEW_GAMES, 0);
		_actionBar.onScreenChange(ScreenType.NEW_GAMES, AppScreens.getCurrentTab(ScreenType.NEW_GAMES));
		_actionBar.setActionBarText(_actionBarText);
		
		_progressLayout = (LinearLayout)findViewById(R.id.progressLayout);
		_listView = (ListView)findViewById(android.R.id.list);
		_listView.setOnScrollListener(this);
		
		_progressLayout.setVisibility(View.VISIBLE);
		_listView.setVisibility(View.GONE);
		
		_startRecord = 0;
		
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
    	AppScreens.onActivityResume(ScreenType.NEW_GAMES);
    	_actionBar.onScreenChange(ScreenType.NEW_GAMES, AppScreens.getCurrentTab(ScreenType.NEW_GAMES));
    	_actionBar.setActionBarText(_actionBarText);
    }
	
	@Override
	public ScreenType getScreenType() {
		return ScreenType.NEW_GAMES;
	}

	@Override
	public TabType getTabType() {
		return TabType.NEW_GAMES_NEW_GAMES;
	}
	
	@Override
	public void onRefresh() {
		if (!isReloading()) {
			setIsReloading(true);
			
			if (Globals.D) Log.i("NewGamesActivity", "onRefresh()");
			
	        ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress);
			TextView progressText = (TextView)findViewById(R.id.progressText);
	        
			progressText.setGravity(Gravity.CENTER_HORIZONTAL);
			progressText.setTextAppearance(this, android.R.style.TextAppearance_Large_Inverse);
			progressText.setText("Retrieving data...");
			progressText.setTextColor(getResources().getColor(R.color.text));
	        _progressLayout.setVisibility(View.VISIBLE);
	        progressBar.setVisibility(View.VISIBLE);
	    	_listView.setVisibility(View.GONE);
	    	
	    	_startRecord = 0;
	    	
	    	_task = new WorkerTask(this, true);
	    	_task.execute();
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (totalItemCount < Globals.NEW_GAMES_ROW_COUNT)
			return;
		//if (Globals.D) Log.i(TAG, String.format("LOADMORE: SCROLLPOS: firstVisibleItem:%d visibleItemCount:%d totalItemCount:%d", firstVisibleItem, visibleItemCount, totalItemCount));
		boolean loadMore = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;

        if (loadMore) {
        	if (!isNextPageLoading()) {
        		setIsNextPageLoading(true);
        		if (Globals.D) Log.i(TAG, String.format("LOADMORE: firstVisibleItem:%d visibleItemCount:%d totalItemCount:%d", firstVisibleItem, visibleItemCount, totalItemCount));
	        	
	        	_startRecord += Globals.NEW_GAMES_ROW_COUNT;
	        	
	        	_task = new WorkerTask(this, false);
		    	_task.execute();
        	}
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		// id is what we return in GameListAdaptor::getItemId(position)
		if (Globals.D) Log.i(TAG, "Item clicked: " + id);
        super.onListItemClick(l, v, position, id);
        
        // pass data of selected game to GameActivity
        GameListItem item = (GameListItem)_adapter.getItem(position);
        Intent intent = new Intent(this, GameActivity.class);
        item.populateIntent(intent);
        startActivity(intent);
	}
	
	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback {
		private Context _context;
		private ArrayList<GameListItem> _items;
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
			// http://noms.apphb.com/Xblig/GameList?St=1&Skip=0&Num=10
			// http://noms.apphb.com/Xblig/GameGenreList?Genre=10&Skip=0&Num=10
			
			String url = null;
			if (_gamesActivityType == GAMES_ACTIVITY_TYPE.GENRE)
	        	url = String.format("http://noms.apphb.com/Xblig/GameGenreList?Genre=%d&Skip=%d&Num=%d", _genreId, _startRecord, Globals.NEW_GAMES_ROW_COUNT);
			else
				url = String.format("http://noms.apphb.com/Xblig/GameList?St=%d&Skip=%d&Num=%d", _listType, _startRecord, Globals.NEW_GAMES_ROW_COUNT);
			
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
						_items = new ArrayList<GameListItem>(len + 20);
					for (int i = 0; i < len; i++) {
						JSONObject o = ary.getJSONObject(i);
						_items.add(new GameListItem(o.getInt("Id"),
								o.getString("Name"),
								o.getString("BoxArt"),
								o.getDouble("Score"),
								o.getInt("Votes")));
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
					ProgressBar progressBar = (ProgressBar)GamesActivity.this.findViewById(R.id.progress);
					TextView progressText = (TextView)GamesActivity.this.findViewById(R.id.progressText);
					
					_progressLayout.setVisibility(View.VISIBLE);
					progressBar.setVisibility(View.GONE);
					progressText.setGravity(Gravity.LEFT);
					progressText.setTextAppearance(_context, android.R.style.TextAppearance_Large_Inverse);
					progressText.setText(Globals.getErrorMsg(_errorType, true));
					_listView.setVisibility(View.GONE);
					//}
				}
				else {
					if (_startRecord == 0) {
						_adapter = new GameListAdapter(_context, _items);
						setListAdapter(_adapter);
					}
					else {
						_adapter.appendItems(_items);
						_adapter.notifyDataSetChanged();
						setIsNextPageLoading(false);
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
