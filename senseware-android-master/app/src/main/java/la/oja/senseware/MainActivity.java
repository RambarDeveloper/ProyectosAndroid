package la.oja.senseware;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;
import la.oja.senseware.models.Days;
import la.oja.senseware.models.Lessons;

public class MainActivity extends AppCompatActivity {

    SharedPreferences settings;
    VideoView videoView;
    ApiCall call;

    String PROJECT_NUMBER="586199636323";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        call = new ApiCall(getApplicationContext());

        //Obteniendo las UTMs
        Uri url = getIntent().getData();
        if(url != null)
        {
            for (String key : url.getQueryParameterNames()) {
                String value = url.getQueryParameter(key);
                if(value != null && value != "") {
                    SharedPreferences.Editor editor = settings.edit();
                    if (key.equals("day"))
                    {
                        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
                        SQLiteDatabase db = sDbHelper.getReadableDatabase();
                        Cursor c = null;
                        int id_day = 1;
                        String[] select_items = {sensewareDataSource.Day.COLUMN_NAME_ID_DAY};
                        String where_string = sensewareDataSource.Day.COLUMN_NAME_TITLE + " = '"+ value + "'";
                        Log.i("where", where_string);
                        c = db.query(sensewareDataSource.Day.TABLE_NAME,select_items,where_string,null,null,null,null);
                        if (c.moveToFirst()) {
                            id_day = Integer.valueOf(c.getString(0));
                        }
                        c.close();
                        db.close();
                        Log.i("DIAA", ""+ id_day);
                        editor.putInt(key, id_day);
                        editor.putInt("current", 1);
                        editor.commit();
                    } else {
                        editor.putString(key, value);
                        editor.commit();
                    }
                }
            }
        }

        new HttpRequestGetDays().execute();
        new HttpRequestGetLessons().execute();

        final int id_user = settings.getInt("id_user", 0);

        final SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        final boolean newRegisterId = prefs.getBoolean("newRegistrationID", false);

