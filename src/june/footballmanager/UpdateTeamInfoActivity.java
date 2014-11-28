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

public class UpdateTeamInfoActivity extends Activity implements OnClickListener {
	private static final int LOCATION = 1;
	
	EditText etTeamName;
	EditText etPhone;
	TextView tvLocation;
	EditText etHomeGround;
	EditText etNumOfPlayer;
	TextView tvAges;
	
	LoginManager lm;
	
	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_team_info);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    etTeamName = (EditText)findViewById(R.id.et_team_name);
	    etPhone = (EditText)findViewById(R.id.et_team_phone);
	    tvLocation = (TextView)findViewById(R.id.et_location);
	    etHomeGround = (EditText)findViewById(R.id.et_home_ground);
	    etNumOfPlayer = (EditText)findViewById(R.id.et_num_of_player);
	    tvAges = (TextView)findViewById(R.id.et_ages);
	    
	    lm = new LoginManager(this);
	    
	    etTeamName.setText(lm.getTeamName());
	    etPhone.setText(lm.getPhone());
	    tvLocation.setText(lm.getLocation());
	    tvLocation.setOnClickListener(this);
	    etHomeGround.setText(lm.getHome());
	    etNumOfPlayer.setText(Integer.toString(lm.getNumOfPlayer()));
	    tvAges.setText(lm.getAges());
	    tvAges.setOnClickListener(this);
	    
	    Button btnComplete = (Button)findViewById(R.id.btn_edit_complete);
	    btnComplete.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if( id == R.id.et_location)
			startActivityForResult(new Intent(this, SelectLocationActivity.class), LOCATION);
		
		else if( id == R.id.et_ages ) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("연령층을 선택하세요");
			builder.setItems(R.array.ages, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					tvAges.setText(UpdateTeamInfoActivity.this.getResources().getStringArray(R.array.ages)[which]);
				}
			});
			AlertDialog ad = builder.create();
			ad.show();
			
		} else if( id == R.id.btn_edit_complete) {
			// 팀 정보를 업데이트 한다.
			updateTeamInfo();
			Log.i("FM","complete");
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch(requestCode) {
		case LOCATION:
			if(resultCode == RESULT_OK) {
				tvLocation.setText(intent.getStringExtra("location"));
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
	
	// 수정된 팀 정보를 서버에 업데이트 하는 메서드
	private void updateTeamInfo() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.update_team_info);
		
		// 파라미터 구성
		String param = "email=" + lm.getEmail()
				+ "&name=" + etTeamName.getText().toString()
				+ "&location=" + tvLocation.getText().toString()
				+ "&home=" + etHomeGround.getText().toString()
				+ "&numOfPlayer=" + etNumOfPlayer.getText().toString()
				+ "&ages=" + tvAges.getText().toString()
				+ "&phone=" + etPhone.getText().toString();
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if( json.getInt("success") == 1 ) {
						lm.setTeamInfo(etTeamName.getText().toString(), 
								tvLocation.getText().toString(), 
								etHomeGround.getText().toString(), 
								Integer.parseInt(etNumOfPlayer.getText().toString()), 
								tvAges.getText().toString(),
								etPhone.getText().toString());
						
						Toast.makeText(UpdateTeamInfoActivity.this, "정보가 수정되었습니다.", 0).show();
						finish();
					} else {
						Toast.makeText(UpdateTeamInfoActivity.this, "정보 수정에 실패하였습니다.", 0).show();
					}
				} catch (NumberFormatException e) {
					Log.e("updateTeamInfo", e.getMessage());
				} catch (JSONException e) {
					Log.e("updateTeamInfo", e.getMessage());
				}
				
			}
			
		}.execute();
		
	}
}
