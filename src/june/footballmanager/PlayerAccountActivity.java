package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PlayerAccountActivity extends Activity implements OnClickListener {
	LoginManager lm;
	
	TextView nickname;
	TextView position;
	TextView age;
	TextView location;
	TextView phone;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_player_account);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    lm = new LoginManager(this);
	    
	    TextView currentEmail = (TextView) findViewById(R.id.current_email);
	    currentEmail.setText(lm.getEmail());
	    
	    nickname = (TextView)findViewById(R.id.nickname);
	    position = (TextView) findViewById(R.id.position);
	    age = (TextView) findViewById(R.id.age);
	    location = (TextView) findViewById(R.id.location);
	    phone = (TextView)findViewById(R.id.phone);
	    
	    // 정보수정 버튼
	    Button btnEditInfo = (Button) findViewById(R.id.btn_editinfo);
	    btnEditInfo.setOnClickListener(this);
	}
	
	public void onResume() {
		super.onResume();
		nickname.setText(lm.getNickname());
		position.setText(lm.getPosition());
		age.setText(lm.getAge() + "세");
		location.setText(lm.getLocation());
		phone.setText(lm.getPhone());
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if(id == R.id.btn_editinfo)
			// 정보 수정 버튼 클릭시
			startActivity( new Intent(PlayerAccountActivity.this, UpdatePlayerInfoActivity.class));
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
		// 홈버튼 클릭시
		case android.R.id.home:
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
					Toast.makeText(PlayerAccountActivity.this, "로그아웃 되었습니다", 0).show();
					finish();
				}
			});
			builder.create().show();
			return true;
		}

		return false;
	}

}
