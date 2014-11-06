/*
 ��й�ȣ ã�� ȭ��
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
		
		// �׼ǹ� ����
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    // �� ���۷���
	    memberGroup = (RadioGroup)findViewById(R.id.rg_member_type);
	    tvEmail = (TextView)findViewById(R.id.tv_email);
	    btnSend = (Button)findViewById(R.id.btn_send);
	    btnSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if( memberGroup.getCheckedRadioButtonId() == -1 ) {
					Toast.makeText(FindPasswordActivity.this, "ȸ�� ������ �������ּ���.", 0).show();
				} else if(tvEmail.getText().toString().isEmpty())
					Toast.makeText(FindPasswordActivity.this, "�̸��� �ּҸ� �Է����ּ���", 0).show();
				else {
					// ȸ�� ������ �ĺ��Ͽ� memberType ������ �����Ѵ�.
					RadioButton btnMemberType = (RadioButton)findViewById(memberGroup.getCheckedRadioButtonId());
					int memberType;
					if(btnMemberType.getText().toString().equals("��ȸ��"))
						memberType = 0;
					else memberType = 1;
					
					// �ӽ� ��й�ȣ�� �����Ѵ�.
					String tempPasswd = createTempPasswd();
					
					// ������ �ӽ� ��й�ȣ�� ����� ������ ��й�ȣ�� �ʱ�ȭ �Ѵ�.
					setTempPasswd(memberType, tvEmail.getText().toString(), tempPasswd);
					
					// �ӽ� ��й�ȣ�� ���Ϸ� �߼��Ѵ�.
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
	
	// �ӽ� ��й�ȣ�� �����Ѵ�.
	private String createTempPasswd() {
		String elements = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		String tempPasswd = "";
		Random random = new Random();
		
		// ���� ��ġ�� �����Ͽ� 8�ڸ� ��й�ȣ�� �����Ѵ�.
		for( int i = 0; i < 8; i++ )
			tempPasswd += elements.charAt(random.nextInt(elements.length()));
		
		return tempPasswd;
	}
	
	// �Է��� ������ ��й�ȣ�� �ʱ�ȭ�Ѵ�.
	private void setTempPasswd(int memberType, String email, String tempPasswd) {
		// �Ķ���� ����
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
	
	// email�� �ӽ� ��й�ȣ�� �߼��Ѵ�.
	private void sendTempPasswd(String to, String tempPw) {
		// �߽�, ���� ����
        final String fromEmail = "our.town.fm.master";
        final String password = "dnflehdsp123";
        final String toEmail = to;
        final String tempPasswd = tempPw;
		
        // ������ �����忡�� ����
		new AsyncTask<Void, Void, Void>() {
			
			ProgressDialog pd;
			
			@Override
			protected Void doInBackground(Void... params) {

		        // ���� ����
		        String subject="�ӽ� ��й�ȣ �Դϴ�.";
		        String body="�ȳ��ϼ���.\n�츮���� ǲ�� �Ŵ��� �ӽ� ��й�ȣ �Դϴ�.\n\n"
		        		+ "- �ӽ� ��й�ȣ : " + tempPasswd + "\n\n"
		        		+ "���� �ӽ� ��й�ȣ�� �α��� �� ��й�ȣ�� �������ּ���.\n\n"
		        		+ "�����մϴ�.";
		         
		        Properties props = new Properties();
		        // SSL ����ϴ� ���
		        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
		        props.put("mail.smtp.socketFactory.port", "465"); //SSL Port
		        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
		        props.put("mail.smtp.auth", "true"); //Enabling SMTP Authentication
		        props.put("mail.smtp.port", "465"); //SMTP Port
		         
		        // ����
		        Authenticator auth = new Authenticator() {
		            protected PasswordAuthentication getPasswordAuthentication() {
		                return new PasswordAuthentication(fromEmail, password);
		            }
		        };
		        
		        // ���� ����
		        Session session = Session.getInstance(props, auth);
		         
		        // �޽��� ��� ����
		        MimeMessage msg = new MimeMessage(session);
		        try {
					msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
					msg.addHeader("format", "flowed");
			        msg.addHeader("Content-Transfer-Encoding", "8bit");
			         
			        msg.setFrom(new InternetAddress("no_reply@fm.co.kr", "�츮���� ǲ�� �Ŵ���"));
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
				// ���α׷��� ���̾�α� ���
				pd = new ProgressDialog(FindPasswordActivity.this);
				pd.setMessage("������ �߼��ϰ� �ֽ��ϴ�...");
				pd.show();
			}
			
			public void onPostExecute(Void params) {
				pd.dismiss();
				Toast.makeText(FindPasswordActivity.this, "������ �߼۵Ǿ����ϴ�", 0).show();
				finish();
			}
			
		}.execute();
	}
}


