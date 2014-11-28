package june.footballmanager;

public class PlayerItem {
	int _memberNo;
	String _nickname;
	String _position;
	int _age;
	String _location;
	
	public PlayerItem(int memberNo, String nickname, String position, int age, String location ) {
		this._memberNo = memberNo;
		this._nickname = nickname;
		this._position = position;
		this._age = age;
		this._location = location;
	}
	
	public int getMemberNo() {
		return this._memberNo;
	}
	
	public String getNickname() {
		return this._nickname;
	}
	
	public String getPosition() {
		return this._position;
	}
	
	public int getAge() {
		return this._age;
	}
	
	public String getLocation() {
		return this._location;
	}
}

