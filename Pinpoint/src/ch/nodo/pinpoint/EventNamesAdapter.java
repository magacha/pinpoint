package ch.nodo.pinpoint;

import android.content.Context;
import android.database.Cursor;
import android.widget.Filterable;
import android.widget.SimpleCursorAdapter;

public class EventNamesAdapter extends SimpleCursorAdapter implements Filterable {

	private PinpointDatabaseHelper mHelper;
		
	public EventNamesAdapter(Context context, PinpointDatabaseHelper mHelper2) {
		super(context, android.R.layout.simple_list_item_1, null,
									new String[] { PinpointDatabaseHelper.EventColumns.NAME }, 
									new int[]{ android.R.id.text1}, 0);
		
		mHelper = mHelper2;
	}


    @Override
    public String convertToString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(PinpointDatabaseHelper.EventColumns.NAME));
    }
	
	
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {

		if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }

		if ( constraint == null) {
			return mHelper.getEventsNames();
		} else {
			return mHelper.getEventsNames(constraint.toString());
		}
		
	}
	
}
