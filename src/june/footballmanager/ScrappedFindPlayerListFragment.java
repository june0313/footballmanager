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

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */

public class ScrappedFindPlayerListFragment extends Fragment implements OnItemClickListener {
	ListView list;
	TextView empty;
	TextView count;
	TextView txtSort;		// 정렬기준 text
	ArrayList<FindPlayerItem> playerList;
	FindPlayerListAdapter plAdapter;
	
	// 스크랩한 글 번호 스트링
	String scrappedItems;
	
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
	    empty.setText("스크랩한 게시물이 존재하지 않습니다.");
	    
	    // DB로부터 스크랩 목록 가져오기
	    DatabaseHandler db = new DatabaseHandler(getActivity());
		scrappedItems = db.getAllScrapFindPlayer();
		Log.i("Scrapped Find Player List", scrappedItems);
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
		
		getFindPlayerList();
	}

	// 어댑터 정의
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
			
			// 날짜 뷰
			TextView dateHeader = (TextView)convertView.findViewById(R.id.date_header);
			dateHeader.setVisibility(View.GONE);
			
			TextView teamName = (TextView)convertView.findViewById(R.id.team_name);
			teamName.setText(getItem(position).getTeamName());
			
			TextView title = (TextView)convertView.findViewById(R.id.title);
			title.setText(getItem(position).getTitle());
			
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
			
			TextView ages = (TextView)convertView.findViewById(R.id.ages);
			ages.setText(getItem(position).getAges());

			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(getItem(position).getLocation());
			
			// 즐겨찾기 버튼
			ImageView scrap = (ImageView) convertView
					.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(
					ScrappedFindPlayerListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapFindPlayer(getItem(position).getNo());
			
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
							ScrappedFindPlayerListFragment.this.getActivity());
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
	
	// 서버로부터 스크랩한 선수모집 리스트를 가져오는 메서드
	private void getFindPlayerList() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.scrapped_find_player_list);
		
		// 파라미터 설정
		String param = "nos=" + scrappedItems;
		
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

					playerList.clear();
					for (int i = 0; i < jsonArr.length(); i++) {
						item = jsonArr.getJSONObject(i);
						playerList.add(new FindPlayerItem(item));
					}
					
				} catch (JSONException e) {
					playerList.clear();
					Log.i("getFindPlayerList", e.getMessage());
				} finally {
					plAdapter.notifyDataSetChanged();
					count.setText("총 " + playerList.size() + "개");
				}	
				
			}
			
		}.execute();
		
	}
}


