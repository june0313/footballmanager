package june.footballmanager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyFindTeamListActivity extends Activity implements OnItemClickListener{
	
	TextView txtCount;
	TextView txtSort;
	ListView listview;
	TextView empty;
	ArrayList<FindTeamItem> items;
	MyFindTeamListAdapter adapter;
	
	LoginManager lm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// 레이아웃 설정
		setContentView(R.layout.list_layout);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    // 로그인 관리자
	    lm = new LoginManager(this);
	    
	    // 뷰 참조
	    txtCount = (TextView)findViewById(R.id.count);
	    
	    txtSort = (TextView)findViewById(R.id.txt_sort);
	    txtSort.setVisibility(View.INVISIBLE);
	    
	    empty = (TextView)findViewById(R.id.empty);
	    empty.setText("작성한 글이 없습니다");
	    
	    items = new ArrayList<FindTeamItem>();
	    
	    adapter = new MyFindTeamListAdapter(this, items);
	    
	    listview = (ListView)findViewById(R.id.list);
	    listview.setEmptyView(empty);
	    listview.addHeaderView(new View(this), null, true);
	    listview.addFooterView(new View(this), null, true);  
	    listview.setAdapter(adapter);
	    listview.setOnItemClickListener(this);
	    
	    // 리스트 출력
	    getMyFindTeamList();
	}
	
	public void listCountUpdate() {
		txtCount.setText("총 " + items.size() + "개");
	}
	
	// 리스트뷰 클릭 콜백 메서드
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long arg3) {
		
		Intent intent = new Intent(this, FindTeamDetailActivity.class);

		// 글 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		intent.putExtra("no", items.get(position - 1).getNo());
		startActivity(intent);
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
	
	// 서버로부터 리스트를 가져와 출력한다.
	private void getMyFindTeamList() {
		String url = getString(R.string.server) + getString(R.string.my_find_team_list);
		String param = "memberNo=" + lm.getMemberNo();
		
		new HttpAsyncTask(url, param, this, "잠시만 기다려 주세요...") {
			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				JSONArray jsonArr = null;
				
				// 네트워크 접속 실패 다이얼로그 출력
				if (result == null) {
					NetworkErrorDialog.create(getApplicationContext()).show();
					return;
				}
				
				try {
					json = new JSONObject(result);
					jsonArr = json.getJSONArray("list");
					JSONObject item;

					items.clear();
					for (int i = 0; i < jsonArr.length(); i++) {
						item = jsonArr.getJSONObject(i);
						items.add(new FindTeamItem(item));
					}
				} catch (JSONException e) {
					items.clear();
					Log.e("getMyFindTeamList", e.getMessage());
				} finally {
					adapter.notifyDataSetChanged();
					listCountUpdate();
				}
			}
		}.execute();
	}
	
	// 어댑터 정의
	public class MyFindTeamListAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<FindTeamItem> list;
		private LayoutInflater inflater;

		public MyFindTeamListAdapter(Context c, ArrayList<FindTeamItem> list) {
			this.context = c;
			this.list = list;
			this.inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public FindTeamItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.find_team_item, parent,
						false);
			}
			TextView dateHeader = (TextView)convertView.findViewById(R.id.date_header);
			// 첫번째 아이템이거나, 이전 아이템과 등록 날짜가 다른경우 등록 날짜를 출력한다.
			if (position == 0
					|| !getItem(position - 1).getPostedDate().equals(
							getItem(position).getPostedDate())) {
				dateHeader.setText(getItem(position).getPostedDate());
				dateHeader.setVisibility(View.VISIBLE);
			} else
				dateHeader.setVisibility(View.GONE);
			
			// 선수 닉네임 출력
			TextView nickname = (TextView)convertView.findViewById(R.id.nickname);
			nickname.setText(getItem(position).getNickName());
			
			// 제목 출력
			TextView title = (TextView)convertView.findViewById(R.id.title);
			title.setText(getItem(position).getTitle());
			
			// 포지션 출력
			TextView tvPosition = (TextView)convertView.findViewById(R.id.position);
			tvPosition.setText(getItem(position).getPosition());
			
			String strPos = getItem(position).getPosition();
			
			// 포지션별 색상 처리
			if(strPos.equals("GK"))
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
			else if(strPos.equals("LB") || strPos.equals("CB") || strPos.equals("RB") 
					|| strPos.equals("LWB") || strPos.equals("RWB"))
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_green_light));
			else if(strPos.equals("LM") || strPos.equals("CM") || strPos.equals("RM") 
					|| strPos.equals("AM") || strPos.equals("DM"))
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
			else
				tvPosition.setTextColor(getResources().getColor(android.R.color.holo_red_light));
			
			// 나이 출력
			TextView age = (TextView)convertView.findViewById(R.id.age);
			age.setText(getItem(position).getAge() + "세");
			
			// 지역 출력
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(getItem(position).getLocation());
			
			// 활동 요일 출력
			TextView actDay = (TextView)convertView.findViewById(R.id.act_day);
			actDay.setText(getItem(position).getActDay());
			
			// 활동 시간 출력
			TextView actSession = (TextView)convertView.findViewById(R.id.act_session);
			actSession.setText(getItem(position).getActSession());
			
			// 즐겨찾기 버튼
			ImageView scrap = (ImageView)convertView.findViewById(R.id.img_scrap);
			scrap.setVisibility(View.INVISIBLE);

			return convertView;
		}
	}
}
