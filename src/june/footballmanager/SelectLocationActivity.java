package june.footballmanager;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SelectLocationActivity extends Activity implements OnItemSelectedListener, OnClickListener {
	
	Spinner spnrSIDO;
	Spinner spnrGUGUN;
	Spinner spnrDONG;
	Button complete;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_select_location);
	    
	    // 액션바 설정
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    // 시, 도 스피너
	    spnrSIDO = (Spinner)findViewById(R.id.sido);
	    spnrSIDO.setOnItemSelectedListener(this);
	    
	    // 구, 군 스피너
	    spnrGUGUN = (Spinner)findViewById(R.id.gugun);
	    spnrGUGUN.setOnItemSelectedListener(this);
	    
	    // 동(면리) 스피너
	    spnrDONG = (Spinner)findViewById(R.id.dong);
	    spnrDONG.setOnItemSelectedListener(this);
	    
	    // 설정 완료 버튼
	    complete = (Button)findViewById(R.id.complete);
	    complete.setOnClickListener(this);
	    
	    // 스피너 초기 데이터 로드
	    loadSIDOData();
	    loadGUGUNData(spnrSIDO.getSelectedItem().toString());
	    loadDONGData(spnrSIDO.getSelectedItem().toString(), spnrGUGUN.getSelectedItem().toString());

	}

	// 스피너 아이템 선택
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long arg3) {
		Log.i("FM", "onItemSelected");
		
		int id = parent.getId();
		if (id == R.id.sido) {
			loadGUGUNData(spnrSIDO.getSelectedItem().toString());
		} else if (id == R.id.gugun) {
			loadDONGData(spnrSIDO.getSelectedItem().toString(), spnrGUGUN.getSelectedItem().toString());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
	
	// 버튼 클릭
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.complete) {
			// 주소 결합
			String location = spnrSIDO.getSelectedItem().toString() + " "
				+ spnrGUGUN.getSelectedItem().toString() + " "
				+ spnrDONG.getSelectedItem().toString();
			// 주소 전달 : 자신을 호출한 Activity로 결과값을 넘긴다.
			Intent intent = new Intent();
			intent.putExtra("location", location);
			setResult(RESULT_OK, intent);
			finish();
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
	

	
	private void loadSIDOData() {
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		
		// spinner drop down elements
		List<String> SIDOList = db.getSIDOList();
		
		for(int i = 0; i < SIDOList.size(); i++) {
			Log.i("FM", SIDOList.get(i).toString());
		}
		
		// create adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, SIDOList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spnrSIDO.setAdapter(dataAdapter);
	}
	
	private void loadGUGUNData(String sido) {
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		
		// spinner drop down elements
		List<String> GUGUNList = db.getGUGUNList(sido);
		
		for(int i = 0; i < GUGUNList.size(); i++) {
			Log.i("FM", GUGUNList.get(i).toString());
		}
		
		// create adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, GUGUNList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spnrGUGUN.setAdapter(dataAdapter);
	}
	
	private void loadDONGData(String sido, String gugun) {
		DatabaseHandler db = new DatabaseHandler(getApplicationContext());
		
		// spinner drop down elements
		List<String> DONGList = db.getDONGList(sido, gugun);
		
		for(int i = 0; i < DONGList.size(); i++) {
			Log.i("FM", DONGList.get(i).toString());
		}
		
		// create adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, DONGList);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		spnrDONG.setAdapter(dataAdapter);
	}

	

}
