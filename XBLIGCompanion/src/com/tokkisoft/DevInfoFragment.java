package com.tokkisoft;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IObserver;
import com.tokkisoft.droidlib.Subject;
import com.tokkisoft.droidlib.IErrorCallback.ErrorType;
import com.tokkisoft.droidlib.Utils;

public class DevInfoFragment extends Fragment implements IObserver {
	private static final String TAG = "DevInfoFragment";
	
	private ScrollView _dataLayout;
	private LinearLayout _progressLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
	private TextView _website;
	private TextView _twitterHandle;
	private TextView _facebookPage;
	private TextView _youtubeChannel;
	private TextView _bio;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dev_info_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		View view = getView();
		_dataLayout = (ScrollView)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		_website = (TextView)view.findViewById(R.id.website);
		_twitterHandle = (TextView)view.findViewById(R.id.twitterHandle);
		_facebookPage = (TextView)view.findViewById(R.id.facebookPage);
		_youtubeChannel = (TextView)view.findViewById(R.id.youtubeChannel);
		_bio = (TextView)view.findViewById(R.id.bio);
		
		// hide data/errormsg and show spinner
		showSpinner();
		
		DevActivity activity = (DevActivity)getActivity();
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
	
	@Override
	public void onSubjectUpdated(Subject changedSubject, ErrorType errorType) {
		if (!isAdded()) // prevent java.lang.IllegalStateException: Fragment GameScreensFragment{44f6de00} not attached to Activity
			return;
		Context context = getActivity();
		if (context == null)
		{
			if (Globals.D) Log.e(TAG, "context null!");
			return;
		}
		
		if (errorType != ErrorType.NO_ERROR) {
			// show error msg
			_progressLayout.setVisibility(View.VISIBLE);
			_progressBar.setVisibility(View.GONE);
			_progressText.setGravity(Gravity.LEFT);
			_progressText.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
			_progressText.setText(Globals.getErrorMsg(errorType, true));
			_dataLayout.setVisibility(View.GONE);
		}
		else {
			// hide spinner and show data
			
			DeveloperInfo dev = ((DevActivityRequestSubject)changedSubject).getDevInfo();
			DeveloperItem devItem = dev.getDevItem();
			
			int textColor = getResources().getColor(R.color.text);
			
			final String website = devItem.getWebsite();
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
			
			final String twitter = devItem.getTwitterHandle();
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
			
			final String facebook = devItem.getFacebookPage(); 
			if (facebook == null || facebook.trim().equals("") || !Utils.isValidUrl(facebook)) {
				_facebookPage.setText("No Facebook Given");
				_facebookPage.setTextColor(textColor);
				_facebookPage.setClickable(false);
			}
			else {
				_facebookPage.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(devItem.getName()) + "</u></font>"));
				_facebookPage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebook));
						startActivity(browserIntent);
					}
				});
			}
			
			final String youtube = devItem.getYoutubeChannel(); 
			if (youtube == null || youtube.trim().equals("") || !Utils.isValidUrl(youtube)) {
				_youtubeChannel.setText("No Youtube Given");
				_youtubeChannel.setTextColor(textColor);
				_youtubeChannel.setClickable(false);
			}
			else {
				_youtubeChannel.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(devItem.getName()) + "</u></font>"));
				_youtubeChannel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youtube));
						startActivity(browserIntent);
					}
				});
			}
			
			String bio = devItem.getBio();
			if (bio == null || bio.trim().equals("")) {
				_bio.setText("No information about this developer.");
			}
			else {
				_bio.setText(bio);
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
