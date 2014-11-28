
/*
 * 팀 구함 글쓰기 액티비티
 */

package june.footballmanager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
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
				addFindTeam();
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
	
	// 서버로 팀구함 게시물을 등록하는 메서드
	private void addFindTeam() {
		// 연결할 페이지의 URL
		String url = getString(R.string.server)
				+ getString(R.string.add_find_team);

		// 파라미터 구성
		String param = "memberNo=" + lm.getMemberNo() + "&title="
				+ title.getText().toString() + "&content="
				+ content.getText().toString().replace("\n", "__");
		
		// 서버 연결
		new HttpAsyncTask(url, param, this, "게시물을 등록하는 중 입니다..."){

			@Override
			protected void onPostExecute(String result) {
				JSONObject json = null;
				try {
					json = new JSONObject(result);
					if(json.getInt("success") == 1) {
						Toast.makeText(AddFindTeamActivity.this, "게시물이 등록되었습니다.", 0).show();
						finish();
					}
				} catch (JSONException e) {
					Log.e("getFindTeam", e.getMessage());
				}	
			}
			
		}.execute();
	}
}
