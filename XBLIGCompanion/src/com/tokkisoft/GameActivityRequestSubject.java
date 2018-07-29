package com.tokkisoft;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.tokkisoft.droidlib.HttpRequest;
import com.tokkisoft.droidlib.IErrorCallback;
import com.tokkisoft.droidlib.JsonUtils;
import com.tokkisoft.droidlib.Subject;
import com.tokkisoft.droidlib.Utils;

public class GameActivityRequestSubject extends Subject
{
	private Context _context;
	private WorkerTask _task;
	private GameListItem _gameInput;
	private Game _gameOutput;
	
	public GameActivityRequestSubject(Context context)
	{
		super();
		_context = context;
	}
	
	public Game getGameData()
	{
		return _gameOutput;
	}
	
	public void asyncRequest(GameListItem game)
	{
		//if (requestIsRunning())
		//	return;
		//setRequestIsRunning(true);
		
		_gameInput = game;
		
		_task = new WorkerTask();
    	_task.execute();
	}
	
	public class WorkerTask extends AsyncTask<Void, Void, Void> implements IErrorCallback {

		private boolean _isError;
    	private ErrorType _errorType;
    	//private Exception _exception;

		@Override
		protected Void doInBackground(Void... arg0)
		{
			_errorType = ErrorType.NO_ERROR;
			
			HttpRequest request = new HttpRequest(_context, this);
			// http://noms.apphb.com/Xblig/GetGame?id=2527
			String url = String.format("http://noms.apphb.com/Xblig/GetGame?id=%d", _gameInput.getId());
			String response = request.httpGet(url, -1);
			
			if (_isError)
				return null;
			
			//if (Globals.D) Log.i("GameActivityRequestSubject", "json: " + response);
			
			JSONObject json = JsonUtils.getObj(response);
			if (json == null) {
				_isError = true;
				_errorType = ErrorType.INVALID_RESPONSE_FORMAT;
				return null;
			}
			
			_gameOutput = new Game();
			
			try {
				// populate Game data
				_gameOutput.setId(json.getInt("Id"));
				_gameOutput.setName(json.getString("Name"));
				_gameOutput.setDeveloperId(json.getInt("DeveloperId"));
				_gameOutput.setDeveloperName(json.getString("DeveloperName"));
				_gameOutput.setGenreId(json.getInt("GenreId"));
				_gameOutput.setGenreName(json.getString("GenreName"));
				_gameOutput.setInfo(json.getString("Info"));
				_gameOutput.setMarketPlaceLink(json.getString("MarketPlaceLink"));
				_gameOutput.setMsPointsCost(json.getInt("MsPointsCost"));
				_gameOutput.setDevInfo(json.getString("DevInfo"));
				_gameOutput.setScore(json.getDouble("Score"));
				_gameOutput.setVotes(json.getInt("Votes"));
				_gameOutput.setReleasedOn(Utils.getDateFromDotNetJsonDate(json.getString("ReleasedOn")));
				_gameOutput.setUpdatedOn(Utils.getDateFromDotNetJsonDate(json.getString("UpdatedOn")));
				
				JSONArray imagesAry = json.getJSONArray("Images");
				int imagesLen = imagesAry.length();
				for (int i = 0; i < imagesLen; i++) {
					JSONObject o = imagesAry.getJSONObject(i);
					//int id = o.getInt("Id");
					//int gameId = o.getInt("XbligGameId");
					//String imageLink = o.getString("ImageLink");
					//int imageType = o.getInt("ImageType");
					//GameImage gameImage = _gameOutput.new GameImage(id, gameId, imageLink, imageType);
					//_gameOutput.addGameImage(gameImage);
					_gameOutput.addGameImage(_gameOutput.new GameImage(
							o.getInt("Id"),
							o.getInt("XbligGameId"),
							o.getString("ImageLink"),
							o.getInt("ImageType")));
				}
				
				JSONArray videosAry = json.getJSONArray("Videos");
				int videosLen = videosAry.length();
				for (int i = 0; i < videosLen; i++) {
					JSONObject o = videosAry.getJSONObject(i);
					int vidType = o.getInt("VidType");
					if (vidType == 0) {  // yahoo link
						String yahooLink = o.getString("VidLink");
						if (yahooLink != null && !yahooLink.trim().equals("")) {
							_gameOutput.setYahooLink(yahooLink);
							break; // no other video types?
						}
					}
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
