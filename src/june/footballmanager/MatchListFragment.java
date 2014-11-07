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
	TextView txtSort;		// ���ı��� text
	ArrayList<MatchItem> matchList;
	MatchListAdapter mlAdapter;
	
	// �α��� ����
	LoginManager lm;

	// �����׸�Ʈ�� ������ �� ���� �ѹ��� ����
	// �����׸�Ʈ�� onCreate������ UI �۾��� �� �� ����.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		// �α������� ��������
		lm = new LoginManager(getActivity());
		
		matchList = new ArrayList<MatchItem>();
		
		// ����� ����
		mlAdapter = new MatchListAdapter(getActivity(), matchList);
		
	}
	
	// ��Ʈ view�� �����Ͽ� �����Ѵ�.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_list, container, false);
		return view;
	}
	
	// Activity�� setContentView()�������� ����
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// count(��ġ ����) TextView ����
		count = (TextView)getView().findViewById(R.id.count);
		
		// �˻����� TextView ����
		txtSort = (TextView)getView().findViewById(R.id.txt_sort);
		txtSort.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
			}
		});
		
		// ����Ʈ ���� �� ����
	    list = (ListView)getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(mlAdapter);
	    list.setOnItemClickListener(this);
	    list.setOnScrollListener(this);
	}
	
	public void listCountUpdate() {
		count.setText("�� " + matchList.size() + "���� ��ġ");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// ����Ʈ�� ��������� �����κ��� ����Ʈ�� �����´�.
		if (matchList.size() == 0) {
			GetMatchList gml = new GetMatchList();
			gml.execute(new Integer[] { 0 });
		} else
			// ����Ʈ�� �̹� �������� ������ ���� ����Ѵ�.
			listCountUpdate();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case MATCH_CONDITION:
			// �˻� ���� ���� �Ŀ��� ���ǿ� �°� �ٽ� �����´�.
			if (resultCode == Activity.RESULT_OK) {
				GetMatchList gml = new GetMatchList();
				gml.execute(new Integer[] { 0 });
			}
		}
	}
	
	// ��ġ ������ Ŭ�� �̺�Ʈ
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		// ��ġ �� ��Ƽ��Ƽ ����
		Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
		// ��ġ ��ȣ�� �Ѱ���
		// ����䰡 �߰��Ǿ��� ������ �ε����� 1 ���ҽ�Ų��.
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
	  
	    // ��ġ ��� ��ư Ŭ����
		if (itemId == R.id.add) {
			// ������ �α��� ���θ� Ȯ���Ѵ�.
			if(lm.isLogin() && lm.getMemberType().equals("��ȸ��")) {
	    		startActivity(new Intent(getActivity(), AddMatchActivity.class));
	    	} else {
	    		Toast.makeText(getActivity(), "��ġ�� ����Ϸ��� �� �������� �α��� �ؾ��մϴ�.", 0).show();
	    	}
		} else if (itemId == R.id.search) {
			// �˻����� Activity ȣ��
			Intent intent = new Intent(getActivity(), SetMatchConditionActivity.class);
			startActivityForResult(intent, MATCH_CONDITION);
		} else if (itemId == R.id.refresh) {
			// load match list
		 	GetMatchList gml = new GetMatchList();
			gml.execute( new Integer[]{0} );
		}	    
	    return true;
	}
	
	// ����� ����
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
			
			// ù��° �������̰ų�, ���� �����۰� ��� ��¥�� �ٸ���� ��� ��¥�� ����Ѵ�.
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
			// ��ġ ���¿� ���� �ٸ� text ���
			// 0 : ����� ��û �����
			// 1 : ��ġ�� �����
			// 2 : ��ġ�� �����
			switch(getItem(position).getState()) {
			case 0:
				state.setText(getItem(position).getApplyCnt() + "�� ��û");
				state.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
				break;
			case 1:
				state.setText("��Ī �Ϸ�");
				state.setTextColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case 2:
				state.setText("�����");
				state.setTextColor(getResources().getColor(R.color.gray));
				break;
			}
			
			// ���ã�� ��ư 
			ImageView scrap = (ImageView)convertView.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(MatchListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapMatch(getItem(position).getMatchNo());
			// ���ã�� ���ο� ���� �ٸ� �̹����� ����Ѵ�.
			if(isScrapped)
				scrap.setImageResource(R.drawable.scrapped);
			else
				scrap.setImageResource(R.drawable.scrap);
			
			// Ŭ�� �̺�Ʈ ������ ���
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
		
		// ����� �˻� ���� ��������
		SharedPreferences prefCondition;
		
		// �˻� ������ ������ �Ķ���� ���ڿ�
		String param = "";
		
		ProgressDialog pd;
		
		boolean isInitial = false;
		
		@Override
		public void onPreExecute() {
			
			// ���â ���
			pd = new ProgressDialog(getActivity());
			pd.setMessage("����Ʈ�� �ҷ����� ���Դϴ�...");
			pd.show();
			
			// �ð��� �迭
			String[] startTimes = getResources().getStringArray(R.array.start_time);
			String[] endTimes = getResources().getStringArray(R.array.end_time);
			
			// �˻� ���� �����۷��� ����
			prefCondition = getActivity().getSharedPreferences("matchConditions", Context.MODE_PRIVATE);
			
			// �˻� ���� �ĸ����� ����
			param += "location=" + prefCondition.getString("location", "����");
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
				
				// ������ ���ڵ��� ���� �ε����� �Ķ���Ϳ� �߰����ش�.
				param += "&startIdx=" + params[0];
				
				// ���� �ε����� 0 �̸� ����Ʈ�� ���ʷ� ����ϴ� �� �̹Ƿ� isInitial �÷��׸� true�� �������ش�.
				if( params[0] == 0) isInitial = true;
				
				// URL�� �ĸ����� �Ѱ��ֱ�
				OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream(), "euc-kr" );
				out.write(param);
				out.flush();
				out.close();
				
				// �������� ��� ��������
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
				
				// ����Ʈ�� ó������ ��µǴ� ��� ������ ����Ʈ�� clear �Ѵ�.
				if(isInitial)
					matchList.clear();
				
				// check the success of getting information
				if (jsonObj.getInt("success") == 1) {
					isSuccess = true;

					jsonArr = jsonObj.getJSONArray("list");

					JSONObject jo;
					
					// �߰��� ������ ���ڵ带 ����Ʈ�� �߰��Ѵ�.
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
	
	
	// ����Ʈ�� ��ũ�� �̺�Ʈ ���� �ݹ� �޼���� �÷���
	boolean isEndOfList = false;
	int totalCount;
	
	// ��ũ���� �߻��ϸ� ȣ��ȴ�.
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {

		Log.i("FM", "first : " + firstVisibleItem + " visiCnt : " + visibleItemCount + " totalCnt : " + totalItemCount);
		
		// ù��° �������� �ε��� + ���̴� �������� ������ �� �������� ������ ������
		// ������ �������� ���̴� ����
		if( firstVisibleItem + visibleItemCount == totalItemCount ) {
			totalCount = totalItemCount;
			isEndOfList = true;
		}
		else
			isEndOfList = false;
	}
	
	// ��ũ�� ���°� ���� �� ȣ��ȴ�.
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// ����Ʈ�� ���� �����ϸ�
		if( scrollState == SCROLL_STATE_IDLE && isEndOfList ) {
			
			// �����κ��� ���� ����Ʈ�� �����´�.
			new GetMatchList().execute( new Integer[]{ totalCount -1 } );
		}
	}
}


