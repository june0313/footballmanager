package june.footballmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class AgreementActivity extends Activity implements OnClickListener {
	
	String registerType;
	
	CheckBox cbServiceAgree;	// 서비스 이용 약관 동의 체크박스
	CheckBox cbPrivacyAgree;	// 개인정보 취급 방침 동의 체크박스

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 레이아웃 설정
		setContentView(R.layout.activity_agreement);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		// 뷰 레퍼런스
	    cbServiceAgree = (CheckBox)findViewById(R.id.cbServiceAgree);
	    cbPrivacyAgree = (CheckBox)findViewById(R.id.cbPrivacyAgree);
	    
	    Button btnNext = (Button)findViewById(R.id.btnNext);
	    // btnNext.setEnabled(false);
	    btnNext.setOnClickListener(this);
	    
	    // 계정 타입 가져오기
	    registerType = getIntent().getStringExtra("registerType");
	    
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.btnNext) {
			if(!cbServiceAgree.isChecked())
				Toast.makeText(this, "서비스 이용 약관에 동의하셔야 합니다", 0).show();
			else if(!cbPrivacyAgree.isChecked())
				Toast.makeText(this, "개인정보 취급 방침에 동의하셔야 합니다", 0).show();
			else {
				if(registerType.equals("team")) {
					Intent i = new Intent(AgreementActivity.this, TeamRegisterActivity.class);
					startActivityForResult( i, 0);
				}	
				else if(registerType.equals("player")) {
					Intent i = new Intent(AgreementActivity.this, PlayerRegisterActivity.class);
					startActivityForResult( i, 0);
				}
			}
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(requestCode == 0) {
			if(resultCode == RESULT_OK) finish();
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
}
