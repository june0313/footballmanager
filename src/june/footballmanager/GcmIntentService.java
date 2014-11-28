package june.footballmanager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    
    /**
     * Tag used on log messages.
     */
    static final String TAG = "FM_GCM";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString(), -1, -1);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString(), -1, -1);
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				try {
					// 한글 깨짐 방지(UTF-8 인코딩, 디코딩)
					sendNotification(URLDecoder.decode(extras.getString("message"), "UTF-8"), Integer.parseInt(extras.getString("matchNo")), Integer.parseInt(extras.getString("type")));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
	// Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, int matchNo, int type) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        
        // AppliedTeamActivity를 실행하기 위해 매치 번호를 넘겨주어야 한다.
        Intent intent = null;
        if(type == 0)
        	intent = new Intent(getApplicationContext(), AppliedTeamActivity.class);
        else if(type == 1)
        	intent = new Intent(getApplicationContext(), MatchDetailActivity.class);
        
        intent.putExtra("matchNo", matchNo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        // 실행하려는 intent의 extra값이 반영되도록 FLAG_UPDATE_CURRENT 플래그를 설정한다.
        // 두번째 인자(request code)에 0을 넘기면 액티비티가 실행되지 않는 버그(android 4.3)
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
        .setTicker(msg)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle(getString(R.string.app_name))
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg)
        .setVibrate(new long[]{0, 1000})
        .setLights(Color.GREEN, 3000, 3000)
        .setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
