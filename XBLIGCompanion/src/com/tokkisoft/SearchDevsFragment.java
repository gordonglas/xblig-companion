package com.tokkisoft;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IErrorCallback.ErrorType;
import com.tokkisoft.droidlib.HttpRequest;
import com.tokkisoft.droidlib.IErrorCallback;
import com.tokkisoft.droidlib.IObserver;
import com.tokkisoft.droidlib.JsonUtils;
import com.tokkisoft.droidlib.Subject;

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
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class SearchDevsFragment extends ListFragment implements IObserver, OnScrollListener {
	private static final String TAG = "SearchDevsFragment";

	private BasicListAdapter _adapter;
	private FrameLayout _dataLayout;
	private LinearLayout _progressLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
//	private boolean _isReloading;
	private boolean _isNextPageLoading;
	private String _prevSearchString = "";
	private int _startRecord;
	private WorkerTask _task;
	private ListView _listView;
	
//	// synchronization for reload button
//	public synchronized boolean isReloading() {
//		return _isReloading;
//	}
//
//	public synchronized void setIsReloading(boolean isReloading) {
//		_isReloading = isReloading;
//	}
	
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
		return inflater.inflate(R.layout.picks_fragment, container, false);
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		View view = getView();
		_dataLayout = (FrameLayout)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		
		_listView = (ListView)view.findViewById(android.R.id.list);
		_listView.setOnScrollListener(this);
		
		// hide data/errormsg and show spinner
		//showSpinner();
		
		showNothing();
		
		SearchActivity activity = (SearchActivity)getActivity();
		activity.attachObserver(this);
	}
	
	private void showNothing() {
		_progressLayout.setVisibility(View.GONE);
		_dataLayout.setVisibility(View.GONE);
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
	public void onSubjectUpdated(Subject changedSubject, ErrorType errorType) {
		Context context = getActivity();
		if (context == null)
		{
			if (Globals.D) Log.e(TAG, "context null!");
			return;
		}
		
//		if (errorType != ErrorType.NO_ERROR) {
//			// show error msg
//			_progressLayout.setVisibility(View.VISIBLE);
//			_progressBar.setVisibility(View.GONE);
//			_progressText.setGravity(Gravity.LEFT);
//			_progressText.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
//			_progressText.setText(Globals.getErrorMsg(errorType, false, true));
//			_dataLayout.setVisibility(View.GONE);
//		}
//		else {
		
		String searchString = ((SearchActivityRequestSubject)changedSubject).getSearchString().trim();
		
		if (!searchString.equals(_prevSearchString))
		{
			_prevSearchString = searchString;
			
			// if request is currently running, cancel it
			if (_task != null)
				_task.cancel(true);
			
			// clear the dev list
			if (_adapter != null)
			{
				_adapter.clear();
				_adapter.notifyDataSetChanged();
				if (Globals.D) Log.i(TAG, "adapter cleared");
			}
			
			if (searchString.equals(""))
			{
				showNothing();
			}
			else
			{
				if (Globals.D) Log.i(TAG, "adapter will be populated");
				showSpinner();
				
				_startRecord = 0;
				
				_task = new WorkerTask(getActivity(), searchString);
				_task.execute();
			}
		}
		
//		}
	}

	@Override
	public void onSubjectReloading(Subject reloadingSubject) {
		showSpinner();
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
	        	
	        	_task = new WorkerTask(getActivity(), _prevSearchString);
		    	_task.execute();
        	}
        }
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		if (Globals.D) Log.i(TAG, "Item clicked: " + id);
        super.onListItemClick(l, v, position, id);
        
        // go to DevActivity
        BasicListItem item = _adapter.getItem(position);
        Intent intent = new Intent(getActivity(), DevActivity.class);
        DevActivityInput input = new DevActivityInput(item.getId(), item.getName());
        input.populateIntent(intent);
        startActivity(intent);
    }
	
	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback {
		private Context _context;
		private ArrayList<BasicListItem> _items;
		private String _searchString;
		
		private boolean _isError;
    	private ErrorType _errorType;
    	//private Exception _exception;
		
		public WorkerTask(Context context, String searchString) {
			_context = context;
			_searchString = searchString;
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			HttpRequest request = new HttpRequest(_context, this);
			
			String encodedSearchString;
			try {
				encodedSearchString = URLEncoder.encode(_searchString, "utf-8");
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				return null;
			}
			
			String url = String.format("http://noms.apphb.com/Xblig/SearchDevList?Term=%s&Skip=%d&Num=%d", encodedSearchString, _startRecord, Globals.NEW_GAMES_ROW_COUNT);
			
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
						_items = new ArrayList<BasicListItem>(len);
					for (int i = 0; i < len; i++) {
						JSONObject o = ary.getJSONObject(i);
						String name = o.getString("Name");
						_items.add(new BasicListItem(o.getInt("Id"), name, name));
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
			if (_isError) {
				//if (_startRecord == 0)
				//{
				_progressLayout.setVisibility(View.VISIBLE);
				_progressBar.setVisibility(View.GONE);
				_progressText.setGravity(Gravity.LEFT);
				_progressText.setTextAppearance(_context, android.R.style.TextAppearance_Large_Inverse);
				_progressText.setText(Globals.getErrorMsg(_errorType, false, true));
				_dataLayout.setVisibility(View.GONE);
				//}
			}
			else {
				if (_startRecord == 0) {
					if (Globals.D) Log.i(TAG, "adapter populating");
					_adapter = new BasicListAdapter(_context, _items);
					setListAdapter(_adapter);
				}
				else {
					_adapter.appendItems(_items);
					_adapter.notifyDataSetChanged();
				}
				
				setIsNextPageLoading(false);
				
				_progressLayout.setVisibility(View.GONE);
				_dataLayout.setVisibility(View.VISIBLE);
			}
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
