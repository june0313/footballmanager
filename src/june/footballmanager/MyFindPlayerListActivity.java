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

public class MyFindPlayerListActivity extends Activity implements OnItemClickListener{
	
	TextView txtCount;
	TextView txtSort;
	ListView listview;
	TextView empty;
	ArrayList<FindPlayerItem> items;
	MyFindPlayerListAdapter adapter;
	
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
	    
	    items = new ArrayList<FindPlayerItem>();
	    
	    adapter = new MyFindPlayerListAdapter(this, items);
	    
	    listview = (ListView)findViewById(R.id.list);
	    listview.setEmptyView(empty);
	    listview.addHeaderView(new View(this), null, true);
	    listview.addFooterView(new View(this), null, true);  
	    listview.setAdapter(adapter);
	    listview.setOnItemClickListener(this);
	    
	    // 리스트 출력
	    getMyFindPlayerList();
	}
	
	public void listCountUpdate() {
		txtCount.setText("총 " + items.size() + "개");
	}

	// 리스트뷰 클릭 콜백 메서드
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long arg3) {
		
		Intent intent = new Intent(this, FindPlayerDetailActivity.class);

		// 글 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		intent.putExtra("no", items.get(position - 1).getNo());
		startActivity(intent);
	}
	
	// 서버로부터 리스트를 가져온후 어댑터에게 알린다.
	private void getMyFindPlayerList() {
		String url = getString(R.string.server) + getString(R.string.my_find_player_list);
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
						items.add(new FindPlayerItem(item));
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
	
	// 내가 등록한 선수모집 리스트 어댑터
	public class MyFindPlayerListAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<FindPlayerItem> list;
		private LayoutInflater inflater;

		public MyFindPlayerListAdapter(Context c, ArrayList<FindPlayerItem> list) {
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
		public FindPlayerItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.find_player_item, parent,
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
	
			
			// 팀이름 출력
			TextView teamName = (TextView)convertView.findViewById(R.id.team_name);
			teamName.setText(getItem(position).getTeamName());
			
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
			
			// 연령대 출력
			TextView ages = (TextView)convertView.findViewById(R.id.ages);
			ages.setText(getItem(position).getAges());

			// 지역 출력
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(getItem(position).getLocation());
			
			// 활동요일 출력
			TextView tvActDay = (TextView)convertView.findViewById(R.id.act_day);
			tvActDay.setText(getItem(position).getActDay());
			
			// 활동시간(세션) 출력
			TextView tvActSession = (TextView)convertView.findViewById(R.id.act_session);
			tvActSession.setText(getItem(position).getActSession());
			
			// 스크랩 버튼
			ImageView scrap = (ImageView) convertView.findViewById(R.id.img_scrap);
			scrap.setVisibility(View.INVISIBLE);

			return convertView;
		}
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
}
