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

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * ���� ����� ��ġ�� ��û�� �� ����Ʈ�� ����ϴ� ��Ƽ��Ƽ
 * 
 */
public class AppliedTeamActivity extends Activity {
	int matchNo;
	int memberNo;
	
	// ��û�� �� ����Ʈ
	ArrayList<TeamItem> appliedTeamList;
	
	// ����Ʈ �����
	AppliedTeamListAdapter atlAdapter;
	
	// ��û�� �� ����Ʈ��
	ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_applied_team);
		
		// �׼ǹ� ����
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		
		// ��ġ ��ȣ ��������
		Intent intent = getIntent();
		matchNo = intent.getIntExtra("matchNo", -1);
		
		// ����Ʈ ��ü �ʱ�ȭ
		appliedTeamList = new ArrayList<TeamItem>();
		
		// ����� ����
		atlAdapter = new AppliedTeamListAdapter( this, appliedTeamList );
		
		// ����Ʈ�� ���� �� ����
		list = (ListView) findViewById(R.id.list);
		list.addHeaderView(new View(this), null, true);
	    list.addFooterView(new View(this), null, true);
		list.setAdapter(atlAdapter);
		// list.setOnItemClickListener(this);
		
		// DB�κ��� ��û�� �� ���� ��������
		new GetAppliedTeam().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.applied_team, menu);
		return true;
	}

	// �޴� ���ý� �ݹ� �޼���
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return false;
	}
	
	private class AppliedTeamListAdapter extends BaseAdapter {
		
		private Context context;
		private ArrayList<TeamItem> list;
		private LayoutInflater inflater;
		
		public AppliedTeamListAdapter( Context c, ArrayList<TeamItem> list ) {
			this.context = c;
			this.list = list;
			this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public TeamItem getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final int pos = position;
			
			if( convertView == null ) {
				convertView = inflater.inflate(R.layout.applied_team_item, parent, false);
			}
			
			TextView teamName = (TextView) convertView.findViewById(R.id.team_name);
			teamName.setText(getItem(position).getTeamName());
			
			TextView ages = (TextView) convertView.findViewById(R.id.ages);
			ages.setText(getItem(position).getAges());
			
			TextView numOfPlayers = (TextView) convertView.findViewById(R.id.num_of_players);
			numOfPlayers.setText(getItem(position).getNumOfPlayers() + "��");
			
			TextView location = (TextView) convertView.findViewById(R.id.location);
			location.setText(list.get(position).getLocation());
			
			TextView applyMsg = (TextView) convertView.findViewById(R.id.apply_msg);
			if(getItem(position).getMsg().length() != 0)
				applyMsg.setText(getItem(position).getMsg());
			
			TextView teamInfo = (TextView) convertView.findViewById(R.id.team_info);
			teamInfo.setOnClickListener(new OnClickListener() {
				// "�� ����" ��ư Ŭ����
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(AppliedTeamActivity.this, TeamInfoActivity.class);
					intent.putExtra("memberNo", getItem(pos).getMemberNo());
					intent.putExtra("teamName", getItem(pos).getTeamName());
					startActivity(intent);
				}
			});
			
			TextView btnAccept = (TextView) convertView.findViewById(R.id.accept);
			btnAccept.setOnClickListener(new OnClickListener() {

				// "����" ��ư Ŭ����
				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(AppliedTeamActivity.this);
					builder.setMessage(getItem(pos).getTeamName() + "�� ��û�� �����Ͻðڽ��ϱ�?")
					.setPositiveButton("��", new DialogInterface.OnClickListener() {
						// "��" ��ư Ŭ����
						@Override
						public void onClick(DialogInterface dialog, int which) {
							memberNo = getItem(pos).getMemberNo();
							
							new AcceptOpposingTeam().execute();
						}
					})
					.setNegativeButton("�ƴϿ�", null);
					
					builder.create().show();
				}
			});
			
			return convertView;
		}
		
	}
	
	private class GetAppliedTeam extends AsyncTask<Void, Void, Void> {
		
		// ������ ������ �Ķ����(��ġ ��ȣ)
		String param = "";
		
		// URL�κ��� ������ json ������ string
		String jsonString = "";
		
		ProgressDialog pd;
		
		@Override
		public void onPreExecute() {
			param = "matchNo=" + matchNo;
			Log.i("param", param);
			
			// ���α׷��� ���̾�α� ���
			pd = new ProgressDialog(AppliedTeamActivity.this);
			pd.setMessage("�� ����Ʈ�� �������� ���Դϴ�...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			try {
				URL url = new URL(getString(R.string.server) + getString(R.string.applied_team));
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
				
				Log.i( "AppliedTeam", jsonString );
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		public void onPostExecute(Void arg) {
			
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				JSONArray jsonArr = jsonObj.getJSONArray("list");
				
				JSONObject item;
				appliedTeamList.clear();
				
				for( int i = 0; i < jsonArr.length(); i++ ) {
					item = jsonArr.getJSONObject(i);
					appliedTeamList.add( new TeamItem(
							item.getInt("MEMBER_NO"),
							item.getString("TEAM_NAME"),
							item.getString("AGES"),
							item.getInt("NUM_OF_PLAYERS"),
							item.getString("LOCATION"),
							null,
							item.getString("PHONE"),
							item.getString("MSG")
							) 
					);
				}
				
			} catch (JSONException e) {
				
				appliedTeamList.clear();
				e.printStackTrace();
			} finally {
				atlAdapter.notifyDataSetChanged();
				pd.dismiss();
			}
			
		}
	}
	
	// ��ġ ����
	private class AcceptOpposingTeam extends AsyncTask<Void, Void, Void> {
		// �ĸ����� ����(��ġ��ȣ + ����ȣ)
		String param = "";
		
		// ����� ��� ���� ���ڿ�
		String jsonString = "";
		
		ProgressDialog pd;
		
		@Override
		public void onPreExecute() {
			param += "matchNo=" + matchNo;
			param +="&memberNo=" + memberNo; 
			Log.i("param", param);
			
			pd = new ProgressDialog(AppliedTeamActivity.this);
			pd.setMessage("������� ��û�� �����ϴ� ���Դϴ�...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				URL url = new URL(getString(R.string.server) + getString(R.string.accept_opposing_team));
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
				
				Log.i( "AcceptOpposingTeam", jsonString );
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		public void onPostExecute(Void arg) {
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				if( jsonObj.getInt("success") == 1 ) {
					Toast.makeText(AppliedTeamActivity.this, "��ġ�� ����Ǿ����ϴ�!", 0).show();
					finish();
				}

				else
					Toast.makeText(AppliedTeamActivity.this, "fail!", 0).show();
					
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				pd.dismiss();
			}
			
		}
	}
}
