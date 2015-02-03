package june.footballmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class NetworkErrorDialog {
	
	public static AlertDialog create(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("접속 오류");
		builder.setMessage("서버에 연결할 수 없습니다. 인터넷 연결 상태를 확인하고 다시 시도해주세요.");
		builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 어플리케이션 종료
//				Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//			    homeIntent.addCategory( Intent.CATEGORY_HOME );
//			    homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
//			    context.startActivity(homeIntent); 
				
				System.exit(1);
			}
		});
		
		return builder.create();
	}

}
