package june.footballmanager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */

public class FindPlayerListFragment extends Fragment implements OnItemClickListener, DialogInterface.OnClickListener {
	ListView list;
	TextView empty;
	TextView count;
	TextView txtSort;		// 정렬기준 text
	ArrayList<FindPlayerItem> playerList;
	FindPlayerListAdapter plAdapter;
	
	ArrayList<Integer> scrappedList;	// 스크랩 리스트
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_list, container, false);
	    	    
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		count = (TextView) getView().findViewById(R.id.count);
		
		// 리스트 객체 생성
		playerList = new ArrayList<FindPlayerItem>();
		
		// 어댑터 객체 생성
		plAdapter = new FindPlayerListAdapter(getActivity(), playerList);
		
		list = (ListView) getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(plAdapter);
	    list.setOnItemClickListener(this);
	    
	    // 엠티뷰 텍스트 설정
	    empty = (TextView)getView().findViewById(R.id.empty);
	    empty.setText("게시물이 존재하지 않습니다.");
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		Intent intent = new Intent(getActivity(), FindPlayerDetailActivity.class);
		
		// 게시물 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		position--;
		intent.putExtra("no", playerList.get(position).getNo());
		startActivity(intent);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// 로컬DB에서 스크랩 정보 가져오기
		DatabaseHandler db = new DatabaseHandler(FindPlayerListFragment.this.getActivity());
		scrappedList = db.getScrappedFindPlayer();
		
		// 서버로부터 선수찾기 리스트를 가져온다.
		getFindPlayerList();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {		
		inflater.inflate(R.menu.find_player_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {
		case R.id.add:
			LoginManager lm = new LoginManager(getActivity());
			if(lm.isLogin() && lm.getMemberType().equals("팀회원")) {
	    		startActivity(new Intent(getActivity(), AddFindPlayerActivity.class));
	    	} else if(lm.isLogin() && lm.getMemberType().equals("선수회원")) {
	    		Toast.makeText(getActivity(), "선수모집 글 작성은 팀 계정만 가능합니다", 0).show();
	    	} else {
	    		// 로그인 할것인지 묻는 다이얼로그를 띄운다.
	    		showLoginAlert();
	    	}
			break;
			
		case R.id.search:
			startActivity(new Intent(getActivity(), SetFindPlayerConditionActivity.class));
		}
		
		return super.onOptionsItemSelected(item);
	}

	// 선수모집 리스트 어댑터
	public class FindPlayerListAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<FindPlayerItem> list;
		private LayoutInflater inflater;

		public FindPlayerListAdapter(Context c, ArrayList<FindPlayerItem> list) {
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
			if(position == 0 || !getItem(position-1).getPostedDate().equals(getItem(position).getPostedDate())) {
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
			Integer no = new Integer(getItem(position).getNo());
			boolean isScrapped = scrappedList.contains(no);
			
			// 스크랩 여부에 따라 다른 이미지를 출력한다.
			if (isScrapped)
				scrap.setImageResource(R.drawable.scrapped);
			else
				scrap.setImageResource(R.drawable.scrap);

			// 클릭 이벤트 리스너 등록
			scrap.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ImageView imgView = (ImageView) v;
					DatabaseHandler db = new DatabaseHandler(
							FindPlayerListFragment.this.getActivity());
					boolean isScrapped = db.selectScrapFindPlayer(getItem(position)
							.getNo());

					if (isScrapped) {
						db.deleteScrapFindPlayer(getItem(position).getNo());
						imgView.setImageResource(R.drawable.scrap);
					} else {
						db.insertScrapFindPlayer(getItem(position).getNo());
						imgView.setImageResource(R.drawable.scrapped);
					}
				}

			});
			

			return convertView;
		}
	}
	
	private void getFindPlayerList() {
		// 웹 서버 URL
		String url = getString(R.string.server) + getString(R.string.find_player_list);
		
		// 검색 조건 프리퍼런스 열기
		SharedPreferences prefCondition = getActivity().getSharedPreferences("findPlayer", Context.MODE_PRIVATE);
					
		// 검색 조건 파리미터 구성
		String param = "location=" + prefCondition.getString("location", "전국");
		for( int i = 0; i < 15; i++ )
			param += "&pos" + i + "=" + prefCondition.getBoolean("pos" + i, true);
		for( int i = 0; i < 6; i++ )
			param += "&age" + i + "=" + prefCondition.getBoolean("age" + i, true);
		param += "&actDay=" + prefCondition.getString("actDay", "무관");
		for( int i = 0; i < 7; i++ )
			param += "&day" + i + "=" + prefCondition.getBoolean("day" + i, true);
		
		String[] startTimes = getResources().getStringArray(R.array.start_time);
		String[] endTimes = getResources().getStringArray(R.array.end_time);
		
		param += "&startTime=" + startTimes[prefCondition.getInt("time", 0)];
		param += "&endTime=" + endTimes[prefCondition.getInt("time", 0)];
		
		// 서버 연결
		new HttpAsyncTask(url, param) {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				JSONArray jsonArr = null;
				
				// 리스트 업데이트
				try {
					json = new JSONObject(result);
					jsonArr = json.getJSONArray("list");
					JSONObject item;

					playerList.clear();
					for (int i = 0; i < jsonArr.length(); i++) {
						item = jsonArr.getJSONObject(i);
						playerList.add(new FindPlayerItem(item));
					}
					
				} catch (JSONException e) {
					playerList.clear();
					Log.e("getFindPlayerList", e.getMessage());
				} finally {
					plAdapter.notifyDataSetChanged();
					count.setText("총 " + playerList.size() + "개");
				}
			}
			
		}.execute();
	}
	
	// 로그인 다이얼로그를 띄운다.
	private void showLoginAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("선수모집 글을 작성하려면 팀 계정으로 로그인 해야합니다.\n\n로그인 하시겠습니까?")
		.setNegativeButton("아니오", null)
		.setPositiveButton("예", this);
		
		AlertDialog ad = builder.create();
		ad.show();
	}
	
	// 다이얼로그 이벤트 콜백 메서드
	@Override
	public void onClick(DialogInterface dialog, int which) {
		startActivity(new Intent(this.getActivity(), LoginActivity.class));
	}
}


