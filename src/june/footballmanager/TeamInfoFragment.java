package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass. Activities that
 * contain this fragment must implement the
 * {@link TeamInfoFragment.OnFragmentInteractionListener} interface to handle
 * interaction events.
 * 
 */
public class TeamInfoFragment extends Fragment {
	
	int memberNo;
	
	TextView tvLocation;
	TextView tvHome;
	TextView tvAges;
	TextView tvNumOfPlayers;
	TextView tvIntroduce;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_team_info, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// 팀 번호 가져오기
		Bundle extra = getArguments();
		memberNo = extra.getInt("memberNo");
		Log.i("TeamInfoFragment - memberNo : ", memberNo + "");
		
		// 뷰 레퍼런싱
		tvLocation = (TextView)getView().findViewById(R.id.location);
		tvHome = (TextView)getView().findViewById(R.id.home);
		tvAges = (TextView)getView().findViewById(R.id.ages);
		tvNumOfPlayers = (TextView)getView().findViewById(R.id.num_of_players);
		tvIntroduce = (TextView)getView().findViewById(R.id.introduce);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		new GetTeamInfo().execute();
	}
	
	private class GetTeamInfo extends AsyncTask<Void, Void, Void> {
		String param = "";
		
		// URL로부터 가져온 json 형식의 string
		String jsonString = "";
		
		ProgressDialog pd;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
			param += "memberNo=" + memberNo;
			Log.i("param", param);
			
			// 프로그레스 다이얼로그 출력
			pd = new ProgressDialog(getActivity());
			pd.setMessage("팀 정보를 불러오는 중입니다...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.team_info));
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

				Log.i("FM", "GetTeamInfo result : " + jsonString);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				tvLocation.setText(jsonObj.getString("LOCATION"));
				tvHome.setText(jsonObj.getString("HOME"));
				tvAges.setText(jsonObj.getString("AGES"));
				tvNumOfPlayers.setText(jsonObj.getString("NUM_OF_PLAYERS") + "명");
				tvIntroduce.setText(jsonObj.getString("INTRODUCE").replace("__", "\n"));
				
			} catch(JSONException e) {
				
			} finally {
				pd.dismiss();
			}
		}
	}
}
