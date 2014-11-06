package june.footballmanager;

import java.text.SimpleDateFormat;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SetMatchConditionActivity extends Activity implements OnClickListener {
	ListView searchConditionList;
	
	// ���� ���̾ƿ�
	RelativeLayout condLocation;
	RelativeLayout condTime;
	RelativeLayout condEndTime;
	RelativeLayout condDays;
	RelativeLayout condAges;
	
	// ������ ������ ����� �ؽ�Ʈ��
	TextView txtLocation;
	TextView txtTime;
	TextView txtDays;
	TextView txtAges;
	
	// ���� �Ϸ� ��ư
	Button btnComplete;
	
	// �˻� ���ǵ��� �����ϱ� ���� �����۷���
	SharedPreferences prefCondition;			
	SharedPreferences.Editor prefConditionEditor;
	
	// �ð����� ��� ����
	SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm");
	SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
	
	// �ð��� �迭
	String[] arrTimes;
	
	// �ð� ���� ������ ������ ����
	int selectedTime;
	
	// ���� ���� ���θ� ������ �迭
	boolean[] bDays;
	
	// ���ɴ� ���� ���θ� ������ �迭
	boolean[] bAges;
	
	static final int LOCATION = 1;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_search_condition);
		
		// �׼ǹ� ����
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setIcon(R.drawable.search);
	    
	    // �����۷��� ����
	    prefCondition = getSharedPreferences("matchConditions", MODE_PRIVATE);
	    prefConditionEditor = prefCondition.edit();
	    
	    // �ð��� �迭 ����
	    arrTimes = getResources().getStringArray(R.array.time);
	    
	    // �� ���Ǻ� ���̾ƿ� ��ü ����
	    condLocation = (RelativeLayout) findViewById(R.id.cond_location);
	    condLocation.setOnClickListener(this);
	    
	    condTime = (RelativeLayout) findViewById(R.id.cond_time);
	    condTime.setOnClickListener(this);
	    
	    condDays = (RelativeLayout) findViewById(R.id.cond_days);
	    condDays.setOnClickListener(this);
	    
	    condAges = (RelativeLayout) findViewById(R.id.cond_ages);
	    condAges.setOnClickListener(this);
	    
	    // ���� �Ϸ� ��ư ��ü ����
	    btnComplete = (Button) findViewById(R.id.complete);
	    btnComplete.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// �� ���ǵ��� ����� �ؽ�Ʈ�� ���� �� �ؽ�Ʈ ���
	    txtLocation = (TextView) findViewById(R.id.txt_location);
	    txtLocation.setText(prefCondition.getString("location", "����"));
	    
	    // �����۷����� ����� �ð� ���� ��������
	    selectedTime = prefCondition.getInt("time", 0);
		
	    // ������ �ð� ���� ����ϱ�
	    txtTime = (TextView) findViewById(R.id.txt_start_time);
	    txtTime.setText(arrTimes[selectedTime]);
	    
	    Resources res = getResources();
	  
	    // �����۷����� ����� ���� ���� ��������
	    bDays = new boolean[7];
	    for( int i = 0; i < 7; i++ ) {
	    	bDays[i] = prefCondition.getBoolean("day" + i, true);
	    }
	    
	    // ������ ���� ���
	    String[] days = res.getStringArray(R.array.days_short);
	    String daysForDisplay = "";
	    for( int i = 0; i < 7; i++ ) {
	    	if( bDays[i] )
	    		daysForDisplay += " " + days[i];
	    }
	    txtDays = (TextView) findViewById(R.id.txt_days);
	    txtDays.setText(daysForDisplay);
	    
	    // �����۷����� ����� ���ɴ� ���� ��������
	    bAges = new boolean[6];
	    for( int i = 0; i < 6; i++ ) {
	    	bAges[i] = prefCondition.getBoolean("age" + i, true);
	    }
	    
	    // ������ ���ɴ� ���
	    String[] ages = res.getStringArray(R.array.ages);
	    String agesForDisplay = "";
	    for( int i = 0; i < 6; i++ ) {
	    	if( bAges[i] ) 
	    		agesForDisplay += " " + ages[i];
	    }
	    txtAges = (TextView) findViewById(R.id.txt_ages);
	    txtAges.setText(agesForDisplay);
	    
	}
	
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set_search_condition, menu);
		return true;
	}
	*/
	
	// �޴� ���ý� �ݹ� �޼���
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId()) {
		case android.R.id.home :
			finish();
			return true;
		}
		
		return false;
	}

	// �� ���Ǻ� Ŭ���� ���� �ݹ� �޼���
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if( id == R.id.cond_location ) {
			
			// ���� ���� ��Ƽ��Ƽ ȣ��
			startActivityForResult( new Intent(this, LocationConditionActivity.class), LOCATION );
			
		} else if( id == R.id.cond_time ) {
			
			// �ð� ����
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(R.array.time, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					selectedTime = which;
					
					// ������ �ð� ������ �����۷����� �����Ѵ�.
					prefConditionEditor.putInt("time", selectedTime);
					prefConditionEditor.commit();
					
					// ������ �ð� ������ ����Ѵ�.
					txtTime.setText(arrTimes[selectedTime]);
				}
			});
			builder.setTitle("��� �ð��� �����ϼ���");
			builder.create().show();
			
		} else if( id == R.id.cond_days ) {
			
			// ��� ���� ����
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("������ �����ϼ���");
			builder.setMultiChoiceItems(R.array.days, bDays, new DialogInterface.OnMultiChoiceClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					bDays[which] = isChecked;
				}
				
			});
			builder.setPositiveButton("Ȯ��", daySetListener);
			builder.create().show();		
		} else if ( id == R.id.cond_ages ) {
			
			// ����� ���ɴ� ����
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("���ɴ븦 �����ϼ���");
			builder.setMultiChoiceItems(R.array.ages, bAges, new DialogInterface.OnMultiChoiceClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					bAges[which] = isChecked;
				}
			} );
			builder.setPositiveButton("Ȯ��", ageSetListener);
			builder.create().show();			
		} else if ( id == R.id.complete ) {
			Toast.makeText(SetMatchConditionActivity.this, "�˻� ������ �����Ǿ����ϴ�", 0).show();
			finish();
		}
	}
	
	// ���� ���� �޾ƿ���
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOCATION:
			if (resultCode == RESULT_OK) {
				// �޾ƿ� ���������� �����۷����� �����Ѵ�.
				prefConditionEditor.putString("location", intent.getStringExtra("location"));
				prefConditionEditor.commit();
			}
		}
	}
	
	// ���� ���� ���̾�α� ������
	private DialogInterface.OnClickListener daySetListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			
			// ������ ���� ������ �����۷����� �����Ѵ�.
			for( int i = 0; i < 7; i++ ) {
				prefConditionEditor.putBoolean("day" + i, bDays[i]);
				prefConditionEditor.commit();
			}
			
			 // �����۷����� ����� ���� ���� ��������
		    for( int i = 0; i < 7; i++ ) {
		    	bDays[i] = prefCondition.getBoolean("day" + i, true);
		    }
		    
		    // ������ ���� ���
		    Resources res = getResources();
		    String[] days = res.getStringArray(R.array.days_short);
		    String daysForDisplay = "";
		    for( int i = 0; i < 7; i++ ) {
		    	if( bDays[i] )
		    		daysForDisplay += " " + days[i];
		    }

		    txtDays.setText(daysForDisplay);
		}
	};
	
	// ���ɴ� ���� ���̾�α� ������
	private DialogInterface.OnClickListener ageSetListener = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// ������ ���ɴ� ������ �����۷����� �����Ѵ�.
			for( int i = 0; i < 6; i++ ) {
				prefConditionEditor.putBoolean("age" + i, bAges[i]);
				prefConditionEditor.commit();
			}
			
			// �����۷����� ����� ���ɴ� ���� ��������
		    for( int i = 0; i < 6; i++ ) {
		    	bAges[i] = prefCondition.getBoolean("age" + i, true);
		    }
		    
		    // ������ ���ɴ� ���
		    Resources res = getResources();
		    String[] ages = res.getStringArray(R.array.ages);
		    String agesForDisplay = "";
		    for( int i = 0; i < 6; i++ ) {
		    	if( bAges[i] ) 
		    		agesForDisplay += " " + ages[i];
		    }

		    txtAges.setText(agesForDisplay);
		}
	};
}