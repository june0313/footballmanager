package june.footballmanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SetFindTeamConditionActivity extends Activity implements OnClickListener {
	// ���� ���̾ƿ�
		RelativeLayout condLocation;
		RelativeLayout condPosition;
		RelativeLayout condAge;
		
		// ������ ������ ����� �ؽ�Ʈ��
		TextView txtLocation;
		TextView txtPosition;
		TextView txtAge;
		
		// ���� �Ϸ� ��ư
		Button btnComplete;
		
		// �˻� ���ǵ��� �����ϱ� ���� �����۷���
		SharedPreferences prefCondition;			
		SharedPreferences.Editor prefConditionEditor;
		
		// ���ɴ� ���� ���θ� ������ �迭
		boolean[] bAges;
		
		// ������ ���� ���θ� ������ �迭
		boolean[] bPosition;
		
		static final int LOCATION = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_find_team_condition);
		
		// �׼ǹ� ����
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(R.drawable.search);
		actionBar.setSubtitle("�˻� ���� ����");
		
		// �� ���Ǻ� ���̾ƿ� ��ü ����
	    condLocation = (RelativeLayout) findViewById(R.id.cond_location);
	    condLocation.setOnClickListener(this);
	    
	    condPosition = (RelativeLayout) findViewById(R.id.cond_position);
	    condPosition.setOnClickListener(this);
	    
	    condAge = (RelativeLayout) findViewById(R.id.cond_age);
	    condAge.setOnClickListener(this);
	    
	    // ���� �Ϸ� ��ư ��ü ����
	    btnComplete = (Button) findViewById(R.id.complete);
	    btnComplete.setOnClickListener(this);
		
		// �����۷��� ����
	    prefCondition = getSharedPreferences("findTeam", MODE_PRIVATE);
	    prefConditionEditor = prefCondition.edit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Resources res = getResources();
		
		// �� ���ǵ��� ����� �ؽ�Ʈ�� ���� �� �ؽ�Ʈ ���
		// ��ġ �˻� ���� ���
	    txtLocation = (TextView) findViewById(R.id.txt_location);
	    txtLocation.setText(prefCondition.getString("location", "����"));
	    
	    // �����۷����� ����� ������ ���� ��������
	    bPosition = new boolean[15];
	    for( int i = 0; i < 15; i++ ) {
	    	bPosition[i] = prefCondition.getBoolean("pos" + i, true);
	    }
	    
	    // ������ ������ ���
	    String[] positions = res.getStringArray(R.array.positions_short);
	    String positionsForDisplay = "";
	    for(int i = 0; i < positions.length; i++) {
	    	if(bPosition[i])
	    		positionsForDisplay += " " + positions[i];
	    }
	    txtPosition = (TextView)findViewById(R.id.txt_position);
	    txtPosition.setText(positionsForDisplay);
	    
		// ���� �˻� ���� ���
		txtAge = (TextView) findViewById(R.id.txt_age);
		txtAge.setText(prefCondition.getInt("startAge", 0) + "�� ~ "
				+ prefCondition.getInt("endAge", 99) + "��");
	}
	
	// �� ���Ǻ� Ŭ���� ���� �ݹ� �޼���
	@Override
	public void onClick(View v) {
		AlertDialog.Builder builder;

		switch (v.getId()) {
		case R.id.cond_location:
			// ���� ���� ��Ƽ��Ƽ ȣ��
			startActivityForResult(new Intent(this,
					LocationConditionActivity.class), LOCATION);
			break;

		case R.id.cond_position:
			// ���� ������ ����
			builder = new AlertDialog.Builder(this);
			builder.setTitle("�������� �����ϼ���");
			builder.setMultiChoiceItems(R.array.positions_long, bPosition,
					new DialogInterface.OnMultiChoiceClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							bPosition[which] = isChecked;
						}
					});
			builder.setPositiveButton("Ȯ��", posSetListener);
			builder.create().show();
			break;

		case R.id.cond_age:
			// ���� ���� ����
			/*
			builder = new AlertDialog.Builder(this);
			builder.setTitle("���ɴ븦 �����ϼ���");
			builder.setMultiChoiceItems(R.array.ages, bAges,
					new DialogInterface.OnMultiChoiceClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which,
								boolean isChecked) {
							bAges[which] = isChecked;
						}
					});
			builder.setPositiveButton("Ȯ��", ageSetListener);
			builder.create().show();
			*/
			
			builder = new AlertDialog.Builder(this);
			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dialog_set_age, null);
			final NumberPicker startAge = (NumberPicker)layout.findViewById(R.id.start_age);
			final NumberPicker endAge = (NumberPicker)layout.findViewById(R.id.end_age);
			
			startAge.setMinValue(0);
			startAge.setMaxValue(99);
			startAge.setValue(prefCondition.getInt("startAge", 0));
			
			endAge.setMinValue(0);
			endAge.setMaxValue(99);
			endAge.setValue(prefCondition.getInt("endAge", 99));
			
			builder.setView(layout);
			builder.setTitle("���̸� �����ϼ���");
			builder.setPositiveButton("Ȯ��", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// ���� ���� ���
					txtAge.setText(startAge.getValue() + "�� ~ " + endAge.getValue() + "��");
					
					// ���� ���� ����
					prefConditionEditor.putInt("startAge", startAge.getValue());
					prefConditionEditor.putInt("endAge", endAge.getValue());
					prefConditionEditor.commit();
				}
				
			});
			builder.create().show();
			break;

		case R.id.complete:
			Toast.makeText(SetFindTeamConditionActivity.this, "�˻� ������ �����Ǿ����ϴ�",
					0).show();
			finish();
			break;
		}
	}
	
	// ���� ���� �޾ƿ���
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOCATION:
			if (resultCode == RESULT_OK) {
				// �޾ƿ� ���������� �����۷����� �����Ѵ�.
				prefConditionEditor.putString("location",
						intent.getStringExtra("location"));
				prefConditionEditor.commit();
			}
		}
	}

	// ������ ���� ���̾�α� ������
	private DialogInterface.OnClickListener posSetListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// ������ ������ ������ �����۷����� �����Ѵ�.
			for (int i = 0; i < 15; i++) {
				prefConditionEditor.putBoolean("pos" + i, bPosition[i]);
				prefConditionEditor.commit();
			}

			// �����۷����� ����� ������ ���� ��������
			for (int i = 0; i < 15; i++) {
				bPosition[i] = prefCondition.getBoolean("pos" + i, true);
			}

			// ������ ������ ���
			Resources res = getResources();
			String[] positinos = res.getStringArray(R.array.positions_short);
			String posForDisplay = "";
			for (int i = 0; i < 15; i++) {
				if (bPosition[i])
					posForDisplay += " " + positinos[i];
			}

			txtPosition.setText(posForDisplay);
		}
	};

	// ���� ���� ���̾�α� ������
	private DialogInterface.OnClickListener ageSetListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// ������ ���ɴ� ������ �����۷����� �����Ѵ�.
			for (int i = 0; i < 6; i++) {
				prefConditionEditor.putBoolean("age" + i, bAges[i]);
				prefConditionEditor.commit();
			}

			// �����۷����� ����� ���ɴ� ���� ��������
			for (int i = 0; i < 6; i++) {
				bAges[i] = prefCondition.getBoolean("age" + i, true);
			}

			// ������ ���ɴ� ���
			Resources res = getResources();
			String[] ages = res.getStringArray(R.array.ages);
			String agesForDisplay = "";
			for (int i = 0; i < 6; i++) {
				if (bAges[i])
					agesForDisplay += " " + ages[i];
			}

			txtAge.setText(agesForDisplay);
		}
	};

	// �޴� ���ý� �ݹ� �޼���
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return false;
	}


}
