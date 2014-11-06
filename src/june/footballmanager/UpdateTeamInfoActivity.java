package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
			UpdateTeamInfo uti = new UpdateTeamInfo();
			uti.execute();
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
	
	public class UpdateTeamInfo extends AsyncTask<Void, Void, Boolean> {
		String param;
		String jsonString = "";
		JSONObject jsonObj;
		
		@Override
		public void onPreExecute() {
			param = "email=" + lm.getEmail()
				+ "&name=" + etTeamName.getText().toString()
				+ "&location=" + tvLocation.getText().toString()
				+ "&home=" + etHomeGround.getText().toString()
				+ "&numOfPlayer=" + etNumOfPlayer.getText().toString()
				+ "&ages=" + tvAges.getText().toString()
				+ "&phone=" + etPhone.getText().toString();
			
			pd = new ProgressDialog(UpdateTeamInfoActivity.this);
			pd.setMessage("잠시만 기다려 주세요.");
			pd.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean isSuccess = false;
			Log.i("FM", "param : " + param);
			
			try {
				URL url = new URL(getString(R.string.server) + getString(R.string.update_team_info));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream(), "euc-kr" );
				out.write(param);
				out.flush();
				out.close();
				
				String buffer = null;
				BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream(), "euc-kr"));
				while( (buffer = in.readLine()) != null ) {
					jsonString += buffer;
				}
				in.close();
				
				jsonObj = new JSONObject(jsonString);
				
				
				// check the success of registration
				if(jsonObj.getInt("success") == 1)
					isSuccess = true;
					
			} catch (ProtocolException e) {
				Log.e("FM", "UpdateTeamInfo : " + e.getMessage());
			} catch (MalformedURLException e) {
				Log.e("FM", "UpdateTeamInfo : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "UpdateTeamInfo : " + e.getMessage());
			} catch (JSONException e) {
				Log.e("FM", "UpdateTeamInfo : " + e.getMessage());;
			}
				
			return isSuccess;
		}
		
		@Override
		public void onPostExecute(Boolean isSuccess) {
			pd.dismiss();
			
			if( isSuccess ) {
				lm.setTeamInfo(etTeamName.getText().toString(), 
						tvLocation.getText().toString(), 
						etHomeGround.getText().toString(), 
						Integer.parseInt(etNumOfPlayer.getText().toString()), 
						tvAges.getText().toString(),
						etPhone.getText().toString());
				
				Toast.makeText(getApplicationContext(), "정보가 수정되었습니다.", 0).show();
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "정보 수정에 실패하였습니다.", 0).show();
			}
		}
	}
}
