package la.oja.senseware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import la.oja.senseware.R;
import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;
import la.oja.senseware.models.Project;

public class MyProjectsActivity extends AppCompatActivity {

    protected Project[] projects;
    protected String[] project_name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_projects);

        ImageButton close = (ImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyProjectsActivity.this.finish();

                            }
        });

        Button newProject = (Button) findViewById(R.id.btnNewProject);
        newProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MyProjectsActivity.this.finish();
                Intent intent = new Intent(getApplicationContext(), NewProjectActivity.class);
                startActivity(intent);
            }
        });

        loadProjectsBD();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadProjects();

    }

    private void loadProjectsBD() {

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        int id_user = settings.getInt("id_user", 0);
        Cursor c = null;

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
            projects = new Project[c.getCount()];
            project_name = new String[c.getCount()];
            int i = 0;
            do {
                Project p = new Project(c.getInt(1), c.getInt(2), c.getString(3), c.getString(4));

                projects[i] = p;
                project_name[i]= p.getNa_project();


                i++;

            } while (c.moveToNext());

            c.close();
            db.close();

        }

    }

    private void loadProjects() {

        if(project_name == null)
        {

            Typeface ultralight = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
            TextView noProject = (TextView) findViewById(R.id.noProject);
            noProject.setVisibility(View.VISIBLE);
        }
        else {

            Typeface ultralight = Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
            ProjectListAdapter adapter = new ProjectListAdapter(MyProjectsActivity.this, project_name, ultralight, projects);
            ListView list = (ListView) findViewById(R.id.listProjects);
            list.setAdapter(adapter);

            final ImageButton edit = (ImageButton) findViewById(R.id.edit);
            final ImageButton next = (ImageButton) findViewById(R.id.select);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });
        }

        return;
    }
}
