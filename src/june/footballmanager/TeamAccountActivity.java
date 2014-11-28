package june.footballmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TeamAccountActivity extends Activity implements OnClickListener {
	
	LoginManager lm;
	
	TextView teamName;
	TextView location;
	TextView homeGround;
	TextView numOfPlayer;
	TextView ages;
	TextView phone;
	
	ProgressDialog pd;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_team_account);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    lm = new LoginManager(this);
	    
	    TextView currentEmail = (TextView) findViewById(R.id.current_email);
	    currentEmail.setText(lm.getEmail());
	    
	  	teamName = (TextView) findViewById(R.id.a_team_name);
	  	phone = (TextView) findViewById(R.id.a_team_phone);
		location = (TextView) findViewById(R.id.location);
		homeGround = (TextView) findViewById(R.id.home_ground);
		numOfPlayer = (TextView) findViewById(R.id.num_of_player);
		ages = (TextView) findViewById(R.id.ages);
	    
	    // 정보 수정 버튼
	    Button btnEditInfo = (Button) findViewById(R.id.btn_editinfo);
	    btnEditInfo.setOnClickListener(this);
	    
	    // 비밀번호 변경 버튼
	    Button btnUpdatePW = (Button) findViewById(R.id.btn_change_pw);
	    btnUpdatePW.setOnClickListener(this);
	}
	
	public void onResume() {
		super.onResume();
		
		// 프레퍼런스에 저장되어 있는 팀정보를 가져와 출력한다.
		// 정보 수정 후 수정된 내용을 바로 반영하여 출력해야 하기 때문에 onResume에 구현함.
		teamName.setText(lm.getTeamName());
		phone.setText(lm.getPhone());
		location.setText(lm.getLocation());
		homeGround.setText(lm.getHome());
		numOfPlayer.setText(Integer.toString(lm.getNumOfPlayer()));
		ages.setText(lm.getAges());
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if( id == R.id.btn_editinfo) {
			// 정보 수정 버튼 클릭시
			startActivity( new Intent(TeamAccountActivity.this, UpdateTeamInfoActivity.class));
		} else if(id == R.id.btn_change_pw) {
			// 비밀번호 변경 버튼 클릭시
			startActivity( new Intent(TeamAccountActivity.this, UpdatePasswordActivity.class));
		}
		
	}
	
	// 메뉴 출력
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.account_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	// 메뉴 선택
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// 홈버튼 클릭시
			finish();
			return true;
			
		case R.id.logout:
			// 로그아웃 클릭시
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("로그아웃 하시겠습니까?")
			.setNegativeButton("아니오", null)
			.setPositiveButton("예", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					lm.removeLoginInfo();
					Toast.makeText(TeamAccountActivity.this, "로그아웃 되었습니다", 0).show();
					finish();
				}
			});
			builder.create().show();
			return true;
		}

		return false;
	}



}
