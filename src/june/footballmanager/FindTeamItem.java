package june.footballmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private String _actDay;
	private String _actTimeStart;
	private String _actTimeEnd;
	private String _postedDate;
	
	FindTeamItem(JSONObject item) {
		try {
			this._no = item.getInt("NO");
			this._nickName = item.getString("NICKNAME");
			this._title = item.getString("TITLE");
			this._location = item.getString("LOCATION");
			this._position = item.getString("POSITION");
			this._age = item.getInt("AGE");
			this._actDay = item.getString("ACT_DAY");
			this._actTimeStart = item.getString("ACT_TIME_START");
			this._actTimeEnd = item.getString("ACT_TIME_END");
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
	
	public String getActDay() {
		return this._actDay;
	}
	
	public String getActTimeStart() {
		return this._actTimeStart;
	}
	
	public String getActTimeEnd() {
		return this._actTimeEnd;
	}
	
	// 시작시간 ~ 종료시간 형태의 문자열을 만들어 리턴한다.
	public String getActSession() {
		String session = getActTimeStart().substring(0, 5) 
				+ " ~ " + getActTimeEnd().substring(0, 5);
		
		return session;
	}
	
	public String getPostedDate() {
		String[] s = _postedDate.substring(0, 10).split("-");
		String postedDateKor = s[0] + "년 " + s[1] + "월 " + s[2] + "일";
		return postedDateKor;
	}
}
