package june.footballmanager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * 내가 등록한 매치에 신청한 팀 리스트를 출력하는 액티비티
 * 
 */
public class AppliedTeamActivity extends Activity {
	int matchNo;
	int memberNo;
	String regid;
	
	// 신청한 팀 리스트
	ArrayList<TeamItem> appliedTeamList;
	
	// 리스트 어댑터
	AppliedTeamListAdapter atlAdapter;
	
	// 신청한 팀 리스트뷰
	ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_applied_team);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		
		// 매치 번호 가져오기
		Intent intent = getIntent();
		matchNo = intent.getIntExtra("matchNo", -1);
		
		// 리스트 객체 초기화
		appliedTeamList = new ArrayList<TeamItem>();
		
		// 어댑터 생성
		atlAdapter = new AppliedTeamListAdapter( this, appliedTeamList );
		
		// 리스트뷰 생성 및 설정
		list = (ListView) findViewById(R.id.list);
		list.addHeaderView(new View(this), null, true);
	    list.addFooterView(new View(this), null, true);
		list.setAdapter(atlAdapter);
		// list.setOnItemClickListener(this);
		
		// 서버로부터 신청한 팀의 리스트르 가져온다.
		getAppliedTeamList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.applied_team, menu);
		return true;
	}

	// 메뉴 선택시 콜백 메서드
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return false;
	}
	
	private class AppliedTeamListAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<TeamItem> list;
		private LayoutInflater inflater;
		
		public AppliedTeamListAdapter( Context c, ArrayList<TeamItem> list ) {
			this.context = c;
			this.list = list;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public TeamItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			
			if( convertView == null ) {
				convertView = inflater.inflate(R.layout.applied_team_item, parent, false);
			}
			
			TextView teamName = (TextView) convertView.findViewById(R.id.team_name);
			teamName.setText(getItem(position).getTeamName());
			
			TextView ages = (TextView) convertView.findViewById(R.id.ages);
			ages.setText(getItem(position).getAges());
			
			TextView numOfPlayers = (TextView) convertView.findViewById(R.id.num_of_players);
			numOfPlayers.setText(getItem(position).getNumOfPlayers() + "명");
			
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(list.get(position).getLocation());
			
			TextView applyMsg = (TextView) convertView.findViewById(R.id.apply_msg);
			if(getItem(position).getMsg().length() != 0)
				applyMsg.setText(getItem(position).getMsg());
			
			TextView teamInfo = (TextView) convertView.findViewById(R.id.team_info);
			teamInfo.setOnClickListener(new OnClickListener() {
				// "팀 정보" 버튼 클릭시
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(AppliedTeamActivity.this, TeamInfoActivity.class);
					intent.putExtra("memberNo", getItem(pos).getMemberNo());
					intent.putExtra("teamName", getItem(pos).getTeamName());
					startActivity(intent);
				}
			});
			
			TextView btnAccept = (TextView) convertView.findViewById(R.id.accept);
			btnAccept.setOnClickListener(new OnClickListener() {

				// "수락" 버튼 클릭시
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(AppliedTeamActivity.this);
					builder.setMessage(getItem(pos).getTeamName() + "의 신청을 수락하시겠습니까?")
					.setPositiveButton("예", new DialogInterface.OnClickListener() {
						// "예" 버튼 클릭시
						@Override
						public void onClick(DialogInterface dialog, int which) {
							memberNo = getItem(pos).getMemberNo();
							regid = getItem(pos).getRegid();
							// 신청을 수락한다.
							acceptOpposingTeam();
						}
					})
					.setNegativeButton("아니오", null);
					
					builder.create().show();
				}
			});
			
			return convertView;
		}
		
	}
	
	// 서버로부터 신청한 팀 리스트를 가져오는 메서드
	private void getAppliedTeamList() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.applied_team);
		
		// 파라미터 구성
		String param = "matchNo=" + matchNo;
		
		// 서버 연결
		new HttpAsyncTask(url, param) {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				JSONArray jsonArr = null;
				
				try {
					json = new JSONObject(result);
					
					jsonArr = json.getJSONArray("list");
					
					JSONObject item;
					appliedTeamList.clear();
					
					for( int i = 0; i < jsonArr.length(); i++ ) {
						item = jsonArr.getJSONObject(i);
						appliedTeamList.add( new TeamItem(
								item.getInt("MEMBER_NO"),
								item.getString("TEAM_NAME"),
								item.getString("AGES"),
								item.getInt("NUM_OF_PLAYERS"),
								item.getString("LOCATION"),
								null,
								item.getString("PHONE"),
								item.getString("MSG"),
								item.getString("REGID")
								) 
						);
					}
				} catch (JSONException e) {
					appliedTeamList.clear();
					Log.e("getAppliedTeamList", e.getMessage());
				} finally {
					atlAdapter.notifyDataSetChanged();
				}
			}
			
		}.execute();
	}
	
	// 신청을 수락하는 메서드
	private void acceptOpposingTeam() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.accept_opposing_team);
		
		// 파라미터 구성
		String param = "matchNo=" + matchNo;
		param +="&memberNo=" + memberNo; 
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				
				try {
					json = new JSONObject(result);
					
					if( json.getInt("success") == 1 ) {
						Toast.makeText(AppliedTeamActivity.this, "매치가 성사되었습니다!", 0).show();
						
						// Push 알림을 보낸다. 마지막 인자 1은 신청에 대한 '수락'을 의미한다.
						LoginManager lm = new LoginManager(AppliedTeamActivity.this);
						new GCMManager(AppliedTeamActivity.this).sendMessage(regid, lm.getTeamName() + "팀과의 매치가 성사되었습니다!", matchNo, 1);
						finish();
					}
					else
						Toast.makeText(AppliedTeamActivity.this, "신청 수락을 실패하였습니다.", 0).show();
				} catch (JSONException e) {
					Log.e("acceptOpposingTeam", e.getMessage());
				}
			}
		}.execute();
	}
}
