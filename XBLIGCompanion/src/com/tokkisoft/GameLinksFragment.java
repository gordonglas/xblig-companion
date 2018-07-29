package com.tokkisoft;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IObserver;
import com.tokkisoft.droidlib.Subject;
import com.tokkisoft.droidlib.Utils;
import com.tokkisoft.droidlib.IErrorCallback.ErrorType;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RadioGroup.LayoutParams;

public class GameLinksFragment extends Fragment implements IObserver {
	private static final String TAG = "GameLinksFragment";
	
	//private ImageLoader _imageLoader;
	private FrameLayout _dataLayout;
	private LinearLayout _progressLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
	//private TextView _textLink1;
	private RelativeLayout _linksLayout;
	private int _lastLinkId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Globals.D) Log.i(TAG, "onCreateView");
		
		return inflater.inflate(R.layout.game_links_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onActivityCreated");
		
		//_imageLoader = ImageLoader.getInstance(getActivity());
		
		View view = getView();
		_dataLayout = (FrameLayout)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		//_textLink1 = (TextView)view.findViewById(R.id.textlink1);
		_linksLayout = (RelativeLayout)view.findViewById(R.id.linksLayout);
		
		_lastLinkId = 0;
		
		// hide data/errormsg and show spinner
		showSpinner();
		
		GameActivity activity = (GameActivity)getActivity();
		activity.attachObserver(this);
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
	
	public void addLink(String linkText, final String linkUrl) {
		if (!isAdded()) // prevent java.lang.IllegalStateException: Fragment GameScreensFragment{44f6de00} not attached to Activity
			return;
		Context context = getActivity();
		Resources resources = getResources();
		TextView view = new TextView(context);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		layoutParams.topMargin = Utils.dpToPixels(resources, 10);
		if (_lastLinkId > 0)
			layoutParams.addRule(RelativeLayout.BELOW, _lastLinkId);
		view.setLayoutParams(layoutParams);
		//view.setPadding(0, 0, 0, Utils.dpToPixels(resources, 8));
		view.setMaxLines(1);
		view.setTextAppearance(context, android.R.attr.textAppearanceSmall);
		view.setTextColor(resources.getColor(R.color.text));
		view.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(linkText) + "</u></font>"));
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
				startActivity(browserIntent);
			}
		});
		view.setId(_lastLinkId + 1);
		_linksLayout.addView(view);
		_lastLinkId = view.getId();
		//if (Globals.D) Log.i(TAG, "lastImageId: " + _lastLinkId);
	}
	
	@Override
	public void onSubjectUpdated(Subject changedSubject, ErrorType errorType) {
		Context context = getActivity();
		
		if (errorType != ErrorType.NO_ERROR) {
			_progressLayout.setVisibility(View.VISIBLE);
			_progressBar.setVisibility(View.GONE);
			_progressText.setGravity(Gravity.LEFT);
			_progressText.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
			_progressText.setText(Globals.getErrorMsg(errorType, true));
			_dataLayout.setVisibility(View.GONE);
		}
		else {
			// hide spinner and show data
			
			// clear imageViews under the relativeLayout
			_linksLayout.removeAllViews();
			
			Game game = ((GameActivityRequestSubject)changedSubject).getGameData();
			String marketPlaceLink = game.getMarketPlaceLink();
			String yahooLink = game.getYahooLink();
			if (yahooLink != null && !yahooLink.trim().equals("")) {
				addLink("View YouTube Trailer", yahooLink);
			}
			if (marketPlaceLink != null && !marketPlaceLink.trim().equals("")) {
				addLink("View on Marketplace", marketPlaceLink);
			}
			
			_progressLayout.setVisibility(View.GONE);
			_dataLayout.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onSubjectReloading(Subject reloadingSubject) {
		showSpinner();
	}
}
