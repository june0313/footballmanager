package june.footballmanager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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

public class MatchListFragment extends Fragment implements OnItemClickListener, OnScrollListener, DialogInterface.OnClickListener {
	private static final int MATCH_CONDITION = 0;
	private static final int ADD_MATCH = 1;
	
	ListView list;
	TextView count;
	TextView txtSort;		// 정렬기준 text
	ArrayList<MatchItem> matchList;
	MatchListAdapter mlAdapter;
	
	// 로그인 정보
	LoginManager lm;
	
	// 스크랩 리스트
	ArrayList<Integer> scrappedList;
	
	// 검색조건 프리퍼런스
	SharedPreferences prefCondition;
	SharedPreferences.Editor prefConditionEditor;

	// 프레그먼트가 생성될 때 최초 한번만 실행
	// 프레그먼트의 onCreate에서는 UI 작업을 할 수 없다.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		// 로그인정보 가져오기
		lm = new LoginManager(getActivity());
		
		// 검색조건 가져오기
		prefCondition = getActivity().getSharedPreferences("matchConditions", Context.MODE_PRIVATE);
		prefConditionEditor = prefCondition.edit();
		
		// 리스트는 최초 한번만 생성한다.
		matchList = new ArrayList<MatchItem>();
		
		// 어댑터 생성
		mlAdapter = new MatchListAdapter(getActivity(), matchList);
	}
	
	// 루트 view를 생성하여 리턴한다.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_list, container, false);
		
		// count(매치 개수) TextView 참초
		count = (TextView) view.findViewById(R.id.count);

		// 검색조건 TextView 참조
		txtSort = (TextView) view.findViewById(R.id.txt_sort);
		int which = prefCondition.getInt("orderCondition", 0);
		txtSort.setText(getResources().getStringArray(R.array.match_sort)[which]);
		txtSort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
				b.setTitle("정렬기준 선택");
				b.setItems(R.array.match_sort,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								// 정렬기준 저장
								prefConditionEditor.putInt("orderCondition",
										which);
								prefConditionEditor.commit();

								txtSort.setText(getResources().getStringArray(
										R.array.match_sort)[which]);

								// 설정된 정렬 기준으로 리스트를 다시 불러온다.
								getMatchList(0);
							}
						});
				b.create().show();
			}
		});

		// 리스트 생성 및 설정
		list = (ListView) view.findViewById(R.id.list);
		list.setEmptyView(view.findViewById(R.id.empty));
		list.addHeaderView(new View(getActivity()), null, true);
		list.addFooterView(new View(getActivity()), null, true);
		list.setAdapter(mlAdapter);
		list.setOnItemClickListener(this);
		list.setOnScrollListener(this);
		
		return view;
	}
	
	public void listCountUpdate() {
		count.setText("총 " + matchList.size() + "개");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// 리스트가 비어있으면 서버로부터 리스트를 가져온다.
		if (matchList.size() == 0) {
			getMatchList(0);
		} else
			// 리스트가 이미 차있으면 개수만 새로 출력한다.
			listCountUpdate();
		
		// 로컬DB에서 스크랩 정보 가져오기
		DatabaseHandler db = new DatabaseHandler(MatchListFragment.this.getActivity());
		scrappedList = db.getScrappedMatchList();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case MATCH_CONDITION:
		case ADD_MATCH:
			// 검색 조건 설정 및 매치 등록 후에는 리스트를 갱신한다.
			if (resultCode == Activity.RESULT_OK) {
				getMatchList(0);
			}
		}
	}
	
	// 매치 아이템 클릭 이벤트
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// 매치 상세 액티비티 실행
		Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
		// 매치 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		position--;
		intent.putExtra("matchNo", matchList.get(position).getMatchNo());
		startActivity(intent);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.match_list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    int itemId = item.getItemId();
	  
	    // 매치 등록 버튼 클릭시
		if (itemId == R.id.add) {
			// 팀계정 로그인 여부를 확인한다.
			if(lm.isLogin() && lm.getMemberType().equals("팀회원")) {
				Intent intent = new Intent(getActivity(), AddMatchActivity.class);
				startActivityForResult(intent, ADD_MATCH);
	    	} 
			else if(lm.isLogin() && lm.getMemberType().equals("선수회원")) {
	    		Toast.makeText(getActivity(), "매치 등록은 팀 계정만 가능합니다", 0).show();
	    	} 
			else {
	    		// 로그인 할것인지 묻는 다이얼로그를 띄운다.
	    		showLoginAlert();
	    	}
		} 
		else if (itemId == R.id.search) {
			// 검색조건 Activity 호출
			Intent intent = new Intent(getActivity(), SetMatchConditionActivity.class);
			startActivityForResult(intent, MATCH_CONDITION);
		} 
		else if (itemId == R.id.refresh) {
			// load match list
		 	getMatchList(0);
		}	    
	    return true;
	}
	
	// 어댑터 정의
	public class MatchListAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<MatchItem> list;
		private LayoutInflater inflater;
		
		public MatchListAdapter( Context c, ArrayList<MatchItem> list ) {
			this.context = c;
			this.list = list;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public MatchItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if( convertView == null ) {
				convertView = inflater.inflate(R.layout.match_item, parent, false);
			}
			
			TextView dateHeader = (TextView) convertView.findViewById(R.id.date_header);
			
			// 정렬 기준이 "등록날짜순" 일 때 만 날짜구분선을 출력한다.
			if (prefCondition.getInt("orderCondition", 0) < 2) {
				

				// 첫번째 아이템이거나, 이전 아이템과 등록 날짜가 다른경우 등록 날짜를 출력한다.
				if (position == 0
						|| !getItem(position - 1).getPostedDate().equals(
								getItem(position).getPostedDate())) {
					dateHeader.setText(getItem(position).getPostedDate());
					dateHeader.setVisibility(View.VISIBLE);
				} else
					dateHeader.setVisibility(View.GONE);
			}
			else
				// 이전 정렬 기록이 남아있을 수 있으므로 다른 경우에는 보이지 않게 해준다.
				dateHeader.setVisibility(View.GONE);
				

			TextView teamName = (TextView) convertView.findViewById(R.id.mi_team_name);
			teamName.setText(getItem(position).getTeamName() );
			
			TextView ages = (TextView) convertView.findViewById(R.id.ages);
			ages.setText(getItem(position).getAges());
			
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(getItem(position).getLocation());
			
			TextView ground = (TextView) convertView.findViewById(R.id.ground);
			ground.setText(getItem(position).getGround());
			
			TextView date = (TextView) convertView.findViewById(R.id.date);
			date.setText(getItem(position).getDate() + " " + getItem(position).getDayOfWeek());
			
			TextView time = (TextView) convertView.findViewById(R.id.time);
			time.setText(getItem(position).getSession());
			
			TextView state = (TextView) convertView.findViewById(R.id.state);
			// 매치 상태에 따라 다른 text 출력
			// 0 : 상대팀 신청 대기중
			// 1 : 매치가 성사됨
			// 2 : 매치가 종료됨
			switch(getItem(position).getState()) {
			case 0:
				state.setText(getItem(position).getApplyCnt() + "팀 신청");
				state.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
				break;
			case 1:
				state.setText("매칭 완료");
				state.setTextColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case 2:
				state.setText("종료됨");
				state.setTextColor(getResources().getColor(R.color.gray));
				break;
			}
			
			// 스크랩 버튼 
			ImageView scrap = (ImageView)convertView.findViewById(R.id.img_scrap);
			Integer matchNo = new Integer(getItem(position).getMatchNo());
			boolean isScrapped = scrappedList.contains(matchNo);
			// 스크랩 여부에 따라 다른 이미지를 출력한다.
			if(isScrapped)
				scrap.setImageResource(R.drawable.scrapped);
			else
				scrap.setImageResource(R.drawable.scrap);
			
			// 클릭 이벤트 리스너 등록
			scrap.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ImageView imgView = (ImageView)v;
					DatabaseHandler db = new DatabaseHandler(MatchListFragment.this.getActivity());
					boolean isScrapped = db.selectScrapMatch(getItem(position).getMatchNo());
					
					if(isScrapped) {
						db.deleteScrapMatch(getItem(position).getMatchNo());
						imgView.setImageResource(R.drawable.scrap);
					} else {
						db.insertScrapMatch(getItem(position).getMatchNo());
						imgView.setImageResource(R.drawable.scrapped);
					}
				}
				
			});
			return convertView;
		}
	}
	
	// 서버로부터 매치 리스트를 가져오는 메서드
	private void getMatchList(final int startIdx) {
		
		String url = getString(R.string.server) + getString(R.string.match_list);
			
		// 시간대 배열
		String[] startTimes = getResources().getStringArray(R.array.start_time);
		String[] endTimes = getResources().getStringArray(R.array.end_time);
		
		// 검색 조건 파리미터 구성
		String param = "location=" + prefCondition.getString("location", "전국");
		param += "&startTime=" + startTimes[prefCondition.getInt("time", 0)];
		param += "&endTime=" + endTimes[prefCondition.getInt("time", 0)];
		for( int i = 0; i < 7; i++ )
			param += "&day" + i + "=" + prefCondition.getBoolean("day" + i, true);
		for( int i = 0; i < 6; i++ )
			param += "&age" + i + "=" + prefCondition.getBoolean("age" + i, true);
		param += "&startIdx=" + startIdx;
		param += "&orderCondition=" + prefCondition.getInt("orderCondition", 0);
		
		
		// 서버 연결
		new HttpAsyncTask(url, param, getActivity(), "잠시만 기다려 주세요...") {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				JSONArray jsonArr = null;
				
				try {
					json = new JSONObject(result);
					
					// check the success of getting information
					if (json.getInt("success") == 1) {
						
						// 리스트가 처음부터 출력되는 경우 기존의 리스트를 clear 한다.
						if(startIdx == 0) matchList.clear();

						jsonArr = json.getJSONArray("list");

						JSONObject jo;
						
						// 추가로 가져온 레코드를 리스트에 추가한다.
						for (int i = 0; i < jsonArr.length(); i++) {
							jo = jsonArr.getJSONObject(i);
							matchList.add(new MatchItem(jo));
						}
					} 
				} catch (JSONException e) {
					//matchList.clear();
				} finally {
					mlAdapter.notifyDataSetChanged();
					listCountUpdate();
				}
			}
		}.execute();
	}
	
	// 리스트뷰 스크롤 이벤트 관련 콜백 메서드와 플래그
	boolean isEndOfList = false;
	int totalCount;
	
	// 스크롤이 발생하면 호출된다.
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		Log.i("FM", "first : " + firstVisibleItem + " visiCnt : " + visibleItemCount + " totalCnt : " + totalItemCount);
		
		// 첫번째 아이템의 인덱스 + 보이는 아이템의 개수가 총 아이템의 개수와 같으면
		// 마지막 아이템이 보이는 상태
		if( firstVisibleItem + visibleItemCount == totalItemCount ) {
			totalCount = totalItemCount;
			isEndOfList = true;
		}
		else
			isEndOfList = false;
	}
	
	// 스크롤 상태가 변할 때 호출된다.
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// 리스트의 끝에 도달하면
		if( scrollState == SCROLL_STATE_IDLE && isEndOfList ) {
			
			// 서버로부터 다음 리스트를 가져온다.
			//new GetMatchList().execute( new Integer[]{ totalCount -1 } );
			getMatchList(totalCount - 1);
		}
	}
	
	// 로그인 다이얼로그를 띄운다.
	private void showLoginAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("매치를 등록하려면 팀 계정으로 로그인 해야합니다.\n\n로그인 하시겠습니까?")
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


