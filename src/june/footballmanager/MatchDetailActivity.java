package june.footballmanager;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
		
		// 서버로부터 매치 상세 정보를 가져와 출력한다.
		printMatchDetail();
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
				Toast.makeText(this, "매치 신청은 팀 계정만 가능합니다.", 0).show();
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
				builder.setPositiveButton("신청", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// 신청 메시지 저장
								EditText edit = (EditText)layout.findViewById(R.id.apply_msg);
								String applyMsg = edit.getText().toString();
								
								// 매치 신청 작업 수행
								applyMatch(applyMsg);
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
	
	// 매치가 성사된 팀의 이름을 출력하는 메서드
	private void printOpposingTeamname() {
		String url = getString(R.string.server) + getString(R.string.opposing_teamname);
		String parameter = "matchNo=" + matchNo;
		
		// 서버 연결
		new HttpAsyncTask(url, parameter) {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						state.setText(json.getString("TEAM_NAME") + "팀과 매치가 성사되었습니다.");
					}
				} catch (JSONException e) {
					Log.e("getOpposingTeamname", e.getMessage());
				}
			}
		}.execute();
	}
	
	// 웹 서버로 부터 매치 상세 정보를 가져와 각 뷰에 출력한다.
	private void printMatchDetail() {
		String url = getString(R.string.server) + getString(R.string.match_detail);
		String parameter = "matchNo=" + matchNo;
		
		// 서버 연결
		new HttpAsyncTask(url, parameter, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm:ss");
						SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
						String session = null;
						
						Date d = originalFormat.parse(json.getString("MATCH_TIME"));
						session = newFormat.format(d);
						
						d = originalFormat.parse(json.getString("MATCH_TIME2"));
						session += " ~ " + newFormat.format(d);
						
						// 연락처 저장
						phone = json.getString("PHONE");
						
						// 매치 정보를 뷰에 출력
						teamName.setText(json.getString("TEAM_NAME"));
						teamInfo.setText(json.getString("TEAM_LOCATION") + " / " + json.getString("NUM_OF_PLAYERS") + "명 / " + json.getString("AGES") );
						date.setText(json.getString("MATCH_DATE"));
						time.setText(session);
						location.setText(json.getString("MATCH_LOCATION"));
						ground.setText(json.getString("GROUND"));
						detail.setText(json.getString("DETAIL").replace("__", "\n"));
						
						// 매치 상태 저장
						matchState = json.getInt("STATE");
						
						// 매치 상태별 출력
						if(matchState == 1)
							printOpposingTeamname();
						else
							state.setText("아직 상대팀이 정해지지 않았습니다.");
						
						// 상대팀 Email 정보 저장
						opposingTeamEmail = json.getString("EMAIL");
						
						// 매치를 등록한 팀 번호 저장
						memberNo = json.getInt("MEMBER_NO");
						
						// 매치를 등록한 팀의 GCM Registration ID 저장
						regid = json.getString("REGID");
						
						// 지도에 출력할 좌표 생성
					    List<Address> addrs = GetLocationPoint( location.getText().toString() );
					    if( addrs.size() == 0 ) 
					    	point = new LatLng( 35, 128 );
					    else
					    	point = new LatLng( addrs.get(0).getLatitude(), addrs.get(0).getLongitude() );
					    
					    // 좌표를 지도에 출력
					    map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
					    map.addMarker(new MarkerOptions().position(point));
					}
				} catch (JSONException e) {
					Log.e("printMatchDetail", e.getMessage());
				} catch (ParseException e) {
					Log.e("printMatchDetail", e.getMessage());
				}
			}
		}.execute();
	}
	
	// 매치 신청 작업을 수행하는 메서드
	private void applyMatch(String msg) {
		String url = getString(R.string.server) + getString(R.string.apply_match);
		String parameter = "matchNo=" + matchNo;
		parameter += "&memberNo=" + lm.getMemberNo();
		parameter += "&applyMsg=" + msg;
		
		// 서버 연결
		new HttpAsyncTask(url, parameter, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					int success = json.getInt("success");
					
					if( success == 0 ) {
						Toast.makeText(MatchDetailActivity.this, "매치 신청에 실패하였습니다", 0).show();
					} else if ( success == 1 ) {
						Toast.makeText(MatchDetailActivity.this, "매치를 신청하였습니다", 0).show();
						// GCM 메시지 전송
						// 마지막 인자 0은 매치 '신청'을 나타낸다.(수락은 1)
						gm.sendMessage(regid, lm.getTeamName() + "팀이 매치를 신청하였습니다.", matchNo, 0);
					} else {
						Toast.makeText(MatchDetailActivity.this, "이미 신청한 매치입니다", 0).show();
					}
				} catch (JSONException e) {
					Log.e("applyMatch", e.getMessage());
				}
			}
		}.execute();	
	}
}
