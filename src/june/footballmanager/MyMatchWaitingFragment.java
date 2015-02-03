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
import android.widget.Toast;

public class MyMatchWaitingFragment extends Fragment implements OnItemClickListener {
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
	    empty.setText("대기중인 매치가 없습니다.");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// 서버로부터 매치 리스트를 가져온다.
		getMyMatchWaiting();
	}
	
	// 매치 클릭 이벤트
	// 매치를 신청한 팀의 리스트를 출력하는 액티비티 호출
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if(myMatchList.get(position - 1).getApplyCnt() == 0) {
			Toast.makeText(getActivity(), "신청한 팀이 없습니다", 0).show();
			return;
		}
		
		Intent intent = new Intent(getActivity(), AppliedTeamActivity.class);
		
		// 헤더 뷰 추가로 인한 positioin 감소
		intent.putExtra("matchNo", myMatchList.get(position - 1).getMatchNo());
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
			
			TextView state = (TextView) convertView.findViewById(R.id.state);
			if( getItem(position).getApplyCnt() == 0 ) {
				state.setText("상대팀 신청 대기중");
				state.setTextColor(getResources().getColor(android.R.color.darker_gray));
			} else {
				state.setText( Integer.toString(getItem(position).getApplyCnt()) + "팀 신청" );
				state.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
			}
			
			return convertView;
		}
		
	}
	
	// 서버로부터 내가 등록한 매치 중 성사된 매치를 가져오는 메서드
	private void getMyMatchWaiting() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server) + getString(R.string.my_match_list);
		
		// 파라미터 구성
		LoginManager lm = new LoginManager(getActivity());
		String param = "email=" + lm.getEmail();
		
		// 서버 연결
		new HttpAsyncTask(url, param) {

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				JSONArray jsonArr = null;
				
				// 네트워크 접속 실패 다이얼로그 출력
				if (result == null) {
					NetworkErrorDialog.create(getActivity()).show();
					return;
				}
				
				try {
					myMatchList.clear();
					json = new JSONObject(result);
					jsonArr = json.getJSONArray("list");
					JSONObject item;
					
					
					for( int i = 0; i < jsonArr.length(); i++ ) {
						item = jsonArr.getJSONObject(i);
						myMatchList.add( new MyMatchItem( item.getInt("MATCH_NO"),
								item.getString("LOCATION"),
								item.getString("GROUND"),
								item.getString("MATCH_DATE"),
								item.getString("MATCH_TIME"), 
								item.getString("MATCH_TIME2"),
								item.getInt("APPLY_CNT")
								)
						);
					}
				} catch (JSONException e) {
					myMatchList.clear();
					Log.e("getMyMatchWaiting",e.getMessage());
				} finally {
					mmlAdapter.notifyDataSetChanged();
					count.setText("총 " + myMatchList.size() + "개");
				}
			}
			
		}.execute();
		
	}
}
