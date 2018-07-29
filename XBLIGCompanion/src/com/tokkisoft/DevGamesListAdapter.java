package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.cache.ImageLoader;
import com.tokkisoft.droidlib.cache.ImageLoader.ImageLoadedListener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DevGamesListAdapter extends BaseAdapter {
	private static final String TAG = "DevGamesListAdapter";
	
	private Context _context;
	private LayoutInflater _inflater;
	private ArrayList<Game> _items;
	private ImageLoader _imageLoader;
	
	public DevGamesListAdapter(Context context, ArrayList<Game> items)
	{
		_context = context;
		_inflater = LayoutInflater.from(context);
		_imageLoader = ImageLoader.getInstance(context);
		setGames(items);
	}
	
	public void setGames(ArrayList<Game> items) {
		_items = items;
	}
	
	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public boolean isEnabled(int position) {
		return false; // so that the cell's aren't clickable
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
        TextView gameName;
        TextView genre;
        TextView msPoints;
        TextView releasedOn;
        TextView lastUpdatedOn;
        ImageView rating0;
        ImageView rating1;
        ImageView rating2;
        ImageView rating3;
        ImageView rating4;
        TextView votes;
        TextView description;
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
            convertView = _inflater.inflate(R.layout.dev_games_list_item, null);

            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.boxArt = (ImageView) convertView.findViewById(R.id.boxArt);
            holder.gameName = (TextView) convertView.findViewById(R.id.gameName);
            holder.genre = (TextView) convertView.findViewById(R.id.genre);
            holder.msPoints = (TextView) convertView.findViewById(R.id.msPoints);
            holder.releasedOn = (TextView) convertView.findViewById(R.id.releasedOn);
            holder.lastUpdatedOn = (TextView) convertView.findViewById(R.id.lastUpdatedOn);
            holder.rating0 = (ImageView) convertView.findViewById(R.id.rating0);
            holder.rating1 = (ImageView) convertView.findViewById(R.id.rating1);
            holder.rating2 = (ImageView) convertView.findViewById(R.id.rating2);
            holder.rating3 = (ImageView) convertView.findViewById(R.id.rating3);
            holder.rating4 = (ImageView) convertView.findViewById(R.id.rating4);
            holder.votes = (TextView) convertView.findViewById(R.id.votes);
            holder.description = (TextView) convertView.findViewById(R.id.description);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the view's objects
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        Game item = _items.get(position);
        final int gameId = item.getId();
        final String gameName = item.getName();
        final String boxArt = item.getBoxArt();
        final double score = item.getScore();
        final int votes = item.getVotes();
        final int genreId = item.getGenreId();
        final String genreName = item.getGenreName();
        
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(_context);
        
        holder.boxArt.setImageResource(R.drawable.boxart);
        holder.gameName.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(item.getName()) + "</u></font>"));
        holder.gameName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(_context, GameActivity.class);
				GameListItem gameListItem = new GameListItem(gameId, gameName, boxArt, score, votes);
				gameListItem.populateIntent(intent);
		        _context.startActivity(intent);
			}
		});
        holder.genre.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>" + TextUtils.htmlEncode(item.getGenreName()) + "</u></font>"));
        holder.genre.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(_context, GamesActivity.class);
		        intent.putExtra(Globals.PACKAGE_PREFIX + "gamesActivityType", GamesActivity.GAMES_ACTIVITY_TYPE.GENRE.ordinal());
		        intent.putExtra(Globals.PACKAGE_PREFIX + "genreId", genreId);
		        intent.putExtra(Globals.PACKAGE_PREFIX + "genreName", genreName);
		        _context.startActivity(intent);
			}
		});
        holder.msPoints.setText(item.getMsPointsCost() + " ms points");
		holder.releasedOn.setText("Released On: " + dateFormat.format(item.getReleasedOn()));
		holder.lastUpdatedOn.setText("Last Updated On: " + dateFormat.format(item.getUpdatedOn()));
        holder.votes.setText("Votes: " + item.getVotes());
        holder.description.setText(item.getInfo());
        
        setStarImages(holder, item.getScore());
        
        //if (Globals.D) Log.i(TAG, "Position:" + position + " URL:" + boxArt);
        
        final Bitmap bmp = _imageLoader.getBitmap(_context, boxArt, new ImageLoadedListener() {
			@Override
			public void onImageLoaded(Bitmap bitmap) {
				if (bitmap != null) {
					if (Globals.D) Log.i(TAG, "onImageLoaded: " + boxArt);
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
