package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IErrorCallback.ErrorType;
import com.tokkisoft.droidlib.IObserver;
import com.tokkisoft.droidlib.Subject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DevGamesFragment extends ListFragment implements IObserver {
	private static final String TAG = "DevGamesFragment";
	
	private FrameLayout _dataLayout;
	private LinearLayout _progressLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
	private DevGamesListAdapter _adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dev_games_fragment, container, false);
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		View view = getView();
		_dataLayout = (FrameLayout)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		
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
			ArrayList<Game> games = dev.getGames();
			
			// reload list
			if (_adapter == null) {
				_adapter = new DevGamesListAdapter(context, games);
				setListAdapter(_adapter);
			}
			else {
				_adapter.setGames(games);
				_adapter.notifyDataSetChanged();
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
