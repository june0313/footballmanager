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
import android.view.View.OnClickListener;
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
	
	// ��ũ���� ��ġ ��ȣ ��Ʈ��
	String scrappedItems;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_list,
				container, false);
	}

	// �� ����
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		count = (TextView) getView().findViewById(R.id.count);
		
		// ����Ʈ ��ü ���� �� �ʱ�ȭ
		scrappedMatchList = new ArrayList<MatchItem>();
		
		// ����� ����
		malAdapter = new ScrappedMatchListAdapter( getActivity(), scrappedMatchList );
		
		// ����Ʈ�� ���� �� ����
	    list = (ListView) getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(malAdapter);
	    list.setOnItemClickListener(this);
	    
	    // ��Ƽ�� �ؽ�Ʈ ����
	    empty = (TextView)getView().findViewById(R.id.empty);
	    empty.setText("��ũ���� ��ġ�� �����ϴ�.");
	    
	    // DB�κ��� ��ũ�� ��� ��������
	    DatabaseHandler db = new DatabaseHandler(getActivity());
		scrappedItems = db.getAllScrapMatch();
		Log.i("Scrapped Match List", scrappedItems);
	}

	@Override
	public void onResume() {
		super.onResume();
		new GetScrappedMatchList().execute();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		// ��ġ �� ��Ƽ��Ƽ ����
		Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
		// ��ġ ��ȣ�� �Ѱ���
		// ����䰡 �߰��Ǿ��� ������ �ε����� 1 ���ҽ�Ų��.
		position--;
		intent.putExtra("matchNo", scrappedMatchList.get(position).getMatchNo());
		startActivity(intent);
	}
	
	// ����� ����
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
			ImageView scrap = (ImageView) convertView
					.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(
					ScrappedMatchListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapMatch(getItem(position)
					.getMatchNo());
			// ���ã�� ���ο� ���� �ٸ� �̹����� ����Ѵ�.
			if (isScrapped)
				scrap.setImageResource(R.drawable.scrapped);
			else
				scrap.setImageResource(R.drawable.scrap);

			// Ŭ�� �̺�Ʈ ������ ���
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
	
	// ��û�� ��ġ ����Ʈ�� DB���� �����´�.
	private class GetScrappedMatchList extends AsyncTask<Void, Void, Boolean> {

		// ������ ������ �Ķ����(���� email����)
		String param = "";

		// URL�κ��� ������ json ������ string
		String jsonString = "";

		ProgressDialog pd;

		public void onPreExecute() {
			param += "matchNos=" + scrappedItems;
			Log.i("param", param);

			// ���α׷��� ���̾�α� ���
			pd = new ProgressDialog(getActivity());
			pd.setMessage("����Ʈ�� �ҷ����� ���Դϴ�...");
			pd.show();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {

			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.scrapped_match_list));
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				// URL�� �ĸ����� �ѱ��
				OutputStreamWriter out = new OutputStreamWriter(
						conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();

				// URL ��� ��������
				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();

				Log.i("FM", "GetScrapList result : " + jsonString);

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

				scrappedMatchList.clear();
				for (int i = 0; i < jsonArr.length(); i++) {
					item = jsonArr.getJSONObject(i);
					scrappedMatchList.add(new MatchItem(item));
				}
			} catch (JSONException e) {
				scrappedMatchList.clear();
				e.printStackTrace();
			} finally {

				malAdapter.notifyDataSetChanged();
				count.setText("�� " + scrappedMatchList.size() + "��");

				// ���α׷��� ���̾�α� ����
				pd.dismiss();
			}
		}
	}
}