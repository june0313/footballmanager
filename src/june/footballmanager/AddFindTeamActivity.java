
/*
 * 팀 구함 글쓰기 액티비티
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
	
	// 현재 로그인 정보
	LoginManager lm;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_find_team);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setSubtitle("글쓰기");
		
		// 뷰 레퍼런싱
		title = (EditText)findViewById(R.id.title);
		content = (EditText)findViewById(R.id.content);
		ok = (Button)findViewById(R.id.btnOK);
		ok.setOnClickListener(this);
		
		// 로그인 정보 객체 생성
		lm = new LoginManager(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnOK:
			
			if(title.getText().toString().isEmpty())
				Toast.makeText(this, "글 제목을 입력해주세요", 0).show();
			else if(content.getText().toString().isEmpty())
				Toast.makeText(this, "내용을 입력해주세요", 0).show();
			else
				// 게시물 등록
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
			pd.setMessage("잠시만 기다려 주세요...");
			pd.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			
			// 파라미터 구성
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
					Toast.makeText(getApplicationContext(), "게시물이 등록되었습니다.", 0).show();
					finish();
				}
			} catch (JSONException e) {
				Log.e("FM", "AddFindTeam : " + e.getMessage());
			}
		}
	}
}
