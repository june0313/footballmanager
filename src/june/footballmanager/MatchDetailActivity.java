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
	
	// ��ġ ��û �޽���
	String applyMsg;
	
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
		
		// �����κ��� ��ġ ���� ��������
		GetMatchDetail gmd = new GetMatchDetail();
		gmd.execute();
		
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
				builder.setPositiveButton("��û",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// ��û �޽��� ����
								EditText edit = (EditText)layout.findViewById(R.id.apply_msg);
								applyMsg = edit.getText().toString();
								
								// ��ġ ��û
								new ApplyMatch().execute();
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
	
	// DB�� ���� ��ġ ������ ������ ����Ѵ�.
	public class GetMatchDetail extends AsyncTask<Void, Void, Boolean> {

		String param = "matchNo=" + matchNo;
		String jsonString = "";
		JSONObject jsonObj;
		
		@Override
		public void onPreExecute() {
			pd = new ProgressDialog(MatchDetailActivity.this);
			pd.setMessage("��ġ ������ �ҷ����� ���Դϴ�..");
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
					
					// ����ó ����
					phone = jsonObj.getString("PHONE");
					
					// ��ġ ������ �信 ���
					teamName.setText(jsonObj.getString("TEAM_NAME"));
					teamInfo.setText(jsonObj.getString("TEAM_LOCATION") + " / " + jsonObj.getString("NUM_OF_PLAYERS") + "�� / " + jsonObj.getString("AGES") );
					date.setText(jsonObj.getString("MATCH_DATE"));
					time.setText(session);
					location.setText(jsonObj.getString("MATCH_LOCATION"));
					ground.setText(jsonObj.getString("GROUND"));
					detail.setText(jsonObj.getString("DETAIL").replace("__", "\n"));
					
					// ��ġ ���� ����
					matchState = jsonObj.getInt("STATE");
					
					// ��ġ ���º� ���
					if(matchState == 1)
						state.setText("��ġ�� ����Ǿ����ϴ�.");
					
					// ����� Email ���� ����
					opposingTeamEmail = jsonObj.getString("EMAIL");
					
					// ��ġ�� ����� �� ��ȣ ����
					memberNo = jsonObj.getInt("MEMBER_NO");
					
					// ��ġ�� ����� ���� GCM Registration ID ����
					regid = jsonObj.getString("REGID");
					
					// ������ ����� ��ǥ ����
				    List<Address> addrs = GetLocationPoint( location.getText().toString() );
				    if( addrs.size() == 0 ) 
				    	point = new LatLng( 35, 128 );
				    else
				    	point = new LatLng( addrs.get(0).getLatitude(), addrs.get(0).getLongitude() );
				    
				    // ��ǥ�� ������ ���
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
	
	// ��ġ ��û �񵿱� �½�ũ
	private class ApplyMatch extends AsyncTask<Void, Void, Void> {
		String param;
		
		// �ʱ�ȭ �۾��� ������ ������ ���ڿ� �տ� null�� ���� Object ��ȯ�� ���ܰ� �߻��Ѵ�.
		String jsonString = "";

		@Override
		protected void onPreExecute() {
			pd.setMessage("��ø� ��ٷ� �ּ���...");
			pd.show();
			
			param = "matchNo=" + matchNo;
			param += "&memberNo=" + lm.getMemberNo();
			param += "&applyMsg=" + applyMsg;
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				// URL ���� ����
				URL url = new URL(getString(R.string.server) + getString(R.string.apply_match));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				// �ĸ����� ����
				OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();
				
				// JSON ��� ��������
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
					Toast.makeText(getApplicationContext(), "��ġ ��û�� �����Ͽ����ϴ�", 0).show();
				} else if ( success == 1 ) {
					Toast.makeText(getApplicationContext(), "��ġ�� ��û�Ͽ����ϴ�", 0).show();
					// GCM �޽��� ����
					gm.sendMessage(regid, lm.getTeamName(), matchNo);
				} else {
					Toast.makeText(getApplicationContext(), "�̹� ��û�� ��ġ�Դϴ�", 0).show();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
