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

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

// removed the OnScrollListener, since it looks like picks only have max of 10 games each anyway.
public class PickGamesFragment extends ListFragment implements XBLIGCFragment /*, OnScrollListener*/ {
	private static final String TAG = "PickGamesFragment"; 
	
	private LinearLayout _progressLayout;
	private FrameLayout _dataLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
	//private ListView _listView;
	private GameListAdapter _adapter;
	private int _startRecord;
	private WorkerTask _task;
	private boolean _isReloading;
	private boolean _isNextPageLoading;
	
	private PickActivityInput _input;
	
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
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.pick_game_fragment, container, false);
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		_input = ((PickActivity)getActivity()).getPickActivityInput();
		
		View view = getView();
		_dataLayout = (FrameLayout)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		
		//_listView = (ListView)view.findViewById(android.R.id.list);
		//_listView.setOnScrollListener(this);
		
		// hide data/errormsg and show spinner
		showSpinner();
		
		_startRecord = 0;
		
		_task = new WorkerTask(getActivity(), false);
    	_task.execute();
	}
	
	private void showSpinner() {
		if (!isAdded()) // prevent java.lang.IllegalStateException: Fragment GameScreensFragment{44f6de00} not attached to Activity
			return;
		_progressText.setGravity(Gravity.CENTER_HORIZONTAL);
		_progressText.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large_Inverse);
		_progressText.setText("Retrieving data...");
		_progressText.setTextColor(getResources().getColor(R.color.text));
        _progressLayout.setVisibility(View.VISIBLE);
        _progressBar.setVisibility(View.VISIBLE);
        _dataLayout.setVisibility(View.GONE);
	}
	
	@Override
	public ScreenType getScreenType() {
		return ScreenType.PICK;
	}

	@Override
	public TabType getTabType() {
		return TabType.PICK_GAMES;
	}
	
	@Override
	public void onRefresh() {
		if (!isReloading()) {
			setIsReloading(true);
			
			// hide data/errormsg and show spinner
			showSpinner();
			
			_startRecord = 0;
			
			_task = new WorkerTask(getActivity(), true);
	    	_task.execute();
		}
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		// id is what we return in GameListAdaptor::getItemId(position)
		if (Globals.D) Log.i(TAG, "Item clicked: " + id);
        super.onListItemClick(l, v, position, id);
        
        // pass data of selected game to GameActivity
        GameListItem item = (GameListItem)_adapter.getItem(position);
        Intent intent = new Intent(getActivity(), GameActivity.class);
        item.populateIntent(intent);
        startActivity(intent);
    }

	/*
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		//if (Globals.D) Log.i(TAG, String.format("LOADMORE: SCROLLPOS: firstVisibleItem:%d visibleItemCount:%d totalItemCount:%d", firstVisibleItem, visibleItemCount, totalItemCount));
		boolean loadMore = totalItemCount > 0 && firstVisibleItem + visibleItemCount >= totalItemCount;

        if (loadMore) {
        	if (!isNextPageLoading()) {
        		setIsNextPageLoading(true);
	        	if (Globals.D) Log.i(TAG, String.format("LOADMORE: firstVisibleItem:%d visibleItemCount:%d totalItemCount:%d", firstVisibleItem, visibleItemCount, totalItemCount));
	        	
	        	_startRecord += Globals.NEW_GAMES_ROW_COUNT;
	        	
	        	_task = new WorkerTask(getActivity(), false);
		    	_task.execute();
        	}
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}
	*/

	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback {
		private Context _context;
		private ArrayList<GameListItem> _items;
		private boolean _isReloading;
		
		private boolean _isError;
    	private ErrorType _errorType;
    	//private Exception _exception;
		
    	private int _pickId;
    	
		public WorkerTask(Context context, boolean isReloading) {
			_context = context;
			_isReloading = isReloading;
			_pickId = _input.getPickId();
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
			
			String url = String.format("http://noms.apphb.com/Xblig/PromotedList?Promo=%d&Skip=%d&Num%d", _pickId, _startRecord, 30); // request more just in case one of the picks has more than 10 in the future.
			
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
					_progressLayout.setVisibility(View.VISIBLE);
					_progressBar.setVisibility(View.GONE);
					_progressText.setGravity(Gravity.LEFT);
					_progressText.setTextAppearance(_context, android.R.style.TextAppearance_Large_Inverse);
					_progressText.setText(Globals.getErrorMsg(_errorType, true));
					_dataLayout.setVisibility(View.GONE);
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
					_dataLayout.setVisibility(View.VISIBLE);
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
