package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

// ����� ����(�˻�) ȭ��
public class SelectGroundActivity extends Activity implements OnClickListener{
	EditText searchBox;
	
	TextView tvTemp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_ground);
		
		// �׼ǹ� ����(�ڷΰ��� Ȱ��ȭ)
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
		// �˻���ư
		ImageButton btnSearch = (ImageButton)findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(this);
		
		searchBox = (EditText)findViewById(R.id.searchBox);
		tvTemp = (TextView)findViewById(R.id.textView1);
	}

	// Ŭ�� �̺�Ʈ
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.btn_search) {
			
			new SearchGround().execute();
		}
	}
	
	public class SearchGround extends AsyncTask<Void, Void, Boolean> {
		
		String ground;
		String xmlString = "";
		
		
		 
		
		@Override
		public void onPreExecute() {
			ground = searchBox.getText().toString();
		}

		@Override
		protected Boolean doInBackground(Void... arg0) {
			try {
				// url
				String charset = "UTF-8";
				String url = "http://openapi.naver.com/search";
				
				// parameters
				String paramKey = "de4321ad1f63477e6497d83e08ce1791";
				String paramTarget = "local";
				String paramQuery = searchBox.getText().toString();
				
				// �Ķ���� ����
				String parameters = String.format("key=%s&target=%s&query=%s", 
						URLEncoder.encode( paramKey, charset ), 
						URLEncoder.encode( paramTarget, charset ), 
						URLEncoder.encode( paramQuery, charset ) );
			
				//URL url = new URL(url + "?" + query);
				HttpURLConnection conn = (HttpURLConnection) new URL( url + "?" + parameters ).openConnection();
				conn.setRequestMethod("GET");
				
				if( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
					String buffer = null;
					BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream(), "UTF-8"));
					while( (buffer = in.readLine()) != null ) {
						xmlString += buffer;
					}
					in.close();
					
					Log.i("FM", xmlString);
					
					// xml parsing
					XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
					factory.setNamespaceAware(true);
					
					XmlPullParser xpp = factory.newPullParser();
					xpp.setInput( in );
					
					int eventType = xpp.getEventType();
					
					// xml�� ������ ���鼭 ���ϴ� �����͸� ����
					while( eventType != XmlPullParser.END_DOCUMENT ) {
						
					}

				}
				
				
				
	
			} catch (MalformedURLException e) {


			} catch (IOException e) {

			} catch (XmlPullParserException e) {

				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		public void onPostExecute( Boolean isSuccess ) {
			// �ӽ� �ؽ�Ʈ ���
			tvTemp.setText( xmlString );
		}
		
	}
}
