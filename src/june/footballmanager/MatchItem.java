package june.footballmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class MatchItem {
	int _matchNo;
	String _teamName;
	String _ages;
	String _location;
	String _ground;
	String _date;
	String _time1;
	String _time2;
	int _state;
	int _applyCnt;
	String _postedDate;
	
	public MatchItem(int matchNo, String teamName, String ages, String date, String time1, String time2, String location, String ground, int state, int applyCnt, String postedDate) {
		this._matchNo = matchNo;
		this._teamName = teamName;
		this._ages = ages;
		this._date = date;
		this._time1 = time1;
		this._time2 = time2;
		this._location = location;
		this._ground = ground;
		this._state = state;
		this._applyCnt = applyCnt;
		this._postedDate = postedDate;
	}
	
	public MatchItem( JSONObject item) {
		try {
			this._matchNo = item.getInt("MATCH_NO");
			this._teamName = item.getString("TEAM_NAME");
			this._ages = item.getString("AGES");
			this._date = item.getString("MATCH_DATE");
			this._time1 = item.getString("MATCH_TIME");
			this._time2 = item.getString("MATCH_TIME2"); 
			this._location = item.getString("LOCATION");
			this._ground = item.getString("GROUND");
			this._state = item.getInt("STATE");
			this._applyCnt = item.getInt("APPLY_CNT");
			this._postedDate = item.getString("POSTED_DATE");
		} catch (JSONException e) {
			Log.i("MatchItem", e.getMessage());
		} 
	}
	
	public int getMatchNo() {
		return this._matchNo;
	}
	
	public String getTeamName() {
		return this._teamName;
	}
	
	public String getAges() {
		return this._ages;
	}
	
	public String getDate() {
		return this._date;
	}
	
	public String getStartTime() {
		return this._time1;
	}
	
	public String getEndTime() {
		return this._time2;
	}
	
	public String getLocation() {
		return this._location;
	}
	
	public String getGround() {
		return this._ground;
	}
	
	public int getApplyCnt() {
		return this._applyCnt;
	}
	
	public int getState() {
		return this._state;
	}
	
	public String getSession() {
		SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
		
		String startTime = null;
		String endTime = null;
		String session = null;
		
		try {
			Date date = originalFormat.parse(_time1);
			startTime = newFormat.format(date);
			
			date = originalFormat.parse(_time2);
			endTime = newFormat.format(date);
			
			session = startTime + " ~ " + endTime;
			
		} catch (ParseException e) {
			Log.e("FM", e.getMessage());
		}
		
		return session;
	}
	
	public String getDayOfWeek() {
		String[] s = _date.split("-");
		
		Calendar cal = new GregorianCalendar(
				Integer.parseInt(s[0]), 
				Integer.parseInt(s[1]) - 1, 
				Integer.parseInt(s[2]), 0, 0, 0);
		
		String day = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.KOREAN);
		
		return day;
	}
	
	public String getPostedDate() {
		String[] s = _postedDate.substring(0, 10).split("-");
		String postedDateKor = s[0] + "³â " + s[1] + "¿ù " + s[2] + "ÀÏ";
		return postedDateKor;
	}
}
