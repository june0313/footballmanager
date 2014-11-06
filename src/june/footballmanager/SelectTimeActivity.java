package june.footballmanager;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class SelectTimeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_time);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_time, menu);
		return true;
	}

}
