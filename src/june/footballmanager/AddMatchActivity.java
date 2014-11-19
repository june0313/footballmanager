package june.footballmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddMatchActivity extends Activity implements OnClickListener {
	static final int LOCATION = 1;
	
	TextView tvDate;
	TextView tvTime1;
	TextView tvTime2;
	TextView tvLocation;
	EditText ground;
	TextView tvDetail;
	
	String matchDate;
	String matchTime1;
	String matchTime2;
	
	Button addMatch;
	
	ProgressDialog pd;
	
	LoginManager lm;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_add_match);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);
	    
	    lm = new LoginManager(this);
	    
	    tvDate = (TextView) findViewById(R.id.date);
	    tvDate.setOnClickListener(this);
	    
	    tvTime1 = (TextView) findViewById(R.id.time1);
	    tvTime1.setOnClickListener(this);
	    
	    tvTime2 = (TextView) findViewById(R.id.time2);
	    tvTime2.setOnClickListener(this);
	    
	    tvLocation = (TextView) findViewById(R.id.location);
	    tvLocation.setText(lm.getLocation());
	    tvLocation.setOnClickListener(this);
	    
	    ground = (EditText) findViewById(R.id.ground);
	    ground.setText(lm.getHome());
	    ground.setOnClickListener(this);
	    
	    tvDetail = (TextView) findViewById(R.id.detail);
	    
	    addMatch = (Button) findViewById(R.id.btn_add_match);
	    addMatch.setOnClickListener(this);
	}
	
	@Override
	public void onClick(final View v) {
		
		Calendar cal = new GregorianCalendar();
		
		int id = v.getId();
		if (id == R.id.date) {
			// 데이트 피커 리스너
			DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

				@Override
				public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
					Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth, 0, 0, 0);
					String date = year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일 ";
					date += cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.KOREAN);
					
					// 날짜 출력
					tvDate.setText(date);
					
					// 서버로 전송할 날짜 구성
					matchDate = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
				}
			};
			int curYear = cal.get(Calendar.YEAR);
			int curMonth = cal.get(Calendar.MONTH);
			int curDay = cal.get(Calendar.DAY_OF_MONTH);
			// 데이트 피커 다이얼로그 호출
			new DatePickerDialog(AddMatchActivity.this, mDateSetListener, curYear, curMonth, curDay).show();
		} else if (id == R.id.time1
				|| id == R.id.time2) {
			// 타임 피커 리스너
			TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					// 시간 포맷을 '오전 12:05' 형식으로 변경
					
					SimpleDateFormat originalFormat = new SimpleDateFormat("HH:m");
					SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
					String time = "";
					
					
					try {
						Date date = originalFormat.parse(Integer.toString(hourOfDay) + ":" + Integer.toString(minute));
						time = newFormat.format(date);
						
					} catch (ParseException e) {
						Log.e("FM", e.getMessage());
					}
					
					// 시간 출력 및 서버로 전송할 시간 구성
					if( v.getId() == R.id.time1) {
						tvTime1.setText(time);
						matchTime1 = hourOfDay + ":" + minute;
					}
					else {
						tvTime2.setText(time);
						matchTime2 = hourOfDay + ":" + minute;
					}
				}
			};
			int curHour = cal.get(Calendar.HOUR_OF_DAY);
			int curMinute = cal.get(Calendar.MINUTE);
			// 타임 피커 다이얼로그 호출
			new TimePickerDialog(AddMatchActivity.this, mTimeSetListener, curHour, curMinute, false).show();
		} else if (id == R.id.location) {
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
		} else if (id == R.id.btn_add_match) {
			if( tvDate.getText().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "경기 날짜를 설정해주세요.", 0).show();
			} else if(tvTime1.getText().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "시작 시간을 설정해주세요.", 0).show();
			} else if(tvTime2.getText().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "종료 시간을 설정해주세요.", 0).show();
			} else if(tvLocation.getText().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "지역을 설정해주세요.", 0).show();
			} else if(ground.getText().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "경기장을 설정해주세요.", 0).show();
			} else {
				
				// 모든 조건을 만족하면 매치 등록 작업 수행
				addMatch();
			}
		}
	}
	
	// 주소 받아오기
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOCATION:
			if (resultCode == RESULT_OK) {
				tvLocation.setText(intent.getStringExtra("location"));
			}
		}
	}
	
	// 웹서버로 매치를 등록하는 메서드
	private void addMatch() {
		String url = getString(R.string.server)	+ getString(R.string.add_match);
		String param = "email=" + lm.getEmail()
				+ "&match_date=" + matchDate
				+ "&match_time1=" + matchTime1
				+ "&match_time2=" + matchTime2
				+ "&location=" + tvLocation.getText().toString()
				+ "&ground=" + ground.getText().toString()
				+ "&detail=" + tvDetail.getText().toString().replace("\n", "__");
		
		JSONObject json = new HttpTask(url, param).getJSONObject();
		
		// check the success of getting information
		try {
			if (json.getInt("success") == 1) {
				Toast.makeText(getApplicationContext(), "매치가 등록되었습니다.", 0).show();
				finish();
			}
		} catch (JSONException e) {
			Log.e("addMatch", e.getMessage());
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId()) {
		case android.R.id.home :
			finish();
			return true;
		}
		
		return false;
	}

	

}
