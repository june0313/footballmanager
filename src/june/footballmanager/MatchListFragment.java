package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

public class MatchListFragment extends Fragment implements OnItemClickListener, OnScrollListener {
	private static final int MATCH_CONDITION = 0;
	
	ListView list;
	TextView count;
	TextView txtSort;		// 정렬기준 text
	ArrayList<MatchItem> matchList;
	MatchListAdapter mlAdapter;
	
	// 로그인 정보
	LoginManager lm;

	// 프레그먼트가 생성될 때 최초 한번만 실행
	// 프레그먼트의 onCreate에서는 UI 작업을 할 수 없다.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		// 로그인정보 가져오기
		lm = new LoginManager(getActivity());
		
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
		return view;
	}
	
	// Activity의 setContentView()다음으로 실행
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// count(매치 개수) TextView 참초
		count = (TextView)getView().findViewById(R.id.count);
		
		// 검색조건 TextView 참조
		txtSort = (TextView)getView().findViewById(R.id.txt_sort);
		txtSort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
			}
		});
		
		// 리스트 생성 및 설정
	    list = (ListView)getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(mlAdapter);
	    list.setOnItemClickListener(this);
	    list.setOnScrollListener(this);
	}
	
	public void listCountUpdate() {
		count.setText("총 " + matchList.size() + "개의 매치");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// 리스트가 비어있으면 서버로부터 리스트를 가져온다.
		if (matchList.size() == 0) {
			GetMatchList gml = new GetMatchList();
			gml.execute(new Integer[] { 0 });
		} else
			// 리스트가 이미 차있으면 개수만 새로 출력한다.
			listCountUpdate();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case MATCH_CONDITION:
			// 검색 조건 설정 후에는 조건에 맞게 다시 가져온다.
			if (resultCode == Activity.RESULT_OK) {
				GetMatchList gml = new GetMatchList();
				gml.execute(new Integer[] { 0 });
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
	    		startActivity(new Intent(getActivity(), AddMatchActivity.class));
	    	} else {
	    		Toast.makeText(getActivity(), "매치를 등록하려면 팀 계정으로 로그인 해야합니다.", 0).show();
	    	}
		} else if (itemId == R.id.search) {
			// 검색조건 Activity 호출
			Intent intent = new Intent(getActivity(), SetMatchConditionActivity.class);
			startActivityForResult(intent, MATCH_CONDITION);
		} else if (itemId == R.id.refresh) {
			// load match list
		 	GetMatchList gml = new GetMatchList();
			gml.execute( new Integer[]{0} );
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
			TextView dateHeader = (TextView)convertView.findViewById(R.id.date_header);
			
			// 첫번째 아이템이거나, 이전 아이템과 등록 날짜가 다른경우 등록 날짜를 출력한다.
			if(position == 0 || !getItem(position-1).getPostedDate().equals(getItem(position).getPostedDate())) {
				dateHeader.setText(getItem(position).getPostedDate());
				dateHeader.setVisibility(View.VISIBLE);
			} else
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
			
			// 즐겨찾기 버튼 
			ImageView scrap = (ImageView)convertView.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(MatchListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapMatch(getItem(position).getMatchNo());
			// 즐겨찾기 여부에 따라 다른 이미지를 출력한다.
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
	
	private class GetMatchList extends AsyncTask<Integer, Void, Boolean> {
		
		String jsonString = "";
		
		JSONArray jsonArr;
		
		// 저장된 검색 조건 가져오기
		SharedPreferences prefCondition;
		
		// 검색 조건을 저장할 파라미터 문자열
		String param = "";
		
		ProgressDialog pd;
		
		boolean isInitial = false;
		
		@Override
		public void onPreExecute() {
			
			// 대기창 출력
			pd = new ProgressDialog(getActivity());
			pd.setMessage("리스트를 불러오는 중입니다...");
			pd.show();
			
			// 시간대 배열
			String[] startTimes = getResources().getStringArray(R.array.start_time);
			String[] endTimes = getResources().getStringArray(R.array.end_time);
			
			// 검색 조건 프리퍼런스 열기
			prefCondition = getActivity().getSharedPreferences("matchConditions", Context.MODE_PRIVATE);
			
			// 검색 조건 파리미터 구성
			param += "location=" + prefCondition.getString("location", "전국");
			param += "&startTime=" + startTimes[prefCondition.getInt("time", 0)];
			param += "&endTime=" + endTimes[prefCondition.getInt("time", 0)];
			for( int i = 0; i < 7; i++ )
				param += "&day" + i + "=" + prefCondition.getBoolean("day" + i, true);
			for( int i = 0; i < 6; i++ )
				param += "&age" + i + "=" + prefCondition.getBoolean("age" + i, true);
			
			Log.i("param", param);
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
		
			Boolean isSuccess = false;
			
			try {
				URL url = new URL(getString(R.string.server) + getString(R.string.match_list));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				// 가져올 레코드의 시작 인덱스를 파라미터에 추가해준다.
				param += "&startIdx=" + params[0];
				
				// 시작 인덱스가 0 이면 리스트를 최초로 출력하는 것 이므로 isInitial 플래그를 true로 설정해준다.
				if( params[0] == 0) isInitial = true;
				
				// URL에 파리미터 넘겨주기
				OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream(), "euc-kr" );
				out.write(param);
				out.flush();
				out.close();
				
				// 웹페이지 결과 가져오기
				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();
				
				Log.i("FM", "GetMatchList result : " + jsonString);
				
			} catch (ProtocolException e) {
				Log.e("FM", "GetMatchList : " + e.getMessage());
			} catch (MalformedURLException e) {
				Log.e("FM", "GetMatchList : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "GetMatchList : " + e.getMessage());
			}

			return isSuccess;
		}
		
		public void onPostExecute(Boolean isSuccess) {
			
			JSONObject jsonObj;
			
			try {

				jsonObj = new JSONObject(jsonString);
				
				// 리스트가 처음부터 출력되는 경우 기존의 리스트를 clear 한다.
				if(isInitial)
					matchList.clear();
				
				// check the success of getting information
				if (jsonObj.getInt("success") == 1) {
					isSuccess = true;

					jsonArr = jsonObj.getJSONArray("list");

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

				pd.dismiss();
			}
		}		
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
			new GetMatchList().execute( new Integer[]{ totalCount -1 } );
		}
	}
}


