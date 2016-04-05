package la.oja.senseware;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import la.oja.senseware.R;
import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;

public class NewProjectActivity extends AppCompatActivity  {

    private EditText mProjectView;
    private TextView title;
    private Button btnNewProject;
    String utms;
    private ImageButton btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        Typeface ultralight= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        mProjectView = (EditText) findViewById(R.id.project);
        btnNewProject = (Button) findViewById(R.id.btnNewProject);
        title = (TextView) findViewById(R.id.title);
        btnClear = (ImageButton) findViewById(R.id.project_clear);

        mProjectView.setTypeface(thin);
        title.setTypeface(light);
        btnNewProject.setTypeface(thin);

        ImageButton close = (ImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewProjectActivity.this.finish();
            }
        });

        btnNewProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProject();
            }
        });

        mProjectView.setCursorVisible(true);
        mProjectView.setFocusableInTouchMode(true);
        mProjectView.requestFocus();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mProjectView, InputMethodManager.SHOW_FORCED);


        mProjectView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.project);
                tv.setText("");
                btnClear.setVisibility(View.GONE);
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createProject()
    {
        mProjectView.setError(null);

        String project = mProjectView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(project)) {
            mProjectView.setError(getString(R.string.error_empty_project));
            focusView = mProjectView;
            cancel = true;
        }

        if(cancel)
        {
            focusView.requestFocus();
        }
        else
        {
            SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
            SharedPreferences.Editor editor = settings.edit();

            int id_user = settings.getInt("id_user", 0);
            int id_project = settings.getInt("id_project", 0);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(c.getTime());
            String url =  getString(R.string.urlAPI) + "project";

            String utm_source = settings.getString("utm_source", "");
            String utm_medium = settings.getString("utm_medium", "");
            String utm_term = settings.getString("utm_term", "");
            String utm_content = settings.getString("utm_content", "");
            String utm_campaign = settings.getString("utm_campaign", "");
            utms = "app : 'Android'";

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

            //ID temporal to project
            Random r = new Random();
            int tmp = r.nextInt(id_project - id_user) + id_project;

            String data = "{'id_tmp':"+ tmp +", 'id_user': "+ id_user + ", 'na_project': '"+ project +"', 'create': '"+ date +"', " + "utms: [{" + utms + "}]}";
            ContentValues values_hook = new ContentValues();
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "project");
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "POST");
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

            SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);

            ContentValues values_project = new ContentValues();
            values_project.put(sensewareDataSource.Project.COLUMN_NAME_ID_PROJECT, 0);
            values_project.put(sensewareDataSource.Project.COLUMN_NAME_ID_USER, id_user);
            values_project.put(sensewareDataSource.Project.COLUMN_NAME_NA_PROJECT, project);
            values_project.put(sensewareDataSource.Project.COLUMN_NAME_CREATED, date);
            values_project.put(sensewareDataSource.Project.COLUMN_NAME_ID_TMP, tmp);

            sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
            SQLiteDatabase db = sDbHelper.getWritableDatabase();
            long newRowId2 = db.insert(sensewareDataSource.Project.TABLE_NAME, null, values_project);

            editor.putInt("id_project", tmp);
            editor.putBoolean("idTmp", true);
            editor.putInt("current", 1);
            editor.putInt("day", 1);
            editor.putBoolean("newProject", true);
            editor.commit();

            NewProjectActivity.this.finish();

        }
    }


}
