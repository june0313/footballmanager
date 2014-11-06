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
	
	// ���̾ƿ� ����
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_my_match, container, false);
		return view;
	}
	
	// �� ����
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		count = (TextView) getView().findViewById(R.id.count);

		// ����Ʈ ��ü �ʱ�ȭ
		myMatchList = new ArrayList<MyMatchItem>();
		
	    // ����� ����
	    mmlAdapter = new MyMatchListAdapter( getActivity(), myMatchList );
	    
	    // ����Ʈ�� ���� �� ����
	    list = (ListView) getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(mmlAdapter);
	    list.setOnItemClickListener(this);
	    
	    // ��Ƽ�� �ؽ�Ʈ ����
	    empty = (TextView)getView().findViewById(R.id.empty);
	    empty.setText("����� ��ġ�� �����ϴ�.");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// ��ġ ����Ʈ �ε�
		new GetMyMatchList().execute();	
	}
	
	// ��ġ Ŭ�� �̺�Ʈ
	// ��ġ ���� ���
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
			
			// ��ġ�� ����� �� �̸� ���
			TextView state = (TextView) convertView.findViewById(R.id.state);
			state.setText(getItem(position).getTeamName());
			state.setTextColor(getResources().getColor(android.R.color.holo_green_light));
			return convertView;
		}
		
	}
	
	// DB�κ��� ����� ��ġ ����Ʈ�� ��ȸ�Ͽ� �����´�.
	private class GetMyMatchList extends AsyncTask<Void, Void, Boolean> {
		
		// ������ ������ �Ķ����(���� email����)
		String param = "";
		
		// �α��� ����
		LoginManager lm;
		
		// URL�κ��� ������ json ������ string 
		String jsonString = "";
		
		ProgressDialog pd;
		
		public void onPreExecute() {
			lm = new LoginManager(getActivity());
			param += "email=" + lm.getEmail();
			
			Log.i("param", param);
			
			// ���α׷��� ���̾�α� ���
			pd = new ProgressDialog(getActivity());
			pd.setMessage("����Ʈ�� �ҷ����� ���Դϴ�...");
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
				
				// URL�� �ĸ����� �ѱ��
				OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream(), "euc-kr" );
				out.write(param);
				out.flush();
				out.close();
				
				// URL ��� ��������
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
				count.setText("�� " + myMatchList.size() + "��");
				
				// ���α׷��� ���̾�α� ����
				pd.dismiss();
			}
		}
	}
	
}
