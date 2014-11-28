package june.footballmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SetFindTeamConditionActivity extends Activity implements OnClickListener {
	// 조건 레이아웃
		RelativeLayout condLocation;
		RelativeLayout condPosition;
		RelativeLayout condAge;
		
		// 설정된 조건을 출력할 텍스트뷰
		TextView txtLocation;
		TextView txtPosition;
		TextView txtAge;
		
		// 설정 완료 버튼
		Button btnComplete;
		
		// 검색 조건들을 저장하기 위한 프리퍼런스
		SharedPreferences prefCondition;			
		SharedPreferences.Editor prefConditionEditor;
		
		// 연령대 설정 여부를 저장할 배열
		boolean[] bAges;
		
		// 포지션 설정 여부를 저장할 배열
		boolean[] bPosition;
		
		static final int LOCATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_find_team_condition);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(R.drawable.search);
		actionBar.setSubtitle("검색 조건 설정");
		
		// 각 조건별 레이아웃 객체 생성
	    condLocation = (RelativeLayout) findViewById(R.id.cond_location);
	    condLocation.setOnClickListener(this);
	    
	    condPosition = (RelativeLayout) findViewById(R.id.cond_position);
	    condPosition.setOnClickListener(this);
	    
	    condAge = (RelativeLayout) findViewById(R.id.cond_age);
	    condAge.setOnClickListener(this);
	    
	    // 설정 완료 버튼 객체 생성
	    btnComplete = (Button) findViewById(R.id.complete);
	    btnComplete.setOnClickListener(this);
		
		// 프리퍼런스 열기
	    prefCondition = getSharedPreferences("findTeam", MODE_PRIVATE);
	    prefConditionEditor = prefCondition.edit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Resources res = getResources();
		
		// 각 조건들을 출력할 텍스트뷰 생성 및 텍스트 출력
		// 위치 검색 조건 출력
	    txtLocation = (TextView) findViewById(R.id.txt_location);
	    txtLocation.setText(prefCondition.getString("location", "전국"));
	    
	    // 프리퍼런스에 저장된 포지션 정보 가져오기
	    bPosition = new boolean[15];
	    for( int i = 0; i < 15; i++ ) {
	    	bPosition[i] = prefCondition.getBoolean("pos" + i, true);
	    }
	    
	    // 가져온 포지션 출력
	    String[] positions = res.getStringArray(R.array.positions_short);
	    String positionsForDisplay = "";
	    for(int i = 0; i < positions.length; i++) {
	    	if(bPosition[i])
	    		positionsForDisplay += " " + positions[i];
	    }
	    txtPosition = (TextView)findViewById(R.id.txt_position);
	    txtPosition.setText(positionsForDisplay);
	    
		// 나이 검색 조건 출력
		txtAge = (TextView) findViewById(R.id.txt_age);
		txtAge.setText(prefCondition.getInt("startAge", 0) + "세 ~ "
				+ prefCondition.getInt("endAge", 99) + "세");
	}
	
	// 각 조건뷰 클릭에 대한 콜백 메서드
	@Override
	public void onClick(View v) {
		AlertDialog.Builder builder;

		switch (v.getId()) {
		case R.id.cond_location:
			// 지역 설정 액티비티 호출
			startActivityForResult(new Intent(this,
					LocationConditionActivity.class), LOCATION);
			break;

		case R.id.cond_position:
			// 선수 포지션 설정
			builder = new AlertDialog.Builder(this);
			builder.setTitle("포지션을 선택하세요");
			builder.setMultiChoiceItems(R.array.positions_long, bPosition,
					new DialogInterface.OnMultiChoiceClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							bPosition[which] = isChecked;
						}
					});
			builder.setPositiveButton("확인", posSetListener);
			builder.create().show();
			break;

		case R.id.cond_age:
			// 선수 나이 설정
			/*
			builder = new AlertDialog.Builder(this);
			builder.setTitle("연령대를 선택하세요");
			builder.setMultiChoiceItems(R.array.ages, bAges,
					new DialogInterface.OnMultiChoiceClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							bAges[which] = isChecked;
						}
					});
			builder.setPositiveButton("확인", ageSetListener);
			builder.create().show();
			*/
			
			builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_set_age, null);
			final NumberPicker startAge = (NumberPicker)layout.findViewById(R.id.start_age);
			final NumberPicker endAge = (NumberPicker)layout.findViewById(R.id.end_age);
			
			startAge.setMinValue(0);
			startAge.setMaxValue(99);
			startAge.setValue(prefCondition.getInt("startAge", 0));
			
			endAge.setMinValue(0);
			endAge.setMaxValue(99);
			endAge.setValue(prefCondition.getInt("endAge", 99));
			
			builder.setView(layout);
			builder.setTitle("나이를 선택하세요");
			builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 나이 조건 출력
					txtAge.setText(startAge.getValue() + "세 ~ " + endAge.getValue() + "세");
					
					// 나이 조건 저장
					prefConditionEditor.putInt("startAge", startAge.getValue());
					prefConditionEditor.putInt("endAge", endAge.getValue());
					prefConditionEditor.commit();
				}
				
			});
			builder.create().show();
			break;

		case R.id.complete:
			Toast.makeText(SetFindTeamConditionActivity.this, "검색 조건이 설정되었습니다",
					0).show();
			finish();
			break;
		}
	}
	
	// 지역 정보 받아오기
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOCATION:
			if (resultCode == RESULT_OK) {
				// 받아온 지역정보를 프리퍼런스에 저장한다.
				prefConditionEditor.putString("location",
						intent.getStringExtra("location"));
				prefConditionEditor.commit();
			}
		}
	}

	// 포지션 설정 다이얼로그 리스너
	private DialogInterface.OnClickListener posSetListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// 설정된 포지션 정보를 프리퍼런스에 저장한다.
			for (int i = 0; i < 15; i++) {
				prefConditionEditor.putBoolean("pos" + i, bPosition[i]);
				prefConditionEditor.commit();
			}

			// 프리퍼런스에 저장된 포지션 정보 가져오기
			for (int i = 0; i < 15; i++) {
				bPosition[i] = prefCondition.getBoolean("pos" + i, true);
			}

			// 가져온 포지션 출력
			Resources res = getResources();
			String[] positinos = res.getStringArray(R.array.positions_short);
			String posForDisplay = "";
			for (int i = 0; i < 15; i++) {
				if (bPosition[i])
					posForDisplay += " " + positinos[i];
			}

			txtPosition.setText(posForDisplay);
		}
	};

	// 나이 설정 다이얼로그 리스너
	private DialogInterface.OnClickListener ageSetListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// 설정된 연령대 정보를 프리퍼런스에 저장한다.
			for (int i = 0; i < 6; i++) {
				prefConditionEditor.putBoolean("age" + i, bAges[i]);
				prefConditionEditor.commit();
			}

			// 프리퍼런스에 저장된 연령대 정보 가져오기
			for (int i = 0; i < 6; i++) {
				bAges[i] = prefCondition.getBoolean("age" + i, true);
			}

			// 가져온 연령대 출력
			Resources res = getResources();
			String[] ages = res.getStringArray(R.array.ages);
			String agesForDisplay = "";
			for (int i = 0; i < 6; i++) {
				if (bAges[i])
					agesForDisplay += " " + ages[i];
			}

			txtAge.setText(agesForDisplay);
		}
	};

	// 메뉴 선택시 콜백 메서드
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return false;
	}


}
