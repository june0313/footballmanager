package june.footballmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	private String _actDay;
	private String _actTimeStart;
	private String _actTimeEnd;
	
	// 생성자
	FindPlayerItem(JSONObject item) {
		
		try {
			
			this._no = item.getInt("NO");
			this._teamName = item.getString("TEAM_NAME");
			this._title = item.getString("TITLE");
			this._location = item.getString("LOCATION");
			this._position = item.getString("POSITION");
			this._ages = item.getString("AGES");
			this._postedDate = item.getString("POSTED_DATE");
			this._actDay = item.getString("ACT_DAY");
			this._actTimeStart = item.getString("ACT_TIME_START");
			this._actTimeEnd = item.getString("ACT_TIME_END");
			
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
		String postedDateKor = s[0] + "년 " + s[1] + "월 " + s[2] + "일";
		return postedDateKor;
	}
	
	// 활동 요일 리턴
	public String getActDay() {
		return this._actDay;
	}
	
	// 시작시간 ~ 종료시간 형태의 문자열을 만들어 리턴한다.
	public String getActSession() {
		SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm:ss");
		// SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
		SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm");	// 00 ~ 24 시간 형식으로 표현
		
		String startTime = null;
		String endTime = null;
		String session = null;
		
		try {
			Date date = originalFormat.parse(_actTimeStart);
			startTime = newFormat.format(date);
			
			date = originalFormat.parse(_actTimeEnd);
			endTime = newFormat.format(date);
			
			session = startTime + " ~ " + endTime;
			
		} catch (ParseException e) {
			Log.e("getSession()", e.getMessage());
		}
		
		return session;
	}
}
