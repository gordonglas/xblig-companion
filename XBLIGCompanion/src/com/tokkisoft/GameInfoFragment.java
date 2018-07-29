package com.tokkisoft;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IErrorCallback.ErrorType;
import com.tokkisoft.droidlib.cache.ImageLoader;
import com.tokkisoft.droidlib.cache.ImageLoader.ImageLoadedListener;
import com.tokkisoft.droidlib.IObserver;
import com.tokkisoft.droidlib.Subject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class GameInfoFragment extends Fragment implements IObserver {
	private static final String TAG = "GameInfoFragment";
	
	private ImageLoader _imageLoader;
	private ScrollView _dataLayout;
	private LinearLayout _progressLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
	private ImageView _boxArt;
	private TextView _devName;
	private TextView _genre;
	private TextView _msPoints;
	private TextView _releasedOn;
	private TextView _lastUpdatedOn;
	private ImageView _rating0;
	private ImageView _rating1;
	private ImageView _rating2;
	private ImageView _rating3;
	private ImageView _rating4;
	private TextView _votes;
	private TextView _description;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Globals.D) Log.i(TAG, "onCreateView");
		
		return inflater.inflate(R.layout.game_info_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onActivityCreated");
		
		_imageLoader = ImageLoader.getInstance(getActivity());
		
		View view = getView();
		_dataLayout = (ScrollView)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		_boxArt = (ImageView)view.findViewById(R.id.boxArt);
		_devName = (TextView)view.findViewById(R.id.devName);
		_genre = (TextView)view.findViewById(R.id.genre);
		_msPoints = (TextView)view.findViewById(R.id.msPoints);
		_releasedOn = (TextView)view.findViewById(R.id.releasedOn);
		_lastUpdatedOn = (TextView)view.findViewById(R.id.lastUpdatedOn);
		_rating0 = (ImageView)view.findViewById(R.id.rating0);
		_rating1 = (ImageView)view.findViewById(R.id.rating1);
		_rating2 = (ImageView)view.findViewById(R.id.rating2);
		_rating3 = (ImageView)view.findViewById(R.id.rating3);
		_rating4 = (ImageView)view.findViewById(R.id.rating4);
		_votes = (TextView)view.findViewById(R.id.votes);
		_description = (TextView)view.findViewById(R.id.description);
		
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
			
			Game game = ((GameActivityRequestSubject)changedSubject).getGameData();
			final int devId = game.getDeveloperId();
			final String devName = game.getDeveloperName();
			final int genreId = game.getGenreId();
			final String genreName = game.getGenreName();
			
			java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
			
			_devName.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(game.getDeveloperName()) + "</u></font>"));
			_devName.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(getActivity(), DevActivity.class);
					DevActivityInput devInput = new DevActivityInput(devId, devName);
					devInput.populateIntent(intent);
			        getActivity().startActivity(intent);
				}
			});
			_genre.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(game.getGenreName()) + "</u></font>"));
			_genre.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), GamesActivity.class);
			        intent.putExtra(Globals.PACKAGE_PREFIX + "gamesActivityType", GamesActivity.GAMES_ACTIVITY_TYPE.GENRE.ordinal());
			        intent.putExtra(Globals.PACKAGE_PREFIX + "genreId", genreId);
			        intent.putExtra(Globals.PACKAGE_PREFIX + "genreName", genreName);
			        getActivity().startActivity(intent);
				}
			});
			_msPoints.setText(game.getMsPointsCost() + " ms points");
			_releasedOn.setText("Released On: " + dateFormat.format(game.getReleasedOn()));
			_lastUpdatedOn.setText("Last Updated On: " + dateFormat.format(game.getUpdatedOn()));
			_votes.setText("Votes: " + game.getVotes());
			_description.setText(game.getInfo());
			
			setStarImages(game.getScore());
			
			final String boxArtUrl = game.getBoxArt();
			
			final Bitmap bmp = _imageLoader.getBitmap(context, boxArtUrl, new ImageLoadedListener() {
				@Override
				public void onImageLoaded(Bitmap bitmap) {
					if (bitmap != null) {
						if (Globals.D) Log.i(TAG, "onImageLoaded: " + boxArtUrl);
						_boxArt.setImageBitmap(bitmap);
					}
				}
			});
	        
	        if (bmp != null)
	        	_boxArt.setImageBitmap(bmp);
			
			_progressLayout.setVisibility(View.GONE);
			_dataLayout.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onSubjectReloading(Subject reloadingSubject) {
		showSpinner();
	}
	
	private void setStarImages(double score) {
		// round up to nearest .5
		double roundBy = 0.5;
		double rounded = roundBy * Math.round(score/roundBy);
		
		//if (Globals.D) Log.i(TAG, String.format("score:%f rounded:%f", score, rounded));
		
		int intPart = (int)rounded;
		double floatPart = rounded - intPart;
		
		//if (Globals.D) Log.i(TAG, String.format("intPart:%d floatPart:%f", intPart, floatPart));
		
		int starCount = 0;
		ImageView view = null;
		
		while (starCount < 5) {
			switch (starCount) {
				case 0:
					view = _rating0;
					break;
				case 1:
					view = _rating1;
					break;
				case 2:
					view = _rating2;
					break;
				case 3:
					view = _rating3;
					break;
				case 4:
					view = _rating4;
					break;
			}
			
			if (intPart > 0) {
				view.setImageResource(R.drawable.rating_full);
				intPart--;
			}
			else if (floatPart == roundBy) {
				view.setImageResource(R.drawable.rating_half);
				floatPart = 0;
			}
			else {
				view.setImageResource(R.drawable.rating_none);
			}
			
			starCount++;
		}
	}
}
