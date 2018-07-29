package com.tokkisoft;

import com.tokkisoft.R;
import com.tokkisoft.AppScreens.ScreenType;
import com.tokkisoft.AppScreens.TabType;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class XBLIGCActionBar extends RelativeLayout {

	private View view;
	private TextView actionBarText;
	private Button actionBarButtonRefresh;
	private View actionBarSeparator1;
	private Button actionBarButtonHome;
	//private View actionBarSeparator2;
	private Button actionBarButtonSearch;
	private View actionBarSeparator3;
	private EditText _searchText;
	//private Button _searchBtn;
	
	public XBLIGCActionBar(final Context context, AttributeSet attrs) {
		super(context, attrs);
		
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.xbligc_action_bar, this, true);
		
		actionBarText = (TextView)view.findViewById(R.id.actionBarText);
		
		actionBarButtonHome = (Button)view.findViewById(R.id.actionBarButtonHome);
		actionBarButtonHome.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				ScreenType screen = AppScreens.getCurrentScreen();
				
				if (screen != ScreenType.HOME)
				{
					Intent intent = new Intent(context, XBLIGCompanionActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			}
		});
		
		actionBarButtonRefresh = (Button)view.findViewById(R.id.actionBarButtonRefresh);
		actionBarButtonRefresh.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ScreenType screen = AppScreens.getCurrentScreen();
				TabType tab = AppScreens.getCurrentTab(screen);
				
				if (screen == ScreenType.HOME) {
					if (tab == TabType.HOME_PICKS) {
						Fragment fragment = ((XBLIGCompanionActivity) XBLIGCActionBar.this.getContext())._adapter.getFragment(1);
						PicksFragment picksFragment = (PicksFragment)fragment;
						picksFragment.onRefresh();
					}
				}
				else if (screen == ScreenType.NEW_GAMES) {
					if (tab == TabType.NEW_GAMES_NEW_GAMES) {
						((GamesActivity)XBLIGCActionBar.this.getContext()).onRefresh();
					}
				}
				else if (screen == ScreenType.GAME) {
					((GameActivity)XBLIGCActionBar.this.getContext()).onRefresh();
				}
				else if (screen == ScreenType.DEV) {
					((DevActivity)XBLIGCActionBar.this.getContext()).onRefresh();
				}
				else if (screen == ScreenType.GENRE) {
					((GenreActivity)XBLIGCActionBar.this.getContext()).onRefresh();
				}
				else if (screen == ScreenType.PICK) {
					if (tab == TabType.PICK_INFO) {
						Fragment fragment = ((PickActivity) XBLIGCActionBar.this.getContext())._adapter.getFragment(0);
						PickInfoFragment pickInfoFragment = (PickInfoFragment)fragment;
						pickInfoFragment.onRefresh();
					}
					else if (tab == TabType.PICK_GAMES) {
						Fragment fragment = ((PickActivity) XBLIGCActionBar.this.getContext())._adapter.getFragment(1);
						PickGamesFragment pickGameFragment = (PickGamesFragment)fragment;
						pickGameFragment.onRefresh();
					}
				}
				//else if (screen == ScreenType.SEARCH) {
				//	((SearchActivity)XBLIGCActionBar.this.getContext()).onRefresh();
				//}
			}
		});
		
		actionBarButtonSearch = (Button)view.findViewById(R.id.actionBarButtonSearch);
		actionBarButtonSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, SearchActivity.class);
				context.startActivity(intent);
			}
		});
		
		_searchText = (EditText)findViewById(R.id.searchText);
		_searchText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		_searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					((SearchActivity) XBLIGCActionBar.this.getContext()).onSearchButtonClick(_searchText.getText().toString());
					return true;
				}
				return false;
			}
		});
        //_searchBtn = (Button)findViewById(R.id.searchBtn);
        //_searchBtn.setOnClickListener(new OnClickListener() {
		//	@Override
		//	public void onClick(View v) {
		//		((SearchActivity) XBLIGCActionBar.this.getContext()).onSearchButtonClick(_searchText.getText().toString());
		//	}
		//});
		
		actionBarSeparator1 = (View)view.findViewById(R.id.actionBarSeparator1);
		//actionBarSeparator2 = (View)view.findViewById(R.id.actionBarSeparator2);
		actionBarSeparator3 = (View)view.findViewById(R.id.actionBarSeparator3);
	}
	
	public void onScreenChange(ScreenType screen, TabType tab) {
		if (screen == ScreenType.HOME) {
			actionBarText.setText("XBLIG Companion");
			
			if (tab == TabType.HOME_CATALOG) {
				actionBarSeparator1.setVisibility(VISIBLE);
				actionBarButtonRefresh.setVisibility(VISIBLE);
				//RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams)actionBarSeparator3.getLayoutParams();
				//layout.addRu
				actionBarSeparator3.setVisibility(VISIBLE);
				actionBarButtonSearch.setVisibility(VISIBLE);
				
				_searchText.setVisibility(GONE);
				//_searchBtn.setVisibility(GONE);
			}
			else if (tab == TabType.HOME_PICKS) {
				actionBarSeparator1.setVisibility(VISIBLE);
				actionBarButtonRefresh.setVisibility(VISIBLE);
				actionBarSeparator3.setVisibility(VISIBLE);
				actionBarButtonSearch.setVisibility(VISIBLE);
				
				_searchText.setVisibility(GONE);
				//_searchBtn.setVisibility(GONE);
			}
		}
		else if (screen == ScreenType.NEW_GAMES) {
			if (tab == TabType.NEW_GAMES_NEW_GAMES) {
				actionBarSeparator1.setVisibility(VISIBLE);
				actionBarButtonRefresh.setVisibility(VISIBLE);
				actionBarSeparator3.setVisibility(VISIBLE);
				actionBarButtonSearch.setVisibility(VISIBLE);
				
				_searchText.setVisibility(GONE);
				//_searchBtn.setVisibility(GONE);
			}
		}
		else if (screen == ScreenType.GAME) {
			actionBarText.setText(""); // this is set later via a call to setActionBarText()
			
			actionBarSeparator1.setVisibility(VISIBLE);
			actionBarButtonRefresh.setVisibility(VISIBLE);
			actionBarSeparator3.setVisibility(VISIBLE);
			actionBarButtonSearch.setVisibility(VISIBLE);
			
			_searchText.setVisibility(GONE);
			//_searchBtn.setVisibility(GONE);
		}
		else if (screen == ScreenType.DEV) {
			actionBarText.setText(""); // this is set later via a call to setActionBarText()
			
			actionBarSeparator1.setVisibility(VISIBLE);
			actionBarButtonRefresh.setVisibility(VISIBLE);
			actionBarSeparator3.setVisibility(VISIBLE);
			actionBarButtonSearch.setVisibility(VISIBLE);
			
			_searchText.setVisibility(GONE);
			//_searchBtn.setVisibility(GONE);
		}
		else if (screen == ScreenType.GENRE) {
			actionBarText.setText("Select A Genre");
			
			actionBarSeparator1.setVisibility(VISIBLE);
			actionBarButtonRefresh.setVisibility(VISIBLE);
			actionBarSeparator3.setVisibility(VISIBLE);
			actionBarButtonSearch.setVisibility(VISIBLE);
			
			_searchText.setVisibility(GONE);
			//_searchBtn.setVisibility(GONE);
		}
		else if (screen == ScreenType.PICK) {
			actionBarSeparator1.setVisibility(VISIBLE);
			actionBarButtonRefresh.setVisibility(VISIBLE);
			actionBarSeparator3.setVisibility(VISIBLE);
			actionBarButtonSearch.setVisibility(VISIBLE);
			
			_searchText.setVisibility(GONE);
			//_searchBtn.setVisibility(GONE);
		}
		else if (screen == ScreenType.SEARCH) {
			actionBarText.setText("Search");
			
			actionBarSeparator1.setVisibility(GONE);
			actionBarButtonRefresh.setVisibility(GONE);
			actionBarSeparator3.setVisibility(GONE);
			actionBarButtonSearch.setVisibility(GONE);
			
			_searchText.setVisibility(VISIBLE);
			//_searchBtn.setVisibility(VISIBLE);
		}
		else if (screen == ScreenType.ABOUT) {
			actionBarText.setText("About XBLIG Companion");
			
			actionBarSeparator1.setVisibility(GONE);
			actionBarButtonRefresh.setVisibility(GONE);
			actionBarSeparator3.setVisibility(GONE);
			actionBarButtonSearch.setVisibility(GONE);
			
			_searchText.setVisibility(GONE);
			//_searchBtn.setVisibility(GONE);
		}
	}
	
	public void setActionBarText(String text) {
		actionBarText.setText(text);
	}
}
