package com.tokkisoft;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.cache.ImageLoader;
import com.tokkisoft.droidlib.cache.ImageLoader.ImageLoadedListener;

// http://www.vogella.com/articles/AndroidListView/article.html
// http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/view/List14.html

public class GameListAdapter extends BaseAdapter {
	private static final String TAG = "GameListAdapter";
	
	private Context _context;
	private LayoutInflater _inflater;
	private ArrayList<GameListItem> _items;
	private ImageLoader _imageLoader;
	
	public GameListAdapter(Context context, ArrayList<GameListItem> items)
	{
		_context = context;
		_inflater = LayoutInflater.from(context);
		_items = items;
		_imageLoader = ImageLoader.getInstance(context);
	}
	
	public void appendItems(ArrayList<GameListItem> items)
	{
		_items.addAll(items);
	}
	
	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public Object getItem(int position) {
		// Since the data comes from an array, just returning the index is
        // sufficient to get at the data. If we were using a more complex data
        // structure, we would return whatever object represents one row in the
        // list.
		//return position;
		return _items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return _items.get(position).getId();
	}

	static class ViewHolder {
		ImageView boxArt;
        TextView name;
        ImageView rating0;
        ImageView rating1;
        ImageView rating2;
        ImageView rating3;
        ImageView rating4;
        TextView votes;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// A ViewHolder keeps references to children views to avoid unnecessary calls
        // to inflate() and  findViewById() on each row.
        final ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to re-inflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = _inflater.inflate(R.layout.game_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.boxArt = (ImageView) convertView.findViewById(R.id.boxArt);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.rating0 = (ImageView) convertView.findViewById(R.id.rating0);
            holder.rating1 = (ImageView) convertView.findViewById(R.id.rating1);
            holder.rating2 = (ImageView) convertView.findViewById(R.id.rating2);
            holder.rating3 = (ImageView) convertView.findViewById(R.id.rating3);
            holder.rating4 = (ImageView) convertView.findViewById(R.id.rating4);
            holder.votes = (TextView) convertView.findViewById(R.id.votes);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the view's objects
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        GameListItem item = _items.get(position);
        holder.name.setText(item.getName());
        holder.votes.setText("Votes: " + item.getVotes());
        holder.boxArt.setImageResource(R.drawable.boxart);
        
        setStarImages(holder, item.getScore());
        
        final String url = item.getBoxArt();
        
        //if (Globals.D) Log.i(TAG, "Position:" + position + " URL:" + url);
        
        final Bitmap bmp = _imageLoader.getBitmap(_context, url, new ImageLoadedListener() {
			@Override
			public void onImageLoaded(Bitmap bitmap) {
				if (bitmap != null) {
					if (Globals.D) Log.i(TAG, "onImageLoaded: " + url);
					holder.boxArt.setImageBitmap(bitmap);
					notifyDataSetChanged();
				}
			}
		});
        
        if (bmp != null)
        	holder.boxArt.setImageBitmap(bmp);
        
        return convertView;
	}

	private void setStarImages(ViewHolder holder, double score) {
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
					view = holder.rating0;
					break;
				case 1:
					view = holder.rating1;
					break;
				case 2:
					view = holder.rating2;
					break;
				case 3:
					view = holder.rating3;
					break;
				case 4:
					view = holder.rating4;
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
