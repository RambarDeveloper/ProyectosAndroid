package la.oja.senseware.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * Created by jcoaks on 11/12/2015.
 */
public class sensewareDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "senseware.db";

    public sensewareDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(sensewareDataSource.SQL_CREATE_USERS);
        db.execSQL(sensewareDataSource.SQL_CREATE_LESSONS);
        db.execSQL(sensewareDataSource.SQL_CREATE_DAYS);
        db.execSQL(sensewareDataSource.SQL_CREATE_HISTORY);
        db.execSQL(sensewareDataSource.SQL_CREATE_HOOKS);
        db.execSQL(sensewareDataSource.SQL_CREATE_PROJECT);

        db.execSQL(sensewareDataSource.ALTER_LESSONS_COUNTBACK);
        db.execSQL(sensewareDataSource.ALTER_LESSON_GROUP);
        db.execSQL(sensewareDataSource.SQL_CREATE_RESULT);
        db.execSQL(sensewareDataSource.ALTER_LESSON_SELECT_TEXT);
        db.execSQL(sensewareDataSource.ALTER_LESSON_GETBACK);
        db.execSQL(sensewareDataSource.ALTER_RESULT);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(sensewareDataSource.SQL_DELETE_ENTRIES);
       // onCreate(db);

       if(oldVersion < 2)
        {
            db.execSQL(sensewareDataSource.ALTER_LESSONS_COUNTBACK);
            db.execSQL(sensewareDataSource.ALTER_LESSON_GROUP);
        }
        if(oldVersion < 3)
        {
            db.execSQL(sensewareDataSource.SQL_CREATE_RESULT);
       }
        if(oldVersion < 4) {
            db.execSQL(sensewareDataSource.ALTER_LESSON_SELECT_TEXT);
            db.execSQL(sensewareDataSource.ALTER_LESSON_GETBACK);
        }
        if(oldVersion < 5)
            db.execSQL(sensewareDataSource.ALTER_RESULT);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}