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

public class FindTeamDetailActivity extends Activity {
	// 글 번호
	int no;

	int memberNo;
	String phone;
	
	// 뷰
	TextView nickname;
	TextView playerInfo;
	ImageButton call;
	ImageButton sms;
	ImageButton info;
	
	TextView content;
	
	// 스크랩 여부를 저장하는 변수
	boolean isScrapped = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_team_detail);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		// actionBar.setIcon(R.drawable.player);

		// 게시물 번호 가져오기
		no = getIntent().getIntExtra("no", -1);
		
		// 스크랩 여부 확인
	    DatabaseHandler db = new DatabaseHandler(this);
	    isScrapped = db.selectScrapFindTeam(no);
		
		// 뷰 레퍼런싱
		nickname = (TextView)findViewById(R.id.nickname);
		playerInfo = (TextView)findViewById(R.id.player_info);
		call = (ImageButton)findViewById(R.id.call);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if ( phone.isEmpty() ) {
					Toast.makeText(FindTeamDetailActivity.this, "연락처가 등록되지 않았습니다", 0).show();
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
					Toast.makeText(FindTeamDetailActivity.this, "연락처가 등록되지 않았습니다", 0).show();
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
		getFindTeamDetail();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.find_team_detail, menu);
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
		} else if(id == R.id.scrap){
			// 즐겨찾기
			DatabaseHandler db = new DatabaseHandler(this);
			isScrapped = db.selectScrapFindTeam(no);

			if (isScrapped) {
				db.deleteScrapFindTeam(no);
				item.setIcon(R.drawable.scrap);
				Log.i("팀구함 스크랩 삭제", "글 번호 : " + no);

			} else {
				db.insertScrapFindTeam(no);
				item.setIcon(R.drawable.scrapped);
				Log.i("팀구함 스크랩", "글 번호 : " + no);
			}
		}

		return super.onOptionsItemSelected(item);
	}
	
	// 서버로부터 팀구함 게시물의 상세 정보를 가져오는 메서드
	private void getFindTeamDetail() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server)
				+ getString(R.string.find_team_detail);
		
		// 파라미터 구성
		String param = "no=" + no;
		
		// 서버 연결
		new HttpAsyncTask(url, param) {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				// check the success of getting information
				try {
					json = new JSONObject(result);
					if (json.getInt("success") == 1) {

						// 게시물을 등록한 선수 번호 저장
						memberNo = json.getInt("MEMBER_NO");

						// 연락처 저장
						phone = json.getString("PHONE");
						
						// 제목 출력
						getActionBar().setSubtitle(json.getString("TITLE"));

						// 선수 정보를 뷰에 출력
						nickname.setText(json.getString("NICKNAME"));
						playerInfo.setText(json.getString("LOCATION") 
								+ " / " + json.getString("POSITION") 
								+ " / " + json.getString("AGE") );
							
						// 내용 출력
						content.setText(json.getString("CONTENT").replace("__", "\n"));
					}
				} catch (JSONException e) {
					Log.e("getFindTeamDetail", e.getMessage());
				}
			}
			
		}.execute();
	}
}
