package com.tokkisoft;

import org.json.JSONException;
import org.json.JSONObject;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.HttpRequest;
import com.tokkisoft.droidlib.IErrorCallback;
import com.tokkisoft.droidlib.JsonUtils;
import com.tokkisoft.droidlib.Utils;
import com.tokkisoft.AppScreens.ScreenType;
import com.tokkisoft.AppScreens.TabType;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class PickInfoFragment extends Fragment implements XBLIGCFragment {
	//private static final String TAG = "PickInfoFragment";
	
	private ScrollView _dataLayout;
	private LinearLayout _progressLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
	private TextView _website;
	private TextView _twitterHandle;
	private TextView _bio;
	
	private PickActivityInput _input;
	
	private boolean _isLoading;
	
	public synchronized boolean isLoading() {
		return _isLoading;
	}

	public synchronized void setIsLoading(boolean isLoading) {
		_isLoading = isLoading;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.pick_info_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		_input = ((PickActivity)getActivity()).getPickActivityInput();
		
		View view = getView();
		_dataLayout = (ScrollView)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		_website = (TextView)view.findViewById(R.id.website);
		_twitterHandle = (TextView)view.findViewById(R.id.twitterHandle);
		_bio = (TextView)view.findViewById(R.id.bio);
		
		onRefresh();
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
		return TabType.PICK_INFO;
	}

	@Override
	public void onRefresh() {
		if (!isLoading()) {
			setIsLoading(true);
			
			// hide data/errormsg and show spinner
			showSpinner();
			
			WorkerTask task = new WorkerTask(getActivity(), getView());
	    	task.execute();
		}
	}
	
	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback
    {
    	private Context _context;
    	private View _view;

    	private boolean _isError;
    	private ErrorType _errorType;
    	//private Exception _exception;
    	
    	private int _pickId;
    	private PickInfoOutput _output;
    	
    	public WorkerTask(Context context, View view) {
            _context = context;
            _view = view;
        }
    	
    	// executed on UI thread before creating non-UI thread and calling doInBackground on it.
    	@ Override
        protected void onPreExecute() {
    		_pickId = _input.getPickId();
        }
    	
    	// executed on non-UI thread
		@Override
		protected Void doInBackground(Void... params) {
			HttpRequest request = new HttpRequest(_context, this);
			String url = String.format("http://noms.apphb.com/Xblig/PromoInfo?Promo=%d", _pickId);
			String response = request.httpGet(url, -1);
			
			if (_isError)
				return null;
			
			JSONObject json = JsonUtils.getObj(response);
			if (json == null) {
				_isError = true;
				_errorType = ErrorType.INVALID_RESPONSE_FORMAT;
				return null;
			}
			
			try {
				//String infor = json.getString("Info");
				//byte[] buf = infor.getBytes();
				//String hex = Utils.toHex(buf);
				//infor = infor.replace("\r", "");
				//buf = infor.getBytes();
				//hex = Utils.toHex(buf);
				
				_output = new PickInfoOutput(json.getInt("Id"),
						json.getString("PromotionName"),
						json.getString("PromotionDisplayHeader"),
						json.getString("WebSite"),
						json.getString("TwitterHandle"),
						json.getString("Info"));
			} catch (JSONException e) {
				e.printStackTrace();
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
			if (!isAdded()) // prevent java.lang.IllegalStateException: Fragment GameScreensFragment{44f6de00} not attached to Activity
				return;
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
				// hide spinner and show data
				
				int textColor = getResources().getColor(R.color.text);
				
				final String website = _output.getWebSite();
				if (website == null || website.trim().equals("") || !Utils.isValidUrl(website)) {
					_website.setText("No Website Given");
					_website.setTextColor(textColor);
					_website.setClickable(false);
				}
				else {
					_website.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(Utils.getPrettyUrl(website)) + "</u></font>"));
					_website.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
							startActivity(browserIntent);
						}
					});
				}
				
				final String twitter = _output.getTwitterHandle();
				if (twitter == null || twitter.trim().equals("")) {
					_twitterHandle.setText("No Twitter Given");
					_twitterHandle.setTextColor(textColor);
					_twitterHandle.setClickable(false);
				}
				else {
					_twitterHandle.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(twitter) + "</u></font>"));
					_twitterHandle.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.getTwitterLink(twitter)));
							startActivity(browserIntent);
						}
					});
				}
				
				String bio = _output.getInfo();
				if (bio == null || bio.trim().equals("")) {
					_bio.setText("No information about this pick.");
				}
				else {
					_bio.setText(bio);
				}
				
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
