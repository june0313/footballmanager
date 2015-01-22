/*
 비밀번호 찾기 화면
 */

package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class FindPasswordActivity extends Activity {
	
	RadioGroup memberGroup;
	TextView tvEmail;
	Button btnSend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_password);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    // 뷰 레퍼런스
	    memberGroup = (RadioGroup)findViewById(R.id.rg_member_type);
	    tvEmail = (TextView)findViewById(R.id.tv_email);
	    btnSend = (Button)findViewById(R.id.btn_send);
	    btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if( memberGroup.getCheckedRadioButtonId() == -1 ) {
					Toast.makeText(FindPasswordActivity.this, "회원 유형을 선택해주세요.", 0).show();
				} else if(tvEmail.getText().toString().isEmpty())
					Toast.makeText(FindPasswordActivity.this, "이메일 주소를 입력해주세요", 0).show();
				else {
					// 비밀번호를 초기화하고 메일로 전송한다.
					init_password();
				}
			}
			
	    });
	}
	
	private void init_password() {
		String url = getResources().getString(R.string.server)+ getResources().getString(R.string.init_password); 
		RadioButton btnMemberType = (RadioButton)findViewById(memberGroup.getCheckedRadioButtonId());
		int memberType;
		if(btnMemberType.getText().toString().equals("팀회원"))
			memberType = 0;
		else memberType = 1;
		String param = "memberType=" + memberType
				+ "&email=" + tvEmail.getText().toString();
		
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {
			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						Toast.makeText(FindPasswordActivity.this, "메일이 발송되었습니다.", 0).show();
						finish();
					} else if(json.getInt("success") == 4)
						Toast.makeText(FindPasswordActivity.this, "메일주소가 존재하지 않습니다.", 0).show();
				} catch (JSONException e) {
					Log.e("init_password", e.getMessage());
				}
			}
		}.execute();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.find_password, menu);
		return true;
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