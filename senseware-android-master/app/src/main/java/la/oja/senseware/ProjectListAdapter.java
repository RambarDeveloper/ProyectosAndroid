package la.oja.senseware;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import la.oja.senseware.R;
import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;
import la.oja.senseware.models.Project;

/**
 * Created by Administrador on 21-01-2016.
 */
public class ProjectListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final Typeface font;
    private final Project[] projects;

    public ProjectListAdapter(Activity context, String[] itemname, Typeface font, Project[] projects) {
        super(context, R.layout.list_row, itemname);
        // TODO Auto-generated constructor stub

        this.context = context;
        this.itemname = itemname;
        this.font = font;
       // this.projects = new Project[projects.length];
        this.projects = projects;

    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.list_project_row, null, true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);

        txtTitle.setTypeface(font);
        txtTitle.setText(itemname[position]);

        ImageButton edit = (ImageButton) rowView.findViewById(R.id.edit);
        ImageButton next = (ImageButton) rowView.findViewById(R.id.select);

        SharedPreferences settings = context.getSharedPreferences("ActivitySharedPreferences_data", 0);
        int id_project = settings.getInt("id_project", 0);

        if(projects[position].getId_project()==id_project)
        {
            txtTitle.setTextColor(Color.parseColor("#CC1D1D"));
        }

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, EditProjectActivity.class);
                intent.putExtra("project", itemname[position]);
                intent.putExtra("id_project", projects[position].getId_project());
                context.startActivity(intent);
                context.finish();

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Senseware");
                alertDialog.setMessage("Esta seguro de cambiar de proyecto");
                alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int POSITIVE) {
                        changeProject(projects[position].getId_project(), projects[position].getId_tmp());
                        dialog.cancel();
                        Intent intent = context.getIntent();
                        context.finish();
                        context.startActivity(intent);

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
        });

        return rowView;
    }

    private void changeProject(int id_project, int idTmp) {

        SharedPreferences settings = context.getSharedPreferences("ActivitySharedPreferences_data", 0);
        SharedPreferences.Editor editor = settings.edit();

        if(id_project != 0)
            editor.putInt("id_project", id_project);
        else{
            editor.putInt("id_project", idTmp);
            editor.putBoolean("idTmp", true);
            id_project = idTmp;
        }

        sensewareDbHelper sDbHelper = new sensewareDbHelper(context);
        SQLiteDatabase db = sDbHelper.getWritableDatabase();

        int current = 1;
        int day = 1;

        Cursor c = null;

        try {
            String[] projection = {
                   "MAX(" + sensewareDataSource.Result.COLUMN_NAME_ID_LESSON + ")",
            };

            String whereCol = sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT + " =  " + id_project;

            c = db.query(
                    sensewareDataSource.Result.TABLE_NAME,      // The table to query
                    projection,                                 // The columns to return
                    whereCol,                                   // The columns for the WHERE clause
                    null,                                       // The values for the WHERE clause
                    null,                                       // don't group the rows
                    null,                                       // don't filter by row groups
                    null                                        // The sort order
            );

            if(c.moveToFirst())
            {
                int id_lesson = c.getInt(0);

                Log.i("idLesson", String.valueOf(id_lesson));

                String[] projection2 = {
                        sensewareDataSource.Lesson._ID,
                        sensewareDataSource.Lesson.COLUMN_NAME_ID_DAY,
                        sensewareDataSource.Lesson.COLUMN_NAME_POSITION

                };

                String where = sensewareDataSource.Lesson.COLUMN_NAME_ID_LESSON + " = " + String.valueOf(id_lesson);

                Cursor cl = db.query(
                        sensewareDataSource.Lesson.TABLE_NAME,      // The table to query
                        projection2,                                 // The columns to return
                        where,                                   // The columns for the WHERE clause
                        null,                                       // The values for the WHERE clause
                        null,                                       // don't group the rows
                        null,                                       // don't filter by row groups
                        null                                        // The sort order
                );

                if(cl.moveToFirst()){
                   current  = cl.getInt(2)+1;
                   day = cl.getInt(1);
                }

            }
            else{
                current =1;
                day = 1;
            }


        }catch (Exception e){
        //   e.printStackTrace();
        }
        finally {
            if(c != null && !c.isClosed()){
                c.close();
            }
        }

        editor.putInt("current", current);
        editor.putInt("day", day);
        editor.commit();
    }
}
