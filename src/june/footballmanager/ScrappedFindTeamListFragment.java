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
	TextView txtSort; // ���ı��� text
	ArrayList<FindTeamItem> findTeamList;
	FindTeamListAdapter tlAdapter;

	// ��ũ���� �� ��ȣ ��Ʈ��
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
		empty.setText("��ũ���� �Խù��� �������� �ʽ��ϴ�.");
		
		// DB�κ��� ��ũ�� ��� ��������
	    DatabaseHandler db = new DatabaseHandler(getActivity());
		scrappedItems = db.getAllScrapFindTeam();
		Log.i("Scrapped Find Team List", scrappedItems);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// �����κ��� ��ũ���� ������ ����Ʈ�� �����´�.
		getFindTeamList();
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
					ScrappedFindTeamListFragment.this.getActivity());
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
	
	// �����κ��� ��ũ���� ������ ����Ʈ�� �������� �޼���
	private void getFindTeamList() {
		// ������ �������� URL
		String url = getString(R.string.server) + getString(R.string.scrapped_find_team_list);
		
		// �Ķ���� ����
		String param = "nos=" + scrappedItems;
		
		// ���� ����
		JSONObject json = new HttpTask(url ,param).getJSONObject();
		JSONArray jsonArr = null;
		
		try {
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
			count.setText("�� " + findTeamList.size() + "��");
		}	
	}
}
