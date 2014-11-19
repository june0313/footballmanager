
/*
 * �� ���� �۾��� ��Ƽ��Ƽ
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
	
	// ������ ������ �Խù��� ����ϴ� �޼���
	private void addFindTeam() {
		// ������ �������� URL
		String url = getString(R.string.server)
				+ getString(R.string.add_find_team);

		// �Ķ���� ����
		String param = "memberNo=" + lm.getMemberNo() + "&title="
				+ title.getText().toString() + "&content="
				+ content.getText().toString().replace("\n", "__");
		
		// ���� ����
		JSONObject json = new HttpTask(url, param).getJSONObject();
		
		try {
			
			if(json.getInt("success") == 1) {
				Toast.makeText(getApplicationContext(), "�Խù��� ��ϵǾ����ϴ�.", 0).show();
				finish();
			}
		} catch (JSONException e) {
			Log.e("getFindTeam", e.getMessage());
		}	
	}
}
