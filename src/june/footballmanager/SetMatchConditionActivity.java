package june.footballmanager;

import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SetMatchConditionActivity extends Activity implements OnClickListener {
	ListView searchConditionList;
	
	// 조건 레이아웃
	RelativeLayout condLocation;
	RelativeLayout condTime;
	RelativeLayout condEndTime;
	RelativeLayout condDays;
	RelativeLayout condAges;
	
	// 설정된 조건을 출력할 텍스트뷰
	TextView txtLocation;
	TextView txtTime;
	TextView txtDays;
	TextView txtAges;
	
	// 설정 완료 버튼
	Button btnComplete;
	
	// 검색 조건들을 저장하기 위한 프리퍼런스
	SharedPreferences prefCondition;			
	SharedPreferences.Editor prefConditionEditor;
	
	// 시간조건 출력 포맷
	SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm");
	SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
	
	// 시간대 배열
	String[] arrTimes;
	
	// 시간 설정 정보를 임시로 저장하는 정수
	int selectedTime;
	
	// 요일 설정 여부를 저장할 배열
	boolean[] bDays;
	
	// 연령대 설정 여부를 저장할 배열
	boolean[] bAges;
	
	static final int LOCATION = 1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_search_condition);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setIcon(R.drawable.search);
	    
	    // 프리퍼런스 열기
	    prefCondition = getSharedPreferences("matchConditions", MODE_PRIVATE);
	    prefConditionEditor = prefCondition.edit();
	    
	    // 시간대 배열 생성
	    arrTimes = getResources().getStringArray(R.array.time);
	    
	    // 각 조건별 레이아웃 객체 생성
	    condLocation = (RelativeLayout) findViewById(R.id.cond_location);
	    condLocation.setOnClickListener(this);
	    
	    condTime = (RelativeLayout) findViewById(R.id.cond_time);
	    condTime.setOnClickListener(this);
	    
	    condDays = (RelativeLayout) findViewById(R.id.cond_days);
	    condDays.setOnClickListener(this);
	    
	    condAges = (RelativeLayout) findViewById(R.id.cond_ages);
	    condAges.setOnClickListener(this);
	    
	    // 설정 완료 버튼 객체 생성
	    btnComplete = (Button) findViewById(R.id.complete);
	    btnComplete.setOnClickListener(this);
	    
	    // 저장되어있는 검색 조건을 각 뷰에 출력한다.
	    printStoredCondition();
	}
	
	private void printStoredCondition() {
		
		// 위치 검색 조건 출력
	    txtLocation = (TextView) findViewById(R.id.txt_location);
	    txtLocation.setText(prefCondition.getString("location", "전국"));
	    
	    // 프리퍼런스에 저장된 시간 정보 가져오기
	    selectedTime = prefCondition.getInt("time", 0);
		
	    // 가져온 시간 정보 출력하기
	    txtTime = (TextView) findViewById(R.id.txt_start_time);
	    txtTime.setText(arrTimes[selectedTime]);
	    
	    Resources res = getResources();
	  
	    // 프리퍼런스에 저장된 요일 정보 가져오기
	    bDays = new boolean[7];
	    for( int i = 0; i < 7; i++ ) {
	    	bDays[i] = prefCondition.getBoolean("day" + i, true);
	    }
	    
	    // 가져온 요일 출력
	    String[] days = res.getStringArray(R.array.days_short);
	    String daysForDisplay = "";
	    for( int i = 0; i < 7; i++ ) {
	    	if( bDays[i] )
	    		daysForDisplay += " " + days[i];
	    }
	    txtDays = (TextView) findViewById(R.id.txt_days);
	    txtDays.setText(daysForDisplay);
	    
	    // 프리퍼런스에 저장된 연령대 정보 가져오기
	    bAges = new boolean[6];
	    for( int i = 0; i < 6; i++ ) {
	    	bAges[i] = prefCondition.getBoolean("age" + i, true);
	    }
	    
	    // 가져온 연령대 출력
	    String[] ages = res.getStringArray(R.array.ages);
	    String agesForDisplay = "";
	    for( int i = 0; i < 6; i++ ) {
	    	if( bAges[i] ) 
	    		agesForDisplay += " " + ages[i];
	    }
	    txtAges = (TextView) findViewById(R.id.txt_ages);
	    txtAges.setText(agesForDisplay);
	    
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set_search_condition, menu);
		return true;
	}
	*/
	
	// 메뉴 선택시 콜백 메서드
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId()) {
		case android.R.id.home :
			finish();
			return true;
		}
		
		return false;
	}

	// 각 조건뷰 클릭에 대한 콜백 메서드
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if( id == R.id.cond_location ) {
			
			// 지역 설정 액티비티 호출
			startActivityForResult( new Intent(this, LocationConditionActivity.class), LOCATION );
			
		} else if( id == R.id.cond_time ) {
			
			// 시간 설정
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(R.array.time, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					selectedTime = which;
					
					// 설정된 시간 정보를 출력한다.
					txtTime.setText(arrTimes[selectedTime]);
				}
			});
			builder.setTitle("경기 시간을 선택하세요");
			builder.create().show();
			
		} else if( id == R.id.cond_days ) {
			
			// 경기 요일 설정
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("요일을 선택하세요");
			builder.setMultiChoiceItems(R.array.days, bDays, new DialogInterface.OnMultiChoiceClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					bDays[which] = isChecked;
				}
				
			});
			builder.setPositiveButton("확인", daySetListener);
			builder.create().show();		
		} else if ( id == R.id.cond_ages ) {
			
			// 상대팀 연령대 설정
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("연령대를 선택하세요");
			builder.setMultiChoiceItems(R.array.ages, bAges, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					bAges[which] = isChecked;
				}
			} );
			builder.setPositiveButton("확인", ageSetListener);
			builder.create().show();		
		} else if ( id == R.id.complete ) {	// "설정 완료" 버튼 클릭
			// 임시로 설정된 지역정보를 프리퍼런스에 저장한다.
			prefConditionEditor.putString("location", txtLocation.getText().toString());
			
			// 임시로 설정된 시간 정보를 프리퍼런스에 저장한다.
			prefConditionEditor.putInt("time", selectedTime);

			// 임시로 설정된 요일 정보를 프리퍼런스에 저장한다.
			for (int i = 0; i < 7; i++) {
				prefConditionEditor.putBoolean("day" + i, bDays[i]);
			}

			// 임시로 설정된 연령대 정보를 프리퍼런스에 저장한다.
			for (int i = 0; i < 6; i++) {
				prefConditionEditor.putBoolean("age" + i, bAges[i]);
			}
			
			// 변경 내용을 커밋한다.
			prefConditionEditor.commit();
			
			Toast.makeText(SetMatchConditionActivity.this, "검색 조건이 설정되었습니다", 0).show();
			
			// 호출한 액티비티에 조건이 설정되었음을 알린다.
			setResult(RESULT_OK);
			finish();
		}
	}
	
	// 지역 정보 받아오기
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOCATION:
			if (resultCode == RESULT_OK) {
				// 받아온 지역정보를 출력한다.
				txtLocation.setText(intent.getStringExtra("location"));
			}
		}
	}
	
	// 요일 설정 다이얼로그 리스너
	private DialogInterface.OnClickListener daySetListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
		    // 임시로 설정된 요일 출력
		    Resources res = getResources();
		    String[] days = res.getStringArray(R.array.days_short);
		    String daysForDisplay = "";
		    for( int i = 0; i < 7; i++ ) {
		    	if( bDays[i] )
		    		daysForDisplay += " " + days[i];
		    }

		    txtDays.setText(daysForDisplay);
		}
	};
	
	// 연령대 설정 다이얼로그 리스너
	private DialogInterface.OnClickListener ageSetListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
		    
		    // 임시로 설정된 연령대 출력
		    Resources res = getResources();
		    String[] ages = res.getStringArray(R.array.ages);
		    String agesForDisplay = "";
		    for( int i = 0; i < 6; i++ ) {
		    	if( bAges[i] ) 
		    		agesForDisplay += " " + ages[i];
		    }

		    txtAges.setText(agesForDisplay);
		}
	};
}
