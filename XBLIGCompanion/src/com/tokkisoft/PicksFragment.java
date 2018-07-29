package com.tokkisoft;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.HttpRequest;
import com.tokkisoft.droidlib.JsonUtils;
import com.tokkisoft.droidlib.IErrorCallback;
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

public class PicksFragment extends ListFragment implements XBLIGCFragment {
	private BasicListItem[] _items;
	private ScreenType _screenType;
	private TabType _tabType;
	private boolean _isLoading;
	
	public synchronized boolean isLoading() {
		return _isLoading;
	}

	public synchronized void setIsLoading(boolean isLoading) {
		_isLoading = isLoading;
	}
	
	@Override
	public ScreenType getScreenType() {
		return _screenType;
	}

	@Override
	public TabType getTabType() {
		return _tabType;
	}
	
	@Override
	public void onRefresh() {
		if (!isLoading()) {
			setIsLoading(true);
			
			//if (Globals.D) Log.i("PicksFragment", "onRefresh()");
			
			if (!isAdded()) // prevent java.lang.IllegalStateException: Fragment GameScreensFragment{44f6de00} not attached to Activity
				return;
			
			LinearLayout progressLayout = (LinearLayout)getView().findViewById(R.id.progressLayout);
	        FrameLayout mainPagerFrameLayout = (FrameLayout)getView().findViewById(R.id.dataLayout);
	        ProgressBar progressBar = (ProgressBar)getView().findViewById(R.id.progress);
			TextView progressText = (TextView)getView().findViewById(R.id.progressText);
	        
			progressText.setGravity(Gravity.CENTER_HORIZONTAL);
			progressText.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large_Inverse);
			progressText.setText("Retrieving data...");
			progressText.setTextColor(getResources().getColor(R.color.text));
	        progressLayout.setVisibility(View.VISIBLE);
	        progressBar.setVisibility(View.VISIBLE);
	    	mainPagerFrameLayout.setVisibility(View.GONE);
	    	
	    	WorkerTask task = new WorkerTask(getActivity(), getView());
	    	task.execute();
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //int screenTypeOrdinal = savedInstanceState.getInt("ScreenType");
        //int tabTypeOrdinal = savedInstanceState.getInt("TabType");
        //_screenType = ScreenType.values()[screenTypeOrdinal];
        //_tabType = TabType.values()[tabTypeOrdinal];
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.picks_fragment, container, false);
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		onRefresh();
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		if (Globals.D) Log.i("PicksFragment", "Item clicked: " + id);
        // go to PickActivity
        BasicListItem item = _items[position];
        Intent intent = new Intent(getActivity(), PickActivity.class);
        PickActivityInput input = new PickActivityInput(item.getId(), item.getName());
        input.populateIntent(intent);
        startActivity(intent);
    }
	
	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback
    {
    	private Context _context;
    	private View _view;

    	private boolean _isError;
    	private ErrorType _errorType;
    	//private Exception _exception;
    	
    	public WorkerTask(Context context, View view) {
            _context = context;
            _view = view;
        }
    	
    	// executed on UI thread before creating non-UI thread and calling doInBackground on it.
    	@ Override
        protected void onPreExecute() {
        }
    	
    	// executed on non-UI thread
		@Override
		protected Void doInBackground(Void... params) {
			//_strings = new String[3];
			//_strings[0] = "I";
			//_strings[1] = "<3";
			//_strings[2] = "Android";
			
			HttpRequest request = new HttpRequest(_context, this);
			
			// test timeout
			//String response = request.httpGet("http://192.168.0.105/TestWebService/Service1.asmx/PromoList", 0);
			
			String response = request.httpGet("http://noms.apphb.com/Xblig/PromoList", -1);
			
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
						_items = new BasicListItem[len];
					for (int i = 0; i < len; i++) {
						JSONObject o = ary.getJSONObject(i);
						_items[i] = new BasicListItem(o.getInt("Id"),
								o.getString("PromotionName"),
								o.getString("PromotionDisplayHeader"));
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
		
		// executed on UI thread after doInBackground returns
		@ Override
	    protected void onPostExecute(Void result) {
			LinearLayout progressLayout = (LinearLayout)_view.findViewById(R.id.progressLayout);
            FrameLayout mainPagerFrameLayout = (FrameLayout)_view.findViewById(R.id.dataLayout);
            
			if (_isError) {
				ProgressBar progressBar = (ProgressBar)_view.findViewById(R.id.progress);
				TextView progressText = (TextView)_view.findViewById(R.id.progressText);
				
				progressLayout.setVisibility(View.VISIBLE);
				progressBar.setVisibility(View.GONE);
				progressText.setGravity(Gravity.LEFT);
				progressText.setTextAppearance(_context, android.R.style.TextAppearance_Large_Inverse);
				progressText.setText(Globals.getErrorMsg(_errorType, true));
				mainPagerFrameLayout.setVisibility(View.GONE);
			}
			else {
				setListAdapter(new BasicListAdapter(_context, _items));
				
				//setListAdapter(new ArrayAdapter<String>(_context,
	        	//		android.R.layout.simple_list_item_1, _strings));
				
	            progressLayout.setVisibility(View.GONE);
	        	mainPagerFrameLayout.setVisibility(View.VISIBLE);
			}
        	setIsLoading(false);
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
