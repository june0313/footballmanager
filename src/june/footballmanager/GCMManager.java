package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMManager {
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String GCM_SERVER = "https://android.googleapis.com/gcm/send";
    public static final String API_KEY = "AIzaSyDvNq6z6XIC91bpl1WXtDSIfWkV_eRDaOU";
    // 프로젝트 넘버 = SENDER ID
    public static final String SENDER_ID = "408295761323";
    
    // 로그 메시지에 사용될 태그
    static final String TAG = "FM_GCM";
    
    // GCM에 사용될 객체
    static GoogleCloudMessaging gcm;
    static String regid;
    AtomicInteger msgId = new AtomicInteger();
    Activity activity;
    Context context;
    
    // 생성자
    GCMManager(Activity activity) {
    	this.activity = activity;
    	this.context = activity.getApplicationContext();
    }
    
	// 디바이스가 Play Service를 지원하는지 체크한다.
	// 지원할 경우 GCM 등록을 진행한다.
    public void checkAndRegister() {

    	if (checkPlayServices()) {
        	gcm = GoogleCloudMessaging.getInstance(context);
            regid = getRegistrationId();

            if (regid.isEmpty()) {
                registerInBackground();
            }
            Log.i(TAG, "regid : " + regid);
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }
    
    /**
	 * 디바이스가 Google Play Service APK를 가지고 있는지 확인한다.
	 * 만약 갖고 있지 않다면 Play Store에서 다운받을 수 있도록 하는 다이얼로그를
	 * 출력하거나, 디바이스의 시스템 설정에서 사용 가능하도록 바꾼다.
	 */
    public boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            activity.finish();
	        }
	        return false;
	    }
	    return true;
	}
    
    /**
	 * 현재 registration ID를 가져온다.
	 * registration ID가 없다면 새로 등록해야 한다.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId() {
	    final SharedPreferences prefs = getGCMPreferences();
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // 어플리케이션이 업데이트 되었는지 확인한다.
	    // 업데이트 된 경우, 현재의 regID가 유효하지 않을수도 있으므로,
	    // regID를 초기화 시켜주어야 한다.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion();
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
    
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences() {
		// regID가 저장된 프리퍼런스를 리턴한다.
	    return context.getSharedPreferences(MainActivity.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private int getAppVersion() {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * 어플리케이션을 GCM 서버에 비동기적으로 등록한다.
	 * registration ID와 app versionCode를 shared preferences에 저장한다.
	 */
	private void registerInBackground() {
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                // Persist the regID - no need to register again.
	                // 다음에 다시 등록할 필요가 없도록 registration ID를 저장한다.
	                storeRegistrationId(context, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	        }
	    }.execute();
	}
	
	/**
	 * app server로 registration ID를 전송한다.
	 */
	public void sendRegistrationIdToBackend() {
		new AsyncTask<Void, Void, Void>() {
			
			String jsonString = "";
			@Override
			protected Void doInBackground(Void... params) {
				LoginManager lm = new LoginManager(context);
				String memberType = lm.getMemberType();
				int memberNo = lm.getMemberNo();
				
				// 파라미터 구성
				String param = "memberType=" + memberType;
				param += "&memberNo=" + memberNo;
				param += "&regid=" + regid;
				Log.i(TAG, param);
				
				try {
					URL url = new URL(activity.getString(R.string.server) + activity.getString(R.string.register_gcm_id));
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					
					// 요청방식을 POST로 설정
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					
					// URL에 파리미터 넘기기
					OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "euc-kr");
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
					
					Log.i(TAG, "registration result : " + jsonString);
					
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;	
			}
			
			@Override
            protected void onPostExecute(Void params) {
                try {
					JSONObject jobj = new JSONObject(jsonString);
					if( jobj.getInt("success") == 1)
						Log.i(TAG, "registration id 저장(업데이트) 성공");
					else
						Log.i(TAG, "registration id 저장(업데이트) 성공");
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
		}.execute();
		
		
		Log.i(TAG, "sendRegistrationIdToBackend 호출됨");
	}
	
	/**
	 * {@code SharedPreferences}에 registration ID와 app versionCode를 저장한다.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences();
	    int appVersion = getAppVersion();
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
	
	// GCM 서버로 메시지를 전송한다.
	public void sendMessage(String to, String from) {
		final String receiverID = to;
		final String appliedTeam = from;
		
		new AsyncTask<Void, Void, String>() {
        	String jsonString = "";
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";

                try {
                	String param = "registration_id=" + receiverID;
                    param += "&data.message=" + URLEncoder.encode(appliedTeam + "팀이 매치를 신청하였습니다.", "UTF-8");
					Log.i(TAG, param);
					
					URL url = new URL(GCM_SERVER);
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Authorization", "key=" + API_KEY);
					//conn.setRequestProperty("Content-Type", "application/json");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					
					// URL에 파리미터 넘기기
					OutputStreamWriter out = new OutputStreamWriter(
							conn.getOutputStream(), "euc-kr");
					out.write(param);
					out.flush();
					out.close();
					
					int responseCode = conn.getResponseCode();
					Log.i(TAG, Integer.toString(responseCode));
					
					// URL 결과 가져오기
					
					String buffer = null;
					
					BufferedReader in = new BufferedReader(new InputStreamReader(
							conn.getInputStream(), "euc-kr"));
					while ((buffer = in.readLine()) != null) {
						jsonString += buffer;
					}
					in.close();
					
					Log.i(TAG, "send result : " + jsonString);
					
					
				} catch (MalformedURLException e) {
					msg = "MalFormed Error :" + e.getMessage();;
				} catch (IOException e) {
					msg = "IO Error :" + e.getMessage();
				}
                
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, msg);
            }
        }.execute();
	}
}
