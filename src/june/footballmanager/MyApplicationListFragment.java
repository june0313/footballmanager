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

	// �� ����
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// ��ġ ���� ����
		Bundle extra = getArguments();
		isAccepted = extra.getInt("ISACCEPTED");
		
		count = (TextView) getView().findViewById(R.id.count);
		
		// ����Ʈ ��ü ���� �� �ʱ�ȭ
		myApplicationList = new ArrayList<ApplicationItem>();
		
		// ����� ����
		malAdapter = new MyApplicationListAdapter( getActivity(), myApplicationList );
		
		// ����Ʈ�� ���� �� ����
	    list = (ListView) getView().findViewById(R.id.list);
	    list.setEmptyView(getView().findViewById(R.id.empty));
	    list.addHeaderView(new View(getActivity()), null, true);
	    list.addFooterView(new View(getActivity()), null, true);
	    list.setAdapter(malAdapter);
	    list.setOnItemClickListener(this);
	    
	    // ��Ƽ�� �ؽ�Ʈ ����
	    empty = (TextView)getView().findViewById(R.id.empty);
	    
	    if(isAccepted == 0)
	    	empty.setText("��û�� ��ġ�� �����ϴ�.");
	    else if(isAccepted == 0)
	    	empty.setText("������ ��ġ�� �����ϴ�.");
	    else
	    	empty.setText("������ ��ġ�� �����ϴ�.");
	}

	@Override
	public void onResume() {
		super.onResume();
		
		// �����κ��� ��û ����� �����´�.
		getMyApplicationList();
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
		// ��ġ �� ��Ƽ��Ƽ ����
		Intent intent = new Intent(getActivity(), MatchDetailActivity.class);
		// ��ġ ��ȣ�� �Ѱ���
		// ����䰡 �߰��Ǿ��� ������ �ε����� 1 ���ҽ�Ų��.
		position--;
		intent.putExtra("matchNo", myApplicationList.get(position).getMatchNo());
		startActivity(intent);
	}
	
	// ����� ����
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
			// ù��° �������̰ų�, ���� �����۰� ��� ��¥�� �ٸ���� ��� ��¥�� ����Ѵ�.
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
			
			// ��ũ�� ��ư disable
			ImageView scrap = (ImageView) convertView.findViewById(R.id.img_scrap);
			scrap.setVisibility(View.INVISIBLE);
			
			TextView state = (TextView) convertView.findViewById(R.id.state);
			// ��ġ ���� ���¿� ���� �ٸ� text ���
			// 0 : ���� �����
			// 1 : ��û�� ������
			// 2 : �ٸ����� ��û�� ������(�츮���� ��û�� ��������)
			switch(getItem(position).getAcceptState()) {
			case 0:
				state.setText("���� �����");
				state.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
				break;
			case 1:
				state.setText("��Ī �Ϸ�");
				state.setTextColor(getResources().getColor(android.R.color.holo_green_light));
				break;
			case 2:
				state.setText("�ٸ����� ��Ī�Ǿ����ϴ�");
				state.setTextColor(getResources().getColor(R.color.gray));
				break;
			}

			return convertView;
		}
	}
	
	// �����κ��� ��û�� ��ġ ����Ʈ�� �������� �޼���
	private void getMyApplicationList() {
		// ������ �������� URL
		String url = getString(R.string.server) + getString(R.string.my_application_list);
		
		// �Ķ���� ����
		LoginManager lm = new LoginManager(getActivity());
		String param = "memberNo=" + lm.getMemberNo();
		param += "&isAccepted=" + isAccepted;
		
		// ���� ����
		JSONObject json = new HttpTask(url, param).getJSONObject();
		JSONArray jsonArr = null;
		
		try {
			jsonArr = json.getJSONArray("list");

			JSONObject item;

			myApplicationList.clear();
			for (int i = 0; i < jsonArr.length(); i++) {
				item = jsonArr.getJSONObject(i);
				myApplicationList.add(new ApplicationItem(item));
			}
		} catch (JSONException e) {
			myApplicationList.clear();
			Log.e("getMyApplicationList", e.getMessage());
		} finally {
			malAdapter.notifyDataSetChanged();
			count.setText("�� " + myApplicationList.size() + "��");
		}
	}
}
