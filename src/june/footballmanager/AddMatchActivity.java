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
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
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
//			// 타임 피커 리스너
//			TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
//
//				@Override
//				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//					// 시간 포맷을 '오전 12:05' 형식으로 변경
//					
//					SimpleDateFormat originalFormat = new SimpleDateFormat("HH:m");
//					SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
//					String time = "";
//					
//					try {
//						Date date = originalFormat.parse(Integer.toString(hourOfDay) + ":" + Integer.toString(minute));
//						time = newFormat.format(date);
//						
//					} catch (ParseException e) {
//						Log.e("FM", e.getMessage());
//					}
//					
//					// 시간 출력 및 서버로 전송할 시간 구성
//					if( v.getId() == R.id.time1) {
//						tvTime1.setText(time);
//						matchTime1 = hourOfDay + ":" + minute;
//					}
//					else {
//						tvTime2.setText(time);
//						matchTime2 = hourOfDay + ":" + minute;
//					}
//				}
//			};
//			int curHour = cal.get(Calendar.HOUR_OF_DAY);
//			int curMinute = cal.get(Calendar.MINUTE);
//			// 타임 피커 다이얼로그 호출
//			new TimePickerDialog(AddMatchActivity.this, mTimeSetListener, curHour, curMinute, false).show();
			
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_set_time, null);
			final NumberPicker npHour = (NumberPicker)layout.findViewById(R.id.hour);
			final NumberPicker npMinute = (NumberPicker)layout.findViewById(R.id.minute);
			final NumberPicker npAMPM = (NumberPicker)layout.findViewById(R.id.ampm);
			
			npHour.setMinValue(1);
			npHour.setMaxValue(12);
			
			npMinute.setMinValue(0);
			npMinute.setMaxValue(11);
			npMinute.setDisplayedValues(new String[]{"00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"});
			
			npAMPM.setMinValue(0);
			npAMPM.setMaxValue(1);
			npAMPM.setDisplayedValues(new String[]{"오전", "오후"});
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(layout)
			.setTitle("시간 설정")
			.setPositiveButton("완료", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					String time = npAMPM.getDisplayedValues()[npAMPM.getValue()] + " "
							+ npHour.getValue() + ":" + npMinute.getDisplayedValues()[npMinute.getValue()];
					
					SimpleDateFormat originalFormat = new SimpleDateFormat("a h:mm");
					SimpleDateFormat newFormat = new SimpleDateFormat("HH:mm");
					Date date = null;
					
					
					try {
						date = originalFormat.parse(time);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//시간 출력 및 서버로 전송할 시간 구성
					if( v.getId() == R.id.time1) {
						tvTime1.setText(time);
						matchTime1 = newFormat.format(date);
					} else {
						tvTime2.setText(time);
						matchTime2 = newFormat.format(date);
					}
				};
				
			}).show();
			
		} else if (id == R.id.location) {
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
		} else if (id == R.id.btn_add_match) {
			// 매치 정보들이 조건에 맞게 입력되었는지 확인한다.
			SimpleDateFormat koreanDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
			Calendar currCal = null;
			Calendar settedCal = null;
			
			// 현재 시간, 시작 시간, 종료 시간
			SimpleDateFormat koreanTimeFormat = new SimpleDateFormat("a h:mm");
			Calendar currTime = null;
			Calendar startTime = null;
			Calendar endTime = null;
			
			try {
				
				// 현재 날짜
				Date currDate = new Date();
				currCal = Calendar.getInstance();
				currCal.setTime(currDate);
				
				// 현재 날짜에서 시, 분, 초를 0으로 만든다.
				currCal.set(Calendar.HOUR_OF_DAY, 0);
				currCal.set(Calendar.MINUTE, 0);
				currCal.set(Calendar.SECOND, 0);
				currCal.set(Calendar.MILLISECOND, 0);
				
				// 설정된 날짜
				Date settedDate = koreanDateFormat.parse(tvDate.getText().toString());
				settedCal = Calendar.getInstance();
				settedCal.setTime(settedDate);
				
				// 설정된 날짜에서 시, 분, 초를 0으로 만든다.
				settedCal.set(Calendar.HOUR_OF_DAY, 0);
				settedCal.set(Calendar.MINUTE, 0);
				settedCal.set(Calendar.SECOND, 0);
				
				// 현재 시간
				currTime = Calendar.getInstance();
				currTime.setTime(currDate);
				
				// 현재 시간에서 년, 월, 일을 0으로 만든다.
				currTime.set(Calendar.YEAR, 0);
				currTime.set(Calendar.MONTH, 0);
				currTime.set(Calendar.DAY_OF_MONTH, 0);
				currTime.set(Calendar.SECOND, 0);
				currTime.set(Calendar.MILLISECOND, 0);
				
				// 시작 시간
				settedDate = koreanTimeFormat.parse(tvTime1.getText().toString());
				startTime = Calendar.getInstance();
				startTime.setTime(settedDate);
				
				// 시작 시간에서 년, 월, 일을 0으로 만든다.
				startTime.set(Calendar.YEAR, 0);
				startTime.set(Calendar.MONTH, 0);
				startTime.set(Calendar.DAY_OF_MONTH, 0);
				
				// 종료 시간
				settedDate = koreanTimeFormat.parse(tvTime2.getText().toString());
				endTime = Calendar.getInstance();
				endTime.setTime(settedDate);
				
				// 종료 시간에서 년, 월, 일을 0으로 만든다.
				endTime.set(Calendar.YEAR, 0);
				endTime.set(Calendar.MONTH, 0);
				endTime.set(Calendar.DAY_OF_MONTH, 0);
				
				//Log.i("cur date check", currCal.toString());
				//Log.i("set date check", settedCal.toString());
				
				Log.i("cur time check", currTime.toString());
				Log.i("strat time check", startTime.toString());
				Log.i("end time check", endTime.toString());
			} catch (ParseException e) {
				Log.e("date check", e.getMessage());
			}
			
			if( tvDate.getText().length() <= 0 ) {
				Toast.makeText(this, "경기 날짜를 설정해주세요.", 0).show();
			} else if(tvTime1.getText().length() <= 0 ) {
				Toast.makeText(this, "시작 시간을 설정해주세요.", 0).show();
			} else if(tvTime2.getText().length() <= 0 ) {
				Toast.makeText(this, "종료 시간을 설정해주세요.", 0).show();
			} else if(tvLocation.getText().length() <= 0 ) {
				Toast.makeText(this, "지역을 설정해주세요.", 0).show();
			} else if(ground.getText().length() <= 0 ) {
				Toast.makeText(this, "경기장을 설정해주세요.", 0).show();
			} else if(currCal.compareTo(settedCal) == 1) {
				Toast.makeText(this, "오늘 날짜보다 이전 날짜로 등록할 수 없습니다.", 0).show();
			} else if(currCal.compareTo(settedCal) == 0 && currTime.compareTo(startTime) != -1) {
				// 현재 날짜와 같은 경우, 현재 시간 이후로 등록해야 한다.
				Toast.makeText(this, "현재 시간 이후로 등록해야 합니다.", 0).show();
			} else if(startTime.compareTo(endTime) != -1) {
				Toast.makeText(this, "종료 시간은 시작 시간 이후로 설정해야 합니다.", 0).show();
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
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "매치를 등록하는 중 입니다...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				// check the success of getting information
				try {
					json = new JSONObject(result);
					
					if (json.getInt("success") == 1) {
						Toast.makeText(AddMatchActivity.this, "매치가 등록되었습니다.", 0).show();
						setResult(RESULT_OK);
						finish();
					}
				} catch (JSONException e) {
					Log.e("addMatch", e.getMessage());
				}
			}
		}.execute();
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
