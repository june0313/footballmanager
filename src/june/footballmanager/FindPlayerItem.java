package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class FindPlayerItem {
	private int _no;
	private String _teamName;
	private String _title;
	private String _location;
	private String _position;
	private String _ages;
	private String _postedDate;
	
	FindPlayerItem(JSONObject item) {
		
		try {
			
			this._no = item.getInt("NO");
			this._teamName = item.getString("TEAM_NAME");
			this._title = item.getString("TITLE");
			this._location = item.getString("LOCATION");
			this._position = item.getString("POSITION");
			this._ages = item.getString("AGES");
			this._postedDate = item.getString("POSTED_DATE");
			
		} catch (JSONException e) {
			Log.i(FindPlayerItem.class.getName(), e.getMessage());
		}
	}
	
	public int getNo() {
		return this._no;
	}
	
	public String getTeamName() {
		return this._teamName;
	}
	
	public String getTitle() {
		return this._title;
	}
	
	public String getLocation() {
		return this._location;
	}
	
	public String getPosition() {
		return this._position;
	}
	
	public String getAges() {
		return this._ages;
	}
	
	public String getPostedDate() {
		String[] s = _postedDate.substring(0, 10).split("-");
		String postedDateKor = s[0] + "³â " + s[1] + "¿ù " + s[2] + "ÀÏ";
		return postedDateKor;
	}
}
