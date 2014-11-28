package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class FindTeamItem {
	private int _no;
	private String _nickName;
	private String _title;
	private String _location;
	private String _position;
	private int _age;
	private String _postedDate;
	
	FindTeamItem(JSONObject item) {
		try {
			this._no = item.getInt("NO");
			this._nickName = item.getString("NICKNAME");
			this._title = item.getString("TITLE");
			this._location = item.getString("LOCATION");
			this._position = item.getString("POSITION");
			this._age = item.getInt("AGE");
			this._postedDate = item.getString("POSTED_DATE");
		} catch (JSONException e) {
			Log.i("FindTeamItem", e.getMessage());
		}
	}
	
	public int getNo() {
		return this._no;
	}
	
	public String getNickName() {
		return this._nickName;
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
	
	public int getAge() {
		return this._age;
	}
	
	public String getPostedDate() {
		String[] s = _postedDate.substring(0, 10).split("-");
		String postedDateKor = s[0] + "년 " + s[1] + "월 " + s[2] + "일";
		return postedDateKor;
	}
}
