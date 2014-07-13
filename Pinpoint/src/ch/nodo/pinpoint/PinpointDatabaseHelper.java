package ch.nodo.pinpoint;

import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class PinpointDatabaseHelper extends SQLiteOpenHelper {


	private static final String DATABASE_NAME = "database.db";
	private static final int DATABASE_VERSION = 1;
	
	public interface EventColumns extends BaseColumns {
		
		public static final String TABLE_NAME = "events";
		
		public static final String NAME = "name";
		
		public static final String CONCRETE_NAME = TABLE_NAME + "." + NAME;
		public static final String CONCRETE_ID = TABLE_NAME + "." + _ID;
		
	}
	
	public interface OccurenceColumns extends BaseColumns {
		
		public static final String TABLE_NAME = "occurrences";
		
		public static final String TIME = "time";
		public static final String EVENT_ID = "event_id";
		
		public static final String CONCRETE_TIME = TABLE_NAME + "." + TIME;
		public static final String CONCRETE_ID = TABLE_NAME + "." + _ID;
		
	}
	
	public interface CausesColumns extends BaseColumns {
		
		public static final String SCORE = "score";
		public static final String EVENT_NAME = "event_name";
		
	}
	
	public PinpointDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);		
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
	
		db.execSQL("CREATE TABLE " + EventColumns.TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +                                
                EventColumns.NAME + " INTEGER NOT NULL" +
        ");");
		
        db.execSQL("CREATE TABLE " + OccurenceColumns.TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +                                
                OccurenceColumns.TIME + " INTEGER NOT NULL," +
                OccurenceColumns.EVENT_ID + " INTEGER NOT NULL" + 
        ");");
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	public long findEvent(String name) {
		
		SQLiteDatabase database = getWritableDatabase();
		
		Cursor c  = database.query(true, EventColumns.TABLE_NAME, new String[] { EventColumns._ID } ,
							"UPPER( " + EventColumns.CONCRETE_NAME + ")" + " = ?", new String[] { name.toUpperCase() } , 
								null, null, null, null);	
		try {
			
			if ( c.moveToFirst()) {
				return c.getLong(c.getColumnIndex(EventColumns._ID));
			}
			
		} finally {
			c.close();
		}
		
		return -1;
	}

	public long findOrCreateEvent(String name) {
		
		SQLiteDatabase database = getWritableDatabase();
		
		long id = findEvent(name);
		
		if ( id != -1 ) return id; 
		
		ContentValues values = new ContentValues();
		
		values.put(EventColumns.NAME, name);
		
		return database.insert(EventColumns.TABLE_NAME, null, values);
		
	}

	public Long addOccurence(long eventId, Date time) {
		
		SQLiteDatabase database = getWritableDatabase();
		
		ContentValues values = new ContentValues();
		
		values.put(OccurenceColumns.EVENT_ID, eventId);
		values.put(OccurenceColumns.TIME, time.getTime());
		
		return database.insert(OccurenceColumns.TABLE_NAME, null, values);
	
	}

	public void deleteOccurence(long occurenceId) {
		
		SQLiteDatabase database = getWritableDatabase();
		
		database.delete(OccurenceColumns.TABLE_NAME, OccurenceColumns._ID + " = ?", 
							new String[] { Long.toString(occurenceId)});
		
		
	}

	public Cursor getEventsNames() {

		SQLiteDatabase database = getReadableDatabase();

		return database.query(true, EventColumns.TABLE_NAME, new String[] {EventColumns._ID, EventColumns.NAME},
						null, null, null, null, null, null);
		
	}
	
	public Cursor findCauses(String effect) {
		
		long eventId = findEvent(effect);
		
		if (eventId == -1) return null;
		
		String query = "SELECT cause_events._id AS " + CausesColumns._ID  + ", cause_events.name AS " + CausesColumns.EVENT_NAME + ", COUNT(*) * 1.0 / events_counts.total AS " + CausesColumns.SCORE + " " + 
						       "FROM occurrences AS cause, occurrences AS effect, events AS cause_events, " +
						       " (SELECT event_id, COUNT(*) as total FROM  " + OccurenceColumns.TABLE_NAME + " GROUP BY event_id) AS events_counts " + 
						       "WHERE effect.time > cause.time AND "+
						       			"cause.event_id = events_counts.event_id AND " + 
						       			"effect.event_id <> cause.event_id AND " +
						       			"effect.time - cause.time < 24 * 60 * 60 * 1000 AND " +
						       			"cause_events._id = cause.event_id AND " +
						       			"effect.event_id = ? " +
						       	"GROUP BY cause.event_id, cause_events.name, events_counts.total ORDER BY score DESC;";
		
		SQLiteDatabase db = getReadableDatabase();
		
		System.out.println(query);
		
		return db.rawQuery(query, new String[] { Long.toString(eventId) });
		
	}

	public Cursor getEventsNames(String string) {

		SQLiteDatabase db = getReadableDatabase();
		
        return db.query( EventColumns.TABLE_NAME, 
        					new String[] { EventColumns._ID, EventColumns.NAME },
        					"UPPER(" + EventColumns.NAME + ") GLOB ?",
        					new String[] { string.toUpperCase() + "*"}, null, null, null);
        
	}
	
	
}
