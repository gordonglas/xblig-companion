package com.tokkisoft;

import java.util.ArrayList;

import com.tokkisoft.R;

import android.content.Context;
import android.widget.ArrayAdapter;

//see - http://android.amberfog.com/?p=296
//also see 3.5 - http://www.vogella.com/articles/AndroidListView/article.html
public class BasicListAdapter extends ArrayAdapter<BasicListItem> {
	//private final Context context;
	//private final BasicListItem[] objects;
	//private LayoutInflater inflater;
	
	// can't use appendItems if you use this constructor! Use the ArrayList one if you want to use appendItems.
	public BasicListAdapter(Context context, BasicListItem[] objects) {
		
		super(context, R.layout.basic_list_item, android.R.id.text1, objects);
		
		//this.context = context;
		//this.objects = objects;
		
		//inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public BasicListAdapter(Context context, ArrayList<BasicListItem> objects) {
		super(context, R.layout.basic_list_item, android.R.id.text1, objects);
	}
	
	public void appendItems(ArrayList<BasicListItem> items)
	{
		for (BasicListItem item : items) {
			this.add(item);
		}
	}
	
	@Override
	public long getItemId(int position) {
		BasicListItem item = super.getItem(position);
		return item.getId();
	}
	
	//@Override
	//public View getView(int position, View convertView, ViewGroup parent) {
		
	//}
}
