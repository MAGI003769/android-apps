package com.joedreamer.listview2;

import java.util.List;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListViewLoader extends ListActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	// This is the Adapter being used to display the list's data
	SimpleCursorAdapter mAdapter;

	// These are the Contacts rows that we will retrieve
	static final String[] PROJECTION = new String[] {
			ContactsContract.Data._ID, ContactsContract.Data.DISPLAY_NAME };

	// This is the select criteria
	static final String SELECTION = "((" + ContactsContract.Data.DISPLAY_NAME
			+ " NOTNULL) AND (" + ContactsContract.Data.DISPLAY_NAME
			+ " != '' ))";

	// This is for WifiManager
	TextView mainText;
	WifiManager mainWifi;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiList;
	StringBuilder sb = new StringBuilder();
	StringBuilder csv = new StringBuilder();
	boolean scanFinished = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// // Create a progress bar to display while the list loads
		// ProgressBar progressBar = new ProgressBar(this);
		// progressBar.setLayoutParams(new
		// LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		// progressBar.setIndeterminate(true);
		// getListView().setEmptyView(progressBar);
		//
		// // Must add the progress bar to the root of the layout
		// ViewGroup root = (ViewGroup) findViewById(R.id.mylist);
		// root.addView(progressBar);

		// TODO: start wifi scanning
		setContentView(R.layout.content);
		mainText = (TextView) findViewById(R.id.mainText);
		mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		receiverWifi = new WifiReceiver();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mainWifi.startScan();
		mainText.setText("Starting Scan...\n");

		// Set array adapter
		ListView listView = (ListView) findViewById(R.id.mylist);
		String[] aps = { "" };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, aps);

		listView.setAdapter(adapter);

		// For the cursor adapter, specify which columns go into which views
		String[] fromColumns = { ContactsContract.Data.DISPLAY_NAME };
		int[] toViews = { android.R.id.text1 }; // The TextView in
												// simple_list_item_1

		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, null, fromColumns,
				toViews, 0);
		setListAdapter(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);

		// TODO: for WifiManager
		// setContentView(R.layout.content);
		// mainText = (TextView) findViewById(R.id.mainText);
		// mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// receiverWifi = new WifiReceiver();
		// registerReceiver(receiverWifi, new IntentFilter(
		// WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		// mainWifi.startScan();
		// mainText.setText("Starting Scan...\n");

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// Do something when a list item is clicked
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Refresh");
		menu.add(0, 1, 1, "Finish");
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			mainWifi.startScan();
			mainText.setText("Starting Scan...\n");
			break;
		case 1:
			// To return CSV-formatted text back to calling activity (e.g., MIT
			// App Inventor App)
			Intent scanResults = new Intent();
			scanResults.putExtra("AP_LIST", csv.toString());
			setResult(RESULT_OK, scanResults);
			finish();
			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiverWifi);

		// To return CSV-formatted text back to calling activity (e.g., MIT App
		// Inventor App)
		Intent scanResults = new Intent();
		scanResults.putExtra("AP_LIST", csv.toString());
		setResult(RESULT_OK, scanResults);
		finish();
	}

	protected void onResume() {
		super.onResume();
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		// if (scanFinished == true) {
		// // wait until Wi-Fi scan is finished
		// // Handler handler = new Handler();
		// // handler.postDelayed(new Runnable() {
		// // public void run() {
		// // // TODO: Add runnable later
		// // }
		// // }, 1000);
		// // To return results back to calling activity (e.g., MIT App
		// // Inventor App)
		// Intent scanResults = new Intent();
		// scanResults.putExtra("AP_LIST", sb.toString());
		// setResult(RESULT_OK, scanResults);
		// finish();
		// }
	}

	// Called when a new Loader needs to be created
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Now create and return a CursorLoader that will take care of
		// creating a Cursor for the data being displayed.
		return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
				PROJECTION, SELECTION, null, null);
	}

	// Called when a previously created loader has finished loading
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in. (The framework will take care of closing the
		// old cursor once we return.)
		mAdapter.swapCursor(data);
	}

	// Called when a previously created loader is reset, making the data
	// unavailable
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
		// above is about to be closed. We need to make sure we are no
		// longer using it.
		mAdapter.swapCursor(null);
	}

	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			sb = new StringBuilder();
			csv = new StringBuilder();
			wifiList = mainWifi.getScanResults();

			// prepare text for display and CSV table
			sb.append("Number of APs Detected: ");
			sb.append((Integer.valueOf(wifiList.size())).toString());
			sb.append("\n\n");
			for (int i = 0; i < wifiList.size(); i++) {
				// sb.append((Integer.valueOf(i + 1)).toString() + ".");
				// SSID
				sb.append("SSID:").append((wifiList.get(i)).SSID);
				sb.append("\n");
				csv.append((wifiList.get(i)).SSID);
				csv.append(",");
				// BSSID
				sb.append("BSSID:").append((wifiList.get(i)).BSSID);
				sb.append("\n");
				csv.append((wifiList.get(i)).BSSID);
				csv.append(",");
				// capabilities
				sb.append("Capabilities:").append(
						(wifiList.get(0)).capabilities);
				sb.append("\n");
				// frequency
				sb.append("Frequency:").append((wifiList.get(i)).frequency);
				sb.append("\n");
				csv.append((wifiList.get(i)).frequency);
				csv.append(",");
				// level
				sb.append("Level:").append((wifiList.get(i)).level);
				sb.append("\n\n");
				csv.append((wifiList.get(i)).level);
				csv.append("\n");
			}

			mainText.setText(sb);

			// notify that Wi-Fi scan has finished
			scanFinished = true;
		}
	}
}