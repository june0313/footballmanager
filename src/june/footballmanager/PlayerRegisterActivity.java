package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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

public class PlayerRegisterActivity extends Activity implements OnClickListener{
	
	static final int LOCATION = 1;
	
	// 계정정보 뷰
	EditText email;
	EditText password;
	EditText passwordChk;

	// 선수정보 뷰
	EditText nickname;
	TextView position;
	EditText age;
	TextView location;
	EditText phone;

	// 등록 버튼
	Button btnRegister;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_player_register);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
		// 계정정보
	    email = (EditText)findViewById(R.id.p_email);
	    password = (EditText)findViewById(R.id.p_password);
	    passwordChk = (EditText)findViewById(R.id.p_password_chk);
	    
	    // 선수정보
	    nickname = (EditText)findViewById(R.id.p_nickname);
	    
	    position = (TextView)findViewById(R.id.p_position);
	    position.setOnClickListener(this);
	    
	    age = (EditText)findViewById(R.id.p_age);
	    
	    location = (TextView)findViewById(R.id.p_location);
	    location.setOnClickListener(this);
	    
	    phone = (EditText)findViewById(R.id.p_phone);
	    
	    btnRegister = (Button)findViewById(R.id.btn_register);
	    btnRegister.setOnClickListener(this);
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

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_register) {
			if( email.getText().length() <= 0 ) {
				Toast.makeText(this, "이메일을 입력해주세요.", 0).show();
			} else if( password.getText().length() <= 0 ) {
				Toast.makeText(this, "비밀번호를 입력해주세요.", 0).show();
			} else if( nickname.getText().toString().length() <= 0 ) {
				Toast.makeText(this, "닉네임을 입력해주세요.", 0).show();
			} else if( position.getText().toString().length() <= 0 ) {
				Toast.makeText(this, "포지션을 선택해주세요.", 0).show();
			} else if( age.getText().toString().length() <= 0 ) {
				Toast.makeText(this, "나이를 입력해주세요.", 0).show();
			} else if( location.getText().toString().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "지역을 선택해주세요.", 0).show();
			} else if( phone.getText().toString().length() <= 0 ) {
				Toast.makeText(this, "연락처를 입력해주세요.", 0).show();
			} else if( password.getText().length() < 6 ) {
				Toast.makeText(this, "비밀번호는 최소 6자리 이상 입력해주세요.", 0).show();
			} else if( !password.getText().toString().equals(passwordChk.getText().toString()) ) {
				Toast.makeText(this, "비밀번호가 일치하지 않습니다.", 0).show();
			} else if( !email.getText().toString().matches("^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$") ) {
				Toast.makeText(this, "이메일 주소가 잘못되었습니다.", 0).show();
			} else {
				// 회원가입 실행
				registerPlayerAccount();
			}
		} else if( id == R.id.p_position ) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("포지션을 선택하세요");
			builder.setItems(R.array.positions_long, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					position.setText(PlayerRegisterActivity.this.getResources().getStringArray(R.array.positions_short)[which]);
				}
			});
			AlertDialog ad = builder.create();
			ad.show();
		} else if ( id == R.id.p_location ) {
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
		}
	}

	// 주소 받아오기
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOCATION:
			if (resultCode == RESULT_OK) {
				location.setText(intent.getStringExtra("location"));
			}
		}
	}
	
	private void registerPlayerAccount() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server)+ getString(R.string.regi_player);
		
		// 파라미터 구성
		String param = "email=" + email.getText().toString();
		param += "&password=" + password.getText().toString();
		param += "&nickname=" + nickname.getText().toString();
		param += "&position=" + position.getText().toString();
		param += "&age=" + age.getText().toString();
		param += "&location=" + location.getText().toString();
		param += "&phone=" + phone.getText().toString();
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				// check the success of registration
				try {
					json = new JSONObject(result);
					if (json.getInt("success") == 1) {
						Toast.makeText(PlayerRegisterActivity.this, "선수 계정 등록이 완료되었습니다!", 0).show();
						finish();
					} else {
						int errorcode = json.getInt("errorcode");
						String errormsg = json.getString("errormsg");
						
						if (errorcode == 1062 && errormsg.contains("EMAIL"))
							Toast.makeText(PlayerRegisterActivity.this, "이미 등록된 이메일 주소입니다.", 0).show();
						else if(errorcode == 1062 && errormsg.contains("NICKNAME")) 
							Toast.makeText(PlayerRegisterActivity.this, "중복되는 닉네임이 존재합니다.", 0).show();
						else
							Toast.makeText(PlayerRegisterActivity.this, "선수 계정 등록에 실패하였습니다.", 0).show();
					}
				} catch (JSONException e) {
					Log.e("registerPlayerAccount", e.getMessage());
				}
			}
			
		}.execute();
		
	}
}
