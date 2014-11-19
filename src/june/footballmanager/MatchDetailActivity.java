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

// ��ġ �� ���� ������
public class MatchDetailActivity extends Activity implements GoogleMap.OnMapClickListener {

	// ��ġ ��ȣ
	int matchNo;
	
	// ��ġ ����
	int matchState;
	
	// ��ġ�� ����� ���� ��ȣ
	int memberNo;
	
	// ��ġ�� ����� ���� ����ó
	String phone;
	
	// ��ġ�� ����� ���� GCM Registration ID
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
	
	// �α��� ����
	LoginManager lm;
	
	// ����� �̸��� ����
	String opposingTeamEmail;
	
	// GCM ������ ��ü
	GCMManager gm;
	
	// ��ũ�� ���θ� �����ϴ� ����
	boolean isScrapped = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_match_detail);
	    
	    ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(false);
	    
	    // ��ġ ��ȣ ��������
	    Intent i = getIntent();
	    matchNo = i.getIntExtra("matchNo", 0);
	    Log.i("FM", "Match No : " + Integer.toString(matchNo));
	    
	    // �α��� ���� ��ü ����
	    lm = new LoginManager(this);
	    
	    // GCM ������ ��ü ����
	    gm = new GCMManager(this);
	    
	    // ������� Email 
	    opposingTeamEmail = new String("");
	    
	    // ��ȭ ��ư
	    call = (ImageButton)findViewById(R.id.call);
	    call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if ( phone.equals("") ) {
					Toast.makeText(MatchDetailActivity.this, "����ó�� ��ϵ��� �ʾҽ��ϴ�", 0).show();
				} else {
					Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
					startActivity(i);
				}
			}
		});
	    
	    // SMS ��ư
	    sms = (ImageButton)findViewById(R.id.sms);
	    sms.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( phone.equals("") ) {
					Toast.makeText(MatchDetailActivity.this, "����ó�� ��ϵ��� �ʾҽ��ϴ�", 0).show();
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
	    
	    // �� ���� ��ư
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
	    
	    // ���� ���
	    map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap(); 
	    map.setOnMapClickListener(this);
	    
	    // ��ũ�� ���� Ȯ��
	    DatabaseHandler db = new DatabaseHandler(this);
	    isScrapped = db.selectScrapMatch(matchNo);
	}

	@Override
	public void onStart() {
		super.onStart();
		
		// �����κ��� ��ġ �� ������ ������ ����Ѵ�.
		printMatchDetail();
	}
	
	@Override
	public void onMapClick(LatLng arg0) {
		Uri uri = Uri.parse("geo:" + point.latitude + "," + point.longitude);
		startActivity(new Intent(Intent.ACTION_VIEW,uri));
	}
	
	// �޴� ���
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.match_detail, menu);
		MenuItem scrap = menu.findItem(R.id.scrap);
		// ��ũ�� ���ο� ���� �ٸ� �������� ����Ѵ�.
		if(isScrapped)
			scrap.setIcon(R.drawable.scrapped);
		else
			scrap.setIcon(R.drawable.scrap);
		
		
		return true;
	}
	
	// �޴� ����
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId()) {
		case R.id.apply :
			if( !(lm.isLogin() && lm.getMemberType().equals("��ȸ��") ) ) {
				Toast.makeText(this, "��ġ�� ��û�Ϸ��� �� �������� �α��� �ؾ��մϴ�", 0).show();
			} else if( opposingTeamEmail.equals( lm.getEmail() ) ) {
				Toast.makeText(this, "�ڽ��� ����� ��ġ���� ��ġ�� ��û�� �� �����ϴ�", 0).show();
			} else if( matchState == 1 ) {
				Toast.makeText(this, "�̹� ����� ��ġ�Դϴ�", 0).show();
			} else {

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				final View layout = inflater.inflate(R.layout.dialog_match_apply, null);
				builder.setView(layout);
				
				builder.setTitle("��ġ ��û");
				// builder.setMessage("��ġ�� ��û�Ͻðڽ��ϱ�?");
				builder.setPositiveButton("��û", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// ��û �޽��� ����
								EditText edit = (EditText)layout.findViewById(R.id.apply_msg);
								String applyMsg = edit.getText().toString();
								
								// ��ġ ��û �۾� ����
								applyMatch(applyMsg);
							}
						});
				builder.setNegativeButton("���", null);
				builder.create().show();
			}
				
			return true;
		case R.id.scrap:
			// ���ã��
			DatabaseHandler db = new DatabaseHandler(this);
			isScrapped = db.selectScrapMatch(matchNo);
			
			if(isScrapped) {
				db.deleteScrapMatch(matchNo);
				item.setIcon(R.drawable.scrap);
				Log.i("��ġ ��ũ�� ����", "��ġ ��ȣ : " + matchNo);
				
			} else {
				db.insertScrapMatch(matchNo);
				item.setIcon(R.drawable.scrapped);
				Log.i("��ġ ��ũ��", "��ġ ��ȣ : " + matchNo);
			}
			return true;
		case android.R.id.home :
			finish();
			return true;
		}
		
		return false;
	}
	
	// Location�� ��ǥ�� �����ϴ� �޼���
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
	
	// �� ������ ���� ��ġ �� ������ ������ �� �信 ����Ѵ�.
	private void printMatchDetail() {
		String url = getString(R.string.server) + getString(R.string.match_detail);
		String parameter = "matchNo=" + matchNo;
		JSONObject json = new HttpTask(url, parameter).getJSONObject();
		
		try {
			if(json.getInt("success") == 1) {
				SimpleDateFormat originalFormat = new SimpleDateFormat("HH:mm:ss");
				SimpleDateFormat newFormat = new SimpleDateFormat("a h:mm");
				String session = null;
				
				Date d = originalFormat.parse(json.getString("MATCH_TIME"));
				session = newFormat.format(d);
				
				d = originalFormat.parse(json.getString("MATCH_TIME2"));
				session += " ~ " + newFormat.format(d);
				
				// ����ó ����
				phone = json.getString("PHONE");
				
				// ��ġ ������ �信 ���
				teamName.setText(json.getString("TEAM_NAME"));
				teamInfo.setText(json.getString("TEAM_LOCATION") + " / " + json.getString("NUM_OF_PLAYERS") + "�� / " + json.getString("AGES") );
				date.setText(json.getString("MATCH_DATE"));
				time.setText(session);
				location.setText(json.getString("MATCH_LOCATION"));
				ground.setText(json.getString("GROUND"));
				detail.setText(json.getString("DETAIL").replace("__", "\n"));
				
				// ��ġ ���� ����
				matchState = json.getInt("STATE");
				
				// ��ġ ���º� ���
				if(matchState == 1)
					state.setText("��ġ�� ����Ǿ����ϴ�.");
				
				// ����� Email ���� ����
				opposingTeamEmail = json.getString("EMAIL");
				
				// ��ġ�� ����� �� ��ȣ ����
				memberNo = json.getInt("MEMBER_NO");
				
				// ��ġ�� ����� ���� GCM Registration ID ����
				regid = json.getString("REGID");
				
				// ������ ����� ��ǥ ����
			    List<Address> addrs = GetLocationPoint( location.getText().toString() );
			    if( addrs.size() == 0 ) 
			    	point = new LatLng( 35, 128 );
			    else
			    	point = new LatLng( addrs.get(0).getLatitude(), addrs.get(0).getLongitude() );
			    
			    // ��ǥ�� ������ ���
			    map.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
			    map.addMarker(new MarkerOptions().position(point));
			}
		} catch (JSONException e) {
			Log.e("printMatchDetail", e.getMessage());
		} catch (ParseException e) {
			Log.e("printMatchDetail", e.getMessage());
		}
	}
	
	// ��ġ ��û �۾��� �����ϴ� �޼���
	private void applyMatch(String msg) {
		String url = getString(R.string.server) + getString(R.string.apply_match);
		String parameter = "matchNo=" + matchNo;
		parameter += "&memberNo=" + lm.getMemberNo();
		parameter += "&applyMsg=" + msg;
		
		JSONObject json = new HttpTask(url, parameter).getJSONObject();
		
		try {
			int success = json.getInt("success");
			
			if( success == 0 ) {
				Toast.makeText(getApplicationContext(), "��ġ ��û�� �����Ͽ����ϴ�", 0).show();
			} else if ( success == 1 ) {
				Toast.makeText(getApplicationContext(), "��ġ�� ��û�Ͽ����ϴ�", 0).show();
				// GCM �޽��� ����
				gm.sendMessage(regid, lm.getTeamName(), matchNo);
			} else {
				Toast.makeText(getApplicationContext(), "�̹� ��û�� ��ġ�Դϴ�", 0).show();
			}
		} catch (JSONException e) {
			Log.e("applyMatch", e.getMessage());
		}
	}
}
