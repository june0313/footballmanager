/*
 비밀번호 찾기 화면
 */

package june.footballmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class FindPasswordActivity extends Activity {
	
	RadioGroup memberGroup;
	TextView tvEmail;
	Button btnSend;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_password);
		
		// 액션바 설정
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    // 뷰 레퍼런스
	    memberGroup = (RadioGroup)findViewById(R.id.rg_member_type);
	    tvEmail = (TextView)findViewById(R.id.tv_email);
	    btnSend = (Button)findViewById(R.id.btn_send);
	    btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if( memberGroup.getCheckedRadioButtonId() == -1 ) {
					Toast.makeText(FindPasswordActivity.this, "회원 유형을 선택해주세요.", 0).show();
				} else if(tvEmail.getText().toString().isEmpty())
					Toast.makeText(FindPasswordActivity.this, "이메일 주소를 입력해주세요", 0).show();
				else {
					// 회원 유형을 식별하여 memberType 변수에 저장한다.
					RadioButton btnMemberType = (RadioButton)findViewById(memberGroup.getCheckedRadioButtonId());
					int memberType;
					if(btnMemberType.getText().toString().equals("팀회원"))
						memberType = 0;
					else memberType = 1;
					
					// 임시 비밀번호를 생성한다.
					String tempPasswd = createTempPasswd();
					
					// 생성된 임시 비밀번호로 사용자 계정의 비밀번호를 초기화 한다.
					setTempPasswd(memberType, tvEmail.getText().toString(), tempPasswd);
					
					// 임시 비밀번호를 메일로 발송한다.
					sendTempPasswd(tvEmail.getText().toString(), tempPasswd);
				}
					
			}
			
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.find_password, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		switch( item.getItemId()) {
		case android.R.id.home :
			finish();
			return true;
		}
		
		return false;
	}
	
	// 임시 비밀번호를 생성한다.
	private String createTempPasswd() {
		String elements = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		String tempPasswd = "";
		Random random = new Random();
		
		// 임의 위치를 선택하여 8자리 비밀번호를 생성한다.
		for( int i = 0; i < 8; i++ )
			tempPasswd += elements.charAt(random.nextInt(elements.length()));
		
		return tempPasswd;
	}
	
	// 입력한 계정의 비밀번호를 초기화한다.
	private void setTempPasswd(int memberType, String email, String tempPasswd) {
		// 파라미터 생성
		final String param = "memberType=" + memberType + "&email=" + email + "&passwd=" + tempPasswd;
		Log.i("param", param);
		
		new AsyncTask<Void, Void, Void>() {
			String jsonString = "";
			
			@Override
			protected Void doInBackground(Void... params) {
				
				try {
					
					URL url = new URL(getString(R.string.server)+ getString(R.string.update_password));
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("POST");
					conn.setDoInput(true);
					conn.setDoOutput(true);
					
					OutputStreamWriter out = new OutputStreamWriter( conn.getOutputStream(), "euc-kr" );
					out.write(param);
					out.flush();
					out.close();
					
					String buffer = null;
					BufferedReader in = new BufferedReader( new InputStreamReader( conn.getInputStream(), "euc-kr" ));
					while( (buffer = in.readLine()) != null ) {
						jsonString += buffer;
					}
					in.close();
					
					Log.i("FM", "update passwd result : " + jsonString);
				} catch(MalformedURLException e) {
					Log.i("FindPasswordActivity", e.getMessage());
				} catch (IOException e) {
					Log.i("FindPasswordActivity", e.getMessage());
				}
				
				return null;
			}
			
		}.execute();
	}
	
	// email로 임시 비밀번호를 발송한다.
	private void sendTempPasswd(String to, String tempPw) {
		// 발신, 수신 정보
        final String fromEmail = "our.town.fm.master";
        final String password = "dnflehdsp123";
        final String toEmail = to;
        final String tempPasswd = tempPw;
		
        // 별도의 쓰레드에서 진행
		new AsyncTask<Void, Void, Void>() {
			
			ProgressDialog pd;
			
			@Override
			protected Void doInBackground(Void... params) {

		        // 메일 내용
		        String subject="임시 비밀번호 입니다.";
		        String body="안녕하세요.\n우리동네 풋볼 매니저 임시 비밀번호 입니다.\n\n"
		        		+ "- 임시 비밀번호 : " + tempPasswd + "\n\n"
		        		+ "위의 임시 비밀번호로 로그인 후 비밀번호를 변경해주세요.\n\n"
		        		+ "감사합니다.";
		         
		        Properties props = new Properties();
		        // SSL 사용하는 경우
		        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
		        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
		        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
		        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
		        props.put("mail.smtp.port", "465"); //SMTP Port
		         
		        // 인증
		        Authenticator auth = new Authenticator() {
		            protected PasswordAuthentication getPasswordAuthentication() {
		                return new PasswordAuthentication(fromEmail, password);
		            }
		        };
		        
		        // 메일 세션
		        Session session = Session.getInstance(props, auth);
		         
		        // 메시지 헤더 설정
		        MimeMessage msg = new MimeMessage(session);
		        try {
					msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
					msg.addHeader("format", "flowed");
			        msg.addHeader("Content-Transfer-Encoding", "8bit");
			         
			        msg.setFrom(new InternetAddress("no_reply@fm.co.kr", "우리동네 풋볼 매니저"));
			        msg.setReplyTo(InternetAddress.parse("no_reply@fm.co.kr", false));
			 
			        msg.setSubject(subject, "UTF-8");
			        msg.setText(body, "UTF-8");
			        msg.setSentDate(new Date());
			 
			        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
			        Transport.send(msg);
				} catch (MessagingException e) {
					Log.i("FindPasswordActivity", e.getMessage());
				} catch (UnsupportedEncodingException e) {
					Log.i("FindPasswordActivity", e.getMessage());
				}
				
				return null;
			}
			
			public void onPreExecute() {
				// 프로그레스 다이얼로그 출력
				pd = new ProgressDialog(FindPasswordActivity.this);
				pd.setMessage("메일을 발송하고 있습니다...");
				pd.show();
			}
			
			public void onPostExecute(Void params) {
				pd.dismiss();
				Toast.makeText(FindPasswordActivity.this, "메일이 발송되었습니다", 0).show();
				finish();
			}
			
		}.execute();
	}
}


