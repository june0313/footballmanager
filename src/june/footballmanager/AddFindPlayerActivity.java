package june.footballmanager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

public class AddFindPlayerActivity extends Activity implements OnClickListener {
	static final int LOCATION = 1;
	
	EditText title;			// 글 제목
	TextView position;		// 포지션	
	EditText content;		// 글 내용
	TextView actDay;		// 활동 요일
	TextView actTimeStart;	// 활동 시간(시작)
	TextView actTimeEnd;	// 활동 시간(종료)
	Button ok;				// 확인 버튼
	
	// 현재 로그인 정보
	LoginManager lm;
	
	// 다이얼로그에서 선택한 요일	
	public boolean selectedDays[] = new boolean[7];
	
	// 다이얼로그에서 선택한 시작 시간과 종료 시간
	String strActTimeStart;
	String strActTimeEnd;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 레이아웃 설정
		setContentView(R.layout.activity_add_find_player);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setSubtitle("글쓰기");
		
		// 뷰 레퍼런싱
		title = (EditText)findViewById(R.id.title);
		
		position = (TextView)findViewById(R.id.position);
		position.setOnClickListener(this);
		
		actDay = (TextView)findViewById(R.id.act_day);
		actDay.setOnClickListener(this);
		
		actTimeStart = (TextView)findViewById(R.id.act_time1);
		actTimeStart.setOnClickListener(this);
		
		actTimeEnd = (TextView)findViewById(R.id.act_time2);
		actTimeEnd.setOnClickListener(this);
		
		content = (EditText)findViewById(R.id.content);
		
		
		
		ok = (Button)findViewById(R.id.btnOK);
		ok.setOnClickListener(this);
		
		// 로그인 정보 객체 생성
		lm = new LoginManager(this);
	}

	@Override
	public void onClick(final View v) {
		AlertDialog.Builder builder;
		
		switch(v.getId()){
		// 활동 지역
		case R.id.location:
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
			break;
		// 표지션
		case R.id.position:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("포지션을 선택하세요");
			builder.setItems(R.array.positions_long, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					position.setText(AddFindPlayerActivity.this.getResources().getStringArray(R.array.positions_short)[which]);
				}
			});
			builder.create().show();
			break;
		// 활동 요일
		case R.id.act_day:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("활동 요일을 선택하세요");	
			builder.setItems(R.array.days_big, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					if(which < 3)
						actDay.setText(AddFindPlayerActivity.this.getResources().getStringArray(R.array.days_big)[which]);
					else {
						// 직접 요일을 고를수 있도록 다이얼로그를 띄운다.
						AlertDialog.Builder innerBuilder = new AlertDialog.Builder(AddFindPlayerActivity.this);
						innerBuilder.setMultiChoiceItems(R.array.days, null, new DialogInterface.OnMultiChoiceClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								selectedDays[which] = isChecked;
								
								for(int i = 0; i < 7; i++){
									Log.d("선택된 요일 확인", selectedDays[i] + "");
								}
								
							}
						});
						
						innerBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 선택된 요일로 문자열을 구성하여 출력한다.
								String tmpStr = "";
								for(int i = 0; i < 7; i++) {
									if(selectedDays[i] == true) {
										tmpStr += AddFindPlayerActivity.this.getResources().getStringArray(R.array.days_short)[i] + " ";
										selectedDays[i] = false;
									}
								}
								tmpStr = tmpStr.trim();
								actDay.setText(tmpStr);
							}
						});
						innerBuilder.create().show();
					}
				}
			});	// 리스트 생성
			builder.create().show();
			break;
		case R.id.act_time1:
		case R.id.act_time2:
			// 활동 시간 설정
			
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
			
			builder = new AlertDialog.Builder(this);
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
						Log.e("AddFindPlayerActivity", e.getMessage());
					}
					
					//시간 출력 및 서버로 전송할 시간 구성
					if( v.getId() == R.id.act_time1) {
						actTimeStart.setText(time);
						strActTimeStart = newFormat.format(date);
					} else {
						actTimeEnd.setText(time);
						strActTimeEnd = newFormat.format(date);
					}
				};
				
			}).show();
		
			break;
		case R.id.btnOK:
			
			
			if(position.getText().toString().isEmpty())
				Toast.makeText(this, "포지션을 설정해주세요", 0).show();
			else if(actDay.getText().toString().isEmpty())
				Toast.makeText(this, "활동 요일을 설정해주세요", 0).show();
			else if(actTimeStart.getText().toString().isEmpty())
				Toast.makeText(this, "활동 시작 시간을 설정해주세요", 0).show();
			else if(actTimeEnd.getText().toString().isEmpty())
				Toast.makeText(this, "활동 종료 시간을 설정해주세요", 0).show();
			else if(title.getText().toString().isEmpty())
				Toast.makeText(this, "글 제목을 입력해주세요", 0).show();
			else if(content.getText().toString().isEmpty())
				Toast.makeText(this, "내용을 입력해주세요", 0).show();
			else
				// 게시물 등록
				addFindPlayer();
			
			break;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_find_player, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// 선수 모집 게시물을 서버로 등록하는 메서드
	private void addFindPlayer() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.add_find_player);
		
		// 파라미터 구성
		String param = "memberNo=" + lm.getMemberNo()
				+ "&title=" + title.getText().toString()
				+ "&location=" + lm.getLocation()
				+ "&position=" + position.getText().toString()
				+ "&ages=" + lm.getAges()
				+ "&actDay=" + actDay.getText().toString()
				+ "&actTimeStart=" + strActTimeStart
				+ "&actTimeEnd=" + strActTimeEnd
				+ "&content=" + content.getText().toString().replace("\n", "__");
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "게시물을 등록하는 중 입니다...") {

			@Override
			protected void onPostExecute(String result) {
				
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						Toast.makeText(AddFindPlayerActivity.this, "게시물이 등록되었습니다.", 0).show();
						
						// 액티비티를 종료하기 전에 호출한 액티비티에게 정상적으로 작업이 처리되었음을 알린다.
						setResult(RESULT_OK);
						finish();
					}
				} catch (JSONException e) {
					Log.e("addFildPlayer", e.getMessage());
				}
			}
			
		}.execute();
	}
}
