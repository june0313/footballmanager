package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// 메치 상세 정보 페이지
public class MatchDetailActivity extends Activity implements GoogleMap.OnMapClickListener {

	// 매치 번호
	int matchNo;
	
	// 매치 상태
	int matchState;
	
	// 매치를 등록한 팀의 번호
	int memberNo;
	
	// 매치를 등록한 팀의 연락처
	String phone;
	
	// 매치를 등록한 팀의 GCM Registration ID
	String regid;
	
	ProgressDialog pd;
	
	TextView teamName;
	TextView teamInfo;
	TextView date;
	TextView time;
	TextView location;
	TextView ground;
	TextView detail;
	TextView state;
	
	ImageButton call;
	ImageButton sms;
	ImageButton info;
	
	GoogleMap map;
	LatLng point;
	
	// 로그인 정보
	LoginManager lm;
	
	// 상대팀 이메일 정보
	String opposingTeamEmail;
	
	// 매치 신청 메시지
	String applyMsg;
	
	// GCM 관리자 객체
	GCMManager gm;
	
	// 스크랩 여부를 저장하는 변수
	boolean isScrapped = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_match_detail);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);
	    
	    // 매치 번호 가져오기
	    Intent i = getIntent();
	    matchNo = i.getIntExtra("matchNo", 0);
	    Log.i("FM", "Match No : " + Integer.toString(matchNo));
	    
	    // 로그인 정보 객체 생성
	    lm = new LoginManager(this);
	    
	    // GCM 관리자 객체 생성
	    gm = new GCMManager(this);
	    
	    // 상대팀의 Email 
	    opposingTeamEmail = new String("");
	    
	    // 전화 버튼
	    call = (ImageButton)findViewById(R.id.call);
	    call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if ( phone.equals("") ) {
					Toast.makeText(MatchDetailActivity.this, "연락처가 등록되지 않았습니다", 0).show();
				} else {
					Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
					startActivity(i);
				}
			}
		});
	    
	    // SMS 버튼
	    sms = (ImageButton)findViewById(R.id.sms);
	    sms.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( phone.equals("") ) {
					Toast.makeText(MatchDetailActivity.this, "연락처가 등록되지 않았습니다", 0).show();
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + phone));
					startActivity(i);
				}
			}
		});
	    
	    // referencing views
	    teamName = (TextView)findViewById(R.id.md_team_name);
	    teamInfo = (TextView)findViewById(R.id.team_info);
	    date = (TextView)findViewById(R.id.date);
	    time = (TextView)findViewById(R.id.time);
	    location = (TextView)findViewById(R.id.location);
	    ground = (TextView)findViewById(R.id.ground);
	    detail = (TextView)findViewById(R.id.detail);
	    state = (TextView)findViewById(R.id.state);
	    
	    // 팀 정보 버튼
	    info = (ImageButton)findViewById(R.id.info);
	    info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MatchDetailActivity.this, TeamInfoActivity.class);
				intent.putExtra("memberNo", memberNo);
				intent.putExtra("teamName", teamName.getText().toString());
				startActivity(intent);
			}
	    });
	    
	    // 지도 출력
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); 
	    map.setOnMapClickListener(this);
	    
	    // 스크랩 여부 확인
	    DatabaseHandler db = new DatabaseHandler(this);
	    isScrapped = db.selectScrapMatch(matchNo);
	}

	@Override
	public void onStart() {
		super.onStart();
		
		// 서버로부터 매치 정보 가져오기
		GetMatchDetail gmd = new GetMatchDetail();
		gmd.execute();
		
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		Uri uri = Uri.parse("geo:" + point.latitude + "," + point.longitude);
		startActivity(new Intent(Intent.ACTION_VIEW,uri));
	}
	
	// 메뉴 출력
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.match_detail, menu);
		MenuItem scrap = menu.findItem(R.id.scrap);
		// 스크랩 여부에 따라 다른 아이콘을 출력한다.
		if(isScrapped)
			scrap.setIcon(R.drawable.scrapped);
		else
			scrap.setIcon(R.drawable.scrap);
		
		
		return true;
	}
	
	// 메뉴 선택
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId()) {
		case R.id.apply :
			if( !(lm.isLogin() && lm.getMemberType().equals("팀회원") ) ) {
				Toast.makeText(this, "매치를 신청하려면 팀 계정으로 로그인 해야합니다", 0).show();
			} else if( opposingTeamEmail.equals( lm.getEmail() ) ) {
				Toast.makeText(this, "자신이 등록한 매치에는 매치를 신청할 수 없습니다", 0).show();
			} else if( matchState == 1 ) {
				Toast.makeText(this, "이미 성사된 매치입니다", 0).show();
			} else {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				final View layout = inflater.inflate(R.layout.dialog_match_apply, null);
				builder.setView(layout);
				
				builder.setTitle("매치 신청");
				// builder.setMessage("매치를 신청하시겠습니까?");
				builder.setPositiveButton("신청",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 신청 메시지 저장
								EditText edit = (EditText)layout.findViewById(R.id.apply_msg);
								applyMsg = edit.getText().toString();
								
								// 매치 신청
								new ApplyMatch().execute();
							}
						});
				builder.setNegativeButton("취소", null);
				builder.create().show();
			}
				
			return true;
		case R.id.scrap:
			// 즐겨찾기
			DatabaseHandler db = new DatabaseHandler(this);
			isScrapped = db.selectScrapMatch(matchNo);
			
			if(isScrapped) {
				db.deleteScrapMatch(matchNo);
				item.setIcon(R.drawable.scrap);
				Log.i("매치 스크랩 삭제", "매치 번호 : " + matchNo);
				
			} else {
				db.insertScrapMatch(matchNo);
				item.setIcon(R.drawable.scrapped);
				Log.i("매치 스크랩", "매치 번호 : " + matchNo);
			}
			return true;
		case android.R.id.home :
			finish();
			return true;
		}
		
		return false;
	}
	
	// Location의 좌표를 리턴하는 메서드
	public List<Address> GetLocationPoint( String location ) {
		Geocoder geocoder = null;
		List<Address> addresses = null;
		
		try {
			geocoder = new Geocoder( getApplicationContext(), Locale.getDefault() );
			addresses = geocoder.getFromLocationName( location, 1 );
		} catch (IOException e) {
			Log.e("Geocoder", "Error");
		}
		
		return addresses;
	}
	
	// DB로 부터 매치 정보를 가져와 출력한다.
	public class GetMatchDetail extends AsyncTask<Void, Void, Boolean> {

		String param = "matchNo=" + matchNo;
		String jsonString = "";
		JSONObject jsonObj;
		
		@Override
		public void onPreExecute() {
			pd = new ProgressDialog(MatchDetailActivity.this);
			pd.setMessage("매치 정보를 불러오는 중입니다..");
			pd.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			Boolean isSuccess = false;

			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.match_detail));
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				OutputStreamWriter out = new OutputStreamWriter(
						conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();

				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();

				jsonObj = new JSONObject(jsonString);
				Log.i("FM", "GetMatchDetail result : " + jsonString);

				// check the success of getting information
				if (jsonObj.getInt("success") == 1) {
					isSuccess = true;
				}

			} catch (MalformedURLException e) {
				Log.e("FM", "GetMatchDetail : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "GetMatchDetail : " + e.getMessage());
			} catch (JSONException e) {
				Log.e("FM", "GetMatchDetail : " + e.getMessage());
			}

			return isSuccess;
		}

		@Override
		public void onPostExecute(Boolean isSuccess) {

			if (isSuccess) {
				
				try {
					SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm:ss");
					SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
					String session = null;
					
					Date d = originalFormat.parse(jsonObj.getString("MATCH_TIME"));
					session = newFormat.format(d);
					
					d = originalFormat.parse(jsonObj.getString("MATCH_TIME2"));
					session += " ~ " + newFormat.format(d);
					
					// 연락처 저장
					phone = jsonObj.getString("PHONE");
					
					// 매치 정보를 뷰에 출력
					teamName.setText(jsonObj.getString("TEAM_NAME"));
					teamInfo.setText(jsonObj.getString("TEAM_LOCATION") + " / " + jsonObj.getString("NUM_OF_PLAYERS") + "명 / " + jsonObj.getString("AGES") );
					date.setText(jsonObj.getString("MATCH_DATE"));
					time.setText(session);
					location.setText(jsonObj.getString("MATCH_LOCATION"));
					ground.setText(jsonObj.getString("GROUND"));
					detail.setText(jsonObj.getString("DETAIL").replace("__", "\n"));
					
					// 매치 상태 저장
					matchState = jsonObj.getInt("STATE");
					
					// 매치 상태별 출력
					if(matchState == 1)
						state.setText("매치가 성사되었습니다.");
					
					// 상대팀 Email 정보 저장
					opposingTeamEmail = jsonObj.getString("EMAIL");
					
					// 매치를 등록한 팀 번호 저장
					memberNo = jsonObj.getInt("MEMBER_NO");
					
					// 매치를 등록한 팀의 GCM Registration ID 저장
					regid = jsonObj.getString("REGID");
					
					// 지도에 출력할 좌표 생성
				    List<Address> addrs = GetLocationPoint( location.getText().toString() );
				    if( addrs.size() == 0 ) 
				    	point = new LatLng( 35, 128 );
				    else
				    	point = new LatLng( addrs.get(0).getLatitude(), addrs.get(0).getLongitude() );
				    
				    // 좌표를 지도에 출력
				    map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
				    map.addMarker(new MarkerOptions().position(point));
				    
					
				} catch (JSONException e) {
					Log.e("FM", "GetMatchDetail : " + e.getMessage());
				} catch (ParseException e) {
					Log.e("FM", "GetMatchDetail : " + e.getMessage());
				}
			}
			
			pd.dismiss();
		}
	}
	
	// 매치 신청 비동기 태스크
	private class ApplyMatch extends AsyncTask<Void, Void, Void> {
		String param;
		
		// 초기화 작업을 해주지 않으면 문자열 앞에 null이 들어가서 Object 변환시 예외가 발생한다.
		String jsonString = "";

		@Override
		protected void onPreExecute() {
			pd.setMessage("잠시만 기다려 주세요...");
			pd.show();
			
			param = "matchNo=" + matchNo;
			param += "&memberNo=" + lm.getMemberNo();
			param += "&applyMsg=" + applyMsg;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				// URL 연결 생성
				URL url = new URL(getString(R.string.server) + getString(R.string.apply_match));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				// 파리미터 전송
				OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();
				
				// JSON 결과 가져오기
				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();
				
				Log.i("result", jsonString);
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}  
			return null;
		}

		@Override
		protected void onPostExecute(Void arg0) {
			pd.dismiss();
			
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				int success = jsonObj.getInt("success");
				
				if( success == 0 ) {
					Toast.makeText(getApplicationContext(), "매치 신청에 실패하였습니다", 0).show();
				} else if ( success == 1 ) {
					Toast.makeText(getApplicationContext(), "매치를 신청하였습니다", 0).show();
					// GCM 메시지 전송
					gm.sendMessage(regid, lm.getTeamName(), matchNo);
				} else {
					Toast.makeText(getApplicationContext(), "이미 신청한 매치입니다", 0).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
