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
    // ������Ʈ �ѹ� = SENDER ID
    public static final String SENDER_ID = "408295761323";
    
    // �α� �޽����� ���� �±�
    static final String TAG = "FM_GCM";
    
    // GCM�� ���� ��ü
    static GoogleCloudMessaging gcm;
    static String regid;
    AtomicInteger msgId = new AtomicInteger();
    Activity activity;
    Context context;
    
    // ������
    GCMManager(Activity activity) {
    	this.activity = activity;
    	this.context = activity.getApplicationContext();
    }
    
	// ����̽��� Play Service�� �����ϴ��� üũ�Ѵ�.
	// ������ ��� GCM ����� �����Ѵ�.
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
	 * ����̽��� Google Play Service APK�� ������ �ִ��� Ȯ���Ѵ�.
	 * ���� ���� ���� �ʴٸ� Play Store���� �ٿ���� �� �ֵ��� �ϴ� ���̾�α׸�
	 * ����ϰų�, ����̽��� �ý��� �������� ��� �����ϵ��� �ٲ۴�.
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
	 * ���� registration ID�� �����´�.
	 * registration ID�� ���ٸ� ���� ����ؾ� �Ѵ�.
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
	    // ���ø����̼��� ������Ʈ �Ǿ����� Ȯ���Ѵ�.
	    // ������Ʈ �� ���, ������ regID�� ��ȿ���� �������� �����Ƿ�,
	    // regID�� �ʱ�ȭ �����־�� �Ѵ�.
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
		// regID�� ����� �����۷����� �����Ѵ�.
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
	 * ���ø����̼��� GCM ������ �񵿱������� ����Ѵ�.
	 * registration ID�� app versionCode�� shared preferences�� �����Ѵ�.
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
	                // ������ �ٽ� ����� �ʿ䰡 ������ registration ID�� �����Ѵ�.
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
	 * app server�� registration ID�� �����Ѵ�.
	 */
	public void sendRegistrationIdToBackend() {
		new AsyncTask<Void, Void, Void>() {
			
			String jsonString = "";
			@Override
			protected Void doInBackground(Void... params) {
				LoginManager lm = new LoginManager(context);
				String memberType = lm.getMemberType();
				int memberNo = lm.getMemberNo();
				
				// �Ķ���� ����
				String param = "memberType=" + memberType;
				param += "&memberNo=" + memberNo;
				param += "&regid=" + regid;
				Log.i(TAG, param);
				
				try {
					URL url = new URL(activity.getString(R.string.server) + activity.getString(R.string.register_gcm_id));
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					
					// ��û����� POST�� ����
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					
					// URL�� �ĸ����� �ѱ��
					OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "euc-kr");
					out.write(param);
					out.flush();
					out.close();
					
					// URL ��� ��������
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
						Log.i(TAG, "registration id ����(������Ʈ) ����");
					else
						Log.i(TAG, "registration id ����(������Ʈ) ����");
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
		}.execute();
		
		
		Log.i(TAG, "sendRegistrationIdToBackend ȣ���");
	}
	
	/**
	 * {@code SharedPreferences}�� registration ID�� app versionCode�� �����Ѵ�.
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
	
	// GCM ������ �޽����� �����Ѵ�.
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
                    param += "&data.message=" + URLEncoder.encode(appliedTeam + "���� ��ġ�� ��û�Ͽ����ϴ�.", "UTF-8");
					Log.i(TAG, param);
					
					URL url = new URL(GCM_SERVER);
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod("POST");
					conn.setRequestProperty("Authorization", "key=" + API_KEY);
					//conn.setRequestProperty("Content-Type", "application/json");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					
					// URL�� �ĸ����� �ѱ��
					OutputStreamWriter out = new OutputStreamWriter(
							conn.getOutputStream(), "euc-kr");
					out.write(param);
					out.flush();
					out.close();
					
					int responseCode = conn.getResponseCode();
					Log.i(TAG, Integer.toString(responseCode));
					
					// URL ��� ��������
					
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
