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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyApplicationListFragment extends Fragment implements OnItemClickListener {
	ArrayList<ApplicationItem> myApplicationList;
	MyApplicationListAdapter malAdapter;
	ListView list;
	TextView count;
	TextView empty;
	
	int isAccepted;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.activity_my_match,
				container, false);
	}

	// 뷰 참조
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// 매치 상태 저장
		Bundle extra = getArguments();
		isAccepted = extra.getInt("ISACCEPTED");
		
		count = (TextView) getView().findViewById(R.id.count);
		
		// 리스트 객체 생성 및 초기화
		myApplicationList = new ArrayList<ApplicationItem>();
		
		// 어댑터 생성
		malAdapter = new MyApplicationListAdapter( getActivity(), myApplicationList );
		
		// 리스트뷰 생성 및 설정
	    list = (ListView) getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(malAdapter);
	    list.setOnItemClickListener(this);
	    
	    // 엠티뷰 텍스트 설정
	    empty = (TextView)getView().findViewById(R.id.empty);
	    
	    if(isAccepted == 0)
	    	empty.setText("신청한 매치가 없습니다.");
	    else if(isAccepted == 0)
	    	empty.setText("수락된 매치가 없습니다.");
	    else
	    	empty.setText("거절된 매치가 없습니다.");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		new GetMyApplicationList().execute();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		// 매치 상세 액티비티 실행
		Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
		// 매치 번호를 넘겨줌
		// 헤더뷰가 추가되었기 때문에 인덱스를 1 감소시킨다.
		position--;
		intent.putExtra("matchNo", myApplicationList.get(position).getMatchNo());
		startActivity(intent);
	}
	
	// 어댑터 정의
	public class MyApplicationListAdapter extends BaseAdapter {

		private Context context;
		private ArrayList<ApplicationItem> list;
		private LayoutInflater inflater;

		public MyApplicationListAdapter(Context c, ArrayList<ApplicationItem> list) {
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
		public ApplicationItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.match_item, parent,
						false);
			}
			
			TextView dateHeader = (TextView)convertView.findViewById(R.id.date_header);
			// 첫번째 아이템이거나, 이전 아이템과 등록 날짜가 다른경우 등록 날짜를 출력한다.
			if (position == 0
					|| !getItem(position - 1).getPostedDate().equals(
							getItem(position).getPostedDate())) {
				dateHeader.setText(getItem(position).getPostedDate());
				dateHeader.setVisibility(View.VISIBLE);
			} else
				dateHeader.setVisibility(View.GONE);

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
			
			// 스크랩 버튼 disable
			ImageView scrap = (ImageView) convertView.findViewById(R.id.img_scrap);
			scrap.setVisibility(View.INVISIBLE);
			
			TextView state = (TextView) convertView.findViewById(R.id.state);
			// 매치 수락 상태에 따라 다른 text 출력
			// 0 : 수락 대기중
			// 1 : 신청이 수락됨
			// 2 : 다른팀의 신청을 수락함(우리팀의 신청이 거절당함)
			switch(getItem(position).getAcceptState()) {
			case 0:
				state.setText("수락 대기중");
				state.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
				break;
			case 1:
				state.setText("매칭 완료");
				state.setTextColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case 2:
				state.setText("다른팀과 매칭되었습니다");
				state.setTextColor(getResources().getColor(R.color.gray));
				break;
			}

			return convertView;
		}
	}
	
	// 신청한 매치 리스트를 DB에서 가져온다.
	private class GetMyApplicationList extends AsyncTask<Void, Void, Boolean> {

		// 서버로 전달할 파라미터(팀의 email정보)
		String param = "";

		// 로그인 정보
		LoginManager lm;

		// URL로부터 가져온 json 형식의 string
		String jsonString = "";

		ProgressDialog pd;

		public void onPreExecute() {
			lm = new LoginManager(getActivity());
			param += "memberNo=" + lm.getMemberNo();
			param += "&isAccepted=" + isAccepted;

			Log.i("param", param);

			// 프로그레스 다이얼로그 출력
			pd = new ProgressDialog(getActivity());
			pd.setMessage("리스트를 불러오는 중입니다...");
			pd.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {

			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.my_application_list));
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				// URL에 파리미터 넘기기
				OutputStreamWriter out = new OutputStreamWriter(
						conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();

				// URL 결과 가져오기
				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();

				Log.i("FM", "GetMyApplicationList result : " + jsonString);

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

				myApplicationList.clear();
				for (int i = 0; i < jsonArr.length(); i++) {
					item = jsonArr.getJSONObject(i);
					myApplicationList.add(new ApplicationItem(item));
				}
			} catch (JSONException e) {
				myApplicationList.clear();
				e.printStackTrace();
			} finally {

				malAdapter.notifyDataSetChanged();
				count.setText("총 " + myApplicationList.size() + "개");

				// 프로그레스 다이얼로그 종료
				pd.dismiss();
			}
		}
	}
}
