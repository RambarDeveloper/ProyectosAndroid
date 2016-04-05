package la.oja.senseware;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import org.json.JSONObject;

import la.oja.senseware.data.sensewareDataSource;
import la.oja.senseware.data.sensewareDbHelper;

/**
 * Created by jcoaks on 1/2/2016.
 */
public class SaveHook
{
    Context context;
    SharedPreferences settings;
    int idTmp = -1;
    ApiCall call;

    public SaveHook(Context context, ContentValues values_hook, SharedPreferences settings) {
        this.context = context;
        this.settings = settings;
        sensewareDbHelper sDbHelper = new sensewareDbHelper(context);
        SQLiteDatabase db = sDbHelper.getWritableDatabase();
        long newRowId = db.insert(sensewareDataSource.Hook.TABLE_NAME, null, values_hook);

        db.close();

        new HttpRequestSendHooks().execute();
    }

    private class HttpRequestSendHooks extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            sensewareDbHelper sDbHelper = new sensewareDbHelper(context);
            SQLiteDatabase db = sDbHelper.getReadableDatabase();
            Cursor c = null;

            try {
                String[] projection = {
                        sensewareDataSource.Hook._ID,
                        sensewareDataSource.Hook.COLUMN_NAME_HOOK,
                        sensewareDataSource.Hook.COLUMN_NAME_DATA,
                        sensewareDataSource.Hook.COLUMN_NAME_TYPE,
                        sensewareDataSource.Hook.COLUMN_NAME_DATE,
                        sensewareDataSource.Hook.COLUMN_NAME_URL,
                        sensewareDataSource.Hook.COLUMN_NAME_UPLOAD
                };

                String whereCol = sensewareDataSource.Hook.COLUMN_NAME_UPLOAD + " = 0";

                c = db.query(sensewareDataSource.Hook.TABLE_NAME, projection, whereCol, null, null, null, null);

                try {
                    if (c.moveToFirst())
                    {

                        int id_project = 0;


                        do {
                            int id = c.getInt(0);
                            String data = c.getString(2);
                            String method = c.getString(3);
                            String url = c.getString(5);
                            String hook = c.getString(1);

                            if (url.equals(context.getString(R.string.urlAPI) +"project") && method.equals("POST")) {
                                data = getNewData(data, true, id_project);

                            }

                            else {
                                data = getNewData(data, false, id_project);
                            }
                            try {
                                call = new ApiCall(context);

                                String resp = "";
                                if (method.equals("POST"))
                                    resp = call.callPost(url, data);
                                if (method.equals("GET"))
                                    resp = call.callGet(url);
                                if (method.equals("PUT"))
                                    resp = call.callPut(url, data);

                                //get response obtained of call api

                                //convert the response from string to JsonObject
                                JSONObject obj = new JSONObject(resp);
                                int status = obj.getInt("status");
                                String message = obj.getString("message");

                                if (status == 200 && message.equals("OK")) {

                                    // New value for one column
                                    ContentValues values = new ContentValues();
                                    values.put(sensewareDataSource.Hook.COLUMN_NAME_UPLOAD, 1);

                                    // Which row to update, based on the ID
                                    String selection = sensewareDataSource.Hook._ID + "=?";
                                    String[] selectionArgs = {String.valueOf(id)};

                                    int count = db.update(
                                            sensewareDataSource.Hook.TABLE_NAME,
                                            values,
                                            selection,
                                            selectionArgs);

                                    //if the hook to process is new project

                                    if (url.equals(context.getString(R.string.urlAPI) +"project")) {
                                        JSONObject result = new JSONObject(obj.get("result").toString());
                                        id_project = result.getInt("id_project");
                                        String na_project = result.getString("na_project");
                                        updateProject(id_project, idTmp);
                                        c.close();
                                        db.close();
                                        //new HttpRequestSendHooks();

                                    }
                                    if(url.equals(context.getString(R.string.urlAPI) +"result")) {
                                        JSONObject result = new JSONObject(obj.get("result").toString());
                                        int id_result = result.getInt("id_result");
                                        int project = result.getInt("id_project");
                                        int id_lesson = result.getInt("id_lesson");
                                        String value = result.getString("result");
                                        int id_source = result.getInt("id_source");

                                        ContentValues valuesResult = new ContentValues();
                                        valuesResult.put(sensewareDataSource.Result.COLUMN_NAME_ID_RESULT, id_result);
                                        valuesResult.put(sensewareDataSource.Result.COLUMN_NAME_ID_SOURCE, id_source);

                                        String where = sensewareDataSource.Result.COLUMN_NAME_ID_RESULT+ "=? AND " +
                                                sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT +" =? AND " +
                                                sensewareDataSource.Result.COLUMN_NAME_ID_LESSON +" =? AND " +
                                                sensewareDataSource.Result.COLUMN_NAME_RESULT +" =?";
                                        String[] args = {String.valueOf(0), String.valueOf(project), String.valueOf(id_lesson), value};

                                        int count2 = db.update(
                                                sensewareDataSource.Result.TABLE_NAME,
                                                valuesResult,
                                                where,
                                                args);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } while (c.moveToNext());
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (c != null && !c.isClosed()) {
                        c.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (c.isClosed()) {
                   db.close();
                }
            }
            return null;
        }
    }

    private String getNewData(String data, boolean isNewProject, int id_project) {

        String aux = data.substring(1,data.length()-1);
        String[] valuesData = aux.split(",");
        String newData = null;

        newData = "{";

        for (int i = 0; i < valuesData.length; i++) {
            String[] item = valuesData[i].split(":");
            if (item[0].equals("'id_tmp'")) {
                if(isNewProject)
                {

                    item[1] = item[1].replace(" ", "");
                    idTmp = Integer.valueOf(item[1]);

                }
                else {
                    if(id_project == 0)
                    {
                        int idProject = settings.getInt("id_project", 0);
                        newData += "'id_project' : " + idProject + ",";
                    }
                    else
                        newData += "'id_project' : " + id_project + ",";
                }

            }else if(item[0].equals("'id_project'"))
            {
                item[1] = item[1].replace(" ", "");
                int id = Integer.valueOf(item[1]);
                if(id==0){
                    int idProject = settings.getInt("id_project", 0);
                    newData += "'id_project' : " + idProject + ",";
                }
                else{
                    newData += "'id_project' : " + id + ",";
                }
            }
            else {
                newData += valuesData[i];
                if (i < (valuesData.length - 1))
                    newData += ",";
            }
        }
            newData += "}";

        return newData;
    }

    private void updateProject(int id_project, int idTmp) {

        SharedPreferences.Editor editor = settings.edit();

        int idProject = settings.getInt("id_project", 0);

        if (idProject == idTmp) {
            editor.putInt("id_project", id_project);
            editor.putBoolean("idTmp", false);
            editor.commit();
        }

        sensewareDbHelper sDbHelper = new sensewareDbHelper(context);
        SQLiteDatabase db = sDbHelper.getReadableDatabase();

        String select = sensewareDataSource.Project.COLUMN_NAME_ID_TMP + "=?";
        String[] selectArg = {String.valueOf(idTmp)};

        ContentValues updateProject = new ContentValues();
        updateProject.put(sensewareDataSource.Project.COLUMN_NAME_ID_PROJECT, id_project);

        int count = db.update(
                sensewareDataSource.Project.TABLE_NAME,
                updateProject,
                select,
                selectArg);

        String select2 = sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT + "=?";

        ContentValues updateResult = new ContentValues();
        updateResult.put(sensewareDataSource.Result.COLUMN_NAME_ID_PROJECT, id_project);

        int count2 = db.update(
                sensewareDataSource.Result.TABLE_NAME,
                updateResult,
                select2,
                selectArg);

        String select3 = sensewareDataSource.History.COLUMN_NAME_ID_PROJECT + "=?";
        ContentValues updateHistory = new ContentValues();
        updateHistory.put(sensewareDataSource.History.COLUMN_NAME_ID_PROJECT, id_project);

        int count3 = db.update(
                sensewareDataSource.History.TABLE_NAME,
                updateHistory,
                select3,
                selectArg);


        db.close();

        return;
    }
}
