package ch.nodo.pinpoint;

import ch.nodo.pinpoint.PinpointDatabaseHelper.CausesColumns;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class AnalyzeActivity extends Activity {

	private PinpointDatabaseHelper mHelper;
	
	private View mNotObservedView;
	private ListView mCausesList;
	private AutoCompleteTextView mEventNameText;
	
	private EventNamesAdapter mSuggestionsAdapter;
	
	
	private SimpleCursorAdapter mCausesAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_analyze);
		
		mHelper = new PinpointDatabaseHelper(this);
		
		mNotObservedView = findViewById(R.id.text_event_not_observed);
		mCausesList = (ListView) findViewById(R.id.list_causes);
		mEventNameText = (AutoCompleteTextView) findViewById(R.id.edit_event);
		
		mSuggestionsAdapter = new EventNamesAdapter(this, mHelper);
		
		mEventNameText.setAdapter(mSuggestionsAdapter);
		
		mCausesAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, 
														new String[] { CausesColumns.EVENT_NAME, CausesColumns.SCORE},
														new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		
		mCausesList.setAdapter(mCausesAdapter);
		
	}
	
	public void searchCauses(View view) {
		new UpdateCausesTask().execute();
	}

	private class UpdateCausesTask extends AsyncTask<Void, Void, Cursor> {

		String mName;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			mName = mEventNameText.getText().toString();
			
		}
		
		@Override
		protected Cursor doInBackground(Void... params) {
			return mHelper.findCauses(mName);
		}

		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
		
			if ( result == null) {
				
				mCausesList.setVisibility(View.GONE);
				mNotObservedView.setVisibility(View.VISIBLE);
				
				mCausesAdapter.swapCursor(null);				
				
			} else {
				
				mCausesList.setVisibility(View.VISIBLE);
				mNotObservedView.setVisibility(View.GONE);
				
				mCausesAdapter.swapCursor(result);
				
			}
		
		}

		
		
		
	}
	
	
}
