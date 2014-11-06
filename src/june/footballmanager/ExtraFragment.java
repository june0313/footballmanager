package june.footballmanager;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * 
 */
public class ExtraFragment extends Fragment implements OnItemClickListener {

	ListView list;
	ArrayAdapter<CharSequence> listAdapter;
	LoginManager lm;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_extra, container, false);
		
		// print the list
		list = (ListView) view.findViewById(R.id.extra_list);
		list.setOnItemClickListener(this);
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		// �α��� ������ �޴� �׸�(����Ʈ) ����
		int extraItems;
		lm = new LoginManager(getActivity());

		if (lm.isLogin() && lm.getMemberType().equals("����ȸ��"))
			extraItems = R.array.extra_items_on_player;
		else if (lm.isLogin() && lm.getMemberType().equals("��ȸ��"))
			extraItems = R.array.extra_items_on_team;
		else
			extraItems = R.array.extra_items;

		listAdapter = ArrayAdapter.createFromResource(getActivity(),
				extraItems, android.R.layout.simple_list_item_1);
		list.setAdapter(listAdapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		Intent intent;
		
		switch( position ) {
		case 0:
			// �������� ����
			// �α��� ���θ� Ȯ���Ͽ� ������ ��Ƽ��Ƽ ����
			if( lm.isLogin() && lm.getMemberType().equals("����ȸ��") ) {
				intent = new Intent(this.getActivity(), PlayerAccountActivity.class);
			} else if(lm.isLogin() && lm.getMemberType().equals("��ȸ��") ) {
				intent = new Intent(this.getActivity(), TeamAccountActivity.class);
			} else {
				intent = new Intent(this.getActivity(), LoginActivity.class);
			}
			
			this.getActivity().startActivity(intent);
			break;
			
		case 1:
			if (lm.isLogin()) {
				if(lm.getMemberType().equals("��ȸ��")) {
					// ��ȸ�� : ���� ����� ��ġ
					intent = new Intent(this.getActivity(), MyMatchActivity.class);
					getActivity().startActivity(intent);
				} else {
					// ����ȸ�� : ���� �� ��
					Toast.makeText(getActivity(), "���� �� ��", 0).show();
				}
			} else {
				// ��α��� : ��ũ��
				intent = new Intent(this.getActivity(), ScrapActivity.class);
				startActivity(intent);
			}
			
			break;
			
		case 2:
			if (lm.isLogin() ) {
				if(lm.getMemberType().equals("��ȸ��")) {
					// ���� ��û�� ��ġ
					intent = new Intent(this.getActivity(),MyApplicationActivity.class);
					getActivity().startActivity(intent);
				} else {
					// ����ȸ�� : ��ũ��
					intent = new Intent(this.getActivity(), ScrapActivity.class);
					startActivity(intent);
				}
				
			}
			break;
		case 3:
			if (lm.isLogin() && lm.getMemberType().equals("��ȸ��")) {
				// ��ũ��
				intent = new Intent(this.getActivity(), ScrapActivity.class);
				startActivity(intent);
			}
			break;
		}
	}
}
