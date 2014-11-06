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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
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
	
	ProgressDialog pd;
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
	    
	    // �α��� ��ư
	    Button btnLogin = (Button)findViewById(R.id.btn_login);
	    btnLogin.setOnClickListener( this );
	    
	    // ����ȸ�� ���� ��ư
	    Button btnPlayerRegister = (Button)findViewById(R.id.btn_player_register);
	    btnPlayerRegister.setOnClickListener( this );
	    
	    // ��ȸ�� ���� ��ư
	    Button btnTeamRegister = (Button)findViewById(R.id.btn_team_register);
	    btnTeamRegister.setOnClickListener( this );
	    
	    // ��й�ȣ ã�� �ؽ�Ʈ
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
				Toast.makeText(getApplicationContext(), "ȸ�� ������ �������ּ���.", 0).show();
			}
			else if( email.getText().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "�̸����� �Է����ּ���.", 0).show();
			} 
			else if( password.getText().length() <= 0 ) {
				Toast.makeText(getApplicationContext(), "��й�ȣ�� �Է����ּ���.", 0).show();
			}
			else {
				// ���� ��� ������ �����ϸ� �α��� �õ�
				//hideKeyboard();
				//new Login().execute();
				AttemptLogin al = new AttemptLogin();
				al.execute();
			}
		} else if (id == R.id.btn_player_register) {
			startActivity( new Intent(LoginActivity.this, PlayerRegisterActivity.class));
			//Toast.makeText(getApplicationContext(), "���� �غ��� �Դϴ�.", 0).show();
		} else if (id == R.id.btn_team_register) {
			startActivity( new Intent(LoginActivity.this, TeamRegisterActivity.class));
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
	
	// Login through AsyncTask
	public class AttemptLogin extends AsyncTask<Void, Void, Boolean> {
		RadioButton btnMemberType = (RadioButton)findViewById(memberGroup.getCheckedRadioButtonId());
		String memberType = btnMemberType.getText().toString();
		String email = LoginActivity.this.email.getText().toString();
		String password = LoginActivity.this.password.getText().toString();
		String param = "memberType=" + memberType + "&email=" + email + "&password=" + password;
		
		String jsonString = "";
		JSONObject jsonObj;
		
		@Override
		public void onPreExecute() {
			pd = new ProgressDialog(LoginActivity.this);
			pd.setMessage("��ø� ��ٷ� �ּ���.");
			pd.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			
			Boolean isLoginSuccess = false;
			
			try {
				URL url = new URL(getString(R.string.server)+ getString(R.string.login));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream(), "euc-kr" );
				out.write(param);
				out.flush();
				out.close();
				
				String buffer = null;
				BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream(), "euc-kr" ));
				while( (buffer = in.readLine()) != null ) {
					jsonString += buffer;
				}
				in.close();
				
				Log.i("FM", "Login result : " + jsonString);
				
				jsonObj = new JSONObject(jsonString);
				
				// check the success of login
				if(jsonObj.getInt("success") == 1) {
					isLoginSuccess = true;
					
					// �α����� ���� ������ �����Ѵ�.
					LoginManager lm = new LoginManager(LoginActivity.this);
					lm.setLoginInfo(memberType, email, password);
					
					// �α����� ȸ���� ��ȣ�� �����Ѵ�.
					lm.setMemberNo(jsonObj.getInt("MEMBER_NO"));
					Log.i("MEMBER_NO", Integer.toString(lm.getMemberNo()));
					
					// �� ȸ�� ������ �´� ������ �����Ѵ�.
					if( memberType.equals("��ȸ��"))
						lm.setTeamInfo(jsonObj.getString("TEAM_NAME"), 
								jsonObj.getString("LOCATION"), jsonObj.getString("HOME"), 
								jsonObj.getInt("NUM_OF_PLAYERS"), jsonObj.getString("AGES"),
								jsonObj.getString("PHONE"));
					else
						lm.setPlayerInfo(jsonObj.getString("POSITION"), 
								jsonObj.getInt("AGE"), jsonObj.getString("NICKNAME"), 
								jsonObj.getString("PHONE"), jsonObj.getString("LOCATION"));
				}
			} catch (MalformedURLException e) {
				Log.e("FM", "AttemptLogin : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "AttemptLogin : " + e.getMessage());
			} catch (JSONException e) {
				Log.e("FM", "AttemptLogin : " + e.getMessage());
			}
			
			return isLoginSuccess;
		}
		
		@Override
		public void onPostExecute(Boolean isLoginSuccess) {
			pd.dismiss();
			
			if( isLoginSuccess ) {
				Toast.makeText(getApplicationContext(), "�α��� �Ǿ����ϴ�.", 0).show();
				
				// memberId�� registration ID�� DB�� �����Ѵ�.
				GCMManager gm = new GCMManager(LoginActivity.this);
				gm.sendRegistrationIdToBackend();
				finish();
			} else {
				Toast.makeText(getApplicationContext(), "�̸��� �Ǵ� ��й�ȣ�� �ùٸ��� �ʽ��ϴ�.", 0).show();
			}
		}
	}
}
