package june.footballmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.util.Log;

public class MyMatchItem {
	// 멤버
	int _matchNo;
	String _location;
	String _ground;
	String _date;
	String _startTime;
	String _endTime;
	String _teamName; // 상대팀 이름
	int _applyCnt;
	
	// 생성자(대기중 매치)
	public MyMatchItem( int matchNo, String location, String ground, String date, String startTime, String endTime, int applyCnt ) {
		this._matchNo = matchNo;
		this._location = location;
		this._ground = ground;
		this._date = date;
		this._startTime = startTime;
		this._endTime = endTime;
		this._applyCnt = applyCnt;
	}
	
	// 생성자(성사된 매치)
	public MyMatchItem(int matchNo, String location, String ground, String date, String startTime, String endTime, String teamName) {
		this._matchNo = matchNo;
		this._location = location;
		this._ground = ground;
		this._date = date;
		this._startTime = startTime;
		this._endTime = endTime;
		this._teamName = teamName;
	}
	
	// getter methods
	public int getMatchNo() {
		return this._matchNo;
	}
	
	public String getLocation() {
		return this._location;
	}
	
	public String getGround() {
		return this._ground;
	}
	
	public String getDate() {
		return this._date;
	}
	
	public String getSession() {
		SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
		
		String startTime = null;
		String endTime = null;
		String session = null;
		
		try {
			Date date = originalFormat.parse(_startTime);
			startTime = newFormat.format(date);
			
			date = originalFormat.parse(_endTime);
			endTime = newFormat.format(date);
			
			session = startTime + " ~ " + endTime;
			
		} catch (ParseException e) {
			Log.e("FM", e.getMessage());
		}
		
		return session;
	}
	
	public String getDayOfWeek() {
		String[] s = _date.split("-");
		
		Calendar cal = new GregorianCalendar(Integer.parseInt(s[0]), 
				Integer.parseInt(s[1]) - 1, 
				Integer.parseInt(s[2]), 0, 0, 0);
		
		String day = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.KOREAN);
		
		return day;
	}
	
	public int getApplyCnt() {
		return _applyCnt;
	}
	
	public String getTeamName() {
		return this._teamName;
	}
}
