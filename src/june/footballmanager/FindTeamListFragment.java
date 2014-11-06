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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FindTeamListFragment extends Fragment implements
		OnItemClickListener {
	ListView list;
	TextView count;
	TextView empty;
	TextView txtSort; // ���ı��� text
	ArrayList<FindTeamItem> findTeamList;
	FindTeamListAdapter tlAdapter;

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

		// ����Ʈ ��ü ����
		findTeamList = new ArrayList<FindTeamItem>();

		// ����� ��ü ����
		tlAdapter = new FindTeamListAdapter(getActivity(), findTeamList);

		list = (ListView) getView().findViewById(R.id.list);
		list.setEmptyView(getView().findViewById(R.id.empty));
		list.addHeaderView(new View(getActivity()), null, true);
		list.addFooterView(new View(getActivity()), null, true);
		list.setAdapter(tlAdapter);
		list.setOnItemClickListener(this);

		// ��Ƽ�� �ؽ�Ʈ ����
		empty = (TextView) getView().findViewById(R.id.empty);
		empty.setText("�Խù��� �������� �ʽ��ϴ�.");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// DB -> ����Ʈ�� ���
		new GetFindTeamList().execute();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long arg3) {
		
		Intent intent = new Intent(getActivity(), FindTeamDetailActivity.class);

		// �� ��ȣ�� �Ѱ���
		// ����䰡 �߰��Ǿ��� ������ �ε����� 1 ���ҽ�Ų��.
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

			if (lm.isLogin() && lm.getMemberType().equals("����ȸ��")) {
				startActivity(new Intent(getActivity(),
						AddFindTeamActivity.class));
			} else {
				Toast.makeText(getActivity(),
						"�� ���� �Խù��� ����Ϸ��� ���� �������� �α��� �ؾ��մϴ�.", 0).show();
			}
			break;

		case R.id.search:
			startActivity(new Intent(getActivity(),SetFindTeamConditionActivity.class));
		}

		return super.onOptionsItemSelected(item);
	}

	// ����� ����
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
			
			// ù��° �������̰ų�, ���� �����۰� ��� ��¥�� �ٸ���� ��� ��¥�� ����Ѵ�.
			if(position == 0 || !getItem(position-1).getPostedDate().equals(getItem(position).getPostedDate())) {
				dateHeader.setText(getItem(position).getPostedDate());
				dateHeader.setVisibility(View.VISIBLE);
			} else
				dateHeader.setVisibility(View.GONE);
			
			// ���� �г��� ���
			TextView nickname = (TextView)convertView.findViewById(R.id.nickname);
			nickname.setText(getItem(position).getNickName());
			
			// ���� ���
			TextView title = (TextView)convertView.findViewById(R.id.title);
			title.setText(getItem(position).getTitle());
			
			// ������ ���
			TextView tvPosition = (TextView)convertView.findViewById(R.id.position);
			tvPosition.setText(getItem(position).getPosition());
			
			String strPos = getItem(position).getPosition();
			
			// �����Ǻ� ���� ó��
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
			
			// ���� ���
			TextView age = (TextView)convertView.findViewById(R.id.age);
			age.setText(getItem(position).getAge() + "��");
			
			// ���� ���
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(getItem(position).getLocation());
			
			// ���ã�� ��ư
			ImageView scrap = (ImageView) convertView
					.findViewById(R.id.img_scrap);
			DatabaseHandler db = new DatabaseHandler(
					FindTeamListFragment.this.getActivity());
			boolean isScrapped = db.selectScrapFindTeam(getItem(position)
					.getNo());

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

	// �� ����Ʈ�� DB���� �����´�.
	private class GetFindTeamList extends AsyncTask<Void, Void, Boolean> {
		String param = "";

		// URL�κ��� ������ json ������ string
		String jsonString = "";

		ProgressDialog pd;

		public void onPreExecute() {

			// ���α׷��� ���̾�α� ���
			pd = new ProgressDialog(getActivity());
			pd.setMessage("����Ʈ�� �ҷ����� ���Դϴ�...");
			pd.show();
			
			// �˻� ���� �����۷��� ����
			SharedPreferences prefCondition = getActivity()
					.getSharedPreferences("findTeam", Context.MODE_PRIVATE);

			// �˻� ���� �ĸ����� ����
			param += "location=" + prefCondition.getString("location", "����");
			for (int i = 0; i < 15; i++)
				param += "&pos" + i + "="
						+ prefCondition.getBoolean("pos" + i, true);
			param += "&startAge=" + prefCondition.getInt("startAge", 0);
			param += "&endAge=" + prefCondition.getInt("endAge", 99);
			Log.i("param", param);
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {

			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.find_team_list));
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

				Log.i("FM", "GetFindTeamList result : " + jsonString);

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

				findTeamList.clear();
				for (int i = 0; i < jsonArr.length(); i++) {
					item = jsonArr.getJSONObject(i);
					findTeamList.add(new FindTeamItem(item));
				}
			} catch (JSONException e) {
				findTeamList.clear();
				e.printStackTrace();
			} finally {

				tlAdapter.notifyDataSetChanged();
				count.setText("�� " + findTeamList.size() + "��");

				// ���α׷��� ���̾�α� ����
				pd.dismiss();
			}
		}
	}
}
