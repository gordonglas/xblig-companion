package com.tokkisoft;

import com.tokkisoft.R;
import com.tokkisoft.droidlib.Utils;
import com.tokkisoft.AppScreens.ScreenType;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends Activity {
	XBLIGCActionBar _actionBar;
	TextView _aboutText;
	TextView _webLink;
	TextView _twitterLink;
	TextView _fbLink;
	TextView _versionText;
	ImageView _donateImage;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.about_activity);
		
		_actionBar = (XBLIGCActionBar)findViewById(R.id.tokkiActionBar);
		_aboutText = (TextView)findViewById(R.id.aboutText);
		_webLink = (TextView)findViewById(R.id.webLink);
		_twitterLink = (TextView)findViewById(R.id.twitterLink);
		_fbLink = (TextView)findViewById(R.id.fbLink);
		_versionText = (TextView)findViewById(R.id.versionText);
		_donateImage = (ImageView)findViewById(R.id.donateImage);
		
		String text = "XBLIG (Xbox Live Indie Games) Companion was created in hopes to make more people aware of the XBLIG Channel on the Xbox 360. The XBLIG Channel (found on your Xbox 360 dashboard) has many great games that you may have never heard of. We hope to change that with this app.";
		text += "\n\nAndroid version of XBLIG Companion was created by Tokki Soft. Windows Phone 7 and iOS versions were created by Eat-Studios.";
		_aboutText.setText(text);
		
		_webLink.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>Visit TokkiSoft.com on the web</u></font>"));
		_webLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://tokkisoft.com"));
				startActivity(browserIntent);
			}
		});
		
		_twitterLink.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>Follow @TokkiSoft on Twitter</u></font>"));
		_twitterLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Utils.getTwitterLink("tokkisoft")));
				startActivity(browserIntent);
			}
		});
		
		_fbLink.setText(Html.fromHtml("<font color=\"#0f4a64\"><u>Like Tokki Soft on Facebook</u></font>"));
		_fbLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://facebook.com/tokkisoft"));
				startActivity(browserIntent);
			}
		});
		
		String versionName = null;
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (versionName != null) {
			_versionText.setText(String.format("Version: %s", versionName));
		}
		else {
			_versionText.setText("");
		}
		
		_donateImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=payments%40tokkisoft%2ecom&lc=US&item_name=XBLIG%20Companion%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"));
				startActivity(browserIntent);
			}
		});
	}
	
	@Override
    public void onResume() {
    	super.onResume();
    	AppScreens.onActivityResume(ScreenType.ABOUT);
    	_actionBar.onScreenChange(ScreenType.ABOUT, AppScreens.getCurrentTab(ScreenType.ABOUT));
    }
}
