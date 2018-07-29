package com.tokkisoft;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.cache.ImageLoader;
import com.tokkisoft.droidlib.cache.ImageLoader.ImageLoadedListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageViewerFragment extends Fragment {
	private static final String TAG = "ImageViewerFragment";
	
	private ImageLoader _imageLoader;
	private ImageView _imageView;
	//private int _fragmentPosition;
	private String _imageUrl;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onCreate");
		
		Bundle args = this.getArguments();
		
		if (args == null)
			return;
		
		//_fragmentPosition = args.getInt("fragmentPos");
		_imageUrl = args.getString("imageUrl");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Globals.D) Log.i(TAG, "onCreateView");
		
		return inflater.inflate(R.layout.image_viewer_fragment, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (Globals.D) Log.i(TAG, "onActivityCreated");
		
		Context context = getActivity();
		
		_imageLoader = ImageLoader.getInstance(context);
		
		View view = getView();
		_imageView = (ImageView)view.findViewById(R.id.imageView);
		
		final String imageUrl = _imageUrl;
		
		final Bitmap bmp = _imageLoader.getBitmap(context, imageUrl, new ImageLoadedListener() {
			@Override
			public void onImageLoaded(Bitmap bitmap) {
				if (bitmap != null) {
					if (Globals.D) Log.i(TAG, "onImageLoaded: " + imageUrl);
					_imageView.setImageBitmap(bitmap);
				}
			}
		});
        
        if (bmp != null)
        	_imageView.setImageBitmap(bmp);
	}
}
