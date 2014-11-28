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

public class FindTeamListFragment extends Fragment implements
		OnItemClickListener, DialogInterface.OnClickListener {
	ListView list;
	TextView count;
	TextView empty;
	TextView txtSort; // 정렬기준 text
	ArrayList<FindTeamItem> findTeamList;
	FindTeamListAdapter tlAdapter;
	
	ArrayList<Integer> scrappedList;	// 스크랩 리스트

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
		empty.setText("게시물이 존재하지 않습니다.");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// 로컬DB에서 스크랩 정보 가져오기
		DatabaseHandler db = new DatabaseHandler(FindTeamListFragment.this.getActivity());
		scrappedList = db.getScrappedFindTeam();
		
		// 서버로부터 팀구함 게시물의 리스트를 가져온다.
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.find_team_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.add:
			LoginManager lm = new LoginManager(getActivity());

			if (lm.isLogin() && lm.getMemberType().equals("선수회원")) {
				startActivity(new Intent(getActivity(),
						AddFindTeamActivity.class));
			} else if(lm.isLogin() && lm.getMemberType().equals("팀회원")) {
				Toast.makeText(getActivity(),"팀 구함 글 작성은 선수회원만 가능합니다", 0).show();
			} else {
				// 로그인 할것인지 묻는 다이얼로그를 띄운다.
	    		showLoginAlert();
			}
			break;

		case R.id.search:
			startActivity(new Intent(getActivity(),SetFindTeamConditionActivity.class));
		}

		return super.onOptionsItemSelected(item);
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
			TextView dateHeader = (TextView)convertView.findViewById(R.id.date_header);
			
			// 첫번째 아이템이거나, 이전 아이템과 등록 날짜가 다른경우 등록 날짜를 출력한다.
			if(position == 0 || !getItem(position-1).getPostedDate().equals(getItem(position).getPostedDate())) {
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
			
			// 즐겨찾기 버튼
			ImageView scrap = (ImageView) convertView.findViewById(R.id.img_scrap);
			Integer no = new Integer(getItem(position).getNo());
			boolean isScrapped = scrappedList.contains(no);

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
							FindTeamListFragment.this.getActivity());
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

	// 서버로부터 팀구함 리스트를 가져오는 메서드
	private void getFindTeamList() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.find_team_list);

		// 검색 조건 프리퍼런스 열기
		SharedPreferences prefCondition = getActivity().getSharedPreferences(
				"findTeam", Context.MODE_PRIVATE);

		// 검색 조건 파리미터 구성
		String param = "location=" + prefCondition.getString("location", "전국");
		for (int i = 0; i < 15; i++)
			param += "&pos" + i + "="
					+ prefCondition.getBoolean("pos" + i, true);
		param += "&startAge=" + prefCondition.getInt("startAge", 0);
		param += "&endAge=" + prefCondition.getInt("endAge", 99);
		
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
					Log.e("getFindTeamList", e.getMessage());
				} finally {
					tlAdapter.notifyDataSetChanged();
					count.setText("총 " + findTeamList.size() + "개");
				}
			}
		}.execute();
	}
	
	// 로그인 다이얼로그를 띄운다.
	private void showLoginAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("팀 구함 글을 작성하려면 선수 계정으로 로그인 해야합니다.\n\n로그인 하시겠습니까?")
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
