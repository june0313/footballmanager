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

public class ScrappedFindTeamListFragment extends Fragment implements
		OnItemClickListener {
	ListView list;
	TextView count;
	TextView empty;
	TextView txtSort; // 정렬기준 text
	ArrayList<FindTeamItem> findTeamList;
	FindTeamListAdapter tlAdapter;

	// 스크랩한 글 번호 스트링
	String scrappedItems;

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

		setHasOptionsMenu(true);

		count = (TextView) getView().findViewById(R.id.count);

		// 리스트 객체 생성
		findTeamList = new ArrayList<FindTeamItem>();

		// 어댑터 객체 생성
		tlAdapter = new FindTeamListAdapter(getActivity(), findTeamList);

		list = (ListView) getView().findViewById(R.id.list);
		list.setEmptyView(getView().findViewById(R.id.empty));
		list.addHeaderView(new View(getActivity()), null, true);
		list.addFooterView(new View(getActivity()), null, true);
		list.setAdapter(tlAdapter);
		list.setOnItemClickListener(this);

		// 엠티뷰 텍스트 설정
		empty = (TextView) getView().findViewById(R.id.empty);
		empty.setText("스크랩한 게시물이 존재하지 않습니다.");
		
		// DB로부터 스크랩 목록 가져오기
	    DatabaseHandler db = new DatabaseHandler(getActivity());
		scrappedItems = db.getAllScrapFindTeam();
		Log.i("Scrapped Find Team List", scrappedItems);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// 서버로부터 스크랩한 팀구함 리스트를 가져온다.
		getFindTeamList();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long arg3) {
		
		Intent intent = new Intent(getActivity(), FindTeamDetailActivity.class);

		// 글 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		intent.putExtra("no", findTeamList.get(position - 1).getNo());
		startActivity(intent);
	}

	// 어댑터 정의
	public class FindTeamListAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<FindTeamItem> list;
		private LayoutInflater inflater;

		public FindTeamListAdapter(Context c, ArrayList<FindTeamItem> list) {
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
			
			// 즐겨찾기 버튼
			ImageView scrap = (ImageView) convertView
					.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(
					ScrappedFindTeamListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapFindTeam(getItem(position)
					.getNo());

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
							ScrappedFindTeamListFragment.this.getActivity());
					boolean isScrapped = db.selectScrapFindTeam(getItem(
							position).getNo());

					if (isScrapped) {
						db.deleteScrapFindTeam(getItem(position).getNo());
						imgView.setImageResource(R.drawable.scrap);
					} else {
						db.insertScrapFindTeam(getItem(position).getNo());
						imgView.setImageResource(R.drawable.scrapped);
					}
				}

			});

			return convertView;
		}
	}
	
	// 서버로부터 스크랩한 팀구함 리스트를 가져오는 메서드
	private void getFindTeamList() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.scrapped_find_team_list);
		
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

					findTeamList.clear();
					for (int i = 0; i < jsonArr.length(); i++) {
						item = jsonArr.getJSONObject(i);
						findTeamList.add(new FindTeamItem(item));
					}
					
				} catch (JSONException e) {
					findTeamList.clear();
					Log.i("getFindTeamList", e.getMessage());
				} finally {
					tlAdapter.notifyDataSetChanged();
					count.setText("총 " + findTeamList.size() + "개");
				}	
			}
			
		}.execute();
		
	}
}
