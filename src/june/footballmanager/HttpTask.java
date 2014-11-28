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

import android.util.Log;

public class HttpTask {
	private String _url;			/* 데이터를 가져올 웹서버의 주소 */
	private String _parameter;		/* 서버에 넘길 파라미터 */ 
	private String _contents;		/* 웹페이지에서 읽어온 내용 */
	
	// Constructor
	HttpTask(String url, String parameter) {
		this._url = url;
		this._parameter = parameter;
	}
	
	// 웹페이지에서 읽어온 내용(문자열)을 리턴하는 메서드
	public String getContents() {
		Thread t = new Thread(new HttpConnectionThread());
		try {
			t.start();
			t.join();
		} catch (InterruptedException e) {
			Log.e("getContents", e.getMessage());
		}
		
		return _contents;
	}
	
	// 웹페이지에서 읽어온 내용을 읽어 JSONObject로 리턴하는 메서드
	public JSONObject getJSONObject() {
		JSONObject json = null;
		
		try {
			json = new JSONObject(getContents());
		} catch (JSONException e) {
			Log.e("getJSONObject", e.getMessage());
		}
		
		return json;
	}
	
	// Http 통신하는 쓰레드
	private class HttpConnectionThread implements Runnable {

		@Override
		public void run() {
			try {
				URL url = new URL(_url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
				_contents = "";
				BufferedReader in = new BufferedReader(new InputStreamReader(
						conn.getInputStream(), "euc-kr"));
				while ((buffer = in.readLine()) != null) {
					_contents += buffer;
				}
				in.close();

			} catch (MalformedURLException e) {
				Log.e("HttpConnectionThread", e.getMessage());
			} catch (IOException e) {
				Log.e("HttpConnectionThread", e.getMessage());
			} 
		}
	}
}
