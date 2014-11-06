package june.footballmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SetFindPlayerConditionActivity extends Activity implements OnClickListener{
	// 조건 레이아웃
	RelativeLayout condLocation;
	RelativeLayout condPosition;
	RelativeLayout condAges;
	
	// 설정된 조건을 출력할 텍스트뷰
	TextView txtLocation;
	TextView txtPosition;
	TextView txtAges;
	
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
		setContentView(R.layout.activity_set_find_player_condition);
		
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
	    
	    condAges = (RelativeLayout) findViewById(R.id.cond_ages);
	    condAges.setOnClickListener(this);
	    
	    // 설정 완료 버튼 객체 생성
	    btnComplete = (Button) findViewById(R.id.complete);
	    btnComplete.setOnClickListener(this);
		
		// 프리퍼런스 열기
	    prefCondition = getSharedPreferences("findPlayer", MODE_PRIVATE);
	    prefConditionEditor = prefCondition.edit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Resources res = getResources();
		
		// 각 조건들을 출력할 텍스트뷰 생성 및 텍스트 출력
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
	    
	    // 프리퍼런스에 저장된 연령대 정보 가져오기
	    bAges = new boolean[6];
	    for( int i = 0; i < 6; i++ ) {
	    	bAges[i] = prefCondition.getBoolean("age" + i, true);
	    }
	    
	    // 가져온 연령대 출력
	    String[] ages = res.getStringArray(R.array.ages);
	    String agesForDisplay = "";
	    for( int i = 0; i < ages.length; i++ ) {
	    	if( bAges[i] ) 
	    		agesForDisplay += " " + ages[i];
	    }
	    txtAges = (TextView) findViewById(R.id.txt_ages);
	    txtAges.setText(agesForDisplay);
	}
	
	// 각 조건뷰 클릭에 대한 콜백 메서드
	@Override
	public void onClick(View v) {
		AlertDialog.Builder builder;
		
		switch(v.getId()) {
		case R.id.cond_location:
			// 지역 설정 액티비티 호출
			startActivityForResult( new Intent(this, LocationConditionActivity.class), LOCATION );
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
			
		case R.id.cond_ages:
			// 선수 연령대 설정
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
			break;
			
		case R.id.complete:
			Toast.makeText(SetFindPlayerConditionActivity.this, "검색 조건이 설정되었습니다", 0).show();
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
	
	// 포지션 설정 다이얼로그 리스너
	private DialogInterface.OnClickListener posSetListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// 설정된 포지션 정보를 프리퍼런스에 저장한다.
			for(int i = 0; i < 15; i++) {
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
	
	// 연령대 설정 다이얼로그 리스너
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

			txtAges.setText(agesForDisplay);
		}
	};
}
