package la.oja.senseware;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.os.StrictMode;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;
import la.oja.senseware.models.Lessons;
import la.oja.senseware.models.Project;

/**
 * Created by Juan Robles on 23/11/2015.
 */
public class LessonActivity  extends AppCompatActivity {

    SharedPreferences settings;
    MediaPlayer mp = null;
    static CountDownTimer countDown;
    Lessons current;
    int count_seconds;
    String root = Environment.getExternalStorageDirectory().toString();
    //File myDir = new File(root + "/senseware_sounds");
    File myDir;
    ProgressDialog progress;
    String imageUrl, utms;
    String fileName;
    boolean changeDay = false;
    int clases;
    ApiCall call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        call = new ApiCall(getApplicationContext());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        clases = getIntent().getIntExtra("clases", 0);

        String utm_source = settings.getString("utm_source", "");
        String utm_medium = settings.getString("utm_medium", "");
        String utm_term = settings.getString("utm_term", "");
        String utm_content = settings.getString("utm_content", "");
        String utm_campaign = settings.getString("utm_campaign", "");

        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        int version = prefs.getInt("appVersion", 0);

        utms = "app : 'Android', ";
        utms += "'version': "+version;
        if(utm_source.compareTo("") != 0)
            utms += ", 'utm_source': '" + utm_source + "'";
        if(utm_medium.compareTo("") != 0)
            utms += ", 'utm_medium': '" + utm_medium + "'";
        if(utm_term.compareTo("") != 0)
            utms += ", 'utm_term': '" + utm_term + "'";
        if(utm_content.compareTo("") != 0)
            utms += ", 'utm_content': '" + utm_content + "'";
        if(utm_campaign.compareTo("") != 0)
            utms += ", 'utm_campaign': '" + utm_campaign + "'";

        myDir = new File (getExternalFilesDir(Environment.getExternalStorageDirectory().toString()) + "/senseware_sounds");

        this.mp = new MediaPlayer();
        this.current = ProgramActivity.getCurrent();
        this.count_seconds = current.getSeconds();

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(current.getTitle());

        TextView cd = (TextView) findViewById(R.id.countdown);
        cd.setText(getDurationString(count_seconds));

        TextView cd2 = (TextView) findViewById(R.id.countdown2);
        cd2.setText(getDurationString(count_seconds));

        TextView pos = (TextView) findViewById(R.id.position);
        String textPos = String.valueOf(current.getPosition())+" de " + clases;
        pos.setText(textPos);


        try {
            String url = current.getSrc();
            String[] bits = url.split("/");
            String filename = bits[bits.length-1];
            imageUrl = url;
            fileName = filename;
            int day = settings.getInt("day", 1);
            String audioFile = getFile();


        }
        catch (Exception e){
            e.printStackTrace();
        }

        ImageButton close = (ImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mp.isPlaying()){
                    mp.stop();
                    mp.release();
                    mp = null;

                }

                closeNotification();

                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
               // LessonActivity.String audioFile.finish();

