package june.footballmanager;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginManager {
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	
	// reference LoginInfo preference
	public LoginManager(Context context) {
		pref = context.getSharedPreferences("LoginInfo", Context.MODE_PRIVATE);
		editor = pref.edit();
	}
	
	// return login state
	public boolean isLogin() {
		return pref.getBoolean("isLogin", false);
	}
	
	// get account imfo
	public String getEmail() {
		return pref.getString("email", null);
	}
	
	public String getPassword() {
		return pref.getString("password", null);
	}
	
	public String getMemberType() {
		return pref.getString("memberType", null);
	}
	
	// get member number
	public int getMemberNo() {
		return pref.getInt("memberNo", -1);
	}
	
	// get team info
	public String getTeamName() {
		return pref.getString("teamName", null);
	}
	
	public String getPhone() {
		return pref.getString("phone", null);
	}
	
	public String getLocation() {
		return pref.getString("location", null);
	}
	
	public String getHome() {
		return pref.getString("home", null);
	}
	
	public int getNumOfPlayer() {
		return pref.getInt("numOfPlayer", 0);
	}
	
	public String getAges() {
		return pref.getString("ages", null);
	}

	// get palyer info
	public String getPosition() {
		return pref.getString("position", null);
	}
	
	public int getAge() {
		return pref.getInt("age", 0);
	}
	
	public String getNickname() {
		return pref.getString("nickname", null);
	}
	
	// 로그인한 계정의 정보를 저장한다.
	public void setLoginInfo( String memberType, String email, String password ) {
		editor.putBoolean("isLogin", true);
		editor.putString("memberType", memberType);
		editor.putString("email", email);
		editor.putString("password", password);
		editor.commit();
	}
	
	// 로그인한 회원의 번호를 저장한다.
	public void setMemberNo( int memberNo ) {
		editor.putInt( "memberNo",  memberNo );
		editor.commit();
	}
	
	// 로그인한 팀계정의 정보를 저장한다.
	public void setTeamInfo( String teamName, String location, String home, int numOfPlayer, String ages, String phone) {
		editor.putString("teamName", teamName);
		editor.putString("location", location);
		editor.putString("home", home);
		editor.putInt("numOfPlayer", numOfPlayer);
		editor.putString("ages", ages);
		editor.putString("phone", phone);
		editor.commit();
	}
	
	// 로그인한 선수계정의 정보를 저장한다.
	public void setPlayerInfo( String position, int age, String nickname, String phone, String location) {
		editor.putString("position", position);
		editor.putInt("age", age);
		editor.putString("nickname", nickname);
		editor.putString("phone", phone);
		editor.putString("location", location);
		editor.commit();
	}
	
	// 로그인한 계정 정보를 삭제한다.
	public void removeLoginInfo() {
		editor.clear();
		editor.commit();
	}
}
