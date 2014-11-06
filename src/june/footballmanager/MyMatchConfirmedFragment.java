package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
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
		
		// 매치 리스트 로드
		new GetMyMatchList().execute();	
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
	
	// DB로부터 등록한 매치 리스트를 조회하여 가져온다.
	private class GetMyMatchList extends AsyncTask<Void, Void, Boolean> {
		
		// 서버로 전달할 파라미터(팀의 email정보)
		String param = "";
		
		// 로그인 정보
		LoginManager lm;
		
		// URL로부터 가져온 json 형식의 string 
		String jsonString = "";
		
		ProgressDialog pd;
		
		public void onPreExecute() {
			lm = new LoginManager(getActivity());
			param += "email=" + lm.getEmail();
			
			Log.i("param", param);
			
			// 프로그레스 다이얼로그 출력
			pd = new ProgressDialog(getActivity());
			pd.setMessage("리스트를 불러오는 중입니다...");
			pd.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			
			try {
				URL url = new URL(getString(R.string.server) + getString(R.string.my_match_list_confirmed));
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				// URL에 파리미터 넘기기
				OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream(), "euc-kr" );
				out.write(param);
				out.flush();
				out.close();
				
				// URL 결과 가져오기
				String buffer = null;
				BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream(), "euc-kr" ));
				while( ( buffer = in.readLine() ) != null ) {
					jsonString += buffer;
				}
				in.close();
				
				Log.i( "FM", "GetMyMatchList result : " + jsonString );
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			return null;
		}
		
		public void onPostExecute(Boolean isSuccess) {
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				JSONArray jsonArr = jsonObj.getJSONArray("list");
				
				JSONObject item;
				
				myMatchList.clear();
				for( int i = 0; i < jsonArr.length(); i++ ) {
					item = jsonArr.getJSONObject(i);
					myMatchList.add( new MyMatchItem( item.getInt("MATCH_NO"),
							item.getString("LOCATION"),
							item.getString("GROUND"),
							item.getString("MATCH_DATE"),
							item.getString("MATCH_TIME"), 
							item.getString("MATCH_TIME2"),
							item.getString("TEAM_NAME")
							)
					);
				}
			} catch (JSONException e) {
				myMatchList.clear();
				e.printStackTrace();
			} finally {
				
				mmlAdapter.notifyDataSetChanged();
				count.setText("총 " + myMatchList.size() + "개");
				
				// 프로그레스 다이얼로그 종료
				pd.dismiss();
			}
		}
	}
	
}
