package la.oja.senseware;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;
import la.oja.senseware.models.Lessons;
import la.oja.senseware.models.Project;
import la.oja.senseware.models.Result;

public class ProgramActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences settings;
    public static Lessons current;
    public String[] titleArray;
    public static Lessons[] lessonList;
    public int day;
    HttpRequestGetAudios objDownloadAudios;
    public static int pos;
    DrawerLayout drawer;
    boolean vioMenu = false;
    private NavigationView navigationView;
    Button avanzar;
    int nextDay;
    TextView contacto;
    ApiCall call;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
        call = new ApiCall(getApplicationContext());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        settings = getSharedPreferences("ActivitySharedPreferences_data", 0);

        nextDay = settings.getInt("nextDay", 0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String title = getTitleToolbar();
        getSupportActionBar().setTitle(title);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){

            /*@Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                saveEvent("vioMenuDiasApp");
                super.onDrawerSlide(drawerView, slideOffset);
            }*/

            @Override
            public void onDrawerOpened(View drawerView) {
                //saveEvent("vioMenuDiasApp");
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                vioMenu = false;
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    if (!vioMenu){
                        //saveEvent("vioMenuDiasApp");
                    } else {
                        vioMenu = false;
                    }
                }
                super.onDrawerStateChanged(newState);
            }
        };

        drawer.setDrawerListener(toggle);

        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] {android.R.attr.state_checked}, // checked
                new int[] { android.R.attr.state_pressed}  // pressed

        };

        int[] colors = new int[] {
                Color.parseColor("#777777"),
                Color.parseColor("#777777"),
                Color.parseColor("#333333"),
                Color.parseColor("#333333"),
                Color.parseColor("#333333")

        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        navigationView.setItemTextColor(colorStateList);

        navigationView.setItemIconTintList(null);

        //update data of the profile
        String email = settings.getString("email", " ");
        String phoneNumber = settings.getString("phone", " ");

        View header = navigationView.getHeaderView(0);

        TextView mail = (TextView) header.findViewById(R.id.email);
        mail.setText(email);

        TextView phone = (TextView) header.findViewById(R.id.phone);
        phone.setText(phoneNumber);


        Typeface ultralight = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        getActionBarTextView(toolbar).setTypeface(ultralight);

        avanzar = (Button) findViewById(R.id.avanzar);
        avanzar.setTypeface(thin);

        contacto = (TextView) findViewById(R.id.contacto);
        contacto.setTypeface(thin);
        contacto.setVisibility(View.GONE);

        new HttpRequestGetProjects().execute();

        new HttpRequestGetResults().execute();

        this.day = settings.getInt("day", 1);
        int max_day= settings.getInt("max_day", 0);

        int idItem = 0;

        if(day==5)
            idItem = 3;
        else
            idItem = day - 1;

        Log.i("idItem", String.valueOf(idItem));

        if(idItem >= 0 && idItem < 4)
        {
            navigationView.getMenu().getItem(idItem).setChecked(true);
        }

        if(day == max_day)
        {
            navigationView.getMenu().getItem(0).setChecked(false);
            navigationView.getMenu().findItem(R.id.maxClass).getSubMenu().findItem(R.id.cclass).setChecked(true);
        }


        Log.i("CARGAR DIA", String.valueOf(day));
        loadListFromBdOrApi(day);

      //  if(nextDay != 0 && nextDay != 6){
        if(nextDay != 0 ){
            String text = "";
            String nextTitle = settings.getString("nextDayTitle", null);
            if(nextDay < 5)
                text =  "¿Quieres avanzar a la clase "+ nextTitle +"?";
            else  {
                int newTitle = Integer.valueOf(nextTitle) - 1;
                text = "¿Quieres avanzar a la clase " + newTitle + "?";
            }


            ListView list = (ListView) findViewById(R.id.list);
            int currentLesson = settings.getInt("current", 0);
            list.setSelection(currentLesson - 1);

            avanzar.setText(text);
            avanzar.setVisibility(View.VISIBLE);
            contacto.setVisibility(View.GONE);
        }
        else
        {

            avanzar.setVisibility(View.GONE);

        }

        /*
        if(nextDay==6){
            contacto.setText("Tú asesor te estara contactando, para continuar con las clases avanzadas");
            contacto.setVisibility(View.VISIBLE);
        }
        */

        avanzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("current", 1);
                editor.putInt("day", nextDay);
                editor.putInt("nextDay", 0);
                editor.commit();
                avanzar.setVisibility(View.GONE);

                loadListFromBdOrApi(nextDay);

                if (nextDay < 6) {
                    if (nextDay == 5)
                        navigationView.getMenu().getItem(3).setChecked(true);
                    else
                        navigationView.getMenu().getItem(nextDay - 1).setChecked(true);
                }

                String title = getTitleToolbar();
                getSupportActionBar().setTitle(title);

            }
        });


        if(max_day < day && day != 9)
        {
            SharedPreferences.Editor editor = settings.edit();
            int clase = settings.getInt("current", 1);

            editor.putInt("max_day", day);
            editor.putInt("max_current", clase);
            editor.commit();
        }

        //get data of class current
        String text = getTitleMaxLesson();
        /*Log.i("title", text);
        Menu menu = navigationView.getMenu();
        menu.add(R.id.cclass, 100, 100, text);
        menu.setGroupVisible(R.id.cclass, true);*/

        navigationView.getMenu().findItem(R.id.cclass).setTitle(text);

        setIconsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onContentChanged();
        startService(getIntent());

    }

    private void setIconsMenu(){

        int day= settings.getInt("max_day", 0);

        if(day >=5) {
                navigationView.getMenu().findItem(R.id.d1).setIcon(R.mipmap.check);
                navigationView.getMenu().findItem(R.id.d2).setIcon(R.mipmap.check);
                navigationView.getMenu().findItem(R.id.d3).setIcon(R.mipmap.check);
                navigationView.getMenu().findItem(R.id.d4).setIcon(R.mipmap.check);

        }
        else if(day ==2){
            navigationView.getMenu().findItem(R.id.d1).setIcon(R.mipmap.check);
            navigationView.getMenu().findItem(R.id.d2).setIcon(R.mipmap.play);

        }
        else if(day==3) {
            navigationView.getMenu().findItem(R.id.d1).setIcon(R.mipmap.check);
            navigationView.getMenu().findItem(R.id.d2).setIcon(R.mipmap.check);
            navigationView.getMenu().findItem(R.id.d3).setIcon(R.mipmap.play);
        }

    }

    private String getTitleMaxLesson()
    {
        String title = "Clase ";

        int max_day = settings.getInt("max_day", 0);
        int pos = settings.getInt("max_current", 0);

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getReadableDatabase();

        Cursor c = null;

        String[] select_items = {sensewareDataSource.Day.COLUMN_NAME_TITLE};
        String where_string = sensewareDataSource.Day.COLUMN_NAME_ID_DAY + " = '" + max_day + "'";

        SharedPreferences.Editor editor = settings.edit();
        c = db.query(sensewareDataSource.Day.TABLE_NAME,select_items,where_string,null,null,null,null);
        if (c.moveToFirst())
        {
            title += c.getString(0);
        }
        else
        {
            editor.putInt("max_day", day);
            editor.commit();
            title += "1";
        }

        String[] projection = {
                sensewareDataSource.Lesson._ID,
                sensewareDataSource.Lesson.COLUMN_NAME_TITLE,
                sensewareDataSource.Lesson.COLUMN_NAME_POSITION,
        };

        String whereCol = sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY + " = " + String.valueOf(max_day) + " AND " +sensewareDataSource.Lesson.COLUMN_NAME_POSITION + " = " + String.valueOf(pos);

        c = null;

        c = db.query(
                sensewareDataSource.Lesson.TABLE_NAME,      // The table to query
                projection,                                 // The columns to return
                whereCol,                                   // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                null                                        // The sort order
        );

        if (c.moveToFirst())
        {
            title += " - " + c.getString(1);
        }
        else
        {
            title += " - Actividad 1";
            editor.putInt("max_current", 1);
            editor.commit();
        }

        c.close();
        db.close();

        return title;
    }

    private String getTitleToolbar() {
        settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        int currentDay = settings.getInt("day", 0);
        String title = null;

        /* sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getReadableDatabase();

        String[] projection = {
                sensewareDataSource.Day._ID,
                sensewareDataSource.Day.COLUMN_NAME_TITLE,

        };

        String whereCol = sensewareDataSource.Day.COLUMN_NAME_ID_DAY + " = " + String.valueOf(currentDay);

        Cursor c = db.query(
                sensewareDataSource.Day.TABLE_NAME,      // The table to query
                projection,                                 // The columns to return
                whereCol,                                   // The columns for the WHERE clause
                null,                                       // The values for the WHERE clause
                null,                                       // don't group the rows
                null,                                       // don't filter by row groups
                null                                        // The sort order
        );

        if (c.moveToFirst()) {
            title = c.getString(1);

        }
        */

        switch (currentDay) {
            case 1:
                title = "Aterriza tu idea";
            break;
            case 2:
                title = "Monetización";
                break;
            case 3:
                title = "Administración del tiempo";
                break;
            case 5:
                title = "¿Cómo empezar?";
                break;
            case 9:
                title = "SOS";
                break;
            default:
                title = "Senseware";
                break;
        }

        return title;
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        boolean vioclase = settings.getBoolean("vioclase", false);
        boolean newProject = settings.getBoolean("newProject", false);
        int currentDay = settings.getInt("day", 0);
        nextDay = settings.getInt("nextDay", 0);
        int max_day = settings.getInt("max_day", 0);

        int idItem = 0;

        if(currentDay == 5)
            idItem = 3;
        else
            idItem = currentDay - 1;

        if(idItem >= 0 && idItem < 4){
            navigationView.getMenu().getItem(idItem).setChecked(true);
        }

        if(day == max_day)
        {
            navigationView.getMenu().getItem(0).setChecked(false);
            navigationView.getMenu().findItem(R.id.maxClass).getSubMenu().findItem(R.id.cclass).setChecked(true);
        }

        String title = getTitleToolbar();
        getSupportActionBar().setTitle(title);
        setIconsMenu();

        if (currentDay != this.day) {
            if(currentDay > this.day && currentDay != 9)
                this.day = currentDay;

            loadListFromBdOrApi(currentDay);
        }
        else if(newProject == true) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("newProject", false);
            editor.commit();

            loadList();
        }
        else if (vioclase == true) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("vioclase", false);
            editor.commit();
            loadList();
        }

        if(nextDay !=0 ){
            String text;
            String nextTitle = settings.getString("nextDayTitle", null);
            if(nextDay <5)
                text =  "¿Quieres avanzar a la clase "+ nextTitle +"?";
            else {
                int newTitle = Integer.valueOf(nextTitle) - 1;
                text = "¿Quieres avanzar a la clase " + newTitle + "?";
            }
            avanzar.setText(text);
            avanzar.setVisibility(View.VISIBLE);
        }

        /*
        if(nextDay==6){
            avanzar.setVisibility(View.GONE);
            contacto.setText("Tú asesor te estara contactando, para continuar con las clases avanzadas");
            contacto.setVisibility(View.VISIBLE);
        }
        */

        String text = getTitleMaxLesson();
        navigationView.getMenu().findItem(R.id.cclass).setTitle(text);
    }

    private void loadListFromBdOrApi(int id_day) {

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getReadableDatabase();

        Cursor c = null;
        try
        {
            /*String[] select_items = {sensewareDataSource.Day.COLUMN_NAME_ID_DAY};
            String where_string = sensewareDataSource.Day.COLUMN_NAME_TITLE + " = '"+ title_day+"'";
            Log.i("where", where_string);
            c = db.query(sensewareDataSource.Day.TABLE_NAME,select_items,where_string,null,null,null,null);
            if (c.moveToFirst()) {
                id_day = Integer.valueOf(c.getString(0));
            }*/

            // Define a projection that specifies which columns from the database
            // you will actually use after this query.
            String[] projection = {
                    sensewareDataSource.Lesson._ID,
                    sensewareDataSource.Lesson.COLUMN_NAME_TITLE,
                    sensewareDataSource.Lesson.COLUMN_NAME_SUBTITLE,
                    sensewareDataSource.Lesson.COLUMN_NAME_SRC,
                    sensewareDataSource.Lesson.COLUMN_NAME_ID_LESSON,
                    sensewareDataSource.Lesson.COLUMN_NAME_ID_LANGUAJE,
                    sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY,
                    sensewareDataSource.Lesson.COLUMN_NAME_POSITION,
                    sensewareDataSource.Lesson.COLUMN_NAME_COPY,
                    sensewareDataSource.Lesson.COLUMN_NAME_SECONDS,
                    sensewareDataSource.Lesson.COLUMN_NAME_SECTITLE,
                    sensewareDataSource.Lesson.COLUMN_NAME_NEXTBUTTON,
                    sensewareDataSource.Lesson.COLUMN_NAME_BACKBUTTON,
                    sensewareDataSource.Lesson.COLUMN_NAME_TEXTFIELD,
                    sensewareDataSource.Lesson.COLUMN_NAME_SECTEXTFIELD,
                    sensewareDataSource.Lesson.COLUMN_NAME_DATE_UPDATE,
                    sensewareDataSource.Lesson.COLUMN_NAME_COUNTBACK,
                    sensewareDataSource.Lesson.COLUMN_NAME_GROUP_ALL,
                    sensewareDataSource.Lesson.COLUMN_NAME_SELECT_TEXT,
                    sensewareDataSource.Lesson.COLUMN_NAME_GETBACK
            };

            String whereCol = sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY + " = "+ String.valueOf(id_day);

            String sortOrder = sensewareDataSource.Lesson._ID + " DESC";

            c = db.query(
                    sensewareDataSource.Lesson.TABLE_NAME,      // The table to query
                    projection,                                 // The columns to return
                    whereCol,                                   // The columns for the WHERE clause
                    null,                                // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    null                                        // The sort order
            );

            //Nos aseguramos de que existe al menos un registro
            if (c.moveToFirst())
            {
                lessonList = new Lessons[c.getCount()];
                titleArray = new String[c.getCount()];
                int i = 0;
                do {
                    titleArray[i] = c.getString(1);
                    Lessons les = new Lessons(c.getString(1),
                            c.getString(2),
                            c.getString(3),
                            c.getInt(4),
                            c.getInt(5),
                            c.getInt(6),
                            c.getInt(7),
                            c.getInt(8),
                            c.getInt(9),
                            c.getInt(10),
                            c.getInt(11),
                            c.getInt(12),
                            c.getInt(13),
                            c.getInt(14),
                            c.getString(15),
                            c.getInt(16),
                            c.getInt(17),
                            c.getInt(18),
                            c.getString(19)
                    );

                    lessonList[i] = les;
                    i++;
                } while (c.moveToNext());

                c.close();
                db.close();
                loadList();
            }
            else
            {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("current", id_day);
                editor.commit();

                new HttpRequestGetLessons(id_day, false).execute();
            }
        }
        catch (Exception e)
        {
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

    private TextView getActionBarTextView(Toolbar toolbar) {
        TextView titleTextView = null;

        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
        return titleTextView;
    }

    @Override
    public void onBackPressed() {
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
            this.finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.program, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //saveEvent("vioMenu");

        final SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        final SharedPreferences.Editor editor = settings.edit();

        if(id == R.id.history )
        {
            Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
            startActivity(intent);
        }

        if(id == R.id.my_projects )
        {
            Intent intent = new Intent(getApplicationContext(), MyProjectsActivity.class);
            startActivity(intent);
        }

        if(id == R.id.new_project)
        {
            Intent intent = new Intent(getApplicationContext(), NewProjectActivity.class);
            startActivity(intent);
        }

        if(id == R.id.restart)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(ProgramActivity.this).create();
            alertDialog.setTitle("Senseware");
            alertDialog.setMessage("Esta seguro de reiniciar el día");
            alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int POSITIVE) {

                    editor.putInt("current", 1);
                    editor.commit();
                    Intent intent = getIntent();
                    ProgramActivity.this.finish();
                    startActivity(intent);
                    dialog.cancel();

                }
            });
            alertDialog.setButton(-2, "Cancelar", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int NEGATIVE) {
                    dialog.cancel();
                }
            });
            alertDialog.setIcon(R.mipmap.sw_black);
            alertDialog.show();
        }

        if(id== R.id.clear_cache)
        {
            deleteAllAudios();

            AlertDialog alertDialog = new AlertDialog.Builder(ProgramActivity.this).create();
            alertDialog.setTitle("Senseware");
            alertDialog.setMessage("El cache se ha limpiado correctamente");
            alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int POSITIVE) {

                    Intent intent = getIntent();
                    ProgramActivity.this.finish();
                    startActivity(intent);
                    dialog.cancel();

                }
            });
            alertDialog.setIcon(R.mipmap.sw_black);
            alertDialog.show();
        }

        if (id == R.id.logout) {
            editor.remove("id_user");
            editor.remove("email");
            editor.remove("phone");
            editor.remove("nextDay");
            editor.remove("vioclase");
            editor.remove("newProject");
            editor.remove("current");
            editor.remove("day");
            editor.remove("max_day");
            editor.remove("max_current");
            editor.commit();

            sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
            SQLiteDatabase db = sDbHelper.getWritableDatabase();

            db.delete(sensewareDataSource.Result.TABLE_NAME, null, null);
            db.delete(sensewareDataSource.Project.TABLE_NAME, null, null);
            db.delete(sensewareDataSource.History.TABLE_NAME, null, null);
            db.delete(sensewareDataSource.User.TABLE_NAME, null, null);

            db.close();

            this.finish();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       //saveEvent("vioMenuDias");

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        SharedPreferences.Editor editor = settings.edit();
        int currentDay = settings.getInt("day", 0);
        int max_day = settings.getInt("max_day", 0);
        int max_current = settings.getInt("max_current", 1);
        boolean dayInactive = false;
        String message = null;

        int nextDay = settings.getInt("nextDay", 0);

        if (id == R.id.d1) {
            if(objDownloadAudios != null)
                objDownloadAudios.cancel(true);
            editor.putInt("current", 1);
            editor.commit();
            editor.putInt("day", 1);
            editor.commit();
            avanzar.setVisibility(View.GONE);
            contacto.setVisibility(View.GONE);
            loadListFromBdOrApi(1);
        } else if (id == R.id.d2) {
            if(max_day < 2 && nextDay != 2)
            {
                message = "Upps. Necesitamos que termines la clase de Aterriza tu idea, para acceder a estas clases";
                dayInactive = true;
                item.setCheckable(false);
            }
            else {
                if (objDownloadAudios != null)
                    objDownloadAudios.cancel(true);

                if(max_day==2)
                    editor.putInt("current", max_current);
                else
                    editor.putInt("current", 1);

                editor.putInt("day", 2);
                editor.commit();
                avanzar.setVisibility(View.GONE);
                contacto.setVisibility(View.GONE);
                loadListFromBdOrApi(2);
            }
        } else if (id == R.id.d3) {
            if(max_day < 3 && nextDay !=3)
            {
                message = "Upps. Necesitamos que termines la clase de Monetización, para acceder a estas clases";
                dayInactive = true;
                item.setCheckable(false);

            }else {
                if (objDownloadAudios != null)
                    objDownloadAudios.cancel(true);

                if(max_day==3)
                    editor.putInt("current", max_current);
                else
                    editor.putInt("current", 1);

                editor.putInt("day", 3);
                editor.commit();
                avanzar.setVisibility(View.GONE);
                contacto.setVisibility(View.GONE);
                loadListFromBdOrApi(3);
            }
        } else if (id == R.id.d4) {
            if(max_day < 5 && nextDay !=5 )
            {
                message = "Upps. Necesitamos que termines la clase de Administración del tiempo, para acceder a estas clases";
                dayInactive = true;
                item.setCheckable(false);
            }
            else {
                if (objDownloadAudios != null)
                    objDownloadAudios.cancel(true);

                if(max_day==5)
                    editor.putInt("current", max_current);
                else
                    editor.putInt("current", 1);

                editor.putInt("day", 5);
                editor.commit();
                avanzar.setVisibility(View.GONE);
                contacto.setVisibility(View.GONE);
                loadListFromBdOrApi(5);
            }
        }else if(id == R.id.cclass)
        {
            editor.putInt("current", max_current);
            editor.putInt("day", max_day);
            editor.commit();
            avanzar.setVisibility(View.GONE);
            contacto.setVisibility(View.GONE);
            item.setCheckable(true);
            loadListFromBdOrApi(max_day);
        }

        if(dayInactive) {
            AlertDialog alertDialog = new AlertDialog.Builder(ProgramActivity.this).create();
            alertDialog.setTitle("Senseware");
            alertDialog.setMessage(message);
            alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int POSITIVE) {
                    dialog.cancel();

                }
            });
            alertDialog.setIcon(R.mipmap.sw_black);
            alertDialog.show();
        }

        String title = getTitleToolbar();
        getSupportActionBar().setTitle(title);

        String text = getTitleMaxLesson();
        navigationView.getMenu().findItem(R.id.cclass).setTitle(text);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static Lessons getCurrent() {
        return current;
    }

    public static void updateCurrent() {
        pos++;
        current = lessonList[pos];
    }

    private boolean compareObjects(Lessons[] lessons, Lessons[] lessonList) {
        boolean equals = true;
        if (lessons != null && lessonList != null && lessonList.length == lessons.length) {
            for (int i = 0; i < lessonList.length; i++) {
                if (lessons[i].getTitle() == null || lessonList[i].getTitle() == null || lessonList[i].getTitle().compareTo(lessons[i].getTitle()) != 0 ||
                        lessonList[i].getSubtitle().compareTo(lessons[i].getSubtitle()) != 0 ||
                        lessonList[i].getSrc().compareTo(lessons[i].getSrc()) != 0 ||
                        lessonList[i].getId_lesson() != lessons[i].getId_lesson() ||
                        lessonList[i].getId_day() != lessons[i].getId_day() ||
                        lessonList[i].getId_languaje() != lessons[i].getId_languaje() ||
                        lessonList[i].getSeconds() != lessons[i].getSeconds() ||
                        lessonList[i].getPosition() != lessons[i].getPosition()
                        ) {

                    equals = false;
                    break;
                }
            }
        } else {

            equals = false;
        }
        return equals;
    }

    public void loadList() {
        Typeface ultralight = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        customListAdapter adapter = new customListAdapter(ProgramActivity.this, titleArray, ultralight);
        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
        int currentLesson = settings.getInt("current", 0);
        list.setSelection(currentLesson - 1);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int currentLesson = settings.getInt("current", 0);
                int max_day = settings.getInt("max_day", 1);
                String message = "";
                if (position == (currentLesson - 1)) {
                    pos = position;
                    current = lessonList[+position];
                    view.setEnabled(false);
                    Intent intent = new Intent(view.getContext(), LessonActivity.class);
                    intent.putExtra("clases", lessonList.length);
                    startActivity(intent);
                } else if (position < (currentLesson - 1)) {
                    //message = "Esta clase ya la viste, continua con la clase que tienes activa";
                    pos = position;
                    current = lessonList[+position];
                    view.setEnabled(false);
                    Intent intent = new Intent(view.getContext(), LessonActivity.class);
                    intent.putExtra("clases", lessonList.length);
                    startActivity(intent);
                } else {
                    message = "Aun no puedes llevar a cabo esta actividad.";

                    AlertDialog alertDialog = new AlertDialog.Builder(ProgramActivity.this).create();
                    alertDialog.setTitle("Senseware");
                    alertDialog.setMessage(message);
                    alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int POSITIVE) {
                            //startActivity(new Intent(ProgramActivity.this, ProgramActivity.class));
                            dialog.cancel();
                            //finish();
                        }
                    });
                    alertDialog.setIcon(R.mipmap.sw_black);
                    alertDialog.show();
                }
            }
        });

        if (objDownloadAudios != null)
            objDownloadAudios.cancel(true);

        objDownloadAudios = new HttpRequestGetAudios(lessonList, false);
        objDownloadAudios.execute();
    }


    private class HttpRequestGetLessons extends AsyncTask<Void, Void, Lessons[]> {
        public int id_day;
        public boolean background;
        ProgressDialog progress;
        Lessons[] lessons_local;

        public HttpRequestGetLessons(int id_day, boolean background) {
            this.id_day = id_day;
            this.background = background;
            if (!background){
                progress = ProgressDialog.show(ProgramActivity.this, "Senseware", "Descargando clases...", true);
            }
        }

        @Override
        protected Lessons[] doInBackground(Void... params) {
            try {

                String mail = settings.getString("email", "");
                String pass = settings.getString("password", "");

                final String url =  getString(R.string.urlAPI) + "lessons?id_languaje=1&id_day=" + String.valueOf(id_day);

                String resp = call.callGet(url);

                //convert the response from string to JsonObject
                JSONObject obj = new JSONObject(resp);
                int status = obj.getInt("status");
                String message = obj.getString("message");

                if (status == 200 && message.equals("OK"))
                {
                    //obtained the lessons data
                    ObjectMapper objectMapper = new ObjectMapper();
                    JSONArray lessonData = (JSONArray) obj.get("result");

                    //get lessons to array
                    Lessons[] lessons = objectMapper.readValue(lessonData.toString(), Lessons[].class);
                    return lessons;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Lessons[] lessons) {
            if (!background){
                progress.dismiss();
            }

            if (lessons != null && !compareObjects(lessons, lessonList))
            {
                sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
                SQLiteDatabase db = sDbHelper.getWritableDatabase();

                String selection = sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY + " = ?";
                String[] selectionArgs = {String.valueOf(id_day)};
                db.delete(sensewareDataSource.Lesson.TABLE_NAME, selection, selectionArgs);

                if(!background) {
                    lessonList = new Lessons[lessons.length];
                    titleArray = new String[lessons.length];
                }else{
                    lessons_local = new Lessons[lessons.length];
                }
                for (int i = 0; i < lessons.length; i++) {

                    if(!background){
                        titleArray[i] = lessons[i].getTitle();
                        lessonList[i] = lessons[i];
                    }else{
                        lessons_local[i] = lessons[i];
                    }

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

                if(!background)
                {
                    loadList();
                }
                else
                {
                    if (objDownloadAudios != null)
                        objDownloadAudios.cancel(true);

                    objDownloadAudios = new HttpRequestGetAudios(lessons_local, background);
                    objDownloadAudios.execute();
                }
            }
        }
    }

    private class HttpRequestGetAudios extends AsyncTask<Void, Void, Lessons[]> {
        public Lessons[] lessons;
        public int cursor_activity;
        public boolean background;

        public HttpRequestGetAudios(Lessons[] lessons, boolean backgroud) {
            this.lessons = lessons;
            this.cursor_activity = 0;
            this.background = backgroud;
        }

        @Override
        protected Lessons[] doInBackground(Void... params) {
            getFile(lessons[cursor_activity].getSrc());
            return lessons;
        }

        public String getFile(String url) {
            final String soundUrl = url;
            String[] bits = soundUrl.split("/");
            final String fileName = bits[bits.length - 1];
            String root = Environment.getExternalStorageDirectory().toString();
          //  final File myDir = new File(root + "/senseware_sounds");
            final File myDir = new File (getExternalFilesDir(Environment.getExternalStorageDirectory().toString()) + "/senseware_sounds");
            myDir.mkdirs();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        File file = new File(myDir, fileName);
                        if (!file.exists())
                        {
                            long space = myDir.getUsableSpace();

                            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());

                            file = new File(myDir, fileName + "_tmp");
                                URL url = new URL(soundUrl);
                                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                                c.setRequestMethod("GET");
                                c.setDoOutput(true);
                                c.connect();

                                InputStream is = c.getInputStream();
                                OutputStream os = new FileOutputStream(file);

                            long sizeFile = is.available();

                            if(space > 0 && space >= sizeFile) {
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = is.read(buffer)) != -1) {
                                    os.write(buffer, 0, length);
                                }

                                is.close();
                                os.close();

                                File file_def = new File(myDir, fileName);
                                file.renameTo(file_def);
                            }
                            else{
                                if(day > 2)
                                    deleteAudios();
                            }
                        }


                        if (cursor_activity + 1 < lessons.length && lessons[cursor_activity + 1] != null)
                        {
                            cursor_activity++;
                            getFile(lessons[cursor_activity].getSrc());
                        }
                        else
                        {
                            int id_day = lessons[cursor_activity].getId_day();
                            //loading featured lessons

                            if (id_day == 3) {
                                new HttpRequestGetLessons(5, true).execute();
                            }else if (id_day < 5) {
                                new HttpRequestGetLessons(id_day + 1, true).execute();

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if(day>2)
                            deleteAudios();
                        else
                            objDownloadAudios.cancel(true);


                    }
                }
            }).start();

            return myDir + "/" + fileName;
        }
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
                        else
                            break;
                    }

                }while(c.moveToNext());
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void deleteAllAudios() {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();
        Cursor c = null;

        try {
            String[] projection = {
                    sensewareDataSource.Lesson._ID,
                    sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY,
                    sensewareDataSource.Lesson.COLUMN_NAME_SRC,
            };

            String whereCol = sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY + " > 1 ";

            c = db.query(
                    sensewareDataSource.Lesson.TABLE_NAME,      // The table to query
                    projection,                                 // The columns to return
                    whereCol,                                   // The columns for the WHERE clause
                    null,                                       // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    null                                        // The sort order
            );


            if (c.moveToFirst()){

                do{
                        String src = c.getString(2);
                        String[] bits = src.split("/");
                        String fileName = bits[bits.length - 1];

                        File myDir = new File(getExternalFilesDir(Environment.getExternalStorageDirectory().toString()) + "/senseware_sounds");

                        File file = new File(myDir, fileName);

                        if (file.exists()) {
                            file.delete();
                            Log.i("deleteAudio", fileName);
                        }

                }while(c.moveToNext());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            db.close();
            c.close();
        }

    }

    private void saveEvent(String event)
    {
        vioMenu = true;

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(c.getTime());
        String url = getString(R.string.urlAPI) + "event/"+event;

        String utm_source = settings.getString("utm_source", "");
        String utm_medium = settings.getString("utm_medium", "");
        String utm_term = settings.getString("utm_term", "");
        String utm_content = settings.getString("utm_content", "");
        String utm_campaign = settings.getString("utm_campaign", "");
        String utms = "app : 'Android'";
        String email = settings.getString("email", null);

        if(utm_source.compareTo("") != 0)
            utms += ", utm_source: '" + utm_source + "'";
        if(utm_medium.compareTo("") != 0)
            utms += ", utm_medium: '" + utm_medium + "'";
        if(utm_term.compareTo("") != 0)
            utms += ", utm_term: '" + utm_term + "'";
        if(utm_content.compareTo("") != 0)
            utms += ", utm_content: '" + utm_content + "'";
        if(utm_campaign.compareTo("") != 0)
            utms += ", utm_campaign: '" + utm_campaign + "'";

        String data = "{email: '" + email + "', values: [{" + utms + "}]}";

        ContentValues values_hook = new ContentValues();
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "event");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

        SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);
    }

    private class HttpRequestGetProjects extends AsyncTask<Void, Void, Project[]> {
        @Override
        protected Project[] doInBackground(Void... params) {

            try {
                int id_user = settings.getInt("id_user", 0);
                final String url =  getString(R.string.urlAPI) + "project?id_user="+id_user;
                String resp = call.callGet(url);

                 //convert the response from string to JsonObject
                JSONObject obj = new JSONObject(resp);
                int status = obj.getInt("status");
                String message = obj.getString("message");

                //obtained the lessons data
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                objectMapper.setDateFormat(dateFormat);
                JSONArray projectData = (JSONArray) obj.get("result");

                if (status == 200 && message.equals("OK")) {
                    //get lessons to array
                    Project[] project = objectMapper.readValue(projectData.toString(), Project[].class);

                    return project;
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Project[] projects) {
            //super.onPostExecute(projects);
            if (projects != null) {
               projectsReg(projects);
            }
        }
    }

    private void projectsReg(Project[] projects) {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        int id_user = settings.getInt("id_user", 0);
        Cursor c = null;
        Project[]  projectsBD = null;

        try {
            String[] projection = {
                    sensewareDataSource.Project._ID,
                    sensewareDataSource.Project.COLUMN_NAME_ID_PROJECT,
                    sensewareDataSource.Project.COLUMN_NAME_ID_USER,
                    sensewareDataSource.Project.COLUMN_NAME_NA_PROJECT,
                    sensewareDataSource.Project.COLUMN_NAME_CREATED
            };

            String whereCol = sensewareDataSource.Project.COLUMN_NAME_ID_USER+ " = " + String.valueOf(id_user);

            c = db.query(
                    sensewareDataSource.Project.TABLE_NAME,      // The table to query
                    projection,                                 // The columns to return
                    whereCol,                                   // The columns for the WHERE clause
                    null,                                       // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    null                                        // The sort order
            );

            if (c.moveToFirst()) {
                projectsBD = new Project[c.getCount()];
                int i = 0;
                do {
                    Project p = new Project(c.getInt(1), c.getInt(2), c.getString(3), c.getString(4));

                    projectsBD[i] = p;

                    i++;

                } while (c.moveToNext());

                c.close();
                db.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(c != null && !c.isClosed())
            {
                c.close();
                db.close();
            }
        }

        if(projectsBD!=null) {
            for (int i = 0; i < projects.length; i++) {
                boolean existProject = false;

                for(int j=0; j<projectsBD.length; j++) {
                   if(projectsBD[j].getId_project() == projects[i].getId_project())
                        existProject = true;
                }

                //Genera excepcion revisando
                if(!existProject) {
                    Project[] aux = new Project[1];
                    Project p = new Project(projects[i].getId_project(), projects[i].getId_user(), projects[i].getNa_project(), projects[i].getCreate().toString());
                    aux[0] = p;
                    savePorjects(aux);
                }
            }
        }
        else {
            //guardamos todos los pryectos en BD
            savePorjects(projects);
        }
    }

    public void savePorjects(Project[] projects) {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        for (int i = 0; i < projects.length; i++) {
            ContentValues values = new ContentValues();
            values.put(sensewareDataSource.Project.COLUMN_NAME_ID_PROJECT, projects[i].getId_project());
            values.put(sensewareDataSource.Project.COLUMN_NAME_NA_PROJECT, projects[i].getNa_project());
            values.put(sensewareDataSource.Project.COLUMN_NAME_ID_USER, projects[i].getId_user());
            values.put(sensewareDataSource.Project.COLUMN_NAME_ID_TMP, 0);
            values.put(sensewareDataSource.Project.COLUMN_NAME_CREATED, projects[i].getCreate());

            long newRowId;
            newRowId = db.insert(sensewareDataSource.Project.TABLE_NAME, null, values);
        }

        db.close();
    }


    private class HttpRequestGetResults extends AsyncTask<Void, Void, Result[]>{
        @Override
        protected Result[] doInBackground(Void... params) {
            try {
                int id_project = settings.getInt("id_project", 0);
                final String url = getString(R.string.urlAPI) + "result?id_project=" + id_project;
                String resp = call.callGet(url);

                //convert the response from string to JsonObject
                JSONObject obj = new JSONObject(resp);
                int status = obj.getInt("status");
                String message = obj.getString("message");

                //obtained the results data
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                objectMapper.setDateFormat(dateFormat);
                JSONArray data = (JSONArray) obj.get("result");

                if (status == 200 && message.equals("OK")) {
                    //get lessons to array
                    Result[] results = objectMapper.readValue(data.toString(), Result[].class);

                    return results;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            return new Result[0];
        }

        @Override
        protected void onPostExecute(Result[] results) {
           // super.onPostExecute(results);

            if(results!=null)
                resultsReg(results);
        }
    }

    private void resultsReg(Result[] results) {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        int id_project = settings.getInt("id_project", 0);
        Cursor c = null;
        Result[]  resultsBD = null;

        try{
            String[] projection = {
                    sensewareDataSource.Result.COLUMN_NAME_ID_RESULT,
                    sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT,
                    sensewareDataSource.Result.COLUMN_NAME_ID_LESSON,
                    sensewareDataSource.Result.COLUMN_NAME_ID_SOURCE,
                    sensewareDataSource.Result.COLUMN_NAME_RESULT,
                    sensewareDataSource.Result.COLUMN_NAME_DATE,
                    sensewareDataSource.Result.COLUMN_NAME_ASSIGNED,
                    sensewareDataSource.Result.COLUMN_NAME_HIDDEN
            };

            String whereCol = sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT+ " = " + String.valueOf(id_project);

            c = db.query(
                    sensewareDataSource.Result.TABLE_NAME,      // The table to query
                    projection,                                 // The columns to return
                    whereCol,                                   // The columns for the WHERE clause
                    null,                                       // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    null                                        // The sort order
            );

            if(c.moveToFirst()){
                int i = 0;
                resultsBD = new Result[c.getCount()];

                do{
                    Result r = new Result(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getString(4), c.getString(5), c.getInt(6), c.getInt(7));

                   resultsBD[i] = r;

                    i++;

                }while (c.moveToNext());
            }


        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(c != null && !c.isClosed())
            {
                c.close();
                db.close();
            }
        }

        if(resultsBD!=null) {
            for (int i = 0; i < results.length; i++) {
                boolean existResult = false;

                for(int j=0; j<resultsBD.length; j++) {
                    if(resultsBD[j].getId_result() == results[i].getId_result())
                        existResult = true;
                }

                //Genera excepcion revisando
                if(!existResult) {
                    Result[] aux = new Result[1];
                    Result r = new Result(results[i].getId_result(), results[i].getId_project(), results[i].getId_lesson(), results[i].getId_source(), results[i].getResult(), results[i].getDate(), results[i].getAssigned(), results[i].getHidden() );
                    aux[0] = r;
                    saveResult(aux);
                }
            }
        }
        else {
            //save all results to DB
            saveResult(results);
        }


    }

    private void saveResult(Result[] results) {
        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        for (int i = 0; i < results.length; i++) {
            ContentValues values = new ContentValues();
            values.put(sensewareDataSource.Result.COLUMN_NAME_ID_RESULT, results[i].getId_result());
            values.put(sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT, results[i].getId_project());
            values.put(sensewareDataSource.Result.COLUMN_NAME_ID_LESSON, results[i].getId_lesson());
            values.put(sensewareDataSource.Result.COLUMN_NAME_ID_SOURCE, results[i].getId_source());
            values.put(sensewareDataSource.Result.COLUMN_NAME_RESULT, results[i].getResult());
            values.put(sensewareDataSource.Result.COLUMN_NAME_DATE, results[i].getDate());
            values.put(sensewareDataSource.Result.COLUMN_NAME_ASSIGNED, results[i].getAssigned());
            values.put(sensewareDataSource.Result.COLUMN_NAME_HIDDEN, results[i].getHidden());

            long newRowId;
            newRowId = db.insert(sensewareDataSource.Result.TABLE_NAME, null, values);
        }

        db.close();

    }




}