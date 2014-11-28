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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MyMatchConfirmedFragment extends Fragment implements OnItemClickListener {
	ArrayList<MyMatchItem> myMatchList;
	MyMatchListAdapter mmlAdapter;
	ListView list;
	TextView count;
	TextView empty;
	
	// 레이아웃 생성
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_my_match, container, false);
		return view;
	}
	
	// 뷰 참조
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		count = (TextView) getView().findViewById(R.id.count);

		// 리스트 객체 초기화
		myMatchList = new ArrayList<MyMatchItem>();
		
	    // 어댑터 생성
	    mmlAdapter = new MyMatchListAdapter( getActivity(), myMatchList );
	    
	    // 리스트뷰 생성 및 설정
	    list = (ListView) getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(mmlAdapter);
	    list.setOnItemClickListener(this);
	    
	    // 엠티뷰 텍스트 설정
	    empty = (TextView)getView().findViewById(R.id.empty);
	    empty.setText("성사된 매치가 없습니다.");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// 서버로부터 매치 리스트를 가져온다.
		getMyMatchConfirmed();
	}
	
	// 매치 클릭 이벤트
	// 매치 정보 출력
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
		intent.putExtra("matchNo", myMatchList.get(position-1).getMatchNo());
		startActivity(intent);
	}
	
	private class MyMatchListAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<MyMatchItem> list;
		private LayoutInflater inflater;
		
		public MyMatchListAdapter( Context c, ArrayList<MyMatchItem> list ) {
			this.context = c;
			this.list = list;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public MyMatchItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if( convertView == null ) {
				convertView = inflater.inflate(R.layout.my_match_item, parent, false);
			}
			
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(list.get(position).getLocation());
			
			TextView ground = (TextView) convertView.findViewById(R.id.ground);
			ground.setText(list.get(position).getGround());
			
			TextView date = (TextView) convertView.findViewById(R.id.date);
			date.setText(list.get(position).getDate() + " " + list.get(position).getDayOfWeek());
			
			TextView time = (TextView) convertView.findViewById(R.id.time);
			time.setText(list.get(position).getSession());
			
			// 매치가 성사된 팀 이름 출력
			TextView state = (TextView) convertView.findViewById(R.id.state);
			state.setText(getItem(position).getTeamName());
			state.setTextColor(getResources().getColor(android.R.color.holo_green_light));
			return convertView;
		}
		
	}
	
	// 서버로부터 내가 등록한 매치 중 성사된 매치를 가져오는 메서드
	private void getMyMatchConfirmed() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.my_match_list_confirmed);
		
		// 파라미터 구성
		LoginManager lm = new LoginManager(getActivity());
		String param = "email=" + lm.getEmail();
		
		// 서버 연결
		new HttpAsyncTask(url, param) {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				JSONArray jsonArr = null;
				
				try {
					myMatchList.clear();
					json = new JSONObject(result);
					jsonArr = json.getJSONArray("list");

					JSONObject item;

					
					for (int i = 0; i < jsonArr.length(); i++) {
						item = jsonArr.getJSONObject(i);
						myMatchList.add(new MyMatchItem(item.getInt("MATCH_NO"), 
								item.getString("LOCATION"), 
								item.getString("GROUND"), 
								item.getString("MATCH_DATE"), 
								item.getString("MATCH_TIME"),
								item.getString("MATCH_TIME2"), 
								item.getString("TEAM_NAME")));
					}
				} catch (JSONException e) {
					myMatchList.clear();
					Log.e("getMyMatchConfirmed", e.getMessage());
				} finally {
					mmlAdapter.notifyDataSetChanged();
					count.setText("총 " + myMatchList.size() + "개");
				}
				
			}
			
		}.execute();
	}
}
