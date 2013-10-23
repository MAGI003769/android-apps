package com.joedreamer.listview2;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class ListViewLoader extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_view_loader, menu);
		return true;
	}

}
