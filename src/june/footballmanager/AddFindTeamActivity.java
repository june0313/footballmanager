
/*
 * �� ���� �۾��� ��Ƽ��Ƽ
 */

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

import june.footballmanager.AddFindPlayerActivity.AddFindPlayer;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddFindTeamActivity extends Activity implements OnClickListener {

	EditText title;
	EditText content;
	Button ok;
	
	// ���� �α��� ����
	LoginManager lm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_find_team);
		
		// �׼ǹ� ����
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setSubtitle("�۾���");
		
		// �� ���۷���
		title = (EditText)findViewById(R.id.title);
		content = (EditText)findViewById(R.id.content);
		ok = (Button)findViewById(R.id.btnOK);
		ok.setOnClickListener(this);
		
		// �α��� ���� ��ü ����
		lm = new LoginManager(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnOK:
			
			if(title.getText().toString().isEmpty())
				Toast.makeText(this, "�� ������ �Է����ּ���", 0).show();
			else if(content.getText().toString().isEmpty())
				Toast.makeText(this, "������ �Է����ּ���", 0).show();
			else
				// �Խù� ���
				new AddFindTeam().execute();
			break;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class AddFindTeam extends AsyncTask<Void, Void, Void> {
		
		ProgressDialog pd;
		String jsonString = "";
		
		@Override
		public void onPreExecute() {
			pd = new ProgressDialog(AddFindTeamActivity.this);
			pd.setMessage("��ø� ��ٷ� �ּ���...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			// �Ķ���� ����
			String param = "memberNo=" + lm.getMemberNo()
					+ "&title=" + title.getText().toString()
					+ "&content=" + content.getText().toString().replace("\n", "__");
			
			try {
				URL url = new URL(getString(R.string.server)
						+ getString(R.string.add_find_team));
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				OutputStreamWriter out = new OutputStreamWriter(
						conn.getOutputStream(), "euc-kr");
				out.write(param);
				out.flush();
				out.close();

				String buffer = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();
				
				Log.i("FM", "AddFindTeam result : " + jsonString);


			} catch (MalformedURLException e) {
				Log.e("FM", "AddFindTeam : " + e.getMessage());
			} catch (IOException e) {
				Log.e("FM", "AddFindTeam : " + e.getMessage());
			}

			return null;
		}
		
		@Override
		public void onPostExecute(Void params) {
			pd.dismiss();
			
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(jsonString);
				
				if(jsonObj.getInt("success") == 1) {
					Toast.makeText(getApplicationContext(), "�Խù��� ��ϵǾ����ϴ�.", 0).show();
					finish();
				}
			} catch (JSONException e) {
				Log.e("FM", "AddFindTeam : " + e.getMessage());
			}
		}
	}
}
