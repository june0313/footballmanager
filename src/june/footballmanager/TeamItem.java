package june.footballmanager;

public class TeamItem {
	int _memberNo;
	String _teamName;
	String _ages;
	int _numOfPlayers;
	String _location;
	String _home;
	String _phone;
	String _msg;
	
	public TeamItem(int memberNo, String teamName, String ages, int numOfPlayers, String location, String home, String phone, String msg ) {
		this._memberNo = memberNo;
		this._teamName = teamName;
		this._ages = ages;
		this._numOfPlayers  = numOfPlayers;
		this._location = location;
		this._home = home;
		this._phone = phone;
		this._msg = msg;
	}
	
	public int getMemberNo() {
		return this._memberNo;
	}
	
	public String getTeamName() {
		return this._teamName;
	}
	
	public String getAges() {
		return this._ages;
	}
	
	public int getNumOfPlayers() {
		return this._numOfPlayers;
	}
	
	public String getLocation() {
		return this._location;
	}
	
	public String getHome() {
		return this._home;
	}
	
	public String getPhone() {
		return this._phone;
	}
	
	public String getMsg() {
		return this._msg;
	}
}

