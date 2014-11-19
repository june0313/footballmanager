package june.footballmanager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ScrappedMatchListFragment extends Fragment implements OnItemClickListener {
	ArrayList<MatchItem> scrappedMatchList;
	ScrappedMatchListAdapter malAdapter;
	ListView list;
	TextView count;
	TextView empty;
	
	// 스크랩한 매치 번호 스트링
	String scrappedItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_list,
				container, false);
	}

	// 뷰 참조
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		count = (TextView) getView().findViewById(R.id.count);
		
		// 리스트 객체 생성 및 초기화
		scrappedMatchList = new ArrayList<MatchItem>();
		
		// 어댑터 생성
		malAdapter = new ScrappedMatchListAdapter( getActivity(), scrappedMatchList );
		
		// 리스트뷰 생성 및 설정
	    list = (ListView) getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(malAdapter);
	    list.setOnItemClickListener(this);
	    
	    // 엠티뷰 텍스트 설정
	    empty = (TextView)getView().findViewById(R.id.empty);
	    empty.setText("스크랩한 매치가 없습니다.");
	    
	    // DB로부터 스크랩 목록 가져오기
	    DatabaseHandler db = new DatabaseHandler(getActivity());
		scrappedItems = db.getAllScrapMatch();
		Log.i("Scrapped Match List", scrappedItems);
	}

	@Override
	public void onResume() {
		super.onResume();
		getScrappedMatchList();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		// 매치 상세 액티비티 실행
		Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
		// 매치 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		position--;
		intent.putExtra("matchNo", scrappedMatchList.get(position).getMatchNo());
		startActivity(intent);
	}
	
	// 어댑터 정의
	public class ScrappedMatchListAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<MatchItem> list;
		private LayoutInflater inflater;

		public ScrappedMatchListAdapter(Context c, ArrayList<MatchItem> list) {
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
		public MatchItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.match_item, parent,
						false);
			}

			TextView teamName = (TextView) convertView
					.findViewById(R.id.mi_team_name);
			teamName.setText(getItem(position).getTeamName());

			TextView ages = (TextView) convertView.findViewById(R.id.ages);
			ages.setText(getItem(position).getAges());

			TextView location = (TextView) convertView
					.findViewById(R.id.location);
			location.setText(getItem(position).getLocation());

			TextView ground = (TextView) convertView.findViewById(R.id.ground);
			ground.setText(getItem(position).getGround());

			TextView date = (TextView) convertView.findViewById(R.id.date);
			date.setText(getItem(position).getDate() + " "
					+ getItem(position).getDayOfWeek());

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
			ImageView scrap = (ImageView) convertView
					.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(
					ScrappedMatchListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapMatch(getItem(position)
					.getMatchNo());
			// 즐겨찾기 여부에 따라 다른 이미지를 출력한다.
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
							ScrappedMatchListFragment.this.getActivity());
					boolean isScrapped = db.selectScrapMatch(getItem(position)
							.getMatchNo());

					if (isScrapped) {
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
	
	// 서버로부터 스크랩한 매치 리스트를 가져오는 메서드
	private void getScrappedMatchList() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.scrapped_match_list);
		
		// 파라미터 구성
		String param = "matchNos=" + scrappedItems;
		
		// 서버 연결
		JSONObject json = new HttpTask(url, param).getJSONObject();
		JSONArray jsonArr = null;
		
		try {
			jsonArr = json.getJSONArray("list");

			JSONObject item;

			scrappedMatchList.clear();
			for (int i = 0; i < jsonArr.length(); i++) {
				item = jsonArr.getJSONObject(i);
				scrappedMatchList.add(new MatchItem(item));
			}
		} catch (JSONException e) {
			scrappedMatchList.clear();
			Log.e("getScrappedMatchList", e.getMessage());
		} finally {
			malAdapter.notifyDataSetChanged();
			count.setText("총 " + scrappedMatchList.size() + "개");
		}
	}
}
