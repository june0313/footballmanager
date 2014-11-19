package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FindPlayerDetailActivity extends Activity {
	// �� ��ȣ
	int no;
	
	int memberNo;
	String phone;
	
	// ��
	TextView teamName;
	TextView teamInfo;
	ImageButton call;
	ImageButton sms;
	ImageButton info;
	
	TextView location;
	TextView position;
	TextView ages;
	TextView content;
	
	// ��ũ�� ���θ� �����ϴ� ����
	boolean isScrapped = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_player_detail);

		// �׼ǹ� ����
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		//actionBar.setIcon(R.drawable.team);
		
		// �Խù� ��ȣ ��������
		no = getIntent().getIntExtra("no", -1);
		
		// ��ũ�� ���� Ȯ��
	    DatabaseHandler db = new DatabaseHandler(this);
	    isScrapped = db.selectScrapFindPlayer(no);
		
		// �� ���۷���
		teamName = (TextView)findViewById(R.id.team_name);
		teamInfo = (TextView)findViewById(R.id.team_info);
		call = (ImageButton)findViewById(R.id.call);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if ( phone.isEmpty() ) {
					Toast.makeText(FindPlayerDetailActivity.this, "����ó�� ��ϵ��� �ʾҽ��ϴ�", 0).show();
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
					Toast.makeText(FindPlayerDetailActivity.this, "����ó�� ��ϵ��� �ʾҽ��ϴ�", 0).show();
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
		getFindPlayerDetail();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.find_player_detail, menu);
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
		} else if (id == R.id.scrap){
			// ���ã��
			DatabaseHandler db = new DatabaseHandler(this);
			isScrapped = db.selectScrapFindPlayer(no);

			if (isScrapped) {
				db.deleteScrapFindPlayer(no);
				item.setIcon(R.drawable.scrap);
				Log.i("�������� ��ũ�� ����", "�� ��ȣ : " + no);

			} else {
				db.insertScrapFindPlayer(no);
				item.setIcon(R.drawable.scrapped);
				Log.i("�������� ��ũ��", "�� ��ȣ : " + no);
			}
		}

		return super.onOptionsItemSelected(item);
	}
	
	// �����κ��� �������� �Խù��� �� ������ �������� �޼���
	private void getFindPlayerDetail() {
		// ������ �������� URL
		String url = getString(R.string.server)
				+ getString(R.string.find_player_detail);
		
		// �Ķ���� ����
		String param = "no=" + no;
		
		// ���� ����
		JSONObject json = new HttpTask(url, param).getJSONObject();
		
		// check the success of getting information
		try {
			if (json.getInt("success") == 1) {

				// �Խù��� ����� �� ��ȣ ����
				memberNo = json.getInt("MEMBER_NO");

				// ����ó ����
				phone = json.getString("PHONE");
				
				// ���� ���
				getActionBar().setSubtitle(json.getString("TITLE"));

				// �� ������ �信 ���
				teamName.setText(json.getString("TEAM_NAME"));
				teamInfo.setText(json.getString("T_LOCATION") 
						+ " / " + json.getString("NUM_OF_PLAYERS") 
						+ "�� / " + json.getString("T_AGES") );
					
				// ���� ���� ������ �信 ���
				location.setText(json.getString("P_LOCATION"));
				position.setText(json.getString("POSITION"));
				ages.setText(json.getString("P_AGES"));
				content.setText(json.getString("CONTENT").replace("__", "\n"));

			}
		} catch (JSONException e) {
			Log.e("getFindPlayerDetail", e.getMessage());
		}
	}
}
