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
			UpdatePlayerInfo uti = new UpdatePlayerInfo();
			uti.execute();
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
	
	public class UpdatePlayerInfo extends AsyncTask<Void, Void, Boolean> {
		String param;
		String jsonString = "";
		JSONObject jsonObj;
		
		@Override
		public void onPreExecute() {
			param = "memberNo=" + lm.getMemberNo()
				+ "&nickname=" + nickname.getText().toString()
				+ "&location=" + location.getText().toString()
				+ "&position=" + position.getText().toString()
				+ "&age=" + age.getText().toString()
				+ "&phone=" + phone.getText().toString();
			
			pd = new ProgressDialog(UpdatePlayerInfoActivity.this);
			pd.setMessage("잠시만 기다려 주세요.");
			pd.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean isSuccess = false;
			Log.i("FM", "param : " + param);
			
			try {
				URL url = new URL(getString(R.string.server) + getString(R.string.update_player_info));
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
				Log.e("FM", "UpdatePlayerInfo : " + e.getMessage());
			} catch (MalformedURLException e) {
				Log.e("FM", "UpdatePlayerInfo : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "UpdatePlayerInfo : " + e.getMessage());
			} catch (JSONException e) {
				Log.e("FM", "UpdatePlayerInfo : " + e.getMessage());;
			}
				
			return isSuccess;
		}
		
		@Override
		public void onPostExecute(Boolean isSuccess) {
			pd.dismiss();
			
			if( isSuccess ) {
				lm.setPlayerInfo(
						position.getText().toString(), 
						Integer.parseInt(age.getText().toString()), 
						nickname.getText().toString(), 
						phone.getText().toString(),
						location.getText().toString());
				
				Toast.makeText(getApplicationContext(), "정보가 수정되었습니다.", 0).show();
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "정보 수정에 실패하였습니다.", 0).show();
			}
		}
	}
}
