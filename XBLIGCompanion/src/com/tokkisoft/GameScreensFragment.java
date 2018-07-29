package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.IErrorCallback.ErrorType;
import com.tokkisoft.droidlib.cache.ImageLoader;
import com.tokkisoft.droidlib.cache.ImageLoader.ImageLoadedListener;
import com.tokkisoft.droidlib.IObserver;
import com.tokkisoft.droidlib.Subject;
import com.tokkisoft.droidlib.Utils;
import com.tokkisoft.Game.GameImage;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameScreensFragment extends Fragment implements IObserver {
	private static final String TAG = "GameScreensFragment";
	
	// changing this also affects max # of fragments in memory at a time in ImageViewerActivity
	public static final int MAX_SCREENSHOTS_SHOWN = 4;
	
	private ImageLoader _imageLoader;
	private FrameLayout _dataLayout;
	private LinearLayout _progressLayout;
	private ProgressBar _progressBar;
	private TextView _progressText;
	//private ImageView _screenshot01;
	private RelativeLayout _screensLayout;
	private int _lastImageId;
	private ArrayList<String> _imageUrls;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Globals.D) Log.i(TAG, "onCreateView");
		
		return inflater.inflate(R.layout.game_screens_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onActivityCreated");
		
		_imageLoader = ImageLoader.getInstance(getActivity());
		
		View view = getView();
		_dataLayout = (FrameLayout)view.findViewById(R.id.dataLayout);
		_progressLayout = (LinearLayout)view.findViewById(R.id.progressLayout);
        _progressBar = (ProgressBar)view.findViewById(R.id.progress);
		_progressText = (TextView)view.findViewById(R.id.progressText);
		//_screenshot01 = (ImageView)view.findViewById(R.id.screenshot01);
		_screensLayout = (RelativeLayout)view.findViewById(R.id.screensLayout);
		
		_lastImageId = 0;
		
		// hide data/errormsg and show spinner
		showSpinner();
		
		GameActivity activity = (GameActivity)getActivity();
		activity.attachObserver(this);
	}

	private void showSpinner() {
		if (!isAdded()) // prevent java.lang.IllegalStateException: Fragment GameScreensFragment{44f6de00} not attached to Activity
			return;
		_screensLayout.removeAllViews(); // remove any screen-shots
		_lastImageId = 0;
		
		_progressText.setGravity(Gravity.CENTER_HORIZONTAL);
		_progressText.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large_Inverse);
		_progressText.setText("Retrieving data...");
		_progressText.setTextColor(getResources().getColor(R.color.text));
        _progressLayout.setVisibility(View.VISIBLE);
        _progressBar.setVisibility(View.VISIBLE);
        _dataLayout.setVisibility(View.GONE);
	}
	
	public void addImage(Bitmap bmp) {
		try {
			if (!isAdded()) // prevent java.lang.IllegalStateException: Fragment GameScreensFragment{44f6de00} not attached to Activity
				return;
			ImageView view = new ImageView(getActivity());
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			if (_lastImageId > 0)
				layoutParams.addRule(RelativeLayout.BELOW, _lastImageId);
			view.setLayoutParams(layoutParams);
			Resources resources = getResources();
			view.setPadding(0, 0, 0, Utils.dpToPixels(resources, 8));
			view.setAdjustViewBounds(true);
			//view.setMaxHeight(Utils.dpToPixels(resources, 200));
			view.setScaleType(ScaleType.CENTER_INSIDE);
			view.setImageBitmap(bmp);
			final int imagePosition = _lastImageId;
			view.setId(_lastImageId + 1);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// save image urls in Globals so they can be grabbed by the ImageViewerActivity
					Globals.setImageUrls(_imageUrls);
					Intent intent = new Intent(getActivity(), ImageViewerActivity.class);
					intent.putExtra(Globals.PACKAGE_PREFIX + "imagePosition", imagePosition);
					getActivity().startActivity(intent);
				}
			});
			_screensLayout.addView(view);
			_lastImageId = view.getId();
			//if (Globals.D) Log.i(TAG, "lastImageId: " + _lastImageId);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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
			
			Game game = ((GameActivityRequestSubject)changedSubject).getGameData();
			ArrayList<GameImage> gameImages = game.getGameImages();
			
			int numScreenshots = 0;
			
			// save image links so they can be passed to ImageViewerActivity when user taps an image.
			if (gameImages.size() > 0) {
				if (_imageUrls == null)
					_imageUrls = new ArrayList<String>(gameImages.size());
				else {
					_imageUrls.clear();
				}
				for (GameImage gameImage : gameImages) {
					if (gameImage.getImageType() == 1) {
						String link = gameImage.getImageLink();
						_imageUrls.add(link);
						
						// protect from too much memory usage, since we aren't using list items here
				        numScreenshots++;
				        if (numScreenshots >= MAX_SCREENSHOTS_SHOWN)
				        	break;
					}
				}
			}
			
			numScreenshots = 0;
			for (GameImage gameImage : gameImages) {
				if (gameImage.getImageType() == 1) {
					final String imageUrl = gameImage.getImageLink();
					
					final Bitmap bmp = _imageLoader.getBitmap(context, imageUrl, new ImageLoadedListener() {
						@Override
						public void onImageLoaded(Bitmap bitmap) {
							if (bitmap != null) {
								if (Globals.D) Log.i(TAG, "onImageLoaded: " + imageUrl);
								//_screenshot01.setImageBitmap(bitmap);
								addImage(bitmap);
							}
						}
					});
			        
			        if (bmp != null) {
			        	//_screenshot01.setImageBitmap(bmp);
			        	addImage(bmp);
			        }
			        
			        // protect from too much memory usage, since we aren't using list items here
			        numScreenshots++;
			        if (numScreenshots >= MAX_SCREENSHOTS_SHOWN)
			        	break;
				}
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
