package june.footballmanager;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "address.db";
	
	// ���������� �����ϴ� ���̺�
	private static final String TABLE_LOCATION = "location";
	private static final String KEY_SIDO = "SIDO";
	private static final String KEY_GUGUN = "GUGUN";
	private static final String KEY_DONG = "DONG";
	
	// ��ġ ��ũ�� ������ �����ϴ� ���̺�
	private static final String TABLE_SCRAP_MATCH = "scrap_match";
	private static final String KEY_MATCHNO = "match_no";
	private static final String CREATE_TABLE_SCRAP_MATCH = "CREATE TABLE "
			+ TABLE_SCRAP_MATCH + "(" + KEY_MATCHNO + " INTEGER PRIMARY KEY"
			+ ");";

	// �������� ��ũ�� ������ �����ϴ� ���̺�
	private static final String TABLE_SCRAP_FIND_PLAYER = "scrap_find_player";
	private static final String KEY_NO = "no";
	private static final String CREATE_TABLE_SCRAP_FIND_PLAYER = "CREATE TABLE "
			+ TABLE_SCRAP_FIND_PLAYER
			+ "("
			+ KEY_NO
			+ " INTEGER PRIMARY KEY"
			+ ");";

	// ������ ��ũ�� ������ �����ϴ� ���̺�
	private static final String TABLE_SCRAP_FIND_TEAM = "scrap_find_team";
	private static final String CREATE_TABLE_SCRAP_FIND_TEAM = "CREATE TABLE "
			+ TABLE_SCRAP_FIND_TEAM
			+ "("
			+ KEY_NO
			+ " INTEGER PRIMARY KEY"
			+ ");";
	
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/*
	 * DB ���� ���� ������ �� �ѹ��� ȣ��ȴ�.
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// ��ġ ��ũ�� ���̺� ����
		db.execSQL(CREATE_TABLE_SCRAP_MATCH);
		Log.i("SQLite", "Table is created.");
		
		// �������� ��ũ�� ���̺� ����
		db.execSQL(CREATE_TABLE_SCRAP_FIND_PLAYER);
		Log.i("SQLite", "Table is created.");
		
		// ������ ��ũ�� ���̺� ����
		db.execSQL(CREATE_TABLE_SCRAP_FIND_TEAM);
		Log.i("SQLite", "Table is created.");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	// ��ũ���� ��� ��ġ�� CSV ������ ���ڿ��� ����
	public String getAllScrapMatch() {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + KEY_MATCHNO + " FROM " + TABLE_SCRAP_MATCH;
		Cursor c = db.rawQuery(query, null);
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		if(c.moveToFirst()) {
			do {
				list.add(c.getInt(0));
			} while(c.moveToNext());
		}
		String result = "";
		
		for(int i = 0; i < list.size(); i++ ) {
			if(i == 0)
				result += list.get(i);
			else
				result += ", " + list.get(i);
		}
		
		c.close();
		db.close();
		return result;
	}
	
	// ��ũ���� ��ġ�� DB�� �����ϴ� �Լ�
	public void insertScrapMatch(int matchNo) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_MATCHNO, matchNo);
		long result = db.insert(TABLE_SCRAP_MATCH, null, values);
		Log.i("SQLite", "Insert result(long) : " + result);
		
		db.close();
	}
	
	// ��ġ�� ��ũ�� �Ǿ����� ��ȸ�ϴ� �Լ�
	// ��ġ ��ȣ�� ��ȸ �� ���� ������ �����Ѵ�.
	public boolean selectScrapMatch(int matchNo) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + KEY_MATCHNO + " FROM " + TABLE_SCRAP_MATCH + " WHERE " + KEY_MATCHNO + " = " + matchNo;
		Log.i("SQLite", query);
		
		Cursor c = db.rawQuery(query, null);
		int count = c.getCount();
		c.close();
		db.close();
		
		if(count == 1)
			return true;
		else
			return false;
	}
	
	// ��ũ���� ��ġ�� �����ϴ� �Լ�
	public void deleteScrapMatch(int matchNo) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SCRAP_MATCH, KEY_MATCHNO + " = ?", new String[] { String.valueOf(matchNo) });
		db.close();
	}
	
	// ��ũ���� ��� ���������� CSV ������ ���ڿ��� ����
	public String getAllScrapFindPlayer() {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + KEY_NO + " FROM " + TABLE_SCRAP_FIND_PLAYER;
		Cursor c = db.rawQuery(query, null);
		ArrayList<Integer> list = new ArrayList<Integer>();

		if (c.moveToFirst()) {
			do {
				list.add(c.getInt(0));
			} while (c.moveToNext());
		}
		String result = "";

		for (int i = 0; i < list.size(); i++) {
			if (i == 0)
				result += list.get(i);
			else
				result += ", " + list.get(i);
		}

		c.close();
		db.close();
		return result;
	}
	
	// ��ũ���� ���������� DB�� �����ϴ� �Լ�
	public void insertScrapFindPlayer(int no) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NO, no);
		long result = db.insert(TABLE_SCRAP_FIND_PLAYER, null, values);
		Log.i("SQLite", "Insert result(long) : " + result);

		db.close();
	}

	// ���������� ��ũ�� �Ǿ����� ��ȸ�ϴ� �Լ�
	// �Խù� ��ȣ�� ��ȸ �� ���� ������ �����Ѵ�.
	public boolean selectScrapFindPlayer(int no) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + KEY_NO + " FROM " + TABLE_SCRAP_FIND_PLAYER
				+ " WHERE " + KEY_NO + " = " + no;
		Log.i("SQLite", query);

		Cursor c = db.rawQuery(query, null);
		int count = c.getCount();
		c.close();
		db.close();

		if (count == 1)
			return true;
		else
			return false;
	}
	
	// ��ũ���� ���������� �����ϴ� �Լ�
	public void deleteScrapFindPlayer(int no) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SCRAP_FIND_PLAYER, KEY_NO + " = ?",
				new String[] { String.valueOf(no) });
		db.close();
	}

	// ��ũ���� ��� �������� CSV ������ ���ڿ��� ����
	public String getAllScrapFindTeam() {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + KEY_NO + " FROM " + TABLE_SCRAP_FIND_TEAM;
		Cursor c = db.rawQuery(query, null);
		ArrayList<Integer> list = new ArrayList<Integer>();

		if (c.moveToFirst()) {
			do {
				list.add(c.getInt(0));
			} while (c.moveToNext());
		}
		String result = "";

		for (int i = 0; i < list.size(); i++) {
			if (i == 0)
				result += list.get(i);
			else
				result += ", " + list.get(i);
		}

		c.close();
		db.close();
		return result;
	}
	
	// ��ũ���� �������� DB�� �����ϴ� �Լ�
	public void insertScrapFindTeam(int no) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_NO, no);
		long result = db.insert(TABLE_SCRAP_FIND_TEAM, null, values);
		Log.i("SQLite", "Insert result(long) : " + result);

		db.close();
	}
	
	// �������� ��ũ�� �Ǿ����� ��ȸ�ϴ� �Լ�
	// �Խù� ��ȣ�� ��ȸ �� ���� ������ �����Ѵ�.
	public boolean selectScrapFindTeam(int no) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT " + KEY_NO + " FROM " + TABLE_SCRAP_FIND_TEAM
				+ " WHERE " + KEY_NO + " = " + no;
		Log.i("SQLite", query);

		Cursor c = db.rawQuery(query, null);
		int count = c.getCount();
		c.close();
		db.close();

		if (count == 1)
			return true;
		else
			return false;
	}
	
	// ��ũ���� �������� �����ϴ� �Լ�
	public void deleteScrapFindTeam(int no) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SCRAP_FIND_TEAM, KEY_NO + " = ?",
				new String[] { String.valueOf(no) });
		db.close();
	}
	
	// ������ ��/�� ����Ʈ�� �����Ѵ�.
	public List<String> getSIDOList() {
		List<String> SIDOList = new ArrayList<String>();
		
		String selectQuery = "SELECT DISTINCT " + KEY_SIDO + " FROM " + TABLE_LOCATION;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if(cursor.moveToFirst()) {
			do{
				SIDOList.add(cursor.getString(0));
			} while( cursor.moveToNext());
		}
		
		cursor.close();
		db.close();
		
		return SIDOList;
	}
	
	public List<String> getGUGUNList(String sido) {
		List<String> GUGUNList = new ArrayList<String>();
		
		String selectQuery = "SELECT DISTINCT " + KEY_GUGUN + " FROM " + TABLE_LOCATION 
				+ " WHERE " + KEY_SIDO + " = '" + sido + "'";
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if(cursor.moveToFirst()) {
			do{
				GUGUNList.add(cursor.getString(0));
			} while( cursor.moveToNext());
		}
		
		cursor.close();
		db.close();
		
		return GUGUNList;
	}
	
	public List<String> getDONGList(String sido, String gugun) {
		List<String> DONGList = new ArrayList<String>();
		
		String selectQuery = "SELECT DISTINCT " + KEY_DONG + " FROM " + TABLE_LOCATION 
				+ " WHERE " + KEY_SIDO + " = '" + sido + "'"
				+ " AND " + KEY_GUGUN + " = '" + gugun + "'";
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if(cursor.moveToFirst()) {
			do{
				DONGList.add(cursor.getString(0));
			} while( cursor.moveToNext());
		}
		
		cursor.close();
		db.close();
		
		return DONGList;
	}
}
