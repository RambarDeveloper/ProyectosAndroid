package la.oja.senseware.data;

import android.provider.BaseColumns;

/**
 * Created by jcoaks on 11/12/2015.
 */
public final class sensewareDataSource {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public sensewareDataSource() {}

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String DATETIME_TYPE = " DATETIME";

    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_USERS =
            "CREATE TABLE " + User.TABLE_NAME + " (" +
                    User._ID + " INTEGER PRIMARY KEY," +
                    User.COLUMN_NAME_ID_USER + INTEGER_TYPE + COMMA_SEP +
                    User.COLUMN_NAME_CURRENT + TEXT_TYPE + COMMA_SEP +
                    User.COLUMN_NAME_DAY + TEXT_TYPE + COMMA_SEP +
                    User.COLUMN_NAME_ID_PROJECT + INTEGER_TYPE +
                    " )";
    public static final String SQL_CREATE_LESSONS =
            "CREATE TABLE " + Lesson.TABLE_NAME + " (" +
                    Lesson._ID + " INTEGER PRIMARY KEY," +
                    Lesson.COLUMN_NAME_DOWNLOAD + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_ID_LESSON + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_ID_LANGUAJE + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_ID_DAY + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_POSITION + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_COPY + TEXT_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_SECONDS + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_SECTITLE + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_NEXTBUTTON + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_BACKBUTTON + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_SRC + TEXT_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_TEXTFIELD + TEXT_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_SECTEXTFIELD + INTEGER_TYPE + COMMA_SEP +
                    Lesson.COLUMN_NAME_DATE_UPDATE + TEXT_TYPE +
                    " )";

    public static final String SQL_CREATE_DAYS =
            "CREATE TABLE " + Day.TABLE_NAME + " (" +
                    Day._ID + " INTEGER PRIMARY KEY," +
                    Day.COLUMN_NAME_ID_DAY + INTEGER_TYPE + COMMA_SEP +
                    Day.COLUMN_NAME_DAY + INTEGER_TYPE + COMMA_SEP +
                    Day.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    Day.COLUMN_NAME_VISIBLECLASSES + INTEGER_TYPE + COMMA_SEP +
                    Day.COLUMN_NAME_VISIBLE + INTEGER_TYPE +
                    " )";

    public static final String SQL_CREATE_HOOKS =
            "CREATE TABLE " + Hook.TABLE_NAME + " (" +
                    Hook._ID + " INTEGER PRIMARY KEY," +
                    Hook.COLUMN_NAME_HOOK + INTEGER_TYPE + COMMA_SEP +
                    Hook.COLUMN_NAME_DATA + TEXT_TYPE + COMMA_SEP +
                    Hook.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    Hook.COLUMN_NAME_UPLOAD + INTEGER_TYPE + COMMA_SEP +
                    Hook.COLUMN_NAME_URL + TEXT_TYPE + COMMA_SEP +
                    Hook.COLUMN_NAME_DATE + DATETIME_TYPE +
                    " )";

    public static final String SQL_CREATE_HISTORY =
            "CREATE TABLE " + History.TABLE_NAME + " (" +
                    History._ID + " INTEGER PRIMARY KEY," +
                    History.COLUMN_NAME_ID_LESSON + INTEGER_TYPE + COMMA_SEP +
                    History.COLUMN_NAME_ID_PROJECT + INTEGER_TYPE + COMMA_SEP +
                    History.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    History.COLUMN_NAME_DATE + DATETIME_TYPE +
                    " )";

    public static final String SQL_CREATE_PROJECT =
            "CREATE TABLE " + Project.TABLE_NAME + "( " +
                    Project._ID + " INTEGER PRIMARY KEY," +
                    Project.COLUMN_NAME_ID_PROJECT + INTEGER_TYPE + COMMA_SEP +
                    Project.COLUMN_NAME_NA_PROJECT + TEXT_TYPE + COMMA_SEP +
                    Project.COLUMN_NAME_ID_USER + INTEGER_TYPE + COMMA_SEP +
                    Project.COLUMN_NAME_CREATED + DATETIME_TYPE + COMMA_SEP +
                    Project.COLUMN_NAME_ID_TMP + INTEGER_TYPE +
                    ")";

    public static final String SQL_CREATE_RESULT =
            "CREATE TABLE " + Result.TABLE_NAME + "( " +
                    Result._ID + " INTEGER PRIMARY KEY," +
                    Result.COLUMN_NAME_ID_LESSON + INTEGER_TYPE + COMMA_SEP +
                    Result.COLUMN_NAME_ID_PROJECT + INTEGER_TYPE + COMMA_SEP +
                    Result.COLUMN_NAME_ID_SOURCE + INTEGER_TYPE + COMMA_SEP +
                    Result.COLUMN_NAME_RESULT + TEXT_TYPE + COMMA_SEP +
                    Result.COLUMN_NAME_DATE + DATETIME_TYPE + COMMA_SEP +
                    Result.COLUMN_NAME_ASSIGNED + INTEGER_TYPE +  COMMA_SEP +
                    Result.COLUMN_NAME_HIDDEN + INTEGER_TYPE +
                    ")";

