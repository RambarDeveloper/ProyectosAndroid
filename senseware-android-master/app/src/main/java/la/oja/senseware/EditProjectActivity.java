package la.oja.senseware;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;

public class EditProjectActivity extends AppCompatActivity {

    private EditText mProjectView;
    private Button btnEditProject;
    private TextView title;
    private ImageButton btnClear;
    private int id_project;
    private String projectOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        Typeface ultralight= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        mProjectView = (EditText) findViewById(R.id.name_project);
        btnEditProject = (Button) findViewById(R.id.btnEditProject);
        title = (TextView) findViewById(R.id.title);
        btnClear = (ImageButton) findViewById(R.id.project_clear);

        mProjectView.setTypeface(thin);
        title.setTypeface(light);
        btnEditProject.setTypeface(thin);

        projectOld = getIntent().getStringExtra("project");
        id_project = getIntent().getIntExtra("id_project", -1);

        if(!TextUtils.isEmpty(projectOld)){
            TextView tv = (TextView) findViewById(R.id.name_project);
            tv.setHint(projectOld);
            btnClear.setVisibility(View.VISIBLE);
        }

        ImageButton close = (ImageButton) findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditProjectActivity.this.finish();
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
                TextView tv = (TextView) findViewById(R.id.name_project);
                tv.setText("");
                btnClear.setVisibility(View.GONE);
            }
        });

        btnEditProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProject();
            }
        });
    }

    private void editProject() {

        mProjectView.setError(null);
        boolean cancel = false;
        View focusView = null;

        String project = mProjectView.getText().toString();

        if (TextUtils.isEmpty(project)) {
            mProjectView.setError(getString(R.string.error_edit_project));
            focusView = mProjectView;
            cancel = true;
            YoYo.with(Techniques.Shake).playOn(findViewById(R.id.name_project));
        }

        if(cancel)
            focusView.requestFocus();
        else
        {
            SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
            SharedPreferences.Editor editor = settings.edit();

            int id_user = settings.getInt("id_user", 0);

            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = sdf.format(c.getTime());
            String url =  getString(R.string.urlAPI) +"project/"+id_project;

            String utm_source = settings.getString("utm_source", "");
            String utm_medium = settings.getString("utm_medium", "");
            String utm_term = settings.getString("utm_term", "");
            String utm_content = settings.getString("utm_content", "");
            String utm_campaign = settings.getString("utm_campaign", "");
            String utms = "'app': 'Android'";

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

            String data = "{'na_project': '"+ project +"' }";
            ContentValues values_hook = new ContentValues();
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "editProject");
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "PUT");
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
            values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

            sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
            SQLiteDatabase db = sDbHelper.getWritableDatabase();

            String select = sensewareDataSource.Project.COLUMN_NAME_ID_PROJECT + "=?";
            String[] selectArg = {String.valueOf(id_project)};

            ContentValues updateProject = new ContentValues();
            updateProject.put(sensewareDataSource.Project.COLUMN_NAME_NA_PROJECT, project);

            int count = db.update(
                    sensewareDataSource.Project.TABLE_NAME,
                    updateProject,
                    select,
                    selectArg);

            if(count > 0) {
                SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);

                EditProjectActivity.this.finish();
                Intent intent = new Intent(EditProjectActivity.this, MyProjectsActivity.class);
                startActivity(intent);
            }
            else
            {
                mProjectView.setError("Ocurrio un error actualizando el proyecto");
                focusView = mProjectView;
                YoYo.with(Techniques.Shake).playOn(findViewById(R.id.name_project));
                focusView.requestFocus();
            }



        }

    }
}
