package com.tokkisoft;

import com.tokkisoft.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class CatalogFragment extends ListFragment {
	private BasicListItem[] _items;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		return inflater.inflate(R.layout.catalog_fragment, container, false);
    }
	
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (Globals.D) Log.i("CatalogFragment", "onActivityCreated");
		
		_items = new BasicListItem[4];
        _items[0] = new BasicListItem(0, "New Games", "");
        _items[1] = new BasicListItem(0, "Recently Updated Games", "");
        _items[2] = new BasicListItem(0, "Games By Genre", "");
        _items[3] = new BasicListItem(0, "Games By Rating", "");
		
        setListAdapter(new BasicListAdapter(getActivity(), _items));
        
        //String[] catalogStrings = {
        //    "New Games", "Recently Updated Games", "Games By Genre",
        //    "Games By Rating"
        //};
		
		//setListAdapter(new ArrayAdapter<String>(getActivity(),
		//		android.R.layout.simple_list_item_1, catalogStrings));
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        //if (Globals.D) Log.i("CatalogFragment", "Item clicked: " + id);
        super.onListItemClick(l, v, position, id);
        
        if (position == 0) // New Games
        {
	        Intent intent = new Intent(getActivity(), GamesActivity.class);
	        intent.putExtra(Globals.PACKAGE_PREFIX + "gamesActivityType", GamesActivity.GAMES_ACTIVITY_TYPE.NEW_GAME.ordinal());
	        getActivity().startActivity(intent);
        }
        else if (position == 1) // Recently Updated Games
        {
        	Intent intent = new Intent(getActivity(), GamesActivity.class);
	        intent.putExtra(Globals.PACKAGE_PREFIX + "gamesActivityType", GamesActivity.GAMES_ACTIVITY_TYPE.RECENTLY_UPDATED.ordinal());
	        getActivity().startActivity(intent);
        }
        else if (position == 2) // Games By Genre
        {
        	Intent intent = new Intent(getActivity(), GenreActivity.class);
        	getActivity().startActivity(intent);
        }
        else if (position == 3) // Games By Rating
        {
        	Intent intent = new Intent(getActivity(), GamesActivity.class);
	        intent.putExtra(Globals.PACKAGE_PREFIX + "gamesActivityType", GamesActivity.GAMES_ACTIVITY_TYPE.BY_RATING.ordinal());
	        getActivity().startActivity(intent);
        }
    }
}