    /* Inner class that defines the table */
    public static abstract class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String COLUMN_NAME_ID_USER = "id_user_fk";
        public static final String COLUMN_NAME_CURRENT = "current";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_ID_PROJECT = "id_project";
    }

    /* Inner class that defines the table */
    public static abstract class Lesson implements BaseColumns {
        public static final String TABLE_NAME = "lesson";
        public static final String COLUMN_NAME_DOWNLOAD = "download";
        public static final String COLUMN_NAME_ID_LESSON = "id_lesson";
        public static final String COLUMN_NAME_ID_LANGUAJE = "id_languaje";
        public static final String COLUMN_NAME_ID_DAY = "id_day";
        public static final String COLUMN_NAME_POSITION = "position";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
        public static final String COLUMN_NAME_COPY = "copy";
        public static final String COLUMN_NAME_SECONDS = "seconds";
        public static final String COLUMN_NAME_SECTITLE = "sectitle";
        public static final String COLUMN_NAME_NEXTBUTTON = "nextbutton";
        public static final String COLUMN_NAME_BACKBUTTON = "backbutton";
        public static final String COLUMN_NAME_SRC = "src";
        public static final String COLUMN_NAME_TEXTFIELD = "textfield";
        public static final String COLUMN_NAME_SECTEXTFIELD = "sectextfield";
        public static final String COLUMN_NAME_DATE_UPDATE = "date_update";
        public static final String COLUMN_NAME_COUNTBACK = "countback";
        public static final String COLUMN_NAME_GROUP_ALL = "group_all";
        public static final String COLUMN_NAME_SELECT_TEXT = "select_text";
        public static final String COLUMN_NAME_GETBACK = "getback";
    }

    /* Inner class that defines the table */
    public static abstract class Day implements BaseColumns {
        public static final String TABLE_NAME = "day";
        public static final String COLUMN_NAME_ID_DAY = "id_day";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_VISIBLECLASSES = "visibleclasses";
        public static final String COLUMN_NAME_VISIBLE = "visible";

    }

    /* Inner class that defines the table */
    public static abstract class Hook implements BaseColumns {
        public static final String TABLE_NAME = "hook";
        public static final String COLUMN_NAME_HOOK = "hook";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_UPLOAD = "upload";
        public static final String COLUMN_NAME_DATE = "date";
    }

    /* Inner class that defines the table */
    public static abstract class History implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_ID_PROJECT = "id_project";
        public static final String COLUMN_NAME_ID_LESSON = "id_lesson";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_DATE = "date";
    }

    public static abstract class Project implements BaseColumns {
        public static final  String TABLE_NAME = "project";
        public static final String COLUMN_NAME_ID_PROJECT = "id_project";
        public static final String COLUMN_NAME_NA_PROJECT = "na_project";
        public static final String COLUMN_NAME_ID_USER = "id_user";
        public static final String COLUMN_NAME_CREATED = "created";
        public static final String COLUMN_NAME_ID_TMP = "id_tmp";

    }

    public static abstract class Result implements BaseColumns {
        public static final  String TABLE_NAME = "result";
        public static final String COLUMN_NAME_ID_PROJECT = "id_project";
        public static final String COLUMN_NAME_ID_SOURCE = "id_source";
        public static final String COLUMN_NAME_ID_LESSON = "id_lesson";
        public static final String COLUMN_NAME_DATE = "date_result";
        public static final String COLUMN_NAME_ASSIGNED = "assigned";
        public static final String COLUMN_NAME_RESULT = "result";
        public static final String COLUMN_NAME_HIDDEN = "hidden";
        public static final String COLUMN_NAME_ID_RESULT = "id_result";
    }

    public static final String ALTER_LESSONS_COUNTBACK =
            "ALTER TABLE lesson  ADD COLUMN " + Lesson.COLUMN_NAME_COUNTBACK + INTEGER_TYPE;

    public static final String ALTER_LESSON_GROUP =
            "ALTER TABLE lesson  ADD COLUMN " + Lesson.COLUMN_NAME_GROUP_ALL + INTEGER_TYPE;

    public static final String ALTER_LESSON_SELECT_TEXT =
            "ALTER TABLE lesson  ADD COLUMN " + Lesson.COLUMN_NAME_SELECT_TEXT + INTEGER_TYPE;

    public static final String ALTER_LESSON_GETBACK =
            "ALTER TABLE lesson  ADD COLUMN " + Lesson.COLUMN_NAME_GETBACK + TEXT_TYPE;

    public static  final String ALTER_RESULT =
            "ALTER TABLE result ADD COLUMN " + Result.COLUMN_NAME_ID_RESULT + INTEGER_TYPE;
}