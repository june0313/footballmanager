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

public class UpdatePlayerInfoActivity extends Activity implements OnClickListener {
	private static final int LOCATION = 1;
	
	EditText nickname;
	EditText phone;
	TextView location;
	TextView position;
	EditText age;
	
	LoginManager lm;
	
	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_player_info);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    lm = new LoginManager(this);
	    
	    nickname = (EditText)findViewById(R.id.nickname);
	    nickname.setText(lm.getNickname());
	    
	    phone = (EditText)findViewById(R.id.phone);
	    phone.setText(lm.getPhone());
	    
	    location = (TextView)findViewById(R.id.location);
	    location.setText(lm.getLocation());
	    location.setOnClickListener(this);
	    
	    position = (TextView)findViewById(R.id.position);
	    position.setText(lm.getPosition());
	    position.setOnClickListener(this);
	    
	    age = (EditText)findViewById(R.id.age);
	    age.setText(lm.getAge() + "");
	    
	    Button btnComplete = (Button)findViewById(R.id.btn_edit_complete);
	    btnComplete.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if( id == R.id.location)
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
		
		else if( id == R.id.position) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("포지션을 선택하세요");
			builder.setItems(R.array.positions_long, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					position.setText(UpdatePlayerInfoActivity.this.getResources().getStringArray(R.array.positions_short)[which]);
				}
			});
			builder.create().show();
		}
		else if( id == R.id.btn_edit_complete) {
			// 선수정보를 업데이트한다.
			updatePlayerInfo();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case LOCATION:
			if(resultCode == RESULT_OK) {
				location.setText(intent.getStringExtra("location"));
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return false;
	}
	
	// 수정된 선수 정보를 서버에 업데이트 하는 메서드
	private void updatePlayerInfo() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.update_player_info);
		
		// 파라미터 구성
		String param = "memberNo=" + lm.getMemberNo()
				+ "&nickname=" + nickname.getText().toString()
				+ "&location=" + location.getText().toString()
				+ "&position=" + position.getText().toString()
				+ "&age=" + age.getText().toString()
				+ "&phone=" + phone.getText().toString();
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if( json.getInt("success") == 1) {
						lm.setPlayerInfo(
								position.getText().toString(), 
								Integer.parseInt(age.getText().toString()), 
								nickname.getText().toString(), 
								phone.getText().toString(),
								location.getText().toString());
						
						Toast.makeText(UpdatePlayerInfoActivity.this, "정보가 수정되었습니다.", 0).show();
						finish();
					} else {
						Toast.makeText(UpdatePlayerInfoActivity.this, "정보 수정에 실패하였습니다.", 0).show();
					}
				} catch (NumberFormatException e) {
					Log.e("updatePlayerInfo", e.getMessage());
				} catch (JSONException e) {
					Log.e("updatePlayerInfo", e.getMessage());
				}
				
			}
			
		}.execute();
		
	}
}
