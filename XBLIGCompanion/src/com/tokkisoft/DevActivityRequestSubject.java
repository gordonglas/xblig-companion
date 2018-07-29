package com.tokkisoft;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tokkisoft.droidlib.HttpRequest;
import com.tokkisoft.droidlib.IErrorCallback;
import com.tokkisoft.droidlib.JsonUtils;
import com.tokkisoft.droidlib.Subject;
import com.tokkisoft.droidlib.Utils;

import android.content.Context;
import android.os.AsyncTask;

public class DevActivityRequestSubject extends Subject
{
	private Context _context;
	private WorkerTask _task;
	private DevActivityInput _devInput;
	private DeveloperInfo _devOutput;
	
	public DevActivityRequestSubject(Context context)
	{
		super();
		_context = context;
	}
	
	public DeveloperInfo getDevInfo()
	{
		return _devOutput;
	}
	
	public void asyncRequest(DevActivityInput devInput)
	{
		//if (requestIsRunning())
		//	return;
		//setRequestIsRunning(true);
		
		_devInput = devInput;
		
		_task = new WorkerTask();
		_task.execute();
	}
	
	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback
	{
		private boolean _isError;
		private ErrorType _errorType;
		//private Exception _exception;
		
		@Override
		protected Void doInBackground(Void... arg0)
		{
			_errorType = ErrorType.NO_ERROR;
			
			HttpRequest request = new HttpRequest(_context, this);
			// http://noms.apphb.com/Xblig/GameListByDev?id=277
			String url = String.format("http://noms.apphb.com/Xblig/GameListByDev?id=%d", _devInput.getDevId());
			String response = request.httpGet(url, -1);
			
			if (_isError)
				return null;
			
			//if (Globals.D) Log.i("DevActivityRequestSubject", "json: " + response);
			
			JSONObject json = JsonUtils.getObj(response);
			if (json == null) {
				_isError = true;
				_errorType = ErrorType.INVALID_RESPONSE_FORMAT;
				return null;
			}
			
			_devOutput = new DeveloperInfo();
			
			try {
				// populate Developer data
				
				DeveloperItem devItem = _devOutput.getDevItem();
				JSONObject oDev = json.getJSONObject("Developer");
				devItem.setId(oDev.getInt("Id"));
				devItem.setName(oDev.getString("Name"));
				devItem.setBio(oDev.getString("Bio"));
				devItem.setWebsite(oDev.getString("Website"));
				devItem.setTwitterHandle(oDev.getString("TwitterHandle"));
				devItem.setFacebookPage(oDev.getString("FacebookPage"));
				devItem.setYoutubeChannel(oDev.getString("YouTubeChannel"));
				
				ArrayList<Game> devGames = _devOutput.getGames();
				JSONArray gamesAry = json.getJSONArray("Games");
				int gamesLen = gamesAry.length();
				for (int j = 0; j < gamesLen; j++) {
					JSONObject o = gamesAry.getJSONObject(j);
					
					Game game = new Game();
					
					game.setId(o.getInt("Id"));
					game.setName(o.getString("Name"));
					game.setDeveloperId(o.getInt("DeveloperId"));
					game.setDeveloperName(o.getString("DeveloperName"));
					game.setGenreId(o.getInt("GenreId"));
					game.setGenreName(o.getString("GenreName"));
					game.setInfo(o.getString("Info"));
					game.setMarketPlaceLink(o.getString("MarketPlaceLink"));
					game.setMsPointsCost(o.getInt("MsPointsCost"));
					game.setDevInfo(o.getString("DevInfo"));
					game.setScore(o.getDouble("Score"));
					game.setVotes(o.getInt("Votes"));
					game.setReleasedOn(Utils.getDateFromDotNetJsonDate(o.getString("ReleasedOn")));
					game.setUpdatedOn(Utils.getDateFromDotNetJsonDate(o.getString("UpdatedOn")));
					
					JSONArray imagesAry = o.getJSONArray("Images");
					int imagesLen = imagesAry.length();
					for (int i = 0; i < imagesLen; i++) {
						JSONObject oi = imagesAry.getJSONObject(i);
						//int id = oi.getInt("Id");
						//int gameId = oi.getInt("XbligGameId");
						//String imageLink = oi.getString("ImageLink");
						//int imageType = oi.getInt("ImageType");
						//GameImage gameImage = game.new GameImage(id, gameId, imageLink, imageType);
						//game.addGameImage(gameImage);
						game.addGameImage(game.new GameImage(
								oi.getInt("Id"),
								oi.getInt("XbligGameId"),
								oi.getString("ImageLink"),
								oi.getInt("ImageType")));
					}
					
					JSONArray videosAry = o.getJSONArray("Videos");
					int videosLen = videosAry.length();
					for (int i = 0; i < videosLen; i++) {
						JSONObject ov = videosAry.getJSONObject(i);
						int vidType = ov.getInt("VidType");
						if (vidType == 0) {  // yahoo link
							String yahooLink = ov.getString("VidLink");
							if (yahooLink != null && !yahooLink.trim().equals("")) {
								game.setYahooLink(yahooLink);
								break; // no other video types?
							}
						}
					}
					
					devGames.add(game);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// simulate long load time (for testing)
			//try {
			//	Thread.sleep(5000);
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}
			
			return null;
		}
		
		@ Override
	    protected void onPostExecute(Void result)
		{
			notifyObservers(_errorType);
		}
		
		@Override
		public void onError(ErrorType errorType, Exception ex)
		{
			_isError = true;
			_errorType = errorType;
			//_exception = ex;
		}
	}
}
