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

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FindPlayerDetailActivity extends Activity {
	// 글 번호
	int no;
	
	int memberNo;
	String phone;
	
	// 뷰
	TextView teamName;
	TextView teamInfo;
	ImageButton call;
	ImageButton sms;
	ImageButton info;
	
	TextView location;
	TextView position;
	TextView ages;
	TextView content;
	
	// 스크랩 여부를 저장하는 변수
	boolean isScrapped = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_player_detail);

		// 액션바 설정
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		//actionBar.setIcon(R.drawable.team);
		
		// 게시물 번호 가져오기
		no = getIntent().getIntExtra("no", -1);
		
		// 스크랩 여부 확인
	    DatabaseHandler db = new DatabaseHandler(this);
	    isScrapped = db.selectScrapFindPlayer(no);
		
		// 뷰 레퍼런싱
		teamName = (TextView)findViewById(R.id.team_name);
		teamInfo = (TextView)findViewById(R.id.team_info);
		call = (ImageButton)findViewById(R.id.call);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if ( phone.isEmpty() ) {
					Toast.makeText(FindPlayerDetailActivity.this, "연락처가 등록되지 않았습니다", 0).show();
				} else {
					Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
					startActivity(i);
				}
			}
		});
		
		sms = (ImageButton)findViewById(R.id.sms);
		sms.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( phone.isEmpty() ) {
					Toast.makeText(FindPlayerDetailActivity.this, "연락처가 등록되지 않았습니다", 0).show();
				} else {
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("smsto:" + phone));
					startActivity(i);
				}
			}
		});
		
		info = (ImageButton)findViewById(R.id.info);
		info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FindPlayerDetailActivity.this, TeamInfoActivity.class);
				intent.putExtra("memberNo", memberNo);
				intent.putExtra("teamName", teamName.getText().toString());
				startActivity(intent);
			}
	    });
		
		location = (TextView)findViewById(R.id.location);
		position = (TextView)findViewById(R.id.position);
		ages = (TextView)findViewById(R.id.ages);
		content = (TextView)findViewById(R.id.content);
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		new GetFindPlayerDetail().execute();	
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.find_player_detail, menu);
		MenuItem scrap = menu.findItem(R.id.scrap);
		// 스크랩 여부에 따라 다른 아이콘을 출력한다.
		if (isScrapped)
			scrap.setIcon(R.drawable.scrapped);
		else
			scrap.setIcon(R.drawable.scrap);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		} else if (id == R.id.scrap){
			// 즐겨찾기
			DatabaseHandler db = new DatabaseHandler(this);
			isScrapped = db.selectScrapFindPlayer(no);

			if (isScrapped) {
				db.deleteScrapFindPlayer(no);
				item.setIcon(R.drawable.scrap);
				Log.i("선수모집 스크랩 삭제", "글 번호 : " + no);

			} else {
				db.insertScrapFindPlayer(no);
				item.setIcon(R.drawable.scrapped);
				Log.i("선수모집 스크랩", "글 번호 : " + no);
			}
		}

		return super.onOptionsItemSelected(item);
	}
	
	// DB로 부터 선수 구함 게시물의 상세 정보를 가져와 출력한다.
	public class GetFindPlayerDetail extends AsyncTask<Void, Void, Void> {

		String jsonString = "";
		ProgressDialog pd;

		@Override
		public void onPreExecute() {
			pd = new ProgressDialog(FindPlayerDetailActivity.this);
			pd.setMessage("정보를 불러오는 중입니다...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.find_player_detail));
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				String param = "no=" + no;
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
				Log.i("FM", "GetFindPlayerDetail result : " + jsonString);

			} catch (MalformedURLException e) {
				Log.e("FM", "GetFindPlayerDetail : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "GetFindPlayerDetail : " + e.getMessage());
			}

			return null;
		}

		@Override
		public void onPostExecute(Void params) {

			JSONObject jsonObj;

			try {
				jsonObj = new JSONObject(jsonString);

				// check the success of getting information
				if (jsonObj.getInt("success") == 1) {

					// 게시물을 등록한 팀 번호 저장
					memberNo = jsonObj.getInt("MEMBER_NO");

					// 연락처 저장
					phone = jsonObj.getString("PHONE");
					
					// 제목 출력
					getActionBar().setSubtitle(jsonObj.getString("TITLE"));

					// 팀 정보를 뷰에 출력
					teamName.setText(jsonObj.getString("TEAM_NAME"));
					teamInfo.setText(jsonObj.getString("T_LOCATION") 
							+ " / " + jsonObj.getString("NUM_OF_PLAYERS") 
							+ "명 / " + jsonObj.getString("T_AGES") );
						
					// 선수 구함 정보를 뷰에 출력
					location.setText(jsonObj.getString("P_LOCATION"));
					position.setText(jsonObj.getString("POSITION"));
					ages.setText(jsonObj.getString("P_AGES"));
					content.setText(jsonObj.getString("CONTENT").replace("__", "\n"));

				}

			} catch (JSONException e) {
				Log.e("FM", "GetFindPlayerDetail : " + e.getMessage());
			}

			pd.dismiss();
		}
	}
}