        GCMClientManager pushClientManager = new GCMClientManager(this, PROJECT_NUMBER);
        pushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {

                Log.d("Registration id",registrationId);
                //send this registrationId to your server
                if(newRegisterId && id_user!=0 && !TextUtils.isEmpty(registrationId)) {
                    saveRegistrationID(registrationId);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("newRegistrationID", false);
                    editor.commit();
                }
            }
            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
                Log.i("registrationID", "fallo");
            }
        });

        if(id_user != 0)
        {
            startActivity(new Intent(this, HeadphonesActivity.class));
        }
        else
        {
            setContentView(R.layout.activity_main);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


            saveEvent("abrioApp");

            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("current", 1);
            Log.i("DIAA2", "" + "1");
            editor.putInt("day", 1);
            editor.commit();

            Button login = (Button) findViewById(R.id.login);
            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //startActivity(new Intent(view.getContext(), HeadphonesActivity.class));

                    startActivity(new Intent(view.getContext(), LoginActivity.class));
                }
            });

            Button start = (Button) findViewById(R.id.start);
            start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(view.getContext(), RegisterActivity.class));
                }
            });

            Typeface ultralight= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
            Typeface light= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
            Typeface thin= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
            Typeface regular= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

            start.setTypeface(thin);
            login.setTypeface(thin);
            loadVideo();


        }



    }

    private void saveEvent(String event) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(c.getTime());
        String url =  getString(R.string.urlAPI) + "event/"+event;

        SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        int version = prefs.getInt("appVersion", 0);

        String utm_source = settings.getString("utm_source", "");
        String utm_medium = settings.getString("utm_medium", "");
        String utm_term = settings.getString("utm_term", "");
        String utm_content = settings.getString("utm_content", "");
        String utm_campaign = settings.getString("utm_campaign", "");
        String utms = "'app': 'Android', ";
            utms += "'version': "+version;
        String email = settings.getString("email", null);

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

        String data = "{ 'values': [{" + utms + "}]}";

        if(email!=null){

            String rid = prefs.getString("registration_id", null);

            data = "{'email': '" + email + "', 'registration_id': '"+rid+"', 'values': [{" + utms + "}]}";
        }

        ContentValues values_hook = new ContentValues();
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "event");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

        SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);
    }

    @Override
    protected void onStart() {
        super.onStart();
        int id_user = settings.getInt("id_user", 0);
        if(id_user != 0)
        {
            finish();
        }
    }

    private class HttpRequestGetLessons extends AsyncTask<Void, Void, Lessons[]> {
        @Override
        protected Lessons[] doInBackground(Void... params) {
            try {
                final String url =  getString(R.string.urlAPI) + "lessons?id_languaje=1";

                String resp = call.callGet(url);

                //convert the response from string to JsonObject
                JSONObject obj = new JSONObject(resp);
                int status = obj.getInt("status");
                String message = obj.getString("message");

                //obtained the lessons data
                ObjectMapper objectMapper = new ObjectMapper();
                JSONArray lessonData = (JSONArray) obj.get("result");

                if (status == 200 && message.equals("OK")) {
                    //get lessons to array
                    Lessons[] lessons = objectMapper.readValue(lessonData.toString(), Lessons[].class);

                    return lessons;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Lessons[] lessons)
        {
            if(lessons != null)
            {
                sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
                SQLiteDatabase db = sDbHelper.getWritableDatabase();

                String selection = "";
                String[] selectionArgs = {};
                db.delete(sensewareDataSource.Lesson.TABLE_NAME, selection, selectionArgs);

                for (int i = 0; i < lessons.length; i++) {
                    ContentValues values = new ContentValues();
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_TITLE, lessons[i].getTitle());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_BACKBUTTON, lessons[i].getBackbutton());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_COPY, lessons[i].getCopy());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_DOWNLOAD, 0);
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY, lessons[i].getId_day());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_ID_LANGUAJE, lessons[i].getId_languaje());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_ID_LESSON, lessons[i].getId_lesson());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_NEXTBUTTON, lessons[i].getNextbutton());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_POSITION, lessons[i].getPosition());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_SECONDS, lessons[i].getSeconds());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_SECTEXTFIELD, lessons[i].getSectextfield());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_SECTITLE, lessons[i].getSectitle());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_TEXTFIELD, lessons[i].getTextfield());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_SUBTITLE, lessons[i].getSubtitle());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_SRC, lessons[i].getSrc());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_DATE_UPDATE, lessons[i].getDate_update());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_COUNTBACK, lessons[i].getCountback());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_GROUP_ALL, lessons[i].getGroup_all());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_SELECT_TEXT, lessons[i].getSelect_text());
                    values.put(sensewareDataSource.Lesson.COLUMN_NAME_GETBACK, lessons[i].getGetback());
                    // Insert the new row, returning the primary key value of the new row
                    long newRowId;
                    newRowId = db.insert(sensewareDataSource.Lesson.TABLE_NAME, null, values);
                }
                db.close();
            }
        }
    }

    private class HttpRequestGetDays extends AsyncTask<Void, Void, Days[]> {
        @Override
        protected Days[] doInBackground(Void... params) {
            try {
                final String url = getString(R.string.urlAPI) + "day";

                String resp = call.callGet(url);

                //convert the response from string to JsonObject
                JSONObject obj = new JSONObject(resp);
                int status = obj.getInt("status");
                String message = obj.getString("message");

                //obtained the lessons data
                ObjectMapper objectMapper = new ObjectMapper();
                JSONArray daysData = (JSONArray) obj.get("result");

                if (status == 200 && message.equals("OK")) {
                    //get lessons to array
                    Days[] days = objectMapper.readValue(daysData.toString(), Days[].class);

                    return days;


                }

            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Days[] days) {
            if (days != null)
            {
                sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
                SQLiteDatabase db = sDbHelper.getWritableDatabase();
                db.execSQL("delete from " + sensewareDataSource.Day.TABLE_NAME);

                for (int i = 0; i < days.length; i++) {
                    ContentValues values = new ContentValues();
                    values.put(sensewareDataSource.Day.COLUMN_NAME_ID_DAY, days[i].getId_day());
                    values.put(sensewareDataSource.Day.COLUMN_NAME_DAY, days[i].getDay());
                    values.put(sensewareDataSource.Day.COLUMN_NAME_TITLE, days[i].getTitle());
                    values.put(sensewareDataSource.Day.COLUMN_NAME_VISIBLECLASSES, days[i].getVisibleclasses());
                    values.put(sensewareDataSource.Day.COLUMN_NAME_VISIBLE, days[i].getVisible());

                    long newRowId;
                    newRowId = db.insert(sensewareDataSource.Day.TABLE_NAME, null, values);
                }
                db.close();
            }
        }
    }


    public void loadVideo(){
        VideoView videoView = (VideoView) findViewById(R.id.video);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.senseware;
        videoView.setVideoURI(Uri.parse(path));
        //loop
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
        videoView.requestFocus();
    }


    public void saveRegistrationID(String registrationId){

        String url =  getString(R.string.urlAPI) +"appinstall";

        String mail = settings.getString("email", "");
        String pass = settings.getString("password", "");
        int id_user = settings.getInt("id_user", 0);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String date = sdf.format(c.getTime());

        String data = "{'id_user': "+id_user+", 'registration_id': '"+registrationId+"', 'platform':'Android', 'date_install':'"+date+"'}";

        ContentValues values_hook = new ContentValues();
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "appinstall");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

        SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);

    }

}