                Intent intent = new Intent(LessonActivity.this, ProgramActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        ImageButton play = (ImageButton) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playFunction();
            }
        });

        Button finish = (Button) findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mTextboxView = (EditText) findViewById(R.id.textbox);
                String textbox = mTextboxView.getText().toString();

                if(mp.isPlaying()){
                    mp.stop();
                    closeNotification();

                }
                if(!textbox.isEmpty())
                {
                    final String url = getString(R.string.urlAPI) + "result";
                    SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
                    SharedPreferences.Editor editor = settings.edit();

                    int id_project = settings.getInt("id_project", 0);
                    Calendar c = Calendar.getInstance();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = sdf.format(c.getTime());

                    boolean isTmpProject = settings.getBoolean("idTmp", false);
                    String email = settings.getString("email", "");
                    int day = settings.getInt("day", 0);
                    int pos = settings.getInt("current", 0);
                    String data;

                    if(isTmpProject)
                        data = "{'id_tmp': "+ id_project +", 'id_lesson': "+ current.getId_lesson() + " , 'result': '" + textbox + "', 'date': '"+ date +"', 'utms': [{" + utms + "}]}";
                    else
                        data = "{'id_project': "+ id_project +", 'id_lesson': "+ current.getId_lesson() + " , 'result': '" + textbox + "', 'date': '"+ date +"', 'utms': [{" + utms + "}]}";

                    ContentValues values_hook = new ContentValues();
                    values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
                    values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
                    values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "respuesta");
                    values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
                    values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
                    values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);
                    insertEvent("Hook", values_hook);
                    insertResult(id_project, current.getId_lesson(), date, textbox, 0);

                    ContentValues values_history = new ContentValues();
                    values_history.put(sensewareDataSource.History.COLUMN_NAME_CONTENT, textbox);
                    values_history.put(sensewareDataSource.History.COLUMN_NAME_DATE, date);
                    values_history.put(sensewareDataSource.History.COLUMN_NAME_ID_LESSON, current.getId_lesson());
                    values_history.put(sensewareDataSource.History.COLUMN_NAME_ID_PROJECT, id_project);
                    insertEvent("History", values_history);

                    String urlEvent = getString(R.string.urlAPI) + "event/Terminoclase";
                    String dataEvent = "{email: '" + email + "',  values: [{day: '"+day+"', clase: '"+pos+"'," + utms + "}]}";

                    ContentValues values_event = new ContentValues();
                    values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, dataEvent);
                    values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
                    values_event.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "event");
                    values_event.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
                    values_event.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
                    values_event.put(sensewareDataSource.Hook.COLUMN_NAME_URL, urlEvent);
                    insertEvent("Hook", values_event);

                    String resp = settings.getString("resp", null);
                    editor.putString("resp", textbox);
                    editor.commit();

                    if(current.getId_lesson() == 4 || current.getId_lesson() == 12)
                    {
                        editor.putString("resp2", textbox);
                        editor.commit();
                    }

                    upgradeCurrent();

                    ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);

                    ProgramActivity.updateCurrent();
                    Intent in = getIntent();
                    in.putExtra("clases", clases);
                    LessonActivity.this.finish();
                    startActivity(in);

                }

            }
        });

        ImageButton pause = (ImageButton) findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mp.isPlaying()){
                    mp.pause();
                    closeNotification();
                    ImageButton play = (ImageButton) findViewById(R.id.play);
                    ImageButton pause = (ImageButton) findViewById(R.id.pause);
                    play.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.GONE);
                    countDown.cancel();
                }
            }
        });


        Typeface ultralight= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        EditText textbox = (EditText) findViewById(R.id.textbox);

        title.setTypeface(thin);
        textbox.setTypeface(thin);
        cd.setTypeface(ultralight);
        cd2.setTypeface(ultralight);
        pos.setTypeface(thin);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(textbox, InputMethodManager.SHOW_FORCED);

        final String url =  getString(R.string.urlAPI) + "event/Vioclase";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(c.getTime());
        int id_project = settings.getInt("id_project", 0);
        String email = settings.getString("email", "");
        boolean isIdTmp = settings.getBoolean("idTmp", false);
        String data;

        if(isIdTmp)
            data = "{'email':'"+ email +"', 'id_tmp': "+ id_project + ", 'id_lesson': "+ current.getId_lesson() + " , 'date': '"+ date +"', 'values': [{" + utms + "}]}";
        else
            data = "{'email':'"+ email +"', 'id_project': "+ id_project + ", 'id_lesson': "+ current.getId_lesson() + " , 'date': '"+ date +"', 'values': [{" + utms + "}]}";

        ContentValues values_hook = new ContentValues();
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "vioclase");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

        SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);

        int day = settings.getInt("day", 1);
        int act = settings.getInt("current", 1);

    }


    private void upgradeCurrent()
    {
        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        SharedPreferences.Editor editor = settings.edit();

        int current = settings.getInt("current", 0);
        int day = settings.getInt("day", 0);
        String title_day = String.valueOf(day);

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getReadableDatabase();

        String[] projection = {
                sensewareDataSource.Day._ID,
                sensewareDataSource.Day.COLUMN_NAME_VISIBLECLASSES,

        };

        String whereCol = sensewareDataSource.Day.COLUMN_NAME_ID_DAY + " = " + day;

        Cursor c = db.query(
                sensewareDataSource.Day.TABLE_NAME,      // The table to query
                projection,                                 // The columns to return
                whereCol,                                   // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                null                                        // The sort order
        );

        if (c.moveToFirst())
        {
            int visibleClasses = c.getInt(1);
            int id = c.getInt(0);


            if(current < visibleClasses) {
                current++;

            } 

            else  {
                changeDay = true;
                id++;
                //get next day
                String[] fields = {
                        sensewareDataSource.Day._ID,
                        sensewareDataSource.Day.COLUMN_NAME_ID_DAY,
                        sensewareDataSource.Day.COLUMN_NAME_VISIBLECLASSES,
                        sensewareDataSource.Day.COLUMN_NAME_TITLE
                };

                String where = sensewareDataSource.Day._ID + "="+ id +" AND " + sensewareDataSource.Day.COLUMN_NAME_ID_DAY +"!=9" ;

                Cursor cd = db.query(
                        sensewareDataSource.Day.TABLE_NAME,      // The table to query
                        fields,                                 // The columns to return
                        where,                                   // The columns for the WHERE clause
                        null,                                       // The values for the WHERE clause
                        null,                                       // don't group the rows
                        null,                                       // don't filter by row groups
                        null                                        // The sort order
                );

                if(cd.moveToFirst())
                {
                    int nextday = cd.getInt(1);
                    String titleDay = cd.getString(3);
                    current = -1;

                    editor.putInt("nextDay", nextday);
                    editor.putString("nextDayTitle", titleDay);

                }

            }
        }

        editor.putInt("current", current);
        editor.putInt("day", day);
        editor.putBoolean("vioclase", true);

        int max_day = settings.getInt("max_day", 0);
        if(max_day <= day && current != -1)
        {
            editor.putInt("max_current", current);
            editor.putInt("max_day", day);
        }

        editor.commit();

    }

    private void insertEvent(String tableName, ContentValues values_hook)
    {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        long newRowId;
        if(tableName.equals("Hook")) {
            SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);
        }
        if(tableName.equals("History")) {
            newRowId = db.insert(sensewareDataSource.History.TABLE_NAME, null, values_hook);
        }
        db.close();
    }

    private void insertResult(int id_project, int id_lesson, String date, String result, int hidden)
    {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT, id_project);
        values.put(sensewareDataSource.Result.COLUMN_NAME_ID_LESSON, id_lesson);
        values.put(sensewareDataSource.Result.COLUMN_NAME_DATE, date);
        values.put(sensewareDataSource.Result.COLUMN_NAME_HIDDEN, hidden);
        values.put(sensewareDataSource.Result.COLUMN_NAME_RESULT, result);
        values.put(sensewareDataSource.Result.COLUMN_NAME_ID_SOURCE, 0);
        values.put(sensewareDataSource.Result.COLUMN_NAME_ASSIGNED, 0);
        values.put(sensewareDataSource.Result.COLUMN_NAME_ID_RESULT, 0);

        long newRowId;
        newRowId = db.insert(sensewareDataSource.Result.TABLE_NAME, null, values);

        db.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mp.isPlaying()){
            mp.stop();
            closeNotification();
        }
        //((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);
        this.finish();
    }

    public void playFunction(){

        final int day = settings.getInt("day", 0);
        final String email = settings.getString("email", "");
        final int pos = settings.getInt("current", 0);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String date = sdf.format(c.getTime());

        if(pos==1){
            String urlEvent = getString(R.string.urlAPI) + "event/Empezodia"+day;
            String dataEvent = "{email: '" + email + "', values: [{" + utms + "}]}";

            ContentValues values_event = new ContentValues();
            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, dataEvent);
            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "event");
            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_URL, urlEvent);
            insertEvent("Hook", values_event);
        }

        try
        {
            try {

                if(day == 1)
                {
                    AssetFileDescriptor descriptor = getAssets().openFd("sounds/"+fileName);
                    mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    descriptor.close();

                }
                else
                    mp.setDataSource(myDir + "/" + fileName);

                mp.prepare();
                mp.start();


                String urlEvent = getString(R.string.urlAPI) + "event/Empezoclase";
                String dataEvent = "{email: '" + email + "', 'id_lesson': '"+ current.getId_lesson() +  "', values: [{" + utms + "}]}";

                ContentValues values_event = new ContentValues();
                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, dataEvent);
                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "event");
                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_URL, urlEvent);
                insertEvent("Hook", values_event);
            }
            catch (Exception e)
            {
                mp.start();
            }

            if(mp!=null) {

                if(mp.isPlaying())
                    displayNotification();

                final ImageButton[] play = {(ImageButton) findViewById(R.id.play)};
                ImageButton pause = (ImageButton) findViewById(R.id.pause);
                play[0].setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);

                // And From your main() method or any other method
                countDown = new CountDownTimer(count_seconds * 1000, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                        count_seconds--;
                        int play_seconds = current.getSeconds() - count_seconds;
                        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
                        final EditText textbox = (EditText) findViewById(R.id.textbox);
                        if (play_seconds == current.getSectitle()) {
                            TextView title = (TextView) findViewById(R.id.title);
                            title.setText(current.getSubtitle());
                        }

                        if (current.getTextfield() == 0 && count_seconds == 1) {
                            upgradeCurrent();

                            if (mp.isPlaying()) {
                                mp.stop();
                                closeNotification();
                            }
                            //((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.HIDE_IMPLICIT_ONLY);


                            String urlEvent = getString(R.string.urlAPI) + "event/Terminoclase";
                            String dataEvent = "{email: '" + email + "', values: [{day: '" + day + "', clase: '" + pos + "', " + utms + "}]}";

                            ContentValues values_event = new ContentValues();
                            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, dataEvent);
                            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
                            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "event");
                            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
                            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
                            values_event.put(sensewareDataSource.Hook.COLUMN_NAME_URL, urlEvent);
                            insertEvent("Hook", values_event);

                            //add share
                            if (changeDay) {

                                urlEvent = getString(R.string.urlAPI) + "event/Terminodia" + day;

                                values_event = new ContentValues();
                                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, dataEvent);
                                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
                                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "event");
                                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
                                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
                                values_event.put(sensewareDataSource.Hook.COLUMN_NAME_URL, urlEvent);
                                insertEvent("Hook", values_event);

                                Intent in = new Intent(LessonActivity.this, ShareActivity.class);
                                startActivity(in);
                            }


                            finish();
                            LessonActivity.this.finish();

                        }

                        int textfieltype = current.getTextfield();
                        int moment = current.getSectextfield();
                        int select_text = current.getSelect_text();

                        if (textfieltype > 0 && moment > 2 && play_seconds == moment) {
                            TextView countdown = (TextView) findViewById(R.id.countdown);
                            TextView countdown2 = (TextView) findViewById(R.id.countdown2);
                            TextView pos = (TextView) findViewById(R.id.position);

                            ImageButton play = (ImageButton) findViewById(R.id.play);
                            ImageButton pause = (ImageButton) findViewById(R.id.pause);
                            Button finish = (Button) findViewById(R.id.finish);
                            TextView title = (TextView) findViewById(R.id.title);
                            String titleText = title.getText().toString();
                            if (select_text == 0 && textfieltype == 1) {
                                textbox.setHint(titleText);
                                textbox.setHintTextColor(Color.parseColor("#777777"));
                            } else if (select_text != 0 && textfieltype == 1){
                                textbox.setText(titleText);
                            }

                            if (textfieltype > 1)  // Muestra el textbox muestro respuesta X
                            {
                                String getback = current.getGetback();

                                //FALTA GENTE
                                //Consulto localmente si no existe, me traigo remoto
                                String resp = getResult(getback);
                                Log.i("resp", resp);
                                if(select_text == 0) {
                                    textbox.setHint(resp);
                                    textbox.setHintTextColor(Color.parseColor("#777777"));
                                }
                                else
                                {
                                    textbox.setText(resp);
                                }
                            } else if (textfieltype == 1 && current.getId_day() == 1 && current.getPosition() == 7)  // Muestra el textbox muestro respuesta X
                            {
                                String resumen = getResumen();
                                textbox.setText(resumen);
                                textbox.setHintTextColor(Color.parseColor("#777777"));
                            }

                            title.setVisibility(View.GONE);
                            pos.setVisibility(View.GONE);
                            countdown.setTextSize(50);
                            countdown.setTop(-180);
                            countdown.setVisibility(View.GONE);
                            countdown2.setVisibility(View.VISIBLE);
                            play.setVisibility(View.GONE);
                            pause.setVisibility(View.GONE);
                            finish.setVisibility(View.VISIBLE);
                            textbox.setVisibility(View.VISIBLE);

                            textbox.setCursorVisible(true);
                            textbox.setTextIsSelectable(true);
                            textbox.setFocusableInTouchMode(true);
                            textbox.requestFocus();

                            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(textbox, InputMethodManager.SHOW_FORCED);
                        }

                        TextView cd = (TextView) findViewById(R.id.countdown);
                        cd.setText(getDurationString(count_seconds));

                        TextView cd2 = (TextView) findViewById(R.id.countdown2);
                        cd2.setText(getDurationString(count_seconds));
                        //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);

                        // InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        // imm.showSoftInput(textbox, InputMethodManager.SHOW_FORCED);
                        textbox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                textbox.setFocusableInTouchMode(true);
                                textbox.requestFocus();

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.showSoftInput(textbox, InputMethodManager.SHOW_FORCED);
                            }
                        });
                    }


                    @Override
                    public void onFinish() {
                    }
                }.start();
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getResult(String getback)
    {
        String respuesta = "";
        Cursor c = null;

        String ids_lesson[] = getback.split(" ");

        int id_project = settings.getInt("id_project", 0);

        try
        {
            sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
            SQLiteDatabase db = sDbHelper.getWritableDatabase();

            for (int i = 0; i < ids_lesson.length; i++) {

                final String MY_QUERY = "SELECT " + sensewareDataSource.Result.COLUMN_NAME_RESULT +
                        " FROM " + sensewareDataSource.Result.TABLE_NAME + " r " +
                        " WHERE id_lesson=? AND " + sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT +" =? "+
                        "ORDER BY "+ sensewareDataSource.Result.COLUMN_NAME_DATE +" DESC";

                c = db.rawQuery(MY_QUERY, new String[]{ids_lesson[i], String.valueOf(id_project)});
                if (c.moveToFirst()) {
                    Log.i("value", ids_lesson[i]);
                    if(i > 0 ){
                        if(Integer.valueOf(ids_lesson[i]) != 392 && Integer.valueOf(ids_lesson[i]) != 394)
                            respuesta = respuesta + " | ";
                        else
                            respuesta = respuesta + ", ";
                    }

                    respuesta += c.getString(0);
                }
                else{

                    try
                    {
                        String url = getString(R.string.urlAPI) + "getResultByLesson/" +ids_lesson[i];
                        String resp = call.callGet(url);

                        //convert the response from string to JsonObject
                        JSONObject obj = new JSONObject(resp);
                        int status = obj.getInt("status");
                        String message = obj.getString("message");

                        if (status == 200 && message.equals("OK")) {
                            String result = obj.getString("result");

                            respuesta += " | " + result;
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            c.close();
            db.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


        return respuesta;
    }

    private String getResumen()
    {
        String respuestas[] = new String[5];

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        Cursor c = null;
        Project[]  projectsBD = null;

        int i = 0;

        try
        {
            String[] projection = { sensewareDataSource.Result.COLUMN_NAME_ID_LESSON, sensewareDataSource.Result.COLUMN_NAME_RESULT };
            String whereColProd = sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 200 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 201 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 202 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 203 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 204";

            String whereColPrueba = sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 173 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 174 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 175 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 176 OR " +
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = 177";

            String order = sensewareDataSource.Result.COLUMN_NAME_DATE + " DESC";

            c = db.query(
                    sensewareDataSource.Result.TABLE_NAME,      // The table to query
                    projection,                                 // The columns to return
                    whereColProd,                                   // The columns for the WHERE clause
                    null,                                       // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    order                                        // The sort order
            );

            if (c.moveToFirst())
            {
                do {
                    respuestas[i] = c.getString(1);
                    i++;
                    if(i == 5)
                        break;
                } while (c.moveToNext());

                c.close();
                db.close();
            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String resumen = "Debes completar la actividad";
        if(i == 5) {
            resumen = respuestas[4] + " esta diseñado para " +
                    respuestas[3] + ", resuelve el problema " +
                    respuestas[2] + ", " +
                    respuestas[0] + " para " +
                    respuestas[1];
        }
        return resumen;
    }

    public String getFile() {

        int day = settings.getInt("day", 1);

        if(day == 1) {
           playFunction();
        }
        else {

            myDir.mkdirs();
            File file = new File(myDir, fileName);
            // File file = new File (this.getExternalFilesDir("/senseware_sounds"), fileName);
            if (!file.exists()) {
                progress = ProgressDialog.show(this, "Senseware", "Descargando clase...", true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long space = myDir.getUsableSpace();

                            Log.i("space", String.valueOf(space));

                            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

                            Log.i("CLASE", myDir + " " + fileName + "_tmp");
                            File file = new File(myDir, fileName + "_tmp");

                            URL url = new URL(imageUrl);
                            HttpURLConnection c = (HttpURLConnection) url.openConnection();
                            c.setRequestMethod("GET");
                            c.setDoOutput(true);
                            c.connect();

                            InputStream is = c.getInputStream();
                            OutputStream os = new FileOutputStream(file);

                            long sizeFile = is.available();

                            Log.i("sizeFile", String.valueOf(sizeFile));

                            if (space > 0 && space >= sizeFile) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, length);
                                }

                                is.close();
                                os.close();

                                File file_def = new File(myDir, fileName);
                                file.renameTo(file_def);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        playFunction();
                                    }
                                });
                            } else {

                                int day = settings.getInt("day", 0);

                                if (day > 2) {
                                    deleteAudios();
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog alertDialog = new AlertDialog.Builder(LessonActivity.this).create();
                                            alertDialog.setTitle("Senseware");
                                            alertDialog.setMessage("No se han podido descargar las actividades, debido a que no tienes espacio en tu dispositivo");
                                            alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int POSITIVE) {
                                                    startActivity(new Intent(LessonActivity.this, ProgramActivity.class));
                                                    dialog.cancel();
                                                    finish();
                                                }
                                            });
                                            alertDialog.setIcon(R.mipmap.sw_black);
                                            alertDialog.show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                            progress.dismiss();
                            int day = settings.getInt("id_day", 0);

                            if (day > 2) {
                                deleteAudios();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AlertDialog alertDialog = new AlertDialog.Builder(LessonActivity.this).create();
                                        alertDialog.setTitle("Senseware");
                                        alertDialog.setMessage("No se han podido descargar las actividades, debido a que no tienes espacio en tu dispositivo");
                                        alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int POSITIVE) {
                                                startActivity(new Intent(LessonActivity.this, ProgramActivity.class));
                                                dialog.cancel();
                                                finish();
                                            }
                                        });
                                        alertDialog.setIcon(R.mipmap.sw_black);
                                        alertDialog.show();
                                    }
                                });
                            }
                        }

                    }
                }).start();
            } else {
                playFunction();
            }
        }

        return myDir + "/" + fileName;
    }

    private void deleteAudios() {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();
        Cursor c = null;

        int id_day = settings.getInt("day", 0);

        try {
            String[] projection = {
                    sensewareDataSource.Lesson._ID,
                    sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY,
                    sensewareDataSource.Lesson.COLUMN_NAME_SRC,
            };

            String whereCol = sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY + " > 1 AND " +
                    sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY + " != " + String.valueOf(id_day);

            c = db.query(
                    sensewareDataSource.Lesson.TABLE_NAME,      // The table to query
                    projection,                                 // The columns to return
                    whereCol,                                   // The columns for the WHERE clause
                    null,                                       // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    null                                        // The sort order
            );

            int day_delete;

            if (c.moveToFirst()){
                day_delete = c.getInt(1);
                int count = 0;

                Log.i("dayDelete", String.valueOf(day_delete));

                do{
                    int dayDelete = c.getInt(1);

                    if(day_delete == dayDelete) {
                        String src = c.getString(2);
                        String[] bits = src.split("/");
                        String fileName = bits[bits.length - 1];

                        File myDir = new File(getExternalFilesDir(Environment.getExternalStorageDirectory().toString()) + "/senseware_sounds");

                        File file = new File(myDir, fileName);

                        if (file.exists()) {
                            file.delete();
                            count ++;
                            Log.i("deleteAudio", fileName);
                        }
                    }
                    else{
                        if(count==0){
                            day_delete = dayDelete;
                        }
                        else{

                            break;
                        }
                    }

                }while(c.moveToNext());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        finally
        {
            if(c != null)
                c.close();
            if(!c.isClosed())
                db.close();
        }

    }

    private String getDurationString(int seconds) {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return twoDigitString(minutes) + ":" + twoDigitString(seconds);
    }

    private String twoDigitString(int number) {
        if (number == 0) {
            return "00";
        }
        if (number / 10 == 0) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    public class HttpRequestSaveResult extends AsyncTask<Void, Void, String>{
        String result="";

        HttpRequestSaveResult(String s){
            result = s;
        }

        @Override
        protected String doInBackground(Void... params) {
            String resp = "";

            try{
                final String url =  getString(R.string.urlAPI) + "result";

                SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
                int id_project = settings.getInt("id_project", 0);
                String mail = settings.getString("email", "");
                String pass = settings.getString("password", "");
                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = sdf.format(c.getTime());
                String data = "{'id_project': "+ id_project +", 'id_lesson': "+ current.getId_lesson() + " , 'result': '" + result + "', 'date': '"+ date +"', 'values': [{" + utms + "}]}";

                resp = call.callPost(url, data);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return resp;
        }
    }

    public void displayNotification(){

        // Use NotificationCompat.Builder to set up our notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        //icon appears in device notification bar and right hand corner of notification
        builder.setSmallIcon(R.mipmap.sw_white);

        //sound
        // builder.setDefaults(NotificationDefaults.Sound)

        // This intent is fired when notification is clicked
        Intent intent2 = new Intent(this, LessonActivity.class);
       // intent2.addFlags( Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);


        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Large icon appears on the left of the notification
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        // Content title, which appears in large type at the top of the notification
        builder.setContentTitle("Senseware");

        // Content text, which appears in smaller text below the title
        builder.setContentText("Actualmente tienes una leccion activa");

        // The subtext, which appears under the text on newer devices.
        // This will show-up in the devices with Android 4.2 and above only
        builder.setSubText("Estas realizando la actividad "+ current.getPosition());

        builder.setAutoCancel(true);

        Notification notification = builder.build();
       // notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(current.getPosition(), notification);


    }

    public void closeNotification(){

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(current.getPosition());

    }



}
