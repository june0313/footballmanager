package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class HttpTask {
	private String _url;			/* 데이터를 가져올 웹서버의 주소 */
	private String _parameter;		/* 서버에 넘길 파라미터 */ 
	
	// Constructor
	HttpTask(String url, String parameter) {
		this._url = url;
		this._parameter = parameter;
	}
	
	// 웹페이지에서 읽어온 내용(문자열)을 리턴한다.
	public String getContents() {
		String contents = null;
		
		try {
			contents = new HttpAsyncTask().execute().get();
		} catch (InterruptedException e) {
			Log.e("HttpTask", e.getMessage());
		} catch (ExecutionException e) {
			Log.e("HttpTask", e.getMessage());
		}
		
		return contents;
	}
	
	// 웹페이지에서 읽어온 내용을 읽어 JSONObject로 리턴한다.
	public JSONObject getJSONObject() {
		JSONObject json = null;
		
		try {
			json = new JSONObject(getContents());
		} catch (JSONException e) {
			Log.e("HttpTask", e.getMessage());
		}
		
		return json;
	}
	
	// Http 비동기 테스크
	class HttpAsyncTask extends AsyncTask<Void, Void, String> {
		
		@Override
		protected String doInBackground(Void... params) {
			
			try {
				URL url = new URL(_url);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);

				// URL에 파리미터 넘기기
				OutputStreamWriter out = new OutputStreamWriter(
						conn.getOutputStream(), "euc-kr");
				out.write(_parameter);
				out.flush();
				out.close();

				// URL 결과 가져오기
				String buffer = null;
				String jsonString = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					jsonString += buffer;
				}
				in.close();
				
				return jsonString;

			} catch (MalformedURLException e) {
				Log.e("HttpAsyncTask", e.getMessage());
			} catch (IOException e) {
				Log.e("HttpAsyncTask", e.getMessage());
			} 
			
			return null;
		}		
	}
}
