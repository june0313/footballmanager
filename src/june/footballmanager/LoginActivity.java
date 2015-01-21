package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {

	RadioGroup memberGroup ;
	protected EditText email;
	protected EditText password;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_login);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    // 로그인 버튼
	    Button btnLogin = (Button)findViewById(R.id.btn_login);
	    btnLogin.setOnClickListener( this );
	    
	    // 선수회원 가입 버튼
	    Button btnPlayerRegister = (Button)findViewById(R.id.btn_player_register);
	    btnPlayerRegister.setOnClickListener( this );
	    
	    // 팀회원 가입 버튼
	    Button btnTeamRegister = (Button)findViewById(R.id.btn_team_register);
	    btnTeamRegister.setOnClickListener( this );
	    
	    // 비밀번호 찾기 텍스트
	    TextView txtFindPassword = (TextView)findViewById(R.id.txt_find_pw);
	    txtFindPassword.setOnClickListener( this );
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_login) {
			memberGroup = (RadioGroup) findViewById(R.id.rg_member_type);
			email = (EditText)findViewById(R.id.p_email);
			password = (EditText)findViewById(R.id.p_password);
			if( memberGroup.getCheckedRadioButtonId() == -1 ) {
				Toast.makeText(this, "회원 유형을 선택해주세요.", 0).show();
			}
			else if( email.getText().length() <= 0 ) {
				Toast.makeText(this, "이메일을 입력해주세요.", 0).show();
			} 
			else if( password.getText().length() <= 0 ) {
				Toast.makeText(this, "비밀번호를 입력해주세요.", 0).show();
			}
			else {
				// 위의 모든 조건을 만족하면 로그인 시도
				ProgressDialog pd = new ProgressDialog(this);
				pd.setMessage("로그인 중 입니다...");
				pd.show();
				
				attemptLogin();
				
				pd.dismiss();
			}
		} else if (id == R.id.btn_player_register) {
			// 약관 동의 화면을 보여준다
			startActivity(new Intent(LoginActivity.this, AgreementActivity.class).putExtra("registerType", "player"));
			// startActivity( new Intent(LoginActivity.this, PlayerRegisterActivity.class));
		} else if (id == R.id.btn_team_register) {
			// 약관 동의 화면을 보여준다
			startActivity(new Intent(LoginActivity.this, AgreementActivity.class).putExtra("registerType", "team"));
			// startActivity( new Intent(LoginActivity.this, TeamRegisterActivity.class));
		} else if (id == R.id.txt_find_pw) {
			startActivity( new Intent(LoginActivity.this, FindPasswordActivity.class));
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
	
	private void attemptLogin() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server)+ getString(R.string.login);
		
		// 파라미터 구성
		RadioButton btnMemberType = (RadioButton)findViewById(memberGroup.getCheckedRadioButtonId());
		final String memberType = btnMemberType.getText().toString();
		final String email = LoginActivity.this.email.getText().toString();
		final String password = LoginActivity.this.password.getText().toString();
		final String param = "memberType=" + memberType + "&email=" + email + "&password=" + password;
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "로그인 중 입니다...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				// check the success of login
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						
						// 로그인한 계정 정보를 저장한다.
						LoginManager lm = new LoginManager(LoginActivity.this);
						lm.setLoginInfo(memberType, email, password);
						
						// 로그인한 회원의 번호를 저장한다.
						lm.setMemberNo(json.getInt("MEMBER_NO"));
						Log.i("MEMBER_NO", Integer.toString(lm.getMemberNo()));
						
						// 각 회원 유형에 맞는 정보를 저장한다.
						if( memberType.equals("팀회원"))
							lm.setTeamInfo(json.getString("TEAM_NAME"), 
									json.getString("LOCATION"), json.getString("HOME"), 
									json.getInt("NUM_OF_PLAYERS"), json.getString("AGES"),
									json.getString("PHONE"));
						else
							lm.setPlayerInfo(json.getString("POSITION"), 
									json.getInt("AGE"), json.getString("NICKNAME"), 
									json.getString("PHONE"), json.getString("LOCATION"));
						
						Toast.makeText(LoginActivity.this, "로그인 되었습니다.", 0).show();
						
						// memberId와 registration ID를 DB에 저장한다.
						GCMManager gm = new GCMManager(LoginActivity.this);
						gm.checkAndRegister();
						gm.sendRegistrationIdToBackend();
					
						finish();
					} else {
						Toast.makeText(LoginActivity.this, "이메일 또는 비밀번호가 올바르지 않습니다.", 0).show();
					}
				} catch (JSONException e) {
					Log.e("attemptLogin", e.getMessage());
				}
			}
			
		}.execute();
		
			
	}
}
