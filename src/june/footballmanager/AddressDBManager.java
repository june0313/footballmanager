package june.footballmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class AddressDBManager {
	private Context _context;
	//SQLiteDatabase db;
	
	public AddressDBManager(Context context) {
		_context = context;
	}
	
	public void copyDB() {
		AssetManager assetMgr = _context.getAssets();
		File directory = new File("/data/data/june.footballmanager/databases");
		File addressDB = new File("/data/data/june.footballmanager/databases/address.db");
		
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		
		try {
			InputStream is = assetMgr.open("address.db");
			BufferedInputStream bis = new BufferedInputStream(is);
			
			// make the directory first
			if(!directory.exists())
				directory.mkdir();
			
			// if file exists, remove and regenerate
			if(addressDB.exists()) {
				addressDB.delete();
				addressDB.createNewFile();
			}
			
			fos = new FileOutputStream(addressDB);
			bos = new BufferedOutputStream(fos);
			
			int read = -1;
			byte[] buffer = new byte[1024];
			while((read = bis.read(buffer, 0, 1024)) != -1) {
				bos.write(buffer, 0, read);
			}
			bos.flush();
			
			bos.close();
			fos.close();
			bis.close();
			is.close();
			
		} catch (IOException e) {
			Log.e("AddressDBManager : ", e.getMessage());
		} 
	}
	
	public boolean isDBExists() {
		File addressDB = new File("/data/data/june.footballmanager/databases/address.db");
		
		if( addressDB.exists() ) {
			Log.i("AddressDBManager : ", "DB exists.");
			return true;
		} else {
			Log.i("AddressDBManager : ", "DB not exists.");
			return false;
		}
	}
}
