package la.oja.senseware;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;

public class EditResultActivity extends AppCompatActivity {

    private TextView title;
    private TextView question;
    private EditText answer;
    private Button btnEditResult;
    private int id = 0;
    private int id_result = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_result);

        Typeface ultralight= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Ultralight.ttf");
        Typeface light= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Light.ttf");
        Typeface thin= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Display-Thin.ttf");
        Typeface regular= Typeface.createFromAsset(getAssets(), "fonts/SF-UI-Text-Regular.ttf");

        title = (TextView) findViewById(R.id.title);
        question = (TextView) findViewById(R.id.question);
        answer = (EditText) findViewById(R.id.answer);
        btnEditResult = (Button) findViewById(R.id.btnEditResult);

        question.setTypeface(thin);
        answer.setTypeface(thin);
        title.setTypeface(light);
        btnEditResult.setTypeface(thin);

        String q = getIntent().getStringExtra("question");
        String a = getIntent().getStringExtra("answer");
        id = getIntent().getIntExtra("_ID", 0);

        if(!TextUtils.isEmpty(q)){
            question.setText(q);
        }

        if(!TextUtils.isEmpty(a)){
            answer.setText(a);
        }

        btnEditResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                editResult();
            }
        });

    }

    private void editResult() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(c.getTime());
        String url =  getString(R.string.urlAPI) + "result/"+id_result;

        String data = "{'result': '"+ answer.getText() +"'}";

        ContentValues values_hook = new ContentValues();
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATA, data);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_DATE, date);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_HOOK, "editResult");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_TYPE, "PUT");
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 0);
        values_hook.put(sensewareDataSource.Hook.COLUMN_NAME_URL, url);

        sensewareDbHelper sDbHelper = new sensewareDbHelper(getApplicationContext());
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        String select = sensewareDataSource.Result._ID + "=?";
        String[] selectArg = {String.valueOf(id)};

        ContentValues updateResult = new ContentValues();
        updateResult.put(sensewareDataSource.Result.COLUMN_NAME_RESULT, answer.getText().toString());

        int count = db.update(
                sensewareDataSource.Project.TABLE_NAME,
                updateResult,
                select,
                selectArg);

        SharedPreferences settings = getSharedPreferences("ActivitySharedPreferences_data", 0);
        SaveHook obj = new SaveHook(getApplicationContext(), values_hook, settings);
    }
}
