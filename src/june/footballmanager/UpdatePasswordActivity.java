package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UpdatePasswordActivity extends Activity implements OnClickListener {

	EditText etCurPw;
    EditText etNewPw;
    EditText etNewPwConfirm;
    Button btnUpdatePw;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_password);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);
	    
	    // 뷰 레퍼런스
	    etCurPw = (EditText) findViewById(R.id.cur_password);
	    etNewPw = (EditText) findViewById(R.id.new_password);
	    etNewPwConfirm = (EditText) findViewById(R.id.new_password_confirm);
	    btnUpdatePw = (Button) findViewById(R.id.btn_update_password);
	    btnUpdatePw.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		LoginManager lm = new LoginManager(this);
		if(v.getId() == R.id.btn_update_password) {
			if(etCurPw.getText().length() == 0)
				Toast.makeText(this, "현재 비밀번호를 입력해주세요", 0).show();
			else if(etNewPw.getText().length() == 0)
				Toast.makeText(this, "변경할 비밀번호를 입력해주세요", 0).show();
			else if( etNewPw.getText().length() < 6 )
				Toast.makeText(this, "변경할 비밀번호는 최소 6자리 이상 입력해주세요.", 0).show();
			else if(etNewPwConfirm.getText().length() == 0)
				Toast.makeText(this, "변경할 비밀번호를 한번 더 입력해주세요", 0).show();
			else if(!etCurPw.getText().toString().equals(lm.getPassword()))
				Toast.makeText(this, "현재 비밀번호가 올바르지 않습니다", 0).show();
			else if(!etNewPw.getText().toString().equals(etNewPwConfirm.getText().toString()))
				Toast.makeText(this, "변경할 비밀번호가 일치하지 않습니다", 0).show();
			else
				updatePassword();
		}
	}
	
	private void updatePassword() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.update_password); 
		
		// 파라미터 구성
		LoginManager lm = new LoginManager(this);
		int memberType;
		
		if(lm.getMemberType().equals("팀회원"))
			memberType = 0;
		else
			memberType = 1;
		String param = "memberType=" + memberType;
		param += "&email=" + lm.getEmail();
		param += "&passwd=" + etNewPw.getText().toString();
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						new LoginManager(UpdatePasswordActivity.this).setPassword(etNewPw.getText().toString());
						Toast.makeText(UpdatePasswordActivity.this, "비밀번호가 변경되었습니다!", 0).show();
						finish();
					}
				} catch (JSONException e) {
					Log.e("updatePassword", e.getMessage());
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
