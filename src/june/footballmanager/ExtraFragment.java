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
		
		// 로그인 계정별 메뉴 항목(리스트) 생성
		int extraItems;
		lm = new LoginManager(getActivity());

		if (lm.isLogin() && lm.getMemberType().equals("선수회원"))
			extraItems = R.array.extra_items_on_player;
		else if (lm.isLogin() && lm.getMemberType().equals("팀회원"))
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
			// 계정관리 선택
			// 로그인 여부를 확인하여 적절한 액티비티 실행
			if( lm.isLogin() && lm.getMemberType().equals("선수회원") ) {
				intent = new Intent(this.getActivity(), PlayerAccountActivity.class);
			} else if(lm.isLogin() && lm.getMemberType().equals("팀회원") ) {
				intent = new Intent(this.getActivity(), TeamAccountActivity.class);
			} else {
				intent = new Intent(this.getActivity(), LoginActivity.class);
			}
			
			this.getActivity().startActivity(intent);
			break;
			
		case 1:
			if (lm.isLogin()) {
				if(lm.getMemberType().equals("팀회원")) {
					// 팀회원 : 내가 등록한 매치
					intent = new Intent(this.getActivity(), MyMatchActivity.class);
					getActivity().startActivity(intent);
				} else {
					// 선수회원 : 내가 쓴 글
					Toast.makeText(getActivity(), "내가 쓴 글", 0).show();
				}
			} else {
				// 비로그인 : 스크랩
				intent = new Intent(this.getActivity(), ScrapActivity.class);
				startActivity(intent);
			}
			
			break;
			
		case 2:
			if (lm.isLogin() ) {
				if(lm.getMemberType().equals("팀회원")) {
					// 내가 신청한 매치
					intent = new Intent(this.getActivity(),MyApplicationActivity.class);
					getActivity().startActivity(intent);
				} else {
					// 선수회원 : 스크랩
					intent = new Intent(this.getActivity(), ScrapActivity.class);
					startActivity(intent);
				}
				
			}
			break;
		case 3:
			if (lm.isLogin() && lm.getMemberType().equals("팀회원")) {
				// 스크랩
				intent = new Intent(this.getActivity(), ScrapActivity.class);
				startActivity(intent);
			}
			break;
		}
	}
}
