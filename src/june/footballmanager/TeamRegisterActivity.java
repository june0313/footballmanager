package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// 팀회원 가입 액티비티
public class TeamRegisterActivity extends Activity implements OnClickListener {

	EditText email;
	EditText password;
	EditText passwordChk;
	
	EditText teamName;
	TextView location;
	EditText homeGround;
	EditText numOfPlayer;
	TextView ages;
	EditText phone;
	EditText introduce;
	
	// 유니폼
	View uniformTop;
	
	Button btnRegister;
	
	ProgressDialog pd;
	
	static final int LOCATION = 1;
	static final int GROUND = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_register);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    // 필수 계정 정보
	    email = (EditText)findViewById(R.id.t_email);
	    password = (EditText)findViewById(R.id.t_password);
	    passwordChk = (EditText)findViewById(R.id.t_password_chk);
	    
	    // 팀정보
	    teamName = (EditText)findViewById(R.id.t_team_name);
	    location = (TextView)findViewById(R.id.t_location);
	    location.setOnClickListener(this);
	    
	    // 홈구장
	    homeGround = (EditText)findViewById(R.id.t_homeground);
	    // 경기장을 검색해서 선택할 수 있도록 수정할 예정
	    // homeGround.setOnClickListener(this);
	    
	    numOfPlayer = (EditText)findViewById(R.id.t_num_of_player);
	    ages = (TextView) findViewById(R.id.t_ages);
	    ages.setOnClickListener(this);
	    
	    // 연락처
	    phone = (EditText)findViewById(R.id.t_phone);
	    
	    // 팀 소개
	    introduce = (EditText)findViewById(R.id.t_introduce);
	    
	    // 가입버튼
	    btnRegister = (Button)findViewById(R.id.btn_register);
	    btnRegister.setOnClickListener(this);
	    
	    // 유니폼
	    /*
	    uniformTop = findViewById(R.id.t_uniform_top);
	    uniformTop.setOnClickListener(this);
	    */
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.t_ages) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("연령층을 선택하세요");
			builder.setItems(R.array.ages, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ages.setText(TeamRegisterActivity.this.getResources().getStringArray(R.array.ages)[which]);
				}
			});
			AlertDialog ad = builder.create();
			ad.show();
		} else if (id == R.id.t_location) {
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
		} else if( id == R.id.t_homeground) {
			startActivityForResult(new Intent(this, SelectGroundActivity.class), GROUND);
		} /*else if( id == R.id.t_uniform_top ) {
			
			// initialColor is the initially-selected color to be shown in the rectangle on the left of the arrow.
			// for example, 0xff000000 is black, 0xff0000ff is blue. Please be aware of the initial 0xff which is the alpha.
			AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, 0xff000000, new OnAmbilWarnaListener() {
				@Override
				public void onOk(AmbilWarnaDialog dialog, int color) {
					// color is the color selected by the user.
				}
			                
				@Override
				public void onCancel(AmbilWarnaDialog dialog) {
					// cancel was selected by the user
				}
			});

			dialog.show();

		}*/else if (id == R.id.btn_register) {
			if( email.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "이메일을 입력해주세요", 0).show();
			} else if( password.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "비밀번호를 입력해주세요", 0).show();
			} else if ( password.getText().length() < 6 ) {
				Toast.makeText(TeamRegisterActivity.this, "비밀번호는 최소 6자리 이상 입력해주세요", 0).show();
			} else if( !password.getText().toString().equals(passwordChk.getText().toString()) ) {
				Toast.makeText(TeamRegisterActivity.this, "비밀번호가 일치하지 않습니다", 0).show();
			} else if ( !email.getText().toString().matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$") ) {
				Toast.makeText(TeamRegisterActivity.this, "이메일 주소가 잘못되었습니다", 0).show();
			} else if( teamName.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "팀 이름을 입력해주세요", 0).show();
			} else if( location.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "지역을 입력해주세요", 0).show();
			} else if( homeGround.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "홈 구장을 입력해주세요", 0).show();
			} else if( numOfPlayer.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "인원수를 입력해주세요", 0).show();
			} else if( ages.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "연령층을 입력해주세요", 0).show();
			} else if( phone.getText().length() <= 0 ) {
				Toast.makeText(TeamRegisterActivity.this, "연락처를 입력해주세요", 0).show();
			} else {
				// 위의 모든 조건을 만족하면 팀 회원 가입 시도
				registerTeamAccount();
			}
		}
	}
	
	// 주소 받아오기
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case LOCATION:
			if(resultCode == RESULT_OK) {
				location.setText(intent.getStringExtra("location"));
			}
		}
	}
	
	// 메뉴 선택
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId()) {
		case android.R.id.home :
			finish();
			return true;
		}
		
		return false;
	}
	
	private void registerTeamAccount() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server)+ getString(R.string.regi_team);
		
		// 파라미터 구성
		String param = "email=" + email.getText().toString();
		param += "&password=" + password.getText().toString();
		param += "&name=" + teamName.getText().toString();
		param += "&location=" + location.getText().toString();
		param += "&home=" + homeGround.getText().toString();
		param += "&numOfPlayer=" + numOfPlayer.getText().toString();
		param += "&ages=" + ages.getText().toString();
		param += "&phone=" + phone.getText().toString();
		param += "&introduce=" + introduce.getText().toString().replace("\n", "__");
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						Toast.makeText(TeamRegisterActivity.this, "팀 계정 등록이 완료되었습니다!", 0).show();
						finish();
					} else {
						int errno = json.getInt("errorcode");
						String errorMsg = json.getString("message");
						Log.e("registerTeamAccount", errorMsg);
						
						if( errno == 1062)
							Toast.makeText(TeamRegisterActivity.this, "이미 등록된 이메일 주소입니다", 0).show();
					}
				} catch (JSONException e) {
					Log.e("registerTeamAccount", e.getMessage());
				}
			}
			
		}.execute();
		
	}
}
