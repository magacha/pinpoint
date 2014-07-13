package ch.nodo.pinpoint;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class AddEntryActivity extends Activity implements TimePickerDialog.OnTimeSetListener,
															DatePickerDialog.OnDateSetListener {

	private PinpointDatabaseHelper mHelper;
	
	private AutoCompleteTextView mOccurenceEventName;
	private TextView mOccurenceTime;
	private TextView mOccurenceDate;
	private TextView mUndoMessage;
	private View mUndoLayout;
	
	private long mIdLastAddedOccurence = -1;
	
	private EventNamesAdapter mSuggestionsAdapter;
	
	private Calendar mCalendar = Calendar.getInstance();

	private Handler mHandler;
	
	private Runnable mHideUndo = new Runnable() {
		
		@Override
		public void run() {

			Animation fadeoutAnimation = AnimationUtils.loadAnimation(AddEntryActivity.this, R.anim.fade_out);
			
			fadeoutAnimation.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					mUndoLayout.setVisibility(View.GONE);		
				}
			});
			
			mUndoLayout.startAnimation(fadeoutAnimation);
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_entry);

		mHelper = new PinpointDatabaseHelper(this);
		
		mOccurenceEventName = (AutoCompleteTextView) findViewById(R.id.edit_event);
		
		mOccurenceDate = (TextView) findViewById(R.id.text_date);
		mOccurenceTime = (TextView) findViewById(R.id.text_time);

		mUndoMessage = (TextView) findViewById(R.id.text_undo_message);
		
		mUndoLayout = findViewById(R.id.layout_undo);
		
		mUndoLayout.setVisibility(View.GONE);
		
		mSuggestionsAdapter = new EventNamesAdapter(this, mHelper);
		
		mOccurenceEventName.setAdapter(mSuggestionsAdapter);
		
		mHandler = new Handler();
		
		updateDate();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

		mHelper.close();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_entry, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if ( item.getItemId() == R.id.action_analyze) {
			
			startActivity(new Intent(this, AnalyzeActivity.class));
			
			return true;
		}
		
		
		return super.onOptionsItemSelected(item);
	}

	public void showDatePicker(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		
		Bundle bundle = new Bundle();
		
		bundle.putInt(DatePickerFragment.DAY_ARGUMENT, mCalendar.get(Calendar.DAY_OF_MONTH));
		bundle.putInt(DatePickerFragment.MONTH_ARGUMENT, mCalendar.get(Calendar.MONTH));
		bundle.putInt(DatePickerFragment.YEAR_ARGUMENT, mCalendar.get(Calendar.YEAR));
		
		newFragment.setArguments(bundle);
		
		newFragment.show(getFragmentManager(), "datePicker");
		
	}

	public void showTimePicker(View v) {

		DialogFragment newFragment = new TimePickerFragment();
		
		Bundle bundle = new Bundle();
		
		bundle.putInt(TimePickerFragment.HOUR_ARGUMENT, mCalendar.get(Calendar.HOUR_OF_DAY));
		bundle.putInt(TimePickerFragment.MINUTE_ARGUMENT, mCalendar.get(Calendar.MINUTE));
		
		newFragment.setArguments(bundle);
		
		newFragment.show(getFragmentManager(), "timePicker");

	}

	public void undoAddOccurence(View v) {
		new UndoAddOccurenceTask().execute();
	}

	public void addOccurence(View v) {

		if (mOccurenceEventName.getText().length() == 0) {
			Toast.makeText(this,
					getString(R.string.message_missing_name),
					Toast.LENGTH_SHORT).show();
			return;
		}

		new AddOccurenceTask().execute();
		
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {

		mCalendar.set(Calendar.YEAR, year);
		mCalendar.set(Calendar.MONTH, monthOfYear);
		mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		
		updateDate();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		mCalendar.set(Calendar.MINUTE, minute);	
		
		updateDate();
	}

	private void updateDate() {
		mOccurenceTime.setText(DateFormat.getTimeFormat(this).format(mCalendar.getTime()));
		mOccurenceDate.setText(DateFormat.getDateFormat(this).format(mCalendar.getTime()));
	}
	
	private class AddOccurenceTask extends AsyncTask<Void, Void, Long> {
	
		String mName;
		Date mDate;
	
		@Override
		protected void onPreExecute() {
			mName = mOccurenceEventName.getText().toString();
			mDate = mCalendar.getTime();
		}
	
		@Override
		protected Long doInBackground(Void... params) {

			long eventId = mHelper.findOrCreateEvent(mName);
			
			return mHelper.addOccurence(eventId, mDate);
		}
	
		@Override
		protected void onPostExecute(Long result) {

			mIdLastAddedOccurence = result;
		
			mUndoMessage.setText(getString(R.string.message_event_added));
			
			mUndoLayout.setVisibility(View.VISIBLE);
			
			Animation fadeinAnimation = AnimationUtils.loadAnimation(AddEntryActivity.this, R.anim.fade_in);
			
			mUndoLayout.startAnimation(fadeinAnimation);

			mHandler.postDelayed(mHideUndo, 10000);
			
			mSuggestionsAdapter.notifyDataSetChanged();
			
		}
	
	}

	private class UndoAddOccurenceTask extends AsyncTask<Void, Void, Void> {
		
		long mOccurenceId;
	
		@Override
		protected void onPreExecute() {
			mOccurenceId = mIdLastAddedOccurence;
		}
	
		@Override
		protected Void doInBackground(Void... params) {

			mHelper.deleteOccurence(mOccurenceId);
			
			return null;
		}
	
		@Override
		protected void onPostExecute(Void result) {

			mIdLastAddedOccurence = -1;
		
			mHandler.removeCallbacks(mHideUndo);
			mUndoLayout.setVisibility(View.GONE);
		
			mSuggestionsAdapter.notifyDataSetChanged();
			
		}
	
	}

	
	public static class TimePickerFragment extends DialogFragment {

		public static final String HOUR_ARGUMENT = "hour";
		public static final String MINUTE_ARGUMENT = "minutes";
		
		private TimePickerDialog.OnTimeSetListener mListener;
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			mListener = (TimePickerDialog.OnTimeSetListener) activity;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			int hour = getArguments().getInt(HOUR_ARGUMENT);
			int minute = getArguments().getInt(MINUTE_ARGUMENT);

			return new TimePickerDialog(getActivity(), mListener, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}
		
	}
	
	public static class DatePickerFragment extends DialogFragment {

		public static final String DAY_ARGUMENT = "day";
		public static final String MONTH_ARGUMENT = "month";
		public static final String YEAR_ARGUMENT = "year";
		
		private DatePickerDialog.OnDateSetListener mListener;
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			
			mListener = (DatePickerDialog.OnDateSetListener) activity;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			int day = getArguments().getInt(DAY_ARGUMENT);
			int month = getArguments().getInt(MONTH_ARGUMENT);
			int year = getArguments().getInt(YEAR_ARGUMENT);

			return new DatePickerDialog(getActivity(), mListener, year, month, day);
		}
		
	}
	


}
