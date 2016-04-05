package la.oja.senseware;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;
import la.oja.senseware.models.Project;
import la.oja.senseware.models.Result;

public class HistoryActivity extends AppCompatActivity {

    List <Map <String, String> > results = new ArrayList<>();
    String[] answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ImageButton close = (ImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HistoryActivity.this.finish();

            }
        });

        String project = selectcNameProejct();

        Typeface ultralight = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(ultralight);

        TextView naProject = (TextView) findViewById(R.id.na_project);
        naProject.setText(project);
        naProject.setTypeface(light);

        loadResultBD();

    }

    @Override
    protected void onStart() {
        super.onStart();

        loadResult();
    }

    private String selectcNameProejct() {

        String na_project = null;

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        int id_project = settings.getInt("id_project", 0);
        Cursor c = null;
        try {
            String[] projection = {
                    sensewareDataSource.Project._ID,
                    sensewareDataSource.Project.COLUMN_NAME_NA_PROJECT,

            };

            String whereCol = sensewareDataSource.Project.COLUMN_NAME_ID_PROJECT + " = " + String.valueOf(id_project);

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
                na_project = c.getString(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(c != null && !c.isClosed()){
                c.close();
                db.close();
            }
        }

        return na_project;
    }

    private void loadResultBD() {

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        int id_project = settings.getInt("id_project", 0);
        Cursor c = null;
        try {
            final String myQuery = "SELECT r." +sensewareDataSource.Result._ID +", " + sensewareDataSource.Result.COLUMN_NAME_RESULT
                    + ", "+ sensewareDataSource.Result.COLUMN_NAME_DATE +", " + sensewareDataSource.Lesson.COLUMN_NAME_SUBTITLE
                    + " FROM " + sensewareDataSource.Result.TABLE_NAME + " r INNER JOIN " + sensewareDataSource.Lesson.TABLE_NAME
                    + " l ON r." + sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + " = l." + sensewareDataSource.Lesson.COLUMN_NAME_ID_LESSON
                    + " WHERE r." + sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT +" = ?";


            String[] params = {String.valueOf(id_project)};

            c = db.rawQuery(myQuery, params);

            if (c.moveToFirst()) {
                int i = 0;
                answer = new String[c.getCount()];
                do{
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("_ID", String.valueOf(c.getInt(0)));
                    map.put("answer", c.getString(1));
                    map.put("date", c.getString(2));
                    map.put("question", c.getString(3));

                    answer[i] = c.getString(1);

                    results.add(i, map);
                    i++;

                }while (c.moveToNext());


            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(c != null && !c.isClosed()){
                c.close();
                db.close();
            }
        }


    }

    private void loadResult() {

        if(results.isEmpty())
        {
            Typeface ultralight = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
            TextView noResult = (TextView) findViewById(R.id.noResult);
            noResult.setVisibility(View.VISIBLE);
            noResult.setTypeface(ultralight);
        }
        else{
            Typeface thin = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
            ResultListAdapter adapter = new ResultListAdapter(HistoryActivity.this, answer, thin, results);
            ListView list = (ListView) findViewById(R.id.listHistory);
            list.setAdapter(adapter);



        }
    }




}
