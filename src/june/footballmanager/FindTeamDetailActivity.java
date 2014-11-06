package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import june.footballmanager.FindPlayerDetailActivity.GetFindPlayerDetail;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FindTeamDetailActivity extends Activity {
	// �� ��ȣ
	int no;

	int memberNo;
	String phone;
	
	// ��
	TextView nickname;
	TextView playerInfo;
	ImageButton call;
	ImageButton sms;
	ImageButton info;
	
	TextView content;
	
	// ��ũ�� ���θ� �����ϴ� ����
	boolean isScrapped = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_team_detail);
		
		// �׼ǹ� ����
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		// actionBar.setIcon(R.drawable.player);

		// �Խù� ��ȣ ��������
		no = getIntent().getIntExtra("no", -1);
		
		// ��ũ�� ���� Ȯ��
	    DatabaseHandler db = new DatabaseHandler(this);
	    isScrapped = db.selectScrapFindTeam(no);
		
		// �� ���۷���
		nickname = (TextView)findViewById(R.id.nickname);
		playerInfo = (TextView)findViewById(R.id.player_info);
		call = (ImageButton)findViewById(R.id.call);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if ( phone.isEmpty() ) {
					Toast.makeText(FindTeamDetailActivity.this, "����ó�� ��ϵ��� �ʾҽ��ϴ�", 0).show();
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
					Toast.makeText(FindTeamDetailActivity.this, "����ó�� ��ϵ��� �ʾҽ��ϴ�", 0).show();
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
				// Intent intent = new Intent(FindTeamDetailActivity.this, TeamInfoActivity.class);
				// intent.putExtra("memberNo", memberNo);
				// startActivity(intent);
			}
	    });
		
		content = (TextView)findViewById(R.id.content);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		new GetFindTeamDetail().execute();
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.find_team_detail, menu);
		MenuItem scrap = menu.findItem(R.id.scrap);
		// ��ũ�� ���ο� ���� �ٸ� �������� ����Ѵ�.
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
		} else if(id == R.id.scrap){
			// ���ã��
			DatabaseHandler db = new DatabaseHandler(this);
			isScrapped = db.selectScrapFindTeam(no);

			if (isScrapped) {
				db.deleteScrapFindTeam(no);
				item.setIcon(R.drawable.scrap);
				Log.i("������ ��ũ�� ����", "�� ��ȣ : " + no);

			} else {
				db.insertScrapFindTeam(no);
				item.setIcon(R.drawable.scrapped);
				Log.i("������ ��ũ��", "�� ��ȣ : " + no);
			}
		}

		return super.onOptionsItemSelected(item);
	}
	
	// DB�� ���� �� ���� �Խù��� �� ������ ������ ����Ѵ�.
	public class GetFindTeamDetail extends AsyncTask<Void, Void, Void> {

		String jsonString = "";
		ProgressDialog pd;

		@Override
		public void onPreExecute() {
			pd = new ProgressDialog(FindTeamDetailActivity.this);
			pd.setMessage("������ �ҷ����� ���Դϴ�...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.find_team_detail));
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
				Log.i("FM", "GetFindTeamDetail result : " + jsonString);

			} catch (MalformedURLException e) {
				Log.e("FM", "GetFindTeamDetail : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "GetFindTeamDetail : " + e.getMessage());
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

					// �Խù��� ����� ���� ��ȣ ����
					memberNo = jsonObj.getInt("MEMBER_NO");

					// ����ó ����
					phone = jsonObj.getString("PHONE");
					
					// ���� ���
					getActionBar().setSubtitle(jsonObj.getString("TITLE"));

					// ���� ������ �信 ���
					nickname.setText(jsonObj.getString("NICKNAME"));
					playerInfo.setText(jsonObj.getString("LOCATION") 
							+ " / " + jsonObj.getString("POSITION") 
							+ " / " + jsonObj.getString("AGE") );
						
					// ���� ���
					content.setText(jsonObj.getString("CONTENT").replace("__", "\n"));

				}

			} catch (JSONException e) {
				Log.e("FM", "GetFindTeamDetail : " + e.getMessage());
			}

			pd.dismiss();
		}
	}
}
