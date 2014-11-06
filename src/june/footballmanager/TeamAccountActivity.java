package june.footballmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TeamAccountActivity extends Activity implements OnClickListener {
	
	LoginManager lm;
	
	TextView teamName;
	TextView location;
	TextView homeGround;
	TextView numOfPlayer;
	TextView ages;
	TextView phone;
	
	ProgressDialog pd;

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_team_account);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    lm = new LoginManager(this);
	    
	    TextView currentEmail = (TextView) findViewById(R.id.current_email);
	    currentEmail.setText(lm.getEmail());
	    
	  	teamName = (TextView) findViewById(R.id.a_team_name);
	  	phone = (TextView) findViewById(R.id.a_team_phone);
		location = (TextView) findViewById(R.id.location);
		homeGround = (TextView) findViewById(R.id.home_ground);
		numOfPlayer = (TextView) findViewById(R.id.num_of_player);
		ages = (TextView) findViewById(R.id.ages);
		
		Button logout = (Button) findViewById(R.id.btn_logout);
	    logout.setOnClickListener(this);
	    
	    Button btnEditInfo = (Button) findViewById(R.id.btn_editinfo);
	    btnEditInfo.setOnClickListener(this);
	}
	
	public void onResume() {
		super.onResume();
		
		// �����۷����� ����Ǿ� �ִ� �������� ������ ����Ѵ�.
		// ���� ���� �� ������ ������ �ٷ� �ݿ��Ͽ� ����ؾ� �ϱ� ������ onResume�� ������.
		teamName.setText(lm.getTeamName());
		phone.setText(lm.getPhone());
		location.setText(lm.getLocation());
		homeGround.setText(lm.getHome());
		numOfPlayer.setText(Integer.toString(lm.getNumOfPlayer()));
		ages.setText(lm.getAges());
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if( id == R.id.btn_editinfo) {
			// ���� ���� ��ư Ŭ����
			startActivity( new Intent(TeamAccountActivity.this, UpdateTeamInfoActivity.class));
		}
		
	}
	
	// �޴� ���
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.account_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	// �޴� ����
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// Ȩ��ư Ŭ����
			finish();
			return true;
			
		case R.id.logout:
			// �α׾ƿ� Ŭ����
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("�α׾ƿ� �Ͻðڽ��ϱ�?")
			.setNegativeButton("�ƴϿ�", null)
			.setPositiveButton("��", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					lm.removeLoginInfo();
					Toast.makeText(TeamAccountActivity.this, "�α׾ƿ� �Ǿ����ϴ�", 0).show();
					finish();
				}
			});
			builder.create().show();
			return true;
		}

		return false;
	}



}